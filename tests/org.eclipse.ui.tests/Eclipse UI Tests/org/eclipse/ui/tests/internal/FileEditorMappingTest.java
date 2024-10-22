/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Ignore
public class FileEditorMappingTest {

	private EditorDescriptor textEditor;
	private EditorDescriptor pdeEditor;

	@Before
	public void doSetUp() throws Exception {
		textEditor = (EditorDescriptor) IDE.getEditorDescriptor("test.txt");
		pdeEditor = (EditorDescriptor) IDE.getEditorDescriptor("plugin.xml");
	}

	@Test
	public void testEquals() {
		FileEditorMapping mappingA = new FileEditorMapping("txt");
		mappingA.addEditor(textEditor);

		assertEquals(1, mappingA.getEditors().length);
		assertEquals(textEditor, mappingA.getEditors()[0]);

		FileEditorMapping mappingB = new FileEditorMapping("txt");
		assertFalse("No editor set for B, should not be equal", mappingA
				.equals(mappingB));

		mappingA.addEditor(pdeEditor);
		mappingB.addEditor(textEditor);
		mappingB.addEditor(pdeEditor);

		assertEquals(textEditor, mappingA.getDefaultEditor());
		assertEquals(textEditor, mappingB.getDefaultEditor());

		assertEquals(mappingA, mappingB);

		mappingA.setDefaultEditor(textEditor);
		mappingB.setDefaultEditor(pdeEditor);

		assertEquals(textEditor, mappingA.getDefaultEditor());
		assertEquals(pdeEditor, mappingB.getDefaultEditor());
		assertFalse("Identical except the default editor, should not be equal",
				mappingA.equals(mappingB));
	}

	@Test
	public void testEquals2() {
		FileEditorMapping mappingA = new FileEditorMapping("txt");
		FileEditorMapping mappingB = new FileEditorMapping("txt");

		mappingA.addEditor(textEditor);
		mappingA.addEditor(pdeEditor);
		mappingB.addEditor(textEditor);
		mappingB.addEditor(pdeEditor);

		assertEquals(textEditor, mappingA.getDefaultEditor());
		assertEquals(textEditor, mappingB.getDefaultEditor());

		assertEquals(mappingA, mappingB);

		List<IEditorDescriptor> defaultA = new ArrayList<>();
		defaultA.add(textEditor);
		List<IEditorDescriptor> defaultB = new ArrayList<>();
		defaultB.add(pdeEditor);

		mappingA.setDefaultEditors(defaultA);
		mappingB.setDefaultEditors(defaultB);

		assertFalse("Identical except the default editor, should not be equal",
				mappingA.equals(mappingB));
	}

	@Test
	public void testHashCode() {
		FileEditorMapping mappingA = new FileEditorMapping("txt");
		mappingA.addEditor(textEditor);

		assertEquals(1, mappingA.getEditors().length);
		assertEquals(textEditor, mappingA.getEditors()[0]);

		FileEditorMapping mappingB = new FileEditorMapping("txt");
		assertFalse("No editor set for B, should not be equal", mappingA
				.hashCode() == mappingB.hashCode());

		mappingA.addEditor(pdeEditor);
		mappingB.addEditor(textEditor);
		mappingB.addEditor(pdeEditor);

		assertEquals(textEditor, mappingA.getDefaultEditor());
		assertEquals(textEditor, mappingB.getDefaultEditor());

		assertEquals(mappingA.hashCode(), mappingB.hashCode());

		mappingA.setDefaultEditor(textEditor);
		mappingB.setDefaultEditor(pdeEditor);

		assertEquals(textEditor, mappingA.getDefaultEditor());
		assertEquals(pdeEditor, mappingB.getDefaultEditor());
		assertFalse("Identical except the default editor, should not be equal",
				mappingA.hashCode() == mappingB.hashCode());
	}

	@Test
	public void testHashCode2() {
		FileEditorMapping mappingA = new FileEditorMapping("txt");
		FileEditorMapping mappingB = new FileEditorMapping("txt");

		mappingA.addEditor(textEditor);
		mappingA.addEditor(pdeEditor);
		mappingB.addEditor(textEditor);
		mappingB.addEditor(pdeEditor);

		assertEquals(textEditor, mappingA.getDefaultEditor());
		assertEquals(textEditor, mappingB.getDefaultEditor());

		assertEquals(mappingA.hashCode(), mappingB.hashCode());

		List<IEditorDescriptor> defaultA = new ArrayList<>();
		defaultA.add(textEditor);
		List<IEditorDescriptor> defaultB = new ArrayList<>();
		defaultB.add(pdeEditor);

		mappingA.setDefaultEditors(defaultA);
		mappingB.setDefaultEditors(defaultB);

		assertFalse("Identical except the default editor, should not be equal",
				mappingA.hashCode() == mappingB.hashCode());
	}

	@Test
	public void testClone() {
		FileEditorMapping mapping = new FileEditorMapping("txt");
		assertEquals(mapping, mapping.clone());

		mapping.addEditor(textEditor);
		assertEquals(mapping, mapping.clone());

		mapping.removeEditor(textEditor);
		assertEquals(mapping, mapping.clone());
	}

}
