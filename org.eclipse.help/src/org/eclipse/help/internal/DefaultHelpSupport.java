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
package org.eclipse.help.internal;
import java.net.*;

import org.eclipse.core.boot.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.util.*;

/**
 * This class is the default implementation of the pluggable help support.
 * In is registered into the support extension point, and all 
 * requests to display help are delegated to this class.
 */
public class DefaultHelpSupport implements IHelp {

	private int idCounter = 0;

	/**
	 * BaseHelpViewer constructor.
	 */
	public DefaultHelpSupport() {
		super();
	}

	/**
	 * Displays help.
	 */
	public void displayHelp() {
		// Do not start help view if documentaton is not available, display error
		if (getTocs().length == 0) {
			// There is no documentation
			HelpSystem.getDefaultErrorUtil().displayError(
				Resources.getString("WW001"));
			//Documentation is not installed.
			return;
		}

		displayHelpURL(null);
	}

	/**
	 * Displays context-sensitive help for specified context
	 * @param contexts the context to display
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayContext(IContext context, int x, int y) {
		// no implementation
	}

	/**
	 * Displays context-sensitive help for specified context
	 * @param contextIds context identifier
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayContext(String contextId, int x, int y) {
		IContext context = HelpSystem.getContextManager().getContext(contextId);
		displayContext(context, x, y);
	}

	/**
	 * Displays a help resource
	 */
	public void displayHelpResource(IHelpResource helpResource) {
		if (helpResource instanceof IToc)
			displayHelpURL("toc=" + URLEncoder.encode(helpResource.getHref()));
		else if (helpResource instanceof ITopic)
			displayHelpURL(
				"topic="
					+ URLEncoder.encode(getTopicURL(helpResource.getHref())));
		else
			displayHelpResource(helpResource.getHref());
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
		IToc toc = HelpSystem.getTocManager().getToc(href, BootLoader.getNL());
		if (toc != null)
			displayHelpResource(toc);
		else if (
			href != null
				&& (href.startsWith("tab=")
					|| href.startsWith("toc=")
					|| href.startsWith("topic=")
					|| href.startsWith(
						"contextId="))) { // assume it is a query string
			displayHelpURL(href);
		} else // assume this is a topic
			displayHelpURL("topic=" + URLEncoder.encode(href));
	}

	/**
	 * Displays the specified table of contents.
	 */
	public void displayHelp(String tocFileHref) {
		displayHelp(tocFileHref, null);
	}
	/**
	 * Display help and selected specified topic.
	 */
	public void displayHelp(
		String toc,
		String topic) { // Do not start help view if documentaton is not available, display error
		if (getTocs().length == 0) {
			// There is no documentation
			HelpSystem.getDefaultErrorUtil().displayError(
				Resources.getString("WW001"));
			//Documentation is not installed.
			return;
		}

		String query = null;
		if (toc != null) {
			query = "toc=" + toc;
			if (topic != null)
				query =
					query + "&topic=" + URLEncoder.encode(getTopicURL(topic));
		} else {
			if (topic != null)
				query = "topic=" + URLEncoder.encode(getTopicURL(topic));
		}

		displayHelpURL(query);
	}
	/**
	 * Displays context-sensitive help for specified context
	 * @deprecated
	 */
	public void displayHelp(String contextId, int x, int y) {
		displayContext(contextId, x, y);
	}
	/**
	 * Displays context-sensitive help for specified context
	 * @deprecated
	 */
	public void displayHelp(IContext context, int x, int y) {
		displayContext(context, x, y);
	}
	/**
	 * Display help for the a given topic and related topics.
	 * @param topic topic to be displayed by the help browse
	 * @param relatedTopics topics that will populate related topics view
	 */
	public void displayHelp(IContext context, IHelpResource topic) {
		if (context == null || topic == null || topic.getHref() == null)
			return;
		String url =
			"tab=links"
				+ "&contextId="
				+ URLEncoder.encode(getContextID(context))
				+ "&topic="
				+ URLEncoder.encode(getTopicURL(topic.getHref()));
		displayHelpURL(url);
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
		String url =
			"tab=search&"
				+ searchQuery
				+ "&topic="
				+ URLEncoder.encode(getTopicURL(topic));
		displayHelpURL(url);
	}
	/**
	 * Displays the specified url.
	 * The url can contain query parameters to identify how help displays the document
	 */
	void displayHelpURL(String helpURL) {
		if (!HelpSystem.ensureWebappRunning()) {
			HelpSystem.getDefaultErrorUtil().displayError(
				Resources.getString("E043"));
			return;
		}

		try {
			if (helpURL == null || helpURL.length() == 0) {
				HelpSystem.getHelpBrowser().displayURL(getBaseURL());
			} else if (
				helpURL.startsWith("tab=")
					|| helpURL.startsWith("toc=")
					|| helpURL.startsWith("topic=")
					|| helpURL.startsWith("contextId=")) {
				HelpSystem.getHelpBrowser().displayURL(
					getBaseURL() + "?" + helpURL);
			} else {
				HelpSystem.getHelpBrowser().displayURL(helpURL);
			}
		} catch (Exception e) {
			HelpSystem.getDefaultErrorUtil().displayError(e.getMessage());
		}
	}
	/**
	 * Computes context information for a given context ID.
	 * @param contextID java.lang.String ID of the context
	 * @return IContext
	 */
	public IContext getContext(String contextID) {
		//return HelpSystem.getContextManager().getContext(contextID);
		return new ContextProxy(contextID);
	}
	/**
	 * Returns the list of all integrated tables of contents available.
	 * @return an array of TOC's
	 */
	public IToc[] getTocs() {
		return HelpSystem.getTocManager().getTocs(BootLoader.getNL());
	}

	/**
	 * Returns <code>true</code> if the context-sensitive help
	 * window is currently being displayed, <code>false</code> if not.
	 */
	public boolean isContextHelpDisplayed() {
		return false;
	}

	private String getContextID(IContext context) {
		if (context instanceof Context)
			return ((Context) context).getID();
		if (context instanceof ContextProxy)
			return ((ContextProxy) context).getID();
		// TODO add code not to generate new ID for the same context
		String id = HelpPlugin.PLUGIN_ID + ".ID" + idCounter++;
		HelpSystem.getContextManager().addContext(id, context);
		return id;
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
}
