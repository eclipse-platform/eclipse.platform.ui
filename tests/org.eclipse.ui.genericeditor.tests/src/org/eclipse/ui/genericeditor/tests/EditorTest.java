/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.commands.Command;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public class EditorTest extends AbstratGenericEditorTest{

	@Test
	public void testGenericEditorHasWordWrap() throws Exception {
		this.editor.setFocus();
		final StyledText editorTextWidget = (StyledText) this.editor.getAdapter(Control.class);
		assertFalse(editorTextWidget.getWordWrap());
		assertFalse(this.editor.isWordWrapEnabled());
		// Toggle word wrap
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command wordWrapCommand = commandService.getCommand(ITextEditorActionDefinitionIds.WORD_WRAP);
		assertTrue(wordWrapCommand.isDefined());
		assertTrue(wordWrapCommand.isEnabled());
		assertTrue(wordWrapCommand.isHandled());
		PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(wordWrapCommand.getId(), null);
		//
		assertTrue(editorTextWidget.getWordWrap());
		assertTrue(this.editor.isWordWrapEnabled());
	}

	@Test
	public void testGenericEditorCanShowWhitespaceCharacters() throws Exception {
		this.editor.setFocus();
		// Toggle word wrap
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command wordWrapCommand = commandService.getCommand(ITextEditorActionDefinitionIds.SHOW_WHITESPACE_CHARACTERS);
		assertTrue(wordWrapCommand.isDefined());
		assertTrue(wordWrapCommand.isEnabled());
		assertTrue(wordWrapCommand.isHandled());
		PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(wordWrapCommand.getId(), null);
	}

	@Test
	public void testGenericEditorCanUseBlockSelection() throws Exception {
		this.editor.setFocus();
		assertFalse(this.editor.isBlockSelectionModeEnabled());
		// Toggle word wrap
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command wordWrapCommand = commandService.getCommand(ITextEditorActionDefinitionIds.BLOCK_SELECTION_MODE);
		assertTrue(wordWrapCommand.isDefined());
		assertTrue(wordWrapCommand.isEnabled());
		assertTrue(wordWrapCommand.isHandled());
		PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(wordWrapCommand.getId(), null);
		//
		assertTrue(this.editor.isBlockSelectionModeEnabled());
	}
}
