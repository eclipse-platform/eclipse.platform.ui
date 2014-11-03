/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExportOperation;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class ExportFileSystemOperationTest extends UITestCase implements
		IOverwriteQuery {

    private static final String[] directoryNames = { "dir1", "dir2" };

    private static final String[] fileNames = { "file1.txt", "file2.txt" };

    private String localDirectory;

    private IProject project;

	public ExportFileSystemOperationTest(String testName) {
		super(testName);
	}

	@Override
	public String queryOverwrite(String pathString) {
		return "";
	}

    @Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		project = FileUtil.createProject("Export" + getName());
		File destination =
			new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir())
    			.toOSString());
		localDirectory = destination.getAbsolutePath();
		if (!destination.mkdirs()) {
			fail("Could not set up destination directory for " + getName());
		}
	    setUpData();
	}

    private void setUpData(){
    	try{
	    	for (String directoryName : directoryNames) {
	    		IFolder folder = project.getFolder(directoryName);
	    		folder.create(false, true, new NullProgressMonitor());
	    		for (String fileName : fileNames) {
	    			IFile file = folder.getFile(fileName);
	    			String contents =
	    				directoryName + ", " + fileName;
	    			file.create(new ByteArrayInputStream(contents.getBytes()),
	    				true, new NullProgressMonitor());
	    		}
	    	}
    	}
    	catch(Exception e){
    		fail(e.toString());
    	}
    }

	@Override
	protected void doTearDown() throws Exception {
        super.doTearDown();
        // delete exported data
        File root = new File(localDirectory);
        if (root.exists()){
        	FileSystemHelper.clear(root);
        }
        try {
            project.delete(true, true, null);
        } catch (CoreException e) {
            fail(e.toString());
        }
        finally{
        	project = null;
        	localDirectory = null;
        }
	}

	public void testGetStatus() throws Exception {
		List resources = new ArrayList();
		resources.add(project);
        FileSystemExportOperation operation =
        	new FileSystemExportOperation(
        			null, resources, localDirectory, this);

        assertTrue(operation.getStatus().getCode() == IStatus.OK);
    }

	/* Export a project, with all directories */
	public void testExportRootResource() throws Exception {
		List resources = new ArrayList();
		resources.add(project);
        FileSystemExportOperation operation =
        	new FileSystemExportOperation(
        			null, resources, localDirectory, this);
        openTestWindow().run(true, true, operation);

        verifyFolders(directoryNames.length);
	}

	/* Export a project, create all leadup folders. */
	public void testExportResources() throws Exception {
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (IResource member : members) {
			resources.add(member);
		}
        FileSystemExportOperation operation =
        	new FileSystemExportOperation(
        			null, resources, localDirectory, this);
        openTestWindow().run(true, true, operation);

        verifyFolders(directoryNames.length);
	}

	/* Export folders, do not create leadup folders. */
	public void testExportFolderCreateDirectoryStructure() throws Exception {
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (IResource member : members) {
			if (isDirectory(member)) {
				resources.add(member);
			}
		}
        FileSystemExportOperation operation =
        	new FileSystemExportOperation(
        			null, resources, localDirectory, this);

        operation.setCreateContainerDirectories(true);
        operation.setCreateLeadupStructure(false);
        openTestWindow().run(true, true, operation);

        verifyFolders(directoryNames.length, false);
	}

	/* Export files, do not create leadup folders. */
	public void testExportFilesCreateDirectoryStructure() throws Exception {
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (IResource member : members) {
			if (isDirectory(member)){
				IResource[] folderMembers = ((IFolder)member).members();
				for (IResource folderMember : folderMembers) {
					if (isFile(folderMember)){
						resources.add(folderMember);
					}
				}
			}
		}
		FileSystemExportOperation operation =
        	new FileSystemExportOperation(
        			null, resources, localDirectory, this);

        operation.setCreateContainerDirectories(true);
        operation.setCreateLeadupStructure(false);
        openTestWindow().run(true, true, operation);

        verifyFiles(resources);
	}

	/* Export files, overwrite - do not create container directories or lead up folders. */
	public void testExportOverwrite() throws Exception {
		List resources = new ArrayList();
		resources.add(project);
        FileSystemExportOperation operation =
        	new FileSystemExportOperation(
        			null, resources, localDirectory, this);
        openTestWindow().run(true, true, operation);
        operation.setOverwriteFiles(true);
        operation.setCreateContainerDirectories(false);
        operation.setCreateLeadupStructure(false);
        openTestWindow().run(true, true, operation);

        verifyFolders(directoryNames.length);
	}

	private boolean isFile(IResource resource){
		for (String fileName : fileNames) {
			if (fileName.equals(resource.getName())) {
				return true;
			}
		}
		return false;
	}

	private void verifyFiles(List resources){
		for (int i = 0; i < resources.size(); i++){
			IResource resource = (IResource)resources.get(i);
			assertTrue(
				"Export should have exported " + resource.getName(),
				isFile(resource));

		}
	}

	private void verifyFolders(int folderCount){
		verifyFolders(folderCount, true);
	}

	private void verifyFolders(int folderCount, boolean includeRootFolder){
		File root;
		if (includeRootFolder){
			root = new File(localDirectory, project.getName());
			assertTrue("Export failed: " + project.getName() + " folder does not exist", root.exists());
		}
		else{
			root = new File(localDirectory);
		}
        File[] files = root.listFiles();
        List directories = new ArrayList();
        if (files != null){
	        for (File file : files) {
	        	if (file.isDirectory()) {
					directories.add(file);
				}
	        }
        }
        assertEquals("Export failed to Export all directories",
                folderCount, directories.size());

        for (int i = 0; i < directories.size(); i++) {
        	File directory = (File)directories.get(i);
            assertTrue("Export failed to export directory " + directory.getName(), directory.exists());
            verifyFolder(directory);
        }
	}

	private void verifyFolder(File directory){
    	File[] files = directory.listFiles();
    	if (files != null){
	    	for (File file : files) {
	    		assertTrue("Export failed to export file: " + file.getName(), file.exists());
	    	}
    	}
	}

	private boolean isDirectory(IResource resource){
		for (String directoryName : directoryNames) {
			if (directoryName.equals(resource.getName())) {
				return true;
			}
		}
		return false;
	}
}
