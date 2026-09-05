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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

public class FormEditorMock extends FormEditor {
	public static final String ID = "org.eclipse.ui.tests.forms.FormEditorMock";

	public interface Hook {

		default boolean isSaveAsAllowed(FormEditorMock subject) {
			return false;
		}

		default void addPages(FormEditorMock subject) {
			FormPage page1 = new FormPage(subject, "defaultpage", "defaultpage");
			try {
				subject.addPage(page1);
			} catch (PartInitException e) {
				throw new AssertionError(e);
			}
		}

		default void doSave(FormEditorMock subject) {
		}

		default void createContainer(FormEditorMock subject, CTabFolder container) {
		}

		default void createItem(FormEditorMock subject, CTabItem item) {

		}
	}

	public static final Hook DEFAULT_HOOK = new Hook() {
	};

	public static Hook hook = DEFAULT_HOOK;

	@Override
	protected CTabFolder createContainer(Composite parent) {
		CTabFolder result = super.createContainer(parent);
		hook.createContainer(this, result);
		return result;
	}

	@Override
	protected CTabItem createItem(int index, Control control) {
		CTabItem result = super.createItem(index, control);
		hook.createItem(this, result);
		return result;
	}
	@Override
	protected void addPages() {
		hook.addPages(this);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		hook.doSave(this);
	}

	@Override
	public void doSaveAs() {
		hook.doSave(this);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return hook.isSaveAsAllowed(this);
	}

	public CTabFolder leakContainer() {
		return (CTabFolder) getContainer();
	}

	public IEditorPart[] leakPages() {
		return pages.stream().toArray(IEditorPart[]::new);
	}
}
