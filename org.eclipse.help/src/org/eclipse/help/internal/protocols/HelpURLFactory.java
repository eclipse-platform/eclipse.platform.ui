package org.eclipse.help.internal.protocols;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.*;
/**
 * Creates the URL objects according to the their type
 */
public class HelpURLFactory {
	/**
	 * HelpURLFactory constructor.
	 */
	public HelpURLFactory() {
		super();
	}
	public static HelpURL createHelpURL(String url) {
		if (url == null || url.length() == 0)
			return new PluginURL("", "");
		// Strip off the leading "/" and the query
		if (url.startsWith("/"))
			url = url.substring(1);
		String query = "";
		int indx = url.indexOf("?");
		if (indx != -1) {
			query = url.substring(indx + 1);
			url = url.substring(0, indx);
		} 
		
		if (url.startsWith(TocURL.getPrefix())) // "toc"
			return new TocURL(url.substring(TocURL.getPrefix().length()), query);
		else if (url.startsWith(PreferencesURL.getPrefix())) // "preferences"
			return new PreferencesURL("", "");
		else
			return new PluginURL(url, query);
	}
}
