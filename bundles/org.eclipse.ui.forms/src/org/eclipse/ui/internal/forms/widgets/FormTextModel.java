/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.io.*;
import java.util.Vector;

import javax.xml.parsers.*;

import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.w3c.dom.*;
import org.xml.sax.*;

public class FormTextModel {
	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

	private boolean whitespaceNormalized=true;
	private Vector paragraphs;
	private HyperlinkSegment[] hyperlinks;
	private int selectedLinkIndex = -1;
	private HyperlinkSettings hyperlinkSettings;
	public static final String BOLD_FONT_ID = "f.____bold";
	
	public FormTextModel() {
		reset();
	}

	/*
	 * @see ITextModel#getParagraphs()
	 */
	public Paragraph[] getParagraphs() {
		if (paragraphs == null)
			return new Paragraph[0];
		return (Paragraph[]) paragraphs.toArray(
			new Paragraph[paragraphs.size()]);
	}
	
	public String getAccessibleText() {
		if (paragraphs == null)
			return "";
		StringBuffer sbuf = new StringBuffer();
		for (int i=0; i<paragraphs.size(); i++) {
			Paragraph paragraph  = (Paragraph)paragraphs.get(i);
			String text = paragraph.getAccessibleText();
			sbuf.append(text);
		}
		return sbuf.toString();
	}

	/*
	 * @see ITextModel#parse(String)
	 */
	public void parseTaggedText(String taggedText, boolean expandURLs) {
		if (taggedText==null) {
			reset();
			return;
		}
		try {
			InputStream stream =
				new ByteArrayInputStream(taggedText.getBytes("UTF8"));
			parseInputStream(stream, expandURLs);
		} catch (UnsupportedEncodingException e) {
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT, e);
		}
	}

	public void parseInputStream(InputStream is, boolean expandURLs) {
			
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setIgnoringComments(true);
	
		reset();
		try {
			DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
			InputSource source = new InputSource(is);
			Document doc = parser.parse(source);
			processDocument(doc, expandURLs);
		} catch (ParserConfigurationException e) {
			SWT.error(SWT.ERROR_CANNOT_SET_TEXT, e);
		} catch (SAXException e) {
			SWT.error(SWT.ERROR_CANNOT_SET_TEXT, e);
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	}

	private void processDocument(Document doc, boolean expandURLs) {
		Node root = doc.getDocumentElement();
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				// Make an implicit paragraph
				String text = getSingleNodeText(child);
				if (text != null && !isIgnorableWhiteSpace(text, true)) {
					Paragraph p = new Paragraph(true);
					p.parseRegularText(
						text,
						expandURLs,
						getHyperlinkSettings(),
						null);
					paragraphs.add(p);
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				String tag = child.getNodeName().toLowerCase();
				if (tag.equals("p")) {
					Paragraph p = processParagraph(child, expandURLs);
					if (p != null)
						paragraphs.add(p);
				} else if (tag.equals("li")) {
					Paragraph p = processListItem(child, expandURLs);
					if (p != null)
						paragraphs.add(p);
				}
			}
		}
	}
	private Paragraph processParagraph(Node paragraph, boolean expandURLs) {
		NodeList children = paragraph.getChildNodes();
		NamedNodeMap atts = paragraph.getAttributes();
		Node addSpaceAtt = atts.getNamedItem("addVerticalSpace");
		boolean addSpace = true;
		
		if (addSpaceAtt==null)
			addSpaceAtt = atts.getNamedItem("vspace");

		if (addSpaceAtt != null) {
			String value = addSpaceAtt.getNodeValue();
			addSpace = value.equalsIgnoreCase("true");
		}
		Paragraph p = new Paragraph(addSpace);

		processSegments(p, children, expandURLs);
		return p;
	}
	private Paragraph processListItem(Node listItem, boolean expandURLs) {
		NodeList children = listItem.getChildNodes();
		NamedNodeMap atts = listItem.getAttributes();
		Node addSpaceAtt = atts.getNamedItem("addVerticalSpace");
		Node styleAtt = atts.getNamedItem("style");
		Node valueAtt = atts.getNamedItem("value");
		Node indentAtt = atts.getNamedItem("indent");
		Node bindentAtt = atts.getNamedItem("bindent");
		int style = BulletParagraph.CIRCLE;
		int indent = -1;
		int bindent = -1;
		String text = null;
		boolean addSpace = true;

		if (addSpaceAtt != null) {
			String value = addSpaceAtt.getNodeValue();
			addSpace = value.equalsIgnoreCase("true");
		}
		if (styleAtt != null) {
			String value = styleAtt.getNodeValue();
			if (value.equalsIgnoreCase("text")) {
				style = BulletParagraph.TEXT;
			} else if (value.equalsIgnoreCase("image")) {
				style = BulletParagraph.IMAGE;
			}
			else if (value.equalsIgnoreCase("bullet")) {
				style = BulletParagraph.CIRCLE;
			}
		}
		if (valueAtt != null) {
			text = valueAtt.getNodeValue();
			if (style==BulletParagraph.IMAGE)
				text = "i."+text;
		}
		if (indentAtt != null) {
			String value = indentAtt.getNodeValue();
			try {
				indent = Integer.parseInt(value);
			} catch (NumberFormatException e) {
			}
		}
		if (bindentAtt != null) {
			String value = bindentAtt.getNodeValue();
			try {
				bindent = Integer.parseInt(value);
			} catch (NumberFormatException e) {
			}
		}

		BulletParagraph p = new BulletParagraph(addSpace);
		p.setIndent(indent);
		p.setBulletIndent(bindent);
		p.setBulletStyle(style);
		p.setBulletText(text);

		processSegments(p, children, expandURLs);
		return p;
	}

	private void processSegments(
		Paragraph p,
		NodeList children,
		boolean expandURLs) {
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			ParagraphSegment segment = null;

			if (child.getNodeType() == Node.TEXT_NODE) {
				String value = getSingleNodeText(child);

				if (value != null && !isIgnorableWhiteSpace(value, false)) {
					p.parseRegularText(
						value,
						expandURLs,
						getHyperlinkSettings(),
						null);
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				String name = child.getNodeName();
				if (name.equalsIgnoreCase("img")) {
					segment = processImageSegment(child);
				} else if (name.equalsIgnoreCase("a")) {
					segment =
						processHyperlinkSegment(child, getHyperlinkSettings());
				} else if (name.equalsIgnoreCase("span")) {
					processTextSegment(p, expandURLs, child);
				} else if (name.equalsIgnoreCase("b")) {
					String text = getNodeText(child);
					String fontId = BOLD_FONT_ID;
					p.parseRegularText(
						text,
						expandURLs,
						getHyperlinkSettings(),
						fontId);
				} else if (name.equalsIgnoreCase("br")) {
					segment = new BreakSegment();
				}
			}
			if (segment != null) {
				p.addSegment(segment);
			}
		}
	}

	private boolean isIgnorableWhiteSpace(String text, boolean ignoreSpaces) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (ignoreSpaces && c == ' ')
				continue;
			if (c == '\n' || c == '\r' || c == '\f')
				continue;
			return false;
		}
		return true;
	}

	private ParagraphSegment processImageSegment(Node image) {
		ImageSegment segment = new ImageSegment();
		NamedNodeMap atts = image.getAttributes();
		Node id = atts.getNamedItem("href");
		Node align = atts.getNamedItem("align");
		if (id != null) {
			String value = id.getNodeValue();
			segment.setObjectId("i."+value);
		}
		if (align != null) {
			String value = align.getNodeValue().toLowerCase();
			if (value.equals("top"))
				segment.setVerticalAlignment(ImageSegment.TOP);
			else if (value.equals("middle"))
				segment.setVerticalAlignment(ImageSegment.MIDDLE);
			else if (value.equals("bottom"))
				segment.setVerticalAlignment(ImageSegment.BOTTOM);
		}
		return segment;
	}
	
	private void appendText(String value, StringBuffer buf, int [] spaceCounter) {
		if (!whitespaceNormalized)
			buf.append(value);
		else {
			for (int j=0; j<value.length(); j++) {
				char c = value.charAt(j);
				if (c==' ' || c=='\t') {
					// space
					if (++spaceCounter[0] == 1) {
						buf.append(c);
					}
				}
				else if (c=='\n' || c=='\r' || c=='\f') {
					// new line
					if (++spaceCounter[0]==1) {
						buf.append(' ');
					}
				}
				else {
					// other characters
					spaceCounter[0]=0;
					buf.append(c);
				}
			}
		}
	}
	
	private String getNormalizedText(String text) {
		int [] spaceCounter = new int[1];
		StringBuffer buf = new StringBuffer();
		
		if (text==null) return null;
		appendText(text, buf, spaceCounter);
		return buf.toString();
	}
	
	private String getSingleNodeText(Node node) {
		return getNormalizedText(node.getNodeValue());
	}

	private String getNodeText(Node node) {
		NodeList children = node.getChildNodes();
		StringBuffer buf = new StringBuffer();
		int [] spaceCounter=new int[1];

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				String value = child.getNodeValue();
				appendText(value, buf, spaceCounter);
			}
		}
		return buf.toString().trim();
	}

	private ParagraphSegment processHyperlinkSegment(
		Node link,
		HyperlinkSettings settings) {
		String text = getNodeText(link);
		HyperlinkSegment segment = new HyperlinkSegment(text, settings, null);
		NamedNodeMap atts = link.getAttributes();
		Node href = atts.getNamedItem("href");
		if (href != null) {
			String value = href.getNodeValue();
			segment.setHref(value);
		}
		Node nowrap = atts.getNamedItem("nowrap");
		if (nowrap != null) {
			String value = nowrap.getNodeValue();
			if (value!=null && value.equalsIgnoreCase("true"))
				segment.setWordWrapAllowed(false);
		}
		return segment;
	}

	private void processTextSegment(
		Paragraph p,
		boolean expandURLs,
		Node textNode) {
		String text = getNodeText(textNode);

		NamedNodeMap atts = textNode.getAttributes();
		Node font = atts.getNamedItem("font");
		Node color = atts.getNamedItem("color");
		String fontId = null;
		String colorId = null;
		if (font != null) {
			fontId = "f."+font.getNodeValue();
		}
		if (color != null) {
			colorId = "c."+color.getNodeValue();
		}
		p.parseRegularText(text, expandURLs, getHyperlinkSettings(), fontId, colorId);
	}

	public void parseRegularText(String regularText, boolean convertURLs) {
		reset();
		
		if (regularText==null) return;
		
		regularText = getNormalizedText(regularText);

		Paragraph p = new Paragraph(true);
		paragraphs.add(p);
		int pstart = 0;

		for (int i = 0; i < regularText.length(); i++) {
			char c = regularText.charAt(i);
			if (p == null) {
				p = new Paragraph(true);
				paragraphs.add(p);
			}
			if (c == '\n') {
				String text = regularText.substring(pstart, i);
				pstart = i + 1;
				p.parseRegularText(
					text,
					convertURLs,
					getHyperlinkSettings(),
					null);
				p = null;
			}
		}
		if (p != null) {
			// no new line
			String text = regularText.substring(pstart);
			p.parseRegularText(text, convertURLs, getHyperlinkSettings(), null);
		}
	}

	public HyperlinkSettings getHyperlinkSettings() {
		return hyperlinkSettings;
	}

	public void setHyperlinkSettings(HyperlinkSettings settings) {
		this.hyperlinkSettings = settings;
	}

	private void reset() {
		if (paragraphs == null)
			paragraphs = new Vector();
		paragraphs.clear();
		selectedLinkIndex = -1;
		hyperlinks = null;
	}

	HyperlinkSegment[] getHyperlinks() {
		if (hyperlinks != null || paragraphs == null)
			return hyperlinks;
		Vector result = new Vector();
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph p = (Paragraph) paragraphs.get(i);
			ParagraphSegment[] segments = p.getSegments();
			for (int j = 0; j < segments.length; j++) {
				if (segments[j] instanceof HyperlinkSegment)
					result.add(segments[j]);
			}
		}
		hyperlinks =
			(HyperlinkSegment[]) result.toArray(
				new HyperlinkSegment[result.size()]);
		return hyperlinks;
	}

	public HyperlinkSegment findHyperlinkAt(int x, int y) {
		HyperlinkSegment[] links = getHyperlinks();
		for (int i = 0; i < links.length; i++) {
			if (links[i].contains(x, y))
				return links[i];
		}
		return null;
	}
	public TextSegment findSegmentAt(int x, int y) {
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph p = (Paragraph) paragraphs.get(i);
			TextSegment segment = p.findSegmentAt(x, y);
			if (segment != null)
				return segment;
		}
		return null;
	}

	public HyperlinkSegment getSelectedLink() {
		if (selectedLinkIndex == -1)
			return null;
		return hyperlinks[selectedLinkIndex];
	}

	public boolean traverseLinks(boolean next) {
		HyperlinkSegment[] links = getHyperlinks();
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

	public void selectLink(HyperlinkSegment link) {
		if (link == null)
			selectedLinkIndex = -1;
		else {
			HyperlinkSegment[] links = getHyperlinks();
			selectedLinkIndex = -1;
			if (links == null)
				return;
			for (int i = 0; i < links.length; i++) {
				if (links[i].equals(link)) {
					selectedLinkIndex = i;
					break;
				}
			}
		}
	}

	public boolean hasFocusSegments() {
		HyperlinkSegment[] links = getHyperlinks();
		if (links.length > 0)
			return true;
		return false;
	}

	public void dispose() {
		paragraphs = null;
		selectedLinkIndex = -1;
		hyperlinks = null;
	}
	/**
	 * @return Returns the whitespaceNormalized.
	 */
	public boolean isWhitespaceNormalized() {
		return whitespaceNormalized;
	}
	/**
	 * @param whitespaceNormalized The whitespaceNormalized to set.
	 */
	public void setWhitespaceNormalized(boolean whitespaceNormalized) {
		this.whitespaceNormalized = whitespaceNormalized;
	}
}
