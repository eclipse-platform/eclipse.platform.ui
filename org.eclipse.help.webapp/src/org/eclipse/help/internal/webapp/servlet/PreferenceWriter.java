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

package org.eclipse.help.internal.webapp.servlet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.internal.webapp.WebappResources;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Used by the about.html page to display help system preferences
 */

public class PreferenceWriter {	
	private StringBuffer buf;
	private Locale locale;
	
	public PreferenceWriter(StringBuffer buf, Locale locale) {
		this.buf = buf;
		this.locale = locale;
	}
	
	public void writePreferences() {
		writePreference("org.eclipse.help"); //$NON-NLS-1$
		writePreference("org.eclipse.help.base"); //$NON-NLS-1$
	}
	
	private void writePreference(String plugin) {
		try {
		    InstanceScope instanceScope = new InstanceScope();
		    IEclipsePreferences prefs = instanceScope.getNode(plugin);
			Set keySet = new HashSet();
			keySet.addAll(Arrays.asList(prefs.keys()));
		    DefaultScope defaultScope = new DefaultScope();
		    prefs = defaultScope.getNode(plugin);
			keySet.addAll(Arrays.asList(prefs.keys()));
			String[] allKeys = (String[]) keySet.toArray(new String[keySet.size()]);
			if (allKeys.length > 0) {
			    Arrays.sort(allKeys);
			    buf.append("<h3>"); //$NON-NLS-1$
			    buf.append(plugin);
			    buf.append("</h3>"); //$NON-NLS-1$
			    buf.append("<table>");  //$NON-NLS-1$
			    for (int i = 0; i < allKeys.length; i++) {
			    	buf.append("<tr>"); //$NON-NLS-1$
			    	String key = allKeys[i];
			    	buf.append("<td>"); //$NON-NLS-1$
			    	buf.append(UrlUtil.htmlEncode(key));
			    	buf.append("</td><td>"); //$NON-NLS-1$
			    	String value = Platform.getPreferencesService().getString
			    			(plugin, key, "", null); //$NON-NLS-1$
					buf.append(UrlUtil.htmlEncode(value)); 
					buf.append("</td></tr>"); //$NON-NLS-1$
			    }
			    buf.append("</table>"); //$NON-NLS-1$
			}
		} catch (BackingStoreException e) {
			buf.append(WebappResources.getString("badPreferences", locale)); //$NON-NLS-1$
		}
	}

}
