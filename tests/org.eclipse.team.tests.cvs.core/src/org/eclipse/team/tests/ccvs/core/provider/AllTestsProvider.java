package org.eclipse.team.tests.ccvs.core.provider;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

/**
 * @version 	1.0
 * @author 	${user}
 */
public class AllTestsProvider extends EclipseTest {

	/**
	 * Constructor for AllTests.
	 */
	public AllTestsProvider() {
		super();
	}

	/**
	 * Constructor for AllTests.
	 * @param name
	 */
	public AllTestsProvider(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		//suite.addTestSuite(ModuleTest.class);
		suite.addTest(ImportTest.suite());
		suite.addTest(CVSProviderTest.suite());
		suite.addTest(RemoteResourceTest.suite());
		suite.addTest(SyncElementTest.suite());
		return new CVSTestSetup(suite);
	}
}