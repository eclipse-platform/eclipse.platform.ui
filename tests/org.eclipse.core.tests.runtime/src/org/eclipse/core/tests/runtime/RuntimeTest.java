package org.eclipse.core.tests.runtime;

import junit.framework.Assert;
import junit.framework.TestCase;
/**
 * Common superclass for all runtime tests.
 */
public abstract class RuntimeTest extends TestCase {
/**
 * Constructor required by test framework.
 */
public RuntimeTest(String name) {
	super(name);
}
/**
 * Fails the test due to the given exception.
 * @param message
 * @param e
 */
public void fail(String message, Exception e) {
	fail(message + ": " + e);
}
}

