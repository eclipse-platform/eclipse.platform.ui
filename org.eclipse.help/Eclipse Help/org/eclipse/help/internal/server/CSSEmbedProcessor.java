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
	private static final String href =
		PluginURL.getPrefix() +"/org.eclipse.help/" + Resources.getString("CSS_location");
	private static final byte[] cssLink =
		("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"" + href + "\">")
			.getBytes();
	public CSSEmbedProcessor() {
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
}
