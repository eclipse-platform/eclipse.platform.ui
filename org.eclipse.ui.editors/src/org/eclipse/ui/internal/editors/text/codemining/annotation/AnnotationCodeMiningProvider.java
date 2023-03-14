/*******************************************************************************
 * Copyright (c) 2018, 2023 Altran Netherlands B.V. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Niko Stotz (Altran Netherlands B.V.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text.codemining.annotation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.ISourceViewerExtension3;
import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

/**
 * Shows <i>info</i>, <i>warning</i>, and <i>error</i> Annotations as line header code minings.
 *
 * <p>
 * If the annotation is quickfixable, clicking on the code mining triggers the quickfix.
 * </p>
 * <p>
 * The user can configure which and how many Annotations should be shown in preferences.
 * </p>
 * <p>
 * Works out-of-the-box for all code mining-enabled text editors.
 * </p>
 *
 * @since 3.13
 * @see org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningPreferences
 */
@NonNullByDefault
public class AnnotationCodeMiningProvider extends AbstractCodeMiningProvider
		implements AnnotationCodeMiningFilter.Locator {

	/**
	 * Updates code minings after changes to preferences.
	 */
	private class PropertyChangeListener implements IPropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			switch (event.getProperty()) {
				case AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL:
				case AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_MAX:
					getCodeMiningViewer().updateCodeMinings();
					break;
				default:
					// ignore
			}
		}
	}

	/**
	 * Updates code minings after changes to annotations.
	 */
	private class AnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {
		private final class RemoveCodeMiningTimerTask extends TimerTask {
			@Override
			public void run() {
				getCodeMiningViewer().updateCodeMinings();
				removeTimer= null;
			}
		}

		private Timer removeTimer;

		private void scheduleTimer() {
			if (removeTimer == null) {
				removeTimer= new Timer("Remove Code Mining Annotations");
				removeTimer.schedule(new RemoveCodeMiningTimerTask(), 111);
			}
		}

		@Override
		public void modelChanged(@Nullable IAnnotationModel model) {
			// ignore
		}

		@Override
		public void modelChanged(@SuppressWarnings("null") AnnotationModelEvent event) {
			if (viewer == null) {
				return;
			}

			if (!event.isValid() || event.isEmpty()) {
				return;
			}

			AnnotationCodeMiningFilter addChangeFilter= new AnnotationCodeMiningFilter(getAnnotationAccess(), event.getAddedAnnotations(), event.getChangedAnnotations());
			if (!addChangeFilter.isEmpty()) {
				getCodeMiningViewer().updateCodeMinings();
			}

			AnnotationCodeMiningFilter removeFilter= new AnnotationCodeMiningFilter(getAnnotationAccess(), event.getRemovedAnnotations());
			if (!removeFilter.isEmpty()) {
				scheduleTimer();
			}
		}
	}

	private @Nullable ITextViewer viewer= null;

	private @Nullable AnnotationModelListener annotationModelListener= null;

	private @Nullable PropertyChangeListener propertyChangeListener= null;

	private @Nullable IAnnotationAccessExtension annotationAccess= null;

	@Override
	@SuppressWarnings("null")
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
		if (!(viewer instanceof ISourceViewerExtension5)) {
			throw new IllegalArgumentException("Cannot attach to TextViewer without code mining support"); //$NON-NLS-1$
		}

		if (!new AnnotationCodeMiningPreferences().isEnabled()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		this.viewer= viewer;

		final IAnnotationAccess annotationAccess= getAdapter(IAnnotationAccess.class);
		if (!(annotationAccess instanceof IAnnotationAccessExtension)) {
			throw new IllegalStateException("annotationAccess must implement IAnnotationAccessExtension"); //$NON-NLS-1$
		}
		this.annotationAccess= (IAnnotationAccessExtension) annotationAccess;

		return provideCodeMiningsInternal(monitor);
	}

	@Override
	public void dispose() {
		unregisterPropertyChangeListener();
		unregisterAnnotationModelListener();

		this.viewer= null;

		super.dispose();
	}

	@Override
	@SuppressWarnings("boxing")
	public @Nullable Integer getLine(Annotation annotation) {
		Integer offset= getOffset(annotation);
		if (offset == null) {
			return null;
		}

		try {
			return getDocument().getLineOfOffset(offset);
		} catch (BadLocationException e) {
			return null;
		}
	}

	@Override
	@SuppressWarnings("boxing")
	public @Nullable Integer getOffset(Annotation annotation) {
		final Position position= getAnnotationModel().getPosition(annotation);
		if (position == null) {
			return null;
		}

		return position.getOffset();
	}

	private CompletableFuture<List<? extends ICodeMining>> provideCodeMiningsInternal(IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			if (!checkAnnotationModelAvailable()) {
				return Collections.emptyList();
			}

			registerAnnotationModelListener();
			registerPropertyChangeListener();

			final Stream<Annotation> annotations= getAnnotations();
			final List<AbstractCodeMining> codeMinings= createCodeMinings(annotations, monitor);

			return codeMinings;
		});
	}

	private void registerAnnotationModelListener() {
		if (annotationModelListener == null) {
			annotationModelListener= new AnnotationModelListener();
			getAnnotationModel().addAnnotationModelListener(annotationModelListener);
		}
	}

	private void unregisterAnnotationModelListener() {
		if (this.annotationModelListener != null) {
			getAnnotationModel().removeAnnotationModelListener(annotationModelListener);
			this.annotationModelListener= null;
		}
	}

	private void registerPropertyChangeListener() {
		if (propertyChangeListener == null) {
			final @Nullable IPreferenceStore store= new AnnotationCodeMiningPreferences().getPreferences();
			if (store != null) {
				propertyChangeListener= new PropertyChangeListener();
				store.addPropertyChangeListener(propertyChangeListener);
			}
		}
	}

	private void unregisterPropertyChangeListener() {
		if (this.propertyChangeListener != null) {
			final @Nullable IPreferenceStore store= new AnnotationCodeMiningPreferences().getPreferences();
			if (store != null) {
				store.removePropertyChangeListener(propertyChangeListener);
			}
			this.propertyChangeListener= null;
		}
	}

	private Stream<Annotation> getAnnotations() {
		return new AnnotationCodeMiningFilter(getAnnotationAccess(), getAnnotationModel().getAnnotationIterator())
				.sortDistinctLimit(this);
	}

	private List<AbstractCodeMining> createCodeMinings(Stream<Annotation> annotations, IProgressMonitor monitor) {
		@SuppressWarnings("null")
		final Stream<AbstractCodeMining> result= annotations
				.filter(m -> !monitor.isCanceled())
				.map(this::createCodeMining)
				.filter(Objects::nonNull);
		return result.toList();
	}

	@SuppressWarnings("boxing")
	private @Nullable AbstractCodeMining createCodeMining(Annotation annotation) {
		final Integer lineNumber= getLine(annotation);

		if (lineNumber == null) {
			return null;
		}

		try {
			final Consumer<MouseEvent> action= createAction(annotation);
			return new AnnotationCodeMining(getAnnotationAccess(), annotation, lineNumber, getDocument(), this, action);
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * The action selects the text attached to the Annotation and activates quickfixes.
	 */
	private @Nullable Consumer<MouseEvent> createAction(Annotation annotation) {
		if (!(annotation instanceof IQuickFixableAnnotation) || !(getTextViewer() instanceof ISourceViewerExtension3)) {
			return null;
		}

		final Position position= getAnnotationModel().getPosition(annotation);
		if (position == null) {
			return null;
		}

		return (e -> {
			final IQuickFixableAnnotation quickFixableAnnotation= (IQuickFixableAnnotation) annotation;
			if (!quickFixableAnnotation.isQuickFixableStateSet() || !quickFixableAnnotation.isQuickFixable()) {
				return;
			}

			final IQuickAssistAssistant quickAssistAssistant= ((ISourceViewerExtension3) getTextViewer()).getQuickAssistAssistant();
			if (quickAssistAssistant == null) {
				return;
			}

			if (!quickAssistAssistant.canFix(annotation)) {
				return;
			}

			getTextViewer().setSelectedRange(position.getOffset(), position.getLength());

			final String message= quickAssistAssistant.showPossibleQuickAssists();

			if (message != null) {
				EditorsPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, message));
			}

		});
	}

	private boolean checkAnnotationModelAvailable() {
		return viewer != null && getAnnotationViewer().getVisualAnnotationModel() != null;
	}

	private IAnnotationModel getAnnotationModel() {
		return getAnnotationViewer().getVisualAnnotationModel();
	}

	private ITextViewer getTextViewer() {
		Assert.isNotNull(viewer);
		return viewer;
	}

	private ISourceViewerExtension5 getCodeMiningViewer() {
		return (ISourceViewerExtension5) getTextViewer();
	}

	private ISourceViewerExtension2 getAnnotationViewer() {
		return (ISourceViewerExtension2) getTextViewer();
	}

	private IDocument getDocument() {
		return getTextViewer().getDocument();
	}

	private IAnnotationAccessExtension getAnnotationAccess() {
		Assert.isNotNull(annotationAccess);
		return annotationAccess;
	}
}
