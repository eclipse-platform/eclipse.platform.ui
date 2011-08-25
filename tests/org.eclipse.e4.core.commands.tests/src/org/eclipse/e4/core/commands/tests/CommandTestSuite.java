package org.eclipse.e4.core.commands.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CommandTestSuite extends TestSuite {
	public static Test suite() {
		return new CommandTestSuite();
	}
	
	public CommandTestSuite() {
		addTestSuite(DefineCommandsTest.class);
		addTestSuite(HandlerTest.class);
	}
}
