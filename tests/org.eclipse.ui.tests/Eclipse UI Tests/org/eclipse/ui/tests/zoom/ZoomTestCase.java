/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
package org.eclipse.ui.tests.zoom;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.PreferenceMementoRule;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Assert;
import org.junit.Rule;

public class ZoomTestCase extends UITestCase {
//    protected static final String view2Id = IPageLayout.ID_OUTLINE;

	@Rule
	public final PreferenceMementoRule preferenceMemento = new PreferenceMementoRule();

	protected WorkbenchWindow window;

	protected WorkbenchPage page;

	protected IProject project;

	protected IFile file1, file2;

	protected IEditorPart editor1, editor2, editor3;

	protected IViewPart stackedView1;
	protected IViewPart stackedView2;
	protected IViewPart unstackedView;
	protected IViewPart fastView;

	private IFile file3;

	public ZoomTestCase(String name) {
		super(name);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		window = (WorkbenchWindow) openTestWindow(ZoomPerspectiveFactory.PERSP_ID);
		page = (WorkbenchPage) window.getActivePage();

		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
		preferenceMemento.setPreference(apiStore, IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);

		try {
			project = FileUtil.createProject("IEditorPartTest"); //$NON-NLS-1$
			file1 = FileUtil.createFile("Test1.txt", project); //$NON-NLS-1$
			file2 = FileUtil.createFile("Test2.txt", project); //$NON-NLS-1$
			file3 = FileUtil.createFile("Test3.txt", project); //$NON-NLS-1$
			editor1 = page.openEditor(new FileEditorInput(file1),
					MockEditorPart.ID1);
			editor2 = page.openEditor(new FileEditorInput(file2),
					MockEditorPart.ID2);
			editor3 = page.openEditor(new FileEditorInput(file3),
					MockEditorPart.ID2);

//            DragOperations
//        		.drag(editor3, new EditorAreaDropTarget(new ExistingWindowProvider(window), SWT.RIGHT), false);
		} catch (CoreException e) {
		}

		stackedView1 = findView(ZoomPerspectiveFactory.STACK1_VIEW1);
		stackedView2 = findView(ZoomPerspectiveFactory.STACK1_VIEW2);
//        fastView = findView(ZoomPerspectiveFactory.FASTVIEW1);
	}

	// zooms the given part
	protected void zoom(IWorkbenchPart part) {
		if (part == null) {
			throw new NullPointerException();
		}
		page.activate(part);
		page.toggleZoom(page.getReference(part));
		Assert.assertTrue(page.isPageZoomed());
		Assert.assertTrue(isZoomed(part));
	}

	// open the given file in an editor
	protected void openEditor(IFile file, boolean activate) {
		try {
			if (file == file1) {
				editor1 = IDE.openEditor(page, file, activate);
			}
			if (file == file2) {
				editor2 = IDE.openEditor(page, file, activate);
			}
		} catch (PartInitException e) {
		}
	}

	// show the given view as a regular view
	protected IViewPart showRegularView(String id, int mode) {
		try {
			return page.showView(id, null, mode);
		} catch (PartInitException e) {
		}
		return null;
	}

	protected IViewPart findView(String id) {
		IViewPart view = page.findView(id);
		assertNotNull("View " + id + " not found", view);
		return view;
	}

	protected MPart getPartModel(IWorkbenchPart part) {
		PartSite site = (PartSite) part.getSite();
		return site.getModel();
	}

	protected MUIElement getPartParent(IWorkbenchPart part) {
		MPart partModel = getPartModel(part);

		MUIElement partParent = partModel.getParent();
		if (partParent == null && partModel.getCurSharedRef() != null) {
			partParent = partModel.getCurSharedRef().getParent();
		}

		return partParent;
	}

	// returns whether this part is zoomed
	protected boolean isZoomed(IWorkbenchPart part) {
		if (part == null) {
			return false;
		}

		MUIElement toTest = page.getActiveElement(page.getReference(part));
		if (toTest == null) {
			return false;
		}

		return toTest.getTags().contains(IPresentationEngine.MAXIMIZED);
	}

	/**
	 * Asserts that the given part is zoomed. If the part is null, asserts
	 * that no parts are zoomed.
	 *
	 * @since 3.1
	 */
	protected void assertZoomed(IWorkbenchPart part) {
		if (part == null) {
			Assert.assertFalse("Page should not be zoomed", isZoomed());
		} else {
			// Assert that the part is zoomed
			Assert.assertTrue("Expecting " + partName(part) + " to be zoomed", isZoomed(part));
			// Assert that the page is zoomed (paranoia check)
			Assert.assertTrue("Page should be zoomed", isZoomed());
		}
	}

	/**
	 * Asserts that the given part is active.
	 *
	 * @since 3.1
	 */
	protected void assertActive(IWorkbenchPart part) {
		IWorkbenchPart activePart = page.getActivePart();

		// Assert that the part is active
		Assert.assertTrue("Unexpected active part: expected " + partName(part)
				+ " and found " + partName(activePart), activePart == part);

		// If the part is an editor, assert that the editor is active
		if (part instanceof IEditorPart editorPart) {
			assertActiveEditor(editorPart);
		}
	}

	protected String partName(IWorkbenchPart part) {
		if (part == null) {
			return "null";
		}

		return Util.safeString(part.getTitle());
	}

	protected void assertActiveEditor(IEditorPart part) {
		IWorkbenchPart activeEditor = page.getActiveEditor();

		Assert.assertTrue("Unexpected active editor: expected " + partName(part)
				+ " and found " + partName(activeEditor), activeEditor == part);
	}

	// returns true if the page is not zoomed, false otherwise
	protected boolean isZoomed() {
		return page.isPageZoomed();
	}

	public void close(IWorkbenchPart part) {
		if (part instanceof IViewPart view) {
			page.hideView(view);
		} else if (part instanceof IEditorPart editor) {
			page.closeEditor(editor, false);
		}
	}
}
