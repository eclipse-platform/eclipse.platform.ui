package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;

/**
 * 
 */

public class InternalSiteManager {

	public static ILocalSite localSite;

	public static final String DEFAULT_SITE_TYPE =
		SiteURLContentProvider.SITE_TYPE;
	private static final String DEFAULT_EXECUTABLE_SITE_TYPE =
		SiteFileContentProvider.SITE_TYPE;

	/**
	 * Returns the LocalSite i.e the different sites
	 * the user has access to (either read only or read write)
	 */
	public static ILocalSite getLocalSite() throws CoreException {
		if (localSite == null) {
			localSite = SiteLocal.getLocalSite();
		}
		return localSite;
	}

	/** 
	 * Returns an ISite based on the protocol of the URL
	 * If the Site has a different Type/Site Handler not known up to now,
	 * it will be discovered when parsing the site.xml file.
	 */
	public static ISite getSite(URL siteURL) throws CoreException {
		ISite site = null;

		if (siteURL == null)
			return null;

		try {
			site = attemptCreateSite(DEFAULT_SITE_TYPE, siteURL);
		} catch (CoreException preservedException) {
			try {
				site = attemptCreateSite(DEFAULT_EXECUTABLE_SITE_TYPE, siteURL);
			} catch (CoreException retryException) {

				IStatus firstStatus = preservedException.getStatus();
				IStatus retry =
					new Status(
						IStatus.INFO,
						firstStatus.getPlugin(),
						IStatus.OK,
						" ** Retry accessing site using default installed format instead of default packaged format because of previous log",
						null);
				//$NON-NLS-1$
				IStatus retryStatus = retryException.getStatus();

				UpdateManagerPlugin.getPlugin().getLog().log(firstStatus);
				UpdateManagerPlugin.getPlugin().getLog().log(retry);
				UpdateManagerPlugin.getPlugin().getLog().log(retryStatus);

				throw preservedException;
			}
		}

		return site;
	}

	/**
	 * Attempt to create a site
	 * if the site guessed is not the type found,
	 * attempt to create a type with the type found in the site.xml
	 */
	private static ISite attemptCreateSite(String guessedTypeSite, URL siteURL)
		throws CoreException {
		ISite site = null;

		try {
			site = createSite(guessedTypeSite, siteURL);
		} catch (InvalidSiteTypeException e) {

			//DEBUG:
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_TYPE) {
				UpdateManagerPlugin.getPlugin().debug(
					"The Site :"
						+ siteURL.toExternalForm()
						+ " is a different type than the guessed type based on the protocol. new Type:"
						+ e.getNewType());
				//$NON-NLS-1$ //$NON-NLS-2$
			}

			// the type in the site.xml is not the one expected	
			// attempt to use this type instead			
			try {
				InvalidSiteTypeException exception = (InvalidSiteTypeException) e;
				if (exception.getNewType() == null)
					throw e;
				site = createSite(exception.getNewType(), siteURL);
			} catch (InvalidSiteTypeException e1) {
				throw Utilities.newCoreException(
					Policy.bind(
						"InternalSiteManager.UnableToCreateSiteWithType",
						e.getNewType(),
						siteURL.toExternalForm()),
					e1);
				//$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		return site;
	}

	/**
	 * create an instance of a class that implements ISite
	 * 
	 * the URL can be of the following form
	 * 1 protocol://...../
	 * 2 protocol://.....
	 * 3 protocol://..../site.xml
	 * 4 protocol://...#...
	 * 
	 * 1 If the file of the file of teh url ends with '/', attempt to open the stream.
	 * if it fails, add site.xml and attempt to open the stream
	 * 
	 * 2 attempt to open the stream
	 * 	fail
	 * 		add '/site.xml' and attempt to open the stream
	 * 	sucess
	 * 		attempt to parse, if it fails, add '/site.xml' and attempt to open the stream
	 * 
	 * 3 open the stream
	 * 
	 * 4 open the stream	
	 */
	private static ISite createSite(String siteType, URL url)
		throws CoreException, InvalidSiteTypeException {
		ISite site = null;
		ISiteFactory factory = SiteTypeFactory.getInstance().getFactory(siteType);

		try {

			site = factory.createSite(url);

		} catch (CoreException e) {
			// if the URL is pointing to either a file 
			// or a directory, without reference			
			if (url.getRef() != null) {
				// 4 nothing we can do
				throw Utilities.newCoreException(
					Policy.bind(
						"InternalSiteManager.UnableToAccessURL",
						url.toExternalForm(),
						e.getMessage()),
					e);
				//$NON-NLS-1$
			} else if (url.getFile().endsWith("/")) { //$NON-NLS-1$
				// 1 try to add site.xml
				URL urlRetry;
				try {
					urlRetry = new URL(url, Site.SITE_XML);
				} catch (MalformedURLException e1) {
					throw Utilities.newCoreException(
						Policy.bind(
							"InternalSiteManager.UnableToCreateURL",
							url.toExternalForm() + "+" + Site.SITE_XML),
						e1);
					//$NON-NLS-1$ //$NON-NLS-2$
				}
				try {
					site = factory.createSite(urlRetry);
				} catch (CoreException e1) {
					throw Utilities.newCoreException(
						Policy.bind(
							"InternalSiteManager.UnableToAccessURL",
							url.toExternalForm(),
							e.getMessage()),
						url.toExternalForm(),
						urlRetry.toExternalForm(),
						e,
						e1);
					//$NON-NLS-1$
				}
			} else if (url.getFile().endsWith(Site.SITE_XML)) {
				// 3 nothing we can do
				throw Utilities.newCoreException(
					Policy.bind(
						"InternalSiteManager.UnableToAccessURL",
						url.toExternalForm(),
						e.getMessage()),
					e);
				//$NON-NLS-1$
			} else {
				// 2 try to add /site.xml 
				URL urlRetry;
				try {
					urlRetry =
						new URL(
							url.getProtocol(),
							url.getHost(),
							url.getPort(),
							url.getFile() + "/" + Site.SITE_XML);
					//$NON-NLS-1$
				} catch (MalformedURLException e1) {
					throw Utilities.newCoreException(
						Policy.bind(
							"InternalSiteManager.UnableToCreateURL",
							url.toExternalForm() + "+" + Site.SITE_XML),
						e1);
					//$NON-NLS-1$ //$NON-NLS-2$
				}

				try {
					site = factory.createSite(urlRetry);
				} catch (CoreException e1) {
					throw Utilities.newCoreException(
						Policy.bind(
							"InternalSiteManager.UnableToAccessURL",
							url.toExternalForm(),
							e.getMessage()),
						url.toExternalForm(),
						urlRetry.toExternalForm(),
						e,
						e1);
					//$NON-NLS-1$
				}
			}

		}

		return site;
	}

	/**
	 * Creates a new site on the file system
	 * This is the only Site we can create.
	 * 
	 * @param siteLocation
	 * @throws CoreException
	 */
	public static ISite createSite(File siteLocation) throws CoreException {
		ISite site = null;
		if (siteLocation != null) {
			try {
				URL siteURL = siteLocation.toURL();
				site = getSite(siteURL);
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(
					Policy.bind(
						"InternalSiteManager.UnableToCreateURL",
						siteLocation.getAbsolutePath()),
					e);
				//$NON-NLS-1$
			}
		}
		return site;
	}


}