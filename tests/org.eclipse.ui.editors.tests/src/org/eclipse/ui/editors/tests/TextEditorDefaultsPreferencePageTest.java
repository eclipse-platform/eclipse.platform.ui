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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.TextEditorDefaultsPreferencePage;

/**
 * Tests the code mining settings on the Text Editors preference page.
 */
public class TextEditorDefaultsPreferencePageTest {

	/** Persisted key of the block end code mining preference, must stay stable. */
	private static final String SHOW_BLOCK_END_CODE_MINING= "showBlockEndCodeMining"; //$NON-NLS-1$

	private static final String BLOCK_END_CODE_MINING_MIN_LINES= "blockEndCodeMiningMinLines"; //$NON-NLS-1$

	private Shell shell;

	private TextEditorDefaultsPreferencePage page;

	@BeforeEach
	public void setUp() {
		shell= new Shell(PlatformUI.getWorkbench().getDisplay());
		shell.setLayout(new FillLayout());
		page= new TextEditorDefaultsPreferencePage();
		page.init(PlatformUI.getWorkbench());
		page.createControl(shell);
	}

	@AfterEach
	public void tearDown() {
		page.dispose();
		shell.dispose();
	}

	@Test
	public void blockEndCodeMiningDefaultsAreRegistered() {
		IPreferenceStore store= EditorsUI.getPreferenceStore();

		assertFalse(store.getDefaultBoolean(SHOW_BLOCK_END_CODE_MINING));
		assertEquals(20, store.getDefaultInt(BLOCK_END_CODE_MINING_MIN_LINES));
	}

	@Test
	public void codeMiningSettingsShareOneGroup() {
		List<Group> groups= collect(shell, Group.class);

		assertEquals(1, groups.size(), "expected the code mining settings to be the only group on the page"); //$NON-NLS-1$
		Group group= groups.get(0);
		assertFalse(group.getText().isEmpty(), "the group needs a title"); //$NON-NLS-1$
		// The annotation level combo with its maximum count field, and the block end
		// check box with its minimum block size field.
		assertEquals(1, collect(group, Combo.class).size());
		assertEquals(1, collect(group, Button.class).size());
		assertEquals(2, collect(group, Text.class).size());
	}

	@Test
	public void minimumBlockSizeFollowsTheBlockEndCheckBox() {
		Group group= collect(shell, Group.class).get(0);
		Button blockEndCheckBox= collect(group, Button.class).get(0);
		Text minimumBlockSize= collect(group, Text.class).get(1);

		assertFalse(blockEndCheckBox.getSelection(), "the feature is opt-in"); //$NON-NLS-1$
		assertFalse(minimumBlockSize.getEnabled());

		select(blockEndCheckBox, true);
		assertTrue(minimumBlockSize.getEnabled());

		select(blockEndCheckBox, false);
		assertFalse(minimumBlockSize.getEnabled());
	}

	private static void select(Button button, boolean selected) {
		button.setSelection(selected);
		button.notifyListeners(SWT.Selection, new Event());
	}

	private static <T extends Control> List<T> collect(Composite parent, Class<T> type) {
		List<T> found= new ArrayList<>();
		for (Control child : parent.getChildren()) {
			if (type.isInstance(child)) {
				found.add(type.cast(child));
			}
			if (child instanceof Composite composite) {
				found.addAll(collect(composite, type));
			}
		}
		return found;
	}
}
