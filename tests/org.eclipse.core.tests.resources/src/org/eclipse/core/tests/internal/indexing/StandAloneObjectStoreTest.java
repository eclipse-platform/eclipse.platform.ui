package org.eclipse.core.tests.internal.indexing;
import junit.textui.TestRunner;
public class StandAloneObjectStoreTest {

	public static void main(String[] args) {
		TestRunner.run(BasicObjectStoreTest.suite(new StandAloneTestEnvironment()));
	}
	
}
