/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.text;

import java.util.Iterator;
import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * Filters problems based on their types.
 */
public class XMLAnnotationIterator implements Iterator {
			
	private Iterator fIterator;
	private IXMLAnnotation fNext;
	private boolean fSkipIrrelevants;
	
	public XMLAnnotationIterator(IAnnotationModel model, boolean skipIrrelevants) {
		fIterator= model.getAnnotationIterator();
		fSkipIrrelevants= skipIrrelevants;
		skip();
	}
	
	private void skip() {
		while (fIterator.hasNext()) {
			Object next= fIterator.next();
			if (next instanceof IXMLAnnotation) {
				IXMLAnnotation a= (IXMLAnnotation) next;
				if (fSkipIrrelevants) {
					if (a.isRelevant()) {
						fNext= a;
						return;
					}
				} else {
					fNext= a;
					return;
				}
			}
		}
		fNext= null;
	}
	
	/*
	 * @see Iterator#hasNext()
	 */
	public boolean hasNext() {
		return fNext != null;
	}

	/*
	 * @see Iterator#next()
	 */
	public Object next() {
		try {
			return fNext;
		} finally {
			skip();
		}
	}

	/*
	 * @see Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
