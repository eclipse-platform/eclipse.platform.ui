package org.eclipse.core.tests.harness;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Workspace session tests function as follows:  Each test class looks like a typical JUnit test,
 * except the platform is shutdown and restarted after each test method.  The steps for each
 * test method are:
 *  - startup the platform
 *  - run setUp
 *  - run the test method
 *  - run tearDown
 *  - shutdown the platform
 * 
 * Tests are run according to the natural order of their method names.  This is the
 * partial ordering defined by the String.compareTo() operation.  Each test method
 * must begin with the prefix "test", and have no parameters (thus overloading is
 * not supported).
 * 
 * After all test methods in the class have been run, the platform location is deleted.
 * This way, each test class plays like a series of related operations on the same
 * workspace, with each operation running in a separate platform instance.
 * 
 * The class SessionTestLauncher acts as a harness for running the tests.  Each string
 * passed to the main method of SessionTestLauncher represents a separate fully-qualified 
 * session test class name.  There must be a corresponding test ID in some XML file that exactly
 * matches the fully qualified class name.
 */
public class WorkspaceSessionTest extends EclipseWorkspaceTest {
	protected String testName;

/**
 * Constructor for WorkspaceSessionTest.
 */
public WorkspaceSessionTest() {
	super();
}

/**
 * Constructor for WorkspaceSessionTest.
 * @param name
 */
public WorkspaceSessionTest(String name) {
	super(name);
}

protected void tearDown() throws Exception {
	// We should not run super.tearDown() on session tests.
	// If needed, we should to call it explicitly.
}
}