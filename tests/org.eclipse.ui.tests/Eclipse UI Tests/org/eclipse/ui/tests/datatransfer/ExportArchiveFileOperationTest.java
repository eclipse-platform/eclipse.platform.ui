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
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class ExportArchiveFileOperationTest extends UITestCase implements
		IOverwriteQuery {
	
	private static String FILE_NAME = "test";
	private static String ZIP_FILE_EXT = "zip";
	private static String TAR_FILE_EXT = "tar";
	
    private String localDirectory;
    
    private String filePath;

    private String[] directoryNames = { "dir1", "dir2" };

    private String[] fileNames = { "file1.txt", "file2.txt" };

    private IProject project;
    
    private boolean flattenPaths = false;
    private boolean excludeProjectPath = false;
    
	public ExportArchiveFileOperationTest(String testName) {
		super(testName);
	}
	
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
        openTestWindow().run(true, true, operation);
        
        verifyFolders(directoryNames.length, ZIP_FILE_EXT);	
        
	}
	
	public void testExportZipCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setUseCompression(true);
        operation.setUseTarFormat(false);
        openTestWindow().run(true, true, operation);		
		verifyCompressed(ZIP_FILE_EXT);
	}
	
	public void testExportZipCreateSelectedDirectories() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++){
			if (isDirectory(members[i])){
				IResource[] folderMembers = ((IFolder)members[i]).members();
				for (int k = 0; k < folderMembers.length; k++){
					if (isFile(folderMembers[k])){
						resources.add(folderMembers[k]);
					}
				}
			}
		}
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(false);
        openTestWindow().run(true, true, operation);
        flattenPaths = true;
		verifyFolders(directoryNames.length, ZIP_FILE_EXT);		
	}
	
	public void testExportZipCreateSelectedDirectoriesWithFolders() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++){
			if (isDirectory(members[i]))
				resources.add(members[i]);
		}
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(false);
        openTestWindow().run(true, true, operation);
        excludeProjectPath = true;
		verifyFolders(directoryNames.length, ZIP_FILE_EXT);				
	}
	
	public void testExportZipCreateSelectedDirectoriesCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + ZIP_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++){
			if (isDirectory(members[i])){
				IResource[] folderMembers = ((IFolder)members[i]).members();
				for (int k = 0; k < folderMembers.length; k++){
					if (isFile(folderMembers[k])){
						resources.add(folderMembers[k]);
					}
				}
			}
		}
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(true);
        operation.setUseTarFormat(false);
        openTestWindow().run(true, true, operation);
        flattenPaths = true;
		verifyCompressed(ZIP_FILE_EXT);	
		verifyFolders(directoryNames.length, ZIP_FILE_EXT);
	}
	
	public void testExportTar() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);
        operation.setUseTarFormat(true);
        operation.setUseCompression(false);

        openTestWindow().run(true, true, operation);
        
        verifyFolders(directoryNames.length, TAR_FILE_EXT);	
	}
	
	public void testExportTarCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		resources.add(project);
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setUseTarFormat(true);
        operation.setUseCompression(true);
        openTestWindow().run(true, true, operation);		
		verifyCompressed(TAR_FILE_EXT);		
	}
	
	public void testExportTarCreateSelectedDirectories() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++){
			if (isDirectory(members[i])){
				IResource[] folderMembers = ((IFolder)members[i]).members();
				for (int k = 0; k < folderMembers.length; k++){
					if (isFile(folderMembers[k])){
						resources.add(folderMembers[k]);
					}
				}
			}
		}
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(true);
        openTestWindow().run(true, true, operation);
        flattenPaths = true;
		verifyFolders(directoryNames.length, TAR_FILE_EXT);			
	}
	
	public void testExportTarCreateSelectedDirectoriesWithFolders() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++){
			if (isDirectory(members[i]))
				resources.add(members[i]);
		}
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(false);
        operation.setUseTarFormat(true);
        openTestWindow().run(true, true, operation);
        excludeProjectPath = true;
		verifyFolders(directoryNames.length, TAR_FILE_EXT);				
		
	}
	
	public void testExportTarCreateSelectedDirectoriesCompressed() throws Exception {
		filePath = localDirectory + "/" + FILE_NAME + "." + TAR_FILE_EXT;
		List resources = new ArrayList();
		IResource[] members = project.members();
		for (int i = 0; i < members.length; i++){
			if (isDirectory(members[i])){
				IResource[] folderMembers = ((IFolder)members[i]).members();
				for (int k = 0; k < folderMembers.length; k++){
					if (isFile(folderMembers[k])){
						resources.add(folderMembers[k]);
					}
				}
			}
		}
        ArchiveFileExportOperation operation = 
        	new ArchiveFileExportOperation(resources, filePath);

        operation.setCreateLeadupStructure(false);
        operation.setUseCompression(true);
        operation.setUseTarFormat(true);
        openTestWindow().run(true, true, operation);
        flattenPaths = true;
		verifyCompressed(TAR_FILE_EXT);	
		verifyFolders(directoryNames.length, TAR_FILE_EXT);
		
	}

    protected void doSetUp() throws Exception {
		super.doSetUp();
		project = FileUtil.createProject("Export" + getName());
		File destination = 
			new File(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir())
    			.toOSString());
		localDirectory = destination.getAbsolutePath();
		if (!destination.mkdirs())
			fail("Could not set up destination directory for " + getName());
	    setUpData();
	    flattenPaths = false;
	    excludeProjectPath = false;
	}
    
	protected void doTearDown() throws Exception {
        super.doTearDown();
        // delete exported data
        File root = new File(localDirectory);
        if (root.exists()){
        	File[] files = root.listFiles();
        	if (files != null){
        		for (int i = 0; i < files.length; i++)
        			if (!files[i].delete())
        				fail("Could not delete " + files[i].getAbsolutePath());
        	}
        	root.delete();
        }
        try {
            project.delete(true, true, null);
        } catch (CoreException e) {
            fail(e.toString());
        }
	}
	
    private void setUpData(){
    	try{
	    	for(int i = 0; i < directoryNames.length; i++){
	    		IFolder folder = project.getFolder(directoryNames[i]);
	    		folder.create(false, true, new NullProgressMonitor());
	    		for (int k = 0; k < fileNames.length; k++){
	    			IFile file = folder.getFile(fileNames[k]);
	    			String contents =
	    				directoryNames[i] + ", " + fileNames[k];		
	    			file.create(new ByteArrayInputStream(contents.getBytes()), 
	    				true, new NullProgressMonitor());
	    		}
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
    	catch (Exception e){
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
	    	if (flattenPaths)
	    		verifyFiles(allEntries);
	    	else
	    		verifyArchive(folderCount, allEntries);
    	}
    	catch (Exception e){
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
			files.add(fileName);
			int idx2 = folderPath.lastIndexOf("/");
			if (idx2 != -1){
    			String folderName = folderPath.substring(idx2 + 1, folderPath.length());
    			folderNames.add(folderName);
			}
			else 
				folderNames.add(folderPath);

    	}
    	verifyFolders(folderNames);
    	verifyFiles(files);
    	count += folderNames.size();
    	if (!flattenPaths && !excludeProjectPath)
    		folderCount++;
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
    	for (int i = 0; i < fileNames.length; i++){
    		boolean dotProjectFileShouldBePresent = ".project".equals(entryName) && !flattenPaths && !excludeProjectPath;
    		if (fileNames[i].equals(entryName) || dotProjectFileShouldBePresent)
    			return;
    	}
    	fail("Could not find file named: " + entryName);
    }
    
    private void verifyFolders(Set folderNames){
    	Iterator folders = folderNames.iterator();
    	while (folders.hasNext()){
    		String folderName = (String)folders.next();
    		if (!isDirectory(folderName)){
    			if (flattenPaths)
    				fail(folderName + " is not an expected folder");
    			else if (!project.getName().equals(folderName))
    				fail(folderName + " is not an expected folder");
    		}
    	}
    }
    
    private boolean isDirectory(String name){
    	for (int i = 0; i < directoryNames.length; i++){
    		if (directoryNames[i].equals(name))
    			return true;
    	}
    	return false;
    }
    
	private boolean isDirectory(IResource resource){
		for (int i = 0; i < directoryNames.length; i++){
			if (directoryNames[i].equals(resource.getName()))
				return true;
		}
		return false;
	}
	
	private boolean isFile(IResource resource){
		for (int i = 0; i < fileNames.length; i++){
			if (fileNames[i].equals(resource.getName()))
				return true;
		}
		return false;		
	}
	
}
