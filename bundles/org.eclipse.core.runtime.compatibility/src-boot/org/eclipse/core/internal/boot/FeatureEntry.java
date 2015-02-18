/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

import java.net.URL;
import org.eclipse.core.boot.IPlatformConfiguration.IFeatureEntry;
import org.eclipse.update.configurator.IPlatformConfiguration;

public class FeatureEntry implements IFeatureEntry {
	private IPlatformConfiguration.IFeatureEntry newFeatureEntry;

	public FeatureEntry(IPlatformConfiguration.IFeatureEntry fe) {
		newFeatureEntry = fe;
	}

	public String getFeatureIdentifier() {
		return newFeatureEntry.getFeatureIdentifier();
	}

	public String getFeatureVersion() {
		return newFeatureEntry.getFeatureVersion();
	}

	public String getFeaturePluginIdentifier() {
		return newFeatureEntry.getFeaturePluginIdentifier();
	}

	public String getFeaturePluginVersion() {
		return newFeatureEntry.getFeaturePluginVersion();
	}

	public String getFeatureApplication() {
		return newFeatureEntry.getFeatureApplication();
	}

	public URL[] getFeatureRootURLs() {
		return newFeatureEntry.getFeatureRootURLs();
	}

	public boolean canBePrimary() {
		return newFeatureEntry.canBePrimary();
	}

	public IPlatformConfiguration.IFeatureEntry getNewFeatureEntry() {
		return newFeatureEntry;
	}

	public boolean equals(Object o) {
		if (o instanceof FeatureEntry) {
			return newFeatureEntry.equals(((FeatureEntry) o).newFeatureEntry);
		}
		return false;
	}

	public int hashCode() {
		return newFeatureEntry.hashCode();
	}
}
