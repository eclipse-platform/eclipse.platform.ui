package org.eclipse.team.tests.ccvs.core.compatible;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.EntryFileDateFormat;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;
import org.eclipse.team.tests.ccvs.core.NullOutputStream;


/**
 * This is a TestCase that does provide the possibility
 * to run tests on both the reference reference-client and the
 * client provided by us, and to check on equal results
 * in files and messages to the consol.
 * 
 * No own tests should be placed here, instead you should
 * instanciate this testcase in order to make your test from
 * another suite.
 * The class is final, because you need to be able to open
 * two (or more) different enviorments to test certain things.
 */
public final class SameResultEnv extends JUnitTestCase {
	
	public static final String REFERENCE_CLIENT_WORKSPACE="reference";
	public static final String ECLIPSE_CLIENT_WORKSPACE="eclipse";
	
	private File workspace;
	private File referenceClientRoot;
	private File eclipseClientRoot;
	private boolean ignoreExceptions=false;
	
	private CVSRepositoryLocation referenceClientRepository;
	private CVSRepositoryLocation eclipseClientRepository;
		
	public SameResultEnv(String arg, File workspace) {
		super(arg);
		this.workspace = workspace;
		referenceClientRoot = new File(workspace, REFERENCE_CLIENT_WORKSPACE);
		eclipseClientRoot = new File(workspace, ECLIPSE_CLIENT_WORKSPACE);
		
		try {
			deleteFile(".");
		} catch (CVSException e) {
			fail();
		}
	}
	
	/**
	 * Always to be called in the setUp of the testCase that wants to 
	 * use the same-result Enviorment.
	 */
	public void setUp() throws CVSException {
		// By default, exceptions are not ignored.
		// Specific test cases can choose to ignore exceptions
		ignoreExceptions = false;
		mkdirs(".");
	}
	
	/**
	 * Always to be called in the tearDown of the testCase that wants to 
	 * use the same-result Enviorment.
	 */
	public void tearDown() throws CVSException {
		deleteFile("");
	}
	
	/**
	 * Deletes files on the both of the cvs-servers.
	 */
	public void magicDeleteRemote(String project) throws CVSException {

		referenceClientRepository = CompatibleTestSetup.referenceClientRepository;
		eclipseClientRepository = CompatibleTestSetup.eclipseClientRepository;
		
		String host1 = referenceClientRepository.getHost();
		String repoRoot1 = referenceClientRepository.getRootDirectory();

		String host2 = eclipseClientRepository.getHost();
		String repoRoot2 = eclipseClientRepository.getRootDirectory();
		
		magicDeleteProject(host1,repoRoot1,project);
		magicDeleteProject(host2,repoRoot2,project);		
	}
	
	/**
	 * Set up both of the repos on the cvs-server(s) with the standard
	 * file-structure:
	 * project
	 *   a.txt
	 *   f1
	 *     b.txt
	 *     c.txt
	 */
	public void magicSetUpRepo(String project) throws CVSException {
		magicSetUpRepo(project,new String[]{"a.txt","f1/b.txt","f1/c.txt"});
	}
	
	/**
	 * Set up both of the repos on the cvs-server(s) with a filestructre
	 * resulting for your input in the parameter createResources.
	 */
	public void magicSetUpRepo(String project,String[] createResources) throws CVSException {
		
		// This will trigger asynchronizer reload
		// deleteFile(project);
		magicDeleteRemote(project);
		
		createRandomFile(createResources, project);
		execute("import",new String[]{"-m","msg"},new String[]{project,"a","b"},project);
		
		deleteFile(".");
		mkdirs(".");
	}
	
	/**
	 * Give null this gives an empty string-array back, otherwise
	 * the parameter.
	 */
	private static String[] notNull(String[] arg) {
		if (arg == null) {
			return new String[0];
		} else {
			return arg;
		}
	}

	/**
	 * Convienience Method, does the same like:<br>
	 * execute(request,null,localOptions,arguments,rootExtention) 
	 */				
	public void execute(String request, 
						String[] localOptions, 
						String[] arguments,
						String rootExtention) 
						throws CVSException {
		
		execute(request,new String[0],localOptions,arguments,rootExtention);
	}

	/**
	 * Convienience Method, does the same like:<br>
	 * execute(request,null,localOptions,arguments,null) 
	 */		
	public void execute(String request, 
						String[] localOptions, 
						String[] arguments) 
						throws CVSException {

		execute(request,new String[0],localOptions,arguments,"");
	}
	
	/**
	 * Run a command in the two folders of this enviorment. In one folder the
	 * reference-client runs in the the other the eclipse-client. After that
	 * the results on disc are compared (the output of the clients is not
	 * considert for the comparison)
	 */
	public void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						String rootExtention) 
						throws CVSException {
		
		globalOptions = notNull(globalOptions);
		
		String[] gOptions1 = new String[globalOptions.length + 2];
		String[] gOptions2 = new String[globalOptions.length + 2];
		
		System.arraycopy(globalOptions,0,gOptions1,0,globalOptions.length);
		System.arraycopy(globalOptions,0,gOptions2,0,globalOptions.length);
		
		gOptions1[globalOptions.length] = gOptions2[globalOptions.length] = "-d";
		gOptions1[globalOptions.length + 1] = CompatibleTestSetup.REFERENCE_CLIENT_REPOSITORY;
		gOptions2[globalOptions.length + 1] = CompatibleTestSetup.ECLIPSE_CLIENT_REPOSITORY;

		execute(request,gOptions1,gOptions2,localOptions,arguments,rootExtention);
	}
	
	/**
	 * Acctally run the command in both folders. See doc above.
	 */
	private void execute(String request, 
						String[] globalOptions1, 
						String[] globalOptions2, 
						String[] localOptions, 
						String[] arguments,
						String rootExtention) 
						throws CVSException {
		
		assertNotNull(request);
		assertNotNull(globalOptions1);
		assertNotNull(globalOptions);
		
		localOptions = notNull(localOptions);
		arguments = notNull(arguments);
		if (rootExtention == null || rootExtention.equals(".")) {
			rootExtention = "";
		}
		
		try {
			ReferenceClient.execute(request, 
									globalOptions1, 
									localOptions, 
									arguments,
									new File(referenceClientRoot,rootExtention),
									new NullProgressMonitor(), 
									new PrintStream(new NullOutputStream()));
		} catch (ReferenceException e) {
			if (!ignoreExceptions) {
				throw e;
			}
		}
		
		try {
			execute(request, 
					globalOptions2, 
					localOptions, 
					arguments,
					new File(eclipseClientRoot,rootExtention),
					new NullProgressMonitor(), 
					new PrintStream(new NullOutputStream()));
		} catch (CVSServerException e) {
			if (!ignoreExceptions) {
				throw e;
			}
		}
		assertConsistent();
	}

	/**
	 * Checks whether the two directories inside the environment
	 * are equal and therefore the state valid.
	 */
	public void assertConsistent() throws CVSException {
		ICVSFolder referenceFolder = Session.getManagedFolder(referenceClientRoot);
		ICVSFolder eclipseFolder = Session.getManagedFolder(eclipseClientRoot);
		CVSProviderPlugin.getSynchronizer().reload(((LocalResource)referenceFolder).getLocalFile(), new NullProgressMonitor());
		CVSProviderPlugin.getSynchronizer().reload(((LocalResource)eclipseFolder).getLocalFile(), new NullProgressMonitor());
		assertEquals(referenceFolder,eclipseFolder);
	}
	
	/**
	 * Create a file with random-content in both, the reference client and 
	 * the eclipse-client.
	 * 
	 * @param relativeFileName is the relative path as allways in the 
	           class used for access
	 */
	public void createRandomFile(String relativeFileName) throws CVSException {
		
		String randomContent;
		
		randomContent = createRandomContent();
		try {
			writeToFile(relativeFileName,new String[]{randomContent});
		} catch (IOException e) {
			throw new CVSException("IOException while creating random content",e);
		}
	}

	/**
	 * Call createRandomFile for every element of the array
	 * 
	 * @see SameResultEnv#createRandomFile(String)
	 */
	public void createRandomFile(String[] relativeFileNames, String rootExtention) throws CVSException {
		
		if (rootExtention == null || rootExtention.equals(".")) {
			rootExtention = "";
		}
		
		if (!rootExtention.equals("") && !rootExtention.startsWith("/")) {
			rootExtention = rootExtention + "/";
		}
		
		for (int i=0; i<relativeFileNames.length; i++) {
			createRandomFile(rootExtention + relativeFileNames[i]);
		}
	}
	
	/**
	 * Read from the file (check that we have acctually got the same
	 * content in both versions
	 */
	public String[] readFromFile(String relativeFileName) throws IOException {
		
		String[] content1;
		String[] content2;
		
		content1 = super.readFromFile(new File(referenceClientRoot,relativeFileName));
		content2 = super.readFromFile(new File(eclipseClientRoot,relativeFileName));
		
		assertEqualsArrays(content1,content2);
		
		return content1;
	}
	
	/**
	 * Delete files from both of the directories
	 */
	public void deleteFile(String relativeFileName) throws CVSException {
		
		if (".".equals(relativeFileName)) {
			relativeFileName = "";
		}
		
		File file1 = new File(referenceClientRoot, relativeFileName);
		File file2 = new File(eclipseClientRoot, relativeFileName);
		
		assertEquals(file1.exists(),file2.exists());
		
		if (!file1.exists()) {
			return;
		}
		
		// Call the "clean-up-delete" that cares about deleting the
		// cache
		if (file1.isDirectory()) {
			delete(Session.getManagedFolder(file1));
			delete(Session.getManagedFolder(file2));
		} else {
			delete(Session.getManagedFile(file1));
			delete(Session.getManagedFile(file2));
		}
	}
	
	/**
	 * Create a folder and all the subfolders 
	 * in both of the directories
	 */
	public void mkdirs(String folderName) {	
		(new File(referenceClientRoot,folderName)).mkdirs();
		(new File(eclipseClientRoot,folderName)).mkdirs();
	}
	
	/**
	 * Append a String to an file (acctally to both of the files, that are going
	 * to have the same content)
	 */
	public void appendToFile(String relativeFileName, String txt) throws IOException {	
		File file1 = new File(referenceClientRoot,relativeFileName);
		File file2 = new File(eclipseClientRoot,relativeFileName);

		// Wait a second so that the timestamp will change for sure
		//waitMsec(1100);
		
		appendToFile(file1,txt);
		appendToFile(file2,txt);
	}
	
	/**
	 * Write to the file (acctally to both of the files, that are going
	 * to have the same content)
	 * Does create the underlying folder if they do not exist (the version
	 * of JUnitTest does currently not)
	 */
	public void writeToFile(String relativeFileName, String[] content) throws IOException {	
		
		File file1 = new File(referenceClientRoot,relativeFileName);
		File file2 = new File(eclipseClientRoot,relativeFileName);
		
		file1.getParentFile().mkdirs();
		file2.getParentFile().mkdirs();

		writeToFile(file1,content);
		writeToFile(file2,content);
	}		

	/**
	 * Deep compare of two ManagedResources (most likly folders).
	 * Passwords are ignored.
	 * 
	 * @param ignoreTimestamp if true timestamps of
	           files are ignored for the comparison
	 */
	public static void assertEquals(ICVSResource mResource1, 
										ICVSResource mResource2) 
										throws CVSException {
		
		assertEquals(mResource1.isFolder(), mResource2.isFolder());
		assertEquals(mResource1.isManaged() , mResource2.isManaged());
		assertEquals(mResource1.exists(), mResource2.exists());
		
		if (!mResource1.exists()) {
			return;
		}
		
		if (mResource1.isFolder()) {
			assertEquals((ICVSFolder)mResource1,(ICVSFolder)mResource2);
		} else {
			assertEquals((ICVSFile)mResource1,(ICVSFile)mResource2);
		}	
	}	
	
	/**
	 * Assert that two CVSFile's are equal by comparing the content
	 * and the metainformation out of the ResourceSync.
	 */
	private static void assertEquals(ICVSFile mFile1, ICVSFile mFile2) throws CVSException {
		
		// Check the permissions on disk
		assertEquals(getFile(mFile1).canWrite(), getFile(mFile2).canWrite());
					
		// Compare the content of the files
		try {
			InputStream in1 = new FileInputStream(getFile(mFile1)); 
			InputStream in2 = new FileInputStream(getFile(mFile2)); 
			byte[] buffer1 = new byte[(int)mFile1.getSize()];
			byte[] buffer2 = new byte[(int)mFile2.getSize()];
			// This is not the right way to do it, because the Stream
			// may read less than the whoole file
			in1.read(buffer1);
			in2.read(buffer2);
			in1.close();
			in2.close();
			assertEquals(buffer1.length,buffer2.length);
			assertEquals(new String(buffer1),new String(buffer2));
		} catch (IOException e) {
			throw new CVSException("Error in TestCase");
		}

		// We can not do the ceck, because the reference client does
		// check out dirty files ?!?
		// assertEquals(mFile1.isDirty(),mFile2.isDirty());
		
		assertEquals(mFile1.getSyncInfo() == null,mFile2.getSyncInfo() == null);
		if (mFile1.getSyncInfo() == null) {
			return;
		}
		
		ResourceSyncInfo info1 = mFile1.getSyncInfo();
		ResourceSyncInfo info2 = mFile2.getSyncInfo();
		
		assertEquals(info1.getKeywordMode(), info2.getKeywordMode());
		assertEquals(info1.getTag(), info2.getTag());
		assertEquals(info1.getName(), info2.getName());
		assertEquals(info1.getRevision(), info2.getRevision());
		
		// Ensure that timestamps are written in ISO C asctime() format and if timestamp
		// has a conflict marker then both should have the marker. Also ensure that timestamps
		// are written using same timezone.
		assertTimestampEquals(info1.getTimeStamp(), info2.getTimeStamp());
		
		// We are not able to check for the permissions, as the reference-client doesn't save them
	}

	private static void assertTimestampEquals(String timestamp1, String timestamp2) {
		try {			
			EntryFileDateFormat timestampFormat = new EntryFileDateFormat();
			boolean merge1 = timestamp1.indexOf(ResourceSyncInfo.RESULT_OF_MERGE) != -1;
			boolean merge2 = timestamp2.indexOf(ResourceSyncInfo.RESULT_OF_MERGE) != -1;
			boolean dummy1 = timestamp1.indexOf(ResourceSyncInfo.DUMMY_TIMESTAMP) != -1;
			boolean dummy2 = timestamp2.indexOf(ResourceSyncInfo.DUMMY_TIMESTAMP) != -1;
			assertEquals("both timestamps should show same conflict state", merge1, merge2);
			assertEquals("both timestamps should show same dummy state", dummy1, dummy2);
			if(!merge1 && !dummy1) {
				long time1 = timestampFormat.toMilliseconds(timestamp1);
				long time2 = timestampFormat.toMilliseconds(timestamp2);
				/* timestamp tests don't seem to work on some systems.
				long difference = Math.abs(time1 - time2);
				assertTrue("timestamps should be in same timezone:" + timestamp1 + ":" + timestamp2, difference < (5*60*1000)); // 5 minutes
				*/
			}
		} catch(ParseException e) {			
			fail("timestamps in CVS/Entry file are not in ISO C asctime format:" + timestamp1 + ":" + timestamp2);
		}
	}
	
	/**
	 * Assert that two CVSFile's are equal. First the 
	 * metainformation out of the FolderSync for this 
	 * folder is compared, then the amount of children is 
	 * checked and finally the recussion is started to
	 * compare the children of this folder.
	 */
	private static void assertEquals(ICVSFolder mFolder1, 
										ICVSFolder mFolder2) 
										throws CVSException {

		assertEquals(mFolder1.isCVSFolder(),mFolder2.isCVSFolder());
		
		if (mFolder1.isCVSFolder()) {
			String root1 = Util.removePassword(mFolder1.getFolderSyncInfo().getRoot());
			String root2 = Util.removePassword(mFolder2.getFolderSyncInfo().getRoot());
			root1 = root1.substring(0,root1.lastIndexOf("@"));
			root2 = root2.substring(0,root2.lastIndexOf("@"));
			assertEquals(root1,root2);
			
			assertEquals(mFolder1.getFolderSyncInfo().getRepository(),mFolder2.getFolderSyncInfo().getRepository());
			assertEquals(mFolder1.getFolderSyncInfo().getIsStatic(),mFolder2.getFolderSyncInfo().getIsStatic());
			assertEquals(mFolder1.getFolderSyncInfo().getTag(),mFolder2.getFolderSyncInfo().getTag());
		}
		
		ICVSResource[] resourceList1;
		ICVSResource[] resourceList2;
		boolean fileFound;
		
		resourceList1 = mFolder1.getFiles();
		resourceList2 = mFolder2.getFiles();
		assertEquals(resourceList1.length,resourceList2.length);
		for (int i=0; i<resourceList1.length; i++) {
			fileFound = false;
			for (int j=0; j<resourceList2.length; j++) {
				if (resourceList1[i].getName().equals(resourceList2[j].getName())) {
					assertEquals(resourceList1[i], resourceList2[j]);
					fileFound = true;
					break;
				}
			}
			assertTrue("File " + resourceList1[i].getName() + " not found in the list",fileFound);
		}
		
		resourceList1 = mFolder1.getFolders();
		resourceList2 = mFolder2.getFolders();
		assertEquals(resourceList1.length,resourceList2.length);
		for (int i=0; i<resourceList1.length; i++) {
			fileFound = false;
			for (int j=0; j<resourceList2.length; j++) {
				if (resourceList1[i].getName().equals(resourceList2[j].getName())) {
					assertEquals(resourceList1[i], resourceList2[j]);
					fileFound = true;
					break;
				}
			}
			assertTrue("Folder " + resourceList1[i].getName() + " not found in the list",fileFound);
		}
	}
	
	/**
	 * Sets whether Exceptions that are thrown in the execution of both of the clients are
	 * catched or thrown to the upper level. If the exceptions are catched the result of the 
	 * reference-client and this client are compared as if the execution succseded.
	 */
	public void setIgnoreExceptions(boolean ignoreExceptions) {
		this.ignoreExceptions = ignoreExceptions;
	}
}