/*******************************************************************************
 * Copyright (c) 2021 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Christoph Läubrich - Inital API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.genericeditor.preferences.GenericEditorPreferenceConstants;

/**
 * Extension of the ContentAssistant that supports the following additional
 * features:
 * <ul>
 * <li>#updateTokens refresh the registration of
 * {@link IContentAssistProcessor}s on document changes</li>
 * <li>Using a ContentTypeRelatedExtensionTracker to dynamically track
 * IContentAssistProcessor in the OSGi service registry
 * </ul>
 * 
 * @author christoph
 */
public class GenericEditorContentAssistant extends ContentAssistant implements IPropertyChangeListener {
	private static final DefaultContentAssistProcessor DEFAULT_CONTENT_ASSIST_PROCESSOR = new DefaultContentAssistProcessor();
	private ContentTypeRelatedExtensionTracker<IContentAssistProcessor> contentAssistProcessorTracker;
	private Set<IContentType> types;
	private List<IContentAssistProcessor> processors;
	private final IPreferenceStore preferenceStore;

	/**
	 * Creates a new GenericEditorContentAssistant instance for the given content
	 * types and contentAssistProcessorTracker
	 * 
	 * @param contentAssistProcessorTracker the tracker to use for tracking
	 *                                      additional
	 *                                      {@link IContentAssistProcessor}s in the
	 *                                      OSGi service factory
	 * @param processors                    the static processor list
	 * @param types                         the {@link IContentType} that are used
	 *                                      to filter appropriate candidates from
	 *                                      the registry
	 */
	public GenericEditorContentAssistant(
			ContentTypeRelatedExtensionTracker<IContentAssistProcessor> contentAssistProcessorTracker,
			List<IContentAssistProcessor> processors, Set<IContentType> types) {
		this(contentAssistProcessorTracker, processors, types, null);
	}

	/**
	 * Creates a new GenericEditorContentAssistant instance for the given content
	 * types and contentAssistProcessorTracker
	 * 
	 * @param contentAssistProcessorTracker the tracker to use for tracking
	 *                                      additional
	 *                                      {@link IContentAssistProcessor}s in the
	 *                                      OSGi service factory
	 * @param processors                    the static processor list
	 * @param types                         the {@link IContentType} that are used
	 *                                      to filter appropriate candidates from
	 *                                      the registry
	 * @param preferenceStore               the {@link IPreferenceStore} containing
	 *                                      the content assistant preferences
	 */
	public GenericEditorContentAssistant(
			ContentTypeRelatedExtensionTracker<IContentAssistProcessor> contentAssistProcessorTracker,
			List<IContentAssistProcessor> processors, Set<IContentType> types, IPreferenceStore preferenceStore) {
		super(true);
		this.contentAssistProcessorTracker = contentAssistProcessorTracker;
		this.processors = Objects.requireNonNullElseGet(processors, () -> Collections.emptyList());
		this.types = types;
		this.preferenceStore = preferenceStore;

		setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		setProposalPopupOrientation(IContentAssistant.PROPOSAL_REMOVE);
		enableColoredLabels(true);
		if (preferenceStore != null) {
			updateAutoActivationPreferences();
			preferenceStore.addPropertyChangeListener(this);
		} else {
			enableAutoActivation(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_DEFAULT);
			setAutoActivationDelay(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY_DEFAULT);
			enableAutoActivateCompletionOnType(
					GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE_DEFAULT);
		}
		setInformationControlCreator(new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
	}

	/**
	 * Updates the {@link IContentAssistProcessor} registrations according to the
	 * documents content-type tokens
	 * 
	 * @param document the document to use for updating the tokens
	 */
	public void updateTokens(IDocument document) {
		updateProcessors(document);
		contentAssistProcessorTracker.getTracked().stream().filter(s -> s.isPresent()).map(s -> s.get())
				.forEach(p -> updateProcessorToken(p, document));
	}

	private void updateProcessors(IDocument iDocument) {
		if (processors.isEmpty()) {
			updateProcessorToken(DEFAULT_CONTENT_ASSIST_PROCESSOR, iDocument);
		} else {
			for (IContentAssistProcessor processor : processors) {
				updateProcessorToken(processor, iDocument);
			}
		}
	}

	private void updateProcessorToken(IContentAssistProcessor processor, IDocument document) {
		removeContentAssistProcessor(processor);
		addContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		if (document != null) {
			for (String contentType : document.getLegalContentTypes()) {
				addContentAssistProcessor(processor, contentType);
			}
		}
		if (processor != DEFAULT_CONTENT_ASSIST_PROCESSOR) {
			removeContentAssistProcessor(DEFAULT_CONTENT_ASSIST_PROCESSOR);
		}
	}

	private void updateAutoActivationPreferences() {
		enableAutoActivation(
				preferenceStore.getBoolean(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION));
		setAutoActivationDelay(
				preferenceStore.getInt(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY));
		enableAutoActivateCompletionOnType(
				preferenceStore.getBoolean(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE));
	}

	@Override
	public void uninstall() {
		contentAssistProcessorTracker.stopTracking();
		if (preferenceStore != null) {
			preferenceStore.removePropertyChangeListener(this);
		}
		super.uninstall();
	}

	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		updateProcessors(textViewer.getDocument());
		contentAssistProcessorTracker.onAdd(added -> {
			if (types.contains(added.getContentType())) {
				IContentAssistProcessor processor = added.get();
				if (processor != null) {
					updateProcessorToken(processor, textViewer.getDocument());
				}
			}
		});
		contentAssistProcessorTracker.onRemove(removed -> {
			if (removed.isPresent()) {
				removeContentAssistProcessor(removed.get());
			}
		});
		contentAssistProcessorTracker.startTracking();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		updateAutoActivationPreferences();
	}
}
