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

package org.eclipse.jface.text;


/**
 * ProjectionTextStore.java
 */
public class ProjectionTextStore implements ITextStore {
	
	private ProjectionDocument fProjectionDocument;

	public ProjectionTextStore(ProjectionDocument projectionDocument) {
		fProjectionDocument= projectionDocument;
	}
	
	private int computeParentDocumentOffset(int offset) {
		try {
			return fProjectionDocument.toParentDocumentOffset(offset);
		} catch (BadLocationException x) {
			throw new RuntimeException();
		}
	}

	/*
	 * @see ITextStore#set
	 */
	public void set(String contents) {
		
		try {

			Position[] projection= fProjectionDocument.getProjection();
			if (projection != null && projection.length > 0) {
				Position first=projection[0];
				Position last= projection[projection.length -1];
				int length= last.offset - first.offset + last.length;
				getParentDocument().replace(first.getOffset(), length, contents);
			} else {
				getParentDocument().set(contents);
			}

		} catch (BadLocationException x) {
		}
	}

	/*
	 * @see ITextStore#replace
	 */
	public void replace(int offset, int length, String text) {
		
		try {
			
			int endoffset= length > 0 ? offset + length -1 : offset;
			int o2= computeParentDocumentOffset(endoffset);
			if (length > 0)
				++ o2;
			
			offset= computeParentDocumentOffset(offset);
			length= o2 - offset;
			
			getParentDocument().replace(offset, length, text);
			
		} catch (BadLocationException x) {
			// ignored as surrounding document should have handled this
		}
	}

	/*
	 * @see ITextStore#getLength
	 */
	public int getLength() {
		Position[] projection= fProjectionDocument.getProjection();
		if (projection == null || projection.length == 0)
			return 0;
			
		int length= 0;
		for (int i= 0; i < projection.length; i++)
			length += projection[i].length;
		return length;
	}

	/*
	 * @see ITextStore#get
	 */
	public String get(int offset, int length) {
		try {
			
			Fragment[] fragments= fProjectionDocument.getFragmentsOfRange(offset, length);
			if (fragments == null || fragments.length == 0)
				return ""; //$NON-NLS-1$
			
			StringBuffer buffer= new StringBuffer();
			for (int i= 0; i < fragments.length; i++) {
				Position p= fragments[i].getOrigin();
				buffer.append(getParentDocument().get(p.offset, p.length));
			}
			
			offset -= fragments[0].offset;
			return buffer.substring(offset, offset + length);
			
		} catch (BadLocationException x) {
		}

		return null;
	}

	private IDocument getParentDocument() {
		return fProjectionDocument.getParentDocument();
	}

	/*
	 * @see ITextStore#get
	 */
	public char get(int offset) {
		try {
			int o= computeParentDocumentOffset(offset);
			return getParentDocument().getChar(o);
		} catch (BadLocationException x) {
		}

		return (char) 0;
	}
}
