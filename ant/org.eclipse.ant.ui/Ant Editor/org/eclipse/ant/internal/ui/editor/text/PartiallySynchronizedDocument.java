/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;

/**
 * Document that can also be used by a background reconciler.
 */
public class PartiallySynchronizedDocument extends Document implements ISynchronizable {
    
    private final Object fInternalLockObject= new Object();
    private Object fLockObject;
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ISynchronizable#setLockObject(java.lang.Object)
     */
    public void setLockObject(Object lockObject) {
        fLockObject= lockObject;
    }

    public Object getLockObject() {
        return fLockObject == null ? fInternalLockObject : fLockObject;
    }
   
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentExtension#startSequentialRewrite(boolean)
	 */
	public void startSequentialRewrite(boolean normalized) {
	    synchronized (getLockObject()) {
	        super.startSequentialRewrite(normalized);
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentExtension#stopSequentialRewrite()
	 */
	public void stopSequentialRewrite() {
		synchronized (getLockObject()) {
            super.stopSequentialRewrite();
        }
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get()
	 */
	public String get() {
		synchronized (getLockObject()) {
            return super.get();
        }
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get(int, int)
	 */
	public String get(int offset, int length) throws BadLocationException {
		synchronized (getLockObject()) {
            return super.get(offset, length);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getChar(int)
	 */
	public char getChar(int offset) throws BadLocationException {
		synchronized (getLockObject()) {
            return super.getChar(offset);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentExtension4#getModificationStamp()
	 */
	public long getModificationStamp() {
		synchronized (getLockObject()) {
			return super.getModificationStamp();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#replace(int, int, java.lang.String)
	 */
	public void replace(int offset, int length, String text) throws BadLocationException {
		synchronized (getLockObject()) {
            super.replace(offset, length, text);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentExtension4#replace(int, int, java.lang.String, long)
	 */
	public void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException {
		synchronized (getLockObject()) {
            super.replace(offset, length, text, modificationStamp);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#set(java.lang.String)
	 */
	public void set(String text) {
		synchronized (getLockObject()) {
            super.set(text);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentExtension4#set(java.lang.String, long)
	 */
	public void set(String text, long modificationStamp) {
		synchronized (getLockObject()) {
            super.set(text, modificationStamp);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#addPosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
	public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
		synchronized (getLockObject()) {
            super.addPosition(category, position);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#removePosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
	public void removePosition(String category, Position position) throws BadPositionCategoryException {
		synchronized (getLockObject()) {
            super.removePosition(category, position);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getPositions(java.lang.String)
	 */
	public Position[] getPositions(String category) throws BadPositionCategoryException {
		synchronized (getLockObject()) {
            return super.getPositions(category);
        }
	}
}