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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.dnd.NavigatorPluginDropAction;
import org.eclipse.ui.part.PluginTransfer;

/**
 * 
 * Provides an implementation of {@link DragSourceAdapter} which uses the
 * extensions provided by the associated {@link INavigatorContentService}.
 * 
 * <p>
 * Clients should not need to create an instance of this class unless they are
 * creating their own custom viewer. Otherwise, {@link CommonViewer} configures
 * its drag adapter automatically.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p> *
 * 
 * @see INavigatorDnDService
 * @see CommonDragAdapterAssistant
 * @see CommonDropAdapter
 * @see CommonDropAdapterAssistant
 * @see CommonViewer#initDragAndDrop()
 * @since 3.2
 * 
 */
public final class CommonDragAdapter extends DragSourceAdapter {

	private final INavigatorContentService contentService;

	private final ISelectionProvider provider;

	/**
	 * Create a DragAdapter that drives the configuration of the drag data.
	 * 
	 * @param aContentService
	 *            The content service this Drag Adapter is associated with
	 * @param aProvider
	 *            The provider that can give the current selection from the
	 *            appropriate viewer.
	 */
	public CommonDragAdapter(INavigatorContentService aContentService,
			ISelectionProvider aProvider) {
		super();
		contentService = aContentService;
		provider = aProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragStart(DragSourceEvent event) {
		try {
			// Workaround for 1GEUS9V
			DragSource dragSource = (DragSource) event.widget;
			Control control = dragSource.getControl();
			if (control != control.getDisplay().getFocusControl()) {
				event.doit = false;
				return;
			}
			ISelection selection = provider.getSelection();
			if (selection.isEmpty()) {
				event.doit = false;
				return;
			}
			LocalSelectionTransfer.getTransfer().setSelection(selection);
			event.doit = true;
		} catch (RuntimeException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(DragSourceEvent event) {

		ISelection selection = LocalSelectionTransfer.getTransfer()
				.getSelection();
		if (LocalSelectionTransfer.getTransfer()
				.isSupportedType(event.dataType))
			event.data = selection;
		else if (PluginTransfer.getInstance().isSupportedType(event.dataType))
			event.data = NavigatorPluginDropAction
					.createTransferData(contentService);
		else if (selection instanceof IStructuredSelection) {
			INavigatorDnDService dndService = contentService.getDnDService();
			CommonDragAdapterAssistant[] assistants = dndService
					.getCommonDragAssistants();
			for (int i = 0; i < assistants.length; i++) {

				Transfer[] supportedTransferTypes = assistants[i]
						.getSupportedTransferTypes();
				for (int j = 0; j < supportedTransferTypes.length; j++) {
					if (supportedTransferTypes[j]
							.isSupportedType(event.dataType))
						try {
							assistants[i].setDragData(event,
									(IStructuredSelection) selection);
						} catch (RuntimeException re) {
							NavigatorPlugin.logError(0, re.getMessage(), re);
						}

				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(DragSourceEvent event) {
		LocalSelectionTransfer.getTransfer().setSelection(null);
		if (event.doit == false)
			return;
	}

}
