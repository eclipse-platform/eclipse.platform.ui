package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.navigation.HelpNavigationManager;
import org.eclipse.help.internal.util.Resources;

/**
 * This class inserts links to a CSS file in HTML code
 */
class CSSEmbedProcessor implements OutputProcessor {
	private static final byte[] headTagBegin = "<head".getBytes();
	private static final byte[] headTagBeginCaps = "<HEAD".getBytes();
	private static final char headTagEnd = '>';
	private static final byte[] cssLinkPart1 =
		("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"")
		.getBytes();
	private static final byte[] cssLinkPart2 =
		("org.eclipse.help/" + Resources.getString("CSS_location") + "\">")
		.getBytes();
	private byte[] cssLink;	
		
	public CSSEmbedProcessor(HelpURL url) {
		// We assume the entire path is does not have special characters.
		byte[] relativePath = getRelativePath(url).getBytes();
		// Build the css link bytes
		int offset = 0;
		cssLink = new byte[cssLinkPart1.length + relativePath.length + cssLinkPart2.length];
		System.arraycopy(cssLinkPart1, 0, cssLink, offset, cssLinkPart1.length);
		offset += cssLinkPart1.length;
		System.arraycopy(relativePath, 0, cssLink, offset, relativePath.length);
		offset += relativePath.length;
		System.arraycopy(cssLinkPart2, 0, cssLink, offset, cssLinkPart2.length);
	}
	public byte[] processOutput(byte[] input) {
		// Create new buffer
		byte[] buffer = new byte[input.length + cssLink.length];
		int bufPointer = 0;
		int inputPointer = 0;
		boolean foundHeadTagBegin = false;
		boolean foundHeadTagEnd = false;
		while (inputPointer < input.length) {
			// copy character
			buffer[bufPointer++] = input[inputPointer++];
			// look for head tag copied
			if (!foundHeadTagEnd
				&& !foundHeadTagBegin
				&& (bufPointer >= headTagBegin.length)) {
				for (int i = 0; i < headTagBegin.length; i++) {
					if ((buffer[bufPointer - headTagBegin.length + i] != headTagBegin[i])
						&& (buffer[bufPointer - headTagBegin.length + i] != headTagBeginCaps[i])) {
						break;
					}
					if (i == headTagBegin.length - 1)
						foundHeadTagBegin = true;
				}
			}
			if (!foundHeadTagEnd && foundHeadTagBegin && buffer[bufPointer - 1] == '>') {
				foundHeadTagEnd = true;
				//embed CSS
				System.arraycopy(cssLink, 0, buffer, bufPointer, cssLink.length);
				bufPointer += cssLink.length;
				// copy rest
				System.arraycopy(
					input,
					inputPointer,
					buffer,
					bufPointer,
					input.length - inputPointer);
				return buffer;
			}
		}
		return buffer;

	}
	
	/**
	 * Returns relative path of the url so we can
	 * properly link the help css file
	 */
	private String getRelativePath(HelpURL url)
	{
		String path = url.getPath();
		// The path starts with "/"
		path = path.substring(1);
		StringBuffer relativePath = new StringBuffer();
		for (int i=path.indexOf('/'); i !=-1; i=path.indexOf('/',i+1))
			relativePath.append("../");
		return relativePath.toString();
	}
}
