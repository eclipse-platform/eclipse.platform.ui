package org.eclipse.core.tests.internal.indexing;
import junit.textui.TestRunner;
public class StandAloneFieldTest {

	public static void main(String[] args) {
		TestRunner.run(BasicFieldTest.suite(new StandAloneTestEnvironment()));
	}

}
