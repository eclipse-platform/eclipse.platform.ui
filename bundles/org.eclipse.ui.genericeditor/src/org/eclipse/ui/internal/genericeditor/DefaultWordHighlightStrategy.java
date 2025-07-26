/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Lucas Bullen (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.osgi.util.NLS;

/**
 *
 * This Reconciler Strategy is a default strategy which will be present if no other highlightReconcilers are registered for a given content-type. It splits the text into 'words' (which are defined as
 * anything in-between non-alphanumeric characters) and searches the document highlighting all like words.
 *
 * E.g. if your file contains "t^he dog in the bog" and you leave your caret at ^ you will get both instances of 'the' highlighted.
 */
public class DefaultWordHighlightStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension, IPreferenceChangeListener {

	private static final String ANNOTATION_TYPE = "org.eclipse.ui.genericeditor.text"; //$NON-NLS-1$

	private boolean enabled;
	private ISourceViewer sourceViewer;
	private IDocument document;

	private static final String WORD_REGEXP = "\\w+"; //$NON-NLS-1$
	private static final Pattern WORD_PATTERN = Pattern.compile(WORD_REGEXP, Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern CURRENT_WORD_START_PATTERN = Pattern.compile(WORD_REGEXP + "$", //$NON-NLS-1$
			Pattern.UNICODE_CHARACTER_CLASS);

	private Annotation[] fOccurrenceAnnotations = null;

	private ISelectionChangedListener editorSelectionChangedListener = event -> applyHighlights(event.getSelection());

	private void applyHighlights(ISelection selection) {
		if (!(selection instanceof ITextSelection)) {
			return;
		}
		ITextSelection textSelection = (ITextSelection) selection;
		if (sourceViewer == null || !enabled) {
			removeOccurrenceAnnotations();
			return;
		}

		String text = document.get();
		int offset = textSelection.getOffset();
		if (sourceViewer instanceof ITextViewerExtension5) {
			offset = ((ITextViewerExtension5) sourceViewer).widgetOffset2ModelOffset(textSelection.getOffset());
		}

		String word = findCurrentWord(text, offset);
		if (word == null) {
			removeOccurrenceAnnotations();
			return;
		}

		Matcher m = WORD_PATTERN.matcher(text);
		Map<Annotation, Position> annotationMap = new HashMap<>();
		while (m.find()) {
			if (m.group().equals(word)) {
				annotationMap.put(new Annotation(ANNOTATION_TYPE, false, NLS.bind(Messages.DefaultWordHighlightStrategy_OccurrencesOf, word)), new Position(m.start(), m.end() - m.start()));
			}
		}

		if (annotationMap.size() < 2) {
			removeOccurrenceAnnotations();
			return;
		}

		IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		if (annotationModel != null) {
			synchronized (getLockObject(annotationModel)) {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, annotationMap);
				} else {
					removeOccurrenceAnnotations();
					Iterator<Entry<Annotation, Position>> iter = annotationMap.entrySet().iterator();
					while (iter.hasNext()) {
						Entry<Annotation, Position> mapEntry = iter.next();
						annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
					}
				}
				fOccurrenceAnnotations = annotationMap.keySet().toArray(new Annotation[annotationMap.size()]);
			}
		} else {
			fOccurrenceAnnotations = null;
		}
	}

	private static String findCurrentWord(String text, int offset) {
		if (offset < 0 || offset >= text.length()) {
			return null;
		}
		String wordStart = null;
		String wordEnd = null;

		String substring = text.substring(0, offset);
		Matcher m = CURRENT_WORD_START_PATTERN.matcher(substring);
		if (m.find()) {
			wordStart = m.group();
		}
		substring = text.substring(offset);
		m = WORD_PATTERN.matcher(substring);
		if (m.lookingAt()) {
			wordEnd = m.group();
		}
		if (wordStart != null && wordEnd != null)
			return wordStart + wordEnd;
		if (wordStart != null)
			return wordStart;
		return wordEnd;
	}

	public void install(ITextViewer viewer) {
		if (!(viewer instanceof ISourceViewer)) {
			return;
		}
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GenericEditorPlugin.BUNDLE_ID);
		preferences.addPreferenceChangeListener(this);
		this.enabled = preferences.getBoolean(ToggleHighlight.TOGGLE_HIGHLIGHT_PREFERENCE, true);
		this.sourceViewer = (ISourceViewer) viewer;
		((IPostSelectionProvider) sourceViewer.getSelectionProvider()).addPostSelectionChangedListener(editorSelectionChangedListener);
	}

	public void uninstall() {
		if (sourceViewer != null) {
			((IPostSelectionProvider) sourceViewer.getSelectionProvider()).removePostSelectionChangedListener(editorSelectionChangedListener);
		}
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GenericEditorPlugin.BUNDLE_ID);
		preferences.removePreferenceChangeListener(this);
	}

	@Override public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(ToggleHighlight.TOGGLE_HIGHLIGHT_PREFERENCE)) {
			this.enabled = Boolean.parseBoolean(event.getNewValue().toString());
			if (enabled) {
				initialReconcile();
			} else {
				removeOccurrenceAnnotations();
			}
		}
	}

	@Override public void initialReconcile() {
		if (sourceViewer != null) {
			sourceViewer.getTextWidget().getDisplay().asyncExec(() -> {
				if (sourceViewer != null && sourceViewer.getTextWidget() != null) {
					applyHighlights(sourceViewer.getSelectionProvider().getSelection());
				}
			});
		}
	}

	void removeOccurrenceAnnotations() {
		IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		if (annotationModel == null || fOccurrenceAnnotations == null) {
			return;
		}

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, null);
			} else {
				for (Annotation fOccurrenceAnnotation : fOccurrenceAnnotations) {
					annotationModel.removeAnnotation(fOccurrenceAnnotation);
				}
			}
			fOccurrenceAnnotations = null;
		}
	}

	private static Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null) {
				return lock;
			}
		}
		return annotationModel;
	}

	@Override public void setDocument(IDocument document) {
		this.document = document;
	}

	@Override public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// Do nothing
	}

	@Override public void reconcile(IRegion partition) {
		// Do nothing
	}

	@Override public void setProgressMonitor(IProgressMonitor monitor) {
		// Not used
	}
}
