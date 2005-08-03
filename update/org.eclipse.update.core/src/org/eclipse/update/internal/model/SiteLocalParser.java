/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.model;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.configurator.*;
import org.eclipse.update.internal.configurator.*;
import org.eclipse.update.internal.core.*;

/**
 * parse the default site.xml
 */

public class SiteLocalParser {

	private PlatformConfiguration platformConfig;
	private SiteLocalModel site;
	public static final String CONFIG = "config"; //$NON-NLS-1$
	private ResourceBundle bundle;
	private BaseSiteLocalFactory factory = new BaseSiteLocalFactory();

	/**
	 * return the appropriate resource bundle for this sitelocal
	 */
	private ResourceBundle getResourceBundle() throws CoreException {
		ResourceBundle bundle = null;
		URL url = null;
		try {
			url = UpdateManagerUtils.asDirectoryURL(site.getLocationURL());
			ClassLoader l = new URLClassLoader(new URL[] { url }, null);
			bundle = ResourceBundle.getBundle("platform", Locale.getDefault(), l); //$NON-NLS-1$
		} catch (MissingResourceException e) {
			UpdateCore.warn(e.getLocalizedMessage() + ":" + url.toExternalForm()); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			UpdateCore.warn(e.getLocalizedMessage()); 
		}
		return bundle;
	}

	/**
	 * Constructor for DefaultSiteParser
	 */
	public SiteLocalParser(IPlatformConfiguration platformConfig, ILocalSite site) throws IOException, CoreException {
		Assert.isTrue(platformConfig instanceof PlatformConfiguration);
		this.platformConfig = (PlatformConfiguration)platformConfig;
		
		Assert.isTrue(site instanceof SiteLocalModel);
		this.site = (SiteLocalModel) site;

		// DEBUG:		
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_PARSING) {
			UpdateCore.debug("Start parsing localsite:" + ((SiteLocalModel) site).getLocationURLString()); //$NON-NLS-1$
		}
		
		bundle = getResourceBundle();
		
		processConfig();
		//processHistory();
	}

	
//	/** 
//	 * process the Site info
//	 */
//	private void processSite(Attributes attributes) throws MalformedURLException {
//		//
//		String info = attributes.getValue("label"); //$NON-NLS-1$
//		info = UpdateManagerUtils.getResourceString(info, bundle);
//		site.setLabel(info);
//	
//		// history
//		String historyString = attributes.getValue("history"); //$NON-NLS-1$
//		int history;
//		if (historyString == null || historyString.equals("")) { //$NON-NLS-1$
//			history = SiteLocalModel.DEFAULT_HISTORY;
//		} else {
//			history = Integer.parseInt(historyString);
//		}
//		site.setMaximumHistoryCount(history);
//	
//		//stamp
//		String stampString = attributes.getValue("stamp"); //$NON-NLS-1$
//		long stamp = Long.parseLong(stampString);
//		site.setStamp(stamp);
//	
//		// DEBUG:		
//		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_PARSING) {
//			UpdateCore.debug("End process Site label:" + info); //$NON-NLS-1$
//		}
//	
//	}

	/** 
	 * process the Config info
	 */
	private void processConfig() throws MalformedURLException, CoreException {

		String label = platformConfig.getConfiguration().getDate().toString();
		label = UpdateManagerUtils.getResourceString(label, bundle);
		site.setLabel(label);

		URL url = site.getLocationURL();
		InstallConfigurationModel config = factory.createInstallConfigurationModel();
		config.setLocationURLString(url.toExternalForm());
		config.setLabel(label);
		config.resolve(url, site.getResourceBundleURL());

		// add the config
		((SiteLocal)site).addConfiguration((InstallConfiguration)config);

		// DEBUG:		
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_PARSING) {
			UpdateCore.debug("End Processing Config Tag: url:" + url.toExternalForm()); //$NON-NLS-1$
		}
	}
}
