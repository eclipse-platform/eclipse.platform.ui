/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Sopot Cela, Mickael Istria (Red Hat Inc.) - initial implementation
 *   Lucas Bullen (Red Hat Inc.) - Bug 508829 custom reconciler support
 *                               - Bug 521382 default highlight reconciler
 *   Simon Scholz <simon.scholz@vogella.com> - Bug 527830
 *   Angelo Zerr <angelo.zerr@gmail.com> - [generic editor] Default Code folding for generic editor should use IndentFoldingStrategy - Bug 520659
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.genericeditor.folding.DefaultFoldingReconciler;
import org.eclipse.ui.internal.genericeditor.hover.CompositeTextHover;
import org.eclipse.ui.internal.genericeditor.markers.MarkerResoltionQuickAssistProcessor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingCorrectionProcessor;

/**
 * The configuration of the {@link ExtensionBasedTextEditor}. It registers the proxy composite for hover, completion, syntax highlighting, and then those proxy take care of resolving to the right
 * extensions on-demand.
 *
 * @since 1.0
 */
@SuppressWarnings("restriction")
public final class ExtensionBasedTextViewerConfiguration extends TextSourceViewerConfiguration implements IDocumentPartitioningListener {

	private ITextEditor editor;
	private Set<IContentType> contentTypes;
	private IDocument document;

	private ContentAssistant contentAssistant;
	private List<IContentAssistProcessor> processors;

	/**
	 *
	 * @param editor
	 *            the editor we're creating.
	 * @param preferenceStore
	 *            the preference store.
	 */
	public ExtensionBasedTextViewerConfiguration(ITextEditor editor, IPreferenceStore preferenceStore) {
		super(preferenceStore);
		this.editor = editor;
	}

	Set<IContentType> getContentTypes(ISourceViewer viewer) {
		if (this.contentTypes == null) {
			this.contentTypes = new LinkedHashSet<>();
			String fileName = null;
			if (this.editor != null) {
				fileName = editor.getEditorInput().getName();
			} else {
				IDocument document = viewer.getDocument();
				if (document != null) {
					ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
					if (buffer != null) {
						fileName = buffer.getLocation().lastSegment();
					}
				}
			}
			if (fileName == null) {
				return Collections.emptySet();
			}
			Queue<IContentType> types = new LinkedList<>(Arrays.asList(Platform.getContentTypeManager().findContentTypesFor(fileName)));
			while (!types.isEmpty()) {
				IContentType type = types.poll();
				this.contentTypes.add(type);
				IContentType parent = type.getBaseType();
				if (parent != null) {
					types.add(parent);
				}
			}
		}
		return this.contentTypes;
	}

	@Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		List<ITextHover> hovers = GenericEditorPlugin.getDefault().getHoverRegistry().getAvailableHovers(sourceViewer, editor, getContentTypes(sourceViewer));
		if (hovers == null || hovers.isEmpty()) {
			return null;
		} else if (hovers.size() == 1) {
			return hovers.get(0);
		} else {
			return new CompositeTextHover(hovers);
		}
	}

	@Override public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistProcessorRegistry registry = GenericEditorPlugin.getDefault().getContentAssistProcessorRegistry();
		contentAssistant = new ContentAssistant(true);
		contentAssistant.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_BELOW);
		contentAssistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_REMOVE);
		contentAssistant.setAutoActivationDelay(0);
		contentAssistant.enableColoredLabels(true);
		contentAssistant.enableAutoActivation(true);
		this.processors = registry.getContentAssistProcessors(sourceViewer, editor, getContentTypes(sourceViewer));
		if (this.processors.isEmpty()) {
			this.processors.add(new DefaultContentAssistProcessor());
		}
		for (IContentAssistProcessor processor : this.processors) {
			contentAssistant.addContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		}
		if (this.document != null) {
			associateTokenContentTypes(this.document);
		}
		contentAssistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {
			@Override protected IInformationControl doCreateInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
		watchDocument(sourceViewer.getDocument());
		return contentAssistant;
	}

	@Override public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconcilerRegistry registry = GenericEditorPlugin.getDefault().getPresentationReconcilerRegistry();
		List<IPresentationReconciler> reconciliers = registry.getPresentationReconcilers(sourceViewer, editor, getContentTypes(sourceViewer));
		if (!reconciliers.isEmpty()) {
			return reconciliers.get(0);
		}
		return super.getPresentationReconciler(sourceViewer);
	}

	void watchDocument(IDocument document) {
		if (this.document == document) {
			return;
		}
		if (this.document != null) {
			this.document.removeDocumentPartitioningListener(this);
		}
		if (document != null) {
			this.document = document;
			associateTokenContentTypes(document);
			document.addDocumentPartitioningListener(this);
		}
	}

	@Override public void documentPartitioningChanged(IDocument document) {
		associateTokenContentTypes(document);
	}

	private void associateTokenContentTypes(IDocument document) {
		if (contentAssistant == null || this.processors == null) {
			return;
		}
		for (String legalTokenContentType : document.getLegalContentTypes()) {
			for (IContentAssistProcessor processor : this.processors) {
				contentAssistant.addContentAssistProcessor(processor, legalTokenContentType);
			}
		}
	}

	@Override public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		QuickAssistAssistant quickAssistAssistant = new QuickAssistAssistant();
		CompositeQuickAssistProcessor processor = new CompositeQuickAssistProcessor(Arrays.asList(new MarkerResoltionQuickAssistProcessor(), new SpellingCorrectionProcessor()));
		quickAssistAssistant.setQuickAssistProcessor(processor);
		quickAssistAssistant.setRestoreCompletionProposalSize(EditorsPlugin.getDefault().getDialogSettingsSection("quick_assist_proposal_size")); //$NON-NLS-1$
		quickAssistAssistant.setInformationControlCreator(parent -> new DefaultInformationControl(parent, EditorsPlugin.getAdditionalInfoAffordanceString()));
		return quickAssistAssistant;
	}

	@Override public IReconciler getReconciler(ISourceViewer sourceViewer) {
		ReconcilerRegistry registry = GenericEditorPlugin.getDefault().getReconcilerRegistry();
		List<IReconciler> reconcilers = registry.getReconcilers(sourceViewer, editor, getContentTypes(sourceViewer));
		// Fill with highlight reconcilers
		List<IReconciler> highlightReconcilers = registry.getHighlightReconcilers(sourceViewer, editor, getContentTypes(sourceViewer));
		if (!highlightReconcilers.isEmpty()) {
			reconcilers.addAll(highlightReconcilers);
		} else {
			reconcilers.add(new DefaultWordHighlightReconciler());
		}
		// Fill with folding reconcilers
		List<IReconciler> foldingReconcilers = registry.getFoldingReconcilers(sourceViewer, editor, getContentTypes(sourceViewer));
		if (!foldingReconcilers.isEmpty()) {
			reconcilers.addAll(foldingReconcilers);
		} else {
			reconcilers.add(new DefaultFoldingReconciler());
		}

		if (!reconcilers.isEmpty()) {
			return new CompositeReconciler(reconcilers);
		}
		return null;
	}

	@Override public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		AutoEditStrategyRegistry registry = GenericEditorPlugin.getDefault().getAutoEditStrategyRegistry();
		List<IAutoEditStrategy> editStrategies = registry.getAutoEditStrategies(sourceViewer, editor, getContentTypes(sourceViewer));
		if (!editStrategies.isEmpty()) {
			return editStrategies.toArray(new IAutoEditStrategy[editStrategies.size()]);
		}
		return super.getAutoEditStrategies(sourceViewer, contentType);
	}

	@Override protected Map<String, IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("org.eclipse.ui.genericeditor.GenericEditor", editor); //$NON-NLS-1$
		return targets;
	}
}
