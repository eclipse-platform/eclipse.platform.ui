package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Interface for objects which are interested in getting informed about 
 * document changes. A listener is informed about document changes before 
 * they are applied and after they have been applied. It is ensured that
 * the document event passed into the listener is the same for the two
 * notifications, i.e. the two document events can be checked using object identity.
 * Clients may implement this interface.
 */
public interface IDocumentListener {
	
	
	/**
	 * The manipulation described by the document event will be performed.
	 * 
	 * @param event the document event describing the document change 
	 */
	void documentAboutToBeChanged(DocumentEvent event);

	/**
	 * The manipulation described by the document event has been performed.
	 *
	 * @param event the document event describing the document change
	 */
	void documentChanged(DocumentEvent event);
}
