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
public class AllTests extends EclipseTest {

	/**
	 * Constructor for AllTests.
	 */
	public AllTests() {
		super();
	}

	/**
	 * Constructor for AllTests.
	 * @param name
	 */
	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ImportTest.class);
		suite.addTestSuite(CVSProviderTest.class);
		suite.addTestSuite(RemoteResourceTest.class);
		//suite.addTestSuite(CommandsTest.class);
		suite.addTestSuite(SyncElementTest.class);
		return new CVSTestSetup(suite);
	}
}
