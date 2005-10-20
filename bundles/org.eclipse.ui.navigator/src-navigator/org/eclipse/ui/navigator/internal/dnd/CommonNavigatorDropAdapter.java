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
package org.eclipse.ui.navigator.internal.dnd;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/** 
 *  
 */
public class CommonNavigatorDropAdapter extends PluginDropAdapter {

	private ISerializer serializer;

	private final CommonViewer commonViewer;

	/**
	 * @param viewer
	 */
	public CommonNavigatorDropAdapter(CommonViewer aViewer) {
		super(aViewer);
		commonViewer = aViewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	public boolean performDrop(Object data) {

		boolean result = false;
		if (PluginTransfer.getInstance().isSupportedType(getCurrentTransfer()))
			result = super.performDrop(data);
		else {
			Object target = getCurrentTarget();
			Object draggedObject = getSelectedObject(getCurrentTransfer(), data);
			if (getCurrentTarget() == draggedObject)
				return false;
			IDropValidator dropValidator = null;
			CommonDropHandlerService registry = CommonDropHandlerService.getInstance(commonViewer.getNavigatorContentService().getViewerId());
			DropHandlerDescriptor[] descriptors = registry.getDropHandlersEnabledFor(draggedObject, target);
			ArrayList validatedList = new ArrayList();
			/* there may be multiple drop handler descriptors */
			for (int i = 0; i < descriptors.length; i++) {
				/*
				 * if a drop validator is defined, allow it to perform further validations --
				 * otherwise, run the action
				 */
				dropValidator = registry.getDropValidator(commonViewer, descriptors[i]);
				if (dropValidator == null || dropValidator.validateDrop(this, getCurrentTarget(), getCurrentOperation(), getCurrentTransfer())) {
					validatedList.add(descriptors[i]);
				}
			}

			boolean skipOnSingle = NavigatorPlugin.getDefault().getDialogSettings().getBoolean(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION);

			if (validatedList.size() > 0) {
				DropHandlerDescriptor[] validatedDescriptors = new DropHandlerDescriptor[validatedList.size()];
				validatedList.toArray(validatedDescriptors);
				DropHandlerDescriptor descriptorToUse = validatedDescriptors[0];

				if (validatedDescriptors.length != 1 || !skipOnSingle) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					NavigatorDropSelectionDialog dialog = new NavigatorDropSelectionDialog(shell, validatedDescriptors);
					if (Window.OK == dialog.open()) {
						descriptorToUse = dialog.getSelectedDescriptor();
					} else {
						descriptorToUse = null;
					}
				}
				if (null != descriptorToUse) {
					/* when at least one action runs, we will return true */
					result |= registry.getDropActionDelegate(commonViewer, descriptorToUse).run(this, data, target);
				}
			}

		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int,
	 *      org.eclipse.swt.dnd.TransferData)
	 */
	public boolean validateDrop(Object target, int operation, TransferData transferType) {

		/*
		 * should be called first sense the currentTransfer field is set in the parent in this
		 * method
		 */
		if (super.validateDrop(target, operation, transferType)) {
			// System.out.println(getClass().getName()+".validateDrop(Object target=\""+target+"\",
			// int operation=\""+operation+"\", TransferData transferType=\""+transferType+"\"):
			// Validating for PluginTransfer");
			return true;
		}

		// System.out.println(getClass().getName()+".validateDrop(Object target=\""+target+"\", int
		// operation=\""+operation+"\", TransferData transferType=\""+transferType+"\"): Validating
		// for ExtensibleTransfer");
		Object source = getSelectedObject(transferType);
		if (source == target)
			return false;
		IDropValidator dropValidator = null;
		CommonDropHandlerService registry = CommonDropHandlerService.getInstance(commonViewer.getNavigatorContentService().getViewerId());
		DropHandlerDescriptor[] descriptors = registry.getDropHandlersEnabledFor(source, target);
		boolean result = false;
		for (int i = 0; i < descriptors.length; i++) {
			try {
				dropValidator = registry.getDropValidator(commonViewer, descriptors[i]);
				if (dropValidator == null || dropValidator.validateDrop(this, target, operation, transferType)) {
					result = true;
					break;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * @param transferType
	 * @return the selected object only if the transferType is of the type
	 *         LocalSelectionTransfer.getInstance()
	 */
	public Object getSelectedObject(TransferData transferType) {
		IStructuredSelection selection = null;
		if (LocalSelectionTransfer.getInstance().isSupportedType(transferType))
			selection = (IStructuredSelection) LocalSelectionTransfer.getInstance().nativeToJava(transferType);

		if (selection != null)
			return selection.getFirstElement();
		return null;
	}

	/**
	 * @param transferType
	 * @return the selected object only if the transferType is of the type
	 *         LocalSelectionTransfer.getInstance()
	 */
	public Object getSelectedObject(TransferData transferType, Object data) {
		IStructuredSelection selection = null;
		if (LocalSelectionTransfer.getInstance().isSupportedType(transferType))
			selection = (IStructuredSelection) LocalSelectionTransfer.getInstance().nativeToJava(transferType);

		if (selection != null)
			return selection.getFirstElement();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragEnter(DropTargetEvent event) {
		super.dragEnter(event);
		for (int i = 0; i < event.dataTypes.length; i++) {
			if (LocalSelectionTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
				event.data = LocalSelectionTransfer.getInstance().getSelection();
				//System.out.println(getClass().getName()+".dragEnter(DropTargetEvent
				// event=\""+event+"\"): Setting LocalSelectionTransfer");
				break;
			} else if (PluginTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
				IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getInstance().getSelection();
				byte data[] = getSerializer().toByteArray(selection);
				event.data = new PluginTransferData("org.eclipse.wst.common.navigator.internal.views.navigator.dnd.PluginDropAction", data); //$NON-NLS-1$
				//System.out.println(getClass().getName()+".dragEnter(DropTargetEvent
				// event=\""+event+"\"): Setting PluginTransfer");
				break;
			} else if (FileTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
				event.data = FileTransfer.getInstance().nativeToJava(event.dataTypes[i]);
				// FileTransfer from outside the workbench only allows a Copy action
				event.detail = DND.DROP_COPY;
				//System.out.println(getClass().getName()+".dragEnter(DropTargetEvent
				// event=\""+event+"\"): Setting FileTransfer");
				//System.out.println("Data: " + ((String[])event.data)[0]);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragLeave(DropTargetEvent event) {
		super.dragLeave(event);
		if (LocalSelectionTransfer.getInstance().isSupportedType(event.currentDataType)) {
			IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getInstance().getSelection();
			byte data[] = getSerializer().toByteArray(selection);
			event.data = new PluginTransferData("org.eclipse.wst.common.navigator.internal.views.navigator.dnd.GenericDropAction", data); //$NON-NLS-1$
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#getSelectedObject()
	 */
	public Object getSelectedObject() {
		return super.getSelectedObject();
	}

	/**
	 * @return
	 */
	protected ISerializer getSerializer() {
		if (serializer == null)
			serializer = new NavigatorSelectionSerializer(commonViewer.getNavigatorContentService().getViewerId());
		return serializer;
	}

}