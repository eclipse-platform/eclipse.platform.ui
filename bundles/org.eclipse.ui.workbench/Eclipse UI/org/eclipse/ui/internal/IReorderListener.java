/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


/**
 * Simple interface for informing clients of reordering of an object in an ordered list. 
 *
 */
interface IReorderListener {
	
	/**
	 * An object has been moved, clients might need to react.
	 * 
	 * @param obj
	 * @param newIndex 
	 * 
	 */
	public void reorder(Object obj, int newIndex);
}
