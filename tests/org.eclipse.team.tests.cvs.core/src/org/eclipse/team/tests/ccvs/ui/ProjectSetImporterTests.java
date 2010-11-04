/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ui.ProjectSetImporter;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class ProjectSetImporterTests extends EclipseTest {

	private final static String PSF_FILENAME = "temp.psf";
	private final static File PSF_FILE = new File(PSF_FILENAME);
	private static final int PROJECTS_NO = 30;

	private final static String psf_header_0 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private final static String psf_header_1 = "<psf version=\"2.0\">";
	private final static String psf_header_2 = "<provider id=\"org.eclipse.team.cvs.core.cvsnature\">";
	private final static String psf_0 = "<project reference=\"1.0,"
			+ CVSTestSetup.REPOSITORY_LOCATION + ",";
	private final static String psf_1 = ",";
	private final static String psf_2 = "\"/>";
	private final static String psf_footer_0 = "</provider>";
	private final static String psf_footer_1 = "</psf>";

	public ProjectSetImporterTests() {
		super();
	}

	public ProjectSetImporterTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ProjectSetImporterTests.class);
		return new CVSTestSetup(suite);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		PSF_FILE.delete();
	}

	public void testImportOneProject() throws TeamException, CoreException {
		IProject project = createProject("ProjectSetImporterTests",
				new String[] { "file.txt", "folder1/", "folder1/a.txt" });
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(PSF_FILE)),
					true);

			out.println(psf_header_0);
			out.println(psf_header_1);
			out.println("\t" + psf_header_2);
			out.println("\t\t" + psf_0 + project.getName() /* module */+ psf_1
					+ project.getName() /* project */+ psf_2);
			out.println("\t" + psf_footer_0);
			out.println(psf_footer_1);

			project.delete(true, null);

			IProject[] importProjectSet = null;
			importProjectSet = ProjectSetImporter.importProjectSet(
					PSF_FILENAME, Display.getDefault().getActiveShell(), null);

			assertEquals(project, importProjectSet[0]);
		} catch (InvocationTargetException e) {
			fail("1.", e.getCause());
		} catch (IOException e) {
			fail("2.", e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	public void testImportMultipleProjects() throws TeamException,
			CoreException {

		List projects = new ArrayList(PROJECTS_NO);

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < PROJECTS_NO; i++) {
			IProject project = createProject("ProjectSetImporterTests",
					new String[] { "file.txt", "folder1/", "folder1/a.txt" });

			projects.add(project);

			sb.append("\t\t" + psf_0 + project.getName() /* module */+ psf_1
					+ project.getName() /* project */+ psf_2);
			if (i < PROJECTS_NO - 1)
				sb.append("\n");

			project.delete(true, null);
		}

		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(PSF_FILE)),
					true);

			out.println(psf_header_0);
			out.println(psf_header_1);
			out.println("\t" + psf_header_2);
			out.println(sb.toString());
			out.println("\t" + psf_footer_0);
			out.println(psf_footer_1);

			IProject[] importProjectSet = null;
			importProjectSet = ProjectSetImporter.importProjectSet(
					PSF_FILENAME, Display.getDefault().getActiveShell(), null);

			for (int i = 0; i < importProjectSet.length; i++) {
				if (!projects.contains(importProjectSet[i]))
					fail();
			}
		} catch (InvocationTargetException e) {
			fail("1.", e.getCause());
		} catch (IOException e) {
			fail("2.", e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	public void testBug234149_AFewProviders() throws TeamException,
			CoreException {
		IProject project = createProject("ProjectSetImporterTests",
				new String[0]);
		IProject project2 = createProject("ProjectSetImporterTests",
				new String[0]);

		// create psf with two providers
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(PSF_FILE)),
					true);

			// add first provider to psf
			out.println(psf_header_0);
			out.println(psf_header_1);
			out.println("\t" + psf_header_2);
			out.println("\t\t" + psf_0 + project.getName() /* module */+ psf_1
					+ project.getName() /* project */+ psf_2);
			out.println("\t" + psf_footer_0);

			// add second provider to psf
			out.println("\t" + psf_header_2);
			out.println("\t\t" + psf_0 + project2.getName() /* module */+ psf_1
					+ project2.getName() /* project */+ psf_2);
			out.println("\t" + psf_footer_0);

			out.println(psf_footer_1);

			project.delete(true, null);
			project2.delete(true, null);

			IProject[] importProjectSet = null;
			importProjectSet = ProjectSetImporter.importProjectSet(
					PSF_FILENAME, Display.getDefault().getActiveShell(), null);

			assertEquals(project, importProjectSet[0]);
			assertEquals(project2, importProjectSet[1]);
		} catch (InvocationTargetException e) {
			fail("1.", e.getCause());
		} catch (IOException e) {
			fail("2.", e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	public void testBug298925_noToAll() throws TeamException, CoreException {
		IProject project = createProject("ProjectSetImporterTests",
				new String[0]);
		String[] referenceStrings = new String[] { "1.0,"
				+ CVSTestSetup.REPOSITORY_LOCATION + "," + project.getName() /* module */
				+ psf_1 + project.getName() /* project */};
		RepositoryProviderType type = RepositoryProviderType
				.getProviderType(CVSProviderPlugin.getTypeId());
		ProjectSetCapability c = type.getProjectSetCapability();
		/*
		 * ProjectSetSerializationContext.confirmOverwrite gives the same result
		 * as UIProjectSetSerializationContext when there is no project to
		 * overwrite ('No to All' selected).
		 */
		c.addToWorkspace(referenceStrings,
				new ProjectSetSerializationContext(), null);
		// If we got here and no NPE was thrown, we're good.
	}
}
