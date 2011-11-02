/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.FileUtil;

public class IEditorRegistryTest extends TestCase {
	private IEditorRegistry fReg;

	private IProject proj;

	public IEditorRegistryTest(String testName) {
		super(testName);
	}

	public void setUp() {
		fReg = PlatformUI.getWorkbench().getEditorRegistry();
	}

	public void tearDown() {
		if (proj != null) {
			try {
				FileUtil.deleteProject(proj);
			} catch (CoreException e) {
				TestPlugin.getDefault().getLog().log(e.getStatus());
				fail();
			}
		}
	}

	public void testGetFileEditorMappings() {
		assertTrue(ArrayUtil.checkNotNull(fReg.getFileEditorMappings()));
	}

	/**
	 * tests both of the following: IEditorDescriptor[] getEditors(IFile file)
	 * IEditorDescriptor[] getEditors(String filename)
	 */
	public void testGetEditors() throws Throwable {
		IEditorDescriptor[] editors, editors2;
		String[][] maps = { { "a.mock1", MockEditorPart.ID1 },
				{ "b.mock2", MockEditorPart.ID2 } };

		proj = FileUtil.createProject("testProject");

		for (int i = 0; i < maps.length; i++) {
			editors = fReg.getEditors(maps[i][0]);
			assertEquals(editors.length, 1);
			assertEquals(editors[0].getId(), maps[i][1]);
			editors2 = fReg.getEditors(FileUtil.createFile(maps[i][0], proj)
					.getName());
			assertEquals(ArrayUtil.equals(editors, editors2), true);
		}

		// there is no matching editor
		String fileName = IConstants.UnknownFileName[0];
		editors = fReg.getEditors(fileName);
		assertEquals(editors.length, 0);
		editors = fReg
				.getEditors(FileUtil.createFile(fileName, proj).getName());
		assertEquals(editors.length, 0);
	}

	public void testFindEditor() {
		String id = MockEditorPart.ID1;
		IEditorDescriptor editor = fReg.findEditor(id);
		assertEquals(editor.getId(), id);

		// editor is not found
		id = IConstants.FakeID;
		editor = fReg.findEditor(id);
		assertNull(editor);
	}

	/**
	 * getDefaultEditor()
	 */
	public void testGetDefaultEditor() {
		assertNotNull(fReg.getDefaultEditor());
	}

	/**
	 * getDefaultEditor(String fileName)
	 */
	public void testGetDefaultEditor2() {
		IEditorDescriptor editor = fReg.getDefaultEditor("a.mock1");
		assertEquals(editor.getId(), MockEditorPart.ID1);

		// same extension with different name
		IEditorDescriptor editor2 = fReg.getDefaultEditor("b.mock1");
		assertEquals(editor, editor2);

		// editor not found
		assertNull(fReg.getDefaultEditor(IConstants.UnknownFileName[0]));
	}

	/**
	 * getDefaultEditor(IFile file)
	 */
	public void testGetDefaultEditor3() throws Throwable {
		proj = FileUtil.createProject("testProject");

		IFile file = FileUtil.createFile("Whats up.bro", proj);
		String id = MockEditorPart.ID1;
		IDE.setDefaultEditor(file, id);
		IEditorDescriptor editor = IDE.getDefaultEditor(file);
		assertEquals(editor.getId(), id);

		// attach an IFile object with a registered extension to a different
		// editor
		file = FileUtil.createFile("ambush.mock1", proj);
		id = MockEditorPart.ID2;
		IDE.setDefaultEditor(file, id);
		editor = IDE.getDefaultEditor(file);
		assertEquals(editor.getId(), id);

		// a non-registered IFile object with a registered extension
		String name = "what.mock2";
		file = FileUtil.createFile(name, proj);
		editor = IDE.getDefaultEditor(file);
		assertEquals(editor, fReg.getDefaultEditor(name));

		// a non-registered IFile object with an unregistered extension
		name = IConstants.UnknownFileName[0];
		file = FileUtil.createFile(name, proj);
		assertNull(IDE.getDefaultEditor(file));
	}

	/**
	 * getDefaultEditor(String)
	 */
	public void testGetDefaultEditor4_Bug356116() throws Throwable {
		assertNotNull(fReg.getDefaultEditor("test.bug356116"));
	}

	public void testSetDefaultEditor() throws Throwable {
		proj = FileUtil.createProject("testProject");
		IFile file = FileUtil.createFile("good.file", proj);

		String id = MockEditorPart.ID1;
		IDE.setDefaultEditor(file, id);
		IEditorDescriptor editor = IDE.getDefaultEditor(file);
		assertEquals(editor.getId(), id);

		// change the default editor
		id = MockEditorPart.ID2;
		IDE.setDefaultEditor(file, id);
		editor = IDE.getDefaultEditor(file);
		assertEquals(editor.getId(), id);

		// register the default editor with an invalid editor id
		IDE.setDefaultEditor(file, IConstants.FakeID);
		assertNull(IDE.getDefaultEditor(file));
	}

	/**
	 * tests both of the following: getImageDescriptor(IFile file)
	 * getImageDescriptor(String filename)
	 */
	public void testGetImageDescriptor() throws Throwable {
		proj = FileUtil.createProject("testProject");

		ImageDescriptor image1, image2;
		String fileName;

		fileName = "a.mock1";
		IFile file = FileUtil.createFile(fileName, proj);
		image1 = fReg.getImageDescriptor(fileName);
		image2 = fReg.getDefaultEditor(fileName).getImageDescriptor();
		assertEquals(image1, image2);
		// for getImageDescriptor(IFile file)
		assertEquals(image1, fReg.getImageDescriptor(file.getName()));

		// same extension, different file name
		fileName = "b.mock1";
		file = FileUtil.createFile(fileName, proj);
		assertEquals(image1, fReg.getImageDescriptor(fileName));
		assertEquals(image1, fReg.getImageDescriptor(file.getName()));

		// default image
		fileName = "a.nullAndVoid";
		file = FileUtil.createFile(fileName, proj);
		image1 = fReg.getImageDescriptor(fileName);
		image2 = fReg.getImageDescriptor("b.this_is_not_good");
		assertNotNull(image1);
		if (image1 instanceof ExternalProgramImageDescriptor || image2 instanceof ExternalProgramImageDescriptor) {
			return;//If they are external we can't compare them
		}
		assertEquals(image1, image2);
		assertEquals(image2, fReg.getImageDescriptor(file.getName()));
		
	}

	public void testAddPropertyListener() throws Throwable {
		final String METHOD = "propertyChanged";

		// take out mappings from the registry and put them back right away
		// so that the event gets triggered without making change to the
		// registry
		IFileEditorMapping[] src = fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length];
		System.arraycopy(src, 0, maps, 0, src.length);

		MockPropertyListener listener = new MockPropertyListener(fReg,
				IEditorRegistry.PROP_CONTENTS);
		fReg.addPropertyListener(listener);
		CallHistory callTrace = listener.getCallHistory();

		// multiple listener
		MockPropertyListener listener2 = new MockPropertyListener(fReg,
				IEditorRegistry.PROP_CONTENTS);
		fReg.addPropertyListener(listener2);
		CallHistory callTrace2 = listener2.getCallHistory();

		// fire!!
		callTrace.clear();
		callTrace2.clear();
		((EditorRegistry) fReg).setFileEditorMappings(maps);
		assertEquals(callTrace.contains(METHOD), true);
		assertEquals(callTrace2.contains(METHOD), true);

		// add the same listener second time
		fReg.addPropertyListener(listener);

		// fire!!
		callTrace.clear();
		((EditorRegistry) fReg).setFileEditorMappings(maps);
		// make sure the method was called only once
		assertEquals(callTrace.verifyOrder(new String[] { METHOD }), true);

		fReg.removePropertyListener(listener);
		fReg.removePropertyListener(listener2);
	}

	public void testRemovePropertyListener() {
		IFileEditorMapping[] src = fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length];
		System.arraycopy(src, 0, maps, 0, src.length);

		MockPropertyListener listener = new MockPropertyListener(fReg,
				IEditorRegistry.PROP_CONTENTS);
		fReg.addPropertyListener(listener);
		// remove the listener immediately after adding it
		fReg.removePropertyListener(listener);
		CallHistory callTrace = listener.getCallHistory();

		// fire!!
		callTrace.clear();
		((EditorRegistry) fReg).setFileEditorMappings(maps);
		assertEquals(callTrace.contains("propertyChanged"), false);

		// removing the listener that is not registered yet should have no
		// effect
		try {
			fReg.removePropertyListener(listener);
		} catch (Throwable e) {
			fail();
		}
	}

	/**
	 * Assert that the content-type based editor is chosen.
	 */
	public void testEditorContentTypeByFilenameWithContentType() {
		IContentType contentType = Platform.getContentTypeManager()
				.getContentType("org.eclipse.ui.tests.content-type1");
		IEditorDescriptor descriptor = fReg.getDefaultEditor(
				"content-type1.blah", contentType);
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.contentType1Editor-fallback",
				descriptor.getId());
	}

	/**
	 * Assert that the content type based editor is chosen.
	 */
	public void testEditorContentTypeByExtWithContentType() {
		IContentType contentType = Platform.getContentTypeManager()
				.getContentType("org.eclipse.ui.tests.content-type1");
		IEditorDescriptor descriptor = fReg.getDefaultEditor(
				"blah.content-type1", contentType);
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.contentType1Editor-fallback",
				descriptor.getId());
	}

	/**
	 * Assert that in the absence of content type, fall back to the traditional
	 * filename binding.
	 */
	public void testEditorContentTypeByExtWithoutContentType1() {
		IEditorDescriptor descriptor = fReg
				.getDefaultEditor("blah.content-type1");
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.contentType1Editor-fallback",
				descriptor.getId());
	}

	/**
	 * Assert that in the absence of content type, fall back to the traditional
	 * filename binding.
	 */
	public void testEditorContentTypeByFilenameWithoutContentType1() {
		IEditorDescriptor descriptor = fReg
				.getDefaultEditor("content-type1.blah");
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.contentType1Editor-fallback",
				descriptor.getId());
	}

	/**
	 * Assert that in the absence of content type, choose the content type
	 * editor based on content type guess.
	 */
	public void testEditorContentTypeByFilenameWithoutContentType2() {
		IEditorDescriptor descriptor = fReg
				.getDefaultEditor("content-type2.blah");
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.contentType2Editor", descriptor
				.getId());
	}

	/**
	 * Assert that in the absence of content type, choose the content type
	 * editor based on content type guess.
	 */
	public void testEditorContentTypeByExtWithoutContentType2() {
		IEditorDescriptor descriptor = fReg
				.getDefaultEditor("blah.content-type2");
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.contentType2Editor", descriptor
				.getId());
	}

	public void testDefaultedContentTypeEditor() {
		// check the default editor
		IEditorDescriptor descriptor = fReg
				.getDefaultEditor("foo.defaultedContentType");
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.defaultedContentTypeEditor",
				descriptor.getId());

		// check the entire list
		IEditorDescriptor[] descriptors = fReg
				.getEditors("foo.defaultedContentType");
		assertNotNull(descriptors);
		assertEquals(4, descriptors.length);

		assertEquals("org.eclipse.ui.tests.defaultedContentTypeEditor",
				descriptors[0].getId());
		assertEquals("org.eclipse.ui.tests.nondefaultedContentTypeEditor1",
				descriptors[1].getId());
		assertEquals("org.eclipse.ui.tests.nondefaultedContentTypeEditor2",
				descriptors[2].getId());
		assertEquals("org.eclipse.ui.tests.nondefaultedContentTypeEditor3",
				descriptors[3].getId());
	}

	/**
	 * Assert that IEditorRegistry.getEditors() does not return null children
	 * when the default editor has been set to null.
	 */
	public void testNoDefaultEditors() {
		IEditorDescriptor desc = fReg.getDefaultEditor("bogusfile.txt");

		try {
			fReg.setDefaultEditor("*.txt", null);
			IEditorDescriptor[] descriptors = fReg.getEditors("bogusfile.txt");
			for (int i = 0; i < descriptors.length; i++) {
				assertNotNull(descriptors[i]);
			}
		} finally {
			if (desc != null)
				fReg.setDefaultEditor("*.txt", desc.getId());
		}

	}

	public void testSwitchDefaultToExternalBug236104() {
		IEditorDescriptor htmlDescriptor = fReg.getDefaultEditor("test.html");
		assertNotNull(htmlDescriptor);

		IFileEditorMapping[] src = fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length];
		System.arraycopy(src, 0, maps, 0, src.length);
		FileEditorMapping map = null;

		for (int i = 0; i < maps.length; i++) {
			if (maps[i].getExtension().equals("html")) {
				map = maps[i];
				break;
			}
		}

		assertNotNull(map);

		EditorDescriptor replacementDescriptor = EditorDescriptor
				.createForProgram("notepad.exe");

		try {
			map.setDefaultEditor(replacementDescriptor);

			// invoke the same code that FileEditorsPreferencePage does
			((EditorRegistry) fReg).setFileEditorMappings(maps);
			((EditorRegistry) fReg).saveAssociations();
			PrefUtil.savePrefs();

			IEditorDescriptor newDescriptor = fReg
					.getDefaultEditor("test.html");

			assertEquals(replacementDescriptor, newDescriptor);
			assertFalse(replacementDescriptor.equals(htmlDescriptor));
		} finally {
			src = fReg.getFileEditorMappings();
			maps = new FileEditorMapping[src.length];
			System.arraycopy(src, 0, maps, 0, src.length);
			map = null;

			for (int i = 0; i < maps.length; i++) {
				if (maps[i].getExtension().equals("html")) {
					map = maps[i];
					break;
				}
			}

			assertNotNull(map);

			map.setDefaultEditor((EditorDescriptor) htmlDescriptor);
			((EditorRegistry) fReg).setFileEditorMappings(maps);
			((EditorRegistry) fReg).saveAssociations();
			PrefUtil.savePrefs();
		}
	}

	public void testBug308894() throws Throwable {
		FileEditorMapping newMapping = new FileEditorMapping("*.abc");
		assertNull(newMapping.getDefaultEditor());
		
		FileEditorMapping[] src = (FileEditorMapping[]) fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length + 1];
		System.arraycopy(src, 0, maps, 0, src.length);
		maps[maps.length - 1] = newMapping;

		final Throwable[] thrownException = new Throwable[1];
		ILogListener listener = new ILogListener() {
			public void logging(IStatus status, String plugin) {
				Throwable throwable = status.getException();
				if (throwable == null) {
					thrownException[0] = new CoreException(status);
				} else {
					thrownException[0] = throwable;
				}
			}
		};
		Platform.addLogListener(listener);

		try {
			// invoke the same code that FileEditorsPreferencePage does
			((EditorRegistry) fReg).setFileEditorMappings(maps);
			((EditorRegistry) fReg).saveAssociations();
			PrefUtil.savePrefs();
		} finally {
			// undo the change
			((EditorRegistry) fReg).setFileEditorMappings(src);
			((EditorRegistry) fReg).saveAssociations();
			PrefUtil.savePrefs();

			Platform.removeLogListener(listener);
			
			if (thrownException[0] != null) {
				throw thrownException[0];
			}
		}
	}

}
