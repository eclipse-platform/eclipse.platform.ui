package org.eclipse.ui.tests.datatransfer;

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

public class ImportOperationTest
	extends UITestCase
	implements IOverwriteQuery {

	private String localDirectory;

	private String[] directoryNames = { "dir1", "dir2" };

	private String[] fileNames = { "file1.txt", "file2.txt" };
	
	private IProject project;

	public ImportOperationTest(String testName) {
		super(testName);
	}

	private void createSubDirectory(String parentName, String newDirName)
		throws IOException {
		String newDirPath = parentName + File.separatorChar + newDirName;
		File newDir = new File(newDirPath);
		newDir.mkdir();
		for (int i = 0; i < directoryNames.length; i++) {
			createFile(newDirPath, fileNames[i]);
		}
	}

	private void createFile(String parentName, String filePath)
		throws IOException {
		String newFilePath = parentName + File.separatorChar + filePath;
		File newFile = new File(newFilePath);
		newFile.createNewFile();
	}

	private void deleteDirectory(File directory){
		File[] children = directory.listFiles();
		for(int i = 0; i < children.length; i ++){
			if(children[i].isDirectory())
				deleteDirectory(children[i]);
			else
				children[i].delete();
		}
		directory.delete();
	}

	/*
	 * @see IOverwriteQuery#queryOverwrite(String)
	 */
	public String queryOverwrite(String pathString) {
		//Always return an empty String - we aren't
		//doing anything interesting
		return "";
	}
	
	public void setUp() throws Exception {
		Class testClass =
			Class.forName("org.eclipse.ui.tests.datatransfer.ImportOperationTest");
		InputStream stream = testClass.getResourceAsStream("tests.ini");
		Properties properties = new Properties();
		properties.load(stream);
		localDirectory = properties.getProperty("localSource");
		setUpDirectory();
		super.setUp();
	}
	
	/**
	 * Set up the directories and files used for the test.
	 */

	private void setUpDirectory() throws IOException {
		File rootDirectory = new File(localDirectory);
		rootDirectory.mkdir();
		localDirectory = rootDirectory.getAbsolutePath(); 
		for (int i = 0; i < directoryNames.length; i++) {
			createSubDirectory(localDirectory, directoryNames[i]);
		}
	}
	
	/**
	 * Tear down. Delete the project we created and all of the
	 * files on the file system.
	 */
	public void tearDown() throws Exception {
		super.tearDown();
		try {
			project.delete(true,true,null);
			File topDirectory = new File(localDirectory);
			deleteDirectory(topDirectory);
		}
		catch (CoreException e) {
			fail(e.toString());
		}
	}

	public void testImportAll() throws Exception {
		project = FileUtil.createProject("ImportAll");
		File element = new File(localDirectory);
		List importElements = new ArrayList();
		importElements.add(element);
		ImportOperation operation =
			new ImportOperation(
				project.getFullPath(),
				FileSystemStructureProvider.INSTANCE,
				this,
				importElements);
		openTestWindow().run(true,true,operation);

		verifyFiles(directoryNames.length);
	}
	
	public void testImportSource() throws Exception {
		project = FileUtil.createProject("ImportAll");
		File element = new File(localDirectory);
		ImportOperation operation =
			new ImportOperation(
				project.getFullPath(),
				new File(localDirectory),
				FileSystemStructureProvider.INSTANCE,
				this);
		openTestWindow().run(true,true,operation);
		verifyFiles(directoryNames.length);
	}

	public void testImportSourceList() throws Exception {
		project = FileUtil.createProject("ImportAll");
		File element = new File(localDirectory + File.separator + directoryNames[0]);
		List importElements = new ArrayList();
		importElements.add(element);
		ImportOperation operation =
			new ImportOperation(
				project.getFullPath(),
				new File(localDirectory),
				FileSystemStructureProvider.INSTANCE,
				this,
				importElements);
		openTestWindow().run(true,true,operation);
		verifyFiles(importElements.size());
	}

	public void testSetFilesToImport() throws Exception {
		project = FileUtil.createProject("SetFilesToImport");
		File element = new File(localDirectory + File.separator + directoryNames[0]);
		ImportOperation operation =
			new ImportOperation(
				project.getFullPath(),
				new File(localDirectory),
				FileSystemStructureProvider.INSTANCE,
				this);
		List importElements = new ArrayList();
		importElements.add(element);
		operation.setFilesToImport(importElements);
		openTestWindow().run(true,true,operation);
		verifyFiles(importElements.size());
	}

	private void verifyFiles(int folderCount) {
		try {
			IPath path = new Path(localDirectory);
			IResource targetFolder = project.findMember(path.makeRelative());
			
			assertTrue("Import All failed", targetFolder instanceof IContainer);
			
			IResource[] resources = ((IContainer) targetFolder).members();			
			assertEquals("Import All failed to import all directories", folderCount, resources.length); 
			for (int i = 0; i < resources.length; i++) {
				assertTrue("Import All failed", resources[i] instanceof IContainer);
				verifyFolder((IContainer) resources[i]);
			}
		}
		catch (CoreException e) {
			fail(e.toString());
		}
	}

	private void verifyFolder(IContainer folder) {
		try {
			IResource[] files = folder.members();
			assertEquals("Import All failed to import all files", fileNames.length, files.length);
			for (int j = 0; j < fileNames.length; j++) {
				String fileName = fileNames[j];
				int k;
				for (k = 0; k < files.length; k++) {
					if (fileName.equals(files[k].getName())) break;
				}
				assertTrue("Import All failed to import file " + fileName, k < fileNames.length);
			}
		}
		catch (CoreException e) {
			fail(e.toString());
		}
	}
}	
