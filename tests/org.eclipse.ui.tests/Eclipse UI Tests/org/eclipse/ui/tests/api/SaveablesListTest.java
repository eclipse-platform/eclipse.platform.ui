/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestPage;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler.Save;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.5
 */
@RunWith(JUnit4.class)
public class SaveablesListTest extends UITestCase {

	private IWorkbenchWindow fWin;

	private IProject proj;

	private IWorkbenchPage page;

	public SaveablesListTest() {
		super(SaveablesListTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWin = openTestWindow();
		page = openTestPage(fWin);
	}

	@Override
	protected void doTearDown() throws Exception {
		if (proj != null) {
			FileUtil.deleteProject(proj);
			proj = null;
		}
		if (page != null) {
			page.closeAllEditors(false);
			page.close();
		}
		if (fWin != null) {
			fWin.close();
		}
		super.doTearDown();
	}

	@Test
	public void testPreclosePartsWithSaveOptions_SaveAll() throws Throwable {
		int total = 5;
		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];
		List<IWorkbenchPart> parts = new ArrayList<>();

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
		}

		SaveablesList saveablesList = (SaveablesList) PlatformUI.getWorkbench()
				.getService(ISaveablesLifecycleListener.class);

		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(page, files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			mocks[i].setDirty(true);
			callTraces[i] = mocks[i].getCallHistory();
			parts.add(editors[i]);
		}
		Map<IWorkbenchPart, List<Saveable>> saveableMap = saveablesList.getSaveables(parts);
		Map<Saveable, Save> map = new LinkedHashMap<>();
		for (IWorkbenchPart part : parts) {
			List<Saveable> saveables = saveableMap.get(part);
			if (saveables != null) {
				for (Saveable saveable : saveables) {
					map.put(saveable, Save.YES);
				}
			}
		}
		assertEquals((saveablesList.preCloseParts(parts, false, true, fWin, map) != null), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), true);
			callTraces[i].clear();
		}
	}

	@Test
	public void testPreclosePartsWithSaveOptions_DiscardAll() throws Throwable {
		int total = 5;
		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];
		List<IWorkbenchPart> parts = new ArrayList<>();

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
		}

		SaveablesList saveablesList = (SaveablesList) PlatformUI.getWorkbench()
				.getService(ISaveablesLifecycleListener.class);

		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(page, files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			mocks[i].setDirty(true);
			callTraces[i] = mocks[i].getCallHistory();
			parts.add(editors[i]);
		}
		Map<IWorkbenchPart, List<Saveable>> saveableMap = saveablesList.getSaveables(parts);
		Map<Saveable, Save> map = new LinkedHashMap<>();

		for (IWorkbenchPart part : parts) {
			List<Saveable> saveables = saveableMap.get(part);
			if (saveables != null) {
				for (Saveable saveable : saveables) {
					map.put(saveable, Save.NO);
				}
			}
		}
		assertEquals((saveablesList.preCloseParts(parts, false, true, fWin, map) != null), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), false);
		}
	}

	@Test
	public void testPreclosePartsWithSaveOptions_SaveFew() throws Throwable {
		int total = 5;
		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];
		List<IWorkbenchPart> parts = new ArrayList<>();

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
		}

		SaveablesList saveablesList = (SaveablesList) PlatformUI.getWorkbench()
				.getService(ISaveablesLifecycleListener.class);

		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(page, files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			if (i % 2 == 0) {
				mocks[i].setDirty(true);
			}
			callTraces[i] = mocks[i].getCallHistory();
			parts.add(editors[i]);
		}
		Map<IWorkbenchPart, List<Saveable>> saveableMap = saveablesList.getSaveables(parts);
		Map<Saveable, Save> map = new LinkedHashMap<>();
		int j = 0;
		for (IWorkbenchPart part : parts) {
			List<Saveable> saveables = saveableMap.get(part);
			if (saveables != null) {
				for (Saveable saveable : saveables) {
					if (j % 2 == 0) {
						map.put(saveable, Save.YES);
					} else {
						map.put(saveable, Save.NO);
					}
				}
			}
			j++;
		}
		assertEquals((saveablesList.preCloseParts(parts, false, true, fWin, map) != null), true);
		for (int i = 0; i < total; i++) {
			if (i % 2 == 0) {
				assertEquals(callTraces[i].contains("isDirty"), true);
				assertEquals(callTraces[i].contains("doSave"), true);
			} else {
				assertEquals(callTraces[i].contains("isDirty"), true);
				assertEquals(callTraces[i].contains("doSave"), false);
			}

		}
	}
}
