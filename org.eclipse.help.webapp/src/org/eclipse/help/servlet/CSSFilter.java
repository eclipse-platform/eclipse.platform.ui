/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
package org.eclipse.help.servlet;


/**
 * This class inserts links to a CSS file in HTML code
 */
public class CSSFilter implements IFilter {
	private static final byte[] css = 
	("<style type=\"text/css\">" +
	"body {scrollbar-highlight-color:ThreeDShadow;" +
	"scrollbar-shadow-color:ThreeDShadow;" +
	"scrollbar-arrow-color:#000000;" +
	"scrollbar-darkshadow-color:Window;" +
	"scrollbar-face-color:ButtonFace;" +
	"border-right:1px solid black; border-bottom:1px solid black;}" +
	"</style>").getBytes();
		
	public CSSFilter() {
	}
	/*
	 * @see IFilter#filter(byte[])
	 */
	public byte[] filter(byte[] input){
		return HeadFilterHelper.filter(input, css);
	}
}


