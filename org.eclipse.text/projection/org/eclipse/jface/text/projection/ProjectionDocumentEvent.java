/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.SlaveDocumentEvent;

/**
 * Internal class. Do not use. Only public for testing purposes.
 * 
 * @since 3.0
 */
public class ProjectionDocumentEvent extends SlaveDocumentEvent {
	
	public final static Object PROJECTION_CHANGE= new Object();
	public final static Object CONTENT_CHANGE= new Object();

	private Object fChangeType;
	private int fMasterOffset= -1;
	private int fMasterLength= -1;
	
	public ProjectionDocumentEvent(IDocument doc, int offset, int length, String text, DocumentEvent masterEvent) {
		super(doc, offset, length, text, masterEvent);
		fChangeType= CONTENT_CHANGE;
	}
	
	public ProjectionDocumentEvent(IDocument doc, int offset, int length, String text, int masterOffset, int masterLength) {
		super(doc, offset, length, text, null);
		fChangeType= PROJECTION_CHANGE;
		fMasterOffset= masterOffset;
		fMasterLength= masterLength;
	}

	public ProjectionDocumentEvent(IDocument doc, int offset, int length, String text, int masterOffset, int masterLength, DocumentEvent masterEvent) {
		super(doc, offset, length, text, masterEvent);
		fChangeType= PROJECTION_CHANGE;
		fMasterOffset= masterOffset;
		fMasterLength= masterLength;
	}
	
	public Object getChangeType() {
		return fChangeType;
	}
	
	public int getMasterOffset() {
		return fMasterOffset;
	}
	
	public int getMasterLength() {
		return fMasterLength;
	}
}
