package org.eclipse.ant.tests.core.tests;

import org.eclipse.ant.internal.core.AntSecurityManager;
import org.eclipse.ant.tests.core.AbstractAntTest;

public class AntSecurityManagerTest extends AbstractAntTest {

	public AntSecurityManagerTest(String name) {
		super(name);
	}

	@SuppressWarnings("deprecation")
	public void test_getInCheck() {
		AntSecurityManager securityManager = new AntSecurityManager(System.getSecurityManager(), Thread.currentThread());
		assertFalse(securityManager.getInCheck());
	}

}
