/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v0.5 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.Vector;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

class CommentHistoryContentHandler extends DefaultHandler {

	private boolean inCommentElement;
	private Vector comments;
	public CommentHistoryContentHandler() {
		inCommentElement = false;
	}

	/**
	 * @see ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] chars, int startIndex, int length) throws SAXException {
		if (!inCommentElement) return;
		StringBuffer buffer = new StringBuffer();
		buffer.append(chars, startIndex, length);
		comments.add(buffer.toString());
	}

	/**
	 * @see ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			Attributes atts)
			throws SAXException {
		if (localName.equals(RepositoryManager.ELEMENT_COMMIT_COMMENT)) {
			inCommentElement=true;
			return;
		} 
		if (localName.equals(RepositoryManager.ELEMENT_COMMIT_HISTORY)) {
			comments = new Vector(RepositoryManager.COMMIT_HISTORY_MAX);
			return;
		}
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName) {
		if (localName.equals(RepositoryManager.ELEMENT_COMMIT_COMMENT)) {
			inCommentElement=false;
			return;
		} 
		if (localName.equals(RepositoryManager.ELEMENT_COMMIT_HISTORY)) {
			RepositoryManager.previousComments = new String[comments.size()];
			comments.copyInto(RepositoryManager.previousComments);
			return;
		} 
	}
}
