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


import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.OutlinerRulerColumn;
import org.eclipse.jface.text.source.ProjectionAnnotation;
import org.eclipse.jface.text.source.ProjectionSourceViewer;

import org.eclipse.jface.action.IMenuManager;

import org.eclipse.ui.texteditor.ITextEditorActionConstants;


/**
 * ProjectionTextEditor.java
 */
public class ProjectionTextEditor extends TextEditor {
	
	private IAnnotationModel fProjectionAnnotationModel;
	
	public void collapse(int offset, int length) {
		ProjectionSourceViewer viewer= (ProjectionSourceViewer) getSourceViewer();
		viewer.collapse(offset, length);
	}
	
	public void expand(int offset, int length) {
		ProjectionSourceViewer viewer= (ProjectionSourceViewer) getSourceViewer();;
		viewer.expand(offset, length);
	}
	
	public void defineProjection(int offset, int length) {
		Position p= new Position(offset, length);
		fProjectionAnnotationModel.addAnnotation(new ProjectionAnnotation(p), p);
	}
	
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ProjectionSourceViewer viewer= new ProjectionSourceViewer(parent, ruler, styles);
		if (fProjectionAnnotationModel != null) {
			viewer.setProjectionAnnotationModel(fProjectionAnnotationModel);
			StyledText text= viewer.getTextWidget();
			text.addPaintListener(new ProjectionPainter(viewer));
		}
		return viewer;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();
		setAction("DefineProjection", new DefineProjectionAction(EditorMessages.getResourceBundle(), "Editor.DefineProjection.", this));
	}
	
	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "DefineProjection");
	}
	
	/*
	 * @see AbstractTextEditor#createVerticalRuler()
	 */
	protected IVerticalRuler createVerticalRuler() {
		CompositeRuler ruler= new CompositeRuler(2);
		ruler.addDecorator(0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH));
		ruler.addDecorator(1, new LineNumberRulerColumn());
		fProjectionAnnotationModel= new AnnotationModel();
		IVerticalRulerColumn column= new OutlinerRulerColumn(fProjectionAnnotationModel, VERTICAL_RULER_WIDTH);
		ruler.addDecorator(2, column);
		return ruler;
	}
	
	/*
	 * @see ITextEditor#setHighlightRange
	 */
	public void setHighlightRange(int start, int length, boolean moveCursor) {
		ISourceViewer sourceViewer= getSourceViewer();
		if (sourceViewer != null) {
			IRegion rangeIndication= sourceViewer.getRangeIndication();
			if (rangeIndication == null || start != rangeIndication.getOffset() || length != rangeIndication.getLength())
				sourceViewer.setRangeIndication(start, length, moveCursor);
		}
	}
	
	/*
	 * @see ITextEditor#getHighlightRange
	 */
	public IRegion getHighlightRange() {
		ISourceViewer sourceViewer= getSourceViewer();
		if (sourceViewer != null)
			return sourceViewer.getRangeIndication();
		return null;
	}
}
