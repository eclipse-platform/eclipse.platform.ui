/*******************************************************************************
 * Copyright (c) 2018 Angelo ZERR.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - [minimap] Initialize minimap view - Bug 535450
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.minimap;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.views.minimap.MinimapPage;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.StatusTextEditor;

/**
 * Test create of Minimap page which is possible only if {@link ITextViewer} can be get from the
 * {@link ITextEditor}.
 * 
 * @since 3.11
 */
public class MinimapPageTest {

	enum TextVieverAdapterKind {
		None, ITextViewer, ITextOperationTarget
	}

	class MyTextEditor extends StatusTextEditor {

		private final TextVieverAdapterKind kind;

		public MyTextEditor(TextVieverAdapterKind kind) {
			this.kind= kind;
		}

		@Override
		public <T> T getAdapter(Class<T> required) {
			switch (kind) {
				case ITextViewer:
					if (ITextViewer.class.equals(required)) {
						Composite parent= new Shell();
						return required.cast(new TextViewer(parent, SWT.NONE));
					}
				case ITextOperationTarget:
					if (ITextOperationTarget.class.equals(required)) {
						Composite parent= new Shell();
						return required.cast(new TextViewer(parent, SWT.NONE));
					}
				case None:
				default:
					return null;
			}
		}
	}

	@Test
	public void createNoneMinimapPage() {
		ITextEditor textEditor= new MyTextEditor(TextVieverAdapterKind.None);
		MinimapPage page= MinimapPage.createMinimapPage(textEditor);
		Assert.assertNull(page);
	}

	@Test
	public void createMinimapPageWithITextViewerAdapter() {
		ITextEditor textEditor= new MyTextEditor(TextVieverAdapterKind.ITextViewer);
		MinimapPage page= MinimapPage.createMinimapPage(textEditor);
		Assert.assertNotNull(page);
	}

	@Test
	public void createMinimapPageWithITextOperationTargetAdapter() {
		ITextEditor textEditor= new MyTextEditor(TextVieverAdapterKind.ITextOperationTarget);
		MinimapPage page= MinimapPage.createMinimapPage(textEditor);
		Assert.assertNotNull(page);
	}
}
