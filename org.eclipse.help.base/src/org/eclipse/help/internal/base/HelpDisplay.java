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
package org.eclipse.help.internal.base;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.context.*;
import org.eclipse.osgi.util.NLS;

/**
 * This class provides methods to display help. It is independent of platform
 * UI.
 */
public class HelpDisplay {

	/**
	 * Constructor.
	 */
	public HelpDisplay() {
		super();
	}

	/**
	 * Displays help.
	 */
	public void displayHelp(boolean forceExternal) {
		// Do not start help view if documentaton is not available, display
		// error
		if (HelpSystem.getTocs().length == 0) {
			HelpBasePlugin.logError(
					"Failed launching help.  Documentation is not installed.", //$NON-NLS-1$
					null);
			// There is no documentation
			BaseHelpSystem.getDefaultErrorUtil()
					.displayError(
							HelpBaseResources.HelpDisplay_docsNotInstalled);
			//Documentation is not installed.
			return;
		}

		displayHelpURL(null, forceExternal);
	}

	/**
	 * Displays a help resource specified as a url.
	 * <ul>
	 * <li>a URL in a format that can be returned by
	 * {@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
	 * <li>a URL query in the format format
	 * <em>key=value&amp;key=value ...</em> The valid keys are: "tab", "toc",
	 * "topic", "contextId". For example,
	 * <em>toc="/myplugin/mytoc.xml"&amp;topic="/myplugin/references/myclass.html"</em>
	 * is valid.
	 * </ul>
	 */
	public void displayHelpResource(String href, boolean forceExternal) {
		// check if this is a toc
		IToc toc = HelpPlugin.getTocManager().getToc(href, Platform.getNL());
		if (toc != null)
			try {
				displayHelpURL(
						"toc=" + URLEncoder.encode(toc.getHref(), "UTF-8"), forceExternal); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (UnsupportedEncodingException uee) {
			}
		else if (href != null && (href.startsWith("tab=") //$NON-NLS-1$
				|| href.startsWith("toc=") //$NON-NLS-1$
				|| href.startsWith("topic=") //$NON-NLS-1$
		|| href.startsWith("contextId="))) { //$NON-NLS-1$ // assume it is a query string
			displayHelpURL(href, forceExternal);
		} else { // assume this is a topic
			if (getNoframesURL(href) == null) {
				try {
					displayHelpURL(
							"topic=" + URLEncoder.encode(href, "UTF-8"), forceExternal); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (UnsupportedEncodingException uee) {
				}
			} else if (href.startsWith("jar:file:")) { //$NON-NLS-1$
				// topic from a jar to display without frames
				displayHelpURL(
						getBaseURL() + "nftopic/" + getNoframesURL(href), true); //$NON-NLS-1$
			} else {
				displayHelpURL(getNoframesURL(href), true);
			}
		}
	}

	/**
	 * Display help for the a given topic and related topics.
	 * 
	 * @param context
	 *            context for which related topics will be displayed
	 * @param topic
	 *            related topic to be selected
	 */
	public void displayHelp(IContext context, IHelpResource topic,
			boolean forceExternal) {
		if (context == null || topic == null || topic.getHref() == null)
			return;
		String topicURL = getTopicURL(topic.getHref());
		if (getNoframesURL(topicURL) == null) {
			try {
				String url = "tab=links" //$NON-NLS-1$
						+ "&contextId=" //$NON-NLS-1$
						+ URLEncoder.encode(getContextID(context), "UTF-8") //$NON-NLS-1$
						+ "&topic=" //$NON-NLS-1$
						+ URLEncoder.encode(topicURL, "UTF-8"); //$NON-NLS-1$
				displayHelpURL(url, forceExternal);
			} catch (UnsupportedEncodingException uee) {
			}

		} else if (topicURL.startsWith("jar:file:")) { //$NON-NLS-1$
			// topic from a jar to display without frames
			displayHelpURL(
					getBaseURL() + "nftopic/" + getNoframesURL(topicURL), true); //$NON-NLS-1$
		} else {
			displayHelpURL(getNoframesURL(topicURL), true);
		}
	}

	/**
	 * Display help to search view for given query and selected topic.
	 * 
	 * @param searchQuery
	 *            search query in URL format key=value&key=value
	 * @param topic
	 *            selected from the search results
	 */
	public void displaySearch(String searchQuery, String topic,
			boolean forceExternal) {
		if (searchQuery == null || topic == null)
			return;
		if (getNoframesURL(topic) == null) {
			try {
				String url = "tab=search&" //$NON-NLS-1$
						+ searchQuery + "&topic=" //$NON-NLS-1$
						+ URLEncoder.encode(getTopicURL(topic), "UTF-8"); //$NON-NLS-1$
				displayHelpURL(url, forceExternal);
			} catch (UnsupportedEncodingException uee) {
			}

		} else {
			displayHelpURL(getNoframesURL(topic), true);
		}
	}

	/**
	 * Displays the specified url. The url can contain query parameters to
	 * identify how help displays the document
	 */
	private void displayHelpURL(String helpURL, boolean forceExternal) {
		if (!BaseHelpSystem.ensureWebappRunning()) {
			return;
		}
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
			// wait for Display to be created
			DisplayUtils.waitForDisplay();
		}

		try {
			/*
			if (helpURL == null || helpURL.length() == 0) {
				BaseHelpSystem.getHelpBrowser(forceExternal).displayURL(
						getFramesetURL());
			} else if (helpURL.startsWith("tab=") //$NON-NLS-1$
					|| helpURL.startsWith("toc=") //$NON-NLS-1$
					|| helpURL.startsWith("topic=") //$NON-NLS-1$
					|| helpURL.startsWith("contextId=")) { //$NON-NLS-1$
				BaseHelpSystem.getHelpBrowser(forceExternal).displayURL(
						getFramesetURL() + "?" + helpURL); //$NON-NLS-1$
			} else {
				BaseHelpSystem.getHelpBrowser(forceExternal)
						.displayURL(helpURL);
			}
			*/
			if (helpURL == null || helpURL.length() == 0) {
				helpURL = getFramesetURL();
			} else if (helpURL.startsWith("tab=") //$NON-NLS-1$
					|| helpURL.startsWith("toc=") //$NON-NLS-1$
					|| helpURL.startsWith("topic=") //$NON-NLS-1$
					|| helpURL.startsWith("contextId=")) { //$NON-NLS-1$
				helpURL = getFramesetURL() + "?" + helpURL; //$NON-NLS-1$
			}
			BaseHelpSystem.getHelpBrowser(forceExternal)
						.displayURL(helpURL);
		} catch (Exception e) {
			HelpBasePlugin
					.logError(
							"An exception occurred while launching help.  Check the log at " + Platform.getLogFileLocation().toOSString(), e); //$NON-NLS-1$
			BaseHelpSystem.getDefaultErrorUtil()
					.displayError(
							NLS.bind(HelpBaseResources.HelpDisplay_exceptionMessage, Platform.getLogFileLocation().toOSString()));
		}
	}

	private String getContextID(IContext context) {
		if (context instanceof Context)
			return ((Context) context).getID();
		return HelpPlugin.getContextManager().addContext(context);
	}

	private String getBaseURL() {
		return "http://" //$NON-NLS-1$
				+ WebappManager.getHost() + ":" //$NON-NLS-1$
				+ WebappManager.getPort() + "/help/"; //$NON-NLS-1$
	}

	private String getFramesetURL() {
		return getBaseURL() + "index.jsp"; //$NON-NLS-1$
	}

	private String getTopicURL(String topic) {
		if (topic == null)
			return null;
		if (topic.startsWith("../")) //$NON-NLS-1$
			topic = topic.substring(2);
		/*
		 * if (topic.startsWith("/")) { String base = "http://" +
		 * AppServer.getHost() + ":" + AppServer.getPort(); base +=
		 * "/help/content/help:"; topic = base + topic; }
		 */
		return topic;
	}

	/**
	 * If href contains URL parameter noframes=true return href with that
	 * paramter removed, otherwise returns null
	 * 
	 * @param href
	 * @return String or null
	 */
	private String getNoframesURL(String href) {
		if (href == null) {
			return null;
		}
		int ix = href.indexOf("?noframes=true&"); //$NON-NLS-1$
		if (ix >= 0) {
			//remove noframes=true&
			return href.substring(0, ix + 1)
					+ href.substring(ix + "?noframes=true&".length()); //$NON-NLS-1$

		}
		ix = href.indexOf("noframes=true"); //$NON-NLS-1$
		if (ix > 0) {
			//remove &noframes=true
			return href.substring(0, ix - 1)
					+ href.substring(ix + "noframes=true".length()); //$NON-NLS-1$
		}
		// can be displayed in frames
		return null;
	}

}
