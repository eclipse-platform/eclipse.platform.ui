package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Team plugins can optionally implement this interface to allow running of their providers
 * against the generic provider test framework. 
 */
public interface ITeamProviderTests {
	/**
	 * Allows the test framework to inform the provider to run all further operations
	 * in a unique remote folder. This will provide individual tests with isolated sandboxes.
	 * In addition, using isolated sandboxes for tests allows browsing of the test results
	 * and is valuable for debugging failing tests. Without this support, the test framework
	 * will have to clear the remote location before each test, purging previous test
	 * results.
	 * 
	 * @param name of the remote folder in which to perform provider operations.
	 */
	public void setTestLocation(String name);
}

