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
package org.eclipse.ant.internal.ui.editor.text;


import java.util.Iterator;

import org.eclipse.ant.internal.ui.editor.derived.HTMLPrinter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;


public class XMLTextHover implements ITextHover {

	//private AntEditor fEditor;
	
//	public XMLTextHover(AntEditor editor) {
//		super();
//		fEditor = editor;
//	}
	
	/*
	 * Formats a message as HTML text.
	 */
	private String formatMessage(String message) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		
		if (!(textViewer instanceof ISourceViewer)) {
			return null;
		}
		
		ISourceViewer sourceViewer= (ISourceViewer) textViewer;
		IAnnotationModel model= sourceViewer.getAnnotationModel();
		
		if (model != null) {
			Iterator e= new XMLAnnotationIterator(model, true);
			while (e.hasNext()) {
				Annotation a= (Annotation) e.next();
				Position p= model.getPosition(a);
				if (p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg= ((IXMLAnnotation) a).getMessage();
					if (msg != null && msg.trim().length() > 0)
						return formatMessage(msg);
				}
			}
		}
//		try {
//			IDocument document= textViewer.getDocument();
//			int offset= hoverRegion.getOffset();
//			int length= hoverRegion.getLength();
//			String propertyName= document.get(offset, length);
			//AntModel model fEditor.getModel();
			//String value= model.getPropertyValue(propertyName);
			//if (value != null) {
			//	return value;
			//}
//		} catch (BadLocationException e) {
//			
//		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		IDocument document= textViewer.getDocument();
			
		int start= -1;
		int end= -1;
		
		try {	
			int pos= offset;
			char c;
			
			while (pos >= 0) {
				c= document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			
			start= pos;
			
			pos= offset;
			int length= document.getLength();
			
			while (pos < length) {
				c= document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			
			end= pos;
			
		} catch (BadLocationException x) {
		}
		
		if (start > -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}
		
		return null;
	}
}