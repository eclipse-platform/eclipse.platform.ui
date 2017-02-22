/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertFalse(editorTextWidget.getWordWrap());
		Assert.assertFalse(this.editor.isWordWrapEnabled());
		// Toggle word wrap
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command wordWrapCommand = commandService.getCommand(ITextEditorActionDefinitionIds.WORD_WRAP);
		Assert.assertTrue(wordWrapCommand.isDefined());
		Assert.assertTrue(wordWrapCommand.isEnabled());
		Assert.assertTrue(wordWrapCommand.isHandled());
		PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(wordWrapCommand.getId(), null);
		//
		Assert.assertTrue(editorTextWidget.getWordWrap());
		Assert.assertTrue(this.editor.isWordWrapEnabled());
	}
	
	@Test
	public void testGenericEditorCanShowWhitespaceCharacters() throws Exception {
		this.editor.setFocus();
		// Toggle word wrap
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command wordWrapCommand = commandService.getCommand(ITextEditorActionDefinitionIds.SHOW_WHITESPACE_CHARACTERS);
		Assert.assertTrue(wordWrapCommand.isDefined());
		Assert.assertTrue(wordWrapCommand.isEnabled());
		Assert.assertTrue(wordWrapCommand.isHandled());
		PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(wordWrapCommand.getId(), null);
	}

	@Test
	public void testGenericEditorCanUseBlockSelection() throws Exception {
		this.editor.setFocus();
		Assert.assertFalse(this.editor.isBlockSelectionModeEnabled());
		// Toggle word wrap
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command wordWrapCommand = commandService.getCommand(ITextEditorActionDefinitionIds.BLOCK_SELECTION_MODE);
		Assert.assertTrue(wordWrapCommand.isDefined());
		Assert.assertTrue(wordWrapCommand.isEnabled());
		Assert.assertTrue(wordWrapCommand.isHandled());
		PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(wordWrapCommand.getId(), null);
		//
		Assert.assertTrue(this.editor.isBlockSelectionModeEnabled());
	}
}
