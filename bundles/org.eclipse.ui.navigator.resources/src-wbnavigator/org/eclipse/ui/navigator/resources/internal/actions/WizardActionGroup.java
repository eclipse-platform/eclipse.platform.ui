/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;


/**
 * @author mdelder
 *  
 */
public class WizardActionGroup extends ActionGroup {

	private Map actions;
	private IWorkbenchWindow window;
	private String type;
	private IWizardRegistry wizardRegistry;

	//protected WizardsRegistryReader reader;
	private String[] wizardActionIds;
	public static final String IMPORT_WIZARD = "importWizards"; //$NON-NLS-1$
	public static final String EXPORT_WIZARD = "exportWizards"; //$NON-NLS-1$
	public static final String NEW_WIZARD = "newWizards"; //$NON-NLS-1$


	/**
	 *  
	 */
	public WizardActionGroup(IWorkbenchWindow window, String type) {
		super();
		Assert.isNotNull(type);
		this.type = type;
		this.window = window;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {

		IAction action = null;
		// we save a reference to the field in case the original is changed while processing
		String[] localNewWizardActionIds = wizardActionIds;
		if (localNewWizardActionIds != null) {
			for (int i = 0; i < localNewWizardActionIds.length; i++) {
				if ((action = getAction(localNewWizardActionIds[i])) != null) {
					menu.add(action);
				}
			}
		}

	}

	/*
	 * (non-Javadoc) Returns the action for the given wizard id, or null if not found.
	 */
	protected IAction getAction(String id) {
		if (id == null || id.length() == 0)
			return null;

		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = (IAction) getActions().get(id);
		if (action == null) {
			IWizardDescriptor descriptor = getRegistry().findWizard(id);
			if (descriptor != null) {
				action = new WizardShortcutAction(window, descriptor);
				getActions().put(id, action);
			}
		}

		return action;
	}

	/**
	 * @return
	 */
	private IWizardRegistry getRegistry() { 
		if(wizardRegistry == null) {
			if(NEW_WIZARD.equals(type))
				wizardRegistry = WorkbenchPlugin.getDefault().getNewWizardRegistry();
			else if(IMPORT_WIZARD.equals(type))
				wizardRegistry = WorkbenchPlugin.getDefault().getImportWizardRegistry();
			else if(EXPORT_WIZARD.equals(type))
				wizardRegistry = WorkbenchPlugin.getDefault().getExportWizardRegistry();
		}
		return wizardRegistry;
	}

	/**
	 * @return Returns the actions.
	 */
	protected Map getActions() {
		if (actions == null)
			actions = new HashMap();
		return actions;
	}

//	/**
//	 * @return Returns the reader.
//	 */
//	protected WizardsRegistryReader getReader() {
//		if (reader == null)
//			reader = new WorkbenchWizardsRegistryReader(type);
//		return reader;
//	}

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
	public void setWizardActionIds(String[] wizardActionIds) {
		this.wizardActionIds = wizardActionIds;
	}

}