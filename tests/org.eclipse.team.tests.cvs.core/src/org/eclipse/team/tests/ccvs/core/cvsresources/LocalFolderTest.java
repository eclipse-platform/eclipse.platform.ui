package org.eclipse.team.tests.ccvs.core.cvsresources;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;

import junit.awtui.TestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.NotCVSFolderException;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.Synchronizer;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;

public class LocalFolderTest extends JUnitTestCase {

	ICVSFile file1;
	ICVSFile file1a;
	ICVSFile file2;

	ICVSFolder folder1;
	ICVSFolder folder1a;
	ICVSFolder folder2;
	
	String entryLine1;
	String entryLine2;
	String entryLineExtra1;
	String entryLineExtra2;
	String entryLineExtra3;
	
	String root;
	String repo;
	String root2;
	String repo2;
	
	ResourceSyncInfo fileInfo1;

	FolderSyncInfo folderInfo1;
	FolderSyncInfo folderInfo2;
	
	File ioFile1;
	
	public LocalFolderTest(String arg) {
		super(arg);
	}
	
	public static void main(String[] args) {
		TestRunner.run(LocalFolderTest.class);
	}

	/**
	 * Leaves this file-structure:
	 * 
	 * test (folder1, folder1a)
	 *   proj1 (folder2, ioFolder2) *** Does not exist ***
	 *   file1.txt (file1, file1a)
	 *   file2.txt (file2) *** Does not exist ***
	 */
	public void setUp() throws CVSException, IOException {
				
		folder1 = getManagedFolder("test");
		folder1a = getManagedFolder("test");
		folder2 = folder1.getFolder("proj1");
		folder1.mkdir();
		assertTrue(folder1.exists());
		
		file1 = folder1.getFile("file1.txt");
		file1a = folder1.getFile("file1.txt");
		file2 = folder1.getFile("file2.txt");
		
		root = ":server:user:pwd@test:/home";
		repo = "repository";
		root2 = ":method:user:pwd@test2:/home/myFolder/repo";
		repo2 = "proj1/subdir";
		
		folderInfo1 = new FolderSyncInfo(repo,root,null,false);
		folderInfo2 = new FolderSyncInfo(repo2,root2,null,true);
		
		entryLine1 = "/.vcm_meta/1.1/27 Sep 2001 18:00:16/-kb/TmyTag";
		entryLine2 = "/file3.txt/1.1/27 Sep 2001 18:00:16/-kb/TmyTag";
		entryLineExtra1 = "/file1.txt/1.1/27 Sep 2001 18:00:16/-kb/TmyTag";
		entryLineExtra2 = "/file1.txt/1.2/27 Sep 2001 18:00:16/-kb/TmyTag";
		entryLineExtra3 = "/file2.txt/1.2/27 Sep 2001 18:00:16/-kb/TmyTag";
		
		folder1.setFolderSyncInfo(folderInfo1);
		
		fileInfo1 = new ResourceSyncInfo(entryLineExtra1,null,null);
		file1.setSyncInfo(fileInfo1);
		
		ioFile1 = getFile("test/file1.txt");
		ioFile1.createNewFile();
		
	}
	
	public void tearDown() throws CVSException {
		folder1.delete();
		Synchronizer.getInstance().reload(folder1,new NullProgressMonitor());
		assertSynchronizerEmtpy();
		assertTrue(!folder1.exists());
	}

	public static Test suite() {		
		TestSuite suite = new TestSuite(LocalFolderTest.class);
    	return suite; 	
	}
	
	public void testGetFolders() throws CVSException {
		
		// tests setFolderSyncInfo and getFolderSyncInfo as well
		
		boolean fail = false;
		ICVSFolder folder3;
		ICVSFolder tmpFolder;
		ICVSFolder[] resultFolders;
		
		folder3 = folder1.getFolder("folder3");
		folder3.mkdir();
		
		resultFolders = folder2.getFolders();
		assertEquals(0, resultFolders.length);
		
		// For the moment we assume, that seting a folderInfo
		// does not create the folder automatically but you
		// have to create it with mkdir.
		try {
			folder2.setFolderSyncInfo(folderInfo1);
			fail();
		} catch (CVSException e) {			
		}		
				
		resultFolders = folder1.getFolders();
		assertEquals(1, resultFolders.length);
		
		folder2.mkdir();
		
		resultFolders = folder1.getFolders();
		assertEquals(2, resultFolders.length);
		
		folder2.delete();

		resultFolders = folder1.getFolders();
		assertEquals(1, resultFolders.length);
		
		folder2.mkdir();
		folder2.setFolderSyncInfo(folderInfo1);	
		
		resultFolders = folder1.getFolders();
		assertEquals(2, resultFolders.length);
		
		folder2.delete();
		
		resultFolders = folder1.getFolders();
		assertEquals(2, resultFolders.length);
		
		tmpFolder = folder1.getFolder("proj1");
		assertEquals(false,tmpFolder.exists());
		assertEquals(true,tmpFolder.isManaged());				
	}
	
	public void testGetFiles() throws Exception {
		// tests setFolderSyncInfo and getFolderSyncInfo as well
		
		boolean fail = false;
		ICVSFile[] resultFiles;
		ICVSFile file3;
		File ioFile3;
		ResourceSyncInfo fileInfo2;
		
		file3 = folder1.getFile("file3.txt");
		ioFile3 = getFile("test/file3.txt");
		
		// From here we check if the filelist does include files,
		// that are added locally but not as entries (and exclude them
		// again when we delete them)
		resultFiles = folder1.getFiles();
		assertEquals(1, resultFiles.length);
		
		ioFile3.createNewFile();
		
		resultFiles = folder1.getFiles();
		assertEquals(2, resultFiles.length);
		
		ioFile3.delete();

		resultFiles = folder1.getFiles();
		assertEquals(1, resultFiles.length);
		
		// Here is tested if we get files that do not exist locally but 
		// in the entries
		
		// first we try a fileinfo with the wrong name
		try {
			file3.setSyncInfo(fileInfo1);
		} catch (Throwable e) {fail = true;}		
		assertTrue(fail);
		fail = false;
		
		fileInfo2 = new ResourceSyncInfo(entryLine2,null,null);
		
		file3.setSyncInfo(fileInfo2);

		resultFiles = folder1.getFiles();
		assertEquals(2, resultFiles.length);
		assertEquals(false,file3.exists());
		assertEquals(true,file3.isManaged());
		
		file3.unmanage();
		
		resultFiles = folder1.getFiles();
		assertEquals(1, resultFiles.length);
		assertEquals(false,file3.exists());
		assertEquals(false,file3.isManaged());
	}
	
	public void testGetChild() throws CVSException {

		try {
			folder1.getChild("proj1");
			assertTrue(false);
		} catch (CVSException e) {}		
		
		try {
			folder1.getChild("file2.txt");
			assertTrue(false);
		} catch (CVSException e) {}		
		
		folder2.mkdir();
		
		assertEquals(true, folder1.getChild("proj1").isFolder());
		assertEquals("proj1", folder1.getChild("proj1").getName());
		assertEquals(false, folder1.getChild("file1.txt").isFolder());
		
	}
	
	// ---------------- Here the resource-tests start ---------------
	
	public void testExists() throws Exception {
		assertEquals(false, folder2.exists());
		folder2.mkdir();
		assertEquals(true, folder2.exists());
		folder2.delete();
		assertEquals(false, folder2.exists());
	}
	
	public void testGetName() throws CVSException {
		assertEquals("proj1", folder2.getName());	
		folder2.mkdir();
		assertEquals("proj1", folder2.getName());	
	}
	
	public void testGetParent() {
		assertEquals(folder1,folder2.getParent());
	}

	public void testGetRelativePath() throws CVSException {
		ICVSFolder subFolder1;
		ICVSFolder subFolder2;
		ICVSFolder subFolder3;
		String sep =  "/";
		
		boolean fail = false;
		
		subFolder1 = folder1.getFolder("proj1");
		subFolder2 = subFolder1.getFolder("proj2");
		subFolder3 = subFolder2.getFolder("proj3");

		assertEquals("proj1", folder2.getRelativePath(folder1));		
		assertEquals("testGetRelativeLocation.1","proj1", subFolder1.getRelativePath(folder1));
		assertEquals("testGetRelativeLocation.2","proj1" + sep + "proj2" + sep + "proj3", subFolder3.getRelativePath(folder1));
		assertEquals("testGetRelativeLocation.3","proj2", subFolder2.getRelativePath(subFolder1));
		assertEquals("testGetRelativeLocation.3a","", subFolder2.getRelativePath(subFolder2));
		
		try {
			subFolder1.getRelativePath(subFolder2);
		} catch (CVSException e) {fail = true;}
		assertTrue("testGetRelativeLocation.4",fail);	
		fail = false;		
		
		subFolder1.delete();
	}

	public void testIsFolder() {
		assertEquals(true, folder2.isFolder());
	}		

	public void testFolderIsManaged() throws CVSException {
		ICVSFolder folder = getManagedFolder("testIsManaged");
		folder.mkdir();
		assertEquals(null, folder.getFolderSyncInfo());
		
		FolderSyncInfo info = new FolderSyncInfo("module", ":pserver:user@host:/home", null, false);
		folder.setFolderSyncInfo(info);
		assertTrue(folder.isCVSFolder() && !folder.isManaged());
		assertTrue(folder.getFolderSyncInfo().equals(info));		
		
		folder.delete();
		Synchronizer.getInstance().reload(folder, new NullProgressMonitor());
	}
	
	public void testFolderInfo() throws CVSException {
		FolderSyncInfo folderInfo2;
		FolderSyncInfo folderInfo3;
		String repo;
		String root;
		
		repo = "proj1/folder1";
		root = ":pserver:nkram:pwd@fiji:/home/nkrambro/repo";
		
		assertEquals(null, folder2.getFolderSyncInfo());
		assertEquals(false,folder2.isCVSFolder());
		
		folder1.setFolderSyncInfo(folderInfo1);
		folderInfo2 = folder1.getFolderSyncInfo();
		
		assertEquals(true,folder1.isCVSFolder());	
		assertEquals(this.root,folderInfo2.getRoot());
		assertEquals(this.repo,folderInfo2.getRepository());
		
		folder1.unmanage();
		
		assertEquals(false,folder1.isCVSFolder());	
		assertEquals(null,folder1.getFolderSyncInfo());	
		
		folderInfo3 = new FolderSyncInfo(repo,root,folderInfo2.getTag(),false);

		folder1.setFolderSyncInfo(folderInfo3);
		
		assertEquals(root,folder1.getFolderSyncInfo().getRoot());
		assertEquals(repo,folder1.getFolderSyncInfo().getRepository());
		assertEquals("/home/repository",folderInfo2.getRemoteLocation());
	}
	
	public void testsetSyncInfo() throws CVSException {
		String entry1 = "/file1.txt/a/b/c/";
		String entry2 = "/file1.txt/b/b/c/";
		String entry3 = "/file2.txt/b/b/c/";
		
		ResourceSyncInfo info1 = new ResourceSyncInfo(entry1,null,null);
		ResourceSyncInfo info2 = new ResourceSyncInfo(entry2,null,null);
		ResourceSyncInfo info3 = new ResourceSyncInfo(entry3,null,null);

		ICVSFile file1 = this.file1;
		ICVSFile file2 = folder1.getFile("file2.txt");
		
		assertEquals(1,folder1.getFiles().length);
		file1.setSyncInfo(info1);
		assertEquals(1,folder1.getFiles().length);
		file1.setSyncInfo(info2);
		assertEquals(1,folder1.getFiles().length);
		file2.setSyncInfo(info3);
		assertEquals(2,folder1.getFiles().length);
		
		assertEquals(entry2,file1.getSyncInfo().getEntryLine(true));
		assertEquals(entry3,file2.getSyncInfo().getEntryLine(true));
		
		// The two files in the getFiles are acctually the files
		// we put in there
		assertTrue( ( folder1.getFiles()[0].equals(file1) ||
			      folder1.getFiles()[0].equals(file2)) &&
			    ( folder1.getFiles()[1].equals(file1) ||
			      folder1.getFiles()[1].equals(file2)));
	}
	
	public void testSimpleGetFiles() throws CVSException {
		// When a file is added to the entries it should be in the list
		// of files afterwards ... this should be one of the current problems
		file2.setSyncInfo(new ResourceSyncInfo(entryLineExtra3,null,null));
		assertEquals(2,folder1.getFiles().length);
		file2.unmanage();
		assertEquals(1,folder1.getFiles().length);
	}
	
	public void testSimpleResourceSyncInfo() throws Exception {
		
		// ?? Is this an requirment or is it not ?
		// assertEquals(file1a.getSyncInfo(),fileInfo1);
		
		Synchronizer.getInstance().save(new NullProgressMonitor());
		assertEquals(file1a.getSyncInfo(),fileInfo1);
		
		file1a.setSyncInfo(new ResourceSyncInfo(entryLineExtra2,null,null));
		Synchronizer.getInstance().save(new NullProgressMonitor());
		Synchronizer.getInstance().reload(folder1,new NullProgressMonitor());
		assertEquals(file1.getSyncInfo().getEntryLine(true),entryLineExtra2);
		
		file1a.setSyncInfo(new ResourceSyncInfo(entryLineExtra1,null,null));
		Synchronizer.getInstance().save(new NullProgressMonitor());
		Synchronizer.getInstance().reload(folder1,new NullProgressMonitor());
		assertEquals(file1.getSyncInfo().getEntryLine(true),entryLineExtra1);
		
		file1a.setSyncInfo(new ResourceSyncInfo(entryLineExtra2,null,null));
		Synchronizer.getInstance().save(new NullProgressMonitor());
		Synchronizer.getInstance().reload(file1a.getParent().getParent().getParent(),new NullProgressMonitor());
		assertEquals(file1.getSyncInfo().getEntryLine(true),entryLineExtra2);	
	}
	
	public void testSimpleFolderSyncInfo() throws Exception {
		folder1.setFolderSyncInfo(folderInfo2);
		assertEquals(folder1.getFolderSyncInfo().getRepository(),repo2);		
		assertEquals(folder1.getFolderSyncInfo().getRoot(),root2);		
		assertEquals(folder1.getFolderSyncInfo().getIsStatic(),true);

		Synchronizer.getInstance().save(new NullProgressMonitor());
		Synchronizer.getInstance().reload(folder1,new NullProgressMonitor());
		assertEquals(folder1.getFolderSyncInfo().getRepository(),repo2);		
		assertEquals(folder1.getFolderSyncInfo().getRoot(),root2);		
		assertEquals(folder1.getFolderSyncInfo().getIsStatic(),true);	

		folder1.setFolderSyncInfo(folderInfo1);
		Synchronizer.getInstance().save(new NullProgressMonitor());
		Synchronizer.getInstance().reload(folder1a,new NullProgressMonitor());
		assertEquals(folder1a.getFolderSyncInfo().getRepository(),repo);		
		assertEquals(folder1a.getFolderSyncInfo().getRoot(),root);	
		assertEquals(folder1a.getFolderSyncInfo().getIsStatic(),false);		
	}
	
	public void testSyncIsCvsFolder() throws Exception {
		
		folder1.delete();
		Synchronizer.getInstance().reload(folder1, new NullProgressMonitor());
		assertEquals(false,folder1.isCVSFolder());
		
		folder1.mkdir();
		assertEquals(false,folder1.isCVSFolder());
		
		folder1.setFolderSyncInfo(folderInfo1);
		Synchronizer.getInstance().save(new NullProgressMonitor());
		assertEquals(true,folder1.isCVSFolder());
		assertEquals(false,folder1.isManaged());
		
		assertEquals(false,folder2.isCVSFolder());

		folder2.mkdir();
		assertEquals(false,folder2.isCVSFolder());
		assertEquals(false,folder2.isManaged());

		folder2.setFolderSyncInfo(folderInfo2);
		Synchronizer.getInstance().save(new NullProgressMonitor());
		assertEquals(true,folder2.isCVSFolder());
		assertEquals(true,folder2.isManaged());
	}
	
	public void testNotExistingFail() throws Exception {
		
		ICVSFolder folder3 = folder2.getFolder("nextFolder");
		ICVSFile file3 = folder2.getFile("file1.txt");

		try {
			file3.setSyncInfo(fileInfo1);
			fail();
		} catch (Exception e) {}
		
		try {
			folder3.setSyncInfo(new ResourceSyncInfo(folder3.getName()));
			fail();
		} catch (Exception e) {}
		
		// I do not know whether to check for null or for the
		// file with an extended path
		folder2.getFile("this");
		folder2.getFolder("that");
		
		try {
			folder2.getChild("this");
			fail();
		} catch (CVSException e) {}
		
		try {
			folder2.setSyncInfo(new ResourceSyncInfo(folder2.getName()+"X"));
			fail();
		} catch (Exception e) {
		}		
	}		
}