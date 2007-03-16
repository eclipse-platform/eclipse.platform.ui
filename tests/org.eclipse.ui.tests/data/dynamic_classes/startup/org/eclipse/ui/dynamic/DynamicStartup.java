/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.dynamic;

import org.eclipse.ui.IStartup;

/**
 * @since 3.1
 */
public class DynamicStartup implements IStartup {

	public static Throwable history;
	
	/**
	 * 
	 */
	public DynamicStartup() {
		super();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		history = new Throwable();
		history.fillInStackTrace();
	}
}
