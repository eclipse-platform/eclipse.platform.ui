package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.update.core.*;

public class InstallConfiguration implements IInstallConfiguration {
	
	private boolean isCurrent;
	private List installSites = new ArrayList();
	private List linkedSites = new ArrayList();
	
	/*
	 * default constructor. Create
	 */ 
	public InstallConfiguration() {
		this.isCurrent = true;
	}

	/*
	 * @see IInstallConfiguration#getFeatures()
	 */
	public IFeature[] getFeatures() {
		return new IFeature[0];
	}

	/*
	 * @see IInstallConfiguration#getInstallSites()
	 */
	public ISite[] getInstallSites() {
		return new ISite[0];
	}

	/*
	 * @see IInstallConfiguration#addInstallSite(ISite)
	 */
	public void addInstallSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#removeInstallSite(ISite)
	 */
	public void removeInstallSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#getLinkedSites()
	 */
	public ISite[] getLinkedSites() {
		return new ISite[0];
	}

	/*
	 * @see IInstallConfiguration#addLinkedSite(ISite)
	 */
	public void addLinkedSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#removeLinkedSite(ISite)
	 */
	public void removeLinkedSite(ISite site) {
		if (!isCurrent) return;
	}

	/*
	 * @see IInstallConfiguration#isCurrent()
	 */
	public boolean isCurrent() {
		return isCurrent;
	}

}

