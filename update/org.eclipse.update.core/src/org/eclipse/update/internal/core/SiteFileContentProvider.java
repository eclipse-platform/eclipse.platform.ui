package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.InvalidSiteTypeException;

/**
 * Site on the File System
 */
public class SiteFileContentProvider extends SiteContentProvider {
	
	private String path;
	
	public static final String INSTALL_FEATURE_PATH = "install/features/";	
	public static final String SITE_TYPE = "org.eclipse.update.core.file";	

	/**
	 * Constructor for FileSite
	 */
	public SiteFileContentProvider(URL url) {
		super(url);
	}

	
	
	/**
 	 * move into contentSelector, comment to provider and consumer (SiteFile)
 	 */
	private String getFeaturePath(VersionedIdentifier featureIdentifier) {
		String path = UpdateManagerUtils.getPath(getURL());
		String featurePath = path + INSTALL_FEATURE_PATH + featureIdentifier.toString();
		return featurePath;
	}

	/**
	 * We do not need to optimize the download
	 * As the archives are already available on the file system
	 */
	public boolean optimize() {
		return false;
	}
		
	/*
	 * @see ISite#getDefaultExecutableFeatureType()
	 */
	public String getDefaultExecutableFeatureType() {
		String pluginID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier()+".";
		return pluginID+IFeatureFactory.EXECUTABLE_FEATURE_TYPE;
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


