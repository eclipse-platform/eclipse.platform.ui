/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.navigator.NavigatorContentService;

/**
 * <p>
 * Used by the
 * <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent/commonDropAdapter</b>
 * extension point to carry out pluggable drop operations.
 * </p>
 * <p>
 * Each {@link CommonDropAdapterAssistant} is contained by single content
 * extension. The opportunity for each assistant to handle the drop operation is
 * determined by the <b>possibleChildren</b> expression of the
 * <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent</b> extension;
 * whenever every element in the drag set matches the <b>possibleChildren</b>
 * expression of an extension, it is eligible to handle the drop operation. This
 * initial set is further culled using the <b>possibleDropTargets</b>
 * expression of the <b>commonDropAdapter</b> using the current drop target.
 * </p>
 * <p>
 * If drag operations originate outside of Eclipse, then the set of eligible
 * drop adapters is determined based on the drop target (using the
 * <b>possibleDropTargets</b> expression). Each assistant can then indicate 
 * whether {@link #isSupportedType(TransferData) the incoming type is supported}.
 * <p>
 * Whenever a match is found, the assistant will be given an opportunity to
 * first {@link #validateDrop(Object, int, TransferData)}, and then if the
 * assistant returns true, the assist must
 * {@link #handleDrop(CommonDropAdapter, DropTargetEvent, Object)}. If
 * multiple assistants match the drop target, then the potential assistants are
 * ordered based on priority and their override relationships and given an
 * opportunity to validate the drop operation in turn. The first one to validate
 * will have the opportunty to carry out the drop.
 * </p>
 * 
 * <p>
 * Clients may handle DND operations that begin and end in the current viewer by
 * overriding the following methods:
 * <ul>
 * <li>{@link #validateDrop(Object, int, TransferData)}: Indicate whether this
 * assistant can handle a drop onto the current viewer.</li>
 * <li>{@link #handleDrop(CommonDropAdapter, DropTargetEvent, Object)}: Handle
 * the drop operation onto the current viewer.</li>
 * </ul>
 * </p>
 * <p>
 * If a user originates a drag operation to another viewer that cannot handle
 * one of the available drag transfer types, drop assistants may handle the drop
 * operation for the target viewer. Clients must override :
 * <ul>
 * <li>{@link #validatePluginTransferDrop(IStructuredSelection, Object)}:
 * Indicate whether this assistant can handle the drop onto another viewer.
 * <li>{@link #handlePluginTransferDrop(IStructuredSelection, Object)}: Handle
 * the drop operation onto the other viewer.</li>
 * </ul>
 * </p> 
 * <p>
 * Clients may subclass this.
 * </p>
 * 
 * @see INavigatorDnDService
 * @see INavigatorDnDService#findCommonDropAdapterAssistants(Object,
 *      TransferData)
 * @since 3.2
 * 
 */
public abstract class CommonDropAdapterAssistant {

	private INavigatorContentService contentService;

	private DropTargetEvent _currentEvent;

	private CommonDropAdapter _dropAdapter;
	
	/**
	 * Perform any necessary initialization using the
	 * {@link INavigatorContentService}.
	 * 
	 * 
	 * @param aContentService
	 *            The instance of {@link INavigatorContentService} that the
	 *            current CommonDropAdapterAssistant will be associated with
	 */
	public final void init(INavigatorContentService aContentService) {
		contentService = aContentService;
		doInit();
	}

	
	/**
	 * Override to perform any one-time initialization.
	 */
	protected void doInit() {

	}

	/**
	 * Validates dropping on the given object. This method is called whenever
	 * some aspect of the drop operation changes.
	 * <p>
	 * Subclasses must implement this method to define which drops make sense.
	 * If clients return true, then they will be allowed to handle the drop in
	 * {@link #handleDrop(CommonDropAdapter, DropTargetEvent, Object) }.
	 * </p>
	 * 
	 * @param target
	 *            the object that the mouse is currently hovering over, or
	 *            <code>null</code> if the mouse is hovering over empty space
	 * @param operation
	 *            the current drag operation (copy, move, etc.)
	 * @param transferType
	 *            the current transfer type
	 * @return A status indicating whether the drop is valid.
	 */
	public abstract IStatus validateDrop(Object target, int operation,
			TransferData transferType);

	/**
	 * Carry out the DND operation.
	 * 
	 * <p>
	 * Note: Contrary to the SWT {@link DropTargetListener} specification, you
	 * <i>must</i> make sure that the aDropTargetEvent.detail is not set to
	 * DND.DROP_MOVE unless actual work is required in the
	 * {@link DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)}
	 * to complete the operation (for example removing the moved file). In
	 * particular for the LocalSelectionTransfer case, DND.DROP_MOVE cannot be
	 * used as it will cause incorrect behavior in some existing drag handlers.
	 * 
	 * In case of move operations where no action is required on the source side
	 * (e.g. LocalSelectionTransfer) you must set aDropTargetEvent.detail to
	 * DND.DROP_NONE to signal this to the drag source. Even though the SWT
	 * specification says this is canceling the drop, it is not really doing so,
	 * it is only preventing the DND.DROP_MOVE from being passed through to the
	 * dragFinished() method.
	 * 
	 * @param aDropAdapter
	 *            The Drop Adapter contains information that has already been
	 *            parsed from the drop event.
	 * @param aDropTargetEvent
	 *            The drop target event.
	 * @param aTarget
	 *            The object being dragged onto
	 * @return A status indicating whether the drop completed OK.
	 */
	public abstract IStatus handleDrop(CommonDropAdapter aDropAdapter,
			DropTargetEvent aDropTargetEvent, Object aTarget);

	/**
	 * Clients may extend the supported transfer types beyond the default
	 * {@link LocalSelectionTransfer#getTransfer()} and
	 * {@link org.eclipse.ui.part.PluginTransfer#getInstance()} transfer types. When a transfer type
	 * other than one of these is encountered, the DND Service will query the
	 * <b>visible</b> and <b>active</b> descriptors that are <b>enabled</b>
	 * for the drop target of the current operation.
	 * 
	 * @param aTransferType
	 *            The transfer data from the drop operation
	 * @return True if the given TransferData can be understood by this
	 *         assistant.
	 */
	public boolean isSupportedType(TransferData aTransferType) {
		return LocalSelectionTransfer.getTransfer().isSupportedType(
				aTransferType);
	}

	/**
	 * 
	 * Return true if the client can handle the drop onto the target viewer of
	 * the drop operation.
	 * <p>
	 * The default behavior of this method is to return <b>Status.CANCEL_STATUS</b>.
	 * </p>
	 * 
	 * @param aDragSelection
	 *            The selection dragged from the viewer.
	 * @param aDropTarget
	 *            The target of the drop operation.
	 * 
	 * @return OK if the plugin transfer can be handled by this assistant.
	 */
	public IStatus validatePluginTransferDrop(
			IStructuredSelection aDragSelection, Object aDropTarget) {
		return Status.CANCEL_STATUS;
	}

	/**
	 * Handle the drop operation for the target viewer.
	 * <p>
	 * The default behavior of this method is to return <b>Status.CANCEL_STATUS</b>.
	 * </p>
	 * 
	 * @param aDragSelection
	 *            The selection dragged from the viewer.
	 * @param aDropTarget
	 *            The target of the drop operation.
	 * 
	 * @return OK if the drop operation succeeded.
	 */
	public IStatus handlePluginTransferDrop(
			IStructuredSelection aDragSelection, Object aDropTarget) {
		return Status.CANCEL_STATUS;
	}

	/**
	 * 
	 * @return The associated content service.
	 */
	protected INavigatorContentService getContentService() {
		return contentService;
	}

	/**
	 * 
	 * @return A shell for the viewer currently used by the
	 *         {@link INavigatorContentService}.
	 */
	protected final Shell getShell() {
		return ((NavigatorContentService) contentService).getShell();
	}

	/**
	 * Sets the current {@link DropTargetEvent}.
	 * 
	 * This is used to make the event available to the client methods of this class.
	 * 
	 * @param event
	 *            the new event.
	 * 
	 * @since 3.4
	 */
	void setCurrentEvent(DropTargetEvent event) {
		_currentEvent = event;
	}

	/**
	 * Returns the current {@link DropTargetEvent}.
	 * 
	 * @return event the current DropTargetEvent.
	 * 
	 * @since 3.4
	 */
	public DropTargetEvent getCurrentEvent() {
		return _currentEvent;
	}
	
    /**
     * Sets the {@link CommonDropAdapter}.
     * @param dropAdapter 
     *
     * @noreference
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     */
	public void setCommonDropAdapter(CommonDropAdapter dropAdapter) {
		_dropAdapter = dropAdapter;
	}
	
    /**
     * Returns the {@link CommonDropAdapter}.
     *
     * @return the CommonDropAdapter.
     * 
     * @since 3.4
     *
     */
    protected CommonDropAdapter getCommonDropAdapter() {
        return _dropAdapter;
    }

}
