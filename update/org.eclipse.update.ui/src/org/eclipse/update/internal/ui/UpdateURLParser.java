package org.eclipse.update.internal.ui;

import java.net.URL;
import org.eclipse.core.runtime.Platform;
import java.io.*;
import java.util.*;

public class UpdateURLParser {
	public static final String UPDATE_PROTOCOL="update://";
	public static final String HTML_ROOT = "html/";
	public static final String ACTION = "action/";
	/**
	 * Parse the incoming URL and decide if it is for us.
	 * If it is, return null if it is just an action,
	 * or a redirection URL to resolve it to our
	 * own location. 
	 */
	public String parseURL(String url) {
		URL location = UpdateUIPlugin.getDefault().getDescriptor().getInstallURL();
		try {
			location = Platform.resolve(location);
		}
		catch (IOException e) {
			return null;
		}
		String realURL = url.substring(UPDATE_PROTOCOL.length());
		if (realURL.startsWith(ACTION)) {
			parseAction(realURL.substring(ACTION.length()));
			return null;
		}
		
		String loc = location.toString();
		if (loc.endsWith("/"))
		   loc = loc.substring(0, loc.length()-1);
		realURL = loc + "/" + HTML_ROOT + realURL;
		if (realURL.endsWith("/"))
		   realURL = realURL.substring(0, realURL.length()-1);
	   return realURL;		
	}
	
	public boolean isUpdateURL(String url) {
		return url.startsWith(UPDATE_PROTOCOL);
	}
	
	private void parseAction(String actionCode) {
		// the form is <action_id>?par=value+par=value+par=value
		int loc = actionCode.indexOf('?');
		Hashtable params = null;
		String id;
		if (loc == -1)
		   id = actionCode;
		else
		   id = actionCode.substring(0, loc);
		if (loc!= -1) {
			params = parseParams(actionCode.substring(loc+1));
		}
		executeAction(id, params);
	}
	
	private Hashtable parseParams(String params) {
		Hashtable table = new Hashtable();
		StringTokenizer stok = new StringTokenizer(params, "+");
		while (stok.hasMoreTokens()) {
			String keyValuePair = stok.nextToken();
			int eq = keyValuePair.indexOf('=');
			String key;
			String value;
			if (eq!= -1) {
				key = keyValuePair.substring(0, eq);
				value = keyValuePair.substring(eq+1);
				table.put(key, value);
			}
		}
		return table;
	}
	
	private void executeAction(String id, Hashtable params) {
		IURLAction action = UpdateUIPlugin.getDefault().getURLAction(id);
		if (action!=null) {
			action.run(params);
		}
	}
}