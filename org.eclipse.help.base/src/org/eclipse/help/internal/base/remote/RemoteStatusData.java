/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base.remote;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;

public class RemoteStatusData {


	private static final String INDEXJSP = "/index.jsp"; //$NON-NLS-1$
	
	/*
	 * Convience method to see if any remote help
	 * is down
	 */
	public static boolean isAnyRemoteHelpUnavailable()
	{
		ArrayList sites = getRemoteSites();
		if (sites.isEmpty())
			return false;
		
		ArrayList badSites = checkSitesConnectivity(sites);
		if (badSites.isEmpty())
			return false;
		
		return true;
	}

	/*
	 * Checks each URL in the ArrayList site to see if
	 * a network connection can be opened to 
	 * url+/index.jsp
	 * 
	 * Returns a subset of sites that cannot be connected.
	 * May be empty, or may be the same as sites
	 */
	public static ArrayList checkSitesConnectivity(ArrayList sites)
	{
		ArrayList badSites = new ArrayList();
		
		for (int i=0;i<sites.size();i++)
		{
			URL baseURL = (URL)sites.get(i);
			try{
				URL indexURL = new URL(baseURL.toExternalForm()+INDEXJSP);
				
				InputStream in = indexURL.openStream();
				in.close();
			
			}catch(Exception ex)
			{
				badSites.add(baseURL);
			}
		}
		return badSites;
	}
	
	/*
	 * Loads the remote sites stored in preferences,
	 * and places them as URLs in an ArrayList.
	 * 
	 * Returns the ArrayList with sites in URL form
	 */
	public static ArrayList getRemoteSites()
	{
		ArrayList sites = new ArrayList();
		
		boolean remoteHelpEnabled = 
			Platform.getPreferencesService().getBoolean(
					HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, false,null);
		
		if (!remoteHelpEnabled)
			return sites;

		String hosts[] = Platform.getPreferencesService().getString(
				HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_HOST, "", null).split(","); //$NON-NLS-1$ //$NON-NLS-2$
		String paths[] = Platform.getPreferencesService().getString(
				HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_PATH, "", null).split(","); //$NON-NLS-1$ //$NON-NLS-2$
		String ports[] = Platform.getPreferencesService().getString(
				HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_PORT, "", null).split(","); //$NON-NLS-1$ //$NON-NLS-2$
		String enableds[] = Platform.getPreferencesService().getString(
				HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_REMOTE_HELP_ICEnabled, "", null).split(","); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		for (int i=0;i<hosts.length;i++)
		{
			try{
				if (enableds[i].equalsIgnoreCase("true")) //$NON-NLS-1$
				{
					URL url = new URL(
							"http://"+hosts[i]+':'+ports[i]+paths[i]); //$NON-NLS-1$
					sites.add(url);
				}
			}
			catch(Exception ex){
			}
		}
		return sites;
	}
}
