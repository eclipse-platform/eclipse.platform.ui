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
	private static final String scriptPart1 = "<script>if( self == top ){ window.location = window.location.protocol + \"//\" + window.location.host +\"";
	private static final String scriptPart3 = "window.location.href;}</script>";
	private String location;

	public FramesetFilter(HttpServletRequest req) {
		location = req.getContextPath() + "?topic=\"+";
	}
	/*
	 * @see IFilter#filter(byte[])
	 */
	public byte[] filter(byte[] input){
		String script = scriptPart1 + location + scriptPart3;
		return HeadFilterHelper.filter(input, script.getBytes());
	}
}


