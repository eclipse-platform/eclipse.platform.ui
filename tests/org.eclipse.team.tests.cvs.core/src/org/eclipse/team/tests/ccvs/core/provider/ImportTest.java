package org.eclipse.team.tests.ccvs.core.provider;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

/**
 * @version 	1.0
 * @author 	${user}
 */
public class ImportTest extends EclipseTest {

	/**
	 * Constructor for ImportTest.
	 */
	public ImportTest() {
		super();
	}

	/**
	 * Constructor for ImportTest.
	 * @param name
	 */
	public ImportTest(String name) {
		super(name);
	}

	// Assert that the two containers have equal contents
	protected void assertEquals(String message, IContainer container1, IContainer container2) throws CoreException {
		assertEquals(message, container1.getName(), container2.getName());
		List members1 = new ArrayList();
		members1.addAll(Arrays.asList(container1.members()));
		members1.remove(container1.findMember("CVS"));
		
		List members2 = new ArrayList();
		members2.addAll(Arrays.asList(container2.members()));
		members2.remove(container2.findMember("CVS"));
		
		assertTrue(message, members1.size() == members2.size());
		for (int i=0;i<members1.size();i++) {
			IResource member1 = (IResource)members1.get(i);
			IResource member2 = container2.findMember(member1.getName());
			assertNotNull(message, member2);
			assertEquals(message, member1, member2);
		}
	}
	
	// Assert that the two files have equal contents
	protected void assertEquals(String message, IFile file1, IFile file2) throws CoreException {
		assertEquals(message, file1.getName(), file2.getName());
		assertTrue(message, compareContent(file1.getContents(), file2.getContents()));
	}
	
	// Assert that the two projects have equal contents ignoreing the project name
	// and the .vcm_meta file
	protected void assertEquals(String message, IProject container1, IProject container2) throws CoreException {
		List members1 = new ArrayList();
		members1.addAll(Arrays.asList(container1.members()));
		members1.remove(container1.findMember(".vcm_meta"));
		members1.remove(container1.findMember("CVS"));
		
		List members2 = new ArrayList();
		members2.addAll(Arrays.asList(container2.members()));
		members2.remove(container2.findMember(".vcm_meta"));
		members2.remove(container2.findMember("CVS"));
		
		assertTrue(message, members1.size() == members2.size());
		for (int i=0;i<members1.size();i++) {
			IResource member1 = (IResource)members1.get(i);
			IResource member2 = container2.findMember(member1.getName());
			assertNotNull(message, member2);
			assertEquals(message, member1, member2);
		}
	}
	protected void assertEquals(String message, IResource resource1, IResource resource2) throws CoreException {
		assertEquals(message, resource1.getType(), resource2.getType());
		if (resource1.getType() == IResource.FILE)
			assertEquals(message, (IFile)resource1, (IFile)resource2);
		else 
			assertEquals(message, (IContainer)resource1, (IContainer)resource2);
	}
	
	public void testImportAndCheckout() throws TeamException, CoreException {
		// Create a test project and import it into cvs
		IProject project = getUniqueTestProject("testImport");
		IResource[] result = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/a.txt" }, true);
		importProject(project);
		
		// Check it out under a different name and validate that the results are the same
		IProject copy = getWorkspace().getRoot().getProject(project.getName() + "Copy");
		CVSProviderPlugin.getProvider().checkout(getRepository(), copy, project.getName(), null, DEFAULT_MONITOR);
		assertValidCheckout(copy);
		assertEquals("The checked out copy does not match the original", project, copy);
	}
}
