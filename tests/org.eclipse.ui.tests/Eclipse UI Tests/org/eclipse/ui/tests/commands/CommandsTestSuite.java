package org.eclipse.ui.tests.commands;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CommandsTestSuite extends TestSuite {

    public static Test suite() {
        return new CommandsTestSuite();
    }

    public CommandsTestSuite() {
        addTestSuite(CommandsTestCase1.class);
        addTestSuite(CommandsTestCase2.class);
    }
}