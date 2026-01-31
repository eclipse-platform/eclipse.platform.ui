/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class IFileEditorMappingTest {
	private IFileEditorMapping[] fMappings;

	@BeforeEach
	public void setUp() {
		fMappings = PlatformUI.getWorkbench().getEditorRegistry()
				.getFileEditorMappings();
	}

	@Test
	public void testGetName() throws Throwable {
		for (IFileEditorMapping fMapping : fMappings) {
			assertNotNull(fMapping.getName());
		}
	}

	@Test
	public void testGetLabel() throws Throwable {
		String label;
		for (IFileEditorMapping fMapping : fMappings) {
			label = fMapping.getLabel();
			assertNotNull(label);
			assertEquals(label, fMapping.getName() + "."
					+ fMapping.getExtension());
		}
	}

	@Test
	public void testGetExtension() throws Throwable {
		for (IFileEditorMapping fMapping : fMappings) {
			assertNotNull(fMapping.getExtension());
		}
	}

	@Test
	public void testGetEditors() throws Throwable {
		IEditorDescriptor[] editors;

		for (IFileEditorMapping fMapping : fMappings) {
			editors = fMapping.getEditors();
			assertTrue(ArrayUtil.checkNotNull(editors));
		}
	}

	@Test
	public void testGetImageDescriptor() throws Throwable {
		for (IFileEditorMapping fMapping : fMappings) {
			assertNotNull(fMapping.getImageDescriptor());
		}
	}

	//how do i set the default editor?
	@Test
	@Disabled
	public void testGetDefaultEditor() throws Throwable {
		for (IFileEditorMapping fMapping : fMappings) {
			assertNotNull(fMapping.getDefaultEditor());
		}
	}
}
