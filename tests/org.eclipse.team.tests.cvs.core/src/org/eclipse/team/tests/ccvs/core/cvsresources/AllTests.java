package org.eclipse.team.tests.ccvs.core.cvsresources;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.awtui.TestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class AllTests extends EclipseTest {

	public static void main(String[] args) {	
		TestRunner.run(AllTests.class);
	}
	
	public static Test suite() {	
		TestSuite suite = new TestSuite();
		suite.addTest(ResourceSyncInfoTest.suite());
    	return suite; 	
	}	
	
	public AllTests(String name) {
		super(name);
	}
	
	public AllTests() {
		super();
	}
}


