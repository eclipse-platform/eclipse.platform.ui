package org.eclipse.team.tests.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;

public class RepositoryProviderTests extends TeamTest {
	public RepositoryProviderTests() {
		super();
	}
	
	public RepositoryProviderTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(RepositoryProviderTests.class);
		return new TestSetup(suite);
		//return new testSetup(new RepositoryProviderTests("test"));
	}
	
	public void testProvidersRegistered() throws CoreException, TeamException {
		List repoProviderIds = new ArrayList(Arrays.asList(RepositoryProvider.getAllProviderTypeIds()));
		assertEquals(true, repoProviderIds.contains(RepositoryProviderBic.NATURE_ID));
		assertEquals(true, repoProviderIds.contains(RepositoryProviderNaish.NATURE_ID));
		assertEquals(false, repoProviderIds.contains(RepositoryProviderOtherSport.NATURE_ID));
	}
	
	public void testGetProviderGeneric() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("testGetProviderGeneric");
		
		// test that adding a non-team nature will not return a provider
		RepositoryProvider.addNatureToProject(project, RepositoryProviderOtherSport.NATURE_ID, null);
		assertTrue(RepositoryProvider.getProvider(project) == null);
		
		// adding a valid team provider should be fine
		//RepositoryProvider.addNatureToProject(project, RepositoryProvider);
	}
}
