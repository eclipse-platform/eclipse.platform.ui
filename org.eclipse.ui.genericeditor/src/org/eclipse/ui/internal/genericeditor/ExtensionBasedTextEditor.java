/*******************************************************************************
 * Copyright (c) 2000, 2017 Red Hat Inc. and others.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.genericeditor.preferences.GenericEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * A generic code editor that is aimed at being extended by contributions. Behavior is supposed to be added via extensions, not by inheritance.
 *
 * @since 1.0
 */
public class ExtensionBasedTextEditor extends TextEditor {

	private static final String CONTEXT_ID = "org.eclipse.ui.genericeditor.genericEditorContext"; //$NON-NLS-1$

	private static final String MATCHING_BRACKETS = GenericEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS;
	private static final String MATCHING_BRACKETS_COLOR = GenericEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
	private static final String HIGHLIGHT_BRACKET_AT_CARET_LOCATION = GenericEditorPreferenceConstants.EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION;
	private static final String ENCLOSING_BRACKETS = GenericEditorPreferenceConstants.EDITOR_ENCLOSING_BRACKETS;

	private ExtensionBasedTextViewerConfiguration configuration;

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
	@Override protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { CONTEXT_ID });
	}

	@Override protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		configuration.watchDocument(getDocumentProvider().getDocument(input));
	}

	@Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ProjectionViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		SourceViewerDecorationSupport support = getSourceViewerDecorationSupport(viewer);
		configureCharacterPairMatcher(viewer, support);
		return viewer;
	}

	@Override public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

		new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors()).install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
	}

	@Override protected void initializeEditor() {
		super.initializeEditor();
		setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] { GenericEditorPreferenceConstants.getPreferenceStore(), EditorsUI.getPreferenceStore() }));
	}

	/**
	 * Configure the {@link ICharacterPairMatcher} from the "org.eclipse.ui.genericeditor.characterPairMatchers" extension point.
	 * 
	 * @param viewer
	 *            the source viewer.
	 * 
	 * @param support
	 *            the source viewer decoration support.
	 */
	private void configureCharacterPairMatcher(ISourceViewer viewer, SourceViewerDecorationSupport support) {
		List<ICharacterPairMatcher> matchers = GenericEditorPlugin.getDefault().getCharacterPairMatcherRegistry().getCharacterPairMatchers(viewer, this, configuration.getContentTypes(viewer));
		if (!matchers.isEmpty()) {
			ICharacterPairMatcher matcher = matchers.get(0);
			support.setCharacterPairMatcher(matcher);
			support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR, HIGHLIGHT_BRACKET_AT_CARET_LOCATION, ENCLOSING_BRACKETS);
		}
	}
}
