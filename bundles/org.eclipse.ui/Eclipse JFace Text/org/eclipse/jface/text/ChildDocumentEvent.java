package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A child document event represents a parent document event as a
 * child-relative document event. It also carries the original event.
 */
class ChildDocumentEvent extends DocumentEvent {
	
	/** The parent document event */
	private DocumentEvent fParentEvent;
	
	/**
	 * Creates a new child document event.
	 *
	 * @param doc the child document
	 * @param offset the offset in the child document
	 * @param length the length in the child document
	 * @param text the substitution text
	 * @param parentEvent the parent Event
	 */
	public ChildDocumentEvent(IDocument doc, int offset, int length, String text, DocumentEvent parentEvent) {
		super(doc, offset, length, text);
		fParentEvent= parentEvent;
	}
	
	/**
	 * Returns this event's parent event.
	 *
	 * @return this event's parent event
	 */
	public DocumentEvent getParentEvent() {
		return fParentEvent;
	}
}