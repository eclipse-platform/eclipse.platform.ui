package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */


/**
 * Text input listeners registered with an text viewer are informed 
 * if the document serving as the text viewer's model is replaced.
 * Clients may implement this interface.
 *
 * @see ITextViewer
 * @see IDocument
 */
public interface ITextInputListener {
	
	/**
	 * Called before the input document is replaced.
	 * 
	 * @param oldInput the text viewer's previous input document
	 * @param newInput the text viewer's new input document
	 */
	void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput);
	/**
	 * Called after the input document has been replaced.
	 * 
	 * @param oldInput the text viewer's previous input document
	 * @param newInput the text viewer's new input document
	 */
	void inputDocumentChanged(IDocument oldInput, IDocument newInput);
}
