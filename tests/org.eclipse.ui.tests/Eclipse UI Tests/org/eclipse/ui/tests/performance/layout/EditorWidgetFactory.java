/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.layout;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.performance.UIPerformanceTestSetup;

/**
 * @since 3.1
 */
public class EditorWidgetFactory extends TestWidgetFactory {

    private String editorId;
    private String filename;
    private Composite ctrl;
    
    public EditorWidgetFactory(String filename) {
        this.filename = filename;
        this.editorId = null;
    }
    
    public EditorWidgetFactory(String filename, String editorId) {
        this.filename = filename;
        this.editorId = editorId;
    }
    
    public static Composite getControl(IEditorPart part) {
		EditorSite site = (EditorSite)part.getSite();
		
		PartPane pane = site.getPane();
        
		return (Composite)pane.getControl();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#getName()
     */
    public String getName() {
        return "editor " + filename + (editorId != null ? editorId : "");
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#init()
     */
    public void init() throws CoreException, WorkbenchException {
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
        final IPerspectiveDescriptor perspective1 = registry.findPerspectiveWithId("org.eclipse.ui.tests.util.EmptyPerspective");

        Assert.assertNotNull(perspective1);

		// Open a file.
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		activePage.setPerspective(perspective1);
		
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject testProject = workspace.getRoot().getProject(UIPerformanceTestSetup.PROJECT_NAME);
        
        
        IFile file = testProject.getFile(filename);
		
        if (editorId == null) {
            editorId = IDE.getEditorDescriptor(file).getId();
        }
        
        IEditorPart part = IDE.openEditor(activePage, file, editorId, true);
        ctrl = getControl(part);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#getControl()
     */
    public Composite getControl() throws CoreException, WorkbenchException {
        return ctrl;
    }

}
