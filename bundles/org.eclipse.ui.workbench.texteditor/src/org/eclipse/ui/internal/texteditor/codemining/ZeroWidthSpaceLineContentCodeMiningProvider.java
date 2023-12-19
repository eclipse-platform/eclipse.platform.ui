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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.source.ISourceViewerExtension5;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ZeroWidthSpaceLineContentCodeMiningProvider extends AbstractCodeMiningProvider
		implements IPropertyChangeListener {

	private static final char ZWSP_SIGN = '\u200b';
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
			if (content.charAt(i) == ZWSP_SIGN) {
				list.add(createCodeMining(i));
			}
		}
		return CompletableFuture.completedFuture(list);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(AbstractTextEditor.PREFERENCE_SHOW_ZWSP)) {
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
		return new ZeroWidthSpaceLineContentCodeMining(new Position(offset, 1), this);
	}

	private void readShowZwspFromStore() {
		showZwsp = store.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_ZWSP);
	}
}
