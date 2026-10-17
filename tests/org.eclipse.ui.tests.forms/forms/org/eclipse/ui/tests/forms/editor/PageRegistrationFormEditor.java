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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * Form editor with a lazily created, an eagerly created and another lazily
 * created form page, exposing its page table for inspection.
 */
public class PageRegistrationFormEditor extends FormEditor {

	public static final String ID = "org.eclipse.ui.tests.forms.editor.PageRegistrationFormEditor";

	private final List<IFormPage> formPages = new ArrayList<>();

	@Override
	protected void addPages() {
		try {
			addFormPage(new FormPage(this, "first", "First"));
			FormPage eager = new FormPage(this, "second", "Second");
			eager.createPartControl(getContainer());
			addFormPage(eager);
			addFormPage(new FormPage(this, "third", "Third"));
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}
	}

	private void addFormPage(IFormPage page) throws PartInitException {
		addPage(page);
		formPages.add(page);
	}

	List<IFormPage> getFormPages() {
		return formPages;
	}

	List<Object> getRegisteredPages() {
		return new ArrayList<>(pages);
	}

	int getTabCount() {
		return getPageCount();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
