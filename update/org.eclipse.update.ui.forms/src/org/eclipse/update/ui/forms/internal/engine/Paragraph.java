/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import java.util.Vector;
import java.util.StringTokenizer;
import org.eclipse.update.ui.forms.internal.HyperlinkSettings;

/**
 * @version 	1.0
 * @author
 */
public class Paragraph implements IParagraph {
	private Vector segments;

	/*
	 * @see IParagraph#getSegments()
	 */
	public IParagraphSegment[] getSegments() {
		if (segments == null)
			return new IParagraphSegment[0];
		return (IParagraphSegment[]) segments.toArray(
			new IParagraphSegment[segments.size()]);
	}

	public void addSegment(IParagraphSegment segment) {
		if (segments == null)
			segments = new Vector();
		segments.add(segment);
	}
	
	public void parseRegularText(String text, boolean expandURLs, HyperlinkSettings settings) {
		if (expandURLs) {
			int loc = text.indexOf("http://");
			
			if (loc == -1)
			   addSegment(new TextSegment(text));
			else {
				int textLoc = 0;
				while (loc != -1) {
					addSegment(new TextSegment(text.substring(textLoc, loc)));
					for (textLoc=loc; textLoc<text.length(); textLoc++) {
						char c = text.charAt(textLoc);
						if (Character.isSpaceChar(c)) {
							addSegment(new HyperlinkSegment(text.substring(loc, textLoc), settings));
							break;
						}
					}
					loc = text.indexOf("http://", textLoc);
				}
				if (textLoc<text.length()) {
					addSegment(new TextSegment(text.substring(textLoc)));
				}
			}
		}
		else {
			addSegment(new TextSegment(text));
		}
	}
}