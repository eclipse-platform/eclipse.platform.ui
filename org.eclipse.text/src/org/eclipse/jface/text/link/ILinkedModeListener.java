/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.link;

/**
 * Protocol used by {@link LinkedModeModel}s to communicate state changes, such
 * as leaving linked mode, suspending it due to a child mode coming up, and
 * resuming after a child mode has left.
 * <p>
 * This interface may implemented by clients.
 * </p>
 *
 * @since 3.0
 */
public interface ILinkedModeListener {

	/** Flag to <code>leave</code> specifying no special action. */
	int NONE= 0;
	/**
	 * Flag to <code>leave</code> specifying that all nested modes should
	 * exit.
	 */
	int EXIT_ALL= 1 << 0;
	/**
	 * Flag to <code>leave</code> specifying that the caret should be moved to
	 * the exit position.
	 */
	int UPDATE_CARET= 1 << 1;
	/**
	 * Flag to <code>leave</code> specifying that a UI of a parent mode should
	 * select the current position.
	 */
	int SELECT= 1 << 2;
	/**
	 * Flag to <code>leave</code> specifying that document content outside of
	 * a linked position was modified.
	 */
	int EXTERNAL_MODIFICATION= 1 << 3;

	/**
	 * The leave event occurs when linked is left.
	 *
	 * @param model the model being left
	 * @param flags the reason and commands for leaving linked mode
	 */
	void left(LinkedModeModel model, int flags);

	/**
	 * The suspend event occurs when a nested linked mode is installed within
	 * <code>model</code>.
	 *
	 * @param model the model being suspended due to a nested model being
	 *        installed
	 */
	void suspend(LinkedModeModel model);

	/**
	 * The resume event occurs when a nested linked mode exits.
	 *
	 * @param model the linked mode model being resumed due to a nested mode
	 *        exiting
	 * @param flags the commands to execute when resuming after suspend
	 */
	void resume(LinkedModeModel model, int flags);
}
