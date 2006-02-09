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

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.internal.navigator.dnd.NavigatorPluginDropAction;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.PluginTransfer;

/**
 * Provides an implementation of {@link PluginDropAdapter} which uses the
 * extensions provided by the associated {@link INavigatorContentService}.
 * 
 * <p>
 * Clients should not need to create an instance of this class unless they are
 * creating their own custom viewer. Otherwise, {@link CommonViewer} configures
 * its drop adapter automatically.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @see INavigatorDnDService
 * @see CommonDragAdapter
 * @see CommonDragAdapterAssistant
 * @see CommonDropAdapterAssistant
 * @see CommonViewer#initDragAndDrop()
 * @since 3.2
 */
public final class CommonDropAdapter extends PluginDropAdapter {

	// private final StructuredViewer viewer;

	private final INavigatorContentService contentService;

	/**
	 * Create a DropAdapter that handles a drop based on the given content
	 * service and selection provider.
	 * 
	 * @param aContentService
	 *            The content service this Drop Adapter is associated with
	 * @param aStructuredViewer
	 *            The viewer this DropAdapter is associated with.
	 */
	public CommonDropAdapter(INavigatorContentService aContentService,
			StructuredViewer aStructuredViewer) {
		super(aStructuredViewer);
		// viewer = aStructuredViewer;
		contentService = aContentService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragEnter(DropTargetEvent event) {
		super.dragEnter(event);

		for (int i = 0; i < event.dataTypes.length; i++) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(
					event.dataTypes[i])) {
				event.currentDataType = event.dataTypes[i];
				return;
			} else if (FileTransfer.getInstance().isSupportedType(
					event.dataTypes[i])) {
				event.currentDataType = event.dataTypes[i];
				event.detail = DND.DROP_COPY;
				return;
			} else if (PluginTransfer.getInstance().isSupportedType(
					event.dataTypes[i])) {
				event.currentDataType = event.dataTypes[i];
				return;
			}
		}
		event.detail = DND.DROP_NONE;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragLeave(DropTargetEvent event) {
		super.dragLeave(event);
		if (LocalSelectionTransfer.getTransfer().isSupportedType(
				event.currentDataType)) {
			event.data = NavigatorPluginDropAction
					.createTransferData(contentService);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PluginDropAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void drop(DropTargetEvent event) {
		if (PluginTransfer.getInstance().isSupportedType(event.currentDataType))
			super.drop(event);

		// TODO Use the CommonDropAssistants to validate the drop.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object,
	 *      int, org.eclipse.swt.dnd.TransferData)
	 */
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		/*
		 * should be called first sense the currentTransfer field is set in the
		 * parent in this method
		 */
		if (super.validateDrop(target, operation, transferType)) {
			return true;
		}
		boolean result = false;

		// TODO Use the CommonDropAssistants to validate the drop.

		return result;
	}

}
