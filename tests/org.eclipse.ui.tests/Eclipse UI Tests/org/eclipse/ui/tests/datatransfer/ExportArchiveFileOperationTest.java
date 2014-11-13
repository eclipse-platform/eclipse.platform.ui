/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.TarEntry;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class ExportArchiveFileOperationTest extends UITestCase implements
		IOverwriteQuery {

	private static final String FILE_NAME = "test";
	private static final String ZIP_FILE_EXT = "zip";
	private static final String TAR_FILE_EXT = "tar";
    private static final String[] directoryNames = { "dir1", "dir2" };
    private static final String[] emptyDirectoryNames = { "dir3" };
    private static final String[] fileNames = { "file1.txt", "file2.txt" };

    private String localDirectory;

    private String filePath;

    private IProject project;

    private boolean flattenPaths = false;
    private boolean excludeProjectPath = false;

	public ExportArchiveFileOperationTest(String testName) {
		super(testName);
	}

	@Override
	public String queryOverwrite(String pathString) {
		return "";
	}

	public void testExportStatus(){
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, localDirectory);

        assertTrue(operation.getStatus().getCode() == IStatus.OK);
	}

	public void testExportZip() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setUseCompression(false);
        operation.setUseTarFormat(false);
        operation.run(new NullProgressMonitor());

        verifyFolders(directoryNames.length + emptyDirectoryNames.length, ZIP_FILE_EXT);

	}

	public void testExportZipCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setUseCompression(true);
        operation.setUseTarFormat(false);
        operation.run(new NullProgressMonitor());
		verifyCompressed(ZIP_FILE_EXT);
	}

	public void testExportZipCreateSelectedDirectories() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
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
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(false);
        operation.run(new NullProgressMonitor());
        flattenPaths = true;
		verifyFolders(directoryNames.length + emptyDirectoryNames.length, ZIP_FILE_EXT);
	}

	public void testExportZipCreateSelectedDirectoriesProject() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		ArchiveFileExportOperation operation = new ArchiveFileExportOperation(project, filePath);

		operation.setCreateLeadupStructure(false);
		operation.setUseCompression(false);
		operation.setUseTarFormat(false);
		operation.run(new NullProgressMonitor());
		verifyFolders(directoryNames.length + emptyDirectoryNames.length, ZIP_FILE_EXT);

		ZipFile zipFile = new ZipFile(filePath);
		Enumeration entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String name = entry.getName();
			assertTrue(name, name.startsWith(project.getName() + "/"));
		}
		zipFile.close();

	}

	public void testExportZipCreateSelectedDirectoriesWithFolders() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (IResource member : members) {
			if (isDirectory(member)) {
				resources.add(member);
			}
		}
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(false);
        operation.run(new NullProgressMonitor());
        excludeProjectPath = true;
		verifyFolders(directoryNames.length + emptyDirectoryNames.length, ZIP_FILE_EXT);
	}

	public void testExportZipCreateSelectedDirectoriesCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
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
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(true);
        operation.setUseTarFormat(false);
        operation.run(new NullProgressMonitor());
        flattenPaths = true;
		verifyCompressed(ZIP_FILE_EXT);
		verifyFolders(directoryNames.length + emptyDirectoryNames.length, ZIP_FILE_EXT);
	}

	public void testExportTar() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);
        operation.setUseTarFormat(true);
        operation.setUseCompression(false);

        operation.run(new NullProgressMonitor());

        verifyFolders(directoryNames.length + emptyDirectoryNames.length, TAR_FILE_EXT);
	}

	public void testExportTarCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setUseTarFormat(true);
        operation.setUseCompression(true);
        operation.run(new NullProgressMonitor());
		verifyCompressed(TAR_FILE_EXT);
	}

	public void testExportTarCreateSelectedDirectories() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
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
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(true);
        operation.run(new NullProgressMonitor());
        flattenPaths = true;
		verifyFolders(directoryNames.length + emptyDirectoryNames.length, TAR_FILE_EXT);
	}

	public void testExportTarCreateSelectedDirectoriesWithFolders() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (IResource member : members) {
			if (isDirectory(member)) {
				resources.add(member);
			}
		}
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(true);
        operation.run(new NullProgressMonitor());
        excludeProjectPath = true;
		verifyFolders(directoryNames.length + emptyDirectoryNames.length, TAR_FILE_EXT);

	}

	public void testExportTarCreateSelectedDirectoriesCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
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
        ArchiveFileExportOperation operation =
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(true);
        operation.setUseTarFormat(true);
        operation.run(new NullProgressMonitor());
        flattenPaths = true;
		verifyCompressed(TAR_FILE_EXT);
		verifyFolders(directoryNames.length + emptyDirectoryNames.length, TAR_FILE_EXT);

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
	    flattenPaths = false;
	    excludeProjectPath = false;
	}

	@Override
	protected void doTearDown() throws Exception {
        super.doTearDown();
        // delete exported data
        File root = new File(localDirectory);
        if (root.exists()){
        	File[] files = root.listFiles();
        	if (files != null){
        		for (int i = 0; i < files.length; i++) {
					if (!files[i].delete()) {
						fail("Could not delete " + files[i].getAbsolutePath());
					}
				}
        	}
        	root.delete();
        }
        try {
            project.delete(true, true, null);
        } catch (CoreException e) {
            fail(e.toString());
        }
        finally{
        	project = null;
        	localDirectory = null;
        	filePath = null;
        }
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

	    	// create empty folders to test bug 278402
	    	for (String emptyDirectoryName : emptyDirectoryNames) {
	    		IFolder folder = project.getFolder(emptyDirectoryName);
	    		folder.create(false, true, new NullProgressMonitor());
	    	}
    	}
    	catch(Exception e){
    		fail(e.toString());
    	}
    }

    private void verifyCompressed(String type){
    	String fileName = "";
		boolean compressed = false;
    	try{
	    	if (ZIP_FILE_EXT.equals(type)){
				ZipFile zipFile = new ZipFile(filePath);
				fileName = zipFile.getName();
	    		Enumeration entries = zipFile.entries();
	    		while (entries.hasMoreElements()){
	    			ZipEntry entry = (ZipEntry)entries.nextElement();
	    			compressed = entry.getMethod() == ZipEntry.DEFLATED;
	    		}
	    		zipFile.close();
	    	}
	    	else{
	    		File file = new File(filePath);
	    		InputStream in = new FileInputStream(file);
	    		// Check if it's a GZIPInputStream.
	    		try {
	    			in = new GZIPInputStream(in);
	    			compressed = true;
	    		} catch(IOException e) {
	    			compressed = false;
	    		}
	    		fileName = file.getName();
	    		in.close();
	    	}
    	}
    	catch (IOException e){
    		fail(e.getMessage());
    	}
    	assertTrue(fileName + " does not appear to be compressed.", compressed);
    }

    private void verifyFolders(int folderCount, String type){
    	try{
    		List allEntries = new ArrayList();
	    	if (ZIP_FILE_EXT.equals(type)){
	    		ZipFile zipFile = new ZipFile(filePath);
	    		Enumeration entries = zipFile.entries();
	    		while (entries.hasMoreElements()){
	    			ZipEntry entry = (ZipEntry)entries.nextElement();
	    			allEntries.add(entry.getName());
	    		}
	    		zipFile.close();
	    	}
	    	else{
	    		TarFile tarFile = new TarFile(filePath);
	    		Enumeration entries = tarFile.entries();
	    		while (entries.hasMoreElements()){
	    			TarEntry entry = (TarEntry)entries.nextElement();
	    			allEntries.add(entry.getName());
	    		}
	    		tarFile.close();
	    	}
	    	if (flattenPaths) {
				verifyFiles(allEntries);
			} else {
				verifyArchive(folderCount, allEntries);
			}
    	}
    	catch (IOException e){
    		fail(e.getMessage());
    	}
    	catch (TarException e){
    		fail(e.getMessage());
    	}
    }

    private void verifyArchive(int folderCount, List entries){
    	int count = 0;
    	Set folderNames = new HashSet();
    	List files = new ArrayList();
    	Iterator archiveEntries = entries.iterator();
    	while (archiveEntries.hasNext()){
    		String entryName = (String)archiveEntries.next();
			int idx = entryName.lastIndexOf("/");
			String folderPath = entryName.substring(0, idx);
			String fileName = entryName.substring(idx+1, entryName.length());
			// we get empty strings for folder entries, don't add them as a file name
			if (fileName.length() != 0) {
				files.add(fileName);
			}
			int idx2 = folderPath.lastIndexOf("/");
			if (idx2 != -1){
    			String folderName = folderPath.substring(idx2 + 1, folderPath.length());
    			folderNames.add(folderName);
			} else {
				folderNames.add(folderPath);
			}

    	}
    	verifyFolders(folderNames);
    	verifyFiles(files);
    	count += folderNames.size();
    	if (!flattenPaths && !excludeProjectPath) {
			folderCount++;
		}
    	assertTrue(
    			"Number of folders expected and found not equal: expected=" + folderCount + ", actual=" + count,
    			folderCount == count);

    }

    private void verifyFiles(List files){
    	Iterator iter = files.iterator();
    	while (iter.hasNext()){
    		String file = (String)iter.next();
    		verifyFile(file);
    	}
    }

    private void verifyFile(String entryName){
    	for (String fileName : fileNames) {
    		boolean dotProjectFileShouldBePresent = ".project".equals(entryName) && !flattenPaths && !excludeProjectPath;
    		if (fileName.equals(entryName) || dotProjectFileShouldBePresent) {
				return;
			}
    	}
    	fail("Could not find file named: " + entryName);
    }

    private void verifyFolders(Set folderNames){
    	Iterator folders = folderNames.iterator();
    	while (folders.hasNext()){
    		String folderName = (String)folders.next();
    		if (!isDirectory(folderName)){
    			if (flattenPaths) {
					fail(folderName + " is not an expected folder");
				} else if (!project.getName().equals(folderName)) {
					fail(folderName + " is not an expected folder");
				}
    		}
    	}
    }

    private boolean isDirectory(String name){
    	for (String directoryName : directoryNames) {
    		if (directoryName.equals(name)) {
				return true;
			}
    	}
    	for (String emptyDirectoryName : emptyDirectoryNames) {
    		if (emptyDirectoryName.equals(name)) {
				return true;
			}
    	}
    	return false;
    }

	private boolean isDirectory(IResource resource){
		return isDirectory(resource.getName());
	}

	private boolean isFile(IResource resource){
		for (String fileName : fileNames) {
			if (fileName.equals(resource.getName())) {
				return true;
			}
		}
		return false;
	}

}
