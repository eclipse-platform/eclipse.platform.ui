/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.navigator.wizards.CommonWizardDescriptor;
import org.eclipse.ui.internal.navigator.wizards.CommonWizardDescriptorManager;
import org.eclipse.ui.internal.navigator.wizards.WizardShortcutAction;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 *
 * Populates context menus with shortcut actions for defined wizards. Wizards
 * may be defined by any of the following extension points:
 * <ul>
 * <li><b>org.eclipse.ui.newWizards</b></li>
 * <li><b>org.eclipse.ui.importWizards</b></li>
 * <li><b>org.eclipse.ui.exportWizards</b></li>
 * </ul>
 * <p>
 * Here are the required steps for using this feature correctly:
 * </p>
 * <ol>
 * <li>Declare all new/import/export wizards from the extension points above, or
 * locate the existing wizards that you intend to reuse.</li>
 * <li>Declare <b>org.eclipse.ui.navigator.navigatorContent/commonWizard</b>
 * elements to identify which wizards should be associated with what items in
 * your viewer or navigator.</li>
 * <li>If you are using Resources in your viewer and have bound the resource
 * extension declared in <b>org.eclipse.ui.navigator.resources</b>, then you
 * will get most of this functionality for free.</li>
 * <li>Otherwise, you may choose to build your own custom menu. In which case,
 * you may instantiate this class, and hand it the menu or submenu that you want
 * to list out the available wizard shortcuts via
 * {@link WizardActionGroup#fillContextMenu(IMenuManager)}.</li>
 * </ol>
 *
 * @see PlatformUI#getWorkbench()
 * @see IWorkbench#getNewWizardRegistry()
 * @see IWorkbench#getImportWizardRegistry()
 * @see IWorkbench#getExportWizardRegistry()
 * @since 3.2
 *
 */
public final class WizardActionGroup extends ActionGroup {

	/**
	 * The type for commonWizard extensions with the value "new" for their type
	 * attribute.
	 */
	public static final String TYPE_NEW = "new"; //$NON-NLS-1$

	/**
	 * The type for commonWizard extensions with the value "new" for their type
	 * attribute.
	 */
	public static final String TYPE_IMPORT = "import"; //$NON-NLS-1$

	/**
	 * The type for commonWizard extensions with the value "new" for their type
	 * attribute.
	 */
	public static final String TYPE_EXPORT = "export"; //$NON-NLS-1$

	private static final CommonWizardDescriptor[] NO_DESCRIPTORS = new CommonWizardDescriptor[0];

	private static final String[] NO_IDS = new String[0];

	private CommonWizardDescriptor[] descriptors;

	/* a map of (id, IAction)-pairs. */
	private Map<String, IAction> actions;

	/*
	 * the window is passed to created WizardShortcutActions for the shell and
	 * selection service.
	 */
	private final IWorkbenchWindow window;

	/* the correct wizard registry for this action group (getRegistry()) */
	private final IWizardRegistry wizardRegistry;

	private boolean disposed = false;

	private final String type;

	private final INavigatorContentService contentService;

	private final Predicate<IWizardDescriptor> descriptorFilter;

	private final boolean useSeparators;

	/**
	 *
	 * @param aWindow
	 *            The window that will be used to acquire a Shell and a
	 *            Selection Service
	 * @param aWizardRegistry
	 *            The wizard registry will be used to locate the correct wizard
	 *            descriptions.
	 * @param aType
	 *            Indicates the value of the type attribute of the commonWizard
	 *            extension point. Use any of the TYPE_XXX constants defined on
	 *            this class.
	 * @see PlatformUI#getWorkbench()
	 * @see IWorkbench#getNewWizardRegistry()
	 * @see IWorkbench#getImportWizardRegistry()
	 * @see IWorkbench#getExportWizardRegistry()
	 */
	public WizardActionGroup(IWorkbenchWindow aWindow,
			IWizardRegistry aWizardRegistry, String aType) {
		this(aWindow, aWizardRegistry, aType, null);
	}


	/**
	 *
	 * @param aWindow
	 *            The window that will be used to acquire a Shell and a
	 *            Selection Service
	 * @param aWizardRegistry
	 *            The wizard registry will be used to locate the correct wizard
	 *            descriptions.
	 * @param aType
	 *            Indicates the value of the type attribute of the commonWizard
	 *            extension point. Use any of the TYPE_XXX constants defined on
	 *            this class.
	 * @param aContentService
	 * 			 The content service to use when deciding visibility.
	 * @see PlatformUI#getWorkbench()
	 * @see IWorkbench#getNewWizardRegistry()
	 * @see IWorkbench#getImportWizardRegistry()
	 * @see IWorkbench#getExportWizardRegistry()
	 */
	public WizardActionGroup(IWorkbenchWindow aWindow,
			IWizardRegistry aWizardRegistry, String aType, INavigatorContentService aContentService) {
		this(aWindow, aWizardRegistry, aType, aContentService, null, true);
	}

	/**
	 *
	 * @param aWindow          The window that will be used to acquire a Shell and a
	 *                         Selection Service
	 * @param aWizardRegistry  The wizard registry will be used to locate the
	 *                         correct wizard descriptions.
	 * @param aType            Indicates the value of the type attribute of the
	 *                         commonWizard extension point. Use any of the TYPE_XXX
	 *                         constants defined on this class.
	 * @param aContentService  The content service to use when deciding visibility.
	 * @param descriptorFilter the filter to set, might be <code>null</code> if no
	 *                         filtering is desired.
	 * @param useSeparators    <code>true</code> if seperators should be used,
	 *                         <code>false</code> otherwhise.
	 * @see PlatformUI#getWorkbench()
	 * @see IWorkbench#getNewWizardRegistry()
	 * @see IWorkbench#getImportWizardRegistry()
	 * @see IWorkbench#getExportWizardRegistry()
	 * @since 3.11
	 */
	public WizardActionGroup(IWorkbenchWindow aWindow, IWizardRegistry aWizardRegistry, String aType,
			INavigatorContentService aContentService, Predicate<IWizardDescriptor> descriptorFilter,
			boolean useSeparators) {
		Assert.isNotNull(aWindow);
		Assert.isNotNull(aWizardRegistry);
		Assert.isNotNull(aType);
		Assert.isTrue(TYPE_NEW.equals(aType) || TYPE_IMPORT.equals(aType) || TYPE_EXPORT.equals(aType));
		this.window = aWindow;
		this.wizardRegistry = aWizardRegistry;
		this.type = aType;
		this.contentService = aContentService;
		this.descriptorFilter = descriptorFilter;
		this.useSeparators = useSeparators;
	}

	@Override
	public void setContext(ActionContext aContext) {
		Assert.isTrue(!disposed);

		super.setContext(aContext);
		if (aContext != null) {
			ISelection selection = aContext.getSelection();
			Object element = null;
			if (selection instanceof IStructuredSelection) {
				element = ((IStructuredSelection) selection).getFirstElement();
			}
			if(element == null) {
				element = Collections.EMPTY_LIST;
			}
			// null should be okay here
			setWizardActionDescriptors(CommonWizardDescriptorManager.getInstance()
					.getEnabledCommonWizardDescriptors(element, type, contentService));
		} else {
			setWizardActionDescriptors(NO_DESCRIPTORS);
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		Assert.isTrue(!disposed);

		if (descriptors != null) {
			Map<String, SortedSet<IAction>> groups = findGroups();
			SortedSet<IAction> sortedWizards = null;
			String menuGroupId = null;
			for (Entry<String, SortedSet<IAction>> entry : groups.entrySet()) {
				menuGroupId = entry.getKey();
				sortedWizards = entry.getValue();
				if (useSeparators) {
					menu.add(new Separator(menuGroupId));
				}
				for (Iterator<IAction> wizardItr = sortedWizards.iterator(); wizardItr.hasNext();) {
					menu.add(wizardItr.next());
				}
			}
		}
	}

	/**
	 * @return A Map of menuGroupIds to SortedSets of IActions.
	 */
	private synchronized Map<String, SortedSet<IAction>> findGroups() {
		IAction action = null;
		Map<String, SortedSet<IAction>> groups = new TreeMap<>();
		SortedSet<IAction> sortedWizards = null;
		String menuGroupId = null;
		for (CommonWizardDescriptor descriptor : descriptors) {
			menuGroupId = descriptor.getMenuGroupId() != null ?
							descriptor.getMenuGroupId() : CommonWizardDescriptor.DEFAULT_MENU_GROUP_ID;
			sortedWizards = groups.get(menuGroupId);
			if(sortedWizards == null) {
				groups.put(descriptor.getMenuGroupId(), sortedWizards = new TreeSet<IAction>(ActionComparator.INSTANCE));
			}
			if ((action = getAction(descriptor.getWizardId())) != null) {
				sortedWizards.add(action);
			}
		}
		return groups;
	}


	@Override
	public void dispose() {
		super.dispose();
		actions = null;
		descriptors = null;
		disposed = true;
	}

	/*
	 * Returns the action for the given wizard id, or null if not found.
	 */
	protected IAction getAction(String id) {
		if (id == null || id.length() == 0) {
			return null;
		}

		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = getActions().get(id);
		if (action == null) {
			IWizardDescriptor descriptor = getDescriptor(id);
			if (descriptor != null) {
				action = new WizardShortcutAction(window, descriptor);
				getActions().put(id, action);
			}
		}

		return action;
	}


	protected IWizardDescriptor getDescriptor(String id) {
		IWizardDescriptor descriptor = wizardRegistry.findWizard(id);
		if (descriptor != null && descriptorFilter != null) {
			if (!descriptorFilter.test(descriptor)) {
				return null;
			}
		}
		return descriptor;
	}

	/**
	 * @return a map of (id, IAction)-pairs.
	 */
	protected Map<String, IAction> getActions() {
		if (actions == null) {
			actions = new HashMap<>();
		}
		return actions;
	}

	/**
	 * @return Returns the wizardActionIds.
	 */
	public synchronized String[] getWizardActionIds() {
		if(descriptors != null && descriptors.length > 0) {
			String[] wizardActionIds = new String[descriptors.length];
			for (int i = 0; i < descriptors.length; i++) {
				wizardActionIds[i] = descriptors[i].getWizardId();
			}
			return wizardActionIds;
		}
		return NO_IDS;
	}

	/**
	 * @param theWizardDescriptors
	 *            The wizard action ids to set. These should be defined through
	 *            <b>org.eclipse.ui.xxxWizards</b>
	 */
	private synchronized void setWizardActionDescriptors(CommonWizardDescriptor[] theWizardDescriptors) {
		descriptors = theWizardDescriptors;
	}

	private static class ActionComparator implements Comparator<IAction> {

		private static final ActionComparator INSTANCE = new ActionComparator();
		@Override
		public int compare(IAction arg0, IAction arg1) {
			return arg0.getText().compareToIgnoreCase(arg1.getText());
		}
	}
}
