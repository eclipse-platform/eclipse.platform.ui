/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.editors.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ProjectionAnnotation;
import org.eclipse.jface.text.source.ProjectionSourceViewer;

/**
 * ProjectionPainter.java
 */
public class ProjectionPainter implements PaintListener{
	
	private ProjectionSourceViewer fSourceViewer;
	
	
	public ProjectionPainter(ProjectionSourceViewer sourceViewer) {
		fSourceViewer= sourceViewer;
	}
	
	private IAnnotationModel getAnnotationModel() {
		return fSourceViewer.getProjectionAnnotationModel();
	}
	
	private IDocument getDocument() {
		return fSourceViewer.getDocument();
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent e) {
		IRegion lineRange= computeLineRange(e);
		IRegion characterRange= computeCharacterRange(lineRange);
		List annotations= findAnnotations(characterRange);
		if (annotations != null) {
			
			IAnnotationModel model= getAnnotationModel();
			IDocument document= getDocument();
			StyledText text= fSourceViewer.getTextWidget();
			
			Iterator iter= annotations.iterator();
			while (iter.hasNext()) {
				ProjectionAnnotation a= (ProjectionAnnotation) iter.next();
				Position p= model.getPosition(a);
				
				try {
					IRegion lineInfo= document.getLineInformationOfOffset(p.getOffset());
					int modelOffset= lineInfo.getOffset() + lineInfo.getLength();
					int widgetOffset= fSourceViewer.modelOffset2WidgetOffset(modelOffset);
					doPaint(text, e.gc, text.getLocationAtOffset(widgetOffset));
				} catch (BadLocationException x) {
				}
			}
		}
	}
		
	private void doPaint(StyledText styledText, GC gc, Point point) {
		gc.setForeground(styledText.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		FontMetrics metrics= gc.getFontMetrics();
		gc.drawRectangle(point.x +3, point.y, metrics.getAverageCharWidth() * 2, metrics.getHeight() - 1);
	}
	
	private IRegion computeLineRange(PaintEvent e) {
		StyledText text= fSourceViewer.getTextWidget();
		int widgetLine= ((e.y + text.getTopPixel()) / text.getLineHeight());
		int startLine= fSourceViewer.widgetlLine2ModelLine(widgetLine);
		
		widgetLine= ((e.y + e.height + text.getTopPixel()) / text.getLineHeight());
		IDocument visible= fSourceViewer.getVisibleDocument();
		widgetLine= Math.min(widgetLine, visible.getNumberOfLines() -1);
		int endLine= fSourceViewer.widgetlLine2ModelLine(widgetLine);
		
		return new Region(startLine, Math.max(0, endLine - startLine));
	}
	
	private IRegion computeCharacterRange(IRegion lineRange) {
		IDocument document= fSourceViewer.getDocument();
		try {
			int startOffset= document.getLineOffset(lineRange.getOffset());
			int endLine= lineRange.getOffset() + lineRange.getLength();
			int endOffset= document.getLineOffset(endLine) + document.getLineLength(endLine);
			return new Region(startOffset, endOffset - startOffset);
		} catch (BadLocationException x) {
		}
		return null;
	}
	
	private List findAnnotations(IRegion characterRange) {
		List result= new ArrayList();
		IAnnotationModel model= getAnnotationModel();
		if (model != null) {
			Iterator e= model.getAnnotationIterator();
			while (e.hasNext()) {
				Object next= e.next();
				if (next instanceof ProjectionAnnotation) {
					ProjectionAnnotation annotation= (ProjectionAnnotation) next;
					if (annotation.isFolded()) {
						Position p= model.getPosition(annotation);
						if (p.overlapsWith(characterRange.getOffset(), characterRange.getLength()))
							result.add(annotation);
					}
				}
			}
		}
		return result;
	}
}