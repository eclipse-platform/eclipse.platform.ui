/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @since 3.11
 *
 */
public class ZoomTest {

	private static IProject project;
	private static IFile file;
	private StyledText text;
	private AbstractTextEditor editor;
	private int initialFontSize;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		file = project.getFile("foo.txt");
		file.create(new ByteArrayInputStream("bar".getBytes()), true, new NullProgressMonitor());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		file.delete(true, new NullProgressMonitor());
		project.delete(true, new NullProgressMonitor());
		TestUtil.cleanUp();
	}

	@Before
	public void setUp() throws Exception {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}

		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		editor = (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(file), desc.getId());
		editor.setFocus();
		text = (StyledText) editor.getAdapter(Control.class);
		// make sure we start from a clean state
		initialFontSize = text.getFont().getFontData()[0].getHeight();
	}

	@After
	public void tearDown() throws Exception {
		editor.close(false);
		editor= null;
	}

	@Test
	public void testZoomCommand() throws Exception {
		int times = 6;
		{
			Command zoomInCommand = PlatformUI.getWorkbench().getService(ICommandService.class)
					.getCommand("org.eclipse.ui.edit.text.zoomIn");
			for (int i = 0; i < times; i++) {
				zoomInCommand.executeWithChecks(new ExecutionEvent(zoomInCommand, Collections.EMPTY_MAP, null, null));
			}
			Assert.assertEquals(this.initialFontSize + 12, text.getFont().getFontData()[0].getHeight());
		}
		{
			Command zoomOutCommand = PlatformUI.getWorkbench().getService(ICommandService.class)
					.getCommand("org.eclipse.ui.edit.text.zoomOut");
			for (int i = 0; i < times; i++) {
				zoomOutCommand.executeWithChecks(new ExecutionEvent(zoomOutCommand, Collections.EMPTY_MAP, null, null));
			}
			Assert.assertEquals(this.initialFontSize, text.getFont().getFontData()[0].getHeight());
		}
	}

}
