/*******************************************************************************
 * Copyright (c) 2000, 2021 Red Hat Inc. and others.
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
 *   Angelo Zerr <angelo.zerr@gmail.com> - Bug 538111 - [generic editor] Extension point for ICharacterPairMatcher
 *   Bin Zou <zoubin1011@gmail.com> - Bug 544867 - [Generic Editor] ExtensionBasedTextEditor does not allow its subclasses to setKeyBindingScopes
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.HyperlinkManager;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension2;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcherExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.genericeditor.preferences.GenericEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * A generic code editor that is aimed at being extended by contributions.
 * Behavior is supposed to be added via extensions, not by inheritance.
 *
 * @since 1.0
 */
public class ExtensionBasedTextEditor extends TextEditor {

	private static final String CONTEXT_ID = "org.eclipse.ui.genericeditor.genericEditorContext"; //$NON-NLS-1$
	public static final String GENERIC_EDITOR_ID = "org.eclipse.ui.genericeditor.GenericEditor"; //$NON-NLS-1$

	private static final String MATCHING_BRACKETS = GenericEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS;
	private static final String MATCHING_BRACKETS_COLOR = GenericEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
	private static final String HIGHLIGHT_BRACKET_AT_CARET_LOCATION = GenericEditorPreferenceConstants.EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION;
	private static final String ENCLOSING_BRACKETS = GenericEditorPreferenceConstants.EDITOR_ENCLOSING_BRACKETS;

	private ExtensionBasedTextViewerConfiguration configuration;
	private Image contentTypeImage;
	private ImageDescriptor contentTypeImageDescripter;

	private List<IRegion> fPreviousSelections;
	private ICharacterPairMatcher pairMatcher;

	/**
	 *
	 */
	public ExtensionBasedTextEditor() {
		configuration = new ExtensionBasedTextViewerConfiguration(this, getPreferenceStore());
		setSourceViewerConfiguration(configuration);
	}

	/**
	 * Initializes the key binding scopes of this generic code editor.
	 */
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { CONTEXT_ID });
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		configuration.watchDocument(getDocumentProvider().getDocument(input));
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ProjectionViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),
				styles) {

			@Override
			public void doOperation(int operation) {
				if (HyperlinkManager.OPEN_HYPERLINK == operation) {
					if (!openFirstHyperlink()) {
						MessageDialog.openInformation(getControl().getShell(),
								Messages.TextViewer_open_hyperlink_error_title,
								Messages.TextViewer_open_hyperlink_error_message);
					}
					return;
				}

				super.doOperation(operation);
			}

			private boolean openFirstHyperlink() {
				ITextSelection sel = (ITextSelection) this.getSelection();
				int offset = sel.getOffset();
				if (offset == -1)
					return false;

				IRegion region = new Region(offset, 0);
				IHyperlink hyperlink = findFirstHyperlink(region);
				if (hyperlink != null) {
					hyperlink.open();
					return true;
				}
				return false;
			}

			private IHyperlink findFirstHyperlink(IRegion region) {
				int activeHyperlinkStateMask = getSourceViewerConfiguration().getHyperlinkStateMask(this);
				synchronized (fHyperlinkDetectors) {
					for (IHyperlinkDetector detector : fHyperlinkDetectors) {
						if (detector == null)
							continue;

						if (detector instanceof IHyperlinkDetectorExtension2) {
							int stateMask = ((IHyperlinkDetectorExtension2) detector).getStateMask();
							if (stateMask != -1 && stateMask != activeHyperlinkStateMask)
								continue;
							else if (stateMask == -1 && activeHyperlinkStateMask != fHyperlinkStateMask)
								continue;
						} else if (activeHyperlinkStateMask != fHyperlinkStateMask)
							continue;

						boolean canShowMultipleHyperlinks = fHyperlinkPresenter.canShowMultipleHyperlinks();
						IHyperlink[] hyperlinks = detector.detectHyperlinks(this, region, canShowMultipleHyperlinks);
						if (hyperlinks == null)
							continue;

						Assert.isLegal(hyperlinks.length > 0);

						return hyperlinks[0];
					}
				}
				return null;
			}

		};

		SourceViewerDecorationSupport support = getSourceViewerDecorationSupport(viewer);

		configureCharacterPairMatcher(viewer, support);
		return viewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

		new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors()).install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		computeImage();
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] {
				GenericEditorPreferenceConstants.getPreferenceStore(), EditorsUI.getPreferenceStore() }));
	}

	/**
	 * Configure the {@link ICharacterPairMatcher} from the
	 * "org.eclipse.ui.genericeditor.characterPairMatchers" extension point.
	 *
	 * @param viewer  the source viewer.
	 *
	 * @param support the source viewer decoration support.
	 */
	private void configureCharacterPairMatcher(ISourceViewer viewer, SourceViewerDecorationSupport support) {
		List<ICharacterPairMatcher> matchers = GenericEditorPlugin.getDefault().getCharacterPairMatcherRegistry()
				.getCharacterPairMatchers(viewer, this, configuration.getContentTypes(viewer.getDocument()));
		if (!matchers.isEmpty()) {
			pairMatcher = matchers.get(0);
			support.setCharacterPairMatcher(pairMatcher);
			support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR,
					HIGHLIGHT_BRACKET_AT_CARET_LOCATION, ENCLOSING_BRACKETS);
		}
	}

	@Override
	public Image getTitleImage() {
		return this.contentTypeImage != null ? this.contentTypeImage : super.getTitleImage();
	}

	private void computeImage() {
		contentTypeImageDescripter = GenericEditorPlugin.getDefault().getContentTypeImagesRegistry()
				.getImageDescriptor(getContentTypes());
		if (contentTypeImageDescripter != null) {
			this.contentTypeImage = contentTypeImageDescripter.createImage();
		}
	}

	private IContentType[] getContentTypes() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer != null) {
			return configuration.getContentTypes(sourceViewer.getDocument()).toArray(new IContentType[] {});
		}
		return new IContentType[] {};
	}

	@Override
	public void dispose() {
		if (this.contentTypeImage != null) {
			this.contentTypeImage.dispose();
			this.contentTypeImage = null;
		}
		super.dispose();
	}

	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {
		if (pairMatcher == null || !(pairMatcher instanceof ICharacterPairMatcherExtension)) {
			return;
		}

		ISourceViewer sourceViewer = getSourceViewer();
		IDocument document = sourceViewer.getDocument();
		if (document == null)
			return;

		IRegion selection = getSignedSelection(sourceViewer);
		if (fPreviousSelections == null)
			initializePreviousSelectionList();

		ICharacterPairMatcherExtension fBracketMatcher = (ICharacterPairMatcherExtension) pairMatcher;
		IRegion region = fBracketMatcher.match(document, selection.getOffset(), selection.getLength());
		if (region == null) {
			region = fBracketMatcher.findEnclosingPeerCharacters(document, selection.getOffset(),
					selection.getLength());
			initializePreviousSelectionList();
			fPreviousSelections.add(selection);
		} else {
			if (fPreviousSelections.size() == 2) {
				if (!selection.equals(fPreviousSelections.get(1))) {
					initializePreviousSelectionList();
				}
			} else if (fPreviousSelections.size() == 3) {
				if (selection.equals(fPreviousSelections.get(2)) && !selection.equals(fPreviousSelections.get(0))) {
					IRegion originalSelection = fPreviousSelections.get(0);
					sourceViewer.setSelectedRange(originalSelection.getOffset(), originalSelection.getLength());
					sourceViewer.revealRange(originalSelection.getOffset(), originalSelection.getLength());
					initializePreviousSelectionList();
					return;
				}
				initializePreviousSelectionList();
			}
		}

		if (region == null) {
			setStatusLineErrorMessage(Messages.GotoMatchingBracket_error_noMatchingBracket);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		int offset = region.getOffset();
		int length = region.getLength();

		if (length < 1)
			return;

		int anchor = pairMatcher.getAnchor();
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
		int targetOffset = (ICharacterPairMatcher.RIGHT == anchor) ? offset + 1 : offset + length - 1;

		boolean visible = false;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion = sourceViewer.getVisibleRegion();
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
			visible = (targetOffset >= visibleRegion.getOffset()
					&& targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}

		if (!visible) {
			setStatusLineErrorMessage(Messages.GotoMatchingBracket_error_bracketOutsideSelectedElement);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		int adjustment = 0; // getOffsetAdjustment(document, selection.getOffset() + selection.getLength(),
		// selection.getLength());
		targetOffset += adjustment;
		int direction = (selection.getLength() == 0) ? 0 : ((selection.getLength() > 0) ? 1 : -1);
		if (fPreviousSelections.size() == 1 && direction < 0) {
			targetOffset++;
		}

		if (fPreviousSelections.size() > 0) {
			fPreviousSelections.add(new Region(targetOffset, direction));
		}
		sourceViewer.setSelectedRange(targetOffset, direction);
		sourceViewer.revealRange(targetOffset, direction);
	}

	private void initializePreviousSelectionList() {
		fPreviousSelections = new ArrayList<>(3);
	}

	/*
	 * Copy of
	 * org.eclipse.jface.text.source.MatchingCharacterPainter.getSignedSelection(
	 * ISourceViewer)
	 */
	private static final IRegion getSignedSelection(ISourceViewer sourceViewer) {
		Point viewerSelection = sourceViewer.getSelectedRange();

		StyledText text = sourceViewer.getTextWidget();
		Point selection = text.getSelectionRange();
		if (text.getCaretOffset() == selection.x) {
			viewerSelection.x = viewerSelection.x + viewerSelection.y;
			viewerSelection.y = -viewerSelection.y;
		}

		return new Region(viewerSelection.x, viewerSelection.y);
	}

}
