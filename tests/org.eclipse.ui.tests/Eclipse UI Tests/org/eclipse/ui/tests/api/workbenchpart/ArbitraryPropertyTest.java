/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.core.runtime.CoreException;

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
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.0
 */
public class ArbitraryPropertyTest extends UITestCase {
	/**
	 *
	 */
	private static final String EDITOR_ID = "org.eclipse.ui.tests.TitleTestEditor";

	/**
	 *
	 */
	private static final String USER_PROP = "org.eclipse.ui.test.user";

	final static String VIEW_ID = "org.eclipse.ui.tests.workbenchpart.OverriddenTitleView";

	/**
	 * @param testName
	 */
	public ArbitraryPropertyTest(String testName) {
		super(testName);
	}

	IWorkbenchWindow window;

	IWorkbenchPage page;

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = openTestWindow();
		page = window.getActivePage();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
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
	};

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
			public Object getAdapter(Class adapter) {
				return null;
			}

			@Override
			public IFile getFile() {
				return null;
			}

			/**
			 * {@inheritDoc}
			 *
			 * @throws CoreException
			 */
			@Override
			public IStorage getStorage() throws CoreException {
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
