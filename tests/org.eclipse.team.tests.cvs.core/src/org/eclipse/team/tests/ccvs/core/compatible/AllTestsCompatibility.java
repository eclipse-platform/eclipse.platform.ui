package org.eclipse.team.tests.ccvs.core.compatible;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;

public class AllTestsCompatibility extends TestSuite {
			
	public static Test suite() {	
		TestSuite suite = new TestSuite();
		suite.addTest(BasicTest.suite());
		suite.addTest(ConflictTest.suite());
		suite.addTest(ModuleTest.suite());
    	return new CompatibleTestSetup(suite);
	}	
	
	public AllTestsCompatibility(String name) {
		super(name);
	}

	public AllTestsCompatibility() {
		super();
	}
}

