/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.part.PluginTransfer;

/**
 * Assist the {@link CommonDragAdapter} by providing new TransferTypes and the
 * logic to handle setting up the transfer data. Clients must extend this class
 * as part of the <b>org.eclipse.ui.navigator.viewer/dragAssistant</b>
 * extension. By default, the Common Navigator supports
 * {@link LocalSelectionTransfer} and {@link PluginTransfer}.
 * 
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * @see INavigatorDnDService
 * @see CommonDragAdapter
 * @see CommonDropAdapter
 * @see CommonDropAdapterAssistant
 * @see CommonViewer
 * @see <a
 *      href="http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html">Drag
 *      and Drop: Adding Drag and Drop to an SWT Application</a>
 * @see <a
 *      href="http://www.eclipse.org/articles/Article-Workbench-DND/drag_drop.html">Drag
 *      and Drop in the Eclipse UI (Custom Transfer Types)</a>
 * 
 * @since 3.2
 * 
 */
public abstract class CommonDragAdapterAssistant {

	private INavigatorContentService contentService;

	/**
	 * Extra TransferTypes allow the Navigator to generate different kinds of
	 * payloads for DND clients. By default, the {@link CommonDragAdapter}
	 * supports {@link LocalSelectionTransfer} and {@link PluginTransfer}.
	 * 
	 * <p>
	 * CommonDragAdapterAssistants can extend the available TransferTypes that a
	 * Common Navigator Viewer can generate. Clients should return the set of
	 * Transfer Types they support. When a drop event occurs, the available drag
	 * assistants will be searched for a <i>enabled</i> assistants for the
	 * {@link DragSourceEvent}. Only if the drop event occurs will
	 * {@link #setDragData(DragSourceEvent, IStructuredSelection)} be called. If
	 * the drop event is cancelled,
	 * {@link #setDragData(DragSourceEvent, IStructuredSelection)} will not be
	 * called.
	 * </p>
	 * 
	 * @return The added transfer types. (e.g. FileTransfer.getInstance()).
	 */
	public abstract Transfer[] getSupportedTransferTypes();

	/**
	 * Set the value of the {@link org.eclipse.swt.widgets.Event#data} field using the given selection.
	 * Clients will only have an opportunity to set the drag data if they have
	 * returned a matching Transfer Type from
	 * {@link #getSupportedTransferTypes()} for the
	 * {@link DragSourceEvent#dataType}.
	 * <p>
	 * Clients will only have an opportunity to set the data when the drop event
	 * occurs. If the drop operation is cancelled, then this method will not be
	 * called.
	 * </p>
	 * 
	 * @param anEvent
	 *            The event object should have its {@link Event#data} field set
	 *            to a value that matches a supported {@link TransferData} type.
	 * @param aSelection
	 *            The current selection from the viewer.
	 * @return True if the data could be set; false otherwise.
	 */
	public abstract boolean setDragData(DragSourceEvent anEvent,
			IStructuredSelection aSelection);

	/**
	 * 
	 * Allows the drag assistant indicate it wants to participate in the drag operation.
	 * This is called at {@link DragSourceListener#dragStart(DragSourceEvent)} 
	 * time.
	 * 
	 * @param anEvent
	 *            The event object should return doit = true if it wants to participate
	 *            in the drag and set doit = false if it does not want to further 
	 *            participate.
	 * @param aSelection
	 *            The current selection from the viewer.
	 * 
	 * @since 3.4
	 */
	public void dragStart(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {
		// May be subclassed
	}
	
	/**
	 * 
	 * Allows the drag assistant to do any necessary cleanup after the drop operation
	 * is done. This is called at {@link DragSourceListener#dragFinished(DragSourceEvent)} 
	 * time.  This is called on the same assistant that was called for the set data.
	 * 
	 * @param anEvent
	 *            The event object should have its {@link Event#data} field set
	 *            to a value that matches a supported {@link TransferData} type.
	 * @param aSelection
	 *            The current selection from the viewer.
	 * 
	 * @since 3.4
	 */
	public void dragFinished(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {
		// May be subclassed
	}
	
	/**
	 * Accept and remember the content service this assistant is associated
	 * with.
	 * 
	 * @param aContentService
	 */
	public final void setContentService(INavigatorContentService aContentService) {
		contentService = aContentService;
	}

	/**
	 * 
	 * @return The associated content service.
	 */
	public INavigatorContentService getContentService() {
		return contentService;
	}

	/**
	 * 
	 * @return The shell for the viewer this assistant is associated with or the
	 *         shell of the active workbench window.
	 */
	public final Shell getShell() {
		if (contentService != null) {
			((NavigatorContentService) contentService).getShell();
		}
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

}
