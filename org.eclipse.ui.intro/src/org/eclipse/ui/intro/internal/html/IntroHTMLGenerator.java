/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro.internal.html;
import java.io.*;
import java.net.*;

import org.eclipse.ui.intro.internal.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;
public class IntroHTMLGenerator {
	private AbstractIntroPage introPage;
	private String introTitle; 
	/**
	 * Generates the HTML code that will be presented in the browser widget for
	 * the provided intro page.
	 * 
	 * @param page
	 *            the page to generate HTML for
	 * @param title
	 *            the title of the intro presentation, or null
	 */
	public HTMLElement generateHTMLforPage(AbstractIntroPage page, String title) {
		if (page == null)
			return null;
		this.introPage = page;
		this.introTitle = title;
		// generate and add the appropriate encoding to the top of the document
		// generateEncoding();
		// create the main HTML element, and all of its contents
		return generateHTMLElement();
	}
	private HTMLElement generateEncoding() {
		HTMLElement encoding = new HTMLElement("");
		// TODO: figure out how to handle locale based encoding
		// As far as the HTML generator is concerned, this is probably as
		// simple as asking the model for the information
		return encoding;
	}
	/**
	 * Generates the HTML element and its content:
	 * 
	 * <pre>
	 *  &lt;HTML&gt;
	 *  &lt;HEAD&gt;
	 *  head content
	 *  &lt;/HEAD&gt;
	 *  &lt;BODY&gt;
	 *  body content
	 *  &lt;/BODY&gt;
	 *  &lt;/HTML&gt;
	 * </pre>
	 * 
	 * @return the html HTMLElement
	 */
	private HTMLElement generateHTMLElement() {
		// this is the outermost element, so it has no indent
		int indentLevel = 0;
		HTMLElement html = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_HTML, indentLevel, true);
		HTMLElement head = generateHeadElement(indentLevel + 1);
		HTMLElement body = generateBodyElement(indentLevel + 1);
		html.addContent(head);
		html.addContent(body);
		return html;
	}
	/**
	 * Generates the HEAD element and its content:
	 * 
	 * <pre>
	 *  &lt;HEAD&gt;
	 *  &lt;style type=&quot;text/css&quot;&gt;HTML, IMG { border: 0px; } &lt;/style&gt;
	 *  &lt;TITLE&gt;page title &lt;/TITLE&gt;
	 *  &lt;LINK href=&quot;style sheet&quot;&gt;
	 *  additional head content, if specified
	 *  &lt;/HEAD&gt;
	 * </pre>
	 * 
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return the head HTMLElement
	 */
	private HTMLElement generateHeadElement(int indentLevel) {
		HTMLElement head = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_HEAD, indentLevel, true);
		// create the HTML style block
		head.addContent(generateStyleElement(indentLevel + 1));
		// add the title
		if (introPage.getTitle() != null)
			head.addContent(generateTitleElement(introPage.getTitle(),
					indentLevel + 1));
		// add the presentation style
		String style = IntroPlugin.getDefault().getIntroModelRoot()
				.getPresentation().getStyle();
		if (style != null)
			head.addContent(generateLinkElement(style, indentLevel + 1));
		//TODO: Should introPage.getStyles() return the main page style as
		// well?
		style = introPage.getStyle();
		if (style != null)
			head.addContent(generateLinkElement(style, indentLevel + 1));
		// add the page's inherited style(s)
		String[] pageStyles = introPage.getStyles();
		for (int i = 0; i < pageStyles.length; i++) {
			style = pageStyles[i];
			if (style != null)
				head.addContent(generateLinkElement(style, indentLevel + 1));
		}
		// if there is additional head conent specified in an external file,
		// include it. Additional head content can be specified at the
		// implementation level (which would apply to ALL pages) and at the
		// page level (which would apply only to that particular page).
		// For the implementation's head contribution:
		StringBuffer content = null;
		IntroHead introHead = IntroPlugin.getDefault().getIntroModelRoot()
				.getPresentation().getHead();
		if (introHead != null) {
			content = readFromFile(introHead.getSrc());
			if (content != null)
				head.addContent(content);
		}
		// For the page's head contribution:
		// TODO: there should only be one of these at the page level, not a
		// collection..
		IntroHead[] htmlHeads = introPage.getHTMLHeads();
		for (int i = 0; i < htmlHeads.length; i++) {
			introHead = htmlHeads[i];
			if (introHead != null) {
				content = readFromFile(introHead.getSrc());
				if (content != null)
					head.addContent(content);
			}
		}
		return head;
	}
	/**
	 * Generates the BODY element and its content:
	 * 
	 * <pre>
	 *  &lt;BODY&gt;
	 *  &lt;DIV id=&quot;presentation-header&quot;&gt;
	 *  presentation title
	 *  &lt;/DIV&gt;
	 *  &lt;DIV id=&quot;pageId&quot; class=&quot;page-content&quot;&gt;
	 *  &lt;DIV id=&quot;page-header&quot;&gt;
	 *  page title
	 *  &lt;/DIV&gt;
	 *  page content
	 *  &lt;/DIV&gt;
	 *  &lt;/BODY&gt;
	 * </pre>
	 * 
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return the body HTMLElement
	 */
	private HTMLElement generateBodyElement(int indentLevel) {
		HTMLElement body = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_BODY, indentLevel, true);
		// Create the div that contains the intro presentation title
		if (introTitle != null) {
			HTMLElement presentationHeader = generateHeaderDiv(
					IIntroHTMLConstants.DIV_ID_PRESENTATION_HEADER,
					IIntroHTMLConstants.ELEMENT_H1,
					IIntroHTMLConstants.SPAN_ID_PRESENTATION_TITLE, null,
					introTitle, indentLevel + 1);
			body.addContent(presentationHeader);
		}
		// Create the div that contains the page content
		HTMLElement pageContentDiv = generateDivElement(introPage.getId(),
				IIntroHTMLConstants.DIV_CLASS_PAGE_CONTENT, indentLevel + 1);
		// Create the div that contains the page title, if necessary
		if (introPage.getTitle() != null) {
			// use indentLevel + 2 here, since this div is contained within the
			// pageContentDiv
			HTMLElement pageHeader = generateHeaderDiv(
					IIntroHTMLConstants.DIV_ID_PAGE_HEADER,
					IIntroHTMLConstants.ELEMENT_H2,
					IIntroHTMLConstants.SPAN_ID_PAGE_TITLE, null, introPage
							.getTitle(), indentLevel + 2);
			pageContentDiv.addContent(pageHeader);
		}
		// Add any children of the page, in the order they are defined
		AbstractIntroElement[] children = introPage.getChildren();
		for (int i = 0; i < children.length; i++) {
		    AbstractIntroElement child = children[i];
			// use indentLevel + 2 here, since this element is contained within
			// the pageContentDiv
			HTMLElement childElement = generateIntroElement(child,
					indentLevel + 2);
			if (childElement != null)
				pageContentDiv.addContent(childElement);
		}
		body.addContent(pageContentDiv);
		return body;
	}
	/**
	 * Given an IntroElement, generate the appropriate HTMLElement
	 * 
	 * @param element
	 *            the IntroElement
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return an HTMLElement
	 */
	private HTMLElement generateIntroElement(AbstractIntroElement element,
			int indentLevel) {
		if (element == null)
			return null;
		switch (element.getType()) {
			case AbstractIntroElement.DIV :
				return generateIntroDiv((IntroDiv) element, indentLevel);
			case AbstractIntroElement.LINK :
				return generateIntroLink((IntroLink) element, indentLevel);
			case AbstractIntroElement.HTML :
				return generateIntroHTML((IntroHTML) element, indentLevel);
			case AbstractIntroElement.IMAGE :
				return generateIntroImage((IntroImage) element, indentLevel);
			case AbstractIntroElement.TEXT :
				return generateIntroText((IntroText) element, indentLevel);
			default :
				return null;
		}
	}
	/**
	 * Create a div element and its content from an IntroDiv:
	 * 
	 * <pre>
	 *  &lt;div id=&quot;attrvalue&quot;&gt;
	 *  &lt;h4&gt;&lt;span class=&quot;div-label&quot;&gt;attrvalue&lt;/span&gt;&lt;h4&gt;
	 *  any defined divs, links, html, images, text, includes
	 *  &lt;/div&gt;
	 * </pre>
	 * 
	 * @param element
	 *            the IntroDiv
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a div HTMLElement
	 */
	private HTMLElement generateIntroDiv(IntroDiv element, int indentLevel) {
		// Create the outer div element
		HTMLElement divElement = generateDivElement(element.getId(),
				indentLevel);
		// Create the div label, if specified
		if (element.getLabel() != null) {
			HTMLElement divLabel = generateTextElement(
					IIntroHTMLConstants.ELEMENT_H4, null,
					IIntroHTMLConstants.SPAN_CLASS_DIV_LABEL, element
							.getLabel(), indentLevel + 1);
			divElement.addContent(divLabel);
		}
		// Add any children of the div, in the order they are defined
		AbstractIntroElement[] children = element.getChildren();
		for (int i = 0; i < children.length; i++) {
		    AbstractIntroElement child = children[i];
			HTMLElement childElement = generateIntroElement(child,
					indentLevel + 1);
			if (childElement != null)
				divElement.addContent(childElement);
		}
		return divElement;
	}
	/**
	 * Generates an anchor (link) element and its content from an IntroLink:
	 * 
	 * <pre>
	 *  &lt;A id=linkId class=&quot;link&quot; href=linkHref&gt;
	 *  &lt;IMG src=&quot;blank.gif&quot;&gt;
	 *  &lt;SPAN class=&quot;link-label&quot;&gt;linkLabel &lt;/SPAN&gt;
	 *  &lt;P&gt;&lt;SPAN&gt;text&lt;/SPAN&gt;&lt;/P&gt;
	 *  &lt;/A&gt;
	 * </pre>
	 * 
	 * @param element
	 *            the IntroLink
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return an anchor (&lt;A&gt;) HTMLElement
	 */
	private HTMLElement generateIntroLink(IntroLink element, int indentLevel) {
		HTMLElement anchor = generateAnchorElement(element, indentLevel);
		// add <IMG src="blank.gif">
		String blankImageURL = IntroModelRoot.getPluginLocation(
				IIntroHTMLConstants.IMAGE_SRC_BLANK, IIntroConstants.PLUGIN_ID);
		if (blankImageURL != null) {
			anchor.addContent(generateImageElement(blankImageURL,
					indentLevel + 1));
		}
		// add <SPAN class="link-label">linkLabel</SPAN>
		if (element.getLabel() != null) {
			HTMLElement label = generateSpanElement(
					IIntroHTMLConstants.SPAN_CLASS_LINK_LABEL, indentLevel + 1);
			label.addContent(element.getLabel());
			anchor.addContent(label);
		}
		if (element.getText() != null) {
			HTMLElement text = generateTextElement(
					IIntroHTMLConstants.ELEMENT_PARAGRAPH, null,
					IIntroHTMLConstants.SPAN_CLASS_TEXT, element.getText(),
					indentLevel + 1);
			anchor.addContent(text);
		}
		return anchor;
	}
	/**
	 * Generate the appropriate HTML from an IntroHTML. If the IntroHTML type
	 * is "inline", then the content from the referenced file is emitted as-is
	 * into a div element. If the type is "embed", an OBJECT html element is
	 * created whose <code>data</code> attribute is equal to the IntroHTML's
	 * <code>src</code> value
	 * 
	 * @param element
	 *            the IntroHTML
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return an HTMLElement
	 */
	private HTMLElement generateIntroHTML(IntroHTML element, int indentLevel) {
		if (element.isInlined())
			return generateInlineIntroHTML(element, indentLevel);
		else
			return generateEmbeddedIntroHTML(element, indentLevel);
	}
	/**
	 * Generate an image element from an IntroImage:
	 * 
	 * <pre>
	 *  &lt;IMG src=imageSrc id=imageId&gt;
	 * </pre>
	 * 
	 * @param element
	 *            the IntroImage
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return an img HTMLElement
	 */
	private HTMLElement generateIntroImage(IntroImage element, int indentLevel) {
		HTMLElement imageElement = generateImageElement(element.getSrc(),
				indentLevel);
		if (element.getId() != null)
			imageElement.addAttribute(IIntroHTMLConstants.ATTRIBUTE_ID, element
					.getId());
		return imageElement;
	}
	/**
	 * Generate a paragraph (&lt;P&gt;) element from an IntroText. The
	 * paragraph element will contain a span element that will contain the
	 * actual text. Providing the span element provides additional flexibility
	 * for CSS designers.
	 * 
	 * <pre>
	 *  &lt;P&gt;&lt;SPAN&gt;spanContent&lt;/SPAN&gt;&lt;/P&gt;
	 * </pre>
	 * 
	 * @param element
	 *            the IntroText
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a paragraph HTMLElement
	 */
	private HTMLElement generateIntroText(IntroText element, int indentLevel) {
		HTMLElement textElement = generateTextElement(
				IIntroHTMLConstants.ELEMENT_PARAGRAPH, element.getId(),
				IIntroHTMLConstants.SPAN_CLASS_TEXT, element.getText(),
				indentLevel);
		return textElement;
	}
	/**
	 * Generate "inline" content from an IntroHTML. The content from the file
	 * referenced by the IntroHTML's <code>src</code> attribute is emitted
	 * as-is into a div element:
	 * 
	 * <pre>
	 *  &lt;div id=&quot;attrvalue&quot; class=&quot;inline-html&quot;&gt;
	 *  content from file specified in src attribute
	 *  &lt;/div&gt;
	 * </pre>
	 * 
	 * @param element
	 *            the IntroHTML
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a div HTMLElement, or null if there was a problem reading from
	 *         the file
	 */
	private HTMLElement generateInlineIntroHTML(IntroHTML element,
			int indentLevel) {
		StringBuffer content = readFromFile(element.getSrc());
		if (content != null && content.length() > 0) {
			// Create the outer div element
			HTMLElement divElement = generateDivElement(element.getId(),
					IIntroHTMLConstants.DIV_CLASS_INLINE_HTML, indentLevel);
			// add the content of the specified file into the div element
			divElement.addContent(content);
			return divElement;
		}
		return null;
	}
	/**
	 * Reads the content of the file referred to by the <code>src</code>
	 * parameter and returns the content in the form of a StringBuffer
	 * 
	 * @param src -
	 *            the file that contains the target conent
	 * @return a StringBuffer containing the content in the file, or null
	 */
	private StringBuffer readFromFile(String src) {
		if (src == null)
			return null;
		InputStream stream = null;
		StringBuffer content = new StringBuffer();
		BufferedReader reader = null;
		try {
			URL url = new URL(src);
			stream = url.openStream();
			//TODO: Do we need to worry about the encoding here? e.g.:
			//reader = new BufferedReader(new InputStreamReader(stream,
			// ResourcesPlugin.getEncoding()));
			reader = new BufferedReader(new InputStreamReader(stream));
			while (true) {
				String line = reader.readLine();
				if (line == null) // EOF
					break; // done reading file
				content.append(line);
				content.append(IIntroHTMLConstants.NEW_LINE);
			}
		} catch (Exception exception) {
			Logger.logError("Error reading from file", exception); //$NON-NLS-1$
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				Logger.logError("Error closing input stream", e); //$NON-NLS-1$
				return null;
			}
		}
		return content;
	}
	/**
	 * Generate "embedded" content from an IntroHTML. An OBJECT html element is
	 * created whose <code>data</code> attribute is equal to the IntroHTML's
	 * <code>src</code> value.
	 * 
	 * <pre>
	 *  &lt;OBJECT type=&quot;text/html&quot; data=&quot;attrvalue&quot;&gt;
	 *  alternative text in case the object can not be rendered
	 *  &lt;/OBJECT&gt; 
	 * </pre>
	 * 
	 * @param element
	 *            the IntroHTML
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return an object HTMLElement
	 */
	private HTMLElement generateEmbeddedIntroHTML(IntroHTML element,
			int indentLevel) {
		HTMLElement objectElement = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_OBJECT, indentLevel, true);
		objectElement.addAttribute(IIntroHTMLConstants.ATTRIBUTE_TYPE,
				IIntroHTMLConstants.OBJECT_TYPE);
		if (element.getId() != null)
			objectElement.addAttribute(IIntroHTMLConstants.ATTRIBUTE_ID,
					element.getId());
		if (element.getSrc() != null)
			objectElement.addAttribute(IIntroHTMLConstants.ATTRIBUTE_DATA,
					element.getSrc());
		// The alternative content is added in case the browser can not render
		// the specified content
		if (element.getText() != null) {
			HTMLElement text = generateTextElement(
					IIntroHTMLConstants.ELEMENT_PARAGRAPH, null,
					IIntroHTMLConstants.SPAN_CLASS_TEXT, element.getText(),
					indentLevel);
			if (text != null)
				objectElement.addContent(text);
		}
		if (element.getIntroImage() != null) {
			HTMLElement img = generateIntroImage(element.getIntroImage(),
					indentLevel);
			if (img != null)
				objectElement.addContent(img);
		}
		return objectElement;
	}
	/**
	 * Generates the style element that goes into HEAD:
	 * 
	 * <pre>
	 *  &lt;style type=&quot;text/css&quot;&gt;HTML, IMG { border: 0px; } &lt;/style&gt;
	 * </pre>
	 * 
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return the style HTMLElement
	 */
	private HTMLElement generateStyleElement(int indentLevel) {
		HTMLElement style = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_STYLE, indentLevel, false);
		style.addAttribute(IIntroHTMLConstants.ATTRIBUTE_TYPE,
				IIntroHTMLConstants.LINK_STYLE);
		style.addContent(IIntroHTMLConstants.STYLE_HTML);
		return style;
	}
	/**
	 * Generates the title element and its content:
	 * 
	 * <pre>
	 *  &lt;TITLE&gt;intro title&lt;/TITLE&gt;
	 * </pre>
	 * 
	 * @param title
	 *            the title of this intro page
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return the title HTMLElement
	 */
	private HTMLElement generateTitleElement(String title, int indentLevel) {
		HTMLElement titleElement = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_TITLE, indentLevel, false);
		titleElement.addContent(title);
		return titleElement;
	}
	/**
	 * Generates a link element that refers to a cascading style sheet (CSS):
	 * 
	 * <pre>
	 *  &lt;LINK rel=&quot;stylesheet&quot; style=&quot;text/css&quot; href=&quot;style sheet&quot;&gt;
	 * </pre>
	 * 
	 * @param href
	 *            the value of the href attribute for this link element
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a link HTMLElement
	 */
	private HTMLElement generateLinkElement(String href, int indentLevel) {
		HTMLElement link = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_LINK, indentLevel, true, false);
		link.addAttribute(IIntroHTMLConstants.ATTRIBUTE_RELATIONSHIP,
				IIntroHTMLConstants.LINK_REL);
		link.addAttribute(IIntroHTMLConstants.ATTRIBUTE_STYLE,
				IIntroHTMLConstants.LINK_STYLE);
		if (href != null)
			link.addAttribute(IIntroHTMLConstants.ATTRIBUTE_HREF, href);
		return link;
	}
	/**
	 * Generate an anchor element:
	 * 
	 * <pre>
	 *  &lt;A id=linkId class=linkClass href=linkHref&gt; &lt;/A&gt;
	 * </pre>
	 * 
	 * @param link
	 *            the IntroLink element that contains the value for the id and
	 *            href attributes
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return an anchor (&lt;A&gt;) HTMLElement
	 */
	private HTMLElement generateAnchorElement(IntroLink link, int indentLevel) {
		HTMLElement anchor = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_ANCHOR, indentLevel, true);
		if (link.getId() != null)
			anchor.addAttribute(IIntroHTMLConstants.ATTRIBUTE_ID, link.getId());
		anchor.addAttribute(IIntroHTMLConstants.ATTRIBUTE_CLASS,
				IIntroHTMLConstants.ANCHOR_CLASS_LINK);
		if (link.getUrl() != null)
			anchor.addAttribute(IIntroHTMLConstants.ATTRIBUTE_HREF, link
					.getUrl());
		return anchor;
	}
	/**
	 * Generates a div block that contains a header and span element:
	 * 
	 * <pre>
	 *  &lt;DIV id=divId&gt;
	 *  &lt;H&gt;&lt;SPAN&gt;spanContent &lt;/SPAN&gt; &lt;/H&gt;
	 *  &lt;/DIV&gt;
	 * </pre>
	 * 
	 * @param divId
	 *            the id of the div to create
	 * @param headerType
	 *            what type of header to create (e.g., H1, H2, etc)
	 * @param spanID
	 *            the id of the span element, or null
	 * @param spanClass
	 *            the class of the span element, or null
	 * @param spanContent
	 *            the span content
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a div HTMLElement that contains a header
	 */
	private HTMLElement generateHeaderDiv(String divId, String headerType,
			String spanID, String spanClass, String spanContent, int indentLevel) {
		// create the text element: <P><SPAN>spanContent</SPAN></P>
		HTMLElement text = generateTextElement(headerType, spanID, spanClass,
				spanContent, indentLevel + 1);
		// create the containing div element
		HTMLElement div = generateDivElement(divId, indentLevel);
		div.addContent(text);
		return div;
	}
	/**
	 * Generates a span element inside a text element, where the text element
	 * can be a P (paragraph), or any of the H (Header) elements. Providing the
	 * span element provides additional flexibility for CSS designers.
	 * 
	 * <pre>
	 *  &lt;P&gt;&lt;SPAN&gt;spanContent&lt;/SPAN&gt;&lt;/P&gt;
	 * </pre>
	 * 
	 * @param type
	 *            the type of text element to create (e.g., P, H1, H2, etc)
	 * @param spanID
	 *            the id of the span element, or null
	 * @param spanClass
	 *            the class of the span element, or null
	 * @param spanContent
	 *            the span content
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a text HTMLElement that contains a span element
	 */
	private HTMLElement generateTextElement(String type, String spanID,
			String spanClass, String spanContent, int indentLevel) {
		// Create the span: <SPAN>spanContent</SPAN>
		HTMLElement span = new HTMLElement(IIntroHTMLConstants.ELEMENT_SPAN);
		if (spanID != null)
			span.addAttribute(IIntroHTMLConstants.ATTRIBUTE_ID, spanID);
		if (spanClass != null)
			span.addAttribute(IIntroHTMLConstants.ATTRIBUTE_CLASS, spanClass);
		if (spanContent != null)
			span.addContent(spanContent);
		// Create the enclosing text element: <P><SPAN>spanContent</SPAN></P>
		HTMLElement text = new FormattedHTMLElement(type, indentLevel, false);
		text.addContent(span);
		return text;
	}
	/**
	 * Generates a DIV element with the provided indent, id, and class.
	 * 
	 * @param divId
	 *            value for the div's id attribute
	 * @param divClass
	 *            value for the div's class attribute
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a div HTMLElement
	 */
	private HTMLElement generateDivElement(String divId, String divClass,
			int indentLevel) {
		HTMLElement div = generateDivElement(divId, indentLevel);
		div.addAttribute(IIntroHTMLConstants.ATTRIBUTE_CLASS, divClass);
		return div;
	}
	/**
	 * Generates a DIV element with the provided indent and id.
	 * 
	 * @param divId
	 *            value for the div's id attribute
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a div HTMLElement
	 */
	private HTMLElement generateDivElement(String divId, int indentLevel) {
		HTMLElement div = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_DIV, indentLevel, true);
		if (divId != null)
			div.addAttribute(IIntroHTMLConstants.ATTRIBUTE_ID, divId);
		return div;
	}
	/**
	 * Generates an IMG element:
	 * 
	 * <pre>
	 *  &lt;IMG src=imageSrc&gt;
	 * </pre>
	 * 
	 * @param imageSrc
	 *            the value to be supplied to the src attribute
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return an img HTMLElement
	 */
	private HTMLElement generateImageElement(String imageSrc, int indentLevel) {
		HTMLElement image = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_IMG, indentLevel, true, false);
		image.addAttribute(IIntroHTMLConstants.ATTRIBUTE_SRC, imageSrc);
		return image;
	}
	/**
	 * Generate a span element
	 * 
	 * <pre>
	 *  &lt;SPAN class=spanClass&gt; &lt;/SPAN&gt;
	 * </pre>
	 * 
	 * @param spanClass
	 *            the value to be supplied to the class attribute
	 * @param indentLevel
	 *            the number of indents to insert before the element when it is
	 *            printed
	 * @return a span HTMLElement
	 */
	private HTMLElement generateSpanElement(String spanClass, int indentLevel) {
		HTMLElement span = new FormattedHTMLElement(
				IIntroHTMLConstants.ELEMENT_SPAN, indentLevel, false);
		span.addAttribute(IIntroHTMLConstants.ATTRIBUTE_CLASS, spanClass);
		return span;
	}
}
