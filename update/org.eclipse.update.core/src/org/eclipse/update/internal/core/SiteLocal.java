package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.ILocalSite;

public class SiteLocal extends SiteFile implements ILocalSite {
	
	private InstallConfiguration config = new InstallConfiguration();

	/*
	 * Constructor for LocalSite
	 */
	public SiteLocal(URL siteReference) throws CoreException {
		super(siteReference);
	}

	/*
	 * @see ILocalSite#getCurrentConfiguration()
	 */
	public IInstallConfiguration getCurrentConfiguration() {
		return config;
	}

	/*
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		return new IInstallConfiguration[0];
	}
}

