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
	public static final String HTTP = "http://";
	private Vector segments;
	private boolean addVerticalSpace = true;
	
	public Paragraph(boolean addVerticalSpace) {
		this.addVerticalSpace = addVerticalSpace;
	}
	
	public int getIndent() {
		return 0;
	}
	
	public boolean getAddVerticalSpace() {
		return addVerticalSpace;
	}

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
	
	public void parseRegularText(String text, boolean expandURLs, HyperlinkSettings settings, String fontId) {
		if (text.length()==0) return;
		if (expandURLs) {
			int loc = text.indexOf(HTTP);
			
			if (loc == -1)
			   addSegment(new TextSegment(text, fontId));
			else {
				int textLoc = 0;
				while (loc != -1) {
					addSegment(new TextSegment(text.substring(textLoc, loc), fontId));
					boolean added=false;
					for (textLoc=loc; textLoc<text.length(); textLoc++) {
						char c = text.charAt(textLoc);
						if (Character.isSpaceChar(c)) {
							addHyperlinkSegment(text.substring(loc, textLoc), settings, fontId);
							added=true;
							break;
						}
					}
					if (!added) {
						// there was no space - just end of text
						addHyperlinkSegment(text.substring(loc), settings, fontId);
						break;
					}
					loc = text.indexOf(HTTP, textLoc);
				}
				if (textLoc<text.length()) {
					addSegment(new TextSegment(text.substring(textLoc), fontId));
				}
			}
		}
		else {
			addSegment(new TextSegment(text, fontId));
		}
	}
	
	private void addHyperlinkSegment(String text, HyperlinkSettings settings, String fontId) {
		HyperlinkSegment hs = new HyperlinkSegment(text, settings, fontId);
		hs.setWordWrapAllowed(false);
		hs.setActionId(FormEngine.URL_HANDLER_ID);
		addSegment(hs);
	}
}