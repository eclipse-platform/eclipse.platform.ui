package org.eclipse.update.internal.core;

import java.net.URL;

import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.ILocalSite;
import org.eclipse.update.core.ISite;

public class LocalSite extends FileSite implements ILocalSite {

	/*
	 * Constructor for LocalSite
	 */
	public LocalSite(URL siteReference) {
		super(siteReference);
	}

	/*
	 * @see ILocalSite#getCurrentConfiguration()
	 */
	public IInstallConfiguration getCurrentConfiguration() {
		return null;
	}

	/*
	 * @see ILocalSite#getConfigurationHistory()
	 */
	public IInstallConfiguration[] getConfigurationHistory() {
		return new IInstallConfiguration[0];
	}

	/*
	 * @see ILocalSite#getInstallSites()
	 */
	public ISite[] getInstallSites() {
		return new ISite[0];
	}

	/*
	 * @see ILocalSite#addInstallSite(ISite)
	 */
	public void addInstallSite(ISite site) {
	}

	/*
	 * @see ILocalSite#removeInstallSite(ISite)
	 */
	public void removeInstallSite(ISite site) {
	}

	/*
	 * @see ILocalSite#getLinkedSites()
	 */
	public ISite[] getLinkedSites() {
		return new ISite[0];
	}

	/*
	 * @see ILocalSite#addLinkedSite(ISite)
	 */
	public void addLinkedSite(ISite site) {
	}

	/*
	 * @see ILocalSite#removeLinkedSite(ISite)
	 */
	public void removeLinkedSite(ISite site) {
	}

}

