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
/*
 * @see Assert#assert(boolean)
 */
public static void assertTrue(boolean arg0) {
	Assert.assert(arg0);
}
/*
 * @see Assert#assert(String, boolean)
 */
public static void assertTrue(String arg0, boolean arg1) {
	Assert.assert(arg0, arg1);
}
}

