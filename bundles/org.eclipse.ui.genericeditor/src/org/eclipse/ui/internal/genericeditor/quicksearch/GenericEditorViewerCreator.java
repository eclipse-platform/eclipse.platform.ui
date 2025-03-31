/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.quicksearch;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ITextViewerCreator;
import org.eclipse.text.quicksearch.ReconcilingAwareSourceViewerHandle;
import org.eclipse.text.quicksearch.SourceViewerConfigurer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

/**
 * Creates quicksearch text viewer handles that use {@link GenericEditorViewer}.
 */
public class GenericEditorViewerCreator implements ITextViewerCreator {

	@Override
	public ITextViewerHandle createTextViewer(Composite parent) {
		return new GenericEditorSourceViewerHandle(parent);
	}

	private class GenericEditorSourceViewerHandle extends ReconcilingAwareSourceViewerHandle<GenericEditorViewer> {

		public GenericEditorSourceViewerHandle(Composite parent) {
			super(new SourceViewerConfigurer<>(GenericEditorViewer::new), parent, false, false);
		}
	}

	static class GenericEditorViewer extends SourceViewer {

		public GenericEditorViewer(Composite parent, CompositeRuler verticalRuler, int styles) {
			super(parent, verticalRuler, styles);
		}

		@Override
		public void setInput(Object input) {
			unconfigure();

			if (input instanceof IDocument doc) {
				setDocument(doc);
				var configuration = new ExtensionBasedTextViewerConfiguration(null,
						new ChainedPreferenceStore(new IPreferenceStore[] { EditorsUI.getPreferenceStore(),
								GenericEditorPlugin.getDefault().getPreferenceStore() }));
				configure(configuration);
			}
		}

	}

}
