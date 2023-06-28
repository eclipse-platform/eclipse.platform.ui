/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
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
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;

/**
*
* This Reconciler Strategy is an example for how to override the default highlight strategy.
* It will highlight closing and opening tag names that match the current word the user is on.
*
*/
public class HighlightStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension, CaretListener, IPreferenceChangeListener {

	private static final String ANNOTATION_TYPE = "org.eclipse.ui.genericeditor.text"; //$NON-NLS-1$
	public static final String TOGGLE_HIGHLIGHT_PREFERENCE = "org.eclipse.ui.genericeditor.togglehighlight"; //$NON-NLS-1$
	public static final String GENERIC_EDITOR_BUNDLE_ID = "org.eclipse.ui.genericeditor"; //$NON-NLS-1$

	private boolean enabled;
	private ISourceViewer sourceViewer;
	private IDocument document;

	public static final String SEARCH_TERM = "'bar'";

	private Annotation[] fOccurrenceAnnotations = null;

	private void applyHighlights() {
		if (sourceViewer == null || !enabled) {
			return;
		}

		removeOccurrenceAnnotations();
		int searchOffset = document.get().indexOf(SEARCH_TERM);
		if(searchOffset == -1) {
			return;
		}
		sourceViewer.getAnnotationModel().addAnnotation(new Annotation(ANNOTATION_TYPE, false, null),
				new Position(searchOffset, 5));

	}

	public void install(ITextViewer viewer) {
		if (!(viewer instanceof ISourceViewer)) {
			return;
		}
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GENERIC_EDITOR_BUNDLE_ID);
		preferences.addPreferenceChangeListener(this);
		this.enabled = preferences.getBoolean(TOGGLE_HIGHLIGHT_PREFERENCE, true);
		this.sourceViewer = (ISourceViewer) viewer;
		this.sourceViewer.getTextWidget().addCaretListener(this);
	}

	public void uninstall() {
		if (sourceViewer != null) {
			sourceViewer.getTextWidget().removeCaretListener(this);
		}
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GENERIC_EDITOR_BUNDLE_ID);
		preferences.removePreferenceChangeListener(this);
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(TOGGLE_HIGHLIGHT_PREFERENCE)) {
			this.enabled = Boolean.parseBoolean(event.getNewValue().toString());
			if (enabled) {
				initialReconcile();
			} else {
				removeOccurrenceAnnotations();
			}
		}
	}

	@Override
	public void caretMoved(CaretEvent event) {
		applyHighlights();
	}

	@Override
	public void initialReconcile() {
		if (sourceViewer != null) {
			sourceViewer.getTextWidget().getDisplay().asyncExec(() -> {
				if (sourceViewer != null && sourceViewer.getTextWidget() != null) {
					applyHighlights();
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

	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// Do nothing
	}

	@Override
	public void reconcile(IRegion partition) {
		// Do nothing
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// Not used
	}
}
