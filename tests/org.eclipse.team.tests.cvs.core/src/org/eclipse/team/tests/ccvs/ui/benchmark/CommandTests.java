package org.eclipse.team.tests.ccvs.ui.benchmark;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.tests.ccvs.core.CommandLineCVSClient;
import org.eclipse.team.tests.ccvs.core.EclipseCVSClient;
import org.eclipse.team.tests.ccvs.ui.CVSUITestCase;
import org.eclipse.team.tests.ccvs.ui.Util;

public class CommandTests extends CVSUITestCase {
	private String baseName;
	private IProject referenceProject;
	private IProject eclipseProject;
	private IProject uiProject;
	
	public CommandTests(String name) {
		super(name);
	}
	public CommandTests() {
		super("");
	}
	public static Test suite() {
    	return new BenchmarkTestSetup(new TestSuite(CommandTests.class));
	}

	public void setUp() throws Exception {
		super.setUp();
		baseName = Util.makeUniqueName(null, getName(), null);
		referenceProject = Util.createProject(baseName + "-reference");
		eclipseProject = Util.createProject(baseName + "-eclipse");
		uiProject = Util.createProject(baseName);
	}
	
	public void testCheckout() throws Throwable {
		// import a large project
		Util.importZipIntoProject(uiProject, BenchmarkTestSetup.BIG_ZIP_FILE);
		disableLog();
		actionShareProject(uiProject);
		syncCommitResources(new IResource[] { uiProject }, null, "initial");
		enableLog();
		Util.deleteProject(uiProject);
		
		// check it out using each client
		startGroup("checkout big project");
		execute("co", Command.NO_ARGUMENTS, new String[] { "-P" }, new String[] { baseName }, "", new Protectable() {
			public void protect() throws Throwable {
				actionCheckoutProjects(new String[] { baseName }, new CVSTag[] { CVSTag.DEFAULT });
			}
		});
		endGroup();
	}
	
	protected void execute(String command, String[] globalOptions, String[] localOptions,
		String[] arguments, String pathRelativeToRoot, Protectable uiCode) throws Throwable {
		startGroup("command line client");
		IContainer container = referenceProject;
		if (pathRelativeToRoot.length() != 0) container = container.getFolder(new Path(pathRelativeToRoot));
		startTask(command);
		CommandLineCVSClient.execute(testRepository.getLocation(), container.getLocation().toFile(),
			command, globalOptions, localOptions, arguments);
		endTask();
		endGroup();

		startGroup("eclipse client");
		container = eclipseProject;
		if (pathRelativeToRoot.length() != 0) container = container.getFolder(new Path(pathRelativeToRoot));
		startTask(command);
		EclipseCVSClient.execute(testRepository, CVSWorkspaceRoot.getCVSFolderFor(container),
			command, globalOptions, localOptions, arguments);
		endTask();
		endGroup();

		startGroup("user interface");
		uiCode.protect();
		endGroup();
	}
}
