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
package org.eclipse.ui.tests.activities;

import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;

public class NatureTests extends UITestCase {

	private static final String ANOTHERJAVAACTIVITY = "anotherJavaActivity";
	private IProject project;
	
    /**
     * @param testName
     */
    public NatureTests(String testName) {
        super(testName);
    }
	
	public void testNatureEnablement() throws CoreException {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		IActivityManager manager = activitySupport.getActivityManager();
		
		activitySupport.setEnabledActivityIds(Collections.EMPTY_SET);
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(IDEInternalPreferences.NATURES_ARE_TRIGGER_POINTS, false);
		createProject();
		assertFalse(manager.getEnabledActivityIds().contains(ANOTHERJAVAACTIVITY));		
		
		activitySupport.setEnabledActivityIds(Collections.EMPTY_SET);		
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setToDefault(IDEInternalPreferences.NATURES_ARE_TRIGGER_POINTS);
		createProject();
		assertTrue(manager.getEnabledActivityIds().contains(ANOTHERJAVAACTIVITY));		
	}

	private void createProject() throws CoreException {
		if (project != null)
			project.delete(true, null);
		
		project = FileUtil.createProject("someProject"); 
		IProjectDescription projectDescription = project.getDescription();
		String[] natureIds = { "org.eclipse.jdt.core.javanature" }; //$NON-NLS-1$
		projectDescription.setNatureIds(natureIds);
		project.setDescription(projectDescription, null);
	}
	
	protected void doTearDown() throws Exception {
		super.doTearDown();
		if (project != null)
			project.delete(true, null);
	}
}
