package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URLEncoder;

import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.util.URLCoder;

/**
 * This class is an implementation of the pluggable help support.
 * In is registered into the support extension point, and all 
 * requests to display help are delegated to this class.
 * The methods on this class interact with the actual
 * UI component handling the display
 */
public class DefaultHelp implements IHelp
 {
	private static DefaultHelp instance;
	private ContextHelpDialog f1Dialog = null;
	private int idCounter = 0;
	/**
	 * BaseHelpViewer constructor.
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
		displayHelp(null);
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
		if (!AppServer.isRunning())
			return; // may want to display an error message
		String base = "http://" + AppServer.getHost() + ":" + AppServer.getPort();
		String query = "";
		if (toc != null) {
			query = "?toc=" + toc;
			if (topic != null)
				query = query + "&topic=" + URLEncoder.encode(createTopicURL(topic));
		} else {
			if (topic != null)
				query = "?topic=" + URLEncoder.encode(createTopicURL(topic));
		}
		String url =
			"http://" + AppServer.getHost() + ":" + AppServer.getPort() + "/help" + query;
		WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(url);
	}	
	/**
	 * Displays context-sensitive help for specified context
	 * @param contextIds context identifier
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(String contextId, int x, int y) {
		IContext context = HelpSystem.getContextManager().getContext(contextId);
		displayHelp(context, x, y);
	}
		
	/**
	 * Displays context-sensitive help for specified context
	 * @param contexts the context to display
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayHelp(IContext context, int x, int y) {
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
	 * Display help for the a given topic and related topics.
	 * @param topic topic to be displayed by the help browser
	 * @param relatedTopics topics that will populate related topics view
	 */
	public void displayHelp(IContext context, IHelpResource topic) {
		if (context == null || topic == null || topic.getHref() == null)
			return;
		String contextID = getContextID(context);
		if (!AppServer.isRunning())
			return; // may want to display an error message
		String url =
			"http://"
				+ AppServer.getHost()
				+ ":"
				+ AppServer.getPort()
				+ "/help?tab=links&contextId="
				+ URLEncoder.encode(contextID)
				+ "&topic="
				+ URLEncoder.encode(createTopicURL(topic.getHref()));
		WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(url);
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
		if (!AppServer.isRunning())
			return; // may want to display an error message
		String url =
			"http://"
				+ AppServer.getHost()
				+ ":"
				+ AppServer.getPort()
				+ "/help?tab=search&"
				+ searchQuery
				+ "&topic="
				+ URLEncoder.encode(createTopicURL(topic));
		WorkbenchHelpPlugin.getDefault().getHelpBrowser().displayURL(url);
	}	/**
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
		return HelpSystem.getTocManager().getTocs();
	}

	private String getContextID(IContext context) {
		if (context instanceof Context)
			return ((Context) context).getID();
		if (context instanceof ContextProxy)
			return ((ContextProxy) context).getID();
		// TODO add code not to generate new ID for the same context
		String id = "org.eclipse.help.ID" + idCounter++;
		HelpSystem.getContextManager().addContext(id, context);
		return id;
	}
	
	private String createTopicURL(String topic) {
		if (topic == null)
			return null;
		if (topic.startsWith("../"))
			topic = topic.substring(2);
		if (topic.startsWith("/")) {
			String base = "http://" + AppServer.getHost() + ":" + AppServer.getPort();
			base += "/help/content/help:";
			topic = base + topic;
		}
		return topic;
	}
}