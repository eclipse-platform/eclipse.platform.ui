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

package org.eclipse.jface.text.source.projection;


import java.util.Iterator;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.projection.ProjectionDocument;
import org.eclipse.jface.text.projection.ProjectionDocumentManager;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;


/**
 * A projection source viewer is a source viewer which does not support the
 * concept of a single visible region. Instead it supports multiple visible
 * regions which can dynamically be changed.
 * <p>
 * A projection source viewer uses a <code>ProjectionDocumentManager</code>
 * for the management of the visible document.
 * <p>
 * API in progress. Do not yet use.
 * 
 * @since 3.0
 */
public class ProjectionViewer extends SourceViewer implements ITextViewerExtension5 {
	
	/**
	 * Internal implementer of <code>IProjectionAnnotationModel</code>.
	 */
	private static class ProjectionAnnotationModel extends AnnotationModel implements IProjectionAnnotationModel {
	}
	
	/**
	 * Key of the projection annotation model inside the visual annotation model.
	 */
	protected final static Object PROJECTION_ANNOTATION_MODEL= new Object();
		
	
	/** The projection annotation model used by this viewer. */
	private ProjectionAnnotationModel fProjectionAnnotationModel;
	
	
	/**
	 * Creates a new projection source viewer.
	 * 
	 * @param parent the SWT parent control
	 * @param ruler the vertical ruler
	 * @param styles the SWT style bits
	 */
	public ProjectionViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
	}
	
	/**
	 * Creates the projection annotation model and adds it to the given annotation model.
	 * 
	 * @param model the model to which the projection annotation model is added
	 */
	protected void addProjectionAnnotationModel(IAnnotationModel model) {
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			extension.addAnnotationModel(PROJECTION_ANNOTATION_MODEL, fProjectionAnnotationModel);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createVisualAnnotationModel(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	protected IAnnotationModel createVisualAnnotationModel(IAnnotationModel annotationModel) {
		IAnnotationModel model= super.createVisualAnnotationModel(annotationModel);
		fProjectionAnnotationModel= new ProjectionAnnotationModel();
		addProjectionAnnotationModel(model);
		return model;
	}

	/**
	 * Returns the projection annotation model.
	 * 
	 * @return the projection annotation model
	 */
	public IProjectionAnnotationModel getProjectionAnnotationModel() {
		IAnnotationModel model= getVisualAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			return (IProjectionAnnotationModel) extension.getAnnotationModel(PROJECTION_ANNOTATION_MODEL);
		}
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#createSlaveDocumentManager()
	 */
	protected ISlaveDocumentManager createSlaveDocumentManager() {
		return new ProjectionDocumentManager();
	}

	/*
	 * @see org.eclipse.jface.text.TextViewer#updateSlaveDocument(org.eclipse.jface.text.IDocument, int, int)
	 */
	protected boolean updateSlaveDocument(IDocument slaveDocument, int modelRangeOffset, int modelRangeLength) throws BadLocationException {
		if (slaveDocument instanceof ProjectionDocument) {
			ProjectionDocument document= (ProjectionDocument) slaveDocument;
			document.replaceMasterDocumentRanges(modelRangeOffset, modelRangeLength);
			return true;
		}
		return false;
	}
	
	/**
	 * Returns whether this viewer is in projection mode.
	 * 
	 * @return <code>true</code> if this viewer is in projection mode,
	 *         <code>false</code> otherwise
	 */
	private boolean isProjectionMode() {
		return getProjectionAnnotationModel() != null;
	}

	/**
	 * Disables the projection mode. 
	 */
	private void disableProjection() {
		if (isProjectionMode()) {
			IAnnotationModel model= getVisualAnnotationModel();
			if (model instanceof IAnnotationModelExtension) {
				IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
				extension.removeAnnotationModel(PROJECTION_ANNOTATION_MODEL);
			}
			fProjectionAnnotationModel.removeAllAnnotations();
		}
	}
	
	/**
	 * Enables the projection mode.
	 */
	private void enableProjection() {
		if (!isProjectionMode()) 
			addProjectionAnnotationModel(getVisualAnnotationModel());
	}

	/*
	 * @see org.eclipse.jface.text.TextViewer#setVisibleRegion(int, int)
	 */
	public void setVisibleRegion(int start, int length) {
		disableProjection();
		super.setVisibleRegion(start, length);
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#resetVisibleRegion()
	 */
	public void resetVisibleRegion() {
		super.resetVisibleRegion();
		enableProjection();
	}
	
	/*
	 * @see org.eclipse.jface.text.ITextViewer#getVisibleRegion()
	 */
	public IRegion getVisibleRegion() {
		disableProjection();
		return super.getVisibleRegion();
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewer#overlapsWithVisibleRegion(int,int)
	 */
	public boolean overlapsWithVisibleRegion(int offset, int length) {
		disableProjection();
		return super.overlapsWithVisibleRegion(offset, length);
	}
	
	/**
	 * Replace the visible document with the given document. Maintains the
	 * scroll offset and the selection.
	 * 
	 * @param visibleDocument the visible document
	 */
	private void replaceVisibleDocument(IDocument visibleDocument) {
		StyledText textWidget= getTextWidget();
		try {
			if (textWidget != null)
				textWidget.setRedraw(false);
			
			int topIndex= getTopIndex();
			Point selection= getSelectedRange();
			setVisibleDocument(visibleDocument);
			setSelectedRange(selection.x, selection.y);
			setTopIndex(topIndex);

		} finally {
			if (textWidget != null)
				textWidget.setRedraw(true);
		}
	}
	
	/**
	 * Hides the given range by collapsing it.
	 *
	 * @param offset the offset of the range to hide
	 * @param length the length of the range to hide
	 */
	public void collapse(int offset, int length) {
		try {
			ProjectionDocument projection= null;
			
			IDocument visibleDocument= getVisibleDocument();
			if (visibleDocument instanceof ProjectionDocument)
				projection= (ProjectionDocument) visibleDocument;
			else {
				IDocument master= getDocument();
				IDocument slave= createSlaveDocument(getDocument());
				if (slave instanceof ProjectionDocument) {
					projection= (ProjectionDocument) slave;
					projection.addMasterDocumentRange(0, master.getLength());
					replaceVisibleDocument(projection);
				}
			}
			
			if (projection != null) {
				projection.removeMasterDocumentRange(offset, length);
				// repaint line above
				IDocument document= getDocument();
				int line= document.getLineOfOffset(offset);
				if (line > 0) {
					IRegion info= document.getLineInformation(line - 1);
					invalidateTextPresentation(info.getOffset(), info.getLength());
				}
			}
			
		} catch (BadLocationException x) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Makes all hidden ranges in the given range visible again.
	 *
	 * @param offset the offset of the range
	 * @param length the length of the range
	 */
	public void expand(int offset, int length) {
		try {
			IDocument slave= getVisibleDocument();
			if (slave instanceof ProjectionDocument) {
				ProjectionDocument projection= (ProjectionDocument) slave;
				projection.addMasterDocumentRange(offset, length);
				
				IDocument master= getDocument();
				if (slave.getLength() == master.getLength()) {
					replaceVisibleDocument(master);
					freeSlaveDocument(slave);
				} else {
					invalidateTextPresentation(offset, length);
				}
			}
		} catch (BadLocationException e) {
			throw new IllegalArgumentException();
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#handleVerifyEvent(org.eclipse.swt.events.VerifyEvent)
	 */
	protected void handleVerifyEvent(VerifyEvent e) {
		IRegion modelRange= event2ModelRange(e);
		if (exposeModelRange(modelRange))
			e.doit= false;
	}

	/**
	 * Adds the give column as last column to this viewer's vertical ruler.
	 * 
	 * @param column the column to be added
	 */
	public void addVerticalRulerColumn(IVerticalRulerColumn column) {
		IVerticalRuler ruler= getVerticalRuler();
		if (ruler instanceof CompositeRuler) {
			CompositeRuler compositeRuler= (CompositeRuler) ruler;
			compositeRuler.addDecorator(99, column);
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension5#exposeModelRange(org.eclipse.jface.text.IRegion)
	 */
	public boolean exposeModelRange(IRegion modelRange) {
		IAnnotationModel model= getProjectionAnnotationModel();
		if (model != null) {
			Iterator iterator= model.getAnnotationIterator();
			while (iterator.hasNext()) {
				ProjectionAnnotation annotation= (ProjectionAnnotation) iterator.next();
				if (annotation.isFolded()) {
					Position position= model.getPosition(annotation);
					if (position.overlapsWith(modelRange.getOffset(), modelRange.getLength()) /* || is a delete at the boundary */ ) {
						annotation.run(this);
						return true;
					}
				}
			}	
		}
		return false;
	}
}
