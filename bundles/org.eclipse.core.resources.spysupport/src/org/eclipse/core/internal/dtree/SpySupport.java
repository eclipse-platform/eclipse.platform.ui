/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.dtree;


/**
 * Provides special internal access to the workspace resource implementation.
 * This class is to be used for testing purposes only.
 */
public class SpySupport {

	/* 
	 * Class cannot be instantiated.
	 */
	private SpySupport() {
		// not allowed
	}
	
	public static AbstractDataTreeNode getRootNode(DeltaDataTree node) {
		return node.getRootNode();
	}
}
