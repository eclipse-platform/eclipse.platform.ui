/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.markers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class MarkerAnnotationHover implements ITextHoverExtension, ITextHoverExtension2, ITextHover {

	protected static boolean isIncluded(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return false;
		}
		AnnotationPreference preference= EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
		if (preference == null) {
			return false;
		}
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


	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		Object hoverInfo = getHoverInfo2(textViewer, hoverRegion);
		if (hoverInfo == null) {
			return null;
		}
		return hoverInfo.toString();
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if (!(textViewer instanceof ISourceViewerExtension2)) {
			return null;
		}
		ISourceViewerExtension2 viewer = (ISourceViewerExtension2)textViewer;
		List<MarkerAnnotation> annotations = findMarkerAnnotations(viewer, new Region(offset, 0));
		if (annotations.isEmpty()) {
			return null;
		}
		// find intersection of regions
		int highestOffsetStart = 0;
		int lowestOffsetEnd = Integer.MAX_VALUE;
		IAnnotationModel annotationModel = viewer.getVisualAnnotationModel();
		for (Annotation annotation : annotations) {
			Position position = annotationModel.getPosition(annotation);
			highestOffsetStart = Math.max(highestOffsetStart, position.getOffset());
			lowestOffsetEnd = Math.min(lowestOffsetEnd, position.getOffset() + position.getLength());
		}
		return new Region(highestOffsetStart, Math.max(0, lowestOffsetEnd - highestOffsetStart));
	}

	@Override
	public List<IMarker> getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		if (!(textViewer instanceof ISourceViewerExtension2)) {
			return null;
		}
		List<MarkerAnnotation> annotations = findMarkerAnnotations((ISourceViewerExtension2)textViewer, hoverRegion);
		if (annotations.isEmpty()) {
			return null;
		}
		List<IMarker> markers = new ArrayList<>(annotations.size());
		for (MarkerAnnotation annotation : annotations) {
			markers.add(annotation.getMarker());
		}
		return markers;
	}
	
	private static List<MarkerAnnotation> findMarkerAnnotations(ISourceViewerExtension2 viewer, IRegion region) {
		List<MarkerAnnotation> res = new ArrayList<>();
		IAnnotationModel annotationModel = viewer.getVisualAnnotationModel();
		annotationModel.getAnnotationIterator().forEachRemaining(annotation -> {
			if (isIncluded(annotation)) {
				Position position = annotationModel.getPosition(annotation);
				if (region.getOffset() >= position.getOffset() && region.getOffset() + region.getLength() <= position.getOffset() + position.getLength()) {
					res.add((MarkerAnnotation)annotation);
				}
			}
		});
		return res;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new MarkerHoverControlCreator();
	}

}
