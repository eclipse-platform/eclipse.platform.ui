/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

/**
 * Interface for executing processes on operating system level
 *
 */
public interface IProcessExecutor {

	/**
	 * Executes the given command with arguments
	 *
	 * @param command The command to be executed on operating system level
	 * @param args    The optional arguments for the command
	 * @return the standard output or - in case of error - an empty string
	 * @throws Exception if something went wrong
	 */
	String execute(String command, String... args) throws Exception;

}
