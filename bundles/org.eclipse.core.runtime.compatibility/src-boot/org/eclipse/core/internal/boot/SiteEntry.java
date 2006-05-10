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
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy;
import org.eclipse.update.configurator.IPlatformConfiguration;

public class SiteEntry implements ISiteEntry {
	private org.eclipse.update.configurator.IPlatformConfiguration.ISiteEntry newSiteEntry;

	public SiteEntry(IPlatformConfiguration.ISiteEntry entry) {
		newSiteEntry = entry;
	}

	public URL getURL() {
		return newSiteEntry.getURL();
	}

	public ISitePolicy getSitePolicy() {
		return new SitePolicy(newSiteEntry.getSitePolicy());
	}

	public void setSitePolicy(ISitePolicy policy) {
		newSiteEntry.setSitePolicy(((SitePolicy) policy).getNewPolicy());
	}

	public String[] getFeatures() {
		return newSiteEntry.getFeatures();
	}

	public String[] getPlugins() {
		return newSiteEntry.getPlugins();
	}

	public long getChangeStamp() {
		return newSiteEntry.getChangeStamp();
	}

	public long getFeaturesChangeStamp() {
		return newSiteEntry.getFeaturesChangeStamp();
	}

	public long getPluginsChangeStamp() {
		return newSiteEntry.getPluginsChangeStamp();
	}

	public boolean isUpdateable() {
		return newSiteEntry.isUpdateable();
	}

	public boolean isNativelyLinked() {
		return newSiteEntry.isNativelyLinked();
	}

	public org.eclipse.update.configurator.IPlatformConfiguration.ISiteEntry getNewSiteEntry() {
		return newSiteEntry;
	}

	public boolean equals(Object o) {
		if (o instanceof SiteEntry)
			return newSiteEntry.equals(((SiteEntry) o).newSiteEntry);
		return false;
	}

	public int hashCode() {
		return newSiteEntry.hashCode();
	}
}
