package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


import org.eclipse.swt.custom.StyledTextContent;


/**
 * Adapts an <code>IDocument</code> to the <code>StyledTextContent</code> interface.
 * The document adapter is used by <code>TextViewer</code> to translate document changes
 * into styled text content changes and vice versa.
 * Clients may implement this interface and override <code>TextViewer.createDocumentAdapter</code>
 * if they want to intercept the communication between the viewer's text widget and
 * the viewer's document.
 * 
 * @see IDocument
 * @see StyledTextContent
 */
public interface IDocumentAdapter extends StyledTextContent {
	
	/**
	 * Sets the adapters document.
	 * 
	 * @param document the document to be adapted
	 */
	void setDocument(IDocument document);
}
