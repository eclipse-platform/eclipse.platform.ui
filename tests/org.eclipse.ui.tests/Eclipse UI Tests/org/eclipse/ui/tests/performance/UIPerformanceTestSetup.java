/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.TestPlugin;

public class UIPerformanceTestSetup extends TestSetup {

	private static final String PERSPECTIVE= "org.eclipse.ui.tests.fastview_perspective";

	public static final String PROJECT_NAME = "Performance Project";

	public static final String MOCK3_FILE = "TestFile.mock3";
	public static final String MULTI_FILE = "TestFile.multi";
    public static final String JAVA_FILE = "TestFile.java";
	
	private static final String INTRO_VIEW= "org.eclipse.ui.internal.introview";

    private IProject testProject;
	
	public UIPerformanceTestSetup(Test test) {
		super(test);		
	}

	/*
	 * @see junit.extensions.TestSetup#setUp()
	 */
	protected void setUp() throws Exception {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow= workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage= activeWindow.getActivePage();
		
		activePage.hideView(activePage.findViewReference(INTRO_VIEW));
		
		workbench.showPerspective(PERSPECTIVE, activeWindow);
		
		boolean wasAutobuilding= ResourceTestHelper.disableAutoBuilding();
		setUpProject();
		ResourceTestHelper.fullBuild();
		if (wasAutobuilding) {
			ResourceTestHelper.enableAutoBuilding();
			EditorTestHelper.calmDown(2000, 30000, 1000);
		}
	}
	
	/*
	 * @see junit.extensions.TestSetup#tearDown()
	 */
	protected void tearDown() throws Exception {
		// do nothing, the set up workspace will be used by the open editor tests
		
		/* 
		 * ensure the workbench state gets saved when running with the Automated Testing Framework
                 * TODO: remove when https://bugs.eclipse.org/bugs/show_bug.cgi?id=71362 is fixed
                 */
		StackTraceElement[] elements=  new Throwable().getStackTrace();
		for (int i= 0; i < elements.length; i++) {
			StackTraceElement element= elements[i];
			if (element.getClassName().equals("org.eclipse.test.EclipseTestRunner")) {
				PlatformUI.getWorkbench().close();
				break;
			}
		}
	}
	
	private void setUpProject() throws IOException, CoreException {
   
        // Create a java project.
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        testProject = workspace.getRoot().getProject(PROJECT_NAME);
        testProject.create(null);
        testProject.open(null);        
        /*IProjectDescription projectDescription = testProject.getDescription();
        String[] natureIds = { "org.eclipse.jdt.core.javanature" };
        projectDescription.setNatureIds(natureIds);*/
       /*ICommand buildCommand = new BuildCommand();
        buildCommand.setBuilderName("org.eclipse.jdt.core.javabuilder");
        projectDescription.setBuildSpec(new ICommand[] { buildCommand });
        testProject.setDescription(projectDescription, null);*/
        
        importFile(MOCK3_FILE);  
        importFile(MULTI_FILE);
        importFile(JAVA_FILE);
	}

    /**
     * @param fileName
     * @throws IOException
     * @throws CoreException
     */
    private IFile importFile(String fileName) throws IOException, CoreException {
        TestPlugin plugin = TestPlugin.getDefault();
        URL fullPathString = plugin.getDescriptor().find(
                new Path("data/performance/" + fileName));
        IPath path = new Path(fullPathString.getPath());
        File file = path.toFile();
        InputStream inputStream = new FileInputStream(file);
        IFile iFile = testProject.getFile(fileName);
        iFile.create(inputStream, true, null);
        inputStream.close();
        return iFile;
    }
}
