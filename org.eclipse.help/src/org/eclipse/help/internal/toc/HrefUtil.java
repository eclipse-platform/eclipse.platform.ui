package org.eclipse.help.internal.toc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.ResourceLocator;
import org.eclipse.help.internal.HelpPlugin;

public class HrefUtil {
	/**
	 * Creates /pluginid/href from href
	 * relative to the current plugin
	 * @param pluginID id of a plugin to which href is relative
	 * @param href relative href
	 *  ex: path[#anchorID]
	 *  ex: ../pluginID/path[#anchorID]
	 * @return String representation of href,
	 * formatted as /pluginID/path[#anchorID]
	 */
	public static String normalizeHref(String pluginID, String href) {
		if (href == null)
			return null;
		if (href.startsWith("/"))
			// already normalized
			return href;
		if(href.startsWith("http://"))
			// external doc
			return href;
		int ddIndex = href.indexOf("../");
		if (ddIndex == 0) {
			return href.substring(2);
		} else {
			if (href.length() > 0)
				return "/" + pluginID + "/" + href;
			else
				return "/" + pluginID;
		}
	}
	/**
	 * Parses href and obtains plugin id
	 * @param href String in format /string1[/string2]
	 * @return plugin ID, or null
	 */
	public static String getPluginIDFromHref(String href) {
		if (href == null || href.length() < 2 || href.charAt(0) != '/')
			return null;
		int secondSlashIx = href.indexOf("/", 1);
		if (secondSlashIx < 0) // href is /pluginID
			return href.substring(1);
		// href is /pluginID/path[#anchorID]
		return href.substring(1, secondSlashIx);
	}

	/**
	 * Parses href and obtains resource path relative to the plugin
	 * @param href String in format /string1[/[string2]][#string3]
	 * @return relative resource path, or null
	 */
	public static String getResourcePathFromHref(String href) {
		if (href == null)
			return null;
		// drop anchor id
		int anchorIx = href.lastIndexOf("#");
		if (anchorIx >= 0) //anchor exists, drop it
			href = href.substring(0, anchorIx);
		if (href.length() < 2 || href.charAt(0) != '/')
			return null;
		int secondSlashIx = href.indexOf("/", 1);
		if (secondSlashIx < 0) // href is /pluginID
			return null;
		if (secondSlashIx + 1 < href.length()) // href is /pluginID/path
			return href.substring(secondSlashIx+1);
		else // href is /pluginID/
			return "";
	}
	
}

