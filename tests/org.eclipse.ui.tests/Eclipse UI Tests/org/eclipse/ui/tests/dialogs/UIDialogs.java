/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448060, 455527
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430988
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.dialogs.TypeFilteringDialog;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.AboutDialog;
import org.eclipse.ui.internal.dialogs.FileExtensionDialog;
import org.eclipse.ui.internal.dialogs.SavePerspectiveDialog;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.junit.Ignore;
import org.junit.Test;

public class UIDialogs {
	private static final String PROJECT_SELECTION_MESSAGE = "Select Project";

	private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessages.FilterSelection_message;

	private Shell getShell() {
		return DialogCheck.getShell();
	}

	private IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	@Test
	public void testAbout() {
		Dialog dialog = null;
		dialog = new AboutDialog(getShell());
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testAddProjects() {
		Dialog dialog = new ListSelectionDialog(getShell(), null, ArrayContentProvider.getInstance(),
				new LabelProvider(), PROJECT_SELECTION_MESSAGE);
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testCopyMoveProject() {
		IProject dummyProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("DummyProject");
		Dialog dialog = new ProjectLocationSelectionDialog(getShell(),
				dummyProject);
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testCopyMoveResource() {
		Dialog dialog = new ContainerSelectionDialog(getShell(), null, true,
				"Select Destination");
		DialogCheck.assertDialog(dialog);
	}

	@Test
	@Ignore("CustomizePerspectiveDialog not implemented")
	public void testEditActionSetsDialog() {
//        Dialog dialog;
//        Object persp = null;
//        //Test perspective: use current perspective of test case
//        try { /*
//         * fixme: should try to get current perspective, or default;
//         * currently only
//         */
//            WorkbenchWindow window = (WorkbenchWindow) getWorkbench().getActiveWorkbenchWindow();
//            persp = new Perspective((PerspectiveDescriptor) getWorkbench()
//                    .getPerspectiveRegistry().getPerspectives()[0],
//                    (WorkbenchPage) window.getActivePage());
//            dialog = window.createCustomizePerspectiveDialog(persp);
//        } catch (WorkbenchException e) {
//            dialog = null;
//        }
//        DialogCheck.assertDialog(dialog);
//        if (persp != null) {
//            persp.dispose();
//        }
	}

	@Test
	public void testEditorSelection() {
		Dialog dialog = new EditorSelectionDialog(getShell());
		DialogCheck.assertDialog(dialog);
	}

	/**
	 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
	 */
	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testFindReplace() {
//		Dialog dialog = TextEditorTestStub.newFindReplaceDialog(getShell());
//		DialogCheck.assertDialog(dialog);
	}

	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testGotoResource() {
//		Dialog dialog = NavigatorTestStub.newGotoResourceDialog(getShell(), new IResource[0]);
//		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testNavigatorFilter() {
		Dialog dialog = new ListSelectionDialog(getShell(), null, ArrayContentProvider.getInstance(),
				new LabelProvider(), FILTER_SELECTION_MESSAGE);
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testNewFileType() {
		Dialog dialog = new FileExtensionDialog(getShell());
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testProgressInformation() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		dialog.setBlockOnOpen(true);
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testSaveAs() {
		Dialog dialog = new SaveAsDialog(getShell());
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testSavePerspective() {
		PerspectiveRegistry reg = (PerspectiveRegistry) WorkbenchPlugin
				.getDefault().getPerspectiveRegistry();
		// Get persp name.
		SavePerspectiveDialog dialog = new SavePerspectiveDialog(getShell(),
				reg);
		IPerspectiveDescriptor description = reg
				.findPerspectiveWithId(getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getPerspective().getId());
		dialog.setInitialSelection(description);
		DialogCheck.assertDialog(dialog);
	}

	// see bug 211350
	@Test
	@Ignore("PerspectiveRegistry.getCustomPersp not implemented")
	public void testLoadNotExistingPerspective() {
//    	final String fakePerspectivID = "fakeperspetive";
//		PerspectiveRegistry reg = (PerspectiveRegistry) WorkbenchPlugin
//				.getDefault().getPerspectiveRegistry();
//		try {
//			reg.getCustomPersp(fakePerspectivID);
//		} catch (WorkbenchException e) {
//			assertTrue(e.getStatus().getMessage().indexOf(fakePerspectivID) != -1);
//		}
	}

	@Test
	public void testSelectPerspective() {
		Dialog dialog = new SelectPerspectiveDialog(getShell(), PlatformUI
				.getWorkbench().getPerspectiveRegistry());
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testSelectTypes() {
		Dialog dialog = new TypeFilteringDialog(getShell(), null);
		DialogCheck.assertDialog(dialog);
	}

	@Test
	public void testShowView() {

		IWorkbench workbench = getWorkbench();

		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		// Get the view identifier, if any.
		IEclipseContext ctx = workbench.getService(IEclipseContext.class);
		EModelService modelService = workbench.getService(EModelService.class);
		EPartService partService = workbench.getService(EPartService.class);
		MApplication app = workbench.getService(MApplication.class);
		MWindow window = workbench.getService(MWindow.class);
		Dialog dialog = new ShowViewDialog(shell, app, window, modelService, partService, ctx);
		DialogCheck.assertDialog(dialog);
	}

	/**
	 * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public packages.
	 */
	@Test
	@Ignore("1GJWD2E: ITPUI:ALL")
	public void testTaskFilters() {
//		Dialog dialog = TaskListTestStub.newFiltersDialog(getShell());
//		DialogCheck.assertDialog(dialog);
	}

}
