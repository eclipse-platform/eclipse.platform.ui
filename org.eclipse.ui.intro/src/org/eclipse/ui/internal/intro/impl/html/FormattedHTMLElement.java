/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.html;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
/**
 * A FormattedHTMLElement is an HTMLElement that is indented and can have its
 * content on a separate line from its start and end tags
 */
public class FormattedHTMLElement extends HTMLElement {
	// an integer representing how many tabs to insert when printing out this
	// element and its content
	private int indentLevel;
	// indicates whether to print this element on a single line or on multiple
	// lines
	private boolean spanMultipleLines;
	// indicates whether an end tag is required for this element. default is
	// true, since most elements require an end tag
	private boolean endTagRequired = true;
	public FormattedHTMLElement(String name, int indentLevel,
			boolean spanMultipleLines) {
		super(name);
		this.indentLevel = indentLevel;
		this.spanMultipleLines = spanMultipleLines;
		// default
		endTagRequired = true;
	}
	public FormattedHTMLElement(String name, int indentLevel,
			boolean spanMultipleLines, boolean endTagRequired) {
		super(name);
		this.indentLevel = indentLevel;
		this.spanMultipleLines = spanMultipleLines;
		this.endTagRequired = endTagRequired;
	}
	public FormattedHTMLElement(String name, Map attributes, Vector content,
			int indentLevel, boolean spanMultipleLines) {
		super(name, attributes, content);
		this.indentLevel = indentLevel;
		this.spanMultipleLines = spanMultipleLines;
		endTagRequired = true;
	}
	/**
	 * Set whether the end tag is required for this element
	 * 
	 * @param required
	 *            true if end tag required, false otherwise
	 */
	public void setEndTagRequired(boolean required) {
		this.endTagRequired = required;
	}
	/**
	 * Set the indent level that should be applied to this element when printed
	 * 
	 * @param indentLevel
	 *            The indentLevel to set.
	 */
	public void setIndentLevel(int indentLevel) {
		this.indentLevel = indentLevel;
	}
	/**
	 * Set whether or not this element should be printed over multiple lines,
	 * or on a single line
	 * 
	 * @param spanMultipleLines
	 *            true if the element should be printed over multiple lines,
	 *            false if it should be printed on a single line
	 */
	public void setSpanMultipleLines(boolean spanMultipleLines) {
		this.spanMultipleLines = spanMultipleLines;
	}
	/**
	 * Create a string of tabs to insert before the element is printed
	 * 
	 * @param indentLevel
	 *            the number of tabs to insert
	 * @return
	 */
	private StringBuffer getIndent(int indentLevel) {
		// figure out the tab width
		StringBuffer indent = new StringBuffer();
		for (int i = 0; i < indentLevel; i++) {
			indent.append(IIntroHTMLConstants.SMALL_TAB);
		}
		return indent;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer element = new StringBuffer();
		// insert the indent
		element.append(getIndent(indentLevel));
		// add the start tag and attributes
		element.append(HTMLUtil.createHTMLStartTag(getElementName(),
				getElementAttributes(), spanMultipleLines));
		// if there is no content and an end tag is not required just
		// return the element as is
		if (getElementContent().isEmpty() && !endTagRequired) {
			return element.toString();
		}
		// include the element's content, if there is any
		for (Iterator it = getElementContent().iterator(); it.hasNext();) {
			Object content = it.next();
			element.append(content);
		}
		// indent the end tag if we're on a new line
		if (indentLevel > 0 && spanMultipleLines)
			element.append(getIndent(indentLevel));
		// include an end tag
		element.append(HTMLUtil.createHTMLEndTag(getElementName(), true));
		return element.toString();
	}
}
