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
package org.eclipse.core.internal.filebuffers;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;


/**
 * Document that can be synchronized with a lock object.
 * <p>
 * Initially no locking takes place.
 * </p>
 * 
 * @since 3.2
 */
public class SynchronizableDocument extends Document implements ISynchronizable {
    
    private Object fLockObject;
    
    /*
     * @see org.eclipse.jface.text.ISynchronizable#setLockObject(java.lang.Object)
     */
    public synchronized void setLockObject(Object lockObject) {
        fLockObject= lockObject;
    }

    /*
     * @see org.eclipse.jface.text.ISynchronizable#getLockObject()
     */
    public synchronized Object getLockObject() {
        return fLockObject;
    }
	
	/*
	 * @see IDocumentExtension#startSequentialRewrite(boolean)
	 */
	public void startSequentialRewrite(boolean normalized) {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.startSequentialRewrite(normalized);
			return;
		}
	    synchronized (lockObject) {
	        super.startSequentialRewrite(normalized);
	    }
	}

	/*
	 * @see IDocumentExtension#stopSequentialRewrite()
	 */
	public void stopSequentialRewrite() {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.stopSequentialRewrite();
			return;
		}
		synchronized (lockObject) {
            super.stopSequentialRewrite();
        }
    }
	
	/*
	 * @see IDocument#get()
	 */
	public String get() {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			return super.get();
		}
		synchronized (lockObject) {
            return super.get();
        }
    }
	
	/*
	 * @see IDocument#get(int, int)
	 */
	public String get(int offset, int length) throws BadLocationException {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			return super.get(offset, length);
		}
		synchronized (lockObject) {
            return super.get(offset, length);
        }
	}
	
	/*
	 * @see IDocument#getChar(int)
	 */
	public char getChar(int offset) throws BadLocationException {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			return super.getChar(offset);
		}
		synchronized (lockObject) {
            return super.getChar(offset);
        }
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentExtension4#getModificationStamp()
	 * @since 3.1
	 */
	public long getModificationStamp() {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			return super.getModificationStamp();
		}
		synchronized (lockObject) {
			return super.getModificationStamp();
		}
	}
	
	/*
	 * @see IDocument#replace(int, int, String)
	 */
	public void replace(int offset, int length, String text) throws BadLocationException {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.replace(offset, length, text);
			return;
		}
		synchronized (lockObject) {
            super.replace(offset, length, text);
        }
	}
	
	/*
	 * @see IDocumentExtension4#replace(int, int, String, long)
	 */
	public void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.replace(offset, length, text, modificationStamp);
			return;
		}
		synchronized (lockObject) {
            super.replace(offset, length, text, modificationStamp);
        }
	}
	
	/*
	 * @see IDocument#set(String)
	 */
	public void set(String text) {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.set(text);
			return;
		}
		synchronized (lockObject) {
            super.set(text);
        }
	}
	
	/*
	 * @see IDocumentExtension4#set(String, long)
	 */
	public void set(String text, long modificationStamp) {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.set(text, modificationStamp);
			return;
		}
		synchronized (lockObject) {
            super.set(text, modificationStamp);
        }
	}
	
	/*
	 * @see org.eclipse.jface.text.AbstractDocument#addPosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
	public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.addPosition(category, position);
			return;
		}
		synchronized (lockObject) {
            super.addPosition(category, position);
        }
	}
	
	/*
	 * @see org.eclipse.jface.text.AbstractDocument#removePosition(java.lang.String, org.eclipse.jface.text.Position)
	 */
	public void removePosition(String category, Position position) throws BadPositionCategoryException {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			super.removePosition(category, position);
			return;
		}
		synchronized (lockObject) {
            super.removePosition(category, position);
        }
	}
	
	/*
	 * @see org.eclipse.jface.text.AbstractDocument#getPositions(java.lang.String)
	 */
	public Position[] getPositions(String category) throws BadPositionCategoryException {
		Object lockObject= getLockObject();
		if (lockObject == null) {
			return super.getPositions(category);
		}
		synchronized (lockObject) {
            return super.getPositions(category);
        }
	}
}
