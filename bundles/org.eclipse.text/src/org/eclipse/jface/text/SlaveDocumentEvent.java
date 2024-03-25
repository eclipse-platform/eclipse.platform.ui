/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jface.text;


/**
 * A slave document event represents a master document event as a slave-relative
 * document event. It also carries the master document event.
 */
public class SlaveDocumentEvent extends DocumentEvent {

	/** The master document event */
	private DocumentEvent fMasterEvent;

	/**
	 * Creates a new slave document event.
	 *
	 * @param doc the slave document
	 * @param offset the offset in the slave document
	 * @param length the length in the slave document
	 * @param text the substitution text
	 * @param masterEvent the master document event
	 */
	public SlaveDocumentEvent(IDocument doc, int offset, int length, String text, DocumentEvent masterEvent) {
		super(doc, offset, length, text);
		fMasterEvent= masterEvent;
	}

	/**
	 * Returns this event's master event.
	 *
	 * @return this event's master event
	 */
	public DocumentEvent getMasterEvent() {
		return fMasterEvent;
	}
}
