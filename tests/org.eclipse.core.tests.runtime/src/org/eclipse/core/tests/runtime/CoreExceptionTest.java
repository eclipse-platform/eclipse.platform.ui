package org.eclipse.core.tests.runtime;

import org.eclipse.core.runtime.*;
import junit.framework.*;

/**
 * Test cases for the Path class.
 */
public class CoreExceptionTest extends RuntimeTest {
/**
 * Need a zero argument constructor to satisfy the test harness.
 * This constructor should not do any real work nor should it be
 * called by user code.
 */
public CoreExceptionTest() {
	super(null);
}
public CoreExceptionTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(CoreExceptionTest.class);
}
public void testCoreException() {
	final String MESSAGE_STRING = "An exception has occurred";
	IStatus status = new Status(
		IStatus.ERROR,
		"org.eclipse.core.tests.runtime",
		31415,
		MESSAGE_STRING,
		new NumberFormatException());
		
	CoreException e = new CoreException(status);
	
	assertEquals("1.0",status,e.getStatus());
	assertEquals("1.1",MESSAGE_STRING,e.getMessage());
}
}
