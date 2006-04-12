/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.update.internal.model.SiteWithTimestamp;

public class ExtendedSite extends SiteWithTimestamp /*Site*/ {
	
	private boolean digestExist;
	private String[] availableLanguages;
	private String[] availableLocals;
	private String digestURL;
	private LiteFeature[] liteFeatures;
	private boolean pack200 = false;
	
	public String getDigestURL() {
		return digestURL;
	}
	public void setDigestURL(String digestURL) {
		this.digestURL = digestURL;
	}
	public String[] getAvailableLanguages() {
		return availableLanguages;
	}
	public void setAvailableLanguages(String[] availableLanguages) {
		this.availableLanguages = availableLanguages;
	}
	public String[] getAvailableLocals() {
		return availableLocals;
	}
	public void setAvailableLocals(String[] availableLocals) {
		this.availableLocals = availableLocals;
	}
	public boolean isDigestExist() {
		return digestExist;
	}
	public void setDigestExist(boolean digestExist) {
		this.digestExist = digestExist;
	}
	public LiteFeature[] getLiteFeatures() {
		if (getCurrentConfiguredSite()!=null)
			return filterFeatures(getNonFilteredLiteFeatures());
		else 
			return getNonFilteredLiteFeatures();
	}
	public void setLiteFeatures(LiteFeature[] liteFeatures) {
		this.liteFeatures = liteFeatures;
	}
	
	public LiteFeature[] getNonFilteredLiteFeatures() {
		return liteFeatures;
	}
	public void setNonFilteredLiteFeatures(LiteFeature[] liteFeatures) {
		this.liteFeatures = liteFeatures;
	}
	
	
	/**
	 * Get whether or not this site may contain jars packed with pack200.
	 * The packed version of foo.jar, is expected to be foo.jar.pack.gz
	 * @return
	 */
	public boolean supportsPack200() {
		return pack200;
	}
	
	/**
	 * Set whether or not this site may contain jars packed with pack200
	 * The packed version of foo.jar is expected to be foo.jar.pack.gz
	 * @param pack
	 */
	public void setSupportsPack200(boolean pack){
		pack200 = pack;
	}
	
	/*
	 * Method filterFeatures.
	 * Also implemented in Feature
	 *  
	 * @param list
	 * @return List
	 */
	private LiteFeature[] filterFeatures(LiteFeature[] allIncluded) {
		List list = new ArrayList();
		if (allIncluded!=null){
			for (int i = 0; i < allIncluded.length; i++) {
				LiteFeature included = allIncluded[i];
				if (UpdateManagerUtils.isValidEnvironment(included))
					list.add(included);
				else{
					if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_WARNINGS){
						UpdateCore.warn("Filtered out feature reference:"+included); //$NON-NLS-1$
					}
				}
			}
		}
		
		LiteFeature[] result = new LiteFeature[list.size()];
		if (!list.isEmpty()){
			list.toArray(result);
		}
		
		return result;	
	}

}
