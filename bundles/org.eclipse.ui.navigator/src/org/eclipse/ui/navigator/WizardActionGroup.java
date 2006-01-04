/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.internal.wizards.CommonWizardDescriptorManager;
import org.eclipse.ui.navigator.internal.wizards.WizardShortcutAction;
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
 *  extension declared in <b>org.eclipse.ui.navigator.resources</b>, then you will
 *  get most of this functionality for free.</li>
 * <li>Otherwise, you may choose to build your own custom menu. In which case,
 * you may instantiate this class, and hand it the menu or submenu that you want
 * to list out the available wizard shortcuts via
 * {@link WizardActionGroup#fillContextMenu(IMenuManager)}.</li>
 * </ol>
 * </p>
 * <p>
 * Clients may instantiate, but not subclass WizardActionGroup.
 * 
 * <p> 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
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

	private static final String[] NO_IDS = new String[0];

	/* a map of (id, IAction)-pairs. */
	private Map actions;

	/*
	 * the window is passed to created WizardShortcutActions for the shell and
	 * selection service.
	 */
	private IWorkbenchWindow window;

	/* the correct wizard registry for this action group (getRegistry()) */
	private IWizardRegistry wizardRegistry;

	private String[] wizardActionIds;

	private boolean disposed = false;

	private String type;

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

	public void setContext(ActionContext aContext) {
		Assert.isTrue(!disposed);

		super.setContext(aContext);
		if(aContext != null) {
		ISelection selection = aContext.getSelection();
		Object element = null;
		if (!selection.isEmpty() && selection instanceof IStructuredSelection)
			element = ((IStructuredSelection) selection).getFirstElement();
		// null should be okay here
		setWizardActionIds(CommonWizardDescriptorManager.getInstance()
				.getEnabledCommonWizardDescriptorIds(element, type));
		} else {
			setWizardActionIds(NO_IDS);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		Assert.isTrue(!disposed);

		IAction action = null;
		if (wizardActionIds != null)
			for (int i = 0; i < wizardActionIds.length; i++)
				if ((action = getAction(wizardActionIds[i])) != null)
					menu.add(action);

	}

	public void dispose() {
		super.dispose();
		actions = null;
		window = null;
		wizardActionIds = null;
		wizardRegistry = null;
		disposed = true;
	}

	/*
	 * (non-Javadoc) Returns the action for the given wizard id, or null if not
	 * found.
	 */
	protected IAction getAction(String id) {
		if (id == null || id.length() == 0)
			return null;

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
		if (actions == null)
			actions = new HashMap();
		return actions;
	}

	/**
	 * @return Returns the wizardActionIds.
	 */
	public String[] getWizardActionIds() {
		return wizardActionIds;
	}

	/**
	 * @param wizardActionIds
	 *            The wizardActionIds to set.
	 */
	protected void setWizardActionIds(String[] wizardActionIds) {
		this.wizardActionIds = wizardActionIds;
	}

}
