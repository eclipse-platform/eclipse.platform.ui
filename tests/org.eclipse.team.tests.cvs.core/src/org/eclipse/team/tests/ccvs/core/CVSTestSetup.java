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
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

public class CVSTestSetup extends TestSetup {
	public static final String REPOSITORY_LOCATION;
	public static final boolean INITIALIZE_REPO;
	public static final boolean DEBUG;
	public static final boolean LOCAL_REPO;
	public static final String RSH;
	
	public static CVSRepositoryLocation repository;
	
	// Static initializer for constants
	static {
		loadProperties();
		REPOSITORY_LOCATION = System.getProperty("eclipse.cvs.repository");
		INITIALIZE_REPO = Boolean.valueOf(System.getProperty("eclipse.cvs.initrepo", "false")).booleanValue();
		DEBUG = Boolean.valueOf(System.getProperty("eclipse.cvs.debug", "false")).booleanValue();
		RSH = System.getProperty("eclipse.cvs.rsh", "rsh");
		LOCAL_REPO = Boolean.valueOf(System.getProperty("eclipse.cvs.localRepo", "false")).booleanValue();
	}

	public static void loadProperties() {
		String propertiesFile = System.getProperty("eclipse.cvs.properties");
		if (propertiesFile == null) return;
		File file = new File(propertiesFile);
		if (file.isDirectory()) file = new File(file, "repository.properties");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try {
				for (String line; (line = reader.readLine()) != null; ) {						
					int sep = line.indexOf("=");
					String property = line.substring(0, sep).trim();
					String value = line.substring(sep + 1).trim();
					System.setProperty("eclipse.cvs." + property, value);
				}
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			System.err.println("Could not read repository properties file: " + file.getAbsolutePath());
		}
	}	

	/**
	 * Constructor for CVSTestSetup.
	 */
	public CVSTestSetup(Test test) {
		super(test);
	}

	public static void executeRemoteCommand(ICVSRepositoryLocation repository, String command) throws IOException, InterruptedException{
		String repositoryHost = repository.getHost();
		String userName = repository.getUsername();
		String localCommand;
		if (LOCAL_REPO) {
			localCommand= new String(command);
		} else {
			localCommand = new String(RSH + " " + repositoryHost + " -l " + userName + " " + command);
		}
		Process p = Runtime.getRuntime().exec(localCommand);
	p.waitFor();
	}
	
	/*
	 * Use rsh to delete any contents of the repository and initialize it again
	 */
	private static void initializeRepository(CVSRepositoryLocation repository) {
		String repoRoot = repository.getRootDirectory();
		try {
			executeRemoteCommand(repository, "rm -rf " + repoRoot);
			executeRemoteCommand(repository, "cvs -d " + repoRoot + " init");
		} catch (IOException e) {
			System.out.println("Unable to initialize remote repository: " + repository.getLocation());
		} catch (InterruptedException e) {
			System.out.println("Unable to initialize remote repository: " + repository.getLocation());
		}
	}
	
	public void setUp() throws CVSException {
		if (repository == null)
			repository = setupRepository(REPOSITORY_LOCATION);
	}

	protected CVSRepositoryLocation setupRepository(String location) throws CVSException {

		// Give some info about which repository the tests are running against
		System.out.println("Connecting to: " + location);
		
		// Validate that we can connect, also creates and caches the repository location. This
		// is important for the UI tests.
		CVSRepositoryLocation repository = (CVSRepositoryLocation)CVSProvider.getInstance().getRepository(location);
		//CVSRepositoryLocation repository = CVSRepositoryLocation.fromString(location);
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

