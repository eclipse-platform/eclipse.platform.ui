package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

/**
 * A child document event represents an absolute document event, i. e.
 * one which has been issued by the parent document, as a child-relative
 * document event. It also carries the original event.
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
