/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 442043
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 527069
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IEditorRegistryTest {
	private IEditorRegistry fReg;

	/**
	 * Number of received log messages with severity error while running a single
	 * test method.
	 */
	private final AtomicInteger loggedErrors = new AtomicInteger();

	/** Listener to count error messages while testing. */
	private final ILogListener errorLogListener = (IStatus status, String plugin) -> {
		if (status.matches(IStatus.ERROR)) {
			System.out.println(status);
			loggedErrors.incrementAndGet();
		}
	};

	private IProject proj;

	@Before
	public void setUp() {
		fReg = PlatformUI.getWorkbench().getEditorRegistry();
	}

	@After
	public void tearDown() throws CoreException {
		if (proj != null) {
			try {
				FileUtil.deleteProject(proj);
			} catch (CoreException e) {
				TestPlugin.getDefault().getLog().log(e.getStatus());
				throw e;
			}
		}
	}

	@Test
	public void testGetFileEditorMappings() {
		assertTrue(ArrayUtil.checkNotNull(fReg.getFileEditorMappings()));
	}

	/**
	 * tests both of the following: IEditorDescriptor[] getEditors(IFile file)
	 * IEditorDescriptor[] getEditors(String filename)
	 */
	@Test
	public void testGetEditors() throws Throwable {
		IEditorDescriptor[] editors, editors2;
		String[][] maps = { { "a.mock1", MockEditorPart.ID1 },
				{ "b.mock2", MockEditorPart.ID2 } };

		proj = FileUtil.createProject("testProject");

		for (String[] map : maps) {
			editors = fReg.getEditors(map[0]);
			assertEquals(editors.length, 1);
			assertEquals(editors[0].getId(), map[1]);
			editors2 = fReg.getEditors(FileUtil.createFile(map[0], proj)
					.getName());
			assertArrayEquals(editors, editors2);
		}

		// there is no matching editor
		String fileName = IConstants.UnknownFileName[0];
		editors = fReg.getEditors(fileName);
		assertEquals(editors.length, 0);
		editors = fReg
				.getEditors(FileUtil.createFile(fileName, proj).getName());
		assertEquals(editors.length, 0);
	}

	@Test
	public void testFindEditor() {
		String id = MockEditorPart.ID1;
		IEditorDescriptor editor = fReg.findEditor(id);
		assertEquals(editor.getId(), id);

		// editor is not found
		id = IConstants.FakeID;
		editor = fReg.findEditor(id);
		assertNull(editor);
	}

	/** Ensures that OS external editors can be found using {@link IEditorRegistry#findEditor(String)} */
	@Test
	public void testFindExternalEditor() {
		IEditorDescriptor[] sortedEditorsFromOS = ((EditorRegistry) fReg).getSortedEditorsFromOS();
		assertTrue("The OS should have at least one external editor", sortedEditorsFromOS.length > 1);

		List<EditorDescriptor> list = Arrays.asList(sortedEditorsFromOS).stream().map(x -> (EditorDescriptor) x)
				.toList();
		Map<String, List<Program>> map = list.stream().collect(Collectors.groupingBy(IEditorDescriptor::getId,
				Collectors.mapping(EditorDescriptor::getProgram, Collectors.toList())));

		assertFalse(map.isEmpty());

		// cycle through external editors
		for (Entry<String, List<Program>> entry : map.entrySet()) {
			String id = entry.getKey();
			// find external editor from registry
			EditorDescriptor found = (EditorDescriptor) fReg.findEditor(id);
			assertNotNull("Found editor must not be null", found);
			List<Program> candidates = entry.getValue();
			boolean contains = candidates.contains(found.getProgram());
			assertTrue("No matching external editor found for id: " + id + ", list: " + candidates + ", expected: "
					+ found + " / " + found.getProgram(), contains);
		}
	}

	/**
	 * getDefaultEditor()
	 */
	@Test
	public void testGetDefaultEditor() {
		assertNotNull(fReg.getDefaultEditor());
	}

	/**
	 * getDefaultEditor(String fileName)
	 */
	@Test
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
	@Test
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
	@Test
	public void testGetDefaultEditor4_Bug356116() throws Throwable {
		assertNotNull(fReg.getDefaultEditor("test.bug356116"));
	}

	@Test
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
	@Test
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

	@Test
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

	@Test
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
		fReg.removePropertyListener(listener);
	}

	/**
	 * Assert that the content-type based editor is chosen.
	 */
	@Test
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
	@Test
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
	 * Assert that the "right" content type based editor (depending on user choice)
	 * is chosen for multiple editors linked to same content type.
	 */
	@Test
	public void testGetDefaultEditorForContentType() {
		IContentType contentType = Platform.getContentTypeManager()
				.getContentType("org.eclipse.ui.tests.content-type3");
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		final String key = IPreferenceConstants.DEFAULT_EDITOR_FOR_CONTENT_TYPE + contentType.getId();
		final String defaultValue = store.getString(key);
		final String fileName = "content-type3.blah";
		final String defaultDefaultEditor = "org.eclipse.ui.tests.contentType3Editor1";
		IEditorDescriptor descriptor = fReg.getDefaultEditor(fileName, contentType);
		assertNotNull(descriptor);
		try {
			// nothing in preferences: just first one found in registry expected
			assertEquals("Should be empty", IPreferenceStore.STRING_DEFAULT_DEFAULT, defaultValue);
			assertEquals("Wrong editor chosen with no default configured", defaultDefaultEditor, descriptor.getId());

			// select third one
			String defaultEditor = "org.eclipse.ui.tests.contentType3Editor3";
			store.setValue(key, defaultEditor);
			descriptor = fReg.getDefaultEditor(fileName, contentType);
			assertEquals("Wrong editor chosen", defaultEditor, descriptor.getId());

			// select first one again
			defaultEditor = "org.eclipse.ui.tests.contentType3Editor1";
			store.setValue(key, defaultEditor);
			descriptor = fReg.getDefaultEditor(fileName, contentType);
			assertEquals("Wrong editor chosen", defaultEditor, descriptor.getId());

			// select second one
			defaultEditor = "org.eclipse.ui.tests.contentType3Editor2";
			store.setValue(key, defaultEditor);
			descriptor = fReg.getDefaultEditor(fileName, contentType);
			assertEquals("Wrong editor chosen", defaultEditor, descriptor.getId());

			// reset to defaults
			store.setValue(key, defaultValue);
			descriptor = fReg.getDefaultEditor(fileName, contentType);
			assertEquals("Wrong editor chosen with no default configured", defaultDefaultEditor, descriptor.getId());
		} finally {
			store.setValue(key, defaultValue);
		}
	}

	/**
	 * Assert that in the absence of content type, fall back to the traditional
	 * filename binding.
	 */
	@Test
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
	@Test
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
	@Test
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
	@Test
	public void testEditorContentTypeByExtWithoutContentType2() {
		IEditorDescriptor descriptor = fReg
				.getDefaultEditor("blah.content-type2");
		assertNotNull(descriptor);
		assertEquals("org.eclipse.ui.tests.contentType2Editor", descriptor
				.getId());
	}

	@Test
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
	@Test
	public void testNoDefaultEditors() {
		IEditorDescriptor desc = fReg.getDefaultEditor("bogusfile.txt");

		try {
			fReg.setDefaultEditor("*.txt", null);
			IEditorDescriptor[] descriptors = fReg.getEditors("bogusfile.txt");
			for (IEditorDescriptor descriptor : descriptors) {
				assertNotNull(descriptor);
			}
		} finally {
			if (desc != null) {
				fReg.setDefaultEditor("*.txt", desc.getId());
			}
		}

	}

	@Test
	public void testSwitchDefaultToExternalBug236104() {
		IEditorDescriptor editor = fReg.getDefaultEditor("a.mock1");
		assertNotNull("Default editor should not be null", editor);
		assertEquals(editor.getId(), MockEditorPart.ID1);

		IFileEditorMapping[] src = fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length];
		System.arraycopy(src, 0, maps, 0, src.length);
		FileEditorMapping map = null;

		for (FileEditorMapping map2 : maps) {
			if (map2.getExtension().equals("mock1")) {
				map = map2;
				break;
			}
		}

		assertNotNull("Parameter map should not be null", map);

		EditorDescriptor replacementDescriptor = EditorDescriptor
				.createForProgram("notepad.exe");

		try {
			map.setDefaultEditor(replacementDescriptor);

			// invoke the same code that FileEditorsPreferencePage does
			((EditorRegistry) fReg).setFileEditorMappings(maps);
			((EditorRegistry) fReg).saveAssociations();
			PrefUtil.savePrefs();

			IEditorDescriptor newDescriptor = fReg
					.getDefaultEditor("a.mock1");

			assertEquals(
					"Parameter replaceDescriptor should be the same as parameter new Descriptor",
					replacementDescriptor, newDescriptor);
			assertFalse(
					"Parameter replaceDescriptor should not be equals to a.mock1 Descriptor",
					replacementDescriptor.equals(editor));
		} finally {
			src = fReg.getFileEditorMappings();
			maps = new FileEditorMapping[src.length];
			System.arraycopy(src, 0, maps, 0, src.length);
			map = null;

			for (FileEditorMapping map2 : maps) {
				if (map2.getExtension().equals("mock1")) {
					map = map2;
					break;
				}
			}

			assertNotNull(
					"Parameter map should not be null before setting the default editor",
					map);

			map.setDefaultEditor(editor);
			((EditorRegistry) fReg).setFileEditorMappings(maps);
			((EditorRegistry) fReg).saveAssociations();
			PrefUtil.savePrefs();
		}
	}

	@Test
	public void testBug308894() throws Throwable {
		FileEditorMapping newMapping = new FileEditorMapping("*.abc");
		assertNull(newMapping.getDefaultEditor());

		FileEditorMapping[] src = (FileEditorMapping[]) fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length + 1];
		System.arraycopy(src, 0, maps, 0, src.length);
		maps[maps.length - 1] = newMapping;
		// add error listener
		loggedErrors.set(0);
		Platform.addLogListener(errorLogListener);

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

			Platform.removeLogListener(errorLogListener);
			assertEquals("Test triggered errors during preference save.", 0, loggedErrors.get());
		}
	}

	@Test
	public void testRemoveExtension() {
		FileEditorMapping mapping1 = new FileEditorMapping(null, "testRemoveExtension1");
		FileEditorMapping mapping2 = new FileEditorMapping(null, "testRemoveExtension2");
		EditorDescriptor editor = EditorDescriptor.createForProgram("notepad.exe");
		mapping1.addEditor(editor);
		mapping2.addEditor(editor);
		FileEditorMapping[] src = (FileEditorMapping[]) fReg.getFileEditorMappings();
		FileEditorMapping[] maps = new FileEditorMapping[src.length + 2];
		System.arraycopy(src, 0, maps, 0, src.length);
		maps[maps.length - 1] = mapping1;
		maps[maps.length - 2] = mapping2;
		try {
			((EditorRegistry) fReg).setFileEditorMappings(maps);
			((EditorRegistry) fReg).saveAssociations();

			IEditorDescriptor editor1 = fReg.getDefaultEditor("a.testRemoveExtension1");
			assertEquals(editor, editor1);
			IEditorDescriptor editor2 = fReg.getDefaultEditor("a.testRemoveExtension2");
			assertEquals(editor, editor2);

			EditorDescriptor[] descriptors = new EditorDescriptor[] { editor };
			((EditorRegistry) fReg).removeExtension(null, descriptors);

			editor1 = fReg.getDefaultEditor("a.testRemoveExtension1");
			assertNull(editor1);
			editor2 = fReg.getDefaultEditor("a.testRemoveExtension2");
			assertNull(editor2);
			IFileEditorMapping[] mappings = fReg.getFileEditorMappings();
			assertEquals(src.length, mappings.length);
		} finally {
			((EditorRegistry) fReg).setFileEditorMappings(src);
			((EditorRegistry) fReg).saveAssociations();
		}
	}

	@Test
	public void testAddContentTypeBinding_bug502837() {
		IEditorDescriptor[] editors = fReg.getEditors("blah.bug502837");
		assertEquals(1, editors.length);
		assertEquals(MockEditorPart.ID1, editors[0].getId());
	}

	@Test
	public void testRemoveContentType_bug520239() throws CoreException {
		ContentTypeManager contentTypeManager = (ContentTypeManager) Platform.getContentTypeManager();
		IContentType contentType = contentTypeManager.addContentType("bug520239", "bug520239", null);
		contentType.addFileSpec("bug520239", IContentType.FILE_EXTENSION_SPEC);
		assertArrayEquals("No editor should be bound by default", new IEditorDescriptor[0],
				fReg.getEditors("blah.bug520239"));

		IEditorDescriptor anEditor = ((EditorRegistry) fReg).getSortedEditorsFromPlugins()[0];
		((EditorRegistry) fReg).addUserAssociation(contentType, anEditor);
		assertArrayEquals("Missing editor association", new IEditorDescriptor[] { anEditor },
				fReg.getEditors("blah.bug520239"));

		contentTypeManager.removeContentType(contentType.getId());
		assertArrayEquals("No editor should be bound after contenttype removal", new IEditorDescriptor[0],
				fReg.getEditors("blah.bug520239"));
	}
}
