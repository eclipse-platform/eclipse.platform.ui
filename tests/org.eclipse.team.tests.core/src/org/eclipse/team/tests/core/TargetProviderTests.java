package org.eclipse.team.tests.core;

import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.ILocationFactory;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;

public class TargetProviderTests extends TeamTest {

	private final String test_url = "http://paris.ott.oti.com/dav/";

	public TargetProviderTests() {
		super();
	}
	
	public TargetProviderTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(TargetProviderTests.class);
		return new TestSetup(suite);
		//return new testSetup(new RepositoryProviderTests("test"));
	}
	
	Site getDavSite() {
		return TargetManager.getSite("org.eclipse.team.webdav", test_url);
	} 
	
	public void testWebDavProjectMapping() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("webdav-projectmapping");
		TargetManager.map(project, getDavSite(), Path.EMPTY);
		TargetProvider target = TargetManager.getProvider(project);
		assertTrue(getDavSite().equals(target.getSite()));
		
		TargetManager.unmap(project);
		assertNull(TargetManager.getProvider(project));
	}
	
	public void testWebDavPut() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("webdav-put");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt"});
		ensureExistsInWorkspace(resources, true);
		TargetManager.map(project, getDavSite(), new Path("noauth"));
		TargetProvider target = TargetManager.getProvider(project);
		assertTrue(getDavSite().equals(target.getSite()));
		
		target.put(resources, null);
		
		TargetManager.unmap(project);
		assertNull(TargetManager.getProvider(project));
	}
	
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ILocationFactory factory = TargetManager.getSiteFactory("org.eclipse.team.webdav");		
		assertNotNull(factory);
		Properties properties = new Properties();
		properties.put("location", test_url);
		properties.put("httpClient.username", "myUsername");
		properties.put("httpClient.password", "myPassword");
		properties.put("httpClient.proxyURL", "");
		properties.put("httpClient.connectionTimeout", "2000"); 
		
		Site[] locations = TargetManager.getSites();
		Site location;
		if(locations.length == 0) {
			Site l = factory.newSite(properties);
			TargetManager.addSite(l);
		}
		location = getDavSite();
		TargetProvider target = location.newProvider(new Path("noauth"));
		assertNotNull(target);
	}
}
