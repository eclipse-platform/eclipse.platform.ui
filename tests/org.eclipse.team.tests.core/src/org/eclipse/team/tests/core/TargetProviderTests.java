package org.eclipse.team.tests.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.ISiteFactory;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;

public class TargetProviderTests extends TeamTest {

	final String test_url = "http://paris.ott.oti.com/dav/";
	
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
		try {
			URL url = new URL(test_url);
			return TargetManager.getSite("org.eclipse.team.webdav", url);
		} catch (MalformedURLException e) {
			return null;
		}
	} 
	
	public IResource[] buildResources(IContainer container, String[] hierarchy, boolean includeContainer) throws CoreException {
		List resources = new ArrayList(hierarchy.length + 1);
		resources.addAll(Arrays.asList(buildResources(container, hierarchy)));
		if (includeContainer)
			resources.add(container);
		IResource[] result = (IResource[]) resources.toArray(new IResource[resources.size()]);
		ensureExistsInWorkspace(result, true);
		for (int i = 0; i < result.length; i++) {
			if (result[i].getType() == IResource.FILE)
				// 3786 bytes is the average size of Eclipse Java files!
				 ((IFile) result[i]).setContents(getRandomContents(100), true, false, null);
		}
		return result;
	}
	
	protected static InputStream getRandomContents(int sizeAtLeast) {
		StringBuffer randomStuff = new StringBuffer(sizeAtLeast + 100);
		while (randomStuff.length() < sizeAtLeast) {
			randomStuff.append(getRandomSnippet());
		}
		return new ByteArrayInputStream(randomStuff.toString().getBytes());
	}
	
	public static String getRandomSnippet() {
		switch ((int) Math.round(Math.random() * 10)) {
			case 0 :
				return "este e' o meu conteudo (portuguese)";
			case 1 :
				return "Dann brauchen wir aber auch einen deutschen Satz!";
			case 2 :
				return "I'll be back";
			case 3 :
				return "don't worry, be happy";
			case 4 :
				return "there is no imagination for more sentences";
			case 5 :
				return "customize yours";
			case 6 :
				return "foo";
			case 7 :
				return "bar";
			case 8 :
				return "foobar";
			case 9 :
				return "case 9";
			default :
				return "these are my contents";
		}
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
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt"}, false);
		
		TargetManager.map(project, getDavSite(), new Path("noauth").append(project.getName()));
		TargetProvider target = TargetManager.getProvider(project);
		
		target.put(resources, null);
		
		TargetManager.unmap(project);
	}
	
	public void testWebDavGet() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("webdav-get");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt"}, false);
		
		TargetManager.map(project, getDavSite(), new Path("noauth").append(project.getName()));
		TargetProvider target = TargetManager.getProvider(project);
		
		target.put(resources, null);
		
		for (int i = 0; i < resources.length; i++) {
			resources[i].delete(true, null);
		}
		
		for (int i = 0; i < resources.length; i++) {
			if(resources[i].getType() == IResource.FILE) {
				assertTrue(!project.getFile(resources[i].getName()).exists());
			} else {
				assertTrue(!project.getFolder(resources[i].getName()).exists());
			}
		}
		
		target.get(new IResource[] {project}, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		IResource[] members = project.members();
		for (int i = 0; i < resources.length; i++) {
			if(resources[i].getType() == IResource.FILE) {
				assertTrue(project.getFile(resources[i].getProjectRelativePath()).exists());
			} else {
				assertTrue(project.getFolder(resources[i].getProjectRelativePath()).exists());
			}
		}
		
		TargetManager.unmap(project);
		assertNull(TargetManager.getProvider(project));
	}
	
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ISiteFactory factory = TargetManager.getSiteFactory("org.eclipse.team.webdav");		
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
