/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

/**
 * Tests use of IResourceMappings on projects.
 */
public class ResourceMappingTest extends EclipseWorkspaceTest {
public ResourceMappingTest() {
	super();
}
public ResourceMappingTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(ResourceMappingTest.class);
}
/**
 * Tests the location of mapped resources.
 */
public void testResourceLocation() {
	assertTrue("not yet implemented", false);
	//create some common resources
	IProject project = getWorkspace().getRoot().getProject("Project1");
	IFolder mappedFolder = project.getFolder("Folder1");
	IFolder unmappedFolder = project.getFolder("Folder2");
	IFolder folderInMappedFolder = mappedFolder.getFolder("Folder3");
	IFile fileInMappedFolder = mappedFolder.getFile("File1");
	IFile fileInUnmappedFolder = unmappedFolder.getFile("File2");
	IFile mappedFile = project.getFile("File3");
	IFile unmappedFile = project.getFile("File4");
	IResource[] allResources= new IResource[] {project, mappedFolder, unmappedFolder,
		folderInMappedFolder, fileInMappedFolder, fileInUnmappedFolder, mappedFile, unmappedFile};
	
	//Scenario 1: project in default area with no mappings
	ensureExistsInWorkspace(allResources, true);
	for (int i = 0; i < allResources.length; i++) {
		assertEquals("1." + i, Platform.getLocation().append(allResources[i].getFullPath()), allResources[i].getLocation());
	}
	ensureDoesNotExistInWorkspace(allResources);
	
	//Scenario 2: project in default area, mapped resources
	IProjectDescription description = getWorkspace().newProjectDescription(project.getName());
	IPath folderLocation = getRandomLocation();
	IPath fileLocation = getRandomLocation();
//	description.addMapping(description.newMapping(mappedFolder.getName(), folderLocation));
//	description.addMapping(description.newMapping(mappedFile.getName(), fileLocation));
	try {
		project.create(description, getMonitor());
		project.open(getMonitor());
		ensureExistsInWorkspace(allResources, true);
		
		assertEquals("2.1", Platform.getLocation().append(project.getName()), project.getLocation());
		assertEquals("2.2", folderLocation, mappedFolder.getLocation());
		assertEquals("2.3", project.getLocation().append(unmappedFolder.getName()), unmappedFolder.getLocation());
		assertEquals("2.4", folderLocation.append(folderInMappedFolder.getName()), folderInMappedFolder.getLocation());
		assertEquals("2.5", folderLocation.append(fileInMappedFolder.getName()), fileInMappedFolder.getLocation());
		assertEquals("2.6", fileLocation, mappedFile.getLocation());
		assertEquals("2.7", project.getLocation().append(unmappedFile.getName()), unmappedFile.getLocation());
	} catch (CoreException e) {
		fail ("2.99", e);
	} finally {
		Workspace.clear(folderLocation.toFile());
		Workspace.clear(fileLocation.toFile());
	}
	ensureDoesNotExistInWorkspace(allResources);
	
	//Scenario 3: project in custom area, no mappings
	description = getWorkspace().newProjectDescription(project.getName());
	IPath projectLocation = getRandomLocation();
	description.setLocation(projectLocation);
	try {
		project.create(description, getMonitor());
		project.open(getMonitor());
		ensureExistsInWorkspace(allResources, true);
		for (int i = 0; i < allResources.length; i++) {
			assertEquals("3." + i, 
				projectLocation.append(allResources[i].getProjectRelativePath()), 
				allResources[i].getLocation());
		}
	} catch (CoreException e) {
		fail ("3.99", e);
	} finally {
		Workspace.clear(projectLocation.toFile());
	}
	ensureDoesNotExistInWorkspace(allResources);

	//Scenario 4: project in custom area, mapped resources
	description = getWorkspace().newProjectDescription(project.getName());
	projectLocation = getRandomLocation();
	folderLocation = getRandomLocation();
	fileLocation = getRandomLocation();
	description.setLocation(projectLocation);
//	description.addMapping(description.newMapping(mappedFolder.getName(), folderLocation));
//	description.addMapping(description.newMapping(mappedFile.getName(), fileLocation));
	try {
		project.create(description, getMonitor());
		project.open(getMonitor());
		ensureExistsInWorkspace(allResources, true);
		
		assertEquals("2.1", projectLocation, project.getLocation());
		assertEquals("2.2", folderLocation, mappedFolder.getLocation());
		assertEquals("2.3", projectLocation.append(unmappedFolder.getName()), unmappedFolder.getLocation());
		assertEquals("2.4", folderLocation.append(folderInMappedFolder.getName()), folderInMappedFolder.getLocation());
		assertEquals("2.5", folderLocation.append(fileInMappedFolder.getName()), fileInMappedFolder.getLocation());
		assertEquals("2.6", fileLocation, mappedFile.getLocation());
		assertEquals("2.7", projectLocation.append(unmappedFile.getName()), unmappedFile.getLocation());
	} catch (CoreException e) {
		fail ("4.99", e);
	} finally {
		Workspace.clear(projectLocation.toFile());
		Workspace.clear(folderLocation.toFile());
		Workspace.clear(fileLocation.toFile());
	}
}
}
