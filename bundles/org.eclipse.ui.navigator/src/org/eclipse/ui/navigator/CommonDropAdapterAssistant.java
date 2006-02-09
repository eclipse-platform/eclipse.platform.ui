/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.PluginTransfer;

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
 * <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent</b> extension.
 * Whenever a match is found, the assistant will be given an opportunity to
 * first {@link #validateDrop(Object, int, TransferData) }, and then if the
 * assistant returns true, the assist must
 * {@link #handleDrop(DropTargetEvent, Object) }. If multiple assistants match
 * the drop target, then the potential assistants are ordered based on priority
 * and their override relationships and given an opportunity to validate the
 * drop operation in turn. The first one to validate will have the opportunty to
 * carry out the drop.
 * </p>
 * <p>
 * That is, if a content extension X overrides content extension Y (see
 * <b>org.eclipse.ui.navigator.navigatorContent/override</b>), then X will have
 * an opportunity before Y. If X and Y override Z, but X has higher priority
 * than Y, then X will have an opportunity before Y, and Y will have an
 * opportunity before Z.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @see INavigatorDnDService
 * @see INavigatorDnDService#findCommonDropAdapterAssistants(Object)
 * @since 3.2
 * 
 */
public abstract class CommonDropAdapterAssistant {

	private INavigatorContentService contentService;

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
	 * Override to perform any one-time intialization.
	 */
	protected void doInit() {

	}

	/**
	 * Validates dropping on the given object. This method is called whenever
	 * some aspect of the drop operation changes.
	 * <p>
	 * Subclasses must implement this method to define which drops make sense.
	 * If clients return true, then they will be allowed to handle the drop in
	 * {@link #handleDrop(DropTargetEvent, Object) }.
	 * </p>
	 * 
	 * @param target
	 *            the object that the mouse is currently hovering over, or
	 *            <code>null</code> if the mouse is hovering over empty space
	 * @param operation
	 *            the current drag operation (copy, move, etc.)
	 * @param transferType
	 *            the current transfer type
	 * @return <code>true</code> if the drop is valid, and <code>false</code>
	 *         otherwise
	 */
	public abstract boolean validateDrop(Object target, int operation,
			TransferData transferType);

	/**
	 * Carry out the DND operation
	 * 
	 * @param aDropTargetEvent
	 *            The drop target event.
	 * @param aTarget
	 *            The object being dragged onto
	 * @return True if the operation completed.
	 */
	public abstract boolean handleDrop(DropTargetEvent aDropTargetEvent,
			Object aTarget);

	/**
	 * When a drop opportunity presents itself, the available TransferData types
	 * will be supplied in the event. The DropAdapter must select one of these
	 * TransferData types for the DragAdapter to provide. By default the Common
	 * Navigator supports {@link LocalSelectionTransfer} and
	 * {@link PluginTransfer}. Clients are required to indicate if they support
	 * other TransferData types using this method.
	 * 
	 * <p>
	 * If none of the given TransferData types are supported, return null.
	 * </p>
	 * 
	 * @param theAvailableTransferData
	 * @return The supported TransferData from the set.
	 */
	public abstract TransferData findSupportedTransferData(
			TransferData[] theAvailableTransferData);

	/**
	 * 
	 * @return The associated content service.
	 */
	protected INavigatorContentService getContentService() {
		return contentService;
	}

}
