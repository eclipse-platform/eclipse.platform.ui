package org.eclipse.core.tests.harness;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Harness for running eclipse workspace session tests.  See the class comments in 
 * the class WorkspaceSessionTest for more information.
 */
public class SessionTestApplication extends EclipseTestHarnessApplication {
/**
 * Test names can have two forms:
 * 1) Unqualified test ID -> Run the suite method for that test class
 * 2) Qualified ID + method name -> Run the single test method with the given name.
 */
protected Object run(String testName) throws Exception {
	if (testName.indexOf('#') == -1) {
		return super.run(testName);
	} else {
		return runSingleTest(testName);
	}
}
/**
 * Runs a single test method of the given test class.
 * The test method is supplied after the '#' in the test name.
 */
protected Object runSingleTest(String testName) throws Exception {
	int hash = testName.indexOf('#');
	String testID = testName.substring(0, hash);
	String testMethod = testName.substring(hash+1);
	
	Object testClass = findTestFor(testID);
	if (testClass == null) {
		return null;
	}
	if (!(testClass instanceof TestCase)) {
		System.out.println("Session tests must conform to the TestCase interface: " + testClass);
		return null;
	}
	TestCase testCase = (TestCase)testClass;
	testCase.setName(testMethod);
	run(testCase);
	return null;
}
}