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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ITextViewerCreator;
import org.eclipse.text.quicksearch.SourceViewerConfigurer;
import org.eclipse.text.quicksearch.SourceViewerHandle;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

/**
 * Creates quicksearch text viewer handles that use
 * {@link GenericEditorViewer2}.
 */
public class GenericEditorViewerCreator implements ITextViewerCreator {

	@Override
	public ITextViewerHandle createTextViewer(Composite parent) {
		return new GenericEditorSourceViewerHandle(parent);
	}

	private class GenericEditorSourceViewerHandle extends SourceViewerHandle<GenericEditorViewer>
			implements ITextPresentationListener {
		private boolean fNewDocumentReconciliation;

		// after focusing on other match in the same document, not all reconciliations
		// are performed again (e.g. LSP reconciliation is done only after setting new
		// input document), so we collect all applied styles to be able to set them
		// after other match focus manually
		private boolean fDoCollectStyles;
		private TextPresentation fMergedStylesPresentation;

		private boolean fScheduleMatchRangesPresentation = true;

		public GenericEditorSourceViewerHandle(Composite parent) {
			super(new SourceViewerConfigurer<>(GenericEditorViewer::new), parent);
			fSourceViewer.addTextPresentationListener(this);
		}

		/*
		 * triggered variable number of times by a) tm4e code (possibly after setInput()
		 * and/or focusMatch() -> fSourceViewer.setVisibleRegion() ), b) lsp4e code
		 * (zero or more times after setInput() only)
		 */
		@Override
		public void applyTextPresentation(TextPresentation textPresentation) {
			if (fDoCollectStyles) {
				StyleRange[] ranges = new StyleRange[textPresentation.getDenumerableRanges()];
				int i = 0;
				for (Iterator<StyleRange> iter = textPresentation.getAllStyleRangeIterator(); iter.hasNext();) {
					ranges[i++] = iter.next();
				}
				mergeStylesToTextPresentation(fMergedStylesPresentation, ranges);
			}
			if (fScheduleMatchRangesPresentation) {
				fScheduleMatchRangesPresentation = false;
				fSourceViewer.getTextWidget().getDisplay().asyncExec(() -> applyMatchRangesTextPresentation());
			}
		}

		private void mergeStylesToTextPresentation(TextPresentation textPresentation, StyleRange[] styleRanges) {
			if (styleRanges != null && styleRanges.length > 0) {
				// mergeStyleRanges() modifies passed ranges so we need to clone
				var ranges = new StyleRange[styleRanges.length];
				for (int i = 0; i < ranges.length; i++) {
					ranges[i] = (StyleRange) styleRanges[i].clone();
				}
				textPresentation.mergeStyleRanges(ranges);
			}
		}

		private void applyMatchRangesTextPresentation() {
			applyMatchesStyles();
			fScheduleMatchRangesPresentation = true;

		}

		@Override
		public void setViewerInput(IDocument document, StyleRange[] matchRangers, IFile file) {
			fNewDocumentReconciliation = true;
			fMergedStylesPresentation = new TextPresentation(1024);
			super.setViewerInput(document, matchRangers, file);
		}

		@Override
		public void focusMatch(IRegion visibleRegion, IRegion revealedRange, int matchLine, IRegion matchRange) {
			if (fNewDocumentReconciliation) {
				fNewDocumentReconciliation = false;
				fDoCollectStyles = true;
				super.focusMatch(visibleRegion, revealedRange, matchLine, matchRange);
			} else {
				fDoCollectStyles = false;
				fScheduleMatchRangesPresentation = false; // temporary disable scheduling match ranges presentation
				super.focusMatch(visibleRegion, revealedRange, matchLine, matchRange);
				// now apply collected styles
				fSourceViewer.changeTextPresentation(fMergedStylesPresentation, false);
				applyMatchRangesTextPresentation(); // also enables scheduling match ranges presentation
				fDoCollectStyles = true;
			}
		}
	}

	static class GenericEditorViewer extends SourceViewer {

		public GenericEditorViewer(Composite parent, CompositeRuler verticalRuler, int styles) {
			super(parent, verticalRuler, styles);
		}

		@Override
		public void refresh() {
			System.out.println("LALALALALALAL"); //$NON-NLS-1$
			// empty implementation
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
