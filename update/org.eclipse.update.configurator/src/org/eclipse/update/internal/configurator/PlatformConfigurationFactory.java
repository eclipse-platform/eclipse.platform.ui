/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;

import java.io.IOException;
import java.net.URL;

import org.eclipse.update.configurator.IPlatformConfiguration;
import org.eclipse.update.configurator.IPlatformConfigurationFactory;

public class PlatformConfigurationFactory implements IPlatformConfigurationFactory {
	@Override
	public IPlatformConfiguration getCurrentPlatformConfiguration() {
		return PlatformConfiguration.getCurrent();
	}
	
	@Override
	public IPlatformConfiguration getPlatformConfiguration(URL url)	throws IOException {
		try {
			return new PlatformConfiguration(url);
		} catch (Exception e) {
			if (e instanceof IOException)
				throw (IOException)e;
			throw new IOException(e.getMessage());
		}
	}
	
	@Override
	public IPlatformConfiguration getPlatformConfiguration(URL url, URL loc) throws IOException {
		try {
			return new PlatformConfiguration(url, loc);
		} catch (Exception e) {
			if (e instanceof IOException)
				throw (IOException)e;
			throw new IOException(e.getMessage());
		}
	}
	
}
