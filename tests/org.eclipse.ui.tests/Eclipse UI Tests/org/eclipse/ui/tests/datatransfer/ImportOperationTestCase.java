package org.eclipse.ui.tests.datatransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

public class ImportOperationTestCase
	extends UITestCase
	implements IOverwriteQuery {

	private String localDirectory;

	private String[] directoryNames = { "dir1", "dir2" };

	private String[] fileNames = { "file1.txt", "file2.txt" };
	
	private IProject project;

	public ImportOperationTestCase(String testName) {
		super(testName);
	}

	public void setUp() throws Exception {
		Class testClass =
			Class.forName("org.eclipse.ui.tests.datatransfer.ImportOperationTestCase");
		InputStream stream = testClass.getResourceAsStream("tests.ini");
		Properties properties = new Properties();
		properties.load(stream);
		localDirectory = properties.getProperty("localSource");
		setUpDirectory();
		super.setUp();
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
	}

	/**
	 * Set up the directories and files used for the test.
	 */

	private void setUpDirectory() throws IOException {
		File rootDirectory = new File(localDirectory);
		rootDirectory.mkdir();
		for (int i = 0; i < directoryNames.length; i++) {
			createSubDirectory(localDirectory, directoryNames[i]);
		}
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

	/*
	 * @see IOverwriteQuery#queryOverwrite(String)
	 */
	public String queryOverwrite(String pathString) {
		//Always return an empty String - we aren't
		//doing anything interesting
		return "";
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
}