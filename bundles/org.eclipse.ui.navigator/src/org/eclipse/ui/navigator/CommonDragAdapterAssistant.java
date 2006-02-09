/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Event;
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
 * @see CommonViewer#initDragAndDrop()
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

	/**
	 * Extra TransferTypes allow the Navigator to generate different kinds of
	 * payloads for DND clients. By default, the {@link CommonDragAdapter}
	 * supports {@link LocalSelectionTransfer} and {@link PluginTransfer}.
	 * 
	 * <p>
	 * CommonDragAdapterAssistants can extend the available TransferTypes that a
	 * Common Navigator Viewer can generate. Clients should return the set of
	 * Transfer Types they support.When a match is found for a particular
	 * {@link DragSourceEvent},
	 * {@link #setDragData(DragSourceEvent, IStructuredSelection)} will be
	 * called directly after.
	 * </p>
	 * 
	 * @return The added transfer types. (e.g. FileTransfer.getInstance()).
	 */
	public abstract Transfer[] getSupportedTransferTypes();

	/**
	 * Set the value of the {@link Event#data} field using the given selection.
	 * Clients will only have an opportunity to set the drag data if they have
	 * returned a matching Transfer Type from
	 * {@link #getSupportedTransferTypes()} for the
	 * {@link DragSourceEvent#dataType}.
	 * 
	 * @param anEvent
	 *            The event object should have its {@link Event#data} field set
	 *            to a value that matches a supported {@link TransferData} type.
	 * @param aSelection
	 *            The current selection from the viewer.
	 */
	public abstract void setDragData(DragSourceEvent anEvent,
			IStructuredSelection aSelection);

}
