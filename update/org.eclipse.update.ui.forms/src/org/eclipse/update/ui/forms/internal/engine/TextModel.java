/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import java.util.Vector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.ui.forms.internal.*;
;

/**
 * @version 	1.0
 * @author
 */
public class TextModel implements ITextModel {
	Vector paragraphs;
	IHyperlinkListener urlListener;
	IHyperlinkSegment[] hyperlinks;
	int selectedLinkIndex = -1;
	HyperlinkSettings hyperlinkSettings;
	
	/*
	 * @see ITextModel#getParagraphs()
	 */
	public IParagraph[] getParagraphs() {
		if (paragraphs == null)
			return new IParagraph[0];
		return (IParagraph[]) paragraphs.toArray(new IParagraph[paragraphs.size()]);
	}

	/*
	 * @see ITextModel#parse(String)
	 */
	public void parseTaggedText(String taggedText) throws CoreException {
		reset();
	}

	public void parseRegularText(String regularText, boolean convertURLs)
		throws CoreException {
		reset();
		StringBuffer buf = new StringBuffer();

		Paragraph p = new Paragraph();
		paragraphs.add(p);
		int pstart = 0;
		boolean oneLine = true;
		for (int i = 0; i < regularText.length(); i++) {
			char c = regularText.charAt(i);
			if (p == null) {
				p = new Paragraph();
				paragraphs.add(p);
			}
			if (c == '\n') {
				oneLine = false;
				String text = regularText.substring(pstart, i);
				pstart = i + 1;
				p.parseRegularText(text, convertURLs, getHyperlinkSettings());
				p = null;
			}
		}
		if (oneLine && p!=null) {
			// no new line
			p.parseRegularText(regularText, convertURLs, getHyperlinkSettings());
		}
	}
	
	public HyperlinkSettings getHyperlinkSettings() {
		if (hyperlinkSettings==null) {
			hyperlinkSettings = new HyperlinkSettings();
		}
		return hyperlinkSettings;
	}
	
	public void setHyperlinkSettings(HyperlinkSettings settings) {
		this.hyperlinkSettings = settings;
	}

	public void setURLListener(IHyperlinkListener urlListener) {
		this.urlListener = urlListener;
	}

	private void reset() {
		if (paragraphs == null)
			paragraphs = new Vector();
		paragraphs.clear();
		selectedLinkIndex = -1;
		hyperlinks = null;
	}

	IHyperlinkSegment[] getHyperlinks() {
		if (hyperlinks != null || paragraphs == null)
			return hyperlinks;
		Vector result = new Vector();
		for (int i = 0; i < paragraphs.size(); i++) {
			IParagraph p = (IParagraph) paragraphs.get(i);
			IParagraphSegment[] segments = p.getSegments();
			for (int j = 0; j < segments.length; j++) {
				if (segments[j] instanceof IHyperlinkSegment)
					result.add(segments[j]);
			}
		}
		hyperlinks =
			(IHyperlinkSegment[]) result.toArray(new IHyperlinkSegment[result.size()]);
		return hyperlinks;
	}

	public IHyperlinkSegment getSelectedLink() {
		if (selectedLinkIndex == -1)
			return null;
		return hyperlinks[selectedLinkIndex];
	}

	public boolean traverseLinks(boolean next) {
		IHyperlinkSegment[] links = getHyperlinks();
		if (links == null)
			return false;
		int size = links.length;
		if (next) {
			selectedLinkIndex++;
		} else
			selectedLinkIndex--;

		if (selectedLinkIndex < 0 || selectedLinkIndex > size - 1) {
			selectedLinkIndex = -1;
		}
		return selectedLinkIndex != -1;
	}

	public void deselectCurrentLink() {
		selectedLinkIndex = -1;
	}
	
	public void dispose() {
		hyperlinkSettings.dispose();
	}
}