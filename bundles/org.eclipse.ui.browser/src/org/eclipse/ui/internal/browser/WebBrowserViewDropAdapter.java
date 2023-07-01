/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.io.File;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
/**
 *
 */
public class WebBrowserViewDropAdapter extends DropTargetAdapter {
	/**
	 * The view to which this drop support has been added.
	 */
	private BrowserViewer view;

	/**
	 * The current operation.
	 */
	private int currentOperation = DND.DROP_NONE;

	/**
	 * The last valid operation.
	 */
	private int lastValidOperation = DND.DROP_NONE;

	protected WebBrowserViewDropAdapter(BrowserViewer view) {
		this.view = view;
	}

	/*
	 * Method declared on DropTargetAdapter.
	 * The mouse has moved over the drop target.  If the
	 * target item has changed, notify the action and check
	 * that it is still enabled.
	 */
	private void doDropValidation(DropTargetEvent event) {
		//update last valid operation
		if (event.detail != DND.DROP_NONE)
			lastValidOperation = event.detail;

		//valid drop and set event detail accordingly
		if (validateDrop(event.detail, event.currentDataType))
			currentOperation = lastValidOperation;
		else
			currentOperation = DND.DROP_NONE;

		event.detail = currentOperation;
	}

	/*
	 * Method declared on DropTargetAdapter.
	 * The drop operation has changed, see if the action
	 * should still be enabled.
	 */
	@Override
public void dragOperationChanged(DropTargetEvent event) {
		doDropValidation(event);
	}

	/*
	 * Method declared on DropTargetAdapter.
	 * The mouse has moved over the drop target.  If the
	 * target item has changed, notify the action and check
	 * that it is still enabled.
	 */
	@Override
public void dragOver(DropTargetEvent event) {
		//set the location feedback
		event.feedback = DND.FEEDBACK_SELECT;

		//see if anything has really changed before doing validation.
		doDropValidation(event);
	}

	/*
	 * Method declared on DropTargetAdapter.
	 * The user has dropped something on the desktop viewer.
	 */
	@Override
public void drop(DropTargetEvent event) {
		//perform the drop behaviour
		if (!performDrop(event.data))
			event.detail = DND.DROP_NONE;

		currentOperation = event.detail;
	}

	/*
	 * Method declared on DropTargetAdapter.
	 * Last chance for the action to disable itself
	 */
	@Override
public void dropAccept(DropTargetEvent event) {
		if (!validateDrop(event.detail, event.currentDataType))
			event.detail = DND.DROP_NONE;
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT)
			event.detail = DND.DROP_COPY;

		doDropValidation(event);
	}

	/**
	 * Performs any work associated with the drop.
	 * <p>
	 * Subclasses must implement this method to provide drop behavior.
	 * </p>
	 *
	 * @param data the drop data
	 * @return <code>true</code> if the drop was successful, and
	 *   <code>false</code> otherwise
	 */
	protected boolean performDrop(Object data) {
		if (data instanceof String[]) {
			String[] s = (String[]) data;
			if (s.length == 0)
				return true;
			File f = new File(s[0]);
			try {
				view.setURL(f.toURI().toURL().toExternalForm());
			} catch (Exception e) {
				// TODO
			}
		}

		return true;
	}

	/**
	 * Validates dropping on the given object. This method is called whenever some
	 * aspect of the drop operation changes.
	 * <p>
	 * Subclasses must implement this method to define which drops make sense.
	 * </p>
	 *
	 * @param operation the current drag operation (copy, move, etc.)
	 * @param transferType the current transfer type
	 * @return <code>true</code> if the drop is valid, and <code>false</code>
	 *   otherwise
	 */
	protected boolean validateDrop(int operation, TransferData transferType) {
		if (FileTransfer.getInstance().isSupportedType(transferType))
			return true;
		/*if (ResourceTransfer.getInstance().isSupportedType(transferType))
			return true;
		if (LocalSelectionTransfer.getInstance().isSupportedType(transferType))
			return true;*/

		return false;
	}
}
