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
package org.eclipse.ui.tests.dynamicplugins;

import java.io.ByteArrayInputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.tests.leaks.LeakTests;

/**
 * @since 3.1
 */
public class EditorTests extends DynamicTestCase {

	private static final String EDITOR_ID = "org.eclipse.newEditor1.newEditor1";
	/**
	 * @param testName
	 */
	public EditorTests(String testName) {
		super(testName);
	}

	@Override
	protected String getExtensionId() {
		return "newEditor1.testDynamicEditorAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_EDITOR;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newEditor1";
	}

	public void testEditorClosure() throws CoreException {
		IWorkbenchWindow window = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		IFile file = getFile();
		getBundle();

		ReferenceQueue queue = new ReferenceQueue();
		IEditorPart part = IDE.openEditor(window.getActivePage(), file, EDITOR_ID);
		WeakReference ref = new WeakReference(part, queue);
        assertNotNull(part);
        part = null; //null the reference

		removeBundle();
		try {
			LeakTests.checkRef(queue, ref);
		} catch (Exception e) {
			fail(e.getMessage());
		}

        assertEquals(0, window.getActivePage().getEditors().length);
	}

	public void testEditorProperties() throws Exception {
		IEditorRegistry registry = WorkbenchPlugin.getDefault().getEditorRegistry();

		assertNull(registry.findEditor(EDITOR_ID));
		getBundle();

		IFile file = getFile("test.xml");
		IContentType contentType = IDE.getContentType(file);
		IEditorDescriptor desc = registry.findEditor(EDITOR_ID);
		assertNotNull(desc);

		testEditorProperties(desc);

		IEditorDescriptor descriptor = registry.getDefaultEditor(file.getName(), contentType);
		// should not get our editor since it is not the default
		assertFalse(desc.equals(descriptor));

		removeBundle();
		assertNull(registry.findEditor(EDITOR_ID));
		try {
			testEditorProperties(desc);
			fail();
		}
		catch (RuntimeException e) {
		}
	}

	/**
	 * @param desc
	 */
	private void testEditorProperties(IEditorDescriptor desc) {
		assertNotNull(desc.getId());
		assertNotNull(desc.getLabel());
		assertNotNull(desc.getImageDescriptor());
	}

	private IFile getFile() throws CoreException {
		return getFile("someFile");
	}

	/**
	 *
	 */
	private IFile getFile(String fileName) throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject testProject = workspace.getRoot().getProject(getName());
        testProject.create(null);
        testProject.open(null);

        IFile iFile = testProject.getFile(fileName);
        iFile.create(new ByteArrayInputStream(new byte[] { '\n' }), true, null);
        return iFile;
	}


	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicEditor";
	}
}
