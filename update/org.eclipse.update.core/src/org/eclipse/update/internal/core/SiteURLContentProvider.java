package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.internal.core.*;
/**
 * 
 */
public class SiteURLContentProvider extends SiteContentProvider {
	
	public static final String SITE_TYPE = "org.eclipse.update.core.http";

	/**
	 * Constructor for HTTPSite
	 */
	public SiteURLContentProvider(URL url) {
		super(url);
	}

	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * @see ISite#getDefaultExecutableFeatureType()
	 */
	public String getDefaultExecutableFeatureType() {
		return null;
	}

	/*
	 * @see ISite#getDefaultInstallableFeatureType()
	 */
	public String getDefaultInstallableFeatureType() {
		String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier()+".";		
		return pluginID+IFeatureFactory.INSTALLABLE_FEATURE_TYPE;
	}
	/*
	 * @see ISiteContentProvider#getArchiveReference(String)
	 */
	public URL getArchiveReference(String archiveId)  throws CoreException {
		URL contentURL = null;
		try {
			contentURL = getArchiveURLfor(archiveId);
			
			// if there is no mapping in the site.xml
			// for this archiveId, use the default one
			if (contentURL==null) {
				String protocol = getURL().getProtocol();
				String host = getURL().getHost();
				String path = UpdateManagerUtils.getPath(getURL());			
				contentURL = new URL(protocol,host,path+archiveId);
			}
			
		} catch (MalformedURLException e){
			String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
			IStatus status = new Status(IStatus.ERROR,id,IStatus.OK,"Error creating URL",e);
			throw new CoreException(status);	
		}		
		return contentURL;
	}

	/**
	 * return the URL associated with the id of teh archive for this site
	 * return null if the archiveId is null, empty or 
	 * if teh list of archives on the site is null or empty
	 * of if there is no URL associated with the archiveID for this site
	 */
	private URL getArchiveURLfor(String archiveId) {
		URL result = null;
		boolean found = false;

		IArchiveEntry[] siteArchives = getSite().getArchives();
		if (siteArchives.length > 0) {
			for (int i = 0; i < siteArchives.length; i++) {
				if (archiveId.trim().equalsIgnoreCase(siteArchives[i].getPath())) {
					result = siteArchives[i].getURL();
					found = true;
					break;
				}
			}
		}
		return result;
	}



	}

