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
 * 1GALH44: ITPCORE:WINNT - SEVERE: Walkback saving workspace trees
 * 
 * This class needs to be used with PR_1GALH44_2Test.
 */
public class PR_1GALH44_1Test extends WorkspaceSessionTest {
public PR_1GALH44_1Test() {
}
public PR_1GALH44_1Test(String name) {
	super(name);
}
public static void doIt() throws Exception {
	String[] testIds = { 
	"regression.PR_1GALH44_1Test", 
	"regression.PR_1GALH44_2Test", 
	"regression.PR_1GALH44_3Test", 
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
	suite.addTest(new PR_1GALH44_1Test("testPrepareEnvironment"));
	return suite;
}
/**
 * Create some resources and save the workspace.
 */
public void testPrepareEnvironment() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IProjectDescription description = getWorkspace().newProjectDescription("MyProject");
	ICommand command = description.newCommand();
	command.setBuilderName(DeltaVerifierBuilder.BUILDER_NAME);
	description.setBuildSpec(new ICommand[] { command });
	try {
		project.create(description, getMonitor());
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	IFile file = project.getFile("foo.txt");
	try {
		file.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}

	try {
		getWorkspace().save(true, getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
}
