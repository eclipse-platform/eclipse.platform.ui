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
package org.eclipse.core.runtime;

/**
 * This interface is intended to allow a fragment to org.eclipse.core.runtime
 * to hook into the termination of the platform, after the application finishes.
 * <p>Not intended to be implemented by clients.</p>
 */
public interface IShutdownHook {
	/**
	 * This method is called right away after the application finishes.
	 */
	public void run();
}
