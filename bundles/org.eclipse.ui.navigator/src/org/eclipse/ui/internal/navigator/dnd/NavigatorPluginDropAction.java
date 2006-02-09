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
package org.eclipse.ui.internal.navigator.dnd;

import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.part.IDropActionDelegate;
import org.eclipse.ui.part.PluginTransferData;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class NavigatorPluginDropAction implements IDropActionDelegate {

	private static final String CN_PLUGIN_ACTION_ID = "org.eclipse.ui.navigator.PluginDropAction"; //$NON-NLS-1$

	/**
	 * 
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

		// TODO Handle the callback for drop targets that cannot handle the drop

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
		return new PluginTransferData(CN_PLUGIN_ACTION_ID, aContentService
				.getViewerId().getBytes());
	}

}
