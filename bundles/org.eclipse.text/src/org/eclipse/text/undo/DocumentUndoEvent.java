/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.undo;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.IDocument;


/**
 * Describes document changes initiated by undo or redo.
 * <p>
 * Clients are not supposed to subclass or create instances of this class.
 * </p>
 *
 * @see IDocumentUndoManager
 * @see IDocumentUndoListener
 * @since 3.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DocumentUndoEvent {

	/**
	 * Indicates that the described document event is about to be
	 * undone.
	 */
	public static final int ABOUT_TO_UNDO= 1 << 0;

	/**
	 * Indicates that the described document event is about to be
	 * redone.
	 */
	public static final int ABOUT_TO_REDO= 1 << 1;

	/**
	 * Indicates that the described document event has been undone.
	 */
	public static final int UNDONE= 1 << 2;

	/**
	 * Indicates that the described document event has been redone.
	 */
	public static final int REDONE= 1 << 3;

	/**
	 * Indicates that the described document event is a compound undo
	 * or redo event.
	 */
	public static final int COMPOUND= 1 << 4;

	/** The changed document. */
	private final IDocument fDocument;

	/** The document offset where the change begins. */
	private final int fOffset;

	/** Text inserted into the document. */
	private final String fText;

	/** Text replaced in the document. */
	private final String fPreservedText;

	/** Bit mask of event types describing the event */
	private final int fEventType;

	/** The source that triggered this event or <code>null</code> if unknown. */
	private final Object fSource;

	/**
	 * Creates a new document event.
	 *
	 * @param doc the changed document
	 * @param offset the offset of the replaced text
	 * @param text the substitution text
	 * @param preservedText the replaced text
	 * @param eventType a bit mask describing the type(s) of event
	 * @param source the source that triggered this event or <code>null</code> if unknown
	 */
	DocumentUndoEvent(IDocument doc, int offset, String text, String preservedText, int eventType, Object source) {

		Assert.isNotNull(doc);
		Assert.isTrue(offset >= 0);

		fDocument= doc;
		fOffset= offset;
		fText= text;
		fPreservedText= preservedText;
		fEventType= eventType;
		fSource= source;
	}

	/**
	 * Returns the changed document.
	 *
	 * @return the changed document
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/**
	 * Returns the offset of the change.
	 *
	 * @return the offset of the change
	 */
	public int getOffset() {
		return fOffset;
	}

	/**
	 * Returns the text that has been inserted.
	 *
	 * @return the text that has been inserted
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Returns the text that has been replaced.
	 *
	 * @return the text that has been replaced
	 */
	public String getPreservedText() {
		return fPreservedText;
	}

	/**
	 * Returns the type of event that is occurring.
	 *
	 * @return the bit mask that indicates the type (or types) of the event
	 */
	public int getEventType() {
		return fEventType;
	}

	/**
	 * Returns the source that triggered this event.
	 *
	 * @return the source that triggered this event.
	 */
	public Object getSource() {
		return fSource;
	}

	/**
	 * Returns whether the change was a compound change or not.
	 *
	 * @return	<code>true</code> if the undo or redo change is a
	 * 			compound change, <code>false</code> if it is not
	 */
	public boolean isCompound() {
		return (fEventType & COMPOUND) != 0;
	}
}
