package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class MultipleTestLocalSite extends UpdateManagerTestCase {
public MultipleTestLocalSite(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Multiple Test Local Site");
	
	// the following will take all teh test methods in teh class that start with 'test'
	
	for(int i=0; i<50; i++){
		suite.addTest(new TestSuite(TestLocalSite.class));
	}
	
	return suite;
}
}
