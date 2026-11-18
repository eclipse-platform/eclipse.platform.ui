/*******************************************************************************
 * Copyright (c) 2026 Goutam Adwant and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.forms.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.IFormPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link org.eclipse.ui.forms.editor.FormEditor} keeps exactly one
 * entry per tab in its page table.
 */
public class FormEditorPageRegistrationTest {

	private IWorkbenchPage workbenchPage;
	private PageRegistrationFormEditor editor;

	@BeforeEach
	public void setUp() throws PartInitException {
		workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		editor = (PageRegistrationFormEditor) workbenchPage.openEditor(new TestEditorInput(), PageRegistrationFormEditor.ID);
	}

	@AfterEach
	public void tearDown() {
		if (editor != null) {
			workbenchPage.closeEditor(editor, false);
		}
	}

	@Test
	public void addPageRegistersEachFormPageOnce() {
		List<IFormPage> formPages = editor.getFormPages();
		assertEquals(3, editor.getTabCount());
		assertEquals(formPages, editor.getRegisteredPages());
		for (int i = 0; i < formPages.size(); i++) {
			assertPageAt(i, formPages.get(i));
		}
	}

	@Test
	public void removePageKeepsPageTableInSyncWithTabs() {
		List<IFormPage> formPages = editor.getFormPages();
		editor.removePage(1);
		assertEquals(2, editor.getTabCount());
		assertEquals(List.of(formPages.get(0), formPages.get(2)), editor.getRegisteredPages());
		assertPageAt(0, formPages.get(0));
		assertPageAt(1, formPages.get(2));
	}

	@Test
	public void insertedControlPageShiftsFormPages() {
		List<IFormPage> formPages = editor.getFormPages();
		editor.addPage(1, (Control) null);
		assertEquals(4, editor.getTabCount());
		assertEquals(Arrays.asList(formPages.get(0), null, formPages.get(1), formPages.get(2)),
				editor.getRegisteredPages());
		assertPageAt(0, formPages.get(0));
		assertPageAt(2, formPages.get(1));
		assertPageAt(3, formPages.get(2));
	}

	private void assertPageAt(int index, IFormPage formPage) {
		assertEquals(index, formPage.getIndex());
		assertSame(formPage, editor.findPage(formPage.getId()));
		assertSame(formPage, editor.setActivePage(formPage.getId()));
		assertEquals(index, editor.getActivePage());
		assertSame(formPage, editor.getActivePageInstance());
		assertSame(formPage, editor.getSelectedPage());
	}

	private static final class TestEditorInput implements IEditorInput {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return ImageDescriptor.getMissingImageDescriptor();
		}

		@Override
		public String getName() {
			return "FormEditorPageRegistrationTest";
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return getName();
		}
	}
}
