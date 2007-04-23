package org.eclipse.team.tests.ccvs.core;

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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.ProjectSetImporter;

public class ProjectSetImporterTests extends EclipseTest {

	private final static String PSF_FILENAME = "temp.psf";
	private final static File PSF_FILE = new File(PSF_FILENAME);
	private static final int PROJECTS_NO = 50;

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
		IProject project = createProject("ProjectSetImporterTests", new String[] { "file.txt", "folder1/",
				"folder1/a.txt" });
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
			fail(e.getLocalizedMessage());
		} catch (IOException ioe) {
			fail(ioe.getLocalizedMessage());
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
			IProject project = createProject("ProjectSetImporterTests", new String[] { "file.txt", "folder1/",
					"folder1/a.txt" });

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
			fail(e.getLocalizedMessage());
		} catch (IOException ioe) {
			fail(ioe.getLocalizedMessage());
		} finally {
			if (out != null)
				out.close();
		}
	}
}
