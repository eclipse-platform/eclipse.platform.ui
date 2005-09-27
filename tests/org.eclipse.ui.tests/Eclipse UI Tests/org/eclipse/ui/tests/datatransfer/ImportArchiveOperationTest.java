/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

public class ImportArchiveOperationTest extends UITestCase implements IOverwriteQuery {
	private static final String DATA_PATH_PREFIX = "data/org.eclipse.datatransferArchives/";

    private String localDirectory;

    private String[] directoryNames = { "dir1", "dir2" };

    private String[] fileNames = { "file1.txt", "file2.txt" };

    private IProject project;
    
    private URL zipFileURL;
    
    private URL tarFileURL;
    
    public ImportArchiveOperationTest(String testName) {
        super(testName);
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
                .forName("org.eclipse.ui.tests.datatransfer.ImportArchiveOperationTest");
        InputStream stream = testClass.getResourceAsStream("tests.ini");
        Properties properties = new Properties();
        properties.load(stream);
        String zipFileName = properties.getProperty("archiveSource");
        localDirectory = zipFileName;
        
        zipFileURL = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
        		new Path(DATA_PATH_PREFIX).append(zipFileName+ ".zip")));
        tarFileURL = Platform.asLocalURL(Platform.find(TestPlugin.getDefault().getBundle(), 
        		new Path(DATA_PATH_PREFIX).append(zipFileName+ ".tar")));
    }

    /**
     * Tear down. Delete the project we created and all of the
     * files on the file system.
     */
    protected void doTearDown() throws Exception {
        super.doTearDown();
        try {
            project.delete(true, true, null);
        } catch (CoreException e) {
            fail(e.toString());
        }
    }

    public void testZipGetStatus() throws Exception {
        project = FileUtil.createProject("ImportZipGetStatus");
        ZipFile zipFile = new ZipFile(zipFileURL.getPath());
        
        ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(zipFile);
        
        Enumeration zipEntries = zipFile.entries();
        List entries = new ArrayList();
        while (zipEntries.hasMoreElements()){
        	entries.add(zipEntries.nextElement());
        }
		ImportOperation operation = new ImportOperation(
				new Path(zipFileURL.getPath()), structureProvider.getRoot(),
				structureProvider, this, entries);

		closeZipFile(zipFile);
        assertTrue(operation.getStatus().getCode() == IStatus.OK);
    }

    public void testTarGetStatus() throws Exception {
        project = FileUtil.createProject("ImportTarGetStatus");
        TarFile tarFile = new TarFile(tarFileURL.getPath());
        TarLeveledStructureProvider structureProvider = new TarLeveledStructureProvider(tarFile);
        
        Enumeration tarEntries = tarFile.entries();
        List entries = new ArrayList();
        while (tarEntries.hasMoreElements()){
        	entries.add(tarEntries.nextElement());
        }
		ImportOperation operation = new ImportOperation(
				new Path(tarFileURL.getPath()), structureProvider.getRoot(),
				structureProvider, this, entries);

        assertTrue(operation.getStatus().getCode() == IStatus.OK);
    }
    
    public void testZipImport() throws Exception {
        project = FileUtil.createProject("ImportZip");
        ZipFile zipFile = new ZipFile(zipFileURL.getPath());
        ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(zipFile);
        zipFile = new ZipFile(zipFileURL.getPath());
	    Enumeration zipEntries = zipFile.entries();
	    List entries = new ArrayList();
	    while (zipEntries.hasMoreElements()){
	    	ZipEntry entry = (ZipEntry)zipEntries.nextElement();
	    	if (!entry.isDirectory())
	    		entries.add(entry);
	    }
		ImportOperation operation = new ImportOperation(
				new Path(project.getName()), structureProvider.getRoot(),
				structureProvider, this, entries);
		
        openTestWindow().run(true, true, operation);
        closeZipFile(zipFile);
        
        verifyFiles(directoryNames.length);
    }

    
    public void testTarImport() throws Exception {
        project = FileUtil.createProject("ImportTar");
        TarFile tarFile = new TarFile(tarFileURL.getPath());
        TarLeveledStructureProvider structureProvider = new TarLeveledStructureProvider(tarFile);
    
	    Enumeration tarEntries = tarFile.entries();
	    List entries = new ArrayList();
	    while (tarEntries.hasMoreElements()){
	    	entries.add(tarEntries.nextElement());
	    }
		ImportOperation operation = new ImportOperation(
				new Path(project.getName()), structureProvider.getRoot(),
				structureProvider, this, entries);
        
        openTestWindow().run(true, true, operation);

        verifyFiles(directoryNames.length);
    }
    
    public void zztestImportTarSource() throws Exception {
        project = FileUtil.createProject("ImportTarSource");
        ImportOperation operation = new ImportOperation(project.getFullPath(),
                new File(localDirectory), FileSystemStructureProvider.INSTANCE,
                this);
        openTestWindow().run(true, true, operation);
        verifyFiles(directoryNames.length);
    }

    public void testTarSetOverwriteResources() throws Exception {
        project = FileUtil.createProject("ImportTarSetOverwriteResources");
        TarFile tarFile = new TarFile(tarFileURL.getPath());
        TarLeveledStructureProvider structureProvider = new TarLeveledStructureProvider(tarFile);
    
	    Enumeration tarEntries = tarFile.entries();
	    List entries = new ArrayList();
	    while (tarEntries.hasMoreElements()){
	    	entries.add(tarEntries.nextElement());
	    }
		ImportOperation operation = new ImportOperation(
				new Path(project.getName()), structureProvider.getRoot(),
				structureProvider, this, entries);
        
        openTestWindow().run(true, true, operation);
        operation.setOverwriteResources(true);
        openTestWindow().run(true, true, operation);
        verifyFiles(directoryNames.length);
    }
    
    public void testZipSetOverwriteResources() throws Exception {
    	project = FileUtil.createProject("ImporZiprSetOverwriteResources");
        ZipFile zipFile = new ZipFile(zipFileURL.getPath());
        ZipLeveledStructureProvider structureProvider = new ZipLeveledStructureProvider(zipFile);
        zipFile = new ZipFile(zipFileURL.getPath());
	    Enumeration zipEntries = zipFile.entries();
	    List entries = new ArrayList();
	    while (zipEntries.hasMoreElements()){
	    	ZipEntry entry = (ZipEntry)zipEntries.nextElement();
	    	if (!entry.isDirectory())
	    		entries.add(entry);
	    }
		ImportOperation operation = new ImportOperation(
				new Path(project.getName()), structureProvider.getRoot(),
				structureProvider, this, entries);
		
        openTestWindow().run(true, true, operation);
        operation.setOverwriteResources(true);
        openTestWindow().run(true, true, operation);
        closeZipFile(zipFile);  
        verifyFiles(directoryNames.length);
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
    
    private boolean closeZipFile(ZipFile zipFile){
    	try{
    		zipFile.close();
    	}
    	catch(IOException e){
    		fail("Could not close zip file " + zipFile.getName(), e);
    		return false;
    	}
    	return true;
    }
}
