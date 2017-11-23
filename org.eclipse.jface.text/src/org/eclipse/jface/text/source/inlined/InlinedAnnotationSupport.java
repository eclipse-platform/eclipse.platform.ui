/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide inline annotations support - Bug 527675
 */
package org.eclipse.jface.text.source.inlined;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextLineSpacingProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Support to draw inlined annotations:
 *
 * <ul>
 * <li>line header annotation with {@link LineHeaderAnnotation}.</li>
 * <li>line content annotation with {@link LineContentAnnotation}.</li>
 * </ul>
 *
 * @since 3.13.0
 */
public class InlinedAnnotationSupport implements StyledTextLineSpacingProvider {

	/**
	 * The annotation inlined strategy singleton.
	 */
	private static final IDrawingStrategy INLINED_STRATEGY= new InlinedAnnotationDrawingStrategy();

	/**
	 * The annotation inlined strategy ID.
	 */
	private static final String INLINED_STRATEGY_ID= "inlined"; //$NON-NLS-1$

	/**
	 * The source viewer
	 */
	private ISourceViewer fViewer;

	/**
	 * The annotation painter to use to draw the inlined annotations.
	 */
	private AnnotationPainter fPainter;

	/**
	 * Holds the current inlined annotations.
	 */
	private Set<AbstractInlinedAnnotation> fInlinedAnnotations;

	/**
	 * Install the inlined annotation support for the given viewer.
	 *
	 * @param viewer the source viewer
	 * @param painter the annotation painter to use to draw the inlined annotations.
	 */
	public void install(ISourceViewer viewer, AnnotationPainter painter) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(painter);
		fViewer= viewer;
		fPainter= painter;
		initPainter();
		StyledText text= fViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		setColor(text.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		text.setLineSpacingProvider(this);
	}

	/**
	 * Initialize painter with inlined drawing strategy.
	 */
	private void initPainter() {
		fPainter.addDrawingStrategy(INLINED_STRATEGY_ID, INLINED_STRATEGY);
		fPainter.addAnnotationType(AbstractInlinedAnnotation.TYPE, INLINED_STRATEGY_ID);
	}

	/**
	 * Set the color to use to draw the inlined annotations.
	 *
	 * @param color the color to use to draw the inlined annotations.
	 */
	public void setColor(Color color) {
		fPainter.setAnnotationTypeColor(AbstractInlinedAnnotation.TYPE, color);
	}

	/**
	 * Unisntall the inlined annotation support
	 */
	public void uninstall() {
		fViewer= null;
		fPainter= null;
	}

	/**
	 * Update the given inlined annotation.
	 *
	 * @param annotations the inlined annotation.
	 */
	public void updateAnnotations(Set<AbstractInlinedAnnotation> annotations) {
		IDocument document= fViewer != null ? fViewer.getDocument() : null;
		if (document == null) {
			// this case comes from when editor is closed before rendered is done.
			return;
		}
		IAnnotationModel annotationModel= fViewer.getAnnotationModel();
		if (annotationModel == null) {
			return;
		}
		StyledText styledText= fViewer.getTextWidget();
		Map<AbstractInlinedAnnotation, Position> annotationsToAdd= new HashMap<>();
		List<AbstractInlinedAnnotation> annotationsToRemove= fInlinedAnnotations != null
				? new ArrayList<>(fInlinedAnnotations)
				: Collections.emptyList();
		// Loop for annotations to update
		for (AbstractInlinedAnnotation ann : annotations) {
			if (!annotationsToRemove.remove(ann)) {
				// The annotation was not created, add it
				annotationsToAdd.put(ann, ann.getPosition());
			}
			if (ann instanceof LineContentAnnotation) {
				// Create metrics with well width to add space where the inline annotation must
				// be drawn.
				runInUIThread(styledText, (text) -> {
					StyleRange s= new StyleRange();
					s.start= ann.getPosition().getOffset();
					s.length= 1;
					s.metrics= ((LineContentAnnotation) ann).createMetrics();
					text.setStyleRange(s);
				});
			}
		}
		// Process annotations to remove
		for (AbstractInlinedAnnotation ann : annotationsToRemove) {
			// Mark annotation as deleted to ignore the draw
			ann.markDeleted(true);
			if (ann instanceof LineContentAnnotation) {
				// Set metrics to null to remove space of the inline annotation
				runInUIThread(styledText, (text) -> {
					StyleRange s= new StyleRange();
					s.start= ann.getPosition().getOffset();
					s.length= 1;
					s.metrics= null;
					text.setStyleRange(s);
				});
			}
		}
		// Update annotation model
		synchronized (getLockObject(annotationModel)) {
			if (annotationsToAdd.size() == 0 && annotationsToRemove.size() == 0) {
				// None change, do nothing. Here the user could change position of codemining
				// range
				// (ex: user key press
				// "Enter"), but we don't need to redraw the viewer because change of position
				// is done by AnnotationPainter.
			} else {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(
							annotationsToRemove.toArray(new Annotation[annotationsToRemove.size()]), annotationsToAdd);
				} else {
					removeInlinedAnnotations();
					Iterator<Entry<AbstractInlinedAnnotation, Position>> iter= annotationsToAdd.entrySet().iterator();
					while (iter.hasNext()) {
						Entry<AbstractInlinedAnnotation, Position> mapEntry= iter.next();
						annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
					}
				}
			}
			fInlinedAnnotations= annotations;
		}
	}

	/**
	 * Returns the existing codemining annotation with the given position information and null
	 * otherwise.
	 *
	 * @param pos the position
	 * @return the existing codemining annotation with the given position information and null
	 *         otherwise.
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractInlinedAnnotation> T findExistingAnnotation(Position pos) {
		if (fInlinedAnnotations == null) {
			return null;
		}
		for (AbstractInlinedAnnotation ann : fInlinedAnnotations) {
			if (ann.getPosition().offset == pos.offset) {
				try {
					return (T) ann;
				} catch (ClassCastException e) {
					// Do nothing
				}
			}
		}
		return null;
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel the annotation model
	 * @return the annotation model's lock object
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock= ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	/**
	 * Remove the inlined annotations.
	 */
	private void removeInlinedAnnotations() {

		IAnnotationModel annotationModel= fViewer.getAnnotationModel();
		if (annotationModel == null || fInlinedAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						fInlinedAnnotations.toArray(new Annotation[fInlinedAnnotations.size()]), null);
			} else {
				for (AbstractInlinedAnnotation annotation : fInlinedAnnotations)
					annotationModel.removeAnnotation(annotation);
			}
			fInlinedAnnotations= null;
		}
	}

	/**
	 * Returns the line spacing from the given line index with the codemining annotations height and
	 * null otherwise.
	 */
	@SuppressWarnings("boxing")
	@Override
	public Integer getLineSpacing(int lineIndex) {
		AbstractInlinedAnnotation annotation= getInlinedAnnotationAtLine(fViewer, lineIndex);
		return (annotation instanceof LineHeaderAnnotation)
				? ((LineHeaderAnnotation) annotation).getHeight()
				: null;
	}

	/**
	 * Returns the {@link AbstractInlinedAnnotation} from the given line index and null otherwise.
	 *
	 * @param viewer the source viewer
	 * @param lineIndex the line index.
	 * @return the {@link AbstractInlinedAnnotation} from the given line index and null otherwise.
	 */
	public static AbstractInlinedAnnotation getInlinedAnnotationAtLine(ISourceViewer viewer, int lineIndex) {
		if (viewer == null) {
			return null;
		}
		IAnnotationModel annotationModel= viewer.getAnnotationModel();
		if (annotationModel == null) {
			return null;
		}
		IDocument document= viewer.getDocument();
		int lineNumber= lineIndex + 1;
		if (lineNumber > document.getNumberOfLines()) {
			return null;
		}
		try {
			IRegion line= document.getLineInformation(lineNumber);
			Iterator<Annotation> iter= (annotationModel instanceof IAnnotationModelExtension2)
					? ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(line.getOffset(),
							line.getLength(), true, true)
					: annotationModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation ann= iter.next();
				if (ann instanceof AbstractInlinedAnnotation) {
					Position p= annotationModel.getPosition(ann);
					if (p != null) {
						if (p.overlapsWith(line.getOffset(), line.getLength())) {
							return (AbstractInlinedAnnotation) ann;
						}
					}
				}
			}
		} catch (BadLocationException e) {
			return null;
		}
		return null;
	}

	/**
	 * Execute UI {@link StyledText} function which requires UI Thread.
	 *
	 * @param text the styled text
	 * @param fn the function to execute.
	 */
	static void runInUIThread(StyledText text, Consumer<StyledText> fn) {
		if (text == null || text.isDisposed()) {
			return;
		}
		Display display= text.getDisplay();
		if (display.getThread() == Thread.currentThread()) {
			try {
				fn.accept(text);
			} catch (Exception e) {
				// Ignore UI error
			}
		} else {
			display.asyncExec(() -> {
				if (text.isDisposed()) {
					return;
				}
				try {
					fn.accept(text);
				} catch (Exception e) {
					// Ignore UI error
				}
			});
		}
	}
}
