package org.eclipse.core.tests.resources;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;
import junit.textui.TestRunner;

public class IProjectTest extends EclipseWorkspaceTest {
public IProjectTest() {
}
public IProjectTest(String name) {
	super(name);
}
public void setGetPersistentProperty(IResource target) throws CoreException {
	String value = "this is a test property value";
	QualifiedName name = new QualifiedName("itp-test", "testProperty");
	target.setPersistentProperty(name, value);
	// see if we can get the property
	assertTrue("get not equal set", ((String) target.getPersistentProperty(name)).equals(value));
	// see what happens if we get a non-existant property
	name = new QualifiedName("eclipse-test", "testNonProperty");
	assertNull("non-existant persistent property not missing", target.getPersistentProperty(name));
}
protected void setUp() throws Exception {
	super.setUp();
}
public static Test suite() {
	return new TestSuite(IProjectTest.class);
}
protected void tearDown() throws Exception {
	super.tearDown();
	getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
}
/**
 * Tests creation and manipulation of projects names that are reserved on some platforms.
 */
public void testInvalidProjectNames() {
	IWorkspaceRoot root = getWorkspace().getRoot();
	
	//do some tests with invalid names
	String[] names = new String[0];
	if (BootLoader.getOS().equals(BootLoader.OS_WIN32)) {
		//invalid windows names
		names = new String[] {"prn", "nul", "con", "aux", "clock$", "com1", 
			"com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
			"lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9",
			"AUX", "con.foo", "LPT4.txt", ":", "*", "?", "\"", "<", ">", "|"};
	} else {
		//invalid names on non-windows platforms
		names = new String[] {":"};
	}
	for (int i = 0; i < names.length; i++) {
		IProject project = root.getProject(names[i]);
		assertTrue("1.0 " + names[i], !project.exists());
		try {
			project.create(getMonitor());
			project.open(getMonitor());
			fail("1.1 " + names[i]);
		} catch (CoreException e) {
		}
		assertTrue("1.2 " + names[i], !project.exists());		
		assertTrue("1.3 " + names[i], !project.isOpen());
	}
		
	//do some tests with valid names that are *almost* invalid
	if (BootLoader.getOS().equals(BootLoader.OS_WIN32)) {
		//these names are valid on windows
		names = new String[] {"hello.prn.txt", "null", "con3", "foo.aux", "lpt0",
			"com0", "com10", "lpt10", ",", "'", ";"};
	} else {
		//these names are valid on non-windows platforms
		names = new String[] {"prn", "nul", "con", "aux", "clock$", 
			"com1", "com2", "com3", "com4", "com5", "com6", "com7", 
			"com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", 
			"lpt7", "lpt8", "lpt9", "AUX", "con.foo", "LPT4.txt", "*", 
			"?", "\"", "<", ">", "|", "hello.prn.txt", "null", "con3", "foo.aux", 
			"lpt0", "com0", "com10", "lpt10", ",", "'", ";"};
	}
	for (int i = 0; i < names.length; i++) {
		IProject project = root.getProject(names[i]);
		assertTrue("2.0 " + names[i], !project.exists());
		try {
			project.create(getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("2.1 " + names[i], e);
		}
		assertTrue("2.2 " + names[i], project.exists());		
		assertTrue("2.3 " + names[i], project.isOpen());
	}
}

public void testProjectCloseOpen() {
	IProject target = getWorkspace().getRoot().getProject("Project");
	ensureExistsInWorkspace(target, true);
	IFolder folder = target.getFolder("Folder");
	ensureExistsInWorkspace(folder, true);

	try {
		target.close(getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	assertTrue("1.1", target.exists());
	assertTrue("1.2", !target.isOpen());
	assertTrue("1.3", !folder.exists());

	try {
		target.open(getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}
	assertTrue("2.1", target.isOpen());
	assertTrue("2.2", folder.exists());
}
public void testProjectCopyVariations() {
	IProject project, destProject;
	IResource[] resources;
	IResource destination, source, sourceChild, destChild;
	String[] children;
	QualifiedName qname = new QualifiedName("com.example", "myProperty");
	String actual = null;
	String value = "this is my property value.";

	// copy a project via the copy(IPath) API
	project = getWorkspace().getRoot().getProject("SourceProject");
	source = project;
	children = new String[] { "/1/", "/1/2" };
	resources = buildResources(project, children);
	ensureExistsInWorkspace(project, true);
	ensureExistsInWorkspace(resources, true);
	destination = getWorkspace().getRoot().getProject("DestProject");
	assertDoesNotExistInWorkspace("1.0", destination);
	// set a property to copy
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("1.1", e);
	}
	try {
		source.copy(destination.getFullPath(), false, getMonitor());
	} catch (CoreException e) {
		fail("1.2", e);
	}
	assertExistsInWorkspace("1.3", project);
	assertExistsInWorkspace("1.4", resources);
	resources = buildResources((IProject) destination, children);
	assertExistsInWorkspace("1.5", destination);
	assertExistsInWorkspace("1.6", resources);
	// ensure the properties were copied ok
	destChild = resources[1];
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("1.7", e);
	}
	assertNotNull("1.8", actual);
	assertEquals("1.9", value, actual);
	// cleanup
	try {
		getWorkspace().getRoot().delete(false, getMonitor());
	} catch (CoreException e) {
		fail("1.10", e);
	}

	// copy a project via the copy(IProjectDescription) API
	project = getWorkspace().getRoot().getProject("SourceProject");
	source = project;
	children = new String[] { "/1/", "/1/2" };
	resources = buildResources(project, children);
	ensureExistsInWorkspace(project, true);
	ensureExistsInWorkspace(resources, true);
	destination = getWorkspace().getRoot().getProject("DestProject");
	IProjectDescription description = getWorkspace().newProjectDescription(destination.getName());
	assertDoesNotExistInWorkspace("2.0", destination);
	// set a property to copy
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("2.1", e);
	}
	try {
		((IProject) source).copy(description, false, getMonitor());
	} catch (CoreException e) {
		fail("2.2", e);
	}
	assertExistsInWorkspace("2.3", project);
	assertExistsInWorkspace("2.4", resources);
	resources = buildResources((IProject) destination, children);
	assertExistsInWorkspace("2.5", destination);
	assertExistsInWorkspace("2.6", resources);
	// ensure the properties were copied ok
	destChild = resources[1];
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("2.7", e);
	}
	assertNotNull("2.8", actual);
	assertEquals("2.9", value, actual);
	// cleanup
	try {
		getWorkspace().getRoot().delete(false, getMonitor());
	} catch (CoreException e) {
		fail("2.10", e);
	}

	// create the source project, copy it to a be a folder under another project,
	// and ensure all is ok
	project = getWorkspace().getRoot().getProject("SourceProject");
	source = project;
	children = new String[] { "/1/", "/1/2" };
	resources = buildResources(project, children);
	destProject = getWorkspace().getRoot().getProject("DestProject");
	destination = destProject.getFolder("MyFolder");
	ensureExistsInWorkspace(new IResource[] { project, destProject }, true);
	ensureExistsInWorkspace(resources, true);
	assertDoesNotExistInWorkspace("3.0", destination);
	// set a property to copy
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("3.1", e);
	}
	try {
		source.copy(destination.getFullPath(), true, getMonitor());
	} catch (CoreException e) {
		fail("3.2", e);
	}
	assertExistsInWorkspace("3.3", project);
	assertExistsInWorkspace("3.4", resources);
	resources = buildResources((IFolder) destination, children);
	assertExistsInWorkspace("3.5", destination);
	assertExistsInWorkspace("3.6", resources);
	// ensure the properties were copied ok
	destChild = resources[1];
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("3.7", e);
	}
	assertNotNull("3.8", actual);
	assertEquals("3.9", value, actual);
	// cleanup
	try {
		getWorkspace().getRoot().delete(false, getMonitor());
	} catch (CoreException e) {
		fail("3.10", e);
	}

	// create a source folder and copy it to be a project and ensure everything is ok
	project = getWorkspace().getRoot().getProject("SourceProject");
	children = new String[] { "/1/", "/1/2" };
	source = project.getFolder("1");
	resources = buildResources(project, children);
	destination = getWorkspace().getRoot().getProject("DestProject");
	ensureExistsInWorkspace(project, true);
	ensureExistsInWorkspace(resources, true);
	assertDoesNotExistInWorkspace("4.0", destination);
	// set a property to copy
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("4.1", e);
	}
	try {
		source.copy(destination.getFullPath(), true, getMonitor());
	} catch (CoreException e) {
		fail("4.2", e);
	}
	assertExistsInWorkspace("4.3", source);
	assertExistsInWorkspace("4.4", destination);
	// ensure the properties were copied ok
	destChild = destProject.getFile("2");
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("4.5", e);
	}
	assertNotNull("4.6", actual);
	assertEquals("4.7", value, actual);
	// cleanup
	try {
		getWorkspace().getRoot().delete(true, getMonitor());
	} catch (CoreException e) {
		fail("4.8", e);
	}
}
public void testProjectCreateOpenCloseDelete() {
	IProject target = getWorkspace().getRoot().getProject("Project");
	try {
		target.create(getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	assertTrue("1.1", target.exists());

	try {
		target.open(getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}
	assertTrue("2.1", target.isOpen());

	try {
		target.close(getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}
	assertTrue("3.1", !target.isOpen());

	try {
		target.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("4.0", e);
	}
	assertTrue("4.1", !target.exists());
}
public void testProjectCreation() {
	IProject target = getWorkspace().getRoot().getProject("Project");

	try {
		target.create(getMonitor());
		assertTrue("1.0", target.exists());
	} catch (CoreException e) {
		fail("1.1", e);
	}

	try {
		target.open(getMonitor());
		assertTrue("2.0", target.isOpen());
	} catch (CoreException e) {
		fail("2.1", e);
	}
}
public void testProjectDeletion() {
	IProject target = getWorkspace().getRoot().getProject("Project");
	ensureExistsInWorkspace(target, true);

	try {
		target.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	assertTrue("1.1", !target.exists());
}
public void testProjectLocationValidation() {

	// validation of the initial project should be ok
	IProject project1 = getWorkspace().getRoot().getProject("Project1");
	IPath root = getWorkspace().getRoot().getLocation().removeLastSegments(1).append("temp");
	IPath path = root.append("foo");
	assertTrue("1.0", getWorkspace().validateProjectLocation(project1, path).isOK());
	// but not if its in the default default area
	path = Platform.getLocation().append(project1.getName());
	assertTrue("1.1", !getWorkspace().validateProjectLocation(project1, path).isOK());
	// create the first project with its default default mapping
	try {
		project1.create(getMonitor());
	} catch (CoreException e) {
		fail("1.2", e);
	}

	// create the second project with a non-default mapping
	IProjectDescription desc = getWorkspace().newProjectDescription("Project2");
	IProject project2 = getWorkspace().getRoot().getProject("Project2");
	path = root.append("project2");
	assertTrue("2.0", getWorkspace().validateProjectLocation(project2, path).isOK());
	desc.setLocation(path);
	try {
		project2.create(desc, getMonitor());
	} catch (CoreException e) {
		fail("2.1", e);
	}

	// create a third project with the default default location
	IProject project3 = getWorkspace().getRoot().getProject("Project3");
	try {
		project3.create(getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}
	// it should be ok to re-set a current project's location.
	path = root.append("project3");
	assertTrue("3.1", getWorkspace().validateProjectLocation(project3, path).isOK());

	// other cases
	assertTrue("4.0", !getWorkspace().validateProjectLocation(project3, root).isOK());
	assertTrue("4.1", !getWorkspace().validateProjectLocation(project3, root.append("project2")).isOK());
	assertTrue("4.2", !getWorkspace().validateProjectLocation(project3, root.append("project2/foo")).isOK());
	assertTrue("4.3", getWorkspace().validateProjectLocation(project3, null).isOK());
	
	try {
		project1.delete(true, getMonitor());
		project2.delete(true, getMonitor());
		project3.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.1", e);
	}
}
public void testProjectMoveContent() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	String[] children = new String[] { "/1/", "/1/2" };
	IResource[] resources = buildResources(project, children);
	ensureExistsInWorkspace(project, true);
	ensureExistsInWorkspace(resources, true);
	
	// move the project content
	IProjectDescription destination = null;
	try {
		destination = project.getDescription();
	} catch (CoreException e) {
		fail("1.0", e);
	}
	IPath oldPath = project.getLocation();
	destination.setLocation(new Path(System.getProperty("user.dir")).append("foo"));
	try {
		project.move(destination, false, getMonitor());
	} catch (CoreException e) {
		fail("1.1", e);
	}
	IPath newPath = project.getLocation();
	
	// ensure that the new description was set correctly and the locations
	// aren't the same
	assertTrue("2.0", !oldPath.equals(newPath));

	// make sure all the resources still exist.	
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			assertExistsInWorkspace("2.1." + resource.getFullPath(), resource);
			return true;
		}
	};
	try {
		getWorkspace().getRoot().accept(visitor);
	} catch (CoreException e) {
		fail("2.2", e);
	}
}
public void testProjectMoveVariations() {
	IProject project, destProject;
	IResource[] resources;
	IResource destination, source, sourceChild, destChild;
	String[] children;
	IMarker marker = null;
	IMarker[] markers = null;
	String actual = null;
	QualifiedName qname = new QualifiedName("com.example", "myProperty");
	String value = "this is my property value.";

	// rename a project via the move(IPath) API
	project = getWorkspace().getRoot().getProject("SourceProject");
	source = project;
	children = new String[] { "/1/", "/1/2" };
	resources = buildResources(project, children);
	ensureExistsInWorkspace(project, true);
	ensureExistsInWorkspace(resources, true);
	destination = getWorkspace().getRoot().getProject("DestProject");
	assertDoesNotExistInWorkspace("1.0", destination);
	// set a property to move
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("1.1", e);
	}
	// create a marker to be moved
	try {
		marker = sourceChild.createMarker(IMarker.PROBLEM);
	} catch (CoreException e) {
		fail("1.2", e);
	}
	try {
		source.move(destination.getFullPath(), false, getMonitor());
	} catch (CoreException e) {
		fail("1.3", e);
	}
	assertDoesNotExistInWorkspace("1.4", project);
	assertDoesNotExistInWorkspace("1.5", resources);
	resources = buildResources((IProject) destination, children);
	assertExistsInWorkspace("1.6", destination);
	assertExistsInWorkspace("1.7", resources);
	// ensure properties are moved too
	destChild = resources[1];
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("1.6", e);
	}
	assertNotNull("1.7", actual);
	assertEquals("1.8", value, actual);
	// ensure the marker was moved
	try {
		markers = destChild.findMarkers(null, true, IResource.DEPTH_ZERO);
	} catch (CoreException e) {
		fail("1.9", e);
	}
	assertEquals("1.10", 1, markers.length);
	// cleanup
	try {
		getWorkspace().getRoot().delete(false, getMonitor());
	} catch (CoreException e) {
		fail("1.11", e);
	}

	// rename a project via the move(IProjectDescription) API
	project = getWorkspace().getRoot().getProject("SourceProject");
	source = project;
	children = new String[] { "/1/", "/1/2" };
	resources = buildResources(project, children);
	ensureExistsInWorkspace(project, true);
	ensureExistsInWorkspace(resources, true);
	// set a property to move
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("2.1", e);
	}
	// create a marker to be moved
	try {
		marker = sourceChild.createMarker(IMarker.PROBLEM);
	} catch (CoreException e) {
		fail("2.2", e);
	}
	destination = getWorkspace().getRoot().getProject("DestProject");
	IProjectDescription description = getWorkspace().newProjectDescription(destination.getName());
	assertDoesNotExistInWorkspace("2.3", destination);
	try {
		((IProject) source).move(description, false, getMonitor());
	} catch (CoreException e) {
		fail("2.4", e);
	}
	assertDoesNotExistInWorkspace("2.5", project);
	assertDoesNotExistInWorkspace("2.6", resources);
	resources = buildResources((IProject) destination, children);
	assertExistsInWorkspace("2.7", destination);
	assertExistsInWorkspace("2.8", resources);
	// ensure properties are moved too
	destChild = resources[1];
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("2.9", e);
	}
	assertNotNull("2.10", actual);
	assertEquals("2.11", value, actual);
	// ensure the marker was moved
	try {
		markers = destChild.findMarkers(null, true, IResource.DEPTH_ZERO);
	} catch (CoreException e) {
		fail("2.12", e);
	}
	assertEquals("2.13", 1, markers.length);
	// cleanup
	try {
		getWorkspace().getRoot().delete(false, getMonitor());
	} catch (CoreException e) {
		fail("2.14", e);
	}

	// create the source project, move it to a be a folder under another project,
	// and ensure all is ok
	project = getWorkspace().getRoot().getProject("SourceProject");
	source = project;
	children = new String[] { "/1/", "/1/2" };
	resources = buildResources(project, children);
	destProject = getWorkspace().getRoot().getProject("DestProject");
	destination = destProject.getFolder("MyFolder");
	ensureExistsInWorkspace(new IResource[] { project, destProject }, true);
	ensureExistsInWorkspace(resources, true);
	assertDoesNotExistInWorkspace("3.0", destination);
	// set a property to move
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("3.1", e);
	}
	// create a marker to be moved
	try {
		marker = sourceChild.createMarker(IMarker.PROBLEM);
	} catch (CoreException e) {
		fail("3.2", e);
	}
	try {
		source.move(destination.getFullPath(), true, getMonitor());
	} catch (CoreException e) {
		fail("3.3", e);
	}
	assertDoesNotExistInWorkspace("3.4", project);
	assertDoesNotExistInWorkspace("3.5", resources);
	resources = buildResources((IFolder) destination, children);
	assertExistsInWorkspace("3.6", destination);
	assertExistsInWorkspace("3.7", resources);
	// ensure properties are moved too
	destChild = resources[1];
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("3.8", e);
	}
	assertNotNull("3.9", actual);
	assertEquals("3.10", value, actual);
	// ensure the marker was moved
	try {
		markers = destChild.findMarkers(null, true, IResource.DEPTH_ZERO);
	} catch (CoreException e) {
		fail("3.10", e);
	}
	assertEquals("3.11", 1, markers.length);
	// cleanup
	try {
		getWorkspace().getRoot().delete(true, getMonitor());
	} catch (CoreException e) {
		fail("3.12", e);
	}

	// create a source folder and move it to be a project and ensure everything is ok
	project = getWorkspace().getRoot().getProject("SourceProject");
	children = new String[] { "/1/", "/1/2" };
	source = project.getFolder("1");
	resources = buildResources(project, children);
	destination = getWorkspace().getRoot().getProject("DestProject");
	ensureExistsInWorkspace(project, true);
	ensureExistsInWorkspace(resources, true);
	assertDoesNotExistInWorkspace("4.0", destination);
	// set a property to move
	sourceChild = resources[1];
	try {
		sourceChild.setPersistentProperty(qname, value);
	} catch (CoreException e) {
		fail("4.1", e);
	}
	// create a marker to be moved
	try {
		marker = sourceChild.createMarker(IMarker.PROBLEM);
	} catch (CoreException e) {
		fail("4.2", e);
	}
	try {
		source.move(destination.getFullPath(), true, getMonitor());
	} catch (CoreException e) {
		fail("4.3", e);
	}
	assertDoesNotExistInWorkspace("4.4", source);
	assertExistsInWorkspace("4.5", destination);
	// ensure properties are moved too
	destChild = destProject.getFile("2");
	try {
		actual = destChild.getPersistentProperty(qname);
	} catch (CoreException e) {
		fail("4.6", e);
	}
	assertNotNull("4.7", actual);
	assertEquals("4.8", value, actual);
	// ensure the marker was moved
	try {
		markers = destChild.findMarkers(null, true, IResource.DEPTH_ZERO);
	} catch (CoreException e) {
		fail("4.9", e);
	}
	assertEquals("4.10", 1, markers.length);
}
public void testProjectReferences() {
	IProject target = getWorkspace().getRoot().getProject("Project1");
	ensureExistsInWorkspace(target, true);

	IProject project = getWorkspace().getRoot().getProject("Project2");
	ensureExistsInWorkspace(project, true);

	try {
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	IProjectDescription description = null;
	try {
		description = project.getDescription();
	} catch (CoreException e) {
		fail("1.0", e);
	}
	description.setReferencedProjects(new IProject[] { target });
	try {
		project.setDescription(description, getMonitor());
	} catch (CoreException e) {
		fail("2.0", e);
	}
	assertTrue("2.1", target.getReferencingProjects().length == 1);
}
public void testSetGetProjectPersistentProperty() {
	IProject target = getWorkspace().getRoot().getProject("Project");
	ensureExistsInWorkspace(target, true);
	try {
		setGetPersistentProperty(target);
	} catch (CoreException e) {
		fail("1.0", e);
	}
}
}
