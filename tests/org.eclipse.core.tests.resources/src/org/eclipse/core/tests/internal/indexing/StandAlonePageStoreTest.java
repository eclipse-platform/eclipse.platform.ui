package org.eclipse.core.tests.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.textui.TestRunner;

public class StandAlonePageStoreTest {

	public static void main(String[] args) {
		TestRunner.run(BasicPageStoreTest.suite(new StandAloneTestEnvironment()));
	}

}
