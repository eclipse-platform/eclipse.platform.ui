package org.eclipse.core.tests.runtime;

import org.eclipse.core.runtime.*;
import junit.framework.*;

/**
 * Test cases for the Path class.
 */
public class OperationCanceledExceptionTest extends RuntimeTest {
/**
 * Need a zero argument constructor to satisfy the test harness.
 * This constructor should not do any real work nor should it be
 * called by user code.
 */
public OperationCanceledExceptionTest() {
	super(null);
}
public OperationCanceledExceptionTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(OperationCanceledExceptionTest.class);
}
public void testCoreException() {
	final String MESSAGE_STRING = "An exception has occurred";
	OperationCanceledException e = new OperationCanceledException(MESSAGE_STRING);
	
	assertEquals("1.0",MESSAGE_STRING,e.getMessage());
}
}
