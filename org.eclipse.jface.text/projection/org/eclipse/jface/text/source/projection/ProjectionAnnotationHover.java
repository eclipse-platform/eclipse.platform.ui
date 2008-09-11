/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source.projection;

import java.util.Iterator;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.LineRange;

/**
 * Annotation hover for projection annotations.
 *
 * @since 3.0
 */
class ProjectionAnnotationHover implements IAnnotationHover, IAnnotationHoverExtension, IInformationProviderExtension2 {


	private IInformationControlCreator fInformationControlCreator;
	private IInformationControlCreator fInformationPresenterControlCreator;

	/**
	 * Sets the hover control creator for this projection annotation hover.
	 *
	 * @param creator the creator
	 */
	public void setHoverControlCreator(IInformationControlCreator creator) {
		fInformationControlCreator= creator;
	}

	/**
	 * Sets the information presenter control creator for this projection annotation hover.
	 *
	 * @param creator the creator
	 * @since 3.3
	 */
	public void setInformationPresenterControlCreator(IInformationControlCreator creator) {
		fInformationPresenterControlCreator= creator;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		// this is a no-op as semantics is defined by the implementation of the annotation hover extension
		return null;
	}

	/*
	 * @since 3.1
	 */
	private boolean isCaptionLine(Position position, IDocument document, int line) {
		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				int captionOffset;
				if (position instanceof IProjectionPosition)
					captionOffset= ((IProjectionPosition) position).computeCaptionOffset(document);
				else
					captionOffset= 0;
				int startLine= document.getLineOfOffset(position.getOffset() + captionOffset);
				return line == startLine;
			} catch (BadLocationException x) {
			}
		}
		return false;
	}

	private String getProjectionTextAtLine(ISourceViewer viewer, int line, int numberOfLines) {

		IAnnotationModel model= null;
		if (viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 viewerExtension= (ISourceViewerExtension2) viewer;
			IAnnotationModel visual= viewerExtension.getVisualAnnotationModel();
			if (visual instanceof IAnnotationModelExtension) {
				IAnnotationModelExtension modelExtension= (IAnnotationModelExtension) visual;
				model= modelExtension.getAnnotationModel(ProjectionSupport.PROJECTION);
			}
		}

		if (model != null) {
			try {
				IDocument document= viewer.getDocument();
				Iterator e= model.getAnnotationIterator();
				while (e.hasNext()) {
					ProjectionAnnotation annotation= (ProjectionAnnotation) e.next();
					if (!annotation.isCollapsed())
						continue;

					Position position= model.getPosition(annotation);
					if (position == null)
						continue;

					if (isCaptionLine(position, document, line))
						return getText(document, position.getOffset(), position.getLength(), numberOfLines);

				}
			} catch (BadLocationException x) {
			}
		}

		return null;
	}

	private String getText(IDocument document, int offset, int length, int numberOfLines) throws BadLocationException {
		int endOffset= offset + length;

		try {
			int endLine= document.getLineOfOffset(offset) + Math.max(0, numberOfLines -1);
			IRegion lineInfo= document.getLineInformation(endLine);
			endOffset= Math.min(endOffset, lineInfo.getOffset() + lineInfo.getLength());
		} catch (BadLocationException x) {
		}

		return document.get(offset, endOffset - offset);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, org.eclipse.jface.text.source.ILineRange, int)
	 */
	public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleLines) {
		return getProjectionTextAtLine(sourceViewer, lineRange.getStartLine(), visibleLines);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverLineRange(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
		return new LineRange(lineNumber, 1);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#canHandleMouseCursor()
	 */
	public boolean canHandleMouseCursor() {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		if (fInformationControlCreator == null) {
			fInformationControlCreator= new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new SourceViewerInformationControl(parent, false, JFaceResources.TEXT_FONT, null);
				}
			};
		}
		return fInformationControlCreator;
	}

	/*
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.3
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fInformationPresenterControlCreator == null) {
			fInformationPresenterControlCreator= new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new SourceViewerInformationControl(parent, true, JFaceResources.TEXT_FONT, null);
				}
			};
		}
		return fInformationPresenterControlCreator;
	}
}
