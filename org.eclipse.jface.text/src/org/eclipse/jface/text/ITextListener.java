package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Text listeners registered with an text viewer are informed about 
 * all text viewer modifications by means of text events. A text event
 * describes a change as a replace operation.<p>
 * The changes described in the event are the changes applied to the text viewer's
 * widget (i.e. its visual representation) and not those applied to the text viewer's
 * document. The text event can be asked to return the according document 
 * event. If a text listener receives a text event, it is guaranteed that 
 * both the document and the viewer's visual representation are in sync.<p>
 * Clients may implement this interface.
 *
 * @see ITextViewer
 * @see TextEvent
 * @see DocumentEvent
 */
public interface ITextListener {
	
	/**
	 * The visual representation of a text viewer this listener is registered with
	 * has been changed.
	 *
	 * @param event the description of the change
	 */
	void textChanged(TextEvent event);
}
