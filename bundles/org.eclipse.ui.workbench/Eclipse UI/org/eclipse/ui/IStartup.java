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
package org.eclipse.ui;


/**
 * Plugins that register a startup extension will be activated after
 * the Workbench initializes and have an opportunity to run 
 * code that can't be implemented using the normal contribution 
 * mechanisms.
 * 
 * <p>
 * @since 2.0
 */
public interface IStartup {
	/**
	 * Will be called in a separed thread after the workbench initializes.
	 */
	public void earlyStartup();
}
 
