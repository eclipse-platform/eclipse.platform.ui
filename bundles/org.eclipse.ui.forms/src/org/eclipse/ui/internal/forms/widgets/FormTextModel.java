/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FormTextModel {
	
	/*
	 * This class prevents parse errors from being written to standard output
	 */
	public class ParseErrorHandler implements ErrorHandler {

		public void error(SAXParseException arg0) throws SAXException {
		}

		public void fatalError(SAXParseException arg0) throws SAXException {
		}

		public void warning(SAXParseException arg0) throws SAXException {
		}
	}

	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();

	private boolean whitespaceNormalized = true;

	private Vector paragraphs;

	private IFocusSelectable[] selectableSegments;

	private int selectedSegmentIndex = -1;
	
	private int savedSelectedLinkIndex = -1;

	private HyperlinkSettings hyperlinkSettings;

	public static final String BOLD_FONT_ID = "f.____bold"; //$NON-NLS-1$

	//private static final int TEXT_ONLY_LINK = 1;

	//private static final int IMG_ONLY_LINK = 2;

	//private static final int TEXT_AND_IMAGES_LINK = 3;

	public FormTextModel() {
		reset();
	}

	/*
	 * @see ITextModel#getParagraphs()
	 */
	public Paragraph[] getParagraphs() {
		if (paragraphs == null)
			return new Paragraph[0];
		return (Paragraph[]) paragraphs
				.toArray(new Paragraph[paragraphs.size()]);
	}

	public String getAccessibleText() {
		if (paragraphs == null)
			return ""; //$NON-NLS-1$
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph paragraph = (Paragraph) paragraphs.get(i);
			String text = paragraph.getAccessibleText();
			sbuf.append(text);
		}
		return sbuf.toString();
	}

	/*
	 * @see ITextModel#parse(String)
	 */
	public void parseTaggedText(String taggedText, boolean expandURLs) {
		if (taggedText == null) {
			reset();
			return;
		}
		try {
			InputStream stream = new ByteArrayInputStream(taggedText
					.getBytes("UTF8")); //$NON-NLS-1$
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
			DocumentBuilder parser = documentBuilderFactory
					.newDocumentBuilder();
			parser.setErrorHandler(new ParseErrorHandler());
			InputSource source = new InputSource(is);
			Document doc = parser.parse(source);
			processDocument(doc, expandURLs);
		} catch (ParserConfigurationException e) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, e, " " + e.getMessage()); //$NON-NLS-1$
		} catch (SAXException e) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, e, " " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	}

	private void processDocument(Document doc, boolean expandURLs) {
		Node root = doc.getDocumentElement();
		NodeList children = root.getChildNodes();
		processSubnodes(paragraphs, children, expandURLs);
	}

	private void processSubnodes(Vector plist, NodeList children, boolean expandURLs) {
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				// Make an implicit paragraph
				String text = getSingleNodeText(child);
				if (text != null && !isIgnorableWhiteSpace(text, true)) {
					Paragraph p = new Paragraph(true);
					p.parseRegularText(text, expandURLs, true,
							getHyperlinkSettings(), null);
					plist.add(p);
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				String tag = child.getNodeName().toLowerCase();
				if (tag.equals("p")) { //$NON-NLS-1$
					Paragraph p = processParagraph(child, expandURLs);
					if (p != null)
						plist.add(p);
				} else if (tag.equals("li")) { //$NON-NLS-1$
					Paragraph p = processListItem(child, expandURLs);
					if (p != null)
						plist.add(p);
				}
			}
		}
	}

	private Paragraph processParagraph(Node paragraph, boolean expandURLs) {
		NodeList children = paragraph.getChildNodes();
		NamedNodeMap atts = paragraph.getAttributes();
		Node addSpaceAtt = atts.getNamedItem("addVerticalSpace"); //$NON-NLS-1$
		boolean addSpace = true;

		if (addSpaceAtt == null)
			addSpaceAtt = atts.getNamedItem("vspace"); //$NON-NLS-1$

		if (addSpaceAtt != null) {
			String value = addSpaceAtt.getNodeValue();
			addSpace = value.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		Paragraph p = new Paragraph(addSpace);

		processSegments(p, children, expandURLs);
		return p;
	}

	private Paragraph processListItem(Node listItem, boolean expandURLs) {
		NodeList children = listItem.getChildNodes();
		NamedNodeMap atts = listItem.getAttributes();
		Node addSpaceAtt = atts.getNamedItem("addVerticalSpace");//$NON-NLS-1$
		Node styleAtt = atts.getNamedItem("style");//$NON-NLS-1$
		Node valueAtt = atts.getNamedItem("value");//$NON-NLS-1$
		Node indentAtt = atts.getNamedItem("indent");//$NON-NLS-1$
		Node bindentAtt = atts.getNamedItem("bindent");//$NON-NLS-1$
		int style = BulletParagraph.CIRCLE;
		int indent = -1;
		int bindent = -1;
		String text = null;
		boolean addSpace = true;

		if (addSpaceAtt != null) {
			String value = addSpaceAtt.getNodeValue();
			addSpace = value.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		if (styleAtt != null) {
			String value = styleAtt.getNodeValue();
			if (value.equalsIgnoreCase("text")) { //$NON-NLS-1$
				style = BulletParagraph.TEXT;
			} else if (value.equalsIgnoreCase("image")) { //$NON-NLS-1$
				style = BulletParagraph.IMAGE;
			} else if (value.equalsIgnoreCase("bullet")) { //$NON-NLS-1$
				style = BulletParagraph.CIRCLE;
			}
		}
		if (valueAtt != null) {
			text = valueAtt.getNodeValue();
			if (style == BulletParagraph.IMAGE)
				text = "i." + text; //$NON-NLS-1$
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

	private void processSegments(Paragraph p, NodeList children,
			boolean expandURLs) {
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			ParagraphSegment segment = null;

			if (child.getNodeType() == Node.TEXT_NODE) {
				String value = getSingleNodeText(child);

				if (value != null && !isIgnorableWhiteSpace(value, false)) {
					p.parseRegularText(value, expandURLs, true,
							getHyperlinkSettings(), null);
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				String name = child.getNodeName();
				if (name.equalsIgnoreCase("img")) { //$NON-NLS-1$
					segment = processImageSegment(child);
				} else if (name.equalsIgnoreCase("a")) { //$NON-NLS-1$
					segment = processHyperlinkSegment(child,
							getHyperlinkSettings());
				} else if (name.equalsIgnoreCase("span")) { //$NON-NLS-1$
					processTextSegment(p, expandURLs, child);
				} else if (name.equalsIgnoreCase("b")) { //$NON-NLS-1$
					String text = getNodeText(child);
					String fontId = BOLD_FONT_ID;
					p.parseRegularText(text, expandURLs, true,
							getHyperlinkSettings(), fontId);
				} else if (name.equalsIgnoreCase("br")) { //$NON-NLS-1$
					segment = new BreakSegment();
				} else if (name.equalsIgnoreCase("control")) { //$NON-NLS-1$
					segment = processControlSegment(child);
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

	private ImageSegment processImageSegment(Node image) {
		ImageSegment segment = new ImageSegment();
		processObjectSegment(segment, image, "i."); //$NON-NLS-1$
		return segment;
	}

	private ControlSegment processControlSegment(Node control) {
		ControlSegment segment = new ControlSegment();
		processObjectSegment(segment, control, "o."); //$NON-NLS-1$
		Node fill = control.getAttributes().getNamedItem("fill"); //$NON-NLS-1$
		if (fill!=null) {
			String value = fill.getNodeValue();
			boolean doFill = value.equalsIgnoreCase("true"); //$NON-NLS-1$
			segment.setFill(doFill);
		}
		try {
			Node width = control.getAttributes().getNamedItem("width"); //$NON-NLS-1$
			if (width!=null) {
				String value = width.getNodeValue();
				int doWidth = Integer.parseInt(value);
				segment.setWidth(doWidth);
			}
			Node height = control.getAttributes().getNamedItem("height"); //$NON-NLS-1$
			if (height!=null) {
				String value = height.getNodeValue();
				int doHeight = Integer.parseInt(value);
				segment.setHeight(doHeight);
			}
		}
		catch (NumberFormatException e) {
			// ignore invalid width or height
		}
		return segment;
	}

	private void processObjectSegment(ObjectSegment segment, Node object, String prefix) {
		NamedNodeMap atts = object.getAttributes();
		Node id = atts.getNamedItem("href"); //$NON-NLS-1$
		Node align = atts.getNamedItem("align"); //$NON-NLS-1$
		if (id != null) {
			String value = id.getNodeValue();
			segment.setObjectId(prefix + value);
		}
		if (align != null) {
			String value = align.getNodeValue().toLowerCase();
			if (value.equals("top")) //$NON-NLS-1$
				segment.setVerticalAlignment(ImageSegment.TOP);
			else if (value.equals("middle")) //$NON-NLS-1$
				segment.setVerticalAlignment(ImageSegment.MIDDLE);
			else if (value.equals("bottom")) //$NON-NLS-1$
				segment.setVerticalAlignment(ImageSegment.BOTTOM);
		}
	}

	private void appendText(String value, StringBuffer buf, int[] spaceCounter) {
		if (!whitespaceNormalized)
			buf.append(value);
		else {
			for (int j = 0; j < value.length(); j++) {
				char c = value.charAt(j);
				if (c == ' ' || c == '\t') {
					// space
					if (++spaceCounter[0] == 1) {
						buf.append(c);
					}
				} else if (c == '\n' || c == '\r' || c == '\f') {
					// new line
					if (++spaceCounter[0] == 1) {
						buf.append(' ');
					}
				} else {
					// other characters
					spaceCounter[0] = 0;
					buf.append(c);
				}
			}
		}
	}

	private String getNormalizedText(String text) {
		int[] spaceCounter = new int[1];
		StringBuffer buf = new StringBuffer();

		if (text == null)
			return null;
		appendText(text, buf, spaceCounter);
		return buf.toString();
	}

	private String getSingleNodeText(Node node) {
		return getNormalizedText(node.getNodeValue());
	}

	private String getNodeText(Node node) {
		NodeList children = node.getChildNodes();
		StringBuffer buf = new StringBuffer();
		int[] spaceCounter = new int[1];

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				String value = child.getNodeValue();
				appendText(value, buf, spaceCounter);
			}
		}
		return buf.toString().trim();
	}

	private ParagraphSegment processHyperlinkSegment(Node link,
			HyperlinkSettings settings) {
		NamedNodeMap atts = link.getAttributes();
		String href = null;
		boolean wrapAllowed = true;
		String boldFontId = null;

		Node hrefAtt = atts.getNamedItem("href"); //$NON-NLS-1$
		if (hrefAtt != null) {
			href = hrefAtt.getNodeValue();
		}
		Node boldAtt = atts.getNamedItem("bold"); //$NON-NLS-1$
		if (boldAtt != null) {
			boldFontId = BOLD_FONT_ID;
		}
		Node nowrap = atts.getNamedItem("nowrap"); //$NON-NLS-1$
		if (nowrap != null) {
			String value = nowrap.getNodeValue();
			if (value != null && value.equalsIgnoreCase("true")) //$NON-NLS-1$
				wrapAllowed = false;
		}
		Object status = checkChildren(link);
		if (status instanceof Node) {
			Node child = (Node)status;
			ImageHyperlinkSegment segment = new ImageHyperlinkSegment();
			segment.setHref(href);
			segment.setWordWrapAllowed(wrapAllowed);
			Node alt = child.getAttributes().getNamedItem("alt"); //$NON-NLS-1$
			if (alt!=null)
				segment.setTooltipText(alt.getNodeValue());
			Node text = child.getAttributes().getNamedItem("text"); //$NON-NLS-1$
			if (text!=null)
				segment.setText(text.getNodeValue());
			processObjectSegment(segment, child, "i."); //$NON-NLS-1$
			return segment;
		}  else if (status instanceof String) {
			String text = (String) status;
			TextHyperlinkSegment segment = new TextHyperlinkSegment(text,
					settings, null);
			segment.setHref(href);
			segment.setFontId(boldFontId);
			Node alt = atts.getNamedItem("alt"); //$NON-NLS-1$
			if (alt!=null)
				segment.setTooltipText(alt.getNodeValue());			
			segment.setWordWrapAllowed(wrapAllowed);
			return segment;
		} else {
			AggregateHyperlinkSegment parent = new AggregateHyperlinkSegment();
			parent.setHref(href);
			NodeList children = link.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.TEXT_NODE) {
					String value = child.getNodeValue();
					TextHyperlinkSegment ts = new TextHyperlinkSegment(
							getNormalizedText(value), settings, null);
					Node alt = atts.getNamedItem("alt"); //$NON-NLS-1$
					if (alt!=null)
						ts.setTooltipText(alt.getNodeValue());
					ts.setWordWrapAllowed(wrapAllowed);
					parent.add(ts);
				} else if (child.getNodeType() == Node.ELEMENT_NODE) {
					String name = child.getNodeName();
					if (name.equalsIgnoreCase("img")) { //$NON-NLS-1$
						ImageHyperlinkSegment is = new ImageHyperlinkSegment();
						processObjectSegment(is, child, "i."); //$NON-NLS-1$
						Node alt = child.getAttributes().getNamedItem("alt"); //$NON-NLS-1$
						if (alt!=null)
							is.setTooltipText(alt.getNodeValue());
						parent.add(is);
						is.setWordWrapAllowed(wrapAllowed);
					}
				}
			}
			return parent;
		}
	}

	private Object checkChildren(Node node) {
		boolean text = false;
		Node imgNode = null;
		//int status = 0;

		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE)
				text = true;
			else if (child.getNodeType() == Node.ELEMENT_NODE
					&& child.getNodeName().equalsIgnoreCase("img")) { //$NON-NLS-1$
				imgNode = child;
			}
		}
		if (text && imgNode == null)
			return getNodeText(node);
		else if (!text && imgNode != null)
			return imgNode;
		else return null;
	}

	private void processTextSegment(Paragraph p, boolean expandURLs,
			Node textNode) {
		String text = getNodeText(textNode);

		NamedNodeMap atts = textNode.getAttributes();
		Node font = atts.getNamedItem("font"); //$NON-NLS-1$
		Node color = atts.getNamedItem("color"); //$NON-NLS-1$
		boolean wrapAllowed=true;
		Node nowrap = atts.getNamedItem("nowrap"); //$NON-NLS-1$
		if (nowrap != null) {
			String value = nowrap.getNodeValue();
			if (value != null && value.equalsIgnoreCase("true")) //$NON-NLS-1$
				wrapAllowed = false;
		}
		String fontId = null;
		String colorId = null;
		if (font != null) {
			fontId = "f." + font.getNodeValue(); //$NON-NLS-1$
		}
		if (color != null) {
			colorId = "c." + color.getNodeValue(); //$NON-NLS-1$
		}
		p.parseRegularText(text, expandURLs, wrapAllowed, getHyperlinkSettings(), fontId,
				colorId);
	}

	public void parseRegularText(String regularText, boolean convertURLs) {
		reset();

		if (regularText == null)
			return;

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
				p.parseRegularText(text, convertURLs, true, getHyperlinkSettings(),
						null);
				p = null;
			}
		}
		if (p != null) {
			// no new line
			String text = regularText.substring(pstart);
			p.parseRegularText(text, convertURLs, true, getHyperlinkSettings(), null);
		}
	}

	public HyperlinkSettings getHyperlinkSettings() {
		// #132723 cannot have null settings
		if (hyperlinkSettings==null)
			hyperlinkSettings = new HyperlinkSettings(SWTUtil.getStandardDisplay());
		return hyperlinkSettings;
	}

	public void setHyperlinkSettings(HyperlinkSettings settings) {
		this.hyperlinkSettings = settings;
	}

	private void reset() {
		if (paragraphs == null)
			paragraphs = new Vector();
		paragraphs.clear();
		selectedSegmentIndex = -1;
		savedSelectedLinkIndex = -1;
		selectableSegments = null;
	}

	IFocusSelectable[] getFocusSelectableSegments() {
		if (selectableSegments != null || paragraphs == null)
			return selectableSegments;
		Vector result = new Vector();
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph p = (Paragraph) paragraphs.get(i);
			ParagraphSegment[] segments = p.getSegments();
			for (int j = 0; j < segments.length; j++) {
				if (segments[j] instanceof IFocusSelectable)
					result.add(segments[j]);
			}
		}
		selectableSegments = (IFocusSelectable[]) result
				.toArray(new IFocusSelectable[result.size()]);
		return selectableSegments;
	}
	
	public IHyperlinkSegment getHyperlink(int index) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		if (selectables.length>index) {
			IFocusSelectable link = selectables[index];
			if (link instanceof IHyperlinkSegment)
				return (IHyperlinkSegment)link;
		}
		return null;
	}
	
	public IHyperlinkSegment findHyperlinkAt(int x, int y) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		for (int i = 0; i < selectables.length; i++) {
			IFocusSelectable segment = selectables[i];
			if (segment instanceof IHyperlinkSegment) {
				IHyperlinkSegment link = (IHyperlinkSegment)segment;
				if (link.contains(x, y))
					return link;
			}
		}
		return null;
	}
	
	public int getHyperlinkCount() {
		return getFocusSelectableSegments().length;
	}
	
	public int indexOf(IHyperlinkSegment link) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		for (int i = 0; i < selectables.length; i++) {
			IFocusSelectable segment = selectables[i];
			if (segment instanceof IHyperlinkSegment) {
				IHyperlinkSegment l = (IHyperlinkSegment)segment;
				if (link==l)
					return i;
			}
		}
		return -1;
	}

	public ParagraphSegment findSegmentAt(int x, int y) {
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph p = (Paragraph) paragraphs.get(i);
			ParagraphSegment segment = p.findSegmentAt(x, y);
			if (segment != null)
				return segment;
		}
		return null;
	}
	
	public void clearCache(String fontId) {
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph p = (Paragraph) paragraphs.get(i);
			p.clearCache(fontId);
		}
	}

	public IFocusSelectable getSelectedSegment() {
		if (selectableSegments==null || selectedSegmentIndex == -1)
			return null;
		return selectableSegments[selectedSegmentIndex];
	}
	
	public int getSelectedSegmentIndex() {
		return selectedSegmentIndex;
	}
	
	public boolean linkExists(IHyperlinkSegment link) {
		if (selectableSegments==null)
			return false;
		for (int i=0; i<selectableSegments.length; i++) {
			if (selectableSegments[i]==link)
				return true;
		}
		return false;
	}

	public boolean traverseFocusSelectableObjects(boolean next) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		if (selectables == null)
			return false;
		int size = selectables.length;
		if (next) {
			selectedSegmentIndex++;
		} else
			selectedSegmentIndex--;

		if (selectedSegmentIndex < 0 || selectedSegmentIndex > size - 1) {
			selectedSegmentIndex = -1;
		}
		return selectedSegmentIndex != -1;
	}
	
	public IFocusSelectable getNextFocusSegment(boolean next) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		if (selectables == null)
			return null;
		int nextIndex = next?selectedSegmentIndex+1:selectedSegmentIndex-1;

		if (nextIndex < 0 || nextIndex > selectables.length - 1) {
			return null;
		}
		return selectables[nextIndex];
	}
	
	public boolean restoreSavedLink() {
		if (savedSelectedLinkIndex!= -1) {
			selectedSegmentIndex = savedSelectedLinkIndex;
			return true;
		}
		return false;
	}

	public void selectLink(IHyperlinkSegment link) {
		if (link == null) {
			savedSelectedLinkIndex = selectedSegmentIndex;	
			selectedSegmentIndex = -1;
		}
		else {
			select(link);

		}
	}
	
	public void select(IFocusSelectable selectable) {
		IFocusSelectable[] selectables = getFocusSelectableSegments();
		selectedSegmentIndex = -1;
		if (selectables == null)
			return;
		for (int i = 0; i < selectables.length; i++) {
			if (selectables[i].equals(selectable)) {
				selectedSegmentIndex = i;
				break;
			}
		}
	}

	public boolean hasFocusSegments() {
		IFocusSelectable[] segments = getFocusSelectableSegments();
		if (segments.length > 0)
			return true;
		return false;
	}

	public void dispose() {
		paragraphs = null;
		selectedSegmentIndex = -1;
		savedSelectedLinkIndex = -1;
		selectableSegments = null;
	}

	/**
	 * @return Returns the whitespaceNormalized.
	 */
	public boolean isWhitespaceNormalized() {
		return whitespaceNormalized;
	}

	/**
	 * @param whitespaceNormalized
	 *            The whitespaceNormalized to set.
	 */
	public void setWhitespaceNormalized(boolean whitespaceNormalized) {
		this.whitespaceNormalized = whitespaceNormalized;
	}
}
