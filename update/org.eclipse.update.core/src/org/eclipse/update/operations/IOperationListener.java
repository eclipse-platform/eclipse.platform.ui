/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package org.eclipse.update.operations;

/**
 * Listener for operation lifecycle.
 */
public interface IOperationListener {
	/**
	 * May be called before an operation starts executing.
	 * @param operation
	 * @param data
	 * @return
	 */
	public boolean beforeExecute(IOperation operation, Object data);
	/**
	 * May be called after an operation finishes executing.
	 * @param operation
	 * @param data
	 * @return
	 */
	public boolean afterExecute(IOperation operation, Object data);
}