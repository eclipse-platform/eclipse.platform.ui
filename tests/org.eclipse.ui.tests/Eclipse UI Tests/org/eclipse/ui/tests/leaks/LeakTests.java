/*******************************************************************************
 * Copyright (c) 2004, 2021 IBM Corporation and others.
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
 *     Remy Chi Jian Suen (Versant Corporation) - bug 255005
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.leaks;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.tests.api.MockViewPart;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Very simple leak tests to determine if any of our heavy objects are not being
 * disposed properly. Note that the results of these tests will in no way
 * actually assist you in tracking down the leak.
 *
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class LeakTests extends UITestCase {
	private IWorkbenchPage fActivePage;

	private IWorkbenchWindow fWin;

	private IProject proj;

	public LeakTests() {
		super(LeakTests.class.getSimpleName());
	}

	public static void checkRef(ReferenceQueue<?> queue, Reference<?> ref)
			throws IllegalArgumentException, InterruptedException {
		boolean flag = false;
		for (int i = 0; i < 100; i++) {
			System.runFinalization();
			System.gc();
			Thread.yield();
			processEvents();
			Reference<?> checkRef = queue.remove(100);
			if (checkRef != null && checkRef.equals(ref)) {
				flag = true;
				break;
			}
		}

		assertTrue("Reference not enqueued", flag);
	}

	@SuppressWarnings("unchecked")
	private Reference createReference(ReferenceQueue queue, Object object) {
		return new PhantomReference<>(object, queue);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWin = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		fActivePage = fWin.getActivePage();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		fWin = null;
		fActivePage = null;
		if (proj != null) {
			FileUtil.deleteProject(proj);
			proj = null;
		}
	}

	@Test
	public void testSimpleEditorLeak() throws Exception {
		proj = FileUtil.createProject("testEditorLeaks");

		IFile file = FileUtil.createFile("test.mock1", proj);

		ReferenceQueue queue = new ReferenceQueue();
		IEditorPart editor = IDE.openEditor(fActivePage, file);
		assertNotNull(editor);
		Reference ref = createReference(queue, editor);
		try {
			fActivePage.closeEditor(editor, false);
			editor = null;
			checkRef(queue, ref);
		} finally {
			ref.clear();
		}
	}

	@Test
	public void testSimpleViewLeak() throws Exception {
		ReferenceQueue queue = new ReferenceQueue();
		IViewPart view = fActivePage.showView(MockViewPart.ID);
		assertNotNull(view);
		Reference ref = createReference(queue, view);

		try {
			fActivePage.hideView(view);
			view = null;
			checkRef(queue, ref);
		} finally {
			ref.clear();
		}
	}

	@Test
	public void testBug255005ServiceLeak() throws Exception {
		ReferenceQueue queue = new ReferenceQueue();
		IViewPart view = fActivePage.showView(MockViewPart.ID);
		assertNotNull(view);

		// create a job to schedule
		Job doNothingJob = new Job("Does Nothing") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			}
		};

		// retrieve the progress service
		IWorkbenchSiteProgressService service = view
				.getSite().getService(IWorkbenchSiteProgressService.class);
		// schedule it
		service.schedule(doNothingJob);

		// create a reference for our service
		Reference ref = createReference(queue, service);

		// wait for the job  to complete
		doNothingJob.join();

		try {
			// hide the view
			fActivePage.hideView(view);
			// remove our references
			service = null;
			view = null;
			// check for leaks
			checkRef(queue, ref);
		} finally {
			ref.clear();
		}
	}

	@Test
	public void testBug255005SiteLeak() throws Exception {
		ReferenceQueue queue = new ReferenceQueue();
		IViewPart view = fActivePage.showView(MockViewPart.ID);
		assertNotNull(view);

		// create a job to schedule
		Job doNothingJob = new Job("Does Nothing") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			}
		};

		// retrieve the progress service
		IWorkbenchSiteProgressService service = view
				.getSite().getService(IWorkbenchSiteProgressService.class);
		// schedule it
		service.schedule(doNothingJob);

		IWorkbenchPartSite site = view.getSite();
		// create a reference for our site
		Reference ref = createReference(queue, site);

		// wait for the job  to complete
		doNothingJob.join();

		try {
			// hide the view
			fActivePage.hideView(view);
			// remove our references
			service = null;
			site = null;
			view = null;
			// check for leaks
			checkRef(queue, ref);
		} finally {
			ref.clear();
		}
	}

	@Test
	public void testBug265449PropertiesLeak() throws Exception {
		// create a project to be selected by the 'Navigator'
		proj = FileUtil.createProject("projectToSelect");

		// show the 'Navigator'
		IViewPart navigator = fActivePage.showView(IPageLayout.ID_PROJECT_EXPLORER);
		// show the 'Properties' view
		IViewPart propertiesView = fActivePage.showView(IPageLayout.ID_PROP_SHEET);

		// select the project in the 'Navigator', this will cause the 'Properties'
		// view to show something, and create a PropertySheetPage, which was leaking
		navigator.getSite().getSelectionProvider().setSelection(new StructuredSelection(proj));

		// create a reference for the 'Properties' view
		ReferenceQueue queue = new ReferenceQueue();
		Reference ref = createReference(queue, propertiesView);

		try {
			// hide the views
			fActivePage.hideView(navigator);
			fActivePage.hideView(propertiesView);
			// remove our references
			navigator = null;
			propertiesView = null;
			// check for leaks
			checkRef(queue, ref);
		} finally {
			ref.clear();
		}
	}

	@Test
	public void testTextEditorContextMenu() throws Exception {
		proj = FileUtil.createProject("testEditorLeaks");

		IEditorInput input = new NullEditorInput();
		ReferenceQueue queue = new ReferenceQueue();
		IEditorPart editor = IDE.openEditor(fActivePage, input, "org.eclipse.ui.tests.leak.contextEditor");
		assertTrue(editor instanceof ContextEditorPart);
		Reference ref = createReference(queue, editor);

		ContextEditorPart contextMenuEditor = (ContextEditorPart) editor;

		contextMenuEditor.showMenu();
		processEvents();

		contextMenuEditor.hideMenu();
		processEvents();

		try {
			contextMenuEditor = null;
			fActivePage.closeEditor(editor, false);
			editor = null;
			checkRef(queue, ref);
		} finally {
			ref.clear();
		}
	}

	@Test
	public void testSimpleWindowLeak() throws Exception {
		// turn off window management so that we dont have a reference to our
		// new
		// window in the listener
		manageWindows(false);
		try {
			ReferenceQueue queue = new ReferenceQueue();
			IWorkbenchWindow newWindow = openTestWindow();

			assertNotNull(newWindow);
			Reference ref = createReference(queue, newWindow);
			try {
				newWindow.close();
				newWindow = null;
				checkRef(queue, ref);
			} finally {
				ref.clear();
			}
		} finally {
			manageWindows(true);
		}
	}

	/**
	 * Test for leaks if dialog is disposed before it is closed.
	 * This is really testing the framework rather than individual
	 * dialogs, since many dialogs or windows will fail if the shell
	 * is destroyed prior to closing them.
	 * See bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=123296
	 */
	@Test
	public void testDestroyedDialogLeaks() throws Exception {
		ReferenceQueue queue = new ReferenceQueue();
		// Use SaveAs dialog because it's simple to invoke and utilizes
		// framework function such as storing dialog bounds.
		// We are really testing the framework itself here.
		Dialog newDialog = new SaveAsDialog(fWin.getShell());
		newDialog.setBlockOnOpen(false);
		newDialog.open();
		assertNotNull(newDialog);
		Reference ref = createReference(queue, newDialog);
		try {
			// Dispose the window before closing it.
			newDialog.getShell().dispose();
			newDialog.close();
			newDialog = null;
			checkRef(queue, ref);
		} finally {
			ref.clear();
		}
	}
}
