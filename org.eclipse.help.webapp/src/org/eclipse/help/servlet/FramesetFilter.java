/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

package org.eclipse.help.servlet;

import javax.servlet.http.*;

/**
 * This class inserts a script for showing the page inside the appropriate frameset
 * when bookmarked.
 */
public class FramesetFilter implements IFilter {
	private static final String scriptPart1 =
		"<script>if( self == top ){ window.location.replace( \"";
	private static final String scriptPart3 = "\");}</script>";
	private StringBuffer location;

	public FramesetFilter(HttpServletRequest req) {
		location = new StringBuffer();
		String path = req.getPathInfo();
		if (path != null) {
			for (int i;
				0 <= (i = path.indexOf('/'));
				path = path.substring(i + 1)) {
				location.append("../");
			}
			location.append("?topic=");
			location.append(req.getPathInfo().substring("/help:".length()));
		}
	}
	/*
	 * @see IFilter#filter(byte[])
	 */
	public byte[] filter(byte[] input) {
		String script = scriptPart1 + location + scriptPart3;
		return HeadFilterHelper.filter(input, script.getBytes());
	}
}
