package org.eclipse.update.internal.core;

import java.net.URL;

import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.ILocalSite;

public class LocalSite extends FileSite implements ILocalSite {

	/**
	 * Constructor for LocalSite
	 */
	public LocalSite(URL siteReference) {
		super(siteReference);
	}

	/**
	 * @see ILocalSite#getCurrentConfiguration()
	 */
	public IInstallConfiguration getCurrentConfiguration() {
		return null;
	}

	/**
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		return null;
	}

}

