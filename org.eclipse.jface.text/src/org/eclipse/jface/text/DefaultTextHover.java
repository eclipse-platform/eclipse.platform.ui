/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;

/**
 * Standard implementation of {@link org.eclipse.jface.text.ITextHover}.
 *
 * @since 3.2
 */
public class DefaultTextHover implements ITextHover {

	/** This hover's source viewer */
	private ISourceViewer fSourceViewer;

	/**
	 * Creates a new annotation hover.
	 *
	 * @param sourceViewer this hover's annotation model
	 */
	public DefaultTextHover(ISourceViewer sourceViewer)  {
		Assert.isNotNull(sourceViewer);
		fSourceViewer= sourceViewer;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.4, replaced by {@link ITextHoverExtension2#getHoverInfo2(ITextViewer, IRegion)}
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model= getAnnotationModel(fSourceViewer);
		if (model == null)
			return null;

		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			if (isIncluded(a)) {
				Position p= model.getPosition(a);
				if (p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg= a.getText();
					if (msg != null && msg.trim().length() > 0)
						return msg;
				}
			}
		}

		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return findWord(textViewer.getDocument(), offset);
	}

	/**
	 * Tells whether the annotation should be included in
	 * the computation.
	 *
	 * @param annotation the annotation to test
	 * @return <code>true</code> if the annotation is included in the computation
	 */
	protected boolean isIncluded(Annotation annotation) {
		return true;
	}

	private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
		if (viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 extension= (ISourceViewerExtension2) viewer;
			return extension.getVisualAnnotationModel();
		}
		return viewer.getAnnotationModel();
	}

	private IRegion findWord(IDocument document, int offset) {
		int start= -2;
		int end= -1;

		try {

			int pos= offset;
			char c;

			while (pos >= 0) {
				c= document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c))
					break;
				--pos;
			}

			start= pos;

			pos= offset;
			int length= document.getLength();

			while (pos < length) {
				c= document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c))
					break;
				++pos;
			}

			end= pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
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
