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
package org.eclipse.core.tests.internal.plugin.d;

import org.eclipse.core.runtime.*;

public class PluginClass extends Plugin {

	public static Plugin plugin = null;
	public int startupCount = 0;
	

public PluginClass(IPluginDescriptor descriptor) {
	super(descriptor);
	plugin = this;
}

public void startup() throws CoreException {
	startupCount++;
}
}
