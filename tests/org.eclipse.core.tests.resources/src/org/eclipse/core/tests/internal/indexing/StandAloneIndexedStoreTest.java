package org.eclipse.core.tests.internal.indexing;
import junit.textui.TestRunner;
public class StandAloneIndexedStoreTest {

	public static void main(String[] args) {
		TestRunner.run(BasicIndexedStoreTest.suite(new StandAloneTestEnvironment()));
	}
	
}
