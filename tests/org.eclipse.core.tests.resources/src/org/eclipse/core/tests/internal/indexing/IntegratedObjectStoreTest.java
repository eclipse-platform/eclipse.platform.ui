package org.eclipse.core.tests.internal.indexing;

import junit.framework.*;

public class IntegratedObjectStoreTest {

	public static Test suite() {
		TestEnvironment env = new IntegratedTestEnvironment();
		return BasicObjectStoreTest.suite(env);
	}
	
}
