/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.link;


/**
 * Protocol used by <code>LinkedEnvironment</code>s to communicate state changes, such
 * as leaving the environment, suspending it due to a child environment coming up, and resuming
 * after a child environment has left.
 * 
 * @since 3.0
 */
public interface ILinkedListener {
	/** Flag to <code>leave</code> specifying no special action. */
	int NONE= 0;
	/** Flag to <code>leave</code> specifying that all nested environments should exit. */
	int EXIT_ALL= 1 << 0;
	/** Flag to <code>leave</code> specifying that the caret should be moved to the exit position. */
	int UPDATE_CARET= 1 << 1;
	/** Flag to <code>leave</code> specifying that a UI of a parent environment should select the current position. */
	int SELECT= 1 << 2;
	/** Flag to <code>leave</code> specifying that document content outside of a linked position was modified. */
	int EXTERNAL_MODIFICATION= 1 << 3;
	/**
	 * The leave event occurs when a linked environment exits.
	 * 
	 * @param environment the leaving environment
	 * @param flags the reason and commands for leaving linked mode
	 */
	void left(LinkedEnvironment environment, int flags);
	/**
	 * The suspend event occurs when a nested linked environment is installed on this environment.
	 * 
	 * @param environment the environment being suspended due to a nested environment being installed
	 */
	void suspend(LinkedEnvironment environment);
	/**
	 * The resume event occurs when a nested linked environment exits.
	 * 
	 * @param environment the environment being resumed due to a nested environment exiting
	 * @param flags the commands to execute when resuming after suspend
	 */
	void resume(LinkedEnvironment environment, int flags);
}
