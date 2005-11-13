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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *  
 */
// Dependencies to ResourceTransferDragAdapter and FileTransferDragAdapter have
// been removed to support RCP applications. These will be revisted for M5.  
public class CommonNavigatorDragAdapter extends DragSourceAdapter {

//	private ResourceTransferDragAdapter delegateResourceDragAdapter;
//
//	private FileTransferDragAdapter delegateFileDragAdapter;

	private ISerializer serializer;

	private final CommonViewer commonViewer;

	/**
	 *  
	 */
	public CommonNavigatorDragAdapter(CommonViewer aViewer) {
		super();
		commonViewer = aViewer;
//		this.delegateResourceDragAdapter = new ResourceTransferDragAdapter(commonViewer);
//		this.delegateFileDragAdapter = new FileTransferDragAdapter(commonViewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragStart(DragSourceEvent event) {
		try {
			ISelection selection = commonViewer.getSelection();
			event.doit = !selection.isEmpty();
			LocalSelectionTransfer.getInstance().setSelection(selection);

		} catch (RuntimeException e) {
			NavigatorPlugin.log("CommonNavigatorDragAdapter.dragStart():" + e.toString()); //$NON-NLS-1$
		}
		// System.out.println(getClass().getName()+".dragStart(DragSourceEvent event=\""+event+"\");
		// widget = " + event.widget);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(DragSourceEvent event) {
		if (LocalSelectionTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = LocalSelectionTransfer.getInstance().getSelection();
			// System.out.println(getClass().getName()+".dragSetData(DragSourceEvent
			// event=\""+event+"\"): Setting LocalSelectionTransfer");
		} else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getInstance().getSelection();
			byte data[] = getSerializer().toByteArray(selection);
			event.data = new PluginTransferData("org.eclipse.wst.common.navigator.internal.views.navigator.dnd.PluginDropAction", data); //$NON-NLS-1$
			// System.out.println(getClass().getName()+".dragSetData(DragSourceEvent
			// event=\""+event+"\"): Setting PluginTransfer");
// Removed to allow RCP integration			
//		} else if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
//			this.delegateResourceDragAdapter.dragSetData(event);
//			// System.out.println(getClass().getName()+".dragSetData(DragSourceEvent
//			// event=\""+event+"\"): Setting ResourceTransfer");
//		} else if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
//			this.delegateFileDragAdapter.dragSetData(event);
//			// System.out.println(getClass().getName()+".dragSetData(DragSourceEvent
//			// event=\""+event+"\"): Setting FileTransfer");

		}
	}

	/**
	 * @return
	 */
	private ISerializer getSerializer() {
		if (serializer == null)
			serializer = new NavigatorSelectionSerializer(commonViewer.getNavigatorContentService().getViewerId());
		return serializer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(DragSourceEvent event) {
		LocalSelectionTransfer.getInstance().setSelection(null);
		/* this.delegateResourceDragAdapter.dragFinished(event); */
	}

}