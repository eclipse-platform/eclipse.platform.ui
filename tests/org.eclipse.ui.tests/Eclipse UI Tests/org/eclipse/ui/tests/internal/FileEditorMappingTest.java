/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class FileEditorMappingTest extends UITestCase {

	private EditorDescriptor textEditor;
	private EditorDescriptor pdeEditor;

	public FileEditorMappingTest(String testName) {
		super(testName);
	}

	protected void doSetUp() throws Exception {
		super.doSetUp();

		textEditor = (EditorDescriptor) IDE.getEditorDescriptor("test.txt");
		pdeEditor = (EditorDescriptor) IDE.getEditorDescriptor("plugin.xml");
	}

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

		List defaultA = new ArrayList();
		defaultA.add(textEditor);
		List defaultB = new ArrayList();
		defaultB.add(pdeEditor);

		mappingA.setDefaultEditors(defaultA);
		mappingB.setDefaultEditors(defaultB);

		assertFalse("Identical except the default editor, should not be equal",
				mappingA.equals(mappingB));
	}

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

		List defaultA = new ArrayList();
		defaultA.add(textEditor);
		List defaultB = new ArrayList();
		defaultB.add(pdeEditor);

		mappingA.setDefaultEditors(defaultA);
		mappingB.setDefaultEditors(defaultB);

		assertFalse("Identical except the default editor, should not be equal",
				mappingA.hashCode() == mappingB.hashCode());
	}

	public void testClone() {
		FileEditorMapping mapping = new FileEditorMapping("txt");		
		assertEquals(mapping, mapping.clone());
		
		mapping.addEditor(textEditor);		
		assertEquals(mapping, mapping.clone());
		
		mapping.removeEditor(textEditor);		
		assertEquals(mapping, mapping.clone());
	}

}
