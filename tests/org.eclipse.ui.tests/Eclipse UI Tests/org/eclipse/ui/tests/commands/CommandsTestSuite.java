package org.eclipse.ui.tests.commands;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for all areas of command support for the platform.
 */
public final class CommandsTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static final Test suite() {
        return new CommandsTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public CommandsTestSuite() {
        addTest(new TestSuite(Bug66182Test.class));
    }
}