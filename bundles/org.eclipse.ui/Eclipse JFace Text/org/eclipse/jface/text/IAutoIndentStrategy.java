package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


/**
 * An auto indent strategy can adapt changes that will be applied to
 * a text viewer's document. The strategy is informed by the text viewer
 * about each upcoming change in form of a document command. By manipulating
 * this document command, the strategy can influence in which way the text 
 * viewer's document is changed. Clients may implement this interface or
 * use the standard implementation <code>DefaultAutoIndentStrategy</code>.
 */
public interface IAutoIndentStrategy {
	
	/**
	 * Allows the strategy to manipulate the document command.
	 *
	 * @param document the document that will be changed
	 * @param command the document command describing the indented change
	 */
	void customizeDocumentCommand(IDocument document, DocumentCommand command);
}
