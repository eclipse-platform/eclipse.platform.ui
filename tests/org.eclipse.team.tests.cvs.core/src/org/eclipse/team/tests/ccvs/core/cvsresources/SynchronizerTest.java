package org.eclipse.team.tests.ccvs.core.cvsresources;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.Synchronizer;
import org.eclipse.team.internal.ccvs.core.util.SyncFileUtil;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class SynchronizerTest extends EclipseTest {

	public SynchronizerTest() {
		super();
	}
	
	public SynchronizerTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(SynchronizerTest.class);
		return new CVSTestSetup(suite);
	}
	
	protected File getSyncFile(File parent, String syncFileName) {
		return new File(SyncFileUtil.getCVSSubdirectory(parent), syncFileName);
	}
	
	protected void appendLineToFile(File file, String line) throws IOException {

		BufferedReader fileReader;
		List fileContentStore = new ArrayList();
		
		if (!file.exists()) {
			return;
		}
		
		String l;
		fileReader = new BufferedReader(new FileReader(file));
		while ((l = fileReader.readLine()) != null) {
			fileContentStore.add(l);
		}
		fileReader.close();			

		String[] content = (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
		String[] newContent;
		
		newContent = new String[content.length + 1];
		System.arraycopy(content,0,newContent,0,content.length);
		newContent[content.length] = line;
		
		file.delete();
				
		BufferedWriter fileWriter;
		
		fileWriter = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i<newContent.length; i++) {
			fileWriter.write(newContent[i]);
			fileWriter.newLine();
		}
		fileWriter.close();
	}
	
	public void testFolderSync() throws CoreException, CVSException  {
		IProject project = getUniqueTestProject("testFolderSync");
		IResource[] resources = buildResources(project, new String[] {"a.txt", "folder1/", "folder1/b.txt", "folder2/", "folder2/c.txt"}, true);
		ensureExistsInFileSystem(resources);
		IFolder folder1 = project.getFolder("folder1");
		IFolder folder2 = project.getFolder("folder2");
		File folder1File = folder1.getLocation().toFile();
		File folder2File = folder2.getLocation().toFile();
		File projectFile = project.getLocation().toFile();
		
		// 1.
		
		try {
			Synchronizer.getInstance().setFolderSync(new File("dummy"), new FolderSyncInfo("repo", "root", null, false));
			fail();
		} catch(CVSException e) {
		}
		
		// 2. 
		
		FolderSyncInfo info = new FolderSyncInfo("repo", "root", null, false);
		Synchronizer.getInstance().setFolderSync(folder1File, info);
		
		// info was stored in cache correctly
		assertTrue(Synchronizer.getInstance().getFolderSync(folder1File).equals(info));
		// nothing should be on the disk yet
		assertTrue(!(getSyncFile(folder1File, SyncFileUtil.REPOSITORY).exists()));
		assertTrue(!(getSyncFile(folder1File, SyncFileUtil.ROOT).exists()));
		assertTrue(new LocalFolder(folder1File).isCVSFolder());
		assertTrue(!(new LocalFolder(folder1File).isManaged()));
		
		Synchronizer.getInstance().save(new NullProgressMonitor());
		
		// repo and root files should exist but not the others
		assertTrue(getSyncFile(folder1File, SyncFileUtil.REPOSITORY).exists());
		assertTrue(getSyncFile(folder1File, SyncFileUtil.ROOT).exists());
		assertTrue(!(getSyncFile(folder1File, SyncFileUtil.TAG).exists()));
		assertTrue(!(getSyncFile(folder1File, SyncFileUtil.STATIC).exists()));
		assertTrue(new LocalFolder(folder1File).isCVSFolder());
		
		// 3.
		
		info = new FolderSyncInfo("repo", "root", new CVSTag("v1", CVSTag.BRANCH), true);
		Synchronizer.getInstance().setFolderSync(folder1File, info);
		Synchronizer.getInstance().save(new NullProgressMonitor());
		
		// repo and root files should exist but not the others
		assertTrue(getSyncFile(folder1File, SyncFileUtil.REPOSITORY).exists());
		assertTrue(getSyncFile(folder1File, SyncFileUtil.ROOT).exists());
		assertTrue(getSyncFile(folder1File, SyncFileUtil.TAG).exists());
		assertTrue(getSyncFile(folder1File, SyncFileUtil.STATIC).exists());
		
		assertTrue(!(new File(folder1File.getParentFile(), "CVS").exists()));
		
		// 4. 
		
		Synchronizer.getInstance().setFolderSync(projectFile, info);
		Synchronizer.getInstance().setFolderSync(folder2File, info);
		
		// sub folder must be added to parent and relevant folder sync must exist
		Synchronizer.getInstance().save(new NullProgressMonitor());
		Synchronizer.getInstance().clear();
		
		assertTrue(getSyncFile(projectFile, SyncFileUtil.REPOSITORY).exists());
		assertTrue(getSyncFile(projectFile, SyncFileUtil.ROOT).exists());
		assertTrue(getSyncFile(projectFile, SyncFileUtil.TAG).exists());
		assertTrue(getSyncFile(projectFile, SyncFileUtil.STATIC).exists());

		assertTrue(Synchronizer.getInstance().getFolderSync(projectFile).equals(info));
		Synchronizer.getInstance().reload(new LocalFolder(projectFile), new NullProgressMonitor());
		assertTrue(Synchronizer.getInstance().members(projectFile)[0].getName().equals(folder2File.getName()));
	}

	public void testDeleteListener() throws CoreException, CVSException, TeamException {
		IProject project = createProject("testDeleteListener", new String[] {"a.txt", "folder1/", "folder1/b.txt"});
		IFolder folder1 = project.getFolder("folder1");
		IFile file1 = folder1.getFile("b.txt");
		ICVSFolder cvsFolder = new LocalFolder(folder1.getLocation().toFile());
		ICVSFolder cvsProject = new LocalFolder(project.getLocation().toFile());
		ICVSFile cvsfile  = new LocalFile(file1.getLocation().toFile());
		
		// 1. delete of a folder deletes sync info deep
		
		FolderSyncInfo info = cvsFolder.getFolderSyncInfo();
		assertTrue(info!=null);
		
		folder1.delete(true, new NullProgressMonitor());
		
		assertTrue(cvsFolder.getFolderSyncInfo()==null);
		assertTrue(cvsfile.getSyncInfo()==null);				
		
		// 2. rename of a project deletes cached sync info of source project
		
		project.move(new Path("movedProject"), true, new NullProgressMonitor());
		
		assertTrue(cvsProject.getFolderSyncInfo()==null);
	}
	
	public void testResourceSync() throws CVSException, CoreException, TeamException {
		IProject project = createProject("testResourceSync", new String[] {"a.txt", "folder1/", "folder1/b.txt"});
		IFolder folder1 = project.getFolder("folder1");
		IFile file1 = project.getFile("a.txt");
		IFile newFile = folder1.getFile("c.txt");
		IFolder newFolder = project.getFolder("folder2");
		ICVSFolder cvsFolder = new LocalFolder(folder1.getLocation().toFile());
		ICVSFolder cvsNewFolder = new LocalFolder(newFolder.getLocation().toFile());
		ICVSFile cvsFile = new LocalFile(file1.getLocation().toFile());
		ICVSFile cvsNewFile = new LocalFile(newFile.getLocation().toFile());
		ICVSFolder cvsProject = new LocalFolder(project.getLocation().toFile());
		
		// 1.
		
		assertTrue(!cvsProject.isManaged());
		assertTrue(cvsProject.isCVSFolder());
		assertTrue(cvsFile.getSyncInfo()!=null);
		assertTrue(cvsFolder.getSyncInfo()!=null);		
		
		assertTrue(cvsNewFile.getSyncInfo()==null);
		assertTrue(cvsNewFolder.getSyncInfo()==null);
				
		// 2.
		
		ResourceSyncInfo folderInfo = new ResourceSyncInfo(cvsNewFolder.getName());
		ResourceSyncInfo fileInfo = new ResourceSyncInfo(cvsNewFile.getName(), "1.1", "timestamp", "-kb", null, null);
		
		cvsNewFile.setSyncInfo(fileInfo);
		cvsNewFolder.setSyncInfo(folderInfo);
		
		assertTrue(cvsNewFile.getSyncInfo().equals(fileInfo));
		assertTrue(cvsNewFolder.getSyncInfo().equals(folderInfo));
		
		Synchronizer.getInstance().save(new NullProgressMonitor());
		
		assertTrue(cvsNewFile.getSyncInfo().equals(fileInfo));
		assertTrue(cvsNewFolder.getSyncInfo().equals(folderInfo));
	}	
	
	public void testReload() throws CVSException, CoreException, TeamException, IOException {
		IProject project = createProject("testReload", new String[] {"a.txt", "folder1/", "folder1/b.txt", "folder2/", "folder2/folder2a/", "folder2/folder2a/b.txt", "folder3/b.txt"});
		
		File projectFile = project.getLocation().toFile();
		IFile newFile = project.getFile("b.txt");
		newFile.create(getRandomContents(), true, null);
		IFolder folder1 = project.getFolder("folder1");
		IFolder newFolder = project.getFolder("newFolder");
		newFolder.create(true, true, null);
		ICVSFolder cvsProject = new LocalFolder(project.getLocation().toFile());
		ICVSFile cvsNewFile = new LocalFile(newFile.getLocation().toFile());
		ICVSFolder cvsNewFolder = new LocalFolder(newFolder.getLocation().toFile());
		ICVSFolder cvsFolder = new LocalFolder(folder1.getLocation().toFile());

		// 1. update entry from outside of synchronizer then reload
		assertTrue(cvsNewFile.getSyncInfo()==null);
		assertTrue(cvsNewFolder.getSyncInfo()==null);
		assertTrue(cvsFolder.getFolderSyncInfo()!=null);
		
		ResourceSyncInfo fileInfo = new ResourceSyncInfo("/b.txt/1.1/Thu Aug 30 15:31:40 2001/-kb/", null, null);
		ResourceSyncInfo folderInfo = new ResourceSyncInfo("newFolder");

		folder1.delete(true, true, null);

		appendLineToFile(getSyncFile(project.getLocation().toFile(), SyncFileUtil.ENTRIES), fileInfo.getEntryLine(true));
		appendLineToFile(getSyncFile(project.getLocation().toFile(), SyncFileUtil.ENTRIES), folderInfo.getEntryLine(true));
		
		Synchronizer.getInstance().reload(cvsProject, new NullProgressMonitor());
		
		assertTrue(cvsNewFile.getSyncInfo().equals(fileInfo));
		assertTrue(cvsNewFolder.getSyncInfo().equals(folderInfo));		
		assertTrue(cvsFolder.getFolderSyncInfo()==null);		
		assertTrue(new LocalFile(new File(folder1.getLocation().toFile(), "b.txt")).getSyncInfo()==null);
		
		// 2. delete multiple directory hierarchy and reload should delete everything from cache
		IFolder folder2 = project.getFolder("folder2");
		IFolder folder2a = folder2.getFolder("folder2a");
		ICVSFolder folder2aFile = new LocalFolder(folder2a.getLocation().toFile());
		ICVSFolder folder2File = new LocalFolder(folder2.getLocation().toFile());
		folder2File.delete();
		Synchronizer.getInstance().reload(folder2File,new NullProgressMonitor());
		Synchronizer.getInstance().save(new NullProgressMonitor());
		assertTrue(!folder2File.exists());
		assertTrue(folder2File.getSyncInfo()==null);
		assertTrue(folder2aFile.getSyncInfo()==null);
		
		// 3. reload should delete cache if entries not found
		cvsProject.delete();
		Synchronizer.getInstance().reload(cvsProject,new NullProgressMonitor());
		Synchronizer.getInstance().save(new NullProgressMonitor());
		assertTrue(!cvsProject.exists());
	}

}