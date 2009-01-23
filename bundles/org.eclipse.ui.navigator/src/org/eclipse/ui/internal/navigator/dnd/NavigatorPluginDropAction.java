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
package org.eclipse.ui.internal.navigator.dnd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.navigator.Policy;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.part.IDropActionDelegate;
import org.eclipse.ui.part.PluginTransferData;

/**
 * 
 * 
 * @since 3.2
 * 
 */
public class NavigatorPluginDropAction implements IDropActionDelegate {

	private static final String CN_PLUGIN_ACTION_ID = "org.eclipse.ui.navigator.PluginDropAction"; //$NON-NLS-1$

	/**
	 * A default no-args constructor is required by the
	 * <b>org.eclipse.ui.dropAdapters</b> extension point
	 */
	public NavigatorPluginDropAction() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IDropActionDelegate#run(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean run(Object sourceData, Object target) {

		if (Policy.DEBUG_DND) {
			System.out.println("NavigatorPluginDropAction.run (begin)"); //$NON-NLS-1$
		}

		String sourceViewerId = new String((byte[]) sourceData);

		IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer
				.getTransfer().getSelection();

		INavigatorContentService contentService = NavigatorContentServiceTransfer
				.getInstance().findService(sourceViewerId);

		if (contentService == null) {
			return false;
		}
		try {
			CommonDropAdapterAssistant[] assistants = contentService
					.getDnDService().findCommonDropAdapterAssistants(target,
							selection); 

			IStatus valid = null;
			for (int i = 0; i < assistants.length; i++) {
				valid = assistants[i].validatePluginTransferDrop(selection, target);
				if (valid != null && valid.isOK()) {
					valid = assistants[i].handlePluginTransferDrop(selection, target);
					return valid != null && valid.isOK();
				}
			}
		} finally {
			NavigatorContentServiceTransfer.getInstance()
					.unregisterContentService(contentService);
		}

		if (Policy.DEBUG_DND) {
			System.out.println("NavigatorPluginDropAction.run (exit)"); //$NON-NLS-1$
		}

		return false;
	}

	/**
	 * 
	 * @param aContentService
	 *            The associated content service that is the source of the drag
	 * @return A PluginTransferData properly configured to call the Common
	 *         Navigator's PluginDropAction.
	 */
	public static PluginTransferData createTransferData(
			INavigatorContentService aContentService) {
		NavigatorContentServiceTransfer.getInstance().registerContentService(
				aContentService);
		return new PluginTransferData(CN_PLUGIN_ACTION_ID, aContentService
				.getViewerId().getBytes());
	}

}
