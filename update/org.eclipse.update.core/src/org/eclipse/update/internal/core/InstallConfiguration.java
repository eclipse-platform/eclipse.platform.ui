package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.update.core.*;

public class InstallConfiguration implements IInstallConfiguration {
	
	private boolean isCurrent;
	private List installSites ;
	private List linkedSites ;
	private List features ;
	
	/*
	 * default constructor. Create
	 */ 
	public InstallConfiguration() {
		this.isCurrent = true;
	}

	/*
	 * @see IInstallConfiguration#getFeatures()
	 */
	public IFeatureReference[] getFeatures() {
		
		IFeatureReference[] result = new IFeatureReference[0];
		
		// initialize if needed
		if (features==null){
			features = new ArrayList();
			//FIXME: what about startup
			//don't they resolve the plugin list
			if (installSites!=null){			
				Iterator iter = installSites.iterator();
				while (iter.hasNext()) {
					ISite currentSite = (ISite) iter.next();
					features.addAll(Arrays.asList(currentSite.getFeatureReferences()));		
				}
			}
			if (linkedSites!=null){
				Iterator iter = linkedSites.iterator();
				while (iter.hasNext()) {
					ISite currentSite = (ISite) iter.next();
					features.addAll(Arrays.asList(currentSite.getFeatureReferences()));		
				}
			}
		}

System.out.println(features);
		if (features!=null && !features.isEmpty()){
			// move List in Array
			result = new IFeatureReference[features.size()];
			features.toArray(result);	
		}		
		
		return result;
	}

	/*
	 * @see IInstallConfiguration#getInstallSites()
	 */
	public ISite[] getInstallSites() {
		ISite[] sites = new ISite[0];
		if (installSites!=null && !installSites.isEmpty()) {
			sites = new ISite[installSites.size()];
			installSites.toArray(sites);
		}
		return sites;
	}

	/*
	 * @see IInstallConfiguration#addInstallSite(ISite)
	 */
	public void addInstallSite(ISite site) {
		if (!isCurrent) return;
		if (installSites==null){
			installSites = new ArrayList();
		}
		installSites.add(site);
	}

	/*
	 * @see IInstallConfiguration#removeInstallSite(ISite)
	 */
	public void removeInstallSite(ISite site) {
		if (!isCurrent) return;
		//FIXME: remove should make sure we synchronize
		if (installSites!=null){
			installSites.remove(site);
		}
	}

	/*
	 * @see IInstallConfiguration#getLinkedSites()
	 */
	public ISite[] getLinkedSites() {
		ISite[] sites = new ISite[0];
		if (linkedSites!=null){
			sites = new ISite[linkedSites.size()];
			linkedSites.toArray(sites);
		}
		return sites;
	}

	/*
	 * @see IInstallConfiguration#addLinkedSite(ISite)
	 */
	public void addLinkedSite(ISite site) {
		if (!isCurrent) return;
		if (linkedSites==null){
			linkedSites = new ArrayList();
		}
		linkedSites.add(site);
	}

	/*
	 * @see IInstallConfiguration#removeLinkedSite(ISite)
	 */
	public void removeLinkedSite(ISite site) {
		if (!isCurrent) return;
		//FIXME: remove should make sure we synchronize
		if (linkedSites!=null){
			linkedSites.remove(site);
		}
	}

	/*
	 * @see IInstallConfiguration#isCurrent()
	 */
	public boolean isCurrent() {
		return isCurrent;
	}

}

