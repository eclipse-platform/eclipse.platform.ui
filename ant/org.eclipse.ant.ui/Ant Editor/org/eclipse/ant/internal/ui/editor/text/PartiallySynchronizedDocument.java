/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.text;

import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * Document that can also be used by a background reconciler.
 */
public class PartiallySynchronizedDocument extends Document {
	
	AntModel fAntModel= null; 
			
	/*
	 * @see IDocumentExtension#startSequentialRewrite(boolean)
	 */
	synchronized public void startSequentialRewrite(boolean normalized) {
		super.startSequentialRewrite(normalized);
	}
		
	/*
	 * @see IDocumentExtension#stopSequentialRewrite()
	 */
	synchronized public void stopSequentialRewrite() {
		super.stopSequentialRewrite();
	}
			
	/*
	 * @see IDocument#get()
	 */
	synchronized public String get() {
		return super.get();
	}
			
	/*
	 * @see IDocument#get(int, int)
	 */
	synchronized public String get(int offset, int length) throws BadLocationException {
		return super.get(offset, length);
	}
			
	/*
	 * @see IDocument#getChar(int)
	 */
	synchronized public char getChar(int offset) throws BadLocationException {
		return super.getChar(offset);
	}
			
	/*
	 * @see IDocument#replace(int, int, String)
	 */
	synchronized public void replace(int offset, int length, String text) throws BadLocationException {
		super.replace(offset, length, text);
		if (length == 0 && text != null) {
			// Insert
		} else if (text == null || text.length() == 0) {
			// Remove
		} else {
			fAntModel.setReplaceHasOccurred();
		}
	}
			
	/*
	 * @see IDocument#set(String)
	 */
	synchronized public void set(String text) {
		super.set(text);
	}
	
	public void setAntModel(AntModel model) {
		fAntModel= model;
	}
}
