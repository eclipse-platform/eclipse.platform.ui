/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 448060, 455527
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430988
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import junit.framework.TestCase;

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

public class UIDialogs extends TestCase {
    private static final String PROJECT_SELECTION_MESSAGE = "Select Project";

    private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessages.FilterSelection_message;

    public UIDialogs(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    private IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    public void testAbout() {
        Dialog dialog = null;
        dialog = new AboutDialog(getShell());
        DialogCheck.assertDialog(dialog, this);
    }

    public void testAddProjects() {
		Dialog dialog = new ListSelectionDialog(getShell(), null, ArrayContentProvider.getInstance(),
				new LabelProvider(), PROJECT_SELECTION_MESSAGE);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testCopyMoveProject() {
        IProject dummyProject = ResourcesPlugin.getWorkspace().getRoot()
                .getProject("DummyProject");
        Dialog dialog = new ProjectLocationSelectionDialog(getShell(),
                dummyProject);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testCopyMoveResource() {
        Dialog dialog = new ContainerSelectionDialog(getShell(), null, true,
                "Select Destination");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testEditActionSetsDialog() {
    	fail("CustomizePerspectiveDialog not implemented");
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
//        DialogCheck.assertDialog(dialog, this);
//        if (persp != null) {
//            persp.dispose();
//        }
    }

    public void testEditorSelection() {
        Dialog dialog = new EditorSelectionDialog(getShell());
        DialogCheck.assertDialog(dialog, this);
    }

    /**
     * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public
     * packages. public void testFindReplace() { Dialog dialog =
     * TextEditorTestStub.newFindReplaceDialog( getShell() );
     * DialogCheck.assertDialog(dialog, this); } public void testGotoResource() {
     * Dialog dialog = NavigatorTestStub.newGotoResourceDialog(getShell(), new
     * IResource[0]); DialogCheck.assertDialog(dialog, this); }
     */
    public void testNavigatorFilter() {
		Dialog dialog = new ListSelectionDialog(getShell(), null, ArrayContentProvider.getInstance(),
				new LabelProvider(), FILTER_SELECTION_MESSAGE);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testNewFileType() {
        Dialog dialog = new FileExtensionDialog(getShell());
        DialogCheck.assertDialog(dialog, this);
    }

    public void testProgressInformation() {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        dialog.setBlockOnOpen(true);
        DialogCheck.assertDialog(dialog, this);
    }

    public void testSaveAs() {
        Dialog dialog = new SaveAsDialog(getShell());
        DialogCheck.assertDialog(dialog, this);
    }

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
        DialogCheck.assertDialog(dialog, this);
    }

    // see bug 211350
	public void testLoadNotExistingPerspective() {
    	fail("PerspectiveRegistry.getCustomPersp not implemented");
//    	final String fakePerspectivID = "fakeperspetive";
//		PerspectiveRegistry reg = (PerspectiveRegistry) WorkbenchPlugin
//				.getDefault().getPerspectiveRegistry();
//		try {
//			reg.getCustomPersp(fakePerspectivID);
//		} catch (WorkbenchException e) {
//			assertTrue(e.getStatus().getMessage().indexOf(fakePerspectivID) != -1);
//		}
    }

    public void testSelectPerspective() {
        Dialog dialog = new SelectPerspectiveDialog(getShell(), PlatformUI
                .getWorkbench().getPerspectiveRegistry());
        DialogCheck.assertDialog(dialog, this);
    }

    public void testSelectTypes() {
        Dialog dialog = new TypeFilteringDialog(getShell(), null);
        DialogCheck.assertDialog(dialog, this);
    }

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
        DialogCheck.assertDialog(dialog, this);
    }
    /**
     * 1GJWD2E: ITPUI:ALL - Test classes should not be released in public
     * packages. public void testTaskFilters() { Dialog dialog =
     * TaskListTestStub.newFiltersDialog( getShell() );
     * DialogCheck.assertDialog(dialog, this); }
     */
}
