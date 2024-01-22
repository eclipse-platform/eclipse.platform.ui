/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The abstract superclass for actions that listen to part activation and
 * open/close events. This implementation tracks the active part (see
 * <code>getActivePart</code>) and provides a convenient place to monitor part
 * lifecycle events that could affect the availability of the action.
 * <p>
 * Subclasses must implement the following <code>IAction</code> method:
 * </p>
 * <ul>
 * <li><code>run</code> - to do the action's work</li>
 * </ul>
 * <p>
 * Subclasses may extend any of the <code>IPartListener</code> methods if the
 * action availablity needs to be recalculated:
 * </p>
 * <ul>
 * <li><code>partActivated</code></li>
 * <li><code>partDeactivated</code></li>
 * <li><code>partOpened</code></li>
 * <li><code>partClosed</code></li>
 * <li><code>partBroughtToTop</code></li>
 * </ul>
 * <p>
 * Although this method implements the <code>IPartListener</code> interface, it
 * does NOT register itself.
 * </p>
 */
public abstract class PartEventAction extends Action implements IPartListener {

	/**
	 * The active part, or <code>null</code> if none.
	 */
	private IWorkbenchPart activePart;

	/**
	 * Creates a new action with the given text.
	 *
	 * @param text the action's text, or <code>null</code> if there is no text
	 */
	protected PartEventAction(String text) {
		super(text);
	}

	/**
	 * Creates a new action with the given text and style.
	 *
	 * @param text  the action's text, or <code>null</code> if there is no text
	 * @param style one of <code>AS_PUSH_BUTTON</code>, <code>AS_CHECK_BOX</code>,
	 *              <code>AS_DROP_DOWN_MENU</code>, <code>AS_RADIO_BUTTON</code>,
	 *              and <code>AS_UNSPECIFIED</code>
	 * @since 3.0
	 */
	protected PartEventAction(String text, int style) {
		super(text, style);
	}

	/**
	 * Returns the currently active part in the workbench.
	 *
	 * @return currently active part in the workbench, or <code>null</code> if none
	 */
	public IWorkbenchPart getActivePart() {
		return activePart;
	}

	/**
	 * The <code>PartEventAction</code> implementation of this
	 * <code>IPartListener</code> method records that the given part is active.
	 * Subclasses may extend this method if action availability has to be
	 * recalculated.
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		activePart = part;
	}

	/**
	 * The <code>PartEventAction</code> implementation of this
	 * <code>IPartListener</code> method does nothing. Subclasses should extend this
	 * method if action availability has to be recalculated.
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// do nothing
	}

	/**
	 * The <code>PartEventAction</code> implementation of this
	 * <code>IPartListener</code> method clears the active part if it just closed.
	 * Subclasses may extend this method if action availability has to be
	 * recalculated.
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part == activePart) {
			activePart = null;
		}
	}

	/**
	 * The <code>PartEventAction</code> implementation of this
	 * <code>IPartListener</code> method records that there is no active part.
	 * Subclasses may extend this method if action availability has to be
	 * recalculated.
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		activePart = null;
	}

	/**
	 * The <code>PartEventAction</code> implementation of this
	 * <code>IPartListener</code> method does nothing. Subclasses should extend this
	 * method if action availability has to be recalculated.
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
		// do nothing
	}
}
