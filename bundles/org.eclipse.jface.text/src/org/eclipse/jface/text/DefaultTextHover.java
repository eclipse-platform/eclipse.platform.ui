/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Deprecated
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IAnnotationModel model= getAnnotationModel(fSourceViewer);
		if (model == null)
			return null;

		Iterator<Annotation> e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= e.next();
			if (isIncluded(a)) {
				Position p= model.getPosition(a);
				if (p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg= a.getText();
					if (msg != null && !msg.trim().isEmpty())
						return msg;
				}
			}
		}

		return null;
	}

	@Override
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
		if (viewer instanceof ISourceViewerExtension2 extension) {
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
