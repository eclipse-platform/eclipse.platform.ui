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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.projection.ProjectionDocument;
import org.eclipse.jface.text.projection.ProjectionDocumentEvent;
import org.eclipse.jface.text.projection.ProjectionDocumentManager;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
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
	 * Internal listener to changes of the projection annotation model.
	 */
	private class ProjectionAnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			postCatchupRequest(null);
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListenerExtension#modelChanged(org.eclipse.jface.text.source.AnnotationModelEvent)
		 */
		public void modelChanged(AnnotationModelEvent event) {
			postCatchupRequest(event);
		}
	}
	
	/**
	 * Key of the projection annotation model inside the visual annotation model.
	 */
	protected final static Object PROJECTION_ANNOTATION_MODEL= new Object();
		
	
	/** The projection annotation model used by this viewer. */
	private ProjectionAnnotationModel fProjectionAnnotationModel;
	/** The projection annotation model listener */
	private IAnnotationModelListener fProjectionAnnotationModelListener= new ProjectionAnnotationModelListener();
	/** The projection summary. */
	private ProjectionSummary fProjectionSummary;
	/** Indication that an annotation world change has not yet been processed. */
	private boolean fPendingAnnotationWorldChange= false;
	
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
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createLayout()
	 */
	protected Layout createLayout() {
		return new RulerLayout(1);
	}
	
	/**
	 * Sets the projection summary for this viewer.
	 * 
	 * @param projectionSummary the projection summary.
	 */
	public void setProjectionSummary(ProjectionSummary projectionSummary) {
		fProjectionSummary= projectionSummary;
		ProjectionAnnotationModel model= getProjectionAnnotationModel();
		if (model != null)
			model.setProjectionSummary(fProjectionSummary);
	}
		
	/**
	 * Adds the projection annotation model to the given annotation model.
	 * 
	 * @param model the model to which the projection annotation model is added
	 */
	private void addProjectionAnnotationModel(IAnnotationModel model) {
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			extension.addAnnotationModel(PROJECTION_ANNOTATION_MODEL, fProjectionAnnotationModel);
			fProjectionAnnotationModel.addAnnotationModelListener(fProjectionAnnotationModelListener);
		}
	}
	
	/**
	 * Removes the projection annotation model from the given annotation model.
	 * 
	 * @param model the mode from which the projection annotation model is removed
	 */
	private void removeProjectionAnnotationModel(IAnnotationModel model) {
		if (model instanceof IAnnotationModelExtension) {
			fProjectionAnnotationModel.removeAnnotationModelListener(fProjectionAnnotationModelListener);
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			extension.removeAnnotationModel(PROJECTION_ANNOTATION_MODEL);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createVisualAnnotationModel(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	protected IAnnotationModel createVisualAnnotationModel(IAnnotationModel annotationModel) {
		IAnnotationModel model= super.createVisualAnnotationModel(annotationModel);
		fProjectionAnnotationModel= new ProjectionAnnotationModel(fProjectionSummary);
		addProjectionAnnotationModel(model);
		return model;
	}

	/**
	 * Returns the projection annotation model.
	 * 
	 * @return the projection annotation model
	 */
	public ProjectionAnnotationModel getProjectionAnnotationModel() {
		IAnnotationModel model= getVisualAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			return (ProjectionAnnotationModel) extension.getAnnotationModel(PROJECTION_ANNOTATION_MODEL);
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
			removeProjectionAnnotationModel(getVisualAnnotationModel());
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
			if (textWidget != null && !textWidget.isDisposed())
				textWidget.setRedraw(false);
			
			int topIndex= getTopIndex();
			Point selection= getSelectedRange();
			setVisibleDocument(visibleDocument);
			setSelectedRange(selection.x, selection.y);
			setTopIndex(topIndex);

		} finally {
			if (textWidget != null && !textWidget.isDisposed())
				textWidget.setRedraw(true);
		}
	}
		
	/**
	 * Hides the given range by collapsing it. If requested, a redraw request is issued.
	 * 
	 * @param offset the offset of the range to hide
	 * @param length the length of the range to hide
	 * @param fireRedraw <code>true</code> if a redraw request should be issued, <code>false</code> otherwise
	 * @throws BadLocationException in case the range is invalid
	 */
	private void collapse(int offset, int length, boolean fireRedraw) throws BadLocationException {
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
			if (fireRedraw) {
				// repaint line above
				IDocument document= getDocument();
				int line= document.getLineOfOffset(offset);
				if (line > 0) {
					IRegion info= document.getLineInformation(line - 1);
					invalidateTextPresentation(info.getOffset(), info.getLength());
				}
			}
		}
	}
	
	/**
	 * Makes the given range visible again while keeping the given collapsed
	 * ranges. If requested, a redraw request is issued.
	 * 
	 * @param expanded the range to be expanded
	 * @param collapsed a sequence of collapsed ranges completely contained by
	 *            the expanded range
	 * @param fireRedraw <code>true</code> if a redraw request should be
	 *            issued, <code>false</code> otherwise
	 * @throws BadLocationException in case the range is invalid
	 */
	private void expand(Position expanded, Position[] collapsed, boolean fireRedraw) throws BadLocationException {
		IDocument slave= getVisibleDocument();
		if (slave instanceof ProjectionDocument) {
			ProjectionDocument projection= (ProjectionDocument) slave;
			
			StyledText textWidget= getTextWidget();
			try {
				
				if (textWidget != null && !textWidget.isDisposed())
					textWidget.setRedraw(false);
				
				// expand
				projection.addMasterDocumentRange(expanded.getOffset(), expanded.getLength());
				
				// collapse contained regions
				if (collapsed != null) {
					for (int i= 0; i < collapsed.length; i++) {
						IRegion p= computeCollapsedRegion(collapsed[i]);
						projection.removeMasterDocumentRange(p.getOffset(), p.getLength());
					}
				}
			
			} finally {
				if (textWidget != null && !textWidget.isDisposed())
					textWidget.setRedraw(true);
			}
			
			
			
			IDocument master= getDocument();
			if (slave.getLength() == master.getLength()) {
				replaceVisibleDocument(master);
				freeSlaveDocument(slave);
			} else if (fireRedraw){
				invalidateTextPresentation(expanded.getOffset(), expanded.getLength());
			}
		}
	}
	
	/**
	 * Posts the request for catch up with the annotation model into the UI thread.
	 * 
	 * @param event the annotation model event
	 */
	protected final void postCatchupRequest(final AnnotationModelEvent event) {
		StyledText widget= getTextWidget();
		if (widget != null) {
			Display display= widget.getDisplay();
			if (display != null) {
				// check for dead locks
				display.syncExec(new Runnable() {
					public void run() {
						catchupWithProjectionAnnotationModel(event);
					}
				});
			}	

		}
	}

	/**
	 * Adapts the slave visual document of this viewer to the changes described
	 * in the annotation model event. When the event is <code>null</code>,
	 * this is identical to a world change event.
	 * 
	 * @param event the annotation model event or <code>null</code>
	 */
	private void catchupWithProjectionAnnotationModel(AnnotationModelEvent event) {
		try {
			if (event == null) {
				
				fPendingAnnotationWorldChange= false;
				reinitializeProjection();
				
			} else if (event.isWorldChange()) {
				
				if (event.isValid()) {
					fPendingAnnotationWorldChange= false;
					reinitializeProjection();
				} else
					fPendingAnnotationWorldChange= true;
				
			} else {
				
				if (fPendingAnnotationWorldChange) {
					if (event.isValid()) {
						fPendingAnnotationWorldChange= false;
						reinitializeProjection();
					}
				} else {
					
					boolean fireRedraw= true;
					
					processDeletions(event, fireRedraw);
					processAdditions(event, fireRedraw);
					processModifications(event, fireRedraw);
					
					if (!fireRedraw) {
						//TODO compute minimal scope for invalidation
						invalidateTextPresentation();
					}
				}
				
			}
		} catch (BadLocationException e) {
			throw new IllegalArgumentException();
		}
	}
	
	private boolean includes(Position expanded, Position position) {
		if (!expanded.equals(position) && !position.isDeleted())
			return expanded.getOffset() <= position.getOffset() &&  position.getOffset() + position.getLength() <= expanded.getOffset() + expanded.getLength();
		return false;
	}
	
	private Position[] computeCollapsedRanges(Position expanded) {
		List positions= new ArrayList(5);
		Iterator e= fProjectionAnnotationModel.getAnnotationIterator();
		while (e.hasNext()) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) e.next();
			if (annotation.isCollapsed()) {
				Position position= fProjectionAnnotationModel.getPosition(annotation);
				if (includes(expanded, position))
					positions.add(position);
			}
		}
		
		if (positions.size() > 0) {
			Position[] result= new Position[positions.size()];
			positions.toArray(result);
			return result;
		}
		
		return null;
	}

	private void processDeletions(AnnotationModelEvent event, boolean fireRedraw) throws BadLocationException {
		Annotation[] annotations= event.getRemovedAnnotations();
		for (int i= 0; i < annotations.length; i++) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) annotations[i];
			if (annotation.isCollapsed()) {
				Position expanded= event.getPositionOfRemovedAnnotation(annotation);
				Position[] collapsed= computeCollapsedRanges(expanded);
				expand(expanded, collapsed, false);
				if (fireRedraw)
					invalidateTextPresentation(expanded.getOffset(), expanded.getLength());
			}
		}
	}

	public IRegion computeCollapsedRegion(Position position) {
		try {
			IDocument document= getDocument();
			int line= document.getLineOfOffset(position.getOffset());
			int offset= document.getLineOffset(line + 1);
			
			int length= position.getLength() - (offset - position.getOffset());
			if (length > 0)
				return new Region(offset, length);
		} catch (BadLocationException x) {
		}
		
		return null;
	}
	
	public Position computeCollapsedRegionAnchor(Position position) {
		try {
			IDocument document= getDocument();
			IRegion lineInfo= document.getLineInformationOfOffset(position.getOffset());
			return new Position(lineInfo.getOffset() + lineInfo.getLength(), 0);
		} catch (BadLocationException x) {
		}		
		return null;
	}
	
	private void processAdditions(AnnotationModelEvent event, boolean fireRedraw) throws BadLocationException {
		Annotation[] annotations= event.getAddedAnnotations();
		for (int i= 0; i < annotations.length; i++) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) annotations[i];
			if (annotation.isCollapsed()) {
				Position position= fProjectionAnnotationModel.getPosition(annotation);
				IRegion region= computeCollapsedRegion(position);
				if (region != null)
					collapse(region.getOffset(), region.getLength(), fireRedraw);
			}
		}
	}
	
	private void processModifications(AnnotationModelEvent event, boolean fireRedraw) throws BadLocationException {
		Annotation[] annotations= event.getChangedAnnotations();
		for (int i= 0; i < annotations.length; i++) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) annotations[i];
			Position position= fProjectionAnnotationModel.getPosition(annotation);
			if (annotation.isCollapsed()) {
				IRegion region= computeCollapsedRegion(position);
				if (region != null)
					collapse(region.getOffset(), region.getLength(), fireRedraw);
			} else {
				Position[] collapsed= computeCollapsedRanges(position);
				expand(position, collapsed, false);
				if (fireRedraw)
					invalidateTextPresentation(position.getOffset(), position.getLength());
			}
		}
	}

	private void reinitializeProjection() throws BadLocationException {
		
		ProjectionDocument projection= null;
		
		ISlaveDocumentManager manager= getSlaveDocumentManager();
		if (manager != null) {
			IDocument master= getDocument();
			if (master != null) {
				IDocument slave= manager.createSlaveDocument(master);
				if (slave instanceof ProjectionDocument) {
					projection= (ProjectionDocument) slave;
					projection.addMasterDocumentRange(0, master.getLength());
				}
			}
		}
		
		if (projection != null) {
			Iterator e= fProjectionAnnotationModel.getAnnotationIterator();
			while (e.hasNext()) {
				ProjectionAnnotation annotation= (ProjectionAnnotation) e.next();
				if (annotation.isCollapsed()) {
					Position position= fProjectionAnnotationModel.getPosition(annotation);
					IRegion region= computeCollapsedRegion(position);
					if (region != null)
						projection.removeMasterDocumentRange(region.getOffset(), region.getLength());
				}
			}
			
		}
		
		replaceVisibleDocument(projection);
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#handleVerifyEvent(org.eclipse.swt.events.VerifyEvent)
	 */
	protected void handleVerifyEvent(VerifyEvent e) {
		IRegion modelRange= event2ModelRange(e);
		if (exposeModelRange(modelRange))
			e.doit= false;
		else
			super.handleVerifyEvent(e);
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
		if (isProjectionMode())
			return fProjectionAnnotationModel.expandAll(modelRange.getOffset(), modelRange.getLength());
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#setRangeIndication(int, int, boolean)
	 */
	public void setRangeIndication(int start, int length, boolean moveCursor) {
		// TODO experimental code
		if (moveCursor)
			exposeModelRange(new Region(start, length));
		super.setRangeIndication(start, length, moveCursor);
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#handleVisibleDocumentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	protected void handleVisibleDocumentChanged(DocumentEvent event) {
		if (isProjectionMode() && event instanceof ProjectionDocumentEvent) {
			ProjectionDocumentEvent e= (ProjectionDocumentEvent) event;
			if (ProjectionDocumentEvent.PROJECTION_CHANGE == e.getChangeType() && e.getLength() == 0 && e.getText().length() != 0)
				fProjectionAnnotationModel.expandAll(e.getMasterOffset(), e.getMasterLength());
		}
	}
}
