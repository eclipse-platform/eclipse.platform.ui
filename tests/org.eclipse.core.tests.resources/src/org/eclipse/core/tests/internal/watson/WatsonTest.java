package org.eclipse.core.tests.internal.watson;

import junit.framework.Assert;
import junit.framework.TestCase;
/**
 * Superclass for all tests involving the watson package.
 */
public abstract class WatsonTest extends TestCase {

/**
 * Constructor for WatsonTest.
 * @param name
 */
public WatsonTest(String name) {
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

