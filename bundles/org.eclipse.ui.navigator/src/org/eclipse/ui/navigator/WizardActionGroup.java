/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
 * <p>
 * <ul>
 * <li><b>org.eclipse.ui.newWizards</b></li>
 * <li><b>org.eclipse.ui.importWizards</b></li>
 * <li><b>org.eclipse.ui.exportWizards</b></li>
 * </ul>
 * </p>
 * <p>
 * Here are the required steps for using this feature correctly:
 * <ol>
 * <li>Declare all new/import/export wizards from the extension points above,
 * or locate the existing wizards that you intend to reuse.</li>
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
 * </p>
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
	private Map actions;

	/*
	 * the window is passed to created WizardShortcutActions for the shell and
	 * selection service.
	 */
	private IWorkbenchWindow window;

	/* the correct wizard registry for this action group (getRegistry()) */
	private IWizardRegistry wizardRegistry; 

	private boolean disposed = false;

	private String type;

	private INavigatorContentService contentService;

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
		super();
		Assert.isNotNull(aWindow);
		Assert.isNotNull(aWizardRegistry);
		Assert
				.isTrue(aType != null
						&& (TYPE_NEW.equals(aType) || TYPE_IMPORT.equals(aType) || TYPE_EXPORT
								.equals(aType)));
		window = aWindow;
		wizardRegistry = aWizardRegistry;
		type = aType;

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
		this(aWindow, aWizardRegistry, aType);
		contentService = aContentService;

	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		Assert.isTrue(!disposed);
 
		if (descriptors != null) { 
			Map groups = findGroups(); 
			SortedSet sortedWizards = null;
			String menuGroupId = null;
			for (Iterator menuGroupItr = groups.keySet().iterator(); menuGroupItr.hasNext();) {
				menuGroupId = (String) menuGroupItr.next();
				sortedWizards = (SortedSet) groups.get(menuGroupId); 
				menu.add(new Separator(menuGroupId));
				for (Iterator wizardItr = sortedWizards.iterator(); wizardItr.hasNext();) {
					menu.add((IAction) wizardItr.next());				
				}
			} 
		} 
	}

	/**
	 * @return A Map of menuGroupIds to SortedSets of IActions. 
	 */
	private synchronized Map/*<String, SortedSet<IAction>>*/  findGroups() {  
		IAction action = null;
		Map groups = new TreeMap();
		SortedSet sortedWizards = null;
		String menuGroupId = null;
		for (int i = 0; i < descriptors.length; i++) {
			menuGroupId = descriptors[i].getMenuGroupId() != null ? 
							descriptors[i].getMenuGroupId() : CommonWizardDescriptor.DEFAULT_MENU_GROUP_ID;
			sortedWizards = (SortedSet) groups.get(menuGroupId);
			if(sortedWizards == null) {
				groups.put(descriptors[i].getMenuGroupId(), sortedWizards = new TreeSet(ActionComparator.INSTANCE));
			}  
			if ((action = getAction(descriptors[i].getWizardId())) != null) {
				sortedWizards.add(action); 
			}			
		}
		return groups;
	}


	public void dispose() {
		super.dispose();
		actions = null;
		window = null;
		descriptors = null;
		wizardRegistry = null;
		disposed = true;
	}

	/*
	 * (non-Javadoc) Returns the action for the given wizard id, or null if not
	 * found.
	 */
	protected IAction getAction(String id) {
		if (id == null || id.length() == 0) {
			return null;
		}

		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = (IAction) getActions().get(id);
		if (action == null) {
			IWizardDescriptor descriptor = wizardRegistry.findWizard(id);
			if (descriptor != null) {
				action = new WizardShortcutAction(window, descriptor);
				getActions().put(id, action);
			}
		}

		return action;
	}

	/**
	 * @return a map of (id, IAction)-pairs.
	 */
	protected Map getActions() {
		if (actions == null) {
			actions = new HashMap();
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
	  
	private static class ActionComparator implements Comparator {
		
		private static final ActionComparator INSTANCE = new ActionComparator();
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object arg0, Object arg1) {
			return ((IAction)arg0).getText().compareTo(((IAction)arg1).getText());
		}
	} 
}
