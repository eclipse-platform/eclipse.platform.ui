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
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class MarkerResoltionQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public boolean canFix(Annotation annotation) {
		return annotation instanceof MarkerAnnotation;
	}

	@Override
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return false;
	}

	@Override
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		IAnnotationModel annotationModel = invocationContext.getSourceViewer().getAnnotationModel();
		Collection<MarkerAnnotation> annotations = new HashSet<>();
		annotationModel.getAnnotationIterator().forEachRemaining(annotation -> {
			Position position = annotationModel.getPosition(annotation);
			if (invocationContext.getOffset() >= position.getOffset() &&
				invocationContext.getOffset() + Math.max(0, invocationContext.getLength()) <= position.getOffset() + position.getLength() &&
				annotation instanceof MarkerAnnotation) {
				annotations.add((MarkerAnnotation)annotation);
			}
		});
		Collection<MarkerResolutionCompletionProposal> resolutions = new ArrayList<>();
		for (MarkerAnnotation annotation : annotations) {
			IMarker marker = annotation.getMarker();
			for (IMarkerResolution resolution : IDE.getMarkerHelpRegistry().getResolutions(marker)) {
				resolutions.add(new MarkerResolutionCompletionProposal(marker, resolution));
			}
		}
		return resolutions.toArray(new ICompletionProposal[resolutions.size()]);
	}
	
}
