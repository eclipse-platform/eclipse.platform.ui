package org.eclipse.team.tests.ccvs.core;
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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.awtui.TestRunner;
import junit.framework.TestCase;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalResource;
import org.eclipse.team.internal.ccvs.core.resources.Synchronizer;
import org.eclipse.team.internal.ccvs.core.util.FileUtil;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Base-class to the low level-testcases for the Session.
 * Supplies convinience-methods and default attributes for the testcases.
 * Especally data for a default-connection to the server is stored.
 */
public abstract class JUnitTestCase extends TestCase {
	
	protected static final int RANDOM_CONTENT_SIZE = 10000;
	protected static final boolean NEWLINE_TEST = false;
	protected static final String PLATFORM_NEWLINE = System.getProperty("line.separator");
	protected static final File workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
	
	public static final String[] EMPTY_ARGS = new String[0];
	
	// Predefined parameters for calles of the client
	protected final String[] globalOptions;
	protected final IProgressMonitor monitor;
	protected final String[] arguments;
	protected static final String REPOSITORY_NAME = CVSTestSetup.REPOSITORY_LOCATION;

	static boolean propertiesSet = false;

	private static final HashMap commandPool = new HashMap();
	static {
		commandPool.put("update", Command.UPDATE);
		commandPool.put("co", Command.CHECKOUT);
		commandPool.put("ci", Command.COMMIT);
		commandPool.put("import", Command.IMPORT);
		commandPool.put("add", Command.ADD);
		commandPool.put("remove", Command.REMOVE);
		commandPool.put("status", Command.STATUS);
		commandPool.put("log", Command.LOG);
		commandPool.put("tag", Command.TAG);
		commandPool.put("admin", Command.ADMIN);
		commandPool.put("diff", Command.DIFF);
	}
	
	/**
	 * Convinience method for:<br>
	 * Session.execute(request,globalOptions,localOptions,arguments,Session.getManagedFolder(root),monitor,messageOut)
	 */	
	public static void execute(String request, 
						String[] globalOptions, 
						String[] localOptions, 
						String[] arguments,
						File root,
						IProgressMonitor monitor, 
						PrintStream messageOut) 
						throws CVSException {
		if (!CVSTestSetup.DEBUG)
			messageOut = new PrintStream(new NullOutputStream());
		
		List globals = new ArrayList();
		for (int i=0;i<globalOptions.length;i++) {
			if (globalOptions[i].equals("-d")) {
				i++;
				continue;
			}
			globals.add(new CustomGlobalOption(globalOptions[i]));
		}
		List locals = new ArrayList();
		for (int i=0;i<localOptions.length;i++) {
			if ((i < localOptions.length - 1) && (localOptions[i + 1].charAt(0) != '-')) {
				locals.add(new CustomLocalOption(localOptions[i], localOptions[i + 1]));
				i++;
			} else {
				locals.add(new CustomLocalOption(localOptions[i], null));
			}
		}
		Session s = new Session(getRepository(globalOptions, Session.getManagedFolder(root)), Session.getManagedFolder(root));
		s.open(monitor);
		try {
			IStatus status = ((Command)commandPool.get(request)).execute(s,
				(GlobalOption[]) globals.toArray(new GlobalOption[globals.size()]),
				(LocalOption[]) locals.toArray(new LocalOption[locals.size()]),
				arguments,
				null,
				monitor);
			if (status.getCode() == CVSException.SERVER_ERROR) {
				throw new CVSServerException(status);
			}
		} finally {
			s.close();
		}
	}
	
	public static class CustomGlobalOption extends GlobalOption {
		public CustomGlobalOption(String option) {
			super(option);
		}
	}
	public static class CustomLocalOption extends LocalOption {
		public CustomLocalOption(String option, String arg) {
			super(option, arg);
		}
	}
	/**
	 * This give you a new repo either from the global "-d" option
	 * or form the root-property in the folder.
	 * 
	 * This has to be rewritten in a nicer style.
	 */
	private static CVSRepositoryLocation getRepository(String[] globalOptions, 
										ICVSFolder mFolder) 
										throws CVSException {
		
		String repoName = null;
		
		// look if the repo is specified in the global Options
		// this delets the option as well which is not so beatyful, but
		// we have got a copy and we do not want this option to appear
		// any more
		repoName = Util.getOption(globalOptions, "-d", true);
		
		// look if we have got an root-entrie in the root-folder
		if (repoName == null && mFolder.exists() && mFolder.isCVSFolder()) {
			repoName = mFolder.getFolderSyncInfo().getRoot();
		}
		
		if (repoName == null) {
			throw new CVSException("CVSROOT is not specified");
		}
		
		return CVSRepositoryLocation.fromString(repoName);
	}
	
	/**
	 * Get a File relative to the working directory.
	 */
	protected static File getFile(String relativePath) {
		// We need to get the cononical file in case relativePath contains a dot indicating the root directory
		try {
			return new File(workspaceRoot, relativePath).getCanonicalFile();
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}
	
	/**
	 * Get the IO File for the given CVS resource
	 */
	protected static File getFile(ICVSResource mResource) {
		return new File(((LocalResource)mResource).getPath());
	}
	
	/**
	 * Get a CVSFolder relative to the working directory.
	 */
	protected static ICVSFolder getManagedFolder(String relativePath) {
		try {
			return Session.getManagedFolder(getFile(relativePath));
		} catch (CVSException e) {
			fail(e.getMessage());
			return null;
		}
	}
	
	/**
	 * Init the options and arguments to standard-values
	 */
	public JUnitTestCase(String name) {
		super(name);
		
		monitor = new NullProgressMonitor();
		globalOptions = new String[]{"-d",REPOSITORY_NAME};
		arguments = new String[]{"proj1"};
	}

	/**
	 * Delete a project/resource form the standard cvs-server
	 */
	protected void magicDeleteProject(String project) throws CVSException {
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(REPOSITORY_NAME);
		String host = location.getHost();
		String repoRoot = location.getRootDirectory();
		magicDeleteProject(host, repoRoot, project);
	}

	/**
	 * Delete a project/resource form the standard cvs-server
	 */
	protected static void magicDeleteProject(String host, String repoRoot, String project) throws CVSException {
		
		String commandLine;
		Process process;
		
		commandLine = new String("rsh " + host + " rm -rf " + repoRoot + "/" + project);

		try {
			process = Runtime.getRuntime().exec(commandLine);
			process.waitFor();
			
			if (process.exitValue() != 0) {
				throw new CVSException("Return Code of magicDeleteProject :" + process.exitValue());
			}
			
		} catch (IOException e) {
			throw new CVSException("IOException in magicDeleteProject");
		} catch (InterruptedException e) {
			throw new CVSException("InterruptedException in magicDeleteProject");
		}		
	}

	/**
	 * Set the project on the standard cvs-server up so that it contains the resources
	 * in createResources. The files have random content.
	 */
	public void magicSetUpRepo(String project, String[] createResources) throws CVSException {
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(REPOSITORY_NAME);
		String host = location.getHost();
		String repoRoot = location.getRootDirectory();
		magicSetUpRepo(workspaceRoot,host,repoRoot,REPOSITORY_NAME,project,createResources);
	}
	
	/**
	 * Set the project on the standard cvs-server up so that it contains the resources
	 * in createResources. The files have random content.
	 * 
	 * @param root a folder to place files temporaryly
	 * @param host e.g. dev.eclipse.org:2401
	 * @param repoRoot e.g. /home/cvs
	 * @param repoName e.g. :pserver:anonymous@dev.eclipse.org:2401:/home/eclipse
	 * @param project e.g. org.eclipse.swt
	 * @param createResources e.g. new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt"}
	 */
	private static void magicSetUpRepo(File root, String host, String repoRoot, String repoName, String project, String[] createResources) throws CVSException {
		
		File workFolder;
		
		workFolder = new File(root,project + "tmpXXXtmp");
		
		createRandomFile(workFolder, createResources);
		
		magicDeleteProject(host,repoRoot,project);
		
		String[] gOptions = new String[]{"-d",repoName};
		String[] lOptions = new String[]{"-m","msg"};
		String[] args = new String[]{project,"a","b"};
		
		execute("import",gOptions,lOptions,args,workFolder,new NullProgressMonitor(),System.err);
		
		FileUtil.deepDelete(workFolder);
	}
	
	/**
	 *  Compare Arrays and find the first different element
	 */
	protected static void assertEqualsArrays(Object[] obArr1, Object[] obArr2) {
		
		assertEquals("Called assertEqualsArrays with null on one side", obArr1 == null,obArr2 == null);
		if (obArr1 == null) {
			return;
		}

		for (int i=0; i<Math.min(obArr1.length,obArr2.length); i++) {
			assertEquals("At Element " + i + " of the array",obArr1[i],obArr2[i]);
		}
		
		// If the Arrays are different in length, look for the first
		// not existing element and compare it to the existing in the
		// other array
		if (obArr1.length > obArr2.length) {
			assertEquals("Arrays of different length",obArr1[obArr2.length],null);
			return;
		}
	
		if (obArr1.length < obArr2.length) {
			assertEquals("Arrays of different length",obArr2[obArr1.length],null);
			return;
		}
			
	}
	
	protected static void assertSynchronizerEmtpy() {
		if (!Synchronizer.getInstance().isEmpty() && CVSTestSetup.DEBUG)
			Synchronizer.getInstance().dump();
		assertTrue(Synchronizer.getInstance().isEmpty());
	}
	
	/**
	 * Write String[] to file as lines
	 */
	protected static void writeToFile(File file, String[] content)
		throws IOException {
		
		BufferedWriter fileWriter;
		
		fileWriter = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i<content.length; i++) {
			fileWriter.write(content[i]);
			fileWriter.newLine();
		}
		fileWriter.close();
	}
	
	/**
	 * load file in lines to String[]
	 */
	protected static String[] readFromFile(File file)
		throws IOException {

		BufferedReader fileReader;
		List fileContentStore = new ArrayList();
		String line;
		
		if (!file.exists()) {
			return null;
		}
		
		fileReader = new BufferedReader(new FileReader(file));
		while ((line = fileReader.readLine()) != null) {
			fileContentStore.add(line);
		}
		fileReader.close();
			
		return (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
	}

	/**
	 * Append a String to an file (acctally to both of the files, that are going
	 * to have the same content)
	 * If the file is empty we create a new file with the content txt.
	 */
	protected void appendToFile(File file, String txt) throws IOException {	
		String[] content;
		String[] newContent;
		
		content = readFromFile(file);
		
		if (content == null) {
			content = new String[0];
		}
		
		newContent = new String[content.length + 1];
		System.arraycopy(content,0,newContent,0,content.length);
		newContent[content.length] = txt;
		
		writeToFile(file,newContent);
	}
	
	/**
	 * genertates Random content meand to be written in a File
	 */
	protected static String createRandomContent() {
		
		StringBuffer content = new StringBuffer();
		int contentSize;
		
		content.append("Random file generated for test" + PLATFORM_NEWLINE);
		
		contentSize = (int) Math.round(RANDOM_CONTENT_SIZE * 2 * Math.random());
		for (int i=0; i<contentSize; i++) {
			
			if (Math.random()>0.99) {
				content.append(PLATFORM_NEWLINE);
			}

			if (Math.random()>0.99) {
				content.append("\n");
			}
			
			if (NEWLINE_TEST) {
				if (Math.random()>0.99) {
					content.append("\n\r");
				}
				if (Math.random()>0.99) {
					content.append('\r');
				}
				if (Math.random()>0.99) {
					content.append("\r\n");
				}
				if (Math.random()>0.99) {
					content.append("\n");
				}
				if (Math.random()>0.99) {
					content.append("\n\n");
				}
				if (Math.random()>0.99) {
					content.append("\r\r");
				}
			}
			
			content.append((char)('\u0021' + Math.round(60 * Math.random())));
		}
		
		return content.toString();
	}
	
	/**
	 * Creates the file with random contend, and all the folders on the
	 * way to there
	 */
	private static void createRandomFile(File file) throws CVSException {
		try {
			file.getParentFile().mkdirs();
			writeToFile(file,new String[]{createRandomContent()});
		} catch (IOException e) {
			throw new CVSException(0,0,"IOException in test-setup",e);
		}		
	}
		
	/**
	 * Build the given fileStructure, all files are going to have
	 * sample content, all folders on the way are created
	 */
	protected static void createRandomFile(File root, String[] fileNameArray) 
														throws CVSException {
		for (int i=0; i<fileNameArray.length; i++) {
			createRandomFile(new File(root, fileNameArray[i]));
		}
	}

	/**
	 * wait milliseconds to continou the execution
	 */
	protected static void waitMsec(int msec) {	
		try {
			Thread.currentThread().sleep(msec);
		} catch(InterruptedException e) {
			fail("wait-problem");
		}
	}
	
	/**
	 * Call this method from the main-method of your test-case.
	 * It initialises some required parameter and runs the testcase.
	 */
	protected static void run(Class test) {
		System.setProperty("eclipse.cvs.standalone","true");
		TestRunner.run(test);
	}

	/**
	 * This delte does a deepDelete for an ICVSResource and deletes all
	 * the cached information for the resource and all its children as
	 * well.
	 * At some point this should be integrated into the LocalResource ...
	 */
	public static void delete(ICVSResource resource) throws CVSException {
		
		// Deleting a file is an add-on that we need for the same-result
		// enviorment
		if (!resource.isFolder()) {
			resource.delete();
			Synchronizer.getInstance().reload(resource.getParent(),new NullProgressMonitor());
			return;
		}
		
		ICVSFolder folder = (ICVSFolder) resource;
		
		if (!folder.isCVSFolder()) {		
			ICVSFolder[] folders = folder.getFolders();
			for (int i = 0; i < folders.length; i++) {
				delete(folders[i]);
			}
		}
		
		folder.delete();
		Synchronizer.getInstance().reload(folder,new NullProgressMonitor());
	}	
}

