package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
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
		String query = "";
		int indx = url.indexOf("?");
		if (indx != -1) {
			query = url.substring(indx + 1);
			url = url.substring(1, indx);
		} else
			url = url.substring(1);
		if (url.startsWith(TempURL.getPrefix())) // "/temp"
			return new TempURL(url.substring(TempURL.getPrefix().length() + 1), query);
		else
			return new PluginURL(url, query);
	}
}