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

package org.eclipse.update.operations;

/**
 * Batch of operations on feature. For example, a batch features installation.
 */
public interface IBatchOperation  extends IOperation {
	/**
	 * Returns the batch operations.
	 * @return
	 */
	public abstract IFeatureOperation[] getOperations();
}