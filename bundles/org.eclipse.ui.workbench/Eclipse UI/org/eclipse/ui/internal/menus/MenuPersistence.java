/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
final class MenuPersistence extends RegistryPersistence {

	private MApplication application;
	private IEclipseContext appContext;
	private ArrayList<MenuAdditionCacheEntry> cacheEntries = new ArrayList<MenuAdditionCacheEntry>();
	private ArrayList<ActionSet> actionContributions = new ArrayList<ActionSet>();
	private ArrayList<EditorAction> editorActionContributions = new ArrayList<EditorAction>();
	private ArrayList<ViewAction> viewActionContributions = new ArrayList<ViewAction>();
	private ArrayList<MMenuContribution> menuContributions = new ArrayList<MMenuContribution>();
	private ArrayList<MToolBarContribution> toolBarContributions = new ArrayList<MToolBarContribution>();
	private ArrayList<MTrimContribution> trimContributions = new ArrayList<MTrimContribution>();
	private final Comparator<IConfigurationElement> comparer = new Comparator<IConfigurationElement>() {
		public int compare(IConfigurationElement c1, IConfigurationElement c2) {
			return c1.getContributor().getName().compareToIgnoreCase(c2.getContributor().getName());
		}
	};

	/**
	 * Constructs a new instance of {@link MenuPersistence}.
	 * 
	 * @param workbenchMenuService
	 * 
	 * @param workbenchMenuService
	 *            The menu service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	MenuPersistence(MApplication application, IEclipseContext appContext) {
		this.application = application;
		this.appContext = appContext;
	}

	public final void dispose() {
		ControlContributionRegistry.clear();
		application.getMenuContributions().removeAll(menuContributions);
		application.getToolBarContributions().removeAll(toolBarContributions);
		application.getTrimContributions().removeAll(trimContributions);
		menuContributions.clear();
		cacheEntries.clear();
		actionContributions.clear();
		editorActionContributions.clear();
		viewActionContributions.clear();
		super.dispose();
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		/*
		 * TODO Menus will need to be re-read (i.e., re-verified) if any of the
		 * menu extensions change (i.e., menus), or if any of the command
		 * extensions change (i.e., action definitions).
		 */
		return false;
	}

	public boolean menusNeedUpdating(final IRegistryChangeEvent event) {
		final IExtensionDelta[] menuDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_MENUS);
		if (menuDeltas.length == 0) {
			return false;
		}

		return true;
	}

	/**
	 * <p>
	 * Reads all of the menu elements and action sets from the registry.
	 * </p>
	 * <p>
	 * TODO Add support for modifications.
	 * </p>
	 */
	protected final void read() {
		super.read();

		// Read legacy 3.2 'trim' additions
		readTrimAdditions();

		ArrayList<MMenuContribution> menuC = new ArrayList<MMenuContribution>();
		ArrayList<MToolBarContribution> toolbarC = new ArrayList<MToolBarContribution>();
		ArrayList<MTrimContribution> trimC = new ArrayList<MTrimContribution>();
		// read the 3.3 menu additions
		readAdditions(menuC, toolbarC, trimC);

		// convert actionSets to MenuContributions
		readActionSets(menuC, toolbarC, trimC);

		readEditorActions(menuC, toolbarC, trimC);

		readViewActions(menuC, toolbarC, trimC);

		// can I rationalize them?
		ContributionsAnalyzer.mergeContributions(menuC, menuContributions);
		application.getMenuContributions().addAll(menuContributions);

		ContributionsAnalyzer.mergeToolBarContributions(toolbarC, toolBarContributions);
		application.getToolBarContributions().addAll(toolBarContributions);
		ContributionsAnalyzer.mergeTrimContributions(trimC, trimContributions);
		application.getTrimContributions().addAll(trimContributions);
	}

	//
	// 3.3 menu extension code
	//

	public void readTrimAdditions() {
	}

	public void readAdditions(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions,
			ArrayList<MTrimContribution> trimContributions) {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

		final IConfigurationElement[] menusExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_MENUS);

		// Create a cache entry for every menu addition;
		for (int i = 0; i < menusExtensionPoint.length; i++) {
			if (PL_MENU_CONTRIBUTION.equals(menusExtensionPoint[i].getName())) {
				configElements.add(menusExtensionPoint[i]);
			}
		}
		Collections.sort(configElements, comparer);

		Iterator<IConfigurationElement> i = configElements.iterator();
		while (i.hasNext()) {
			final IConfigurationElement configElement = i.next();

			if (isProgramaticContribution(configElement)) {
				// newFactory = new ProxyMenuAdditionCacheEntry(
				// configElement
				// .getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI),
				// configElement.getNamespaceIdentifier(), configElement);\
				E4Util.unsupported("Programmatic Contribution Factories not supported"); //$NON-NLS-1$

			} else {
				MenuAdditionCacheEntry menuContribution = new MenuAdditionCacheEntry(application,
						appContext, configElement,
						configElement.getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI),
						configElement.getNamespaceIdentifier());
				cacheEntries.add(menuContribution);
				menuContribution.addToModel(menuContributions, toolBarContributions,
						trimContributions);
			}
		}
	}

	/**
	 * Return whether or not this contribution is programmatic (ie: has a class
	 * attribute).
	 * 
	 * @param menuAddition
	 * @return whether or not this contribution is programamtic
	 * @since 3.5
	 */
	private boolean isProgramaticContribution(IConfigurationElement menuAddition) {
		return menuAddition.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS) != null;
	}

	private void readActionSets(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions,
			ArrayList<MTrimContribution> trimContributions) {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

		configElements.addAll(Arrays.asList(registry
				.getConfigurationElementsFor(IWorkbenchRegistryConstants.EXTENSION_ACTION_SETS)));

		Collections.sort(configElements, comparer);

		HashMap<String, ArrayList<MToolBarContribution>> postProcessing = new HashMap<String, ArrayList<MToolBarContribution>>();
		for (IConfigurationElement element : configElements) {
			ArrayList<MToolBarContribution> localToolbarContributions = new ArrayList<MToolBarContribution>();
			ActionSet actionSet = new ActionSet(application, appContext, element);
			actionContributions.add(actionSet);
			actionSet.addToModel(menuContributions, localToolbarContributions, trimContributions);
			toolBarContributions.addAll(localToolbarContributions);
			postProcessing.put(actionSet.getId(), localToolbarContributions);
		}
		for (Entry<String, ArrayList<MToolBarContribution>> entry : postProcessing.entrySet()) {
			for (MToolBarContribution contribution : entry.getValue()) {
				String targetParentId = contribution.getParentId();
				if (entry.getKey().equals(targetParentId)) {
					continue;
				}
				ArrayList<MToolBarContribution> adjunctContributions = postProcessing
						.get(targetParentId);
				if (adjunctContributions == null) {
					continue;
				}
				boolean processed = false;
				Iterator<MToolBarContribution> i = adjunctContributions.iterator();
				while (i.hasNext() && !processed) {
					MToolBarContribution adjunctContribution = i.next();
					if (targetParentId.equals(adjunctContribution.getParentId())) {
						for (MToolBarElement item : adjunctContribution.getChildren()) {
							if (!(item instanceof MToolBarSeparator) && item.getElementId() != null) {
								processed = true;
								contribution.setPositionInParent("before=" + item.getElementId()); //$NON-NLS-1$
								break;
							}
						}
					}
				}
			}
		}
		postProcessing.clear();
	}

	private void readEditorActions(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions,
			ArrayList<MTrimContribution> trimContributions) {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

		configElements
				.addAll(Arrays.asList(registry
						.getConfigurationElementsFor(IWorkbenchRegistryConstants.EXTENSION_EDITOR_ACTIONS)));

		Collections.sort(configElements, comparer);

		for (IConfigurationElement element : configElements) {
			for (IConfigurationElement child : element.getChildren()) {
				if (child.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION)) {
					EditorAction editorAction = new EditorAction(application, appContext, element,
							child);
					editorActionContributions.add(editorAction);
					editorAction.addToModel(menuContributions, toolBarContributions,
							trimContributions);
				}
			}
		}
	}

	private void readViewActions(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions,
			ArrayList<MTrimContribution> trimContributions) {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

		configElements.addAll(Arrays.asList(registry
				.getConfigurationElementsFor(IWorkbenchRegistryConstants.EXTENSION_VIEW_ACTIONS)));

		Collections.sort(configElements, comparer);

		for (IConfigurationElement element : configElements) {
			for (IConfigurationElement child : element.getChildren()) {
				if (child.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION)) {
					ViewAction viewAction = new ViewAction(application, appContext, element, child,
							false);
					viewActionContributions.add(viewAction);
					viewAction.addToModel(menuContributions, toolBarContributions,
							trimContributions);
				} else if (child.getName().equals(IWorkbenchRegistryConstants.TAG_MENU)) {
					ViewAction viewAction = new ViewAction(application, appContext, element, child,
							true);
					viewActionContributions.add(viewAction);
					viewAction.addToModel(menuContributions, toolBarContributions,
							trimContributions);
				}
			}
		}
	}
}
