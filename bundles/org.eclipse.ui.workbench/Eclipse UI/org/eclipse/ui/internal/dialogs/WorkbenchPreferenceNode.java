/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExtensionNode;
import org.eclipse.ui.internal.registry.CategorizedPageRegistryReader;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * A proxy for a preference page to avoid creation of preference page just to
 * show a node in the preference dialog tree.
 */
public class WorkbenchPreferenceNode extends WorkbenchPreferenceExtensionNode {

	/**
	 * Create a new instance of the receiver.
	 * @param nodeId
	 * @param element
	 */
	public WorkbenchPreferenceNode(String nodeId, IConfigurationElement element) {
		super(nodeId, element);
	}

	/**
	 * Creates the preference page this node stands for.
	 */
	public void createPage() {
		IWorkbenchPreferencePage page;
		try {
			page = (IWorkbenchPreferencePage) WorkbenchPlugin.createExtension(
					getConfigurationElement(), IWorkbenchRegistryConstants.ATT_CLASS);
		} catch (CoreException e) {
			// Just inform the user about the error. The details are
			// written to the log by now.
			IStatus errStatus = StatusUtil.newStatus(e.getStatus(), WorkbenchMessages.PreferenceNode_errorMessage); 
			StatusManager.getManager().handle(errStatus, StatusManager.SHOW);
			page = new ErrorPreferencePage();
		}

		page.init(PlatformUI.getWorkbench());
		if (getLabelImage() != null) {
			page.setImageDescriptor(getImageDescriptor());
		}
		page.setTitle(getLabelText());
		setPage(page);
	}

	/**
	 * Return the category name for the node.
	 * @return java.lang.String
	 */
	public String getCategory() {
		return getConfigurationElement().getAttribute(
				CategorizedPageRegistryReader.ATT_CATEGORY);
	}	
}
