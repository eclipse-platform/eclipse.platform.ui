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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.dnd.NavigatorDnDService;
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
 * @see CommonViewer
 * @since 3.2
 */
public final class CommonDropAdapter extends PluginDropAdapter {

	private static final Transfer[] SUPPORTED_DROP_TRANSFERS = new Transfer[] {
			LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance(),
			PluginTransfer.getInstance() };

	private static final boolean DEBUG = false;

	private final INavigatorContentService contentService;

	private final NavigatorDnDService dndService;

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
		contentService = aContentService;
		dndService = (NavigatorDnDService) contentService.getDnDService();
	}

	/**
	 * 
	 * @return An array of Transfers allowed by the CommonDropAdapter. Includes
	 *         {@link LocalSelectionTransfer#getTransfer()},
	 *         {@link FileTransfer#getInstance()},
	 *         {@link PluginTransfer#getInstance()}.
	 * @see LocalSelectionTransfer
	 * @see FileTransfer
	 * @see PluginTransfer
	 */
	public Transfer[] getSupportedDropTransfers() {
		return SUPPORTED_DROP_TRANSFERS;
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
			}
		}

		for (int i = 0; i < event.dataTypes.length; i++) {
			if (FileTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
				event.currentDataType = event.dataTypes[i];
				event.detail = DND.DROP_COPY;
				return;
			}
		}

		for (int i = 0; i < event.dataTypes.length; i++) {
			if (PluginTransfer.getInstance()
					.isSupportedType(event.dataTypes[i])) {
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
		if (PluginTransfer.getInstance().isSupportedType(event.currentDataType)) {
			super.drop(event);
		} else {

			CommonDropAdapterAssistant[] assistants = dndService
					.findCommonDropAdapterAssistants(getCurrentTarget(),
							getCurrentTransfer());

			IStatus valid = null;
			for (int i = 0; i < assistants.length; i++) {
				try {
					valid = assistants[i].validateDrop(getCurrentTarget(),
							getCurrentOperation(), getCurrentTransfer());
					if (valid != null && valid.isOK()) {
						assistants[i].handleDrop(this, event,
								getCurrentTarget());
						return;
					}
				} catch (Throwable t) {
					NavigatorPlugin.logError(0, t.getMessage(), t);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object,
	 *      int, org.eclipse.swt.dnd.TransferData)
	 */
	public boolean validateDrop(Object aDropTarget, int theDropOperation,
			TransferData theTransferData) {

		if (DEBUG) {
			System.out.println("CommonDropAdapter.validateDrop (begin)"); //$NON-NLS-1$
		}

		boolean result = false;

		IStatus valid = null;

		if (super.validateDrop(aDropTarget, theDropOperation, theTransferData)) {
			result = true;
		} else if (FileTransfer.getInstance().isSupportedType(theTransferData)) {
			// only allow copying when dragging from outside Eclipse
			// result = theDropOperation != DND.DROP_COPY;
			result = true;
		} else {
			CommonDropAdapterAssistant[] assistants = dndService
					.findCommonDropAdapterAssistants(aDropTarget,
							theTransferData);

			for (int i = 0; i < assistants.length; i++) {
				try {
					valid = assistants[i].validateDrop(aDropTarget,
							theDropOperation, theTransferData);
				} catch (Throwable t) {
					NavigatorPlugin.logError(0, t.getMessage(), t);
				}
				if (valid != null && valid.isOK()) {
					result = true;
					break;
				}
			}
		}

		if (DEBUG) {
			System.out
					.println("CommonDropAdapter.validateDrop (returning " + (valid != null ? valid.getSeverity() + ": " + valid.getMessage() : "" + result) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		return result;

	}

	/*
	 * The visibility of the following methods is raised for downstream clients
	 * (assistants).
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#getBounds(org.eclipse.swt.widgets.Item)
	 */
	public Rectangle getBounds(Item item) {
		return super.getBounds(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#getCurrentLocation()
	 */
	public int getCurrentLocation() {
		return super.getCurrentLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#getCurrentOperation()
	 */
	public int getCurrentOperation() {
		return super.getCurrentOperation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#getCurrentTarget()
	 */
	public Object getCurrentTarget() {
		return super.getCurrentTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PluginDropAdapter#getCurrentTransfer()
	 */
	public TransferData getCurrentTransfer() {
		return super.getCurrentTransfer();
	}

}
