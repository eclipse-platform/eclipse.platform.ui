package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;

/**
 * Site on the File System
 */
public class SiteFile extends Site {
	

	/**
	 * 
	 */
	public ISiteContentConsumer createSiteContentConsumer(IFeature feature) throws CoreException {
		SiteFileContentConsumer consumer = new SiteFileContentConsumer(feature);
		consumer.setSite(this);
		return consumer;
	}
	
	public String getDefaultPackagedFeatureType() {
		return DEFAULT_INSTALLED_FEATURE_TYPE;
	}	
}