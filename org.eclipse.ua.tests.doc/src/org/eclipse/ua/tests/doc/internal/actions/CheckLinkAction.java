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

package org.eclipse.ua.tests.doc.internal.actions;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.help.internal.base.HelpBasePlugin;

public class CheckLinkAction implements ILiveHelpAction {
	
	private static Map links = new HashMap();
	private String link;
	public final static String ALL_PAGES_LOADED = "ALL_PAGES_LOADED";
	public final static String CHECK_LINKS = "CHECK_LINKS";

	public void setInitializationString(String data) {
		link = data;
	}

	public void run() {
		//System.out.println("Link = " + link);
		if (ALL_PAGES_LOADED.equals(link)) {
			LoadTocAction.showErrors();
		} else if (CHECK_LINKS.equals(link)) {
			LoadTocAction.showErrors();
			checkLinks();
		} else if (link.startsWith("htt")){
			//String lastPage = LoadTocAction.lastPage;
			links.put(link, VisitPageAction.lastPageVisited);
		}
	}

	private void checkLinks() {
		String errorPage = Platform.getPreferencesService().getString(HelpBasePlugin.PLUGIN_ID, "page_not_found", null, null); //$NON-NLS-1$
		setPageNotFoundPreference("");
		System.out.println("Start check links");
		for (Iterator iter = links.keySet().iterator(); iter.hasNext(); ) {
			String next = (String)iter.next();
			//System.out.println("Process " + next);
			URL url;
			boolean opened;
			try {
				url = new URL(next);
				//URLConnection connection = url.openConnection();
				//connection.
				InputStream input = url.openStream();
			    int nextChar = input.read();
			    if (nextChar == -1) {
			    	System.out.println("Cannot read " + next);
			    	opened = false;
			    } else {
			    	opened = true;
			    }
			    input.close();
			} catch (Exception e) {
				opened = false;
			}
			if (!opened) {
				String containingPage = (String) links.get(next);
				System.out.println("Cannot open link from " + containingPage                       
				       + " to " + next);
			}
		}
		//EclipseConnector.setNotFoundCallout(null);
	    setPageNotFoundPreference(errorPage);
		links = new HashMap();
		System.out.println("End check links");
	}
	
	private void setPageNotFoundPreference(String value) {
		InstanceScope instanceScope = new InstanceScope();
		IEclipsePreferences prefs = instanceScope.getNode(HelpBasePlugin.PLUGIN_ID);
		prefs.put("page_not_found", value);
	}

}
