package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URLEncoder;
import java.util.Locale;

import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.ui.util.*;

/**
 * This class is an implementation of the pluggable help support.
 * In is registered into the support extension point, and all 
 * requests to display help are delegated to this class.
 * The methods on this class interact with the actual
 * UI component handling the display
 */
public class DefaultHelp implements IHelp {
	private static DefaultHelp instance;
	private ContextHelpDialog f1Dialog = null;
	private int idCounter = 0;
	private final static String defaultLocale = Locale.getDefault().toString();
	/**
	 * DefaultHelp constructor.
	 */
	public DefaultHelp() {
		super();
		instance = this;
	}

	/**
	 * Singleton method
	 */
	public static DefaultHelp getInstance() {
		return instance;
	}

	/**
	 * Displays help.
	 */
	public void displayHelp() {
		// Do not start help view if documentaton is not available, display error
		if (getTocs().length == 0) {
			// There is no documentation
			ErrorUtil.displayErrorDialog(WorkbenchResources.getString("WW001"));
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
		if (f1Dialog != null)
			f1Dialog.close();
		if (context == null)
			return;
		f1Dialog = new ContextHelpDialog(context, x, y);
		f1Dialog.open();
		// if any errors or parsing errors have occurred, display them in a pop-up
		ErrorUtil.displayStatus();
	}

	/**
	 * Displays context-sensitive help for specified context
	 * @param contextIds context identifier
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayContext(String contextId, int x, int y) {
		IContext context =
			HelpSystem.getContextManager().getContext(contextId, defaultLocale);
		displayContext(context, x, y);
	}

	/**
	 * Displays a help resource
	 */
	public void displayHelpResource(IHelpResource helpResource) {
		if (helpResource instanceof IToc)
			displayHelpURL("toc=" + helpResource.getHref());
		else if (helpResource instanceof ITopic)
			displayHelpURL(
				"topic=" + URLEncoder.encode(getTopicURL(helpResource.getHref())));
		else
			displayHelpURL(helpResource.getHref());
	}

	/**
	 * Displays a help resource specified as a url
	 */
	public void displayHelpResource(String href) {
		// check if this is a toc
		IToc toc = HelpSystem.getTocManager().getToc(href, defaultLocale);
		if (toc != null)
			displayHelpResource(toc);
		else if (href != null && href.indexOf('=') != -1) {
			// assume it is a query string
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
	public void displayHelp(String toc, String topic) {
		// Do not start help view if documentaton is not available, display error
		if (getTocs().length == 0) {
			// There is no documentation
			ErrorUtil.displayErrorDialog(WorkbenchResources.getString("WW001"));
			//Documentation is not installed.
			return;
		}

		String query = null;
		if (toc != null) {
			query = "toc=" + toc;
			if (topic != null)
				query = query + "&topic=" + URLEncoder.encode(getTopicURL(topic));
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
	 * @param topic topic to be displayed by the help browser
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
			"tab=search&" + searchQuery + "&topic=" + URLEncoder.encode(getTopicURL(topic));

		displayHelpURL(url);
	}

	/**
	 * Displays the specified url.
	 * The url can contain query parameters to identify how help displays the document
	 */
	void displayHelpURL(String helpURL) {
		if (!AppServer.isRunning())
			return; // may want to display an error message

		if (helpURL == null || helpURL.length() == 0) {
			WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(getBaseURL());
		} else if (
			helpURL.startsWith("tab=")
				|| helpURL.startsWith("toc=")
				|| helpURL.startsWith("topic=")
				|| helpURL.startsWith("contextId=")) {
			WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(
				getBaseURL() + "?" + helpURL);
		} else {
			WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(helpURL);
		}
	}

	/**
	 * Computes context information for a given context ID.
	 * @param contextID java.lang.String ID of the context
	 * @return IContext
	 */
	public IContext getContext(String contextID) {
		//return HelpSystem.getContextManager().getContext(contextID);
		return new ContextProxy(contextID, defaultLocale);
	}
	/**
	 * Returns the list of all integrated tables of contents available.
	 * @return an array of TOC's
	 */
	public IToc[] getTocs() {
		return HelpSystem.getTocManager().getTocs(defaultLocale);
	}

	private String getContextID(IContext context) {
		if (context instanceof Context)
			return ((Context) context).getID();
		if (context instanceof ContextProxy)
			return ((ContextProxy) context).getID();
		// TODO add code not to generate new ID for the same context
		String id = "org.eclipse.help.ID" + idCounter++;
		HelpSystem.getContextManager().addContext(id, context, defaultLocale);
		return id;
	}

	private String getBaseURL() {
		return "http://" + AppServer.getHost() + ":" + AppServer.getPort() + "/help";
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