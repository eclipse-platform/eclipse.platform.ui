package org.eclipse.team.tests.ccvs.core.cvsresources;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import junit.awtui.TestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.Synchronizer;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;


public class LocalFileTest extends JUnitTestCase {
	
	ICVSFile file1;
	ICVSFile file1a;
	ICVSFile file2;
	ICVSFolder folder1;
	
	ResourceSyncInfo fileInfo1;
	
	File ioFile;
	
	
	public LocalFileTest(String arg) {
		super(arg);
	}
	
	public static void main(String[] args) {
		TestRunner.run(LocalFileTest.class);
	}


	public void setUp() throws CVSException {
		
		String tmpEntryLine1;


		String root = ":server:user:pwd@test:/home";
		String repo = "repository";		
		folder1 = getManagedFolder("proj1");
		folder1.mkdir();
		folder1.setFolderSyncInfo(new FolderSyncInfo(repo,root,null,false));
		
		file1 = folder1.getFile("file1.txt");
		file1a = folder1.getFile("file1.txt");
		file2 = folder1.getFile("file2.txt");
		
		tmpEntryLine1 = "/.vcm_meta/1.1/Thu Sep 27 18:00:16 2001/-kb/TmyTag";
		fileInfo1 = new ResourceSyncInfo(tmpEntryLine1,null,null);
		
		ioFile = getFile("proj1/file1.txt");
		
	}
	
	public void tearDown() throws CVSException {
		folder1.delete();
		Synchronizer.getInstance().reload(folder1,new NullProgressMonitor());
		assertSynchronizerEmtpy();			
		assertTrue(!folder1.exists());
	}

	public static Test suite() {		
		TestSuite suite = new TestSuite(LocalFileTest.class);
    	return suite; 	
	}
	
	public void testFileInfo() throws CVSException {
		
		String entryLine1;
		String entryLine2;
		String entryLine3;
		boolean fail=false;
	
		entryLine1 = "/.vcm_meta/1.1/27 Sep 2001 18:00:16/-kb/TmyTag";
		entryLine2 = "/file1.txt/1.1/27 Sep 2001 18:00:16/-kb/TmyTag";
		entryLine3 = "/file1.txt/1.2/27 Sep 2001 18:00:16/-kb/TmyTag";

		ResourceSyncInfo fileInfo1 = new ResourceSyncInfo(entryLine1,null,null);
		ResourceSyncInfo fileInfo2 = new ResourceSyncInfo(entryLine2,null,null);
		ResourceSyncInfo fileInfo3 = new ResourceSyncInfo(entryLine3,null,null);

		file1.setSyncInfo(fileInfo2);
		
		assertEquals(file1a.getSyncInfo(),fileInfo2);
	
		file1a.setSyncInfo(fileInfo3);
		
		assertEquals(file1.getSyncInfo(),fileInfo3);
	}
	
	public void testSendReceive() throws Exception {
		
		String sendTxt;
		String expectTxt;
		InputStream in;

		sendTxt = "This is my text";
		expectTxt = sendTxt.length() + "\n" + sendTxt;
		
		byte[] result = new byte[sendTxt.length()];
		
		PipedInputStream pIn;
		PipedOutputStream pOut;
		
		pIn = new PipedInputStream();
		pOut = new PipedOutputStream(pIn);
		
		in = new BufferedInputStream(pIn,sendTxt.length());
		
		pOut.write(sendTxt.getBytes());
		file1.receiveFrom(in,new NullProgressMonitor(),sendTxt.length(),false,false);
		in.close();
		pOut.close();
		
		result = new byte[expectTxt.length()];
		pIn = new PipedInputStream();
		pOut = new PipedOutputStream(pIn);
		
		in = new BufferedInputStream(pIn,sendTxt.length());
		file1.sendTo(pOut,new NullProgressMonitor(),false);
		in.read(result);
		in.close();
		pOut.close();
		
		assertEquals(new String(result),expectTxt);	
	}
	
	public void testTimestamp() throws Exception {
		
		String timeStamp;
		
		ioFile.createNewFile();
		timeStamp = "Tue Oct 30 14:38:16 2001";
		
		file1.setTimeStamp(timeStamp);
		assertEquals(timeStamp, file1a.getTimeStamp());
		
	}
	
	public void testIsDirty() throws Exception {
		
		String timeStamp;
		String entryLine;
		ResourceSyncInfo fileInfo;
		
		ioFile.createNewFile();
		timeStamp = "Tue Oct 30 14:38:16 2001";
		entryLine = "/file1.txt/1.1/Tue Oct 30 14:38:16 2001/-kb/TmyTag";
		
		file1.setTimeStamp(timeStamp);
		timeStamp = file1.getTimeStamp();
		
		fileInfo = new ResourceSyncInfo(entryLine,null,timeStamp);
		
		file1.setSyncInfo(fileInfo);
		
		assertEquals(false, file1.isDirty());
		
		// touch the file
		writeToFile(ioFile,readFromFile(ioFile));
		
		assertEquals(true, file1.isDirty());

		file1.setTimeStamp(timeStamp);

		assertEquals(false, file1.isDirty());	
	}
		
	// ---------------- Here the resource-tests start ---------------
	
	public void testExists() throws Exception {
		
		file1.delete();
		assertEquals(false, file1.exists());
		writeToFile(ioFile,new String[0]);
		assertEquals(true, file1.exists());
		
	}
	
	public void testGetName() {
		assertEquals("file1.txt", file1.getName());	
	}
	
	public void testGetParent() {
		assertEquals(folder1,file1.getParent());	
	}
	
	public void testGetRelativePath() throws CVSException {
		assertEquals("file1.txt", file1.getRelativePath(folder1));
	}
	
	public void testIsFolder() {
		assertEquals(false, file1.isFolder());
	}		

	public void testIsManaged() throws CVSException {
		
		ResourceSyncInfo fileInfo2 = new ResourceSyncInfo("/file1.txt/1.1/27 Sep 2001 18:00:16/-kb/TmyTag",null,null);
		
		assertEquals(false, file1.isManaged());
		file1.setSyncInfo(fileInfo2);
		assertEquals(true, file1.isManaged());
		
	}
}

