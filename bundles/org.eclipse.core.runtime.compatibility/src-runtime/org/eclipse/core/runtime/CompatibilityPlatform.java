/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import java.net.URL;
import org.eclipse.core.internal.plugins.InternalPlatform;
import org.eclipse.core.runtime.model.Factory;
import org.eclipse.core.runtime.model.PluginRegistryModel;

public class CompatibilityPlatform {
	public static PluginRegistryModel parsePlugins(URL[] pluginPath, Factory factory) {
		return InternalPlatform.parsePlugins(pluginPath, factory, false);
	}
}
