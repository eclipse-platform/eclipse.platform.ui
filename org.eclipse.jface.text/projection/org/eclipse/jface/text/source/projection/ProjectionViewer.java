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


import org.eclipse.core.runtime.NullProgressMonitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentInformationMappingExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
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
	
	private static final int BASE= INFORMATION; // see ISourceViewer.INFORMATION
	
	/** Operation constant for the expand operation. */
	public static final int EXPAND= BASE + 1;
	/** Operation constant for the collapse operation. */
	public static final int COLLAPSE= BASE + 2;
	/** Operation constant for the toggle projection operation. */
	public static final int TOGGLE= BASE + 3;
	/** Operation constant for the expand all operation. */
	public static final int EXPAND_ALL= BASE + 4;
	
	/**
	 * Internal listener to changes of the annotation model.
	 */
	private class AnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			processModelChanged(model, null);
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationModelListenerExtension#modelChanged(org.eclipse.jface.text.source.AnnotationModelEvent)
		 */
		public void modelChanged(AnnotationModelEvent event) {
			processModelChanged(event.getAnnotationModel(), event);
		}

		private void processModelChanged(IAnnotationModel model, AnnotationModelEvent event) {
			if (model == fProjectionAnnotationModel) {
				
				if (fProjectionSummary != null)
					fProjectionSummary.updateSummaries(new NullProgressMonitor());
				processCatchupRequest(event);
				
			} else if (model == getAnnotationModel() && fProjectionSummary != null)
				fProjectionSummary.updateSummaries(new NullProgressMonitor());
		}
	}
	
	/**
	 * Executes the 'replaceVisibleDocument' operation when called the first time. Self-destructs afterwards.
	 */
	private class ReplaceVisibleDocumentExecutor implements IDocumentListener {
		
		private IDocument fSlaveDocument;
		private IDocument fExecutionTrigger;
		
		public ReplaceVisibleDocumentExecutor(IDocument slaveDocument) {
			fSlaveDocument= slaveDocument;
		}

		public void install(IDocument executionTrigger) {
			if (executionTrigger != null && fSlaveDocument != null) {
				fExecutionTrigger= executionTrigger;
				fExecutionTrigger.addDocumentListener(this);
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
			fExecutionTrigger.removeDocumentListener(this);
			executeReplaceVisibleDocument(fSlaveDocument);
		}
	}
	
	/**
	 * Internal listener to find the document onto which to hook the replace-visible-document command.
	 */
	private class DocumentListener implements IDocumentListener {

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			fReplaceVisibleDocumentExecutionTrigger= event.getDocument();
			fRememberedTopIndex= getTopIndex();
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
			fReplaceVisibleDocumentExecutionTrigger= null;
			fRememberedTopIndex= -1;
		}
	}
	
	/** The projection annotation model used by this viewer. */
	private ProjectionAnnotationModel fProjectionAnnotationModel;
	/** The annotation model listener */
	private IAnnotationModelListener fAnnotationModelListener= new AnnotationModelListener();
	/** The projection summary. */
	private ProjectionSummary fProjectionSummary;
	/** Indication that an annotation world change has not yet been processed. */
	private boolean fPendingAnnotationWorldChange= false;
	/** Indication whether projection changes in the visible document should be considered. */
	private boolean fHandleProjectionChanges= true;
	/** The list of projection listeners. */
	private List fProjectionListeners;
	/** Internal lock for protecting the list of pending requests */
	private Object fLock= new Object();
	/** The list of pending requests */
	private List fPendingRequests= new ArrayList();
	/** The replace-visible-document execution trigger */
	private IDocument fReplaceVisibleDocumentExecutionTrigger;
	/** Internal document listener */
	private IDocumentListener fDocumentListener= new DocumentListener();
	/** Remembered top index when the master document changes*/
	private int fRememberedTopIndex= -1;
	/** <code>true</code> if projection was on the last time we switched to segmented mode. */
	private boolean fWasProjectionEnabled;
	
	/**
	 * Creates a new projection source viewer.
	 * 
	 * @param parent the SWT parent control
	 * @param ruler the vertical ruler
	 * @param overviewRuler the overview ruler
	 * @param showsAnnotationOverview <code>true</code> if the overview ruler should be shown 
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
	}
		
	/**
	 * Adds the projection annotation model to the given annotation model.
	 * 
	 * @param model the model to which the projection annotation model is added
	 */
	private void addProjectionAnnotationModel(IAnnotationModel model) {
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			extension.addAnnotationModel(ProjectionSupport.PROJECTION, fProjectionAnnotationModel);
			model.addAnnotationModelListener(fAnnotationModelListener);
		}
	}
	
	/**
	 * Removes the projection annotation model from the given annotation model.
	 * 
	 * @param model the mode from which the projection annotation model is removed
	 */
	private IAnnotationModel removeProjectionAnnotationModel(IAnnotationModel model) {
		if (model instanceof IAnnotationModelExtension) {
			model.removeAnnotationModelListener(fAnnotationModelListener);
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			return extension.removeAnnotationModel(ProjectionSupport.PROJECTION);
		}
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#setDocument(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.source.IAnnotationModel, int, int)
	 */
	public void setDocument(IDocument document, IAnnotationModel annotationModel, int modelRangeOffset, int modelRangeLength) {
		boolean wasProjectionEnabled= false;
		
		if (fProjectionAnnotationModel != null) {
			wasProjectionEnabled= removeProjectionAnnotationModel(getVisualAnnotationModel()) != null;
			fProjectionAnnotationModel= null;
		}
		
		super.setDocument(document, annotationModel, modelRangeOffset, modelRangeLength);
		
		if (wasProjectionEnabled)
			enableProjection();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createVisualAnnotationModel(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	protected IAnnotationModel createVisualAnnotationModel(IAnnotationModel annotationModel) {
		IAnnotationModel model= super.createVisualAnnotationModel(annotationModel);
		fProjectionAnnotationModel= new ProjectionAnnotationModel();
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
			return (ProjectionAnnotationModel) extension.getAnnotationModel(ProjectionSupport.PROJECTION);
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
			ProjectionDocument projection= (ProjectionDocument) slaveDocument;
			
			int offset= modelRangeOffset;
			int length= modelRangeLength;
			
			if (!isProjectionMode()) {
				// mimic original TextViewer behavior
				IDocument master= projection.getMasterDocument();
				int line= master.getLineOfOffset(modelRangeOffset);
				offset= master.getLineOffset(line);
				length= (modelRangeOffset - offset) + modelRangeLength;

			}
			
			try {
				fHandleProjectionChanges= false;
				projection.replaceMasterDocumentRanges(offset, length);
			} finally {
				fHandleProjectionChanges= true;
			}
			return true;
		}
		return false;
	}
	
	public void addProjectionListener(IProjectionListener listener) {

		Assert.isNotNull(listener);

		if (fProjectionListeners == null)
			fProjectionListeners= new ArrayList();
	
		if (!fProjectionListeners.contains(listener))
			fProjectionListeners.add(listener);
	}
	
	public void removeProjectionListener(IProjectionListener listener) {

		Assert.isNotNull(listener);

		if (fProjectionListeners != null) {
			fProjectionListeners.remove(listener);
			if (fProjectionListeners.size() == 0)
				fProjectionListeners= null;
		}
	}
	
	protected void fireProjectionEnabled() {
		if (fProjectionListeners != null) {
			Iterator e= new ArrayList(fProjectionListeners).iterator();
			while (e.hasNext()) {
				IProjectionListener l= (IProjectionListener) e.next();
				l.projectionEnabled();
			}
		}
	}

	protected void fireProjectionDisabled() {
		if (fProjectionListeners != null) {
			Iterator e= new ArrayList(fProjectionListeners).iterator();
			while (e.hasNext()) {
				IProjectionListener l= (IProjectionListener) e.next();
				l.projectionDisabled();
			}
		}
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
			fFindReplaceDocumentAdapter= null;
			fireProjectionDisabled();
		}
	}
	
	/**
	 * Enables the projection mode.
	 */
	private void enableProjection() {
		if (!isProjectionMode()) {
			addProjectionAnnotationModel(getVisualAnnotationModel());
			fFindReplaceDocumentAdapter= null;
			fireProjectionEnabled();
		}
	}
	
	private void expandAll() {
		int offset= 0;
		IDocument doc= getDocument();
		int length= doc == null ? 0 : doc.getLength();
		if (isProjectionMode()) {
			fProjectionAnnotationModel.expandAll(offset, length);
		}
	}
	
	private void expand() {
		if (isProjectionMode()) {
			Position found= null;
			Annotation bestMatch= null;
			Point selection= getSelectedRange();
			for (Iterator e= fProjectionAnnotationModel.getAnnotationIterator(); e.hasNext();) {
				ProjectionAnnotation annotation= (ProjectionAnnotation) e.next();
				if (annotation.isCollapsed()) {
					Position position= fProjectionAnnotationModel.getPosition(annotation);
					// take the first most fine grained match
					if (position != null && touches(selection, position))
						if (found == null || position.includes(found.offset) && position.includes(found.offset + found.length)) {
							found= position;
							bestMatch= annotation;
						}
				}
			}
			
			if (bestMatch != null)
				fProjectionAnnotationModel.expand(bestMatch);
		}
	}
	
	private boolean touches(Point selection, Position position) {
		return position.overlapsWith(selection.x, selection.y) || selection.y == 0 && position.offset + position.length == selection.x + selection.y;
	}

	private void collapse() {
		if (isProjectionMode()) {
			Position found= null;
			Annotation bestMatch= null;
			Point selection= getSelectedRange();
			for (Iterator e= fProjectionAnnotationModel.getAnnotationIterator(); e.hasNext();) {
				ProjectionAnnotation annotation= (ProjectionAnnotation) e.next();
				if (!annotation.isCollapsed()) {
					Position position= fProjectionAnnotationModel.getPosition(annotation);
					// take the first most fine grained match
					if (position != null && touches(selection, position))
						if (found == null || found.includes(position.offset) && found.includes(position.offset + position.length)) {
							found= position;
							bestMatch= annotation;
						}
				}
			}
			
			if (bestMatch != null)
				fProjectionAnnotationModel.collapse(bestMatch);
		}
	}
	
	/**
	 * Remembers whether to listen to projection changes of the visible
	 * document.
	 * 
	 * @see ProjectionDocument#addMasterDocumentRange(int, int)
	 */
	private void addMasterDocumentRange(ProjectionDocument projection, int offset, int length) throws BadLocationException {
		try {
			fHandleProjectionChanges= false;
			projection.addMasterDocumentRange(offset, length);
		} finally {
			fHandleProjectionChanges= true;
		}
	}
	
	/**
	 * Remembers whether to listen to projection changes of the visible
	 * document.
	 * 
	 * @see ProjectionDocument#removeMasterDocumentRange(int, int)
	 */
	private void removeMasterDocumentRange(ProjectionDocument projection, int offset, int length) throws BadLocationException {
		try {
			fHandleProjectionChanges= false;
			projection.removeMasterDocumentRange(offset, length);
		} finally {
			fHandleProjectionChanges= true;
		}
	}

	/*
	 * @see org.eclipse.jface.text.TextViewer#setVisibleRegion(int, int)
	 */
	public void setVisibleRegion(int start, int length) {
		if (!isSegmented())
			fWasProjectionEnabled= isProjectionMode();
		disableProjection();
		super.setVisibleRegion(start, length);
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#resetVisibleRegion()
	 */
	public void resetVisibleRegion() {
		super.resetVisibleRegion();
		if (fWasProjectionEnabled)
			enableProjection();
	}
	
	/*
	 * @see org.eclipse.jface.text.ITextViewer#getVisibleRegion()
	 */
	public IRegion getVisibleRegion() {
		disableProjection();
		return getModelCoverage();
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewer#overlapsWithVisibleRegion(int,int)
	 */
	public boolean overlapsWithVisibleRegion(int offset, int length) {
		disableProjection();
		IRegion coverage= getModelCoverage();
		if (coverage == null)
			return false;
		
		boolean appending= (offset == coverage.getOffset() + coverage.getLength()) && length == 0;
		return appending || TextUtilities.overlaps(coverage, new Region(offset, length));
	}
	
	/**
	 * Replace the visible document with the given document. Maintains the
	 * scroll offset and the selection.
	 * 
	 * @param visibleDocument the visible document
	 */
	private void replaceVisibleDocument(IDocument slave) {
		if (fReplaceVisibleDocumentExecutionTrigger != null) {
			ReplaceVisibleDocumentExecutor executor= new ReplaceVisibleDocumentExecutor(slave);
			executor.install(fReplaceVisibleDocumentExecutionTrigger);
		} else
			executeReplaceVisibleDocument(slave);
	}
	
	
	private void executeReplaceVisibleDocument(IDocument visibleDocument) {
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
		
		StyledText textWidget= getTextWidget();
		try {
			
			if (textWidget != null && !textWidget.isDisposed())
				textWidget.setRedraw(false);
			
			IDocument visibleDocument= getVisibleDocument();
			if (visibleDocument instanceof ProjectionDocument)
				projection= (ProjectionDocument) visibleDocument;
			else {
				IDocument master= getDocument();
				IDocument slave= createSlaveDocument(getDocument());
				if (slave instanceof ProjectionDocument) {
					projection= (ProjectionDocument) slave;
					addMasterDocumentRange(projection, 0, master.getLength());
					replaceVisibleDocument(projection);
				}
			}
			
			if (projection != null)
				removeMasterDocumentRange(projection, offset, length);
				
		} finally {
			if (textWidget != null && !textWidget.isDisposed()) {
				if (fRememberedTopIndex != -1)
					setTopIndex(fRememberedTopIndex);
				textWidget.setRedraw(true);
			}
		}
		
		if (projection != null && fireRedraw) {
			// repaint line above
			IDocument document= getDocument();
			int line= document.getLineOfOffset(offset);
			if (line > 0) {
				IRegion info= document.getLineInformation(line - 1);
				invalidateTextPresentation(info.getOffset(), info.getLength());
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
				addMasterDocumentRange(projection, expanded.getOffset(), expanded.getLength());
				
				// collapse contained regions
				if (collapsed != null) {
					for (int i= 0; i < collapsed.length; i++) {
						IRegion p= computeCollapsedRegion(collapsed[i]);
						removeMasterDocumentRange(projection, p.getOffset(), p.getLength());
					}
				}
			
			} finally {
				if (textWidget != null && !textWidget.isDisposed()) {
					if (fRememberedTopIndex != -1)
						setTopIndex(fRememberedTopIndex);
					textWidget.setRedraw(true);
				}
			}
			
			IDocument master= getDocument();
			if (slave.getLength() == master.getLength()) {
				replaceVisibleDocument(master);
			} else if (fireRedraw){
				invalidateTextPresentation(expanded.getOffset(), expanded.getLength());
			}
		}
	}
		
	/**
	 * Processes the request for catch up with the annotation model in the UI thread. If the current
	 * thread is not the UI thread or there are pending catch up requests, a new request is posted.
	 * 
	 * @param event the annotation model event
	 */
	protected final void processCatchupRequest(AnnotationModelEvent event) {
		if (Display.getCurrent() != null) {
			boolean run= false;
			synchronized (fLock) {
				run= fPendingRequests.isEmpty();
			}
			if (run) {
				
				try {
					catchupWithProjectionAnnotationModel(event);
				} catch (BadLocationException x) {
					throw new IllegalArgumentException();
				}
				
			} else
				postCatchupRequest(event);
		} else {
			postCatchupRequest(event);
		}
	}
	
	/**
	 * Posts the request for catch up with the annotation model into the UI thread.
	 * 
	 * @param event the annotation model event
	 */
	protected final void postCatchupRequest(final AnnotationModelEvent event) {
		synchronized (fLock) {
			fPendingRequests.add(event);
			if (fPendingRequests.size() == 1) {
				StyledText widget= getTextWidget();
				if (widget != null) {
					Display display= widget.getDisplay();
					if (display != null) {
						display.asyncExec(new Runnable() {
							public void run() {
								Iterator e= fPendingRequests.iterator();
								try {
									while (true) {
										AnnotationModelEvent ame= null;
										synchronized (fLock) {
											if (e.hasNext()) {
												ame= (AnnotationModelEvent) e.next();
											} else {
												fPendingRequests.clear();
												return;
											}
										}
										catchupWithProjectionAnnotationModel(ame);
									}
								} catch (BadLocationException x) {
									try {
										catchupWithProjectionAnnotationModel(null);
									} catch (BadLocationException x1) {
										throw new IllegalArgumentException();
									} finally {
										synchronized (fLock) {
											fPendingRequests.clear();
										}
									}
								}
							}
						});
					}
				}
			}
		}
	}

	/**
	 * Adapts the slave visual document of this viewer to the changes described
	 * in the annotation model event. When the event is <code>null</code>,
	 * this is identical to a world change event.
	 * 
	 * @param event the annotation model event or <code>null</code>
	 * @exception BadLocationException in case the annotation model event is no longer in sync with the document
	 */
	private void catchupWithProjectionAnnotationModel(AnnotationModelEvent event) throws BadLocationException {
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
				
				Annotation[] addedAnnotations= event.getAddedAnnotations();
				Annotation[] changedAnnotation= event.getChangedAnnotations();
				Annotation[] removedAnnotations= event.getRemovedAnnotations();
				
				boolean fireRedraw= true;
				processDeletions(event, removedAnnotations, fireRedraw);
				List coverage= new ArrayList();
				processChanges(addedAnnotations, fireRedraw, coverage);
				processChanges(changedAnnotation, fireRedraw, coverage);
				
				if (!fireRedraw) {
					//TODO compute minimal scope for invalidation
					invalidateTextPresentation();
				}
			}
			
		}
	}
	
	private boolean covers(Position expanded, Position position) {
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
				if (position == null) {
					// annotation might already be deleted, we will be informed later on about this deletion
					continue;
				}
				if (covers(expanded, position))
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
	
	/*
	 * We pass the removed annotation into this method for performance reasons only. Otherwise, they could be fetch from the event. 
	 */
	private void processDeletions(AnnotationModelEvent event, Annotation[] removedAnnotations, boolean fireRedraw) throws BadLocationException {
		for (int i= 0; i < removedAnnotations.length; i++) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) removedAnnotations[i];
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
	
	private void processChanges(Annotation[] annotations, boolean fireRedraw, List coverage) throws BadLocationException {
		for (int i= 0; i < annotations.length; i++) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) annotations[i];
			Position position= fProjectionAnnotationModel.getPosition(annotation);
			
			if (position == null)
				continue;
			
			if (annotation.isCollapsed()) {
				if (!covers(coverage, position)) {
					coverage.add(position);
					IRegion region= computeCollapsedRegion(position);
					if (region != null)
						collapse(region.getOffset(), region.getLength(), fireRedraw);
				}
			} else {
				if (!covers(coverage, position)) {
					Position[] collapsed= computeCollapsedRanges(position);
					expand(position, collapsed, false);
					if (fireRedraw)
						invalidateTextPresentation(position.getOffset(), position.getLength());
				}
			}
		}
	}

	private boolean covers(List coverage, Position position) {
		Iterator e= coverage.iterator();
		while (e.hasNext()) {
			Position p= (Position) e.next();
			if (p.getOffset() <= position.getOffset() && position.getOffset() + position.getLength() <= p.getOffset() + p.getLength())
				return true;
		}
		return false;
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
					addMasterDocumentRange(projection, 0, master.getLength());
				}
			}
		}
		
		if (projection != null) {
			Iterator e= fProjectionAnnotationModel.getAnnotationIterator();
			while (e.hasNext()) {
				ProjectionAnnotation annotation= (ProjectionAnnotation) e.next();
				if (annotation.isCollapsed()) {
					Position position= fProjectionAnnotationModel.getPosition(annotation);
					if (position != null) {
						IRegion region= computeCollapsedRegion(position);
						if (region != null)
							removeMasterDocumentRange(projection, region.getOffset(), region.getLength());
					}
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

	/**
	 * Removes the give column from this viewer's vertical ruler.
	 * 
	 * @param column the column to be removed
	 */
	public void removeVerticalRulerColumn(IVerticalRulerColumn column) {
		IVerticalRuler ruler= getVerticalRuler();
		if (ruler instanceof CompositeRuler) {
			CompositeRuler compositeRuler= (CompositeRuler) ruler;
			compositeRuler.removeDecorator(column);
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension5#exposeModelRange(org.eclipse.jface.text.IRegion)
	 */
	public boolean exposeModelRange(IRegion modelRange) {
		if (isProjectionMode())
			return fProjectionAnnotationModel.expandAll(modelRange.getOffset(), modelRange.getLength());

		if (!overlapsWithVisibleRegion(modelRange.getOffset(), modelRange.getLength())) {
			resetVisibleRegion();
			return true;
		}
		
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#setRangeIndication(int, int, boolean)
	 */
	public void setRangeIndication(int offset, int length, boolean moveCursor) {
		
		List expand= new ArrayList(2);
		if (moveCursor) {
			
			// expand the immediate effected collapsed regions
			Iterator iterator= fProjectionAnnotationModel.getAnnotationIterator();
			while (iterator.hasNext()) {
				ProjectionAnnotation annotation= (ProjectionAnnotation) iterator.next();
				if (annotation.isCollapsed() && willAutoExpand(fProjectionAnnotationModel.getPosition(annotation), offset, length))
					expand.add(annotation);
			}
			
			if (!expand.isEmpty()) {
				Iterator e= expand.iterator();
				while (e.hasNext())
					fProjectionAnnotationModel.expand((Annotation) e.next());
			}
		}
		
		super.setRangeIndication(offset, length, moveCursor);
	}
	
	private boolean willAutoExpand(Position position, int offset, int length) {
		if (position == null || position.isDeleted())
			return false;
		// right or left boundary
		if (position.getOffset() == offset || position.getOffset() + position.getLength() == offset + length)
			return true;
		// completely embedded in given position
		if (position.getOffset() < offset && offset + length < position.getOffset() + position.getLength())
			return true;
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	protected void inputChanged(Object newInput, Object oldInput) {
		if (oldInput instanceof IDocument) {
			IDocument previous= (IDocument) oldInput;
			previous.removeDocumentListener(fDocumentListener);
		}
		
		super.inputChanged(newInput, oldInput);
		
		if (newInput instanceof IDocument) {
			IDocument current= (IDocument) newInput;
			current.addDocumentListener(fDocumentListener);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#handleVisibleDocumentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	protected void handleVisibleDocumentChanged(DocumentEvent event) {
		if (fHandleProjectionChanges && event instanceof ProjectionDocumentEvent && isProjectionMode()) {
			ProjectionDocumentEvent e= (ProjectionDocumentEvent) event;
			if (ProjectionDocumentEvent.PROJECTION_CHANGE == e.getChangeType() && e.getLength() == 0 && e.getText().length() != 0)
				fProjectionAnnotationModel.expandAll(e.getMasterOffset(), e.getMasterLength());
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension5#getCoveredModelRanges(org.eclipse.jface.text.IRegion)
	 */
	public IRegion[] getCoveredModelRanges(IRegion modelRange) {
		if (fInformationMapping == null)
			return new IRegion[] { new Region(modelRange.getOffset(), modelRange.getLength()) };
			
		if (fInformationMapping instanceof IDocumentInformationMappingExtension) {
			IDocumentInformationMappingExtension extension= (IDocumentInformationMappingExtension) fInformationMapping;
			try {
				return extension.getExactCoverage(modelRange);
			} catch (BadLocationException x) {
			}
		}
		
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
	 */
	public void doOperation(int operation) {
		switch (operation) {
			case TOGGLE:
				if (canDoOperation(TOGGLE)) {
					if (!isProjectionMode()) {
						enableProjection();
					} else {
						expandAll();
						disableProjection();
					}
					return;
				}
		}
		
		if (!isProjectionMode()) {
			super.doOperation(operation);
			return;
		}
		
		StyledText textWidget= getTextWidget();
		if (textWidget == null)
			return;

		Point selection= null;
		switch (operation) {

			case CUT:
				
				if (redraws()) {
					selection= getSelectedRange();
					if (selection.y == 0)
						copyMarkedRegion(true);
					else
						copyToClipboard(selection.x, selection.y, true, textWidget);
					
					selection= textWidget.getSelectionRange();
					fireSelectionChanged(selection.x, selection.y);
				}
				break;
				
			case COPY:
				
				if (redraws()) {
					selection= getSelectedRange();
					if (selection.y == 0)
						copyMarkedRegion(false);
					else
						copyToClipboard(selection.x, selection.y, false, textWidget);
				}
				break;
			
			case EXPAND_ALL:
				if (redraws())
					expandAll();
				break;
				
			case EXPAND:
				if (redraws()) {
					expand();
				}
				break;
			
			case COLLAPSE:
				if (redraws()) {
					collapse();
				}
				break;
			
			default:
				super.doOperation(operation);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#canDoOperation(int)
	 */
	public boolean canDoOperation(int operation) {
		
		switch (operation) {
			case COLLAPSE:
			case EXPAND:
			case EXPAND_ALL:
				return isProjectionMode();
			case TOGGLE:
				return !isSegmented();
		}
		
		return super.canDoOperation(operation);
	}
	
	private boolean isSegmented() {
		IDocument document= getDocument();
		int length= document == null ? 0 : document.getLength();
		IRegion visible= getModelCoverage();
		boolean isSegmented= visible != null && !visible.equals(new Region(0, length));
		return isSegmented;
	}

	/*
	 * @see org.eclipse.jface.text.TextViewer#copyMarkedRegion(boolean)
	 */
	protected void copyMarkedRegion(boolean delete) {
		
		StyledText textWidget= getTextWidget();
		
		if (textWidget == null)
			return;
		
		if (fMarkPosition == null || fMarkPosition.isDeleted())
			return;
		
		int start= fMarkPosition.getOffset();
		int end= getSelectedRange().x;
		
		if (start > end) {
			start= start + end;
			end= start - end;
			start= start - end;
		}
		
		copyToClipboard(start, end - start, delete, textWidget);		
	}
	
	private void copyToClipboard(int offset, int length, boolean delete, StyledText textWidget) {
		
		IDocument document= getDocument();
		Clipboard clipboard= new Clipboard(textWidget.getDisplay());
		
		try {
			
			Transfer[] dataTypes= new Transfer[] { TextTransfer.getInstance() };
			Object[] data= new Object[] { document.get(offset, length) };
			clipboard.setContents(data, dataTypes);
				
			if (delete) {
				int widgetCaret= modelOffset2WidgetOffset(offset);
				textWidget.setSelection(widgetCaret);
				document.replace(offset, length, null);	
			}
			
		} catch (BadLocationException x) {
		} finally {
			clipboard.dispose();			
		}
	}
	
	/**
	 * Adapts the behavior to line based folding.
	 */
	protected Point widgetSelection2ModelSelection(Point widgetSelection) {
		
		if (!isProjectionMode())
			return super.widgetSelection2ModelSelection(widgetSelection);
		
		IRegion modelSelection= widgetRange2ModelRange(new Region(widgetSelection.x, widgetSelection.y));
		if (modelSelection == null)
			return null;
		
		int modelOffset= modelSelection.getOffset();
		int modelLength= modelSelection.getLength();
		int widgetSelectionExclusiveEnd= widgetSelection.x + widgetSelection.y;
		int modelExclusiveEnd= widgetOffset2ModelOffset(widgetSelectionExclusiveEnd);
		
		if (modelOffset + modelLength < modelExclusiveEnd)
			return new Point(modelOffset, modelExclusiveEnd - modelOffset);
		
		if (widgetSelectionExclusiveEnd == getVisibleDocument().getLength())
			return new Point(modelOffset, getDocument().getLength() - modelOffset);
		
		return new Point(modelOffset, modelLength);
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#getFindReplaceDocumentAdapter()
	 */
	protected FindReplaceDocumentAdapter getFindReplaceDocumentAdapter() {
		if (fFindReplaceDocumentAdapter == null) {
			IDocument document= isProjectionMode() ? getDocument() : getVisibleDocument();
			fFindReplaceDocumentAdapter= new FindReplaceDocumentAdapter(document);
		}
		return fFindReplaceDocumentAdapter;
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#findAndSelect(int, java.lang.String, boolean, boolean, boolean, boolean)
	 */
	protected int findAndSelect(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		
		if (!isProjectionMode())
			return super.findAndSelect(startPosition, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
		
		StyledText textWidget= getTextWidget();
		if (textWidget == null)
			return -1;
			
		try {
			
			IRegion matchRegion= getFindReplaceDocumentAdapter().find(startPosition, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
			if (matchRegion != null) {
				exposeModelRange(matchRegion);
				setSelectedRange(matchRegion.getOffset(), matchRegion.getLength());
				textWidget.showSelection();
				return matchRegion.getOffset();
			}
			
		} catch (BadLocationException x) {
		}
		
		return -1;
	}
	
	/*
	 * @see org.eclipse.jface.text.TextViewer#findAndSelectInRange(int, java.lang.String, boolean, boolean, boolean, int, int, boolean)
	 */
	protected int findAndSelectInRange(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, int rangeOffset, int rangeLength, boolean regExSearch) {
		
		if (!isProjectionMode())
			return super.findAndSelect(startPosition, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
		
		StyledText textWidget= getTextWidget();
		if (textWidget == null)
			return -1;
			
		try {
			
			int modelOffset= startPosition;
			if (forwardSearch && (startPosition == -1 || startPosition < rangeOffset)) {
				modelOffset= rangeOffset;
			} else if (!forwardSearch && (startPosition == -1 || startPosition > rangeOffset + rangeLength)) {
				modelOffset= rangeOffset + rangeLength;
			}
			
			IRegion matchRegion= getFindReplaceDocumentAdapter().find(modelOffset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
			if (matchRegion != null) {
				int offset= matchRegion.getOffset();
				int length= matchRegion.getLength();
				if (rangeOffset <= offset && offset + length <= rangeOffset + rangeLength) {
					exposeModelRange(matchRegion);
					setSelectedRange(offset, length);
					textWidget.showSelection();
					return offset;
				}
			}
			
		} catch (BadLocationException x) {
		}
		
		return -1;
	}	
}
