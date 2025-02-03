/**
 *  Copyright (c) 2017, 2018 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.internal.text.codemining.CodeMiningLineContentAnnotation;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
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
 * @since 3.13
 */
public class InlinedAnnotationSupport {

	/**
	 * The annotation inlined strategy ID.
	 */
	private static final String INLINED_STRATEGY_ID= "inlined"; //$NON-NLS-1$

	/**
	 * The StyledText font normal, bold, italic and bold itlaic both.
	 */
	private Font regularFont, boldFont, italicFont, boldItalicFont;

	/**
	 * Listener used to update {@link GlyphMetrics} width style for {@link LineContentAnnotation}.
	 */
	private ITextPresentationListener updateStylesWidth;

	/**
	 * Class to update {@link GlyphMetrics} width style for {@link LineContentAnnotation}.
	 */
	private class UpdateStylesWidth implements ITextPresentationListener {

		@Override
		public void applyTextPresentation(TextPresentation textPresentation) {
			IAnnotationModel annotationModel= fViewer.getAnnotationModel();
			if (annotationModel == null) {
				return;
			}
			IRegion region= textPresentation.getExtent();
			((IAnnotationModelExtension2) annotationModel)
					.getAnnotationIterator(region.getOffset(), region.getLength(), true, true)
					.forEachRemaining(annotation -> {
						if (annotation instanceof LineContentAnnotation) {
							LineContentAnnotation ann= (LineContentAnnotation) annotation;
							StyleRange style= ann.updateStyle(null, fFontMetrics, fViewer, isAfterPosition(ann));
							if (style != null) {
								if (fViewer instanceof ITextViewerExtension5 projectionViewer) {
									IRegion annotationRegion= projectionViewer.widgetRange2ModelRange(new Region(style.start, style.length));
									style.start= annotationRegion.getOffset();
									style.length= annotationRegion.getLength();
								}
								textPresentation.mergeStyleRange(style);
							}
						}
					});
		}

		private static boolean isAfterPosition(LineContentAnnotation annotation) {
			if (annotation instanceof CodeMiningLineContentAnnotation a) {
				return a.isAfterPosition();
			}
			return false;
		}
	}

	/**
	 * Tracker of start/end offset of visible lines.
	 */
	private VisibleLines visibleLines;

	/**
	 * Class to track start/end offset of visible lines.
	 */
	private class VisibleLines implements IViewportListener, IDocumentListener, ControlListener {

		private int startOffset;

		private Integer endOffset;

		public VisibleLines() {
			install();
			fViewer.getTextWidget().getDisplay().asyncExec(() -> {
				compute();
			});
		}

		@Override
		public void viewportChanged(int verticalOffset) {
			compute();
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			endOffset= null;
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			if (endOffset != null && event != null && event.fDocument != null && event.fDocument.getLength() > endOffset) {
				endOffset= null;
			}
		}

		@Override
		public void controlMoved(ControlEvent e) {
			// Do nothing
		}

		@Override
		public void controlResized(ControlEvent e) {
			compute();
		}

		private void compute() {
			startOffset= getInclusiveTopIndexStartOffset();
			endOffset= getExclusiveBottomIndexEndOffset();
		}

		/**
		 * Returns the document offset of the upper left corner of the source viewer's view port,
		 * possibly including partially visible lines.
		 *
		 * @return the document offset if the upper left corner of the view port
		 */
		private int getInclusiveTopIndexStartOffset() {
			if (fViewer != null && fViewer.getTextWidget() != null && !fViewer.getTextWidget().isDisposed()) {
				int top= JFaceTextUtil.getPartialTopIndex(fViewer);
				try {
					IDocument document= fViewer.getDocument();
					return document.getLineOffset(top);
				} catch (BadLocationException x) {
					// Do nothing
				}
			}
			return -1;
		}

		/**
		 * Returns the first invisible document offset of the lower right corner of the source
		 * viewer's view port, possibly including partially visible lines.
		 *
		 * @return the first invisible document offset of the lower right corner of the view port
		 */
		private int getExclusiveBottomIndexEndOffset() {
			if (fViewer != null && fViewer.getTextWidget() != null && !fViewer.getTextWidget().isDisposed()) {
				int bottom= JFaceTextUtil.getPartialBottomIndex(fViewer);
				try {
					IDocument document= fViewer.getDocument();
					if (bottom >= document.getNumberOfLines()) {
						bottom= document.getNumberOfLines() - 1;
					}
					return document.getLineOffset(bottom) + document.getLineLength(bottom);
				} catch (BadLocationException x) {
					// Do nothing
				}
			}
			return -1;
		}

		/**
		 * Return whether the given offset is in visible lines.
		 *
		 * @param documentOffset the document relative offset
		 * @return <code>true</code> if the given offset is in visible lines and <code>false</code>
		 *         otherwise.
		 */
		boolean isInVisibleLines(int documentOffset) {
			if (endOffset == null) {
				Display display= fViewer.getTextWidget().getDisplay();
				if (display.getThread() == Thread.currentThread()) {
					endOffset= getExclusiveBottomIndexEndOffset();
				} else {
					display.syncExec(() -> endOffset= getExclusiveBottomIndexEndOffset());
				}
			}
			return documentOffset >= startOffset && documentOffset <= endOffset;
		}

		/**
		 * Uninstall visible lines
		 */
		void uninstall() {
			if (fViewer != null) {
				fViewer.removeViewportListener(this);
				if (fViewer.getDocument() != null) {
					fViewer.getDocument().removeDocumentListener(this);
				}
				if (fViewer.getTextWidget() != null) {
					fViewer.getTextWidget().removeControlListener(this);
				}
			}
		}

		void install() {
			fViewer.addViewportListener(this);
			fViewer.getDocument().addDocumentListener(this);
			fViewer.getTextWidget().addControlListener(this);
		}
	}

	private class MouseTracker implements MouseMoveListener, MouseListener {

		private AbstractInlinedAnnotation fAnnotation;

		private Consumer<MouseEvent> fAction;

		private void update(MouseEvent e) {
			fAnnotation= null;
			fAction= null;
			AbstractInlinedAnnotation annotation= getInlinedAnnotationAtPoint(e.x, e.y);
			if (annotation != null) {
				Consumer<MouseEvent> action= annotation.getAction(e);
				if (action != null) {
					fAnnotation= annotation;
					fAction= action;
				}
			}
		}

		@Override
		public void mouseMove(MouseEvent e) {
			AbstractInlinedAnnotation oldAnnotation= fAnnotation;
			update(e);
			if (oldAnnotation != null) {
				if (oldAnnotation.equals(fAnnotation)) {
					fAnnotation.onMouseMove(e);
					return;
				} else {
					oldAnnotation.onMouseOut(e);
				}
			}
			if (fAnnotation != null) {
				fAnnotation.onMouseHover(e);
			}
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// Do nothing
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// Do nothing
		}

		@Override
		public void mouseUp(MouseEvent e) {
			if (e.button != 1) {
				return;
			}
			if (fAction != null) {
				fAction.accept(e);
			}
		}
	}

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
	 * The mouse tracker used to support hover, click on inlined annotation.
	 */
	private final MouseTracker fMouseTracker= new MouseTracker();

	private FontMetrics fFontMetrics;

	/**
	 * Install the inlined annotation support for the given viewer.
	 *
	 * @param viewer  the source viewer
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
		text.setData(INLINED_STRATEGY_ID, this);
		if (fViewer instanceof ITextViewerExtension4) {
			updateStylesWidth= new UpdateStylesWidth();
			((ITextViewerExtension4) fViewer).addTextPresentationListener(updateStylesWidth);
		}
		visibleLines= new VisibleLines();
		text.addMouseListener(fMouseTracker);
		text.addMouseMoveListener(fMouseTracker);
		Color c= painter.getInlineAnnotationColor();
		if (c == null) {
			c= text.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
		}
		setColor(c);
		GC gc= new GC(viewer.getTextWidget());
		gc.setFont(viewer.getTextWidget().getFont());
		fFontMetrics= gc.getFontMetrics();
		gc.dispose();
	}

	/**
	 * Initialize painter with inlined drawing strategy.
	 */
	private void initPainter() {
		fPainter.addDrawingStrategy(INLINED_STRATEGY_ID, new InlinedAnnotationDrawingStrategy(this.fViewer));
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
		StyledText text= this.fViewer.getTextWidget();
		if (text != null && !text.isDisposed()) {
			text.removeMouseListener(this.fMouseTracker);
			text.removeMouseMoveListener(this.fMouseTracker);
		}
		if (fViewer != null) {
			if (fViewer instanceof ITextViewerExtension4) {
				((ITextViewerExtension4) fViewer).removeTextPresentationListener(updateStylesWidth);
			}
		}
		if (visibleLines != null) {
			visibleLines.uninstall();
			visibleLines= null;
		}
		removeInlinedAnnotations();
		disposeFont();
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
		}
		// Process annotations to remove
		for (AbstractInlinedAnnotation ann : annotationsToRemove) {
			// Mark annotation as deleted to ignore the draw
			ann.markDeleted(true);
		}
		// Update annotation model
		synchronized (getLockObject(annotationModel)) {
			if (annotationsToAdd.isEmpty() && annotationsToRemove.isEmpty()) {
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
			if (pos.equals(ann.getPosition()) && !ann.getPosition().isDeleted()) {
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
	 * Returns the {@link AbstractInlinedAnnotation} from the given point and null otherwise.
	 *
	 * @param x      the x coordinate of the point
	 * @param y      the y coordinate of the point
	 * @return the {@link AbstractInlinedAnnotation} from the given point and null otherwise.
	 */
	private AbstractInlinedAnnotation getInlinedAnnotationAtPoint(int x, int y) {
		if (fInlinedAnnotations != null) {
			for (AbstractInlinedAnnotation ann : fInlinedAnnotations) {
				ann.setSupport(this);
				if (ann.contains(x, y) && isInVisibleLines(ann.getPosition().getOffset())) {
					return ann;
				}
			}
		}
		return null;
	}

	/**
	 * Return whether the given offset is in visible lines.
	 *
	 * @param documentOffset the document offset
	 * @return <code>true</code> if the given offset is in visible lines and <code>false</code>
	 *         otherwise.
	 */
	boolean isInVisibleLines(int documentOffset) {
		if (visibleLines == null) {
			// case of support has been uninstalled and mining must be drawn.
			return false;
		}
		return visibleLines.isInVisibleLines(documentOffset);
	}


	/**
	 * Returns the font according the specified <code>style</code> that the receiver will use to
	 * paint textual information.
	 *
	 * @param style the style of Font widget to get.
	 * @return the receiver's font according the specified <code>style</code>
	 */
	Font getFont(int style) {
		StyledText styledText= fViewer != null ? fViewer.getTextWidget() : null;
		if (styledText == null) {
			return null;
		}
		if (!styledText.getFont().equals(regularFont)) {
			disposeFont();
			regularFont= styledText.getFont();
		}
		Device device= styledText.getDisplay();
		switch (style) {
			case SWT.BOLD:
				if (boldFont != null)
					return boldFont;
				return boldFont= new Font(device, getFontData(style));
			case SWT.ITALIC:
				if (italicFont != null)
					return italicFont;
				return italicFont= new Font(device, getFontData(style));
			case SWT.BOLD | SWT.ITALIC:
				if (boldItalicFont != null)
					return boldItalicFont;
				return boldItalicFont= new Font(device, getFontData(style));
			default:
				return regularFont;
		}
	}

	/**
	 * Returns the font data array according the given style.
	 *
	 * @param style the style
	 * @return the font data array according the given style.
	 */
	FontData[] getFontData(int style) {
		FontData[] fontDatas= regularFont.getFontData();
		for (FontData fontData : fontDatas) {
			fontData.setStyle(style);
		}
		return fontDatas;
	}

	/**
	 * Dispose the font.
	 */
	void disposeFont() {
		if (boldFont != null)
			boldFont.dispose();
		if (italicFont != null)
			italicFont.dispose();
		if (boldItalicFont != null)
			boldItalicFont.dispose();
		boldFont= italicFont= boldItalicFont= null;
	}

	/**
	 * @return the contextual viewer (this one can change)
	 */
	ISourceViewer getViewer() {
		return this.fViewer;
	}

	static InlinedAnnotationSupport getSupport(StyledText widget) {
		return widget.getData(InlinedAnnotationSupport.INLINED_STRATEGY_ID) instanceof InlinedAnnotationSupport support ?
			support : null;
	}
}
