/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;
import org.eclipse.ui.texteditor.spelling.SpellingService;

public class SpellCheckDocumentListener implements IDocumentListener {
	
	Job lastJob = null;
	SpellingService service = EditorsUI.getSpellingService();

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	@Override
	public void documentChanged(final DocumentEvent event) {
		if (this.lastJob != null) {
			this.lastJob.cancel();
		}
		this.lastJob = new Job("Spellcheck") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IAnnotationModel model = ITextFileBufferManager.DEFAULT.getTextFileBuffer(event.getDocument()).getAnnotationModel();
				String text = event.getDocument().get();
				int commentStart = text.indexOf("<comment>");
				if (commentStart < 0) {
					return Status.OK_STATUS;
				}
				commentStart += "<comment>".length();
				int commentEnd = text.indexOf("</comment>", commentStart);
				if (commentEnd <= commentStart) {
					return Status.OK_STATUS;
				}
				Region region = new Region(commentStart, commentEnd - commentStart);
				service.check(event.getDocument(), new Region[] { region }, new SpellingContext(), new ISpellingProblemCollector() {
					private Map<SpellingAnnotation, Position> annotations = new HashMap<>();
				
					@Override
					public void endCollecting() {
						Set<SpellingAnnotation> previous = new HashSet<>();
						model.getAnnotationIterator().forEachRemaining(annotation -> {
							if (annotation instanceof SpellingAnnotation) {
								previous.add((SpellingAnnotation)annotation);
							}
						});
						if (model instanceof IAnnotationModelExtension) {
							((IAnnotationModelExtension)model).replaceAnnotations(
									previous.toArray(new SpellingAnnotation[previous.size()]),
									annotations);
							
						} else {
							previous.forEach(model::removeAnnotation);
							annotations.forEach(model::addAnnotation);
						}
					}
					
					@Override
					public void beginCollecting() {
					}
					
					@Override
					public void accept(SpellingProblem problem) {
						this.annotations.put(new SpellingAnnotation(problem), new Position(problem.getOffset(), problem.getLength()));
					}
				}, monitor);
				return Status.OK_STATUS;
			}
		};
		this.lastJob.setUser(false);
		this.lastJob.setPriority(Job.DECORATE);
		// set a delay before reacting to user action to handle continuous typing
		this.lastJob.schedule(500);
	}

}
