package org.eclipse.core.tests.internal.indexing;
import junit.textui.TestRunner;
public class StandAlonePageStoreTest {

	public static void main(String[] args) {
		TestRunner.run(BasicPageStoreTest.suite(new StandAloneTestEnvironment()));
	}

}
