package org.eclipse.team.tests.ccvs.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

public class CVSTestSetup extends TestSetup {

	public static String REPOSITORY_LOCATION;
	static boolean INITIALIZE_REPO;
	public static final boolean DEBUG;

	
	public static CVSRepositoryLocation repository;
	
	// Static initializer for constants
	static {
		String propertiesFile = System.getProperty("eclipse.cvs.properties");
		if (propertiesFile != null)
			try {
				readRepositoryProperties(propertiesFile);
			} catch (IOException e) {
				System.out.println("Could not read repository properties file: " + propertiesFile);
			}
		REPOSITORY_LOCATION = System.getProperty("eclipse.cvs.repository");
		INITIALIZE_REPO = (System.getProperty("eclipse.cvs.initrepo")==null)?false:(new Boolean(System.getProperty("eclipse.cvs.initrepo")).booleanValue());
		DEBUG= (System.getProperty("eclipse.cvs.debug")==null)?false:(new Boolean(System.getProperty("eclipse.cvs.debug")).booleanValue());
	}
	/**
	 * Constructor for CVSTestSetup.
	 */
	public CVSTestSetup(Test test) {
		super(test);
	}

	/*
	 * Use rsh to delete any contents of the repository and initialize it again
	 */
	private static void initializeRepository(CVSRepositoryLocation repository) {
		String repositoryHost = repository.getHost();
		String userName = repository.getUsername();
		String repoRoot = repository.getRootDirectory();
		String cmd1 = new String("rsh " + repositoryHost + " -l " + userName + " rm -rf " + repoRoot);
		String cmd2 = new String("rsh " + repositoryHost + " -l " + userName + " cvs -d " + repoRoot + " init");
		try {
			Process p = Runtime.getRuntime().exec(cmd1);
			p.waitFor();
			p = Runtime.getRuntime().exec(cmd2);
		} catch (IOException e) {
			System.out.println("Unable to initialize remote repository: " + repository.getLocation());
		} catch (InterruptedException e) {
			System.out.println("Unable to initialize remote repository: " + repository.getLocation());
		}
	}
	
	static void readRepositoryProperties(String filename) throws IOException {
		File file = new File(filename);
		if (file.isDirectory())
			file = new File(file, "repository.properties");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			int sep = line.indexOf("=");
			String property = line.substring(0, sep).trim();
			String value = line.substring(sep + 1).trim();
			System.setProperty("eclipse.cvs." + property, value);
		}
		
	}
	public void setUp() throws CVSException {
		if (repository == null)
			repository = setupRepository(REPOSITORY_LOCATION);
		if (!DEBUG)
			CVSProviderPlugin.getProvider().setPrintStream(new PrintStream(new NullOutputStream()));
	}

	protected CVSRepositoryLocation setupRepository(String location) throws CVSException {

		// Give some info about which repository the tests are running against
		System.out.println("Connecting to: " + location);
		
		// Validate that we can connect
		CVSRepositoryLocation repository = CVSRepositoryLocation.fromString(location);
		try {
			repository.validateConnection(new NullProgressMonitor());
		} catch (CVSException e) {
			System.out.println("Unable to connect to remote repository: " + repository.getLocation());
			throw e;
		}
		
		// Initialize the repo if requested (requires rsh access)
		if( INITIALIZE_REPO ) {
			initializeRepository(repository);
		}
		
		return repository;
	}
	
	public void tearDown() throws CVSException {
		// Nothing to do here
	}

}

