/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.parts.tests.util;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPartReference;

/**
 * Utility class for the parts test suite.
 */
public class PartsTestUtil {

    public final static String projectName = "TestProject"; //$NON-NLS-1$

    private final static String fileExtension = ".txt"; //$NON-NLS-1$

    private final static String preFileName = "file"; //$NON-NLS-1$

    private final static String View0 = "org.eclipse.ui.views.ResourceNavigator"; //$NON-NLS-1$

    private final static String View1 = "org.eclipse.ui.views.ProblemView"; //$NON-NLS-1$

    private final static String View2 = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$

    public final static int numOfParts = 3;

    /**
     * Get the workspace.
     * 
     * @return the workspace.
     */
    public static Workspace getWorkspace() {
        return (Workspace) ResourcesPlugin.getWorkspace();
    }

    /**
     * Get the file name for the given index.
     * 
     * @param index
     *            The file index.
     * @return the file's name.
     */
    public static String getFileName(int index) {
        return new String(preFileName + Integer.toString(index) + fileExtension);
    }

    /**
     * Get the project.
     * 
     * @return the project.
     */
    public static IProject getProject() {
        IWorkspace ws = getWorkspace();
        IWorkspaceRoot root = ws.getRoot();
        return root.getProject(projectName);
    }

    /**
     * Open an editor.
     * 
     * @param filename
     *            The file's name.
     */
    public static void openEditor(String filename, IWorkbenchPage page) {
        try {
            IDE.openEditor(page, PartsTestUtil.getProject().getFile(filename),
                    "org.eclipse.ui.DefaultTextEditor", true); //$NON-NLS-1$
        } catch (PartInitException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Get the corresponding view id for the index.
     * 
     * @param index
     *            The view index.
     * @return the view's id.
     */
    public static String getView(int index) {
        switch (index) {
        case 0:
            return View0;
        case 1:
            return View1;
        default:
            return View2;
        }
    }

    /**
     * Zoom the given part.
     * 
     * @param part
     *            The part.
     */
    public static void zoom(IWorkbenchPart part) {
		part.getSite().getPage().toggleZoom(
				part.getSite().getPage().getReference(part));
	}

    /**
     * Determines if the part is zoomed.
     * 
     * @param part
     *            The part.
     * @return true if zoomed.
     * 
     * Logic modeled after WorkbenchPage.toggleZoom(IWorkbenchPartReference ref)
     */
    public static boolean isZoomed(IWorkbenchPart part) {
    	IWorkbenchPage page = part.getSite().getPage();
    	IWorkbenchPartReference ref = page.getReference(part);
    	EModelService modelService = (EModelService) part.getSite().getService(EModelService.class);
    	
    	MUIElement element = getActiveElement(page, modelService, ref);
   		return element.getTags().contains(IPresentationEngine.MAXIMIZED);
    }
    
    // Copied from WorkbenchPage
	private static MUIElement getActiveElement(IWorkbenchPage page, EModelService modelService, IWorkbenchPartReference ref) {
		MUIElement element = null;

		MPerspective curPersp = modelService.getActivePerspective(((WorkbenchPage) page).getWindowModel());
		MPlaceholder eaPH = (MPlaceholder) modelService.find(IPageLayout.ID_EDITOR_AREA, curPersp);
		MPart model = ((WorkbenchPartReference) ref).getModel();
		MPlaceholder placeholder = model.getCurSharedRef();

		switch (modelService.getElementLocation(placeholder == null ? model : placeholder)) {
		case EModelService.IN_ACTIVE_PERSPECTIVE:
			MUIElement parent = placeholder == null ? model.getParent() : placeholder.getParent();
			if (parent instanceof MPartStack) {
				element = parent;
			}
			break;
		case EModelService.IN_SHARED_AREA:
			element = eaPH;
			break;
		}
		return element;
	}

}