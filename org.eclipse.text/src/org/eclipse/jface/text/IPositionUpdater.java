package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A position updater is responsible for adapting document positions.
 * When installed on a document, the position updater updates the 
 * document's positions to changes applied to this document. Document
 * updaters can be selective, i.e. they might only update positions of 
 * a certain category.<p>
 * Position updaters are of primary importance for the definition of
 * the semantics of positions.<p>
 * Clients may implement this interface or use the standard implementation
 * <code>DefaultPositionUpdater</code>.
 *
 * @see IDocument
 * @see Position
 */
public interface IPositionUpdater {
	
	/**
	 * Adapts positions to the change specified by the document event.
	 * It is ensured that the document's partitioning has been adapted to
	 * this document change and that all the position updaters which have 
	 * a smaller index in the document's position updater list have been called.
	 *
	 * @param event the document event describing the document change
	 */
	void update(DocumentEvent event);
}
