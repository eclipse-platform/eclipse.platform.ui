/*******************************************************************************
 * Copyright (c) 2015 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.ide.api;

import java.util.Arrays;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IEditorAssociationOverride;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the <code>IDE</code> API and behaviour.
 *
 * @since 3.5
 */
public class IDETest extends UITestCase {

	public IDETest(String testName) {
		super(testName);
	}

	static EditorDescriptor descriptor1 = EditorDescriptor.createForProgram("echo");
	static EditorDescriptor descriptor2 = EditorDescriptor.createForProgram("ps");

	public void testOverrideEditorAssociations() throws Exception {
		IEditorDescriptor[] descriptors = new IEditorDescriptor[] { descriptor1 };
		IEditorDescriptor[] descriptors2 = IDE.overrideEditorAssociations((IEditorInput) null, null, descriptors);
		assertNoNullEntries(descriptors2);
		assertArrayContains(descriptors2, descriptor1, descriptor2);

		descriptors = new IEditorDescriptor[] { descriptor1 };
		descriptors2 = IDE.overrideEditorAssociations((String) null, null, descriptors);
		assertNoNullEntries(descriptors2);
		assertArrayContains(descriptors2, descriptor1, descriptor2);
	}

	void assertNoNullEntries(Object[] arr) {
		for (Object object : arr) {
			assertNotNull("Null entry found in the array: " + Arrays.toString(arr), object);
		}
	}

	void assertArrayContains(Object[] arr, Object... entries) {
		for (Object entry : entries) {
			boolean found = false;
			for (Object object : arr) {
				if (entry == object) {
					found = true;
					break;
				}
			}
			assertTrue("Entry " + entry + " is missing in the array: " + Arrays.toString(arr), found);
		}
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		TestOverride.returnNullEntries = true;
	}

	@Override
	protected void doTearDown() throws Exception {
		TestOverride.returnNullEntries = false;
		super.doTearDown();
	}

	public static class TestOverride implements IEditorAssociationOverride {
		static boolean returnNullEntries;

		@Override
		public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType,
				IEditorDescriptor[] editorDescriptors) {
			if (returnNullEntries) {
				IEditorDescriptor[] descriptors = new IEditorDescriptor[editorDescriptors.length + 2];
				System.arraycopy(editorDescriptors, 0, descriptors, 2, editorDescriptors.length);
				descriptors[0] = null;
				descriptors[1] = descriptor2;
				return descriptors;
			}
			return editorDescriptors;
		}

		@Override
		public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType,
				IEditorDescriptor[] editorDescriptors) {
			if (returnNullEntries) {
				IEditorDescriptor[] descriptors = new IEditorDescriptor[editorDescriptors.length + 2];
				System.arraycopy(editorDescriptors, 0, descriptors, 2, editorDescriptors.length);
				descriptors[1] = descriptor2;
				descriptors[0] = null;
				return descriptors;
			}
			return editorDescriptors;
		}

		@Override
		public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType,
				IEditorDescriptor editorDescriptor) {
			return editorDescriptor;
		}

		@Override
		public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType,
				IEditorDescriptor editorDescriptor) {
			return editorDescriptor;
		}

	}
}
