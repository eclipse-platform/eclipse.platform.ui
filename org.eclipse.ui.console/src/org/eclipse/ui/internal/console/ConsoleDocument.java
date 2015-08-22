/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

/**
 * A console document. Requires synchronization for multi-threaded access.
 */
public class ConsoleDocument extends Document {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get(int, int)
	 */
	@Override
	public synchronized String get(int pos, int length) throws BadLocationException {
		return super.get(pos, length);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLength()
	 */
	@Override
	public synchronized int getLength() {
		return super.getLength();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineDelimiter(int)
	 */
	@Override
	public synchronized String getLineDelimiter(int line) throws BadLocationException {
		return super.getLineDelimiter(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineInformation(int)
	 */
	@Override
	public synchronized IRegion getLineInformation(int line) throws BadLocationException {
		return super.getLineInformation(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineInformationOfOffset(int)
	 */
	@Override
	public synchronized IRegion getLineInformationOfOffset(int offset) throws BadLocationException {
		return super.getLineInformationOfOffset(offset);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineLength(int)
	 */
	@Override
	public synchronized int getLineLength(int line) throws BadLocationException {
		return super.getLineLength(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineOffset(int)
	 */
	@Override
	public synchronized int getLineOffset(int line) throws BadLocationException {
		return super.getLineOffset(line);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLineOfOffset(int)
	 */
	@Override
	public int getLineOfOffset(int pos) throws BadLocationException {
		return super.getLineOfOffset(pos);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getNumberOfLines()
	 */
	@Override
	public synchronized int getNumberOfLines() {
		return super.getNumberOfLines();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#replace(int, int, java.lang.String)
	 */
	@Override
	public synchronized void replace(int pos, int length, String text) throws BadLocationException {
		super.replace(pos, length, text);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#set(java.lang.String)
	 */
    @Override
	public synchronized void set(String text) {
        super.set(text);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.AbstractDocument#completeInitialization()
	 */
    @Override
	protected void completeInitialization() {
        super.completeInitialization();
        addPositionUpdater(new HyperlinkUpdater());
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#addPosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
	@Override
	public synchronized void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
        super.addPosition(category, position);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#removePosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
    @Override
	public synchronized void removePosition(String category, Position position) throws BadPositionCategoryException {
        super.removePosition(category, position);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getPositions(java.lang.String)
	 */
    @Override
	public synchronized Position[] getPositions(String category) throws BadPositionCategoryException {
        return super.getPositions(category);
    }
}
