/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;

public class GenericEditorTestUtils {
	
	private static IProject project;
	private static IFile file;

	 public static void setUpBeforeClass() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("genericEditorTest");
		project.create(null);
		project.open(null);
		file = project.getFile("foo.txt");
		file.create(new ByteArrayInputStream("bar 'bar'".getBytes()), true, null);
	 }

	public static void tearDownAfterClass() throws Exception {
		file.delete(true, null);
		project.delete(true, null);
	}

	public static void closeIntro() {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}
	}
	
	public static IFile getFile(){
		return file;
	}

}
