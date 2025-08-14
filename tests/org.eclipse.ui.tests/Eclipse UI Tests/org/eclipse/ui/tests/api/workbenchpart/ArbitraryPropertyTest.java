/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api.workbenchpart;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ArbitraryPropertyTest {
	private static final String EDITOR_ID = "org.eclipse.ui.tests.TitleTestEditor";

	private static final String USER_PROP = "org.eclipse.ui.test.user";

	static final String VIEW_ID = "org.eclipse.ui.tests.workbenchpart.OverriddenTitleView";

	IWorkbenchWindow window;

	IWorkbenchPage page;

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Before
	public void doSetUp() throws Exception {
		window = openTestWindow();
		page = window.getActivePage();
	}

	static class PropListener implements IPropertyChangeListener {
		String firedProp = null;

		String firedOV = null;

		String firedNV = null;

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			firedProp = event.getProperty();
			firedOV = (String) event.getOldValue();
			firedNV = (String) event.getNewValue();
		}
	}

	@Test
	public void testViewProperties() throws Exception {
		OverriddenTitleView view = (OverriddenTitleView) page.showView(VIEW_ID);
		IViewReference ref = (IViewReference) page.getReference(view);

		PropListener viewListener = new PropListener();
		view.addPartPropertyListener(viewListener);
		PropListener refListener = new PropListener();
		ref.addPartPropertyListener(refListener);

		view.setPartProperty(USER_PROP, "pwebster");

		try {
			assertEquals("pwebster", view.getPartProperty(USER_PROP));
			assertEquals("pwebster", ref.getPartProperty(USER_PROP));

			assertEquals(USER_PROP, viewListener.firedProp);
			assertNull(viewListener.firedOV);
			assertEquals("pwebster", viewListener.firedNV);
			assertEquals(USER_PROP, refListener.firedProp);
			assertNull(refListener.firedOV);
			assertEquals("pwebster", refListener.firedNV);
		} finally {
			view.removePartPropertyListener(viewListener);
			ref.removePartPropertyListener(refListener);
		}
	}

	@Test
	public void testEditorProperties() throws Exception {
		IFileEditorInput input = new IFileEditorInput() {
			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				return null;
			}

			@Override
			public String getName() {
				return "MyInputFile";
			}

			@Override
			public IPersistableElement getPersistable() {
				return null;
			}

			@Override
			public String getToolTipText() {
				return "My Input File";
			}

			@Override
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}

			@Override
			public IFile getFile() {
				return null;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public IStorage getStorage() {
				return null;
			}
		};
		TitleTestEditor editor = (TitleTestEditor) page.openEditor(input,
				EDITOR_ID);
		IEditorReference ref = (IEditorReference) page.getReference(editor);

		PropListener editorListener = new PropListener();
		editor.addPartPropertyListener(editorListener);
		PropListener refListener = new PropListener();
		ref.addPartPropertyListener(refListener);

		editor.setPartProperty(USER_PROP, "pwebster");

		try {
			assertEquals("pwebster", editor.getPartProperty(USER_PROP));
			assertEquals("pwebster", ref.getPartProperty(USER_PROP));

			assertEquals(USER_PROP, editorListener.firedProp);
			assertNull(editorListener.firedOV);
			assertEquals("pwebster", editorListener.firedNV);
			assertEquals(USER_PROP, refListener.firedProp);
			assertNull(refListener.firedOV);
			assertEquals("pwebster", refListener.firedNV);
		} finally {
			editor.removePartPropertyListener(editorListener);
			ref.removePartPropertyListener(refListener);
		}
	}
}
