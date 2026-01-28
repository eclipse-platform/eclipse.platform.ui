/*******************************************************************************
 * Copyright (c) 2025 SAP S.E. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP S.E. - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.codemining;

import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS;
import static org.eclipse.ui.texteditor.AbstractTextEditor.PREFERENCE_SHOW_ZW_CHARACTERS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.source.ISourceViewerExtension5;

/**
 * A code mining provider that draws zero-width characters (like zero-width
 * spaces) as line content code minings.
 * <p>
 * The code mining is only shown if configured in the preferences.
 * </p>
 */
public class ZeroWidthCharactersLineContentCodeMiningProvider extends AbstractCodeMiningProvider
		implements IPropertyChangeListener {

	private static final char ZW_SPACE = '\u200b';
	private static final char ZW_NON_JOINER = '\u200c';
	private static final char ZW_JOINER = '\u200d';
	private static final char ZW_NO_BREAK_SPACE = '\ufeff';

	private static final Set<Character> ZW_CHARACTERS = Set.of(ZW_SPACE, ZW_NON_JOINER, ZW_JOINER, ZW_NO_BREAK_SPACE);

	private IPreferenceStore store;
	private boolean showZwsp = false;

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		if (store == null) {
			loadStoreAndReadProperty();
		}

		if (!showZwsp) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		List<ICodeMining> list = new ArrayList<>();
		String content = viewer.getDocument().get();
		for (int i = 0; i < content.length(); i++) {
			boolean isZwCharacter = ZW_CHARACTERS.contains(content.charAt(i));
			if (isZwCharacter) {
				list.add(createCodeMining(i));
			}
		}
		return CompletableFuture.completedFuture(list);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (PREFERENCE_SHOW_ZW_CHARACTERS.equals(event.getProperty())
				|| PREFERENCE_SHOW_WHITESPACE_CHARACTERS.equals(event.getProperty())) {
			readShowZwspFromStore();
			updateCodeMinings();
		}
	}

	private void updateCodeMinings() {
		ITextViewer viewer = getAdapter(ITextViewer.class);
		if (viewer instanceof ISourceViewerExtension5 codeMiningExtension) {
			codeMiningExtension.updateCodeMinings();
		}
	}

	@Override
	public void dispose() {
		store.removePropertyChangeListener(this);
		super.dispose();
	}

	private void loadStoreAndReadProperty() {
		store = getAdapter(IPreferenceStore.class);
		readShowZwspFromStore();
		store.addPropertyChangeListener(this);
	}

	private ICodeMining createCodeMining(int offset) {
		return new ZeroWidthCharactersLineContentCodeMining(offset + 1, this, store);
	}

	private void readShowZwspFromStore() {
		showZwsp = store.getBoolean(PREFERENCE_SHOW_ZW_CHARACTERS)
				&& store.getBoolean(PREFERENCE_SHOW_WHITESPACE_CHARACTERS);
	}
}
