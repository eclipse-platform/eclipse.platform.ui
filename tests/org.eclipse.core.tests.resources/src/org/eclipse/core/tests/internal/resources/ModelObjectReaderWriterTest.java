package org.eclipse.core.tests.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.net.URL;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.SafeFileOutputStream;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.xml.sax.InputSource;
/**
 * 
 */
public class ModelObjectReaderWriterTest extends EclipseWorkspaceTest {
public ModelObjectReaderWriterTest() {
}
public ModelObjectReaderWriterTest(String name) {
	super(name);
}
protected boolean contains(Object key, Object[] array) {
	for (int i = 0; i < array.length; i++)
		if (key.equals(array[i]))
			return true;
	return false;
}
protected String getInvalidWorkspaceDescription() {
	return 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<workspaceDescription>\n" +
				"<name>Foo</name>\n" +
				"<autobuild>Foo</autobuild>\n" +
				"<snapshotInterval>300Foo000</snapshotInterval>\n" +
				"<fileStateLongevity>Foo480000</fileStateLongevity>\n" +
				"<maxFileStateSize>104856Foo</maxFileStateSize>\n" +
				"<maxFileStates>5Foo0</maxFileStates>\n" +
			"</workspaceDescription>\n";
}
public static Test suite() {
//	TestSuite suite = new TestSuite();
//	suite.addTest(new ModelObjectReaderWriterTest("testMultipleProjectDescriptions"));
//	return suite;
	return new TestSuite(ModelObjectReaderWriterTest.class);
}
public void testProjectDescription() throws Throwable {
	// Use ModelObject2 to read the project description

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest2.pbs");
	/* test write */
	ProjectDescription description = new ProjectDescription();
	description.setLocation(location);
	description.setName("MyProjectDescription");
	HashMap args = new HashMap(3);
	args.put("ArgOne", "ARGH!");
	args.put("ArgTwo", "2 x ARGH!");
	args.put("NullArg", null);
	args.put("EmptyArg", "");
	ICommand[] commands = new ICommand[2];
	commands[0] = description.newCommand();
	commands[0].setBuilderName("MyCommand");
	commands[0].setArguments(args);
	commands[1] = description.newCommand();
	commands[1].setBuilderName("MyOtherCommand");
	commands[1].setArguments(args);
	description.setBuildSpec(commands);

	SafeFileOutputStream output = new SafeFileOutputStream(location.toFile());
	writer.write(description, output);
	output.close();

	/* test read */
	FileInputStream input = new FileInputStream(location.toFile());
	InputSource in = new InputSource(input);
	ProjectDescription description2 = reader.read(in);
	assertTrue("1.1", description.getName().equals(description2.getName()));
	assertTrue("1.3", location.equals(description.getLocation()));

	ICommand[] commands2 = description2.getBuildSpec();
	assertEquals("2.00", 2, commands2.length);
	assertEquals("2.01", "MyCommand", commands2[0].getBuilderName());
	assertEquals("2.02", "ARGH!", commands2[0].getArguments().get("ArgOne"));
	assertEquals("2.03", "2 x ARGH!", commands2[0].getArguments().get("ArgTwo"));
	assertEquals("2.04", "", commands2[0].getArguments().get("NullArg"));
	assertEquals("2.05", "", commands2[0].getArguments().get("EmptyArg"));
	assertEquals("2.06", "MyOtherCommand", commands2[1].getBuilderName());
	assertEquals("2.07", "ARGH!", commands2[1].getArguments().get("ArgOne"));
	assertEquals("2.08", "2 x ARGH!", commands2[1].getArguments().get("ArgTwo"));
	assertEquals("2.09", "", commands2[0].getArguments().get("NullArg"));
	assertEquals("2.10", "", commands2[0].getArguments().get("EmptyArg"));

	/* remove trash */
	Workspace.clear(location.toFile());
}
public void testProjectDescription2() throws Throwable {
	// Use ModelObject2 to read the project description

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest3.pbs");
	/* test write */
	ProjectDescription description = new ProjectDescription();
	description.setLocation(location);
	description.setName("MyProjectDescription");
	HashMap args = new HashMap(3);
	args.put("ArgOne", "ARGH!");
	ICommand[] commands = new ICommand[1];
	commands[0] = description.newCommand();
	commands[0].setBuilderName("MyCommand");
	commands[0].setArguments(args);
	description.setBuildSpec(commands);
	String comment = "Now is the time for all good men to come to the aid of the party.  Now is the time for all good men to come to the aid of the party.  Now is the time for all good men to come to the aid of the party.";
	description.setComment(comment);
	IProject[] refProjects = new IProject[3];
	refProjects[0] = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.core.runtime");
	refProjects[1] = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.core.boot");
	refProjects[2] = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.core.resources");
	description.setReferencedProjects(refProjects);

	SafeFileOutputStream output = new SafeFileOutputStream(location.toFile());
	writer.write(description, output);
	output.close();

	/* test read */
	FileInputStream input = new FileInputStream(location.toFile());
	InputSource in = new InputSource(input);
	ProjectDescription description2 = reader.read(in);
	assertTrue("1.1", description.getName().equals(description2.getName()));
	assertTrue("1.3", location.equals(description.getLocation()));

	ICommand[] commands2 = description2.getBuildSpec();
	assertEquals("2.00", 1, commands2.length);
	assertEquals("2.01", "MyCommand", commands2[0].getBuilderName());
	assertEquals("2.02", "ARGH!", commands2[0].getArguments().get("ArgOne"));
	
	assertTrue("3.0", description.getComment().equals(description2.getComment()));
	
	IProject[] ref = description.getReferencedProjects();
	IProject[] ref2 = description2.getReferencedProjects();
	assertEquals("4.0", 3, ref2.length);
	assertTrue("4.1", ref[0].getName().equals(ref2[0].getName()));
	assertTrue("4.2", ref[1].getName().equals(ref2[1].getName()));
	assertTrue("4.3", ref[2].getName().equals(ref2[2].getName()));

	/* remove trash */
	Workspace.clear(location.toFile());
}
public void testInvalidWorkspaceDescription() {
	/* initialize common objects */
	OldModelObjectReader reader = new OldModelObjectReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest2.pbs");

	/* write the bogus description */
	try {
		FileWriter writer = new FileWriter(location.toFile());
		writer.write(getInvalidWorkspaceDescription());
		writer.close();
	} catch (IOException e) {
		fail("1.91", e);
	}

	/* test read */
	try {
		FileInputStream input = null;
		try {
			input = new FileInputStream(location.toFile());
		} catch (FileNotFoundException e) {
			fail("1.99", e);
		}
		//on reading invalid values the reader should revert to default values
		WorkspaceDescription desc2 = (WorkspaceDescription) reader.read(input);
		//assertion "1.1" removed because workspace name can't be invalid
		assertTrue("1.2", Policy.defaultAutoBuild == desc2.isAutoBuilding());
		assertTrue("1.3", Policy.defaultDeltaExpiration == desc2.getDeltaExpiration());
		assertTrue("1.4", Policy.defaultFileStateLongevity == desc2.getFileStateLongevity());
		assertTrue("1.5", Policy.defaultMaxFileStates == desc2.getMaxFileStates());
		assertTrue("1.6", Policy.defaultMaxFileStateSize == desc2.getMaxFileStateSize());
		assertTrue("1.7", Policy.defaultOperationsPerSnapshot == desc2.getOperationsPerSnapshot());
		assertTrue("1.8", Policy.defaultSnapshots == desc2.isSnapshotEnabled());
	} finally {
		/* remove trash */
		Workspace.clear(location.toFile());
	}
}
private String[] getPathMembers(URL path) {
	String[] list = null;
	String protocol = path.getProtocol();
	if (protocol.equals("file")) { //$NON-NLS-1$
		list = (new java.io.File(path.getFile())).list();
	} else {
		// XXX: attempt to read URL and see if we got html dir page
	}
	return list == null ? new String[0] : list;
}
private void compareProjectDescriptions (int errorTag, ProjectDescription description, ProjectDescription description2) {
	assertTrue (errorTag + ".0", description.getName().equals(description2.getName()));
	String comment = description.getComment();
	if (comment == null) {
		// The old reader previously returned null for an empty comment.  We
		// are changing this so it now returns an empty string.
		assertEquals (errorTag + ".1", 0, description2.getComment().length());
	} else
		assertTrue (errorTag + ".2", description.getComment().equals(description2.getComment()));

	IProject[] projects = description.getReferencedProjects();
	IProject[] projects2 = description2.getReferencedProjects();
	compareProjects(errorTag, projects, projects2);

	ICommand[] commands = description.getBuildSpec();
	ICommand[] commands2 = description2.getBuildSpec();
	compareBuildSpecs(errorTag, commands, commands2);

	String[] natures = description.getNatureIds();
	String[] natures2 = description2.getNatureIds();
	compareNatures(errorTag, natures, natures2);

	HashMap links = description.getLinks();
	HashMap links2 = description2.getLinks();
	compareLinks(errorTag, links, links2);
}
private void compareProjects (int errorTag, IProject[] projects, IProject[] projects2) {
	// ASSUMPTION:  projects and projects2 are non-null
	assertEquals (errorTag + ".1.0", projects.length, projects2.length);
	for (int i = 0; i < projects.length; i++) {
		assertTrue(errorTag + ".1." + (i + 1), projects[i].getName().equals(projects2[i].getName()));
	}
}
private void compareBuildSpecs (int errorTag, ICommand[] commands, ICommand[] commands2) {
	// ASSUMPTION:  commands and commands2 are non-null
	assertEquals (errorTag + ".2.0", commands.length, commands2.length);
	for (int i = 0; i < commands.length; i++) {
		assertTrue(errorTag + ".2." + (i + 1) + "0", commands[i].getBuilderName().equals(commands2[i].getBuilderName()));
		Map args = commands[i].getArguments();
		Map args2 = commands2[i].getArguments();
		assertEquals (errorTag + ".2." + (i + 1) + "0", args.size(), args2.size());
		Set keys = args.entrySet();
		int x = 1;
		for (Iterator j = keys.iterator(); j.hasNext(); x++) {
			Object key = j.next();
			String value = (String)args.get(key);
			String value2 = (String)args2.get(key);
			if (value == null)
				assertNull(errorTag + ".2."  + (i + 1) + x, value2);
			else
				assertTrue(errorTag + ".3."  + (i + 1) + x, ((String)args.get(key)).equals(((String)args2.get(key))));
		}
	}
}
private void compareNatures (int errorTag, String[] natures, String[] natures2) {
	// ASSUMPTION:  natures and natures2 are non-null
	assertEquals (errorTag + ".3.0", natures.length, natures2.length);
	for (int i = 0; i < natures.length; i++) {
		assertTrue(errorTag + ".3." + (i + 1), natures[i].equals(natures2[i]));
	}
}
private void compareLinks (int errorTag, HashMap links, HashMap links2) {
	if (links == null) {
		assertNull (errorTag + ".4.0", links2);
		return;
	}
	assertEquals (errorTag + ".4.01", links.size(), links2.size());
	Set keys = links.keySet();
	int x = 1;
	for (Iterator i = keys.iterator(); i.hasNext(); x++) {
		String key = (String)i.next();
		LinkDescription value = (LinkDescription)links.get(key);
		LinkDescription value2 = (LinkDescription)links2.get(key);
		assertTrue(errorTag + ".4." + x, value.getName().equals(value2.getName()));
		assertEquals(errorTag + ".5." + x, value.getType(), value2.getType());
		assertTrue(errorTag + ".6." + x, value.getLocation().equals(value2.getLocation()));
	}
}
public void testInvalidProjectDescription1() throws Throwable {
	String invalidProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<homeDescription>\n" +
		"	<name>abc</name>\n" +
		"	<comment></comment>\n" +
		"	<projects>\n" +
		"	</projects>\n" +
		"	<buildSpec>\n" +
		"		<buildCommand>\n" +
		"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
		"			<arguments>\n" +
		"			</arguments>\n" +
		"		</buildCommand>\n" +
		"	</buildSpec>\n" +
		"	<natures>\n" +
		"	<nature>org.eclipse.jdt.core.javanature</nature>\n" +
		"	</natures>\n" +
		"	<linkedResources>\n" +
		"		<link>\n" +
		"			<name>newLink</name>\n" +
		"			<type>2</type>\n" +
		"			<location>d:/abc/def</location>\n" +
		"		</link>\n" +
		"	</linkedResources>\n" +
		"</homeDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectReaderWriterTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(location.toFile());
	InputStream stream = new ByteArrayInputStream(invalidProjectDescription.getBytes());
	createFileInFileSystem(location, stream);
	ProjectDescription projDesc = reader.read(location);
	ensureDoesNotExistInFileSystem(location.toFile());
	
	assertNull ("1.0", projDesc);		
}
public void testInvalidProjectDescription2() throws Throwable {
	String invalidProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<projectDescription>\n" +
		"	<bogusname>abc</bogusname>\n" +
		"</projectDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectReaderWriterTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(location.toFile());
	InputStream stream = new ByteArrayInputStream(invalidProjectDescription.getBytes());
	createFileInFileSystem(location, stream);
	ProjectDescription projDesc = reader.read(location);
	ensureDoesNotExistInFileSystem(location.toFile());
	
	assertNotNull ("2.0", projDesc);
	assertNull("2.1", projDesc.getName());
	assertEquals("2.2", 0, projDesc.getComment().length());
	assertNull("2.3", projDesc.getLocation());
	assertEquals("2.4", new IProject[0], projDesc.getReferencedProjects());
	assertEquals("2.5", new String[0], projDesc.getNatureIds());
	assertEquals("2.6", new ICommand[0], projDesc.getBuildSpec());
	assertNull("2.7", projDesc.getLinks());
}
public void testInvalidProjectDescription3() throws Throwable {
	String invalidProjectDescription = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<projectDescription>\n" +
		"	<name>abc</name>\n" +
		"	<comment></comment>\n" +
		"	<projects>\n" +
		"	</projects>\n" +
		"	<buildSpec>\n" +
		"		<badBuildCommand>\n" +
		"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
		"			<arguments>\n" +
		"			</arguments>\n" +
		"		</badBuildCommand>\n" +
		"	</buildSpec>\n" +
		"</projectDescription>";

	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectReaderWriterTest.txt");
	ProjectDescriptionReader reader = new ProjectDescriptionReader();
	// Write out the project description file
	ensureDoesNotExistInFileSystem(location.toFile());
	InputStream stream = new ByteArrayInputStream(invalidProjectDescription.getBytes());
	createFileInFileSystem(location, stream);
	ProjectDescription projDesc = reader.read(location);
	ensureDoesNotExistInFileSystem(location.toFile());
	
	assertNotNull ("3.0", projDesc);
	assertTrue("3.1", projDesc.getName().equals("abc"));
	assertEquals("3.2", 0, projDesc.getComment().length());
	assertNull("3.3", projDesc.getLocation());
	assertEquals("3.4", new IProject[0], projDesc.getReferencedProjects());
	assertEquals("3.5", new String[0], projDesc.getNatureIds());
	assertEquals("3.6", new ICommand[0], projDesc.getBuildSpec());
	assertNull("3.7", projDesc.getLinks());
}
public void testMultipleProjectDescriptions() throws Throwable {
	URL whereToLook = null;
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.resources");
	String pluginPath = tempPlugin.getLocation().concat("MultipleProjectTestFiles/");
	try {
		whereToLook = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	String[] members = getPathMembers(whereToLook);
	OldModelObjectReader reader = new OldModelObjectReader();
	ProjectDescriptionReader reader2 = new ProjectDescriptionReader();
	
	for (int i = 0; i < members.length; i++) {
		URL currentURL = new URL(whereToLook, members[i] + "/.project"); //$NON-NLS-1$

		try {
			FileInputStream input = new FileInputStream(currentURL.getFile());
			ProjectDescription description = (ProjectDescription) reader.read(input);
	
			InputStream is = null;
			try {
				is = currentURL.openStream();
			} catch (IOException e) {
				fail("0.5");
			}
			InputSource in = new InputSource(is);
			ProjectDescription description2 = reader2.read(in);
			
			compareProjectDescriptions(i + 1, description, description2);
		} catch (FileNotFoundException notFound) {
			// Leave this catch clause blank.  We just want to ignore
			// the directories that have no .project file
		}
	}
}
public void testWorkspaceDescription() throws Throwable {

	/* initialize common objects */
	ModelObjectWriter writer = new ModelObjectWriter();
	OldModelObjectReader reader = new OldModelObjectReader();
	IPath root = getWorkspace().getRoot().getLocation();
	IPath location = root.append("ModelObjectWriterTest.pbs");

	/* test write */
	WorkspaceDescription desc = new WorkspaceDescription("MyWorkspace");
	desc.setName("aName");
	desc.setAutoBuilding(false);
	desc.setFileStateLongevity(654321l);
	desc.setMaxFileStates(1000);
	desc.setMaxFileStateSize(123456789l);

	SafeFileOutputStream output = new SafeFileOutputStream(location.toFile());
	writer.write(desc, output);
	output.close();

	/* test read */
	try {
		FileInputStream input = new FileInputStream(location.toFile());
		WorkspaceDescription desc2 = (WorkspaceDescription) reader.read(input);
		assertTrue("1.1", desc.getName().equals(desc2.getName()));
		assertTrue("1.2", desc.isAutoBuilding() == desc2.isAutoBuilding());
		assertTrue("1.3", desc.getDeltaExpiration() == desc2.getDeltaExpiration());
		assertTrue("1.4", desc.getFileStateLongevity() == desc2.getFileStateLongevity());
		assertTrue("1.5", desc.getMaxFileStates() == desc2.getMaxFileStates());
		assertTrue("1.6", desc.getMaxFileStateSize() == desc2.getMaxFileStateSize());
		assertTrue("1.7", desc.getOperationsPerSnapshot() == desc2.getOperationsPerSnapshot());
		assertTrue("1.8", desc.isSnapshotEnabled() == desc2.isSnapshotEnabled());
	} finally {
		/* remove trash */
		Workspace.clear(location.toFile());
	}
}
}
