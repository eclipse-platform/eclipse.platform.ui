package org.eclipse.core.tests.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;

public class IWorkspaceRootTest extends EclipseWorkspaceTest {
public IWorkspaceRootTest() {
	super();
}
public IWorkspaceRootTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(IWorkspaceRootTest.class);
}
protected void tearDown() throws Exception {
	IProject[] projects = getWorkspace().getRoot().getProjects();
	getWorkspace().delete(projects, true, null);
}
public void testPersistentProperty() {
	IWorkspaceRoot root = getWorkspace().getRoot();
	String value = "this is a test property value";
	QualifiedName name = new QualifiedName("test", "testProperty");
	try {
		root.setPersistentProperty(name, value);
	} catch (CoreException e) {
		fail("1.0", e);
	}

	try {
		String storedValue = (String) root.getPersistentProperty(name);
		assertEquals("2.0", value, storedValue);
	} catch (CoreException e) {
		fail("2.1", e);
	}

	try {
		name = new QualifiedName("test", "testNonProperty");
		String storedValue = (String) root.getPersistentProperty(name);
		assertEquals("3.0", null, storedValue);
	} catch (CoreException e) {
		fail("3.1", e);
	}
}
}
