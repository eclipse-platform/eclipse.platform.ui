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
package org.eclipse.update.configurator;

import java.io.IOException;
import java.net.URL;

public interface IPlatformConfigurationFactory {
	/**
	 * Returns the current platform configuration.
	 * 
	 * @return platform configuration used in current instance of platform
	 */	
	public IPlatformConfiguration getCurrentPlatformConfiguration();
	/**
	 * Returns a platform configuration object, optionally initialized with previously saved
	 * configuration information.
	 * 
	 * @param url location of previously save configuration information. If <code>null</code>
	 * is specified, an empty configuration object is returned
	 * @return platform configuration used in current instance of platform
	 */	
	public IPlatformConfiguration getPlatformConfiguration(URL url) throws IOException;
}
