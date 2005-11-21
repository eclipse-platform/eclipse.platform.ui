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
package org.eclipse.debug.internal.ui.viewers;



/**
 * An update policy updates elements from an instance of a model 
 * in a viewer.
 * 
 * @since 3.2
 */
public interface IUpdatePolicy {
	
	/**
	 * Installs this update policy on the given viewer.
	 * 
	 * @param viewer viewer to update
	 */
	public void init(AsynchronousViewer viewer);
	
	/**
	 * Disposes this update policy.
	 */
	public void dispose();

}
