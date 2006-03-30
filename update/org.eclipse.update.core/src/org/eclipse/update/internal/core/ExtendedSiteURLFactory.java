package org.eclipse.update.internal.core;

import org.eclipse.update.core.model.SiteModel;

public class ExtendedSiteURLFactory extends SiteURLFactory {
	
	public SiteModel createSiteMapModel() {
		return new ExtendedSite();
	}

	
    //public SiteFeatureReferenceModel createFeatureReferenceModel() {
    //    return new UpdateSiteLiteFeatureReference();
    //}
}
