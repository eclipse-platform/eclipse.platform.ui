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

import java.io.File;

/**
 * The interface of the service that allows to convert plugin.xml into manifest.mf
 */
public interface IPluginConverter {
	public void convertManifest(IPluginInfo pluginInfo, File pluginLocation, File bundleManifestLocation);
	public boolean convertManifest(File pluginLocation, File bundleManifestLocation);
}
