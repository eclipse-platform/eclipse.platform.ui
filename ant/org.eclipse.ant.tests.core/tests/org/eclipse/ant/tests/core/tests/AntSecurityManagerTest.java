package org.eclipse.ant.tests.core.tests;

import static org.junit.Assert.assertFalse;

import org.eclipse.ant.internal.core.AntSecurityManager;
import org.eclipse.ant.tests.core.AbstractAntTest;
import org.junit.Test;

public class AntSecurityManagerTest extends AbstractAntTest {

	@SuppressWarnings("deprecation")
	@Test
	public void test_getInCheck() {
		AntSecurityManager securityManager = new AntSecurityManager(System.getSecurityManager(), Thread.currentThread());
		assertFalse(securityManager.getInCheck());
	}

}
