package org.eclipse.team.tests.ccvs.core.provider;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.Synchronizer;
import org.eclipse.team.internal.ccvs.core.util.FileUtil;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;

/**
 * This class tests the basic functionality of the Eclipse CVS client
 * and can be used to test basic sanity.
 * 
 * It does not run against another cvs client.
 */
public class CommandsTest extends JUnitTestCase {


	static final String PLATFORM_NEWLINE = System.getProperty("line.separator");
		
	private File ioFolder1;
	private File ioFolder2;
	private File ioFolder3;
	private File ioFolder4;
	
	private String[] arguments;
	
	private boolean isSetUp = false;
	
	public static void main(String[] args) {	
		run(CommandsTest.class);
	}
	
	public void setUp() throws Exception {
		if (!isSetUp) {
			try {
				magicSetUpRepo("proj1", new String[] { "folder1/c.txt", "folder1/d.txt", "folder2/test.flag"});
			} catch (Exception e) {
				System.err.println("Could not setup repository");
			}
			isSetUp = true;
		}
		ioFolder1 = getFile("test1");
		ioFolder2 = getFile("test2");
		ioFolder3 = getFile("test2/proj1");
		ioFolder4 = getFile("test2/proj1/folder1");
		
		FileUtil.deepDelete(ioFolder1);
		FileUtil.deepDelete(ioFolder2);
		ioFolder1.mkdir();
		ioFolder2.mkdir();
		
	}
	
	public void tearDown() throws CVSException {
		FileUtil.deepDelete(ioFolder1);
		FileUtil.deepDelete(ioFolder2);
		Synchronizer.getInstance().clear();
	}
	
	public CommandsTest() {
		super("CommandsTest");
		arguments = new String[]{"proj1"};
	}
	
	public CommandsTest(String name) {
		super(name);
		arguments = new String[]{"proj1"};
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(CommandsTest.class);
		return new CVSTestSetup(suite);
	}
	
	/**
	 * This should somehow create the following file-structure:
	 * 
	 * temp
	 *   test1
	 *     proj1
	 *       CVS
	 *       folder1
	 *         CVS
	 *         c.txt
	 *         d.txt
	 *       folder2
	 *         CVS
	 *         test.flag
	 *   test2
	 *     proj1
	 *       CVS
	 *       folder1
	 *         CVS
	 *         c.txt
	 *         d.txt
	 *       folder2
	 *         CVS
	 *         test.flag
	 * 
	 * where the cvs-folder contains the appropiate files including
	 * the appropiated Entries-files
	 */
	public void subTestCeckout() throws CVSException {

		execute("co",globalOptions, 
							EMPTY_ARGS, 
							arguments, 
							ioFolder1, 
							monitor,
							System.err);

		execute("co",globalOptions, 
							EMPTY_ARGS, 
							arguments, 
							ioFolder2, 
							monitor,
							System.err);
	}
	
	public void testCommitFiles() throws Exception {
		subTestCeckout();
		subTestCommitUpdate();
	}
	
	/**
	 * Made for checking the ability of the client to cope with 
	 * multible folders and files as arguments
	 */
	public void testDoubleRound() throws Exception {
		
		File folder1;
		File folder2;
		
		File file1a;
		File file2a;
		File file3a;
		File file4a;

		File file1b;
		File file2b;
		File file3b;
		File file4b;

		folder1 = ioFolder1;
		folder2 = ioFolder2;
		
		file1a = new File(folder1,"coProject1/a.txt");
		file2a = new File(folder1,"coProject2/a.txt");
		file3a = new File(folder2,"coProject1/a.txt");
		file4a = new File(folder2,"coProject2/a.txt");

		file1b = new File(folder1,"coProject1/f1/b.txt");
		file2b = new File(folder1,"coProject2/f1/b.txt");
		file3b = new File(folder2,"coProject1/f1/b.txt");
		file4b = new File(folder2,"coProject2/f1/b.txt");
		
		
		// Do the setup ...
		magicSetUpRepo("coProject1",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt"});
		magicSetUpRepo("coProject2",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt"});
		
		execute("co",
							globalOptions,
							EMPTY_ARGS,
							new String[]{"coProject1","coProject2"},
							folder1,
							monitor,
							System.err);
		
		execute("co",
							globalOptions,
							EMPTY_ARGS,
							new String[]{"coProject1","coProject2"},
							folder2,
							monitor,
							System.err);
		
		// Change the two projects
		appendToFile(file1a,"AppendIt The first");
		appendToFile(file2a,"AppendIt The second");
		
		appendToFile(file1b,"AppendIt The first");
		appendToFile(file2b,"AppendIt The second");
		
		// Send the two projects to the server
		execute("ci",
							globalOptions,
							new String[]{"-m","msg" + PLATFORM_NEWLINE + "second Line"},
							new String[]{"coProject1","coProject2"},
							folder1,
							monitor,
							System.err);
		
		execute("update",
							globalOptions,
							EMPTY_ARGS,
							new String[]{"coProject1","coProject2"},
							folder2,
							monitor,
							System.err);
		
		// Look if all the changes have been done allright
		assertEqualsArrays(readFromFile(file1a),readFromFile(file3a));
		assertEqualsArrays(readFromFile(file2a),readFromFile(file4a));
		
		assertEqualsArrays(readFromFile(file1b),readFromFile(file3b));
		assertEqualsArrays(readFromFile(file2b),readFromFile(file4b));
		
		
		// Now check if the programm works with single files
		appendToFile(file1a,"AppendIt The EXTRA");
		appendToFile(file1b,"AppendIt The EXTRA");
		execute("ci",
							globalOptions,
							new String[]{"-m","msg"},
							new String[]{"coProject1/a.txt"},
							folder1,
							monitor,
							System.err);
		
		execute("update",
							globalOptions,
							EMPTY_ARGS,
							new String[]{"coProject1/a.txt"},
							folder2,
							monitor,
							System.err);
		
		// It should have the 1a file updated but not the 1b file
		// because we did not upload and commit it
		assertEqualsArrays(readFromFile(file1a),readFromFile(file3a));
		try {
			assertEqualsArrays(readFromFile(file1b),readFromFile(file3b));
			throw new IllegalArgumentException("Fail-Statment");
		} catch (AssertionFailedError e) {}
	}
	
	/**
	 * This TestCase should maybe become simpler.
	 * 
	 * Anyway, it is about Up and downloading changes
	 * without conflicts
	 */
	public void subTestCommitUpdate() throws Exception {
		
		File file1;
		File file2;
		String[] fileContent1;
		// String[] fileContent2;
		boolean fail = false;
		GregorianCalendar calender;
		
		calender = new GregorianCalendar();
		
		fileContent1 = new String[]{"This is a ", "two-liner", calender.get(Calendar.MILLISECOND) + ""};
		// fileContent2 = new String[]{"This is a one-liner", calender.get(Calendar.MILLISECOND) + 10 + ""};
		
		file1 = getFile("test1/proj1/folder1/c.txt");
		file2 = getFile("test2/proj1/folder1/c.txt");
				
		// change something and commit the canges
		// in client1
		// writeToFile("test1/...", contents)
		writeToFile(file1,fileContent1);
		
		execute("ci",globalOptions, 
							new String[]{"-m","TestMessage"}, 
							arguments, 
							ioFolder1, 
							monitor,
							System.err);
		
		// test that files in client1 and client2 are unequal before
		try {
			assertEqualsArrays(readFromFile(file1),readFromFile(file2));
		} catch (Throwable e) {
			fail = true;
		}
		
		assertTrue("subTestCommitUpdate.0a (This could happen by coincedence, try again) ",fail);
		fail = false;

		execute("update",globalOptions, 
							EMPTY_ARGS, 
							arguments, 
							ioFolder2, 
							monitor,
							System.err);
			
		assertEqualsArrays(fileContent1,readFromFile(file2));
				
	}

	public void testDifferentFolders() throws Exception {
		
		File file1;
		File file2;
		String[] fileContent1;
		String[] fileContent2;
		boolean fail = false;
		GregorianCalendar calender;
		
		calender = new GregorianCalendar();
		
		// Init the field
		subTestCeckout();

		file1 = getFile("test1/proj1/folder1/c.txt");		
		file2 = getFile("test2/proj1/folder1/c.txt");
		fileContent1 = new String[]{"This is a ", "two-liner", calender.get(Calendar.MILLISECOND) + ""};
		fileContent2 = new String[]{"This is a one-liner", calender.get(Calendar.MILLISECOND) + 10 + ""};
		
		// First run
		writeToFile(file1,fileContent1);
		

		execute("ci",globalOptions, 
							new String[]{"-m","TestMessage"}, 
							arguments, 
							ioFolder1, 
							monitor,
							System.err);

		try {
			assertEqualsArrays(readFromFile(file1),readFromFile(file2));
		} catch (Throwable e) {
			fail = true;
		}
		
		assertTrue("testDifferentFolders.1 (This could happen by coincedence, try again) ",fail);
		fail = false;
		
		// Should be able to set globalOptions to new String[0]
		execute("update",new String[0], 
							EMPTY_ARGS, 
							new String[0], 
							ioFolder4, 
							monitor,
							System.err);
		
		assertEqualsArrays(fileContent1,readFromFile(file2));

		// Seconde run
		// 
		// wait a second to get a new timestamp and
		// check that the resource has acctually a new timestamp
		waitMsec(4000);
		writeToFile(file2,fileContent2);
		assertEquals(true, Session.getManagedFolder(ioFolder4).getFile("c.txt").isModified());
		
		// Should be able to set globalOptions to new String[0]
		execute("ci",new String[0], 
							new String[]{"-m","TestMessage"}, 
							new String[0], 
							ioFolder4, 
							monitor,
							System.err);

		try {
			assertEqualsArrays(readFromFile(file1),readFromFile(file2));
		} catch (Throwable e) {
			fail = true;
		}
		
		assertTrue("testDifferentFolders.3 (This could happen by coincedence, try again) ",fail);
		fail = false;

		execute("update",globalOptions, 
							EMPTY_ARGS, 
							arguments, 
							ioFolder1, 
							monitor,
							System.err);
		
		assertEqualsArrays(fileContent2,readFromFile(file1));

	}
	
	public void testCommit() throws Exception {
		
		File ioFolder = ioFolder1;

		execute("co",globalOptions, 
							EMPTY_ARGS, 
							arguments, 
							ioFolder, 
							monitor,
							System.err);
							
		GregorianCalendar calender;
		
		
		calender = new GregorianCalendar();
				
		writeToFile(getFile("test1\\proj1\\folder1\\c.txt"), new String[]{"This is my","Test",calender.get(Calendar.MILLISECOND) +""});
		
		execute("ci",globalOptions, 
							new String[]{"-m","TestMessage"}, 
							arguments, 
							ioFolder, 
							monitor,
							System.err);
	}
	
	public void testConnection() throws CVSException {

		File ioFolder = ioFolder1;
		
		execute("co",globalOptions, 
							EMPTY_ARGS, 
							new String[]{"proj1"}, 
							ioFolder, 
							monitor,
							System.err);
		try {
			execute("co",globalOptions, 
							EMPTY_ARGS, 
							new String[]{"proj1XXX"}, 
							ioFolder, 
							monitor,
							System.err);
			fail();
		} catch (CVSException e) {}
	}
	
	public void testDoubleCheckout() throws Exception {
		
		magicSetUpRepo("coProject1",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt"});
		magicSetUpRepo("coProject2",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt"});
		execute("co",
							globalOptions,
							EMPTY_ARGS,
							new String[]{"coProject1","coProject2"},
							workspaceRoot,
							monitor,
							System.err);
		
		assertTrue(getFile("coProject1/a.txt").exists());
		assertTrue(getFile("coProject1/f1/b.txt").exists());
		assertTrue(getFile("coProject2/a.txt").exists());
		assertTrue(getFile("coProject2/f1/b.txt").exists());
		
		FileUtil.deepDelete(getFile("coProject1"));
		FileUtil.deepDelete(getFile("coProject2"));
				
	}
	
	public void testImport() throws Exception {
		File ioFolder = ioFolder1;
		ICVSFolder mFolder = Session.getManagedFolder(ioFolder);
		
		String[] fileStructure = new String[]{"im/a.txt","im/f1/a.txt","im/f1/b.txt"};
		createRandomFile(ioFolder,fileStructure);
		
		magicDeleteProject("im");
		
		execute("import",globalOptions,
					   new String[]{"-m","Initial Release"},
					   new String[]{"im","r1","r2"},
					   getFile(mFolder.getFolder("im")),
					   new NullProgressMonitor(),
					   System.err);
					   
		execute("co",globalOptions,
					   EMPTY_ARGS,
					   new String[]{"im"},
					   getFile(mFolder),
					   new NullProgressMonitor(),
					   System.err);
	}

	public void testUpdate() throws CVSException {

		File ioFolder = ioFolder1;
		
		execute("co",globalOptions, 
							EMPTY_ARGS, 
							arguments, 
							ioFolder, 
							monitor,
							System.err);
							
		try {
			execute("update",globalOptions, 
							EMPTY_ARGS, 
							new String[0], 
							ioFolder, 
							monitor,
							System.err);
			fail();
		} catch (CVSException e) {}

		execute("update",globalOptions, 
							EMPTY_ARGS, 
							arguments, 
							ioFolder, 
							monitor,
							System.err);
	}
}

