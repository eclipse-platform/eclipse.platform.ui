/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sopot Cela, Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * The configuration of the {@link ExtensionBasedTextEditor}. It registers the proxy composite
 * for hover, completion, syntax highlighting, and then those proxy take care of resolving to
 * the right extensions on-demand.
 * 
 * @since 1.0
 */
public final class ExtensionBasedTextViewerConfiguration extends TextSourceViewerConfiguration {

	private IEditorPart editor;
	private Set<IContentType> contentTypes;

	/**
	 * 
	 * @param editor the editor we're creating.
	 * @param preferenceStore the preference store.
	 */
	public ExtensionBasedTextViewerConfiguration(IEditorPart editor, IPreferenceStore preferenceStore) {
		super(preferenceStore);
		this.editor = editor;
	}

	private Set<IContentType> getContentTypes() {
		if (this.contentTypes == null) {
			this.contentTypes = new LinkedHashSet<>();
			this.contentTypes.addAll(Arrays.asList(Platform.getContentTypeManager().findContentTypesFor(editor.getEditorInput().getName())));
			Iterator<IContentType> it = this.contentTypes.iterator();
			while (it.hasNext()) {
				this.contentTypes.add(it.next().getBaseType());
			}
		}
		return this.contentTypes;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		TextHoverRegistry registry= GenericEditorPlugin.getDefault().getHoverRegistry();
		return registry.getAvailableHover(sourceViewer, getContentTypes());
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistProcessorRegistry registry= GenericEditorPlugin.getDefault().getContentAssistProcessorRegistry();
		IContentAssistProcessor processor = new CompositeContentAssistProcessor(registry.getContentAssistProcessors(sourceViewer, getContentTypes()));
		ContentAssistant res= new ContentAssistant();
		res.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_BELOW);
		res.setProposalPopupOrientation(ContentAssistant.PROPOSAL_REMOVE);
		res.enableColoredLabels(true);
		res.enableAutoActivation(true);
		res.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		res.setInformationControlCreator(new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
		return res;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconcilerRegistry registry = GenericEditorPlugin.getDefault().getPresentationReconcilerRegistry();
		List<IPresentationReconciler> reconciliers = registry.getPresentationReconcilers(sourceViewer, getContentTypes());
		if (!reconciliers.isEmpty()) {
			return reconciliers.get(0);
		}
		return super.getPresentationReconciler(sourceViewer);
	}

}
