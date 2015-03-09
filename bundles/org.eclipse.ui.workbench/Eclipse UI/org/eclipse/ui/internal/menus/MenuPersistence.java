/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
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
final public class MenuPersistence extends RegistryPersistence {

	private MApplication application;
	private IEclipseContext appContext;
	private ArrayList<MenuAdditionCacheEntry> cacheEntries = new ArrayList<MenuAdditionCacheEntry>();

	private ArrayList<MMenuContribution> menuContributions = new ArrayList<MMenuContribution>();
	private ArrayList<MToolBarContribution> toolBarContributions = new ArrayList<MToolBarContribution>();
	private ArrayList<MTrimContribution> trimContributions = new ArrayList<MTrimContribution>();

	private final Comparator<IConfigurationElement> comparer = new Comparator<IConfigurationElement>() {
		@Override
		public int compare(IConfigurationElement c1, IConfigurationElement c2) {
			return c1.getContributor().getName().compareToIgnoreCase(c2.getContributor().getName());
		}
	};
	private Pattern contributorFilter;

	/**
	 * @param application
	 * @param appContext
	 */
	public MenuPersistence(MApplication application, IEclipseContext appContext) {
		this.application = application;
		this.appContext = appContext;
	}

	public MenuPersistence(MApplication application, IEclipseContext appContext, String filterRegex) {
		this(application, appContext);
		contributorFilter = Pattern.compile(filterRegex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.services.RegistryPersistence#dispose()
	 */
	@Override
	public void dispose() {
		ControlContributionRegistry.clear();
		application.getMenuContributions().removeAll(menuContributions);
		application.getToolBarContributions().removeAll(toolBarContributions);
		application.getTrimContributions().removeAll(trimContributions);
		menuContributions.clear();
		cacheEntries.clear();
		super.dispose();
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.tests.workbench.RegistryPersistence#isChangeImportant
	 * (org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	@Override
	protected boolean isChangeImportant(IRegistryChangeEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public void reRead() {
		read();
	}

	@Override
	protected final void read() {
		super.read();

		readAdditions();

		ArrayList<MMenuContribution> tmp = new ArrayList<MMenuContribution>(menuContributions);
		menuContributions.clear();
		ContributionsAnalyzer.mergeContributions(tmp, menuContributions);
		application.getMenuContributions().addAll(menuContributions);

		ArrayList<MToolBarContribution> tmpToolbar = new ArrayList<MToolBarContribution>(
				toolBarContributions);
		toolBarContributions.clear();
		ContributionsAnalyzer.mergeToolBarContributions(tmpToolbar, toolBarContributions);
		application.getToolBarContributions().addAll(toolBarContributions);

		ArrayList<MTrimContribution> tmpTrim = new ArrayList<MTrimContribution>(trimContributions);
		trimContributions.clear();
		ContributionsAnalyzer.mergeTrimContributions(tmpTrim, trimContributions);
		application.getTrimContributions().addAll(trimContributions);
	}

	private void readAdditions() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

		final IConfigurationElement[] menusExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_MENUS);

		// Create a cache entry for every menu addition;
		for (int i = 0; i < menusExtensionPoint.length; i++) {
			if (PL_MENU_CONTRIBUTION.equals(menusExtensionPoint[i].getName())) {
				if (contributorFilter == null
						|| contributorFilter.matcher(
								menusExtensionPoint[i].getContributor().getName()).matches()) {
					configElements.add(menusExtensionPoint[i]);
				}
			}
		}
		Collections.sort(configElements, comparer);
		Iterator<IConfigurationElement> i = configElements.iterator();
		while (i.hasNext()) {
			final IConfigurationElement configElement = i.next();

			if (isProgramaticContribution(configElement)) {
				MenuFactoryGenerator gen = new MenuFactoryGenerator(application, appContext,
						configElement,
						configElement.getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI));
				gen.mergeIntoModel(menuContributions, toolBarContributions, trimContributions);
			} else {
				MenuAdditionCacheEntry menuContribution = new MenuAdditionCacheEntry(application,
						appContext, configElement,
						configElement.getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI),
						configElement.getNamespaceIdentifier());
				cacheEntries.add(menuContribution);
				menuContribution.mergeIntoModel(menuContributions, toolBarContributions,
						trimContributions);
			}
		}
	}


	private boolean isProgramaticContribution(IConfigurationElement menuAddition) {
		return menuAddition.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS) != null;
	}
}
