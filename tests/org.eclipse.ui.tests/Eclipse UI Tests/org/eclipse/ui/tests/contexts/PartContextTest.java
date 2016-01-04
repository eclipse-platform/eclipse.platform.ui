/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.contexts;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockViewPart;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test that the contexts activated through their local services are only in
 * play when their local service is active.
 *
 * @since 3.2
 */
public class PartContextTest extends UITestCase {
	/**
	 *
	 */
	public static final String PAGE_VIEW_ID = "org.eclipse.ui.tests.contexts.MockPageView";

	/**
	 *
	 */
	private static final String TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor";

	public static final String WINDOW_CONTEXT_ID = "org.eclipse.ui.tests.contexts.WorkbenchWindow";

	public PartContextTest(String name) {
		super(name);
	}

	public void testBasicContextActivation() throws Exception {
		IContextService globalService = getWorkbench()
				.getService(IContextService.class);

		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, false);

		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewPart view = page.showView(MockViewPart5.ID);

		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, true);

		page.hideView(view);
		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, false);
	}

	public void testContextActivation() throws Exception {
		IContextService globalService = getWorkbench()
				.getService(IContextService.class);

		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, false);

		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewPart view = page.showView(MockViewPart5.ID);
		IContextService localService = view.getSite()
				.getService(IContextService.class);

		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, true);
		checkActiveContext(localService, MockViewPart5.PART_CONTEXT_ID, true);

		IViewPart mockView = page.showView(MockViewPart.ID);
		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, false);
		checkActiveContext(localService, MockViewPart5.PART_CONTEXT_ID, false);

		page.activate(view);
		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, true);

		page.activate(mockView);
		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, false);

		page.hideView(mockView);
		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, true);

		page.hideView(view);
		checkActiveContext(globalService, MockViewPart5.PART_CONTEXT_ID, false);
	}

	public void testWindowContextActivation() throws Exception {
		IContextService globalService = getWorkbench()
				.getService(IContextService.class);
		checkActiveContext(globalService, WINDOW_CONTEXT_ID, false);

		IWorkbenchWindow window = openTestWindow();
		assertTrue(forceActive(window.getShell()));

		IContextService localService = window
				.getService(IContextService.class);
		localService.activateContext(WINDOW_CONTEXT_ID);
		checkActiveContext(globalService, WINDOW_CONTEXT_ID, true);

		window.close();
		checkActiveContext(globalService, WINDOW_CONTEXT_ID, false);
	}

	/**
	 * Test context activation while switching through the pages of a pagebook.
	 * Exercises the NestableContextService.
	 *
	 * @throws Exception
	 *             on error
	 */
	public void testPageBookPageContextActivation() throws Exception {
		IContextService globalService = getWorkbench()
				.getService(IContextService.class);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);

		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();

		IViewPart pageView = page.showView(PAGE_VIEW_ID);
		assertNotNull(pageView);

		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);

		IProject proj = FileUtil.createProject("ContextTest");
		IFile test01 = FileUtil.createFile("test01.txt", proj);
		IEditorPart editor01 = page.openEditor(new FileEditorInput(test01),
				TEXT_EDITOR_ID);
		assertNotNull(editor01);
		page.activate(pageView);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);

		IFile test02 = FileUtil.createFile("test02.xml", proj);
		IEditorPart editor02 = page.openEditor(new FileEditorInput(test02),
				TEXT_EDITOR_ID);
		assertNotNull(editor02);
		page.activate(pageView);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, true);

		page.activate(editor01);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);
		page.activate(pageView);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);

		page.activate(editor02);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);
		page.activate(pageView);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, true);
		page.activate(editor02);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);
		page.activate(pageView);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, true);

		page.activate(editor01);
		page.activate(pageView);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, false);

		page.activate(editor01);
		page.closeEditor(editor01, false);
		page.activate(pageView);
		checkActiveContext(globalService, ContextPage.TEST_CONTEXT_ID, true);
	}

	/**
	 * Assert if the contextId is active in the contextService.
	 *
	 * @param contextService
	 * @param contextId
	 * @param isActive
	 */
	private void checkActiveContext(IContextService contextService,
			String contextId, boolean isActive) {
		Collection activeContexts = contextService.getActiveContextIds();
		assertEquals(contextId, isActive, activeContexts.contains(contextId));
	}
}
