/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base;
import java.net.*;

import org.eclipse.core.boot.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.context.*;

/**
 * This class provides methods to display help.
 * It is independent of platform UI.
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
	public void displayHelp() {
		// Do not start help view if documentaton is not available, display error
		if (HelpSystem.getTocs().length == 0) {
			// There is no documentation
			BaseHelpSystem.getDefaultErrorUtil().displayError(
				HelpBaseResources.getString("WW001"));
			//Documentation is not installed.
			return;
		}

		displayHelpURL(null);
	}

	/**
	 * Displays a help resource specified as a url. 
	 * <ul>
	 *  <li>a URL in a format that can be returned by
	 * 	{@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
	 * 	<li>a URL query in the format format <em>key=value&amp;key=value ...</em>
	 *  The valid keys are: "tab", "toc", "topic", "contextId".
	 *  For example, <em>toc="/myplugin/mytoc.xml"&amp;topic="/myplugin/references/myclass.html"</em>
	 *  is valid.
	 * </ul>
	 */
	public void displayHelpResource(String href) {
		// check if this is a toc
		IToc toc = HelpPlugin.getTocManager().getToc(href, BootLoader.getNL());
		if (toc != null)
			displayHelpURL("toc=" + URLEncoder.encode(toc.getHref()));
		else if (
			href != null
				&& (href.startsWith("tab=")
					|| href.startsWith("toc=")
					|| href.startsWith("topic=")
					|| href.startsWith(
						"contextId="))) { // assume it is a query string
			displayHelpURL(href);
		} else // assume this is a topic
			if (getNoframesURL(href) == null) {
				displayHelpURL("topic=" + URLEncoder.encode(href));
			} else {
				displayHelpURL(getNoframesURL(href));
			}
	}

	/**
	 * Display help for the a given topic and related topics.
	 * @param topic topic to be displayed by the help browse
	 * @param relatedTopics topics that will populate related topics view
	 */
	public void displayHelp(IContext context, IHelpResource topic) {
		if (context == null || topic == null || topic.getHref() == null)
			return;
		String topicURL = getTopicURL(topic.getHref());
		if (getNoframesURL(topicURL) == null) {
			String url =
				"tab=links"
					+ "&contextId="
					+ URLEncoder.encode(getContextID(context))
					+ "&topic="
					+ URLEncoder.encode(topicURL);
			displayHelpURL(url);
		} else {
			displayHelpURL(getNoframesURL(topicURL));
		}
	}
	/**
	 * Display help to search view for given query
	 * and selected topic.
	 * @param query search query in URL format key=value&key=value
	 * @param topic selected from the search results
	 */
	public void displaySearch(String searchQuery, String topic) {
		if (searchQuery == null || topic == null)
			return;
		if (getNoframesURL(topic) == null) {
			String url =
				"tab=search&"
					+ searchQuery
					+ "&topic="
					+ URLEncoder.encode(getTopicURL(topic));
			displayHelpURL(url);
		} else {
			displayHelpURL(getNoframesURL(topic));
		}
	}
	/**
	 * Displays the specified url.
	 * The url can contain query parameters to identify how help displays the document
	 */
	private void displayHelpURL(String helpURL) {
		if (!BaseHelpSystem.ensureWebappRunning()) {
			return;
		}

		try {
			if (helpURL == null || helpURL.length() == 0) {
				BaseHelpSystem.getHelpBrowser().displayURL(getBaseURL());
			} else if (
				helpURL.startsWith("tab=")
					|| helpURL.startsWith("toc=")
					|| helpURL.startsWith("topic=")
					|| helpURL.startsWith("contextId=")) {
				BaseHelpSystem.getHelpBrowser().displayURL(
					getBaseURL() + "?" + helpURL);
			} else {
				BaseHelpSystem.getHelpBrowser().displayURL(helpURL);
			}
		} catch (Exception e) {
			BaseHelpSystem.getDefaultErrorUtil().displayError(e.getMessage());
		}
	}
	private String getContextID(IContext context) {
		if (context instanceof Context)
			return ((Context) context).getID();
		return HelpPlugin.getContextManager().addContext(context);
	}

	private String getBaseURL() {
		return "http://"
			+ WebappManager.getHost()
			+ ":"
			+ WebappManager.getPort()
			+ "/help/index.jsp";
	}

	private String getTopicURL(String topic) {
		if (topic == null)
			return null;
		if (topic.startsWith("../"))
			topic = topic.substring(2);
		/*
		if (topic.startsWith("/")) {
		String base = "http://" + AppServer.getHost() + ":" + AppServer.getPort();
		base += "/help/content/help:";
		topic = base + topic;
		}
		*/
		return topic;
	}

	/**
	 * If href contains URL parameter noframes=true
	 * return href with that paramter removed, otherwise returns null
	 * @param href
	 * @return String or null
	 */
	private String getNoframesURL(String href) {
		if (href == null) {
			return null;
		}
		int ix = href.indexOf("?noframes=true&");
		if (ix >= 0) {
			//remove noframes=true&
			return href.substring(0, ix + 1)
				+ href.substring(ix + "?noframes=true&".length());

		}
		ix = href.indexOf("noframes=true");
		if (ix > 0) {
			//remove &noframes=true
			return href.substring(0, ix - 1)
				+ href.substring(ix + "noframes=true".length());
		}
		// can be displayed in frames
		return null;
	}

}
