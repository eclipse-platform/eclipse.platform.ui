package org.eclipse.core.tests.internal.indexing;

import junit.framework.*;

public class IntegratedFieldTest {

	public static Test suite() {
		TestEnvironment env = new IntegratedTestEnvironment();
		return BasicFieldTest.suite(env);
	}
	
}
