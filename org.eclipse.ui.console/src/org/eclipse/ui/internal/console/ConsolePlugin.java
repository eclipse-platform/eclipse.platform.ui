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

package org.eclipse.ui.internal.console;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ConsolePlugin extends AbstractUIPlugin {

	/**
	 * The singleton console plugin instance
	 */
	private static ConsolePlugin fgPlugin= null;
		
	/**
	 * Returns the singleton instance of the console plugin.
	 */
	public static ConsolePlugin getDefault() {
		return fgPlugin;
	}

	public ConsolePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin = this;
	}
}
