package org.eclipse.core.tests.internal.indexing;

import junit.framework.*;

public class IntegratedPageStoreTest {

	public static Test suite() {
		TestEnvironment env = new IntegratedTestEnvironment();
		return BasicPageStoreTest.suite(env);
	}
	
}
