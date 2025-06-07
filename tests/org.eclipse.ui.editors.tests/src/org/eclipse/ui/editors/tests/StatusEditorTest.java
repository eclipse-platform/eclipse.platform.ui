/*******************************************************************************
 * Copyright (c) 2017 Vasili Gulevich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Vasili Gulevich <vasili.gulevich@xored.com> - initial implementation
 *      Andrey Loskutov <loskutov@gmx.de> - polishing and integration
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.PartSite;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;

public class StatusEditorTest {
	private IWorkbenchWindow window;
	private Display display;
	private IWorkbenchPage page;

	@Before
	public void before() throws WorkbenchException {
		window = PlatformUI.getWorkbench().openWorkbenchWindow(null);
		display = window.getShell().getDisplay();
		page = window.getActivePage();
		processEvents();
	}

	@After
	public void after() {
		window.close();
		page = null;
		processEvents();
		TestUtil.cleanUp();
	}

	/*
	 * https://bugs.eclipse.org/bugs/attachment.cgi?bugid=320672
	 *
	 * No exceptions are thrown when a status editor displaying an erroneous status is activated with a mouse click.
	 */
	@Test
	public void doNotThrowOnActivationInStale() throws Exception {
		IEditorPart editor1 = openNonExistentFile(page, new URI("file:/1.txt"));
		openNonExistentFile(page, new URI("file:/2.txt"));
		ILog log = ILog.of(Platform.getBundle("org.eclipse.e4.ui.workbench"));
		List<String> logEvents = new ArrayList<>();
		ILogListener listener = (status, plugin) -> logEvents.add(status.toString());
		log.addLogListener(listener);
		// Clicks are not equivalent to activation from API, so we need this
		// hack to imitate tab clicks.
		CTabFolder folder = (CTabFolder) (((PartSite) editor1.getSite()).getModel().getParent().getWidget());
		try {
			folder.setSelection(0);
			processEvents();
			folder.setSelection(1);
			processEvents();
		} finally {
			log.removeLogListener(listener);
		}
		if(!logEvents.isEmpty()) {
			Assert.assertEquals("Unexpected errors logged", "", logEvents.toString());
		}
	}

	private void processEvents() {
		while (display.readAndDispatch()) {
			//
		}
	}

	private IEditorPart openNonExistentFile(IWorkbenchPage page1, URI uri) throws Exception {
		FileStoreEditorInput input = new FileStoreEditorInput(EFS.getStore(uri));
		TextEditor editor = (TextEditor) page1.openEditor(input, EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);
		Method setMethod= AbstractTextEditor.class.getDeclaredMethod("setDocumentProvider", IDocumentProvider.class);
		setMethod.setAccessible(true);
		setMethod.invoke(editor, new ErrorDocumentProvider(editor.getDocumentProvider()));
		editor.setInput(input);
		processEvents();
		return editor;
	}

	private static class ErrorDocumentProvider extends ForwardingDocumentProvider {
		public ErrorDocumentProvider(IDocumentProvider parent) {
			super("", ignored -> { /**/	}, parent);
		}

		@Override
		public IStatus getStatus(Object element) {
			return new Status(IStatus.ERROR, "org.eclipse.ui.workbench.texteditor.tests",
					0, "This document provider always fails", null);
		}
	}
}
