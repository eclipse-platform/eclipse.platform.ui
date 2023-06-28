/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.compare;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class GenericEditorMergeViewer extends TextMergeViewer {

	private final Set<IContentType> fallbackContentTypes = new LinkedHashSet<>();

	public GenericEditorMergeViewer(Composite parent, CompareConfiguration configuration) {
		super(parent, configuration);
	}

	@Override
	protected SourceViewer createSourceViewer(Composite parent, int textOrientation) {
		SourceViewer res = super.createSourceViewer(parent, textOrientation);
		res.addTextInputListener(new ITextInputListener() {
			@Override
			public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
				fallbackContentTypes
						.addAll(new ExtensionBasedTextViewerConfiguration(null, null).getContentTypes(newInput));
				configureTextViewer(res);
			}

			@Override
			public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
				// Nothing to do
			}
		});
		return res;
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer.getDocument() != null && textViewer instanceof ISourceViewer) {
			ExtensionBasedTextViewerConfiguration configuration = new ExtensionBasedTextViewerConfiguration(null,
					new ChainedPreferenceStore(new IPreferenceStore[] { EditorsUI.getPreferenceStore(),
							GenericEditorPlugin.getDefault().getPreferenceStore() }));
			configuration.setFallbackContentTypes(fallbackContentTypes);
			((ISourceViewer) textViewer).configure(configuration);
		}
	}

}
