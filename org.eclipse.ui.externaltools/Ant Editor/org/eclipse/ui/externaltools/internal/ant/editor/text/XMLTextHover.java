/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.text;


import java.util.Iterator;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.externaltools.internal.ant.editor.derived.HTMLPrinter;


public class XMLTextHover implements ITextHover {

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
	
	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		
		if (!(textViewer instanceof ISourceViewer))
			return null;
		
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
		
		return null;
	}
	
	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		int startOffset= offset > 0 ? offset - 1 : offset;
		int documentLength= textViewer.getDocument() != null ? textViewer.getDocument().getLength() : 0;
		int endOffset= offset < documentLength - 1 ? offset + 1 : offset;
		return new Region(startOffset, endOffset - startOffset);
	}

}