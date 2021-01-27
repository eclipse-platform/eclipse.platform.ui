/*******************************************************************************
 * Copyright (c) 2017, 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *   Christoph Läubrich - [Generic Editor] misses quick fix if not at start of line
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
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
			if (annotation instanceof MarkerAnnotation) {
				MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
				Position position = annotationModel.getPosition(annotation);
				int documentOffset = invocationContext.getOffset();
				int annotationOffset = position.getOffset();
				int selectionLength = invocationContext.getLength();
				int annotationLength = position.getLength();
				if (annotationLength == 0) {
					// Marker lines are 1-based
					int markerLine = markerAnnotation.getMarker().getAttribute(IMarker.LINE_NUMBER, 0) - 1;
					if (markerLine > -1) {
						try {
							int documentLine = invocationContext.getSourceViewer().getDocument()
									.getLineOfOffset(documentOffset);
							if (markerLine == documentLine) {
								annotations.add((MarkerAnnotation) annotation);
							}
						} catch (BadLocationException e) {
							// can't be used then...
						}
					}
				}
				if (documentOffset >= annotationOffset
						&& documentOffset + Math.max(0, selectionLength) <= annotationOffset + annotationLength) {
					annotations.add(markerAnnotation);
				}
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
