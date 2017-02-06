/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Delegate to {@link DefaultTextHover}, since we need a parameter-less
 * constructor.
 */
public class AnnotationHoverDelegate implements ITextHover {
	
	private DefaultTextHover delegate;
	private ISourceViewer viewer;

	private DefaultTextHover getDelegate(ISourceViewer sourceViewer) {
		if (this.delegate == null || this.viewer != sourceViewer) {
			this.delegate = new DefaultTextHover(sourceViewer) {
				@Override
				protected boolean isIncluded(Annotation annotation) {
					if (annotation instanceof MarkerAnnotation) {
						// this is handled by MarkerAnnotationHover
						return false;
					}
					AnnotationPreference preference= EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
					if (preference == null)
						return false;
					String key= preference.getTextPreferenceKey();
					if (key != null) {
						if (!EditorsUI.getPreferenceStore().getBoolean(key))
							return false;
					} else {
						key= preference.getHighlightPreferenceKey();
						if (key == null || !EditorsUI.getPreferenceStore().getBoolean(key))
							return false;
					}
					return true;
				}
			};
			this.viewer = sourceViewer;
		}
		return this.delegate;
	}

	@Deprecated
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (textViewer instanceof ISourceViewer) {
			return getDelegate((ISourceViewer)textViewer).getHoverInfo(textViewer, hoverRegion);
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if (textViewer instanceof ISourceViewer) {
			return getDelegate((ISourceViewer)textViewer).getHoverRegion(textViewer, offset);
		}
		return null;
	}


}
