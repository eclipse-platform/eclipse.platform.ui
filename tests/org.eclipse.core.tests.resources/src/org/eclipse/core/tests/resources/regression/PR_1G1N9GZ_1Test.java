package org.eclipse.core.tests.resources.regression;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
import org.eclipse.core.tests.internal.builders.DeltaVerifierBuilder;
/**
 * This class needs to be used with PR_1G1N9GZ_2Test.
 */
public class PR_1G1N9GZ_1Test extends WorkspaceSessionTest {
public PR_1G1N9GZ_1Test() {
}
public PR_1G1N9GZ_1Test(String name) {
	super(name);
}
public static void doIt() throws Exception {
	String[] testIds = { 
	"regression.PR_1G1N9GZ_1Test", 
	"regression.PR_1G1N9GZ_2Test", 
	};
	for (int i = 0; i < testIds.length; i++) {
		Process p = Runtime.getRuntime().exec(new String[] {
		"java", "org.eclipse.core.tests.harness.launcher.Main", 
		"-test", testIds[i],
		"-platform", "c:/temp/fixed_folder",
		(i < (testIds.length-1) ? "-nocleanup" : "") });
		p.waitFor();
		java.io.InputStream input = p.getInputStream();
		int c;
		while ((c = input.read()) != -1)
			System.out.print((char) c);
		input.close();
		input = p.getErrorStream();
		while ((c = input.read()) != -1)
			System.out.print((char) c);
		input.close();
	}
	System.exit(-1);
}
public static Test suite() {
	// we do not add the whole class because the order is important
	TestSuite suite = new TestSuite();
	suite.addTest(new PR_1G1N9GZ_1Test("testCreateMyProject"));
	suite.addTest(new PR_1G1N9GZ_1Test("testCreateProject2"));
	suite.addTest(new PR_1G1N9GZ_1Test("testSaveWorkspace"));
	return suite;
}
/**
 * Create some resources and save the workspace.
 */
public void testCreateMyProject() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	try {
		project.create(null);
		project.open(null);
	} catch (CoreException e) {
		fail("0.0", e);
	}
	assertTrue("0.1", project.exists());
	assertTrue("0.2", project.isOpen());

	// Create and set a build spec for the project
	try {
		IProjectDescription description = project.getDescription();
		ICommand command = description.newCommand();
		command.setBuilderName(DeltaVerifierBuilder.BUILDER_NAME);
		description.setBuildSpec(new ICommand[] { command });
	} catch (CoreException e) {
		fail("2.0", e);
	}
}
/**
 * Create another project and leave it closed for next session.
 */
public void testCreateProject2() {
	IProject project = getWorkspace().getRoot().getProject("Project2");
	try {
		project.create(null);
		project.open(null);
	} catch (CoreException e) {
		fail("0.0", e);
	}
	assertTrue("0.1", project.exists());
	assertTrue("0.2", project.isOpen());

	// Create and set a build spec for the project
	try {
		IProjectDescription description = project.getDescription();
		ICommand command = description.newCommand();
		command.setBuilderName(DeltaVerifierBuilder.BUILDER_NAME);
		description.setBuildSpec(new ICommand[] { command });
	} catch (CoreException e) {
		fail("2.0", e);
	}
}
public void testSaveWorkspace() {
	try {
		getWorkspace().save(true, null);
	} catch (CoreException e) {
		fail("2.0", e);
	}
}
}
