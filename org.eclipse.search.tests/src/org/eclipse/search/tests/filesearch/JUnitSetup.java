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
package org.eclipse.search.tests.filesearch;

import java.util.zip.ZipFile;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.search.tests.SearchTestPlugin;

public class JUnitSetup extends TestSetup {
	public static final String PROJECT_NAME= "JUnitSource";
	
	public static IProject getProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
	}

	public JUnitSetup(Test test) {
		super(test);
	}
	
	protected void setUp() throws Exception {
		ZipFile zip= new ZipFile(SearchTestPlugin.getDefault().getFileInPlugin(new Path("testresources/junit37-noUI-src.zip"))); //$NON-NLS-1$
		SearchTestPlugin.importFilesFromZip(zip, new Path(PROJECT_NAME), null); //$NON-NLS-1$
	}
	
	protected void tearDown() throws Exception {
		getProject().delete(true, true, null);
	}
}
