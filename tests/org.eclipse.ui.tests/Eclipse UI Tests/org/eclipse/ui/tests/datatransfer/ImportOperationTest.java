/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

public class ImportOperationTest extends UITestCase implements IOverwriteQuery {

    private static final String[] directoryNames = { "dir1", "dir2" };

    private static final String[] fileNames = { "file1.txt", "file2.txt" };
    
    private String localDirectory;

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

    /*
     * @see IOverwriteQuery#queryOverwrite(String)
     */
    public String queryOverwrite(String pathString) {
        //Always return an empty String - we aren't
        //doing anything interesting
        return "";
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        Class testClass = Class
                .forName("org.eclipse.ui.tests.datatransfer.ImportOperationTest");
        InputStream stream = testClass.getResourceAsStream("tests.ini");
        Properties properties = new Properties();
        properties.load(stream);
        localDirectory = properties.getProperty("localSource");
        setUpDirectory();
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
    protected void doTearDown() throws Exception {
        super.doTearDown();
        try {
            project.delete(true, true, null);
            File topDirectory = new File(localDirectory);
            FileSystemHelper.clear(topDirectory);
        } catch (CoreException e) {
            fail(e.toString());
        }
        finally{
        	project = null;
        	localDirectory = null;
        }
    }

    public void testGetStatus() throws Exception {
        project = FileUtil.createProject("ImportGetStatus");
        File element = new File(localDirectory);
        List importElements = new ArrayList();
        importElements.add(element);
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                FileSystemStructureProvider.INSTANCE, this, importElements);

        assertTrue(operation.getStatus().getCode() == IStatus.OK);
    }

    public void testImportList() throws Exception {
        project = FileUtil.createProject("ImportList");
        File element = new File(localDirectory);
        List importElements = new ArrayList();
        importElements.add(element);
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                FileSystemStructureProvider.INSTANCE, this, importElements);
        openTestWindow().run(true, true, operation);

        verifyFiles(directoryNames.length);
    }

    public void testImportSource() throws Exception {
        project = FileUtil.createProject("ImportSource");
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                new File(localDirectory), FileSystemStructureProvider.INSTANCE,
                this);
        openTestWindow().run(true, true, operation);
        verifyFiles(directoryNames.length);
    }

    public void testImportSourceList() throws Exception {
        project = FileUtil.createProject("ImportSourceList");
        File element = new File(localDirectory + File.separator
                + directoryNames[0]);
        List importElements = new ArrayList();
        importElements.add(element);
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                new File(localDirectory), FileSystemStructureProvider.INSTANCE,
                this, importElements);
        openTestWindow().run(true, true, operation);
        verifyFiles(importElements.size());
    }

    public void testSetContext() throws Exception {
        project = FileUtil.createProject("ImportSetContext");
        File element = new File(localDirectory);
        List importElements = new ArrayList();
        importElements.add(element);
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                FileSystemStructureProvider.INSTANCE, this, importElements);

        operation.setContext(null);
        operation.setContext(openTestWindow().getShell());
    }

    public void testSetCreateContainerStructure() throws Exception {
        project = FileUtil.createProject("ImportSetCreateContainerStructure");
        File element = new File(localDirectory);
        List importElements = new ArrayList();
        importElements.add(element);
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                FileSystemStructureProvider.INSTANCE, this, importElements);

        operation.setCreateContainerStructure(false);
        openTestWindow().run(true, true, operation);

        try {
            IPath path = new Path(localDirectory);
            IResource targetFolder = project.findMember(path.lastSegment());

            assertTrue("Import failed", targetFolder instanceof IContainer);

            IResource[] resources = ((IContainer) targetFolder).members();
            assertEquals("Import failed to import all directories",
                    directoryNames.length, resources.length);
            for (int i = 0; i < resources.length; i++) {
                assertTrue("Import failed", resources[i] instanceof IContainer);
                verifyFolder((IContainer) resources[i]);
            }
        } catch (CoreException e) {
            fail(e.toString());
        }
    }

    public void testSetFilesToImport() throws Exception {
        project = FileUtil.createProject("ImportSetFilesToImport");
        File element = new File(localDirectory + File.separator
                + directoryNames[0]);
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                new File(localDirectory), FileSystemStructureProvider.INSTANCE,
                this);
        List importElements = new ArrayList();
        importElements.add(element);
        operation.setFilesToImport(importElements);
        openTestWindow().run(true, true, operation);
        verifyFiles(importElements.size());
    }

    public void testSetOverwriteResources() throws Exception {
        project = FileUtil.createProject("ImportSetOverwriteResources");
        File element = new File(localDirectory);
        List importElements = new ArrayList();
        importElements.add(element);
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                FileSystemStructureProvider.INSTANCE, this, importElements);

        openTestWindow().run(true, true, operation);
        operation.setOverwriteResources(true);
        openTestWindow().run(true, true, operation);
    }

    /**
     * Verifies that all files were imported.
     * 
     * @param folderCount number of folders that were imported
     */
    private void verifyFiles(int folderCount) {
        try {
            IPath path = new Path(localDirectory);
            IResource targetFolder = project.findMember(path.makeRelative());

            assertTrue("Import failed", targetFolder instanceof IContainer);

            IResource[] resources = ((IContainer) targetFolder).members();
            assertEquals("Import failed to import all directories",
                    folderCount, resources.length);
            for (int i = 0; i < resources.length; i++) {
                assertTrue("Import failed", resources[i] instanceof IContainer);
                verifyFolder((IContainer) resources[i]);
            }
        } catch (CoreException e) {
            fail(e.toString());
        }
    }

    /**
     * Verifies that all files were imported into the specified folder.
     */
    private void verifyFolder(IContainer folder) {
        try {
            IResource[] files = folder.members();
            assertEquals("Import failed to import all files", fileNames.length,
                    files.length);
            for (int j = 0; j < fileNames.length; j++) {
                String fileName = fileNames[j];
                int k;
                for (k = 0; k < files.length; k++) {
                    if (fileName.equals(files[k].getName()))
                        break;
                }
                assertTrue("Import failed to import file " + fileName,
                        k < fileNames.length);
            }
        } catch (CoreException e) {
            fail(e.toString());
        }
    }
}
