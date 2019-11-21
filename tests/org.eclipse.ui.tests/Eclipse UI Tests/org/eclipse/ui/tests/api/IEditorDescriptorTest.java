/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNotNull;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.junit.Before;
import org.junit.Test;

public class IEditorDescriptorTest {
	IEditorDescriptor[] fEditors;

	@Before
	public void setUp() {
		IFileEditorMapping mapping = (IFileEditorMapping) ArrayUtil
				.pickRandom(PlatformUI.getWorkbench().getEditorRegistry()
						.getFileEditorMappings());
		fEditors = mapping.getEditors();
	}

	@Test
	public void testGetId() throws Throwable {
		for (IEditorDescriptor fEditor : fEditors) {
			assertNotNull(fEditor.getId());
		}
	}

	@Test
	public void testGetImageDescriptor() throws Throwable {
		for (IEditorDescriptor fEditor : fEditors) {
			assertNotNull(fEditor.getImageDescriptor());
		}
	}

	@Test
	public void testGetLabel() throws Throwable {
		for (IEditorDescriptor fEditor : fEditors) {
			assertNotNull(fEditor.getLabel());
		}
	}
}
