package org.eclipse.core.tests.internal.indexing;

import junit.framework.*;

public class IntegratedIndexedStoreTest {

	public static Test suite() {
		TestEnvironment env = new IntegratedTestEnvironment();
		return BasicIndexedStoreTest.suite(env);
	}
	
}
