/*******************************************************************************
 * Copyright (c) 2026 Vasili Gulevich and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vasili Gulevich - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditorMock.Hook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormEditorTest {

	private static final IWorkbench WORKBENCH = PlatformUI.getWorkbench();

	public IEditorInput input = mock();

	@BeforeEach
	public void closeEditors() {
		WORKBENCH.getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}

	@Test
	public void activatePage() throws PartInitException {
		FormEditorMock.hook = FormEditorMock.DEFAULT_HOOK;
		FormEditorMock subject = (FormEditorMock) WORKBENCH.getActiveWorkbenchWindow().getActivePage().openEditor(input,
				FormEditorMock.ID);
		assertEquals("defaultpage", subject.getActivePageInstance().getTitle());
		FormPage page1 = addPage(subject, "page1");
		subject.setActivePage("page1");
		dispatch(subject);
		assertSame(page1, subject.getActivePageInstance());
	}

	@Test
	public void removePage() throws PartInitException {
		FormEditorMock.hook = FormEditorMock.DEFAULT_HOOK;
		FormEditorMock subject = (FormEditorMock) WORKBENCH.getActiveWorkbenchWindow().getActivePage().openEditor(input,
				FormEditorMock.ID);
		dispatch(subject);
		CTabItem[] tabs = subject.leakContainer().getItems();
		assertEquals(1, tabs.length);
		FormPage page1 = addPage(subject, "page1");
		assertSame(page1, subject.findPage("page1"));
		tabs = subject.leakContainer().getItems();
		assertEquals(2, tabs.length);
		subject.removePage(page1.getIndex());
		dispatch(subject);
		assertNull(subject.findPage("page1"));
		tabs = subject.leakContainer().getItems();
		assertEquals(1, tabs.length);
	}

	@Test
	public void disposeTab() throws PartInitException {
		FormEditorMock.hook = FormEditorMock.DEFAULT_HOOK;
		FormEditorMock subject = (FormEditorMock) WORKBENCH.getActiveWorkbenchWindow().getActivePage().openEditor(input,
				FormEditorMock.ID);
		dispatch(subject);
		CTabItem[] tabs = subject.leakContainer().getItems();
		assertEquals(1, tabs.length);
		FormPage page1 = addPage(subject, "page1");
		assertSame(page1, subject.findPage("page1"));
		tabs = subject.leakContainer().getItems();
		assertEquals(2, tabs.length);
		tabs[tabs.length - 1].dispose();
		dispatch(subject);
		tabs = subject.leakContainer().getItems();
		assertEquals(1, tabs.length);
		// BUG https://github.com/eclipse-platform/eclipse.platform.ui/issues/3766
		// assertNull(subject.findPage("page1"));
	}

	@Test
	public void maintainIntegrityOnPageClose() throws PartInitException {
		FormEditorMock.hook = new Hook() {
			@Override
			public void createContainer(FormEditorMock subject, CTabFolder container) {
				Hook.super.createContainer(subject, container);
				container.setUnselectedCloseVisible(true);
			}

			@Override
			public void createItem(FormEditorMock subject, CTabItem item) {
				Hook.super.createItem(subject, item);
				item.setShowClose(true);
			}
		};
		FormEditorMock subject = (FormEditorMock) WORKBENCH.getActiveWorkbenchWindow().getActivePage().openEditor(input,
				FormEditorMock.ID);
		FormPage page1 = addPage(subject, "page1");
		CTabFolder tabFolder = subject.leakContainer();
		CTabItem tab = tabFolder.getItem(1);
		assertTrue(tab.getShowClose());
		assertSame(page1, subject.findPage("page1"));
		Event event = new Event();
		event.type = SWT.MouseDown;
		event.button = 1;
		event.x = tab.getBounds().x + tab.getBounds().width - 9;
		event.y = tab.getBounds().y + tab.getBounds().height/2;
		tabFolder.notifyListeners(event.type, event);
		dispatch(subject);
		event.type = SWT.MouseUp;
		tabFolder.notifyListeners(event.type, event);
		dispatch(subject);
		assertEquals(1, tabFolder.getItems().length);
		// BUG https://github.com/eclipse-platform/eclipse.platform.ui/issues/3766
		// assertNull(subject.findPage("page1"));
	}

	@Test
	public void anEditorIsActiveOnStart() throws PartInitException {
		FormEditorMock.hook = FormEditorMock.DEFAULT_HOOK;
		FormEditorMock subject = (FormEditorMock) WORKBENCH.getActiveWorkbenchWindow().getActivePage().openEditor(input,
				FormEditorMock.ID);
		dispatch(subject);
		// BUG https://github.com/eclipse-platform/eclipse.platform.ui/issues/3764
		// assertNotNull(subject.getActiveEditor());
	}

	@Test
	public void activateEditors() throws PartInitException {
		FormEditorMock.hook = FormEditorMock.DEFAULT_HOOK;
		FormEditorMock subject = (FormEditorMock) WORKBENCH.getActiveWorkbenchWindow().getActivePage().openEditor(input,
				FormEditorMock.ID);
		FormPage page1 = addPage(subject, "page1");
		// BUG https://github.com/eclipse-platform/eclipse.platform.ui/issues/3764
		// assertEveryTabCanBeActive(subject);
	}

	private void assertEveryTabCanBeActive(FormEditorMock subject) {
		dispatch(subject);
		for (IEditorPart part : subject.leakPages()) {
			subject.setActiveEditor(part);
			dispatch(subject);
			assertSame(part, subject.getActiveEditor());
		}
	}

	private void dispatch(FormEditorMock subject) {
		while (subject.leakContainer().getDisplay().readAndDispatch()) {
		}
	}

	private FormPage addPage(FormEditorMock subject, String id) throws PartInitException {
		FormPage page1 = new FormPage(subject, id, id);
		subject.addPage(page1);
		dispatch(subject);
		return page1;
	}

}
