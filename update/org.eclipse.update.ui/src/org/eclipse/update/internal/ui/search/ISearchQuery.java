package org.eclipse.update.internal.ui.search;

import org.eclipse.update.core.IFeature;
import java.net.URL;
import org.eclipse.update.internal.ui.model.ISiteAdapter;

public interface ISearchQuery {
/**
 * Returns a URL of an explicit site that needs to 
 * be used for this query. This site is searched first.
 * In addition, local file system and bookmarked sites
 * can be searched as well if selected in search options.
 * @return a url of a site that needs to be searched for
 * this query or <samp>null</samp> if a general scan 
 * should be used.
 */
	public ISiteAdapter getSearchSite();
	
	public IFeature [] getMatchingFeatures(IFeature[] candidates);
}