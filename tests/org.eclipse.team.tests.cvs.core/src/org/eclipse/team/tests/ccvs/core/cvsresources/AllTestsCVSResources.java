package org.eclipse.team.tests.ccvs.core.cvsresources;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class AllTestsCVSResources extends EclipseTest {
	public static Test suite() {	
		TestSuite suite = new TestSuite();
		suite.addTest(ResourceSyncInfoTest.suite());
		suite.addTest(EclipseSynchronizerTest.suite());
		suite.addTest(EclipseFolderTest.suite());
    	return suite; 	
	}	
	
	public AllTestsCVSResources(String name) {
		super(name);
	}
	
	public AllTestsCVSResources() {
		super();
	}
}


