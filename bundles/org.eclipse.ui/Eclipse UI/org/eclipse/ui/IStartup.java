package org.eclipse.ui;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

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
 