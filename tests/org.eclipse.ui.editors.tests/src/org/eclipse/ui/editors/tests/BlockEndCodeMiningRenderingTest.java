/*******************************************************************************
 * Copyright (c) 2026 Eclipse Platform contributors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Verifies that the block end code mining is rendered in a running text editor.
 */
public class BlockEndCodeMiningRenderingTest {

	/** Persisted keys of the block end code mining preferences, they must stay stable. */
	private static final String SHOW_BLOCK_END_CODE_MINING= "showBlockEndCodeMining"; //$NON-NLS-1$

	private static final String BLOCK_END_CODE_MINING_MIN_LINES= "blockEndCodeMiningMinLines"; //$NON-NLS-1$

	/** Zero-based line of the closing brace in the test document. */
	private static final int CLOSING_BRACE_LINE= 5;

	private static final String SOURCE= "void method() {\n\ta();\n\tb();\n\tc();\n\td();\n}\n"; //$NON-NLS-1$

	private IPreferenceStore store;

	private File file;

	private AbstractTextEditor editor;

	private StyledText widget;

	private IDocument document;

	@BeforeEach
	void setUp() throws Exception {
		store= EditorsPlugin.getDefault().getPreferenceStore();
		store.setValue(SHOW_BLOCK_END_CODE_MINING, true);
		store.setValue(BLOCK_END_CODE_MINING_MIN_LINES, 5);

		file= File.createTempFile(BlockEndCodeMiningRenderingTest.class.getName(), ".txt"); //$NON-NLS-1$
		Files.write(file.toPath(), SOURCE.getBytes(StandardCharsets.UTF_8));
		editor= (AbstractTextEditor) IDE.openEditorOnFileStore(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), EFS.getStore(file.toURI()));
		document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
		widget= (StyledText) editor.getAdapter(Control.class);
	}

	@AfterEach
	void tearDown() {
		store.setToDefault(SHOW_BLOCK_END_CODE_MINING);
		store.setToDefault(BLOCK_END_CODE_MINING_MIN_LINES);
		editor.close(false);
		file.delete();
		TestUtil.cleanUp();
	}

	@Test
	void miningIsRenderedAtTheClosingBrace() {
		waitForMining();
	}

	@Test
	void miningEchoesTheOpeningLine() throws Exception {
		int width= waitForMining();

		// A longer opening line has to make the echoed text wider.
		document.replace(0, "void method".length(), "void methodWithAConsiderablyLongerName"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue(DisplayHelper.waitForCondition(widget.getDisplay(), 10_000, () -> reservedWidth(CLOSING_BRACE_LINE) > width),
				"the echoed text did not follow the opening line"); //$NON-NLS-1$
	}

	@Test
	void disablingThePreferenceRemovesTheMining() {
		waitForMining();

		store.setValue(SHOW_BLOCK_END_CODE_MINING, false);

		assertTrue(DisplayHelper.waitForCondition(widget.getDisplay(), 10_000, () -> reservedWidth(CLOSING_BRACE_LINE) == 0),
				"code mining still rendered after disabling the preference"); //$NON-NLS-1$
	}

	/** Waits for the mining to appear at the closing brace and returns the space it reserves. */
	private int waitForMining() {
		assertTrue(DisplayHelper.waitForCondition(widget.getDisplay(), 10_000, () -> reservedWidth(CLOSING_BRACE_LINE) > 0),
				"no code mining rendered at the closing brace"); //$NON-NLS-1$
		return reservedWidth(CLOSING_BRACE_LINE);
	}

	/**
	 * Returns the horizontal space the given line reserves through glyph metrics,
	 * which is how a line content code mining is drawn.
	 */
	private int reservedWidth(int line) {
		try {
			int offset= document.getLineOffset(line);
			int end= Math.min(offset + document.getLineLength(line), widget.getCharCount() - 1);
			for (int i= offset; i <= end; i++) {
				StyleRange range= widget.getStyleRangeAtOffset(i);
				if (range != null && range.metrics != null && range.metrics.width > 0) {
					return range.metrics.width;
				}
			}
			return 0;
		} catch (BadLocationException e) {
			return 0;
		}
	}
}
