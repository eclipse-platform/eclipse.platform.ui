package org.eclipse.team.tests.ccvs.core.compatible;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.framework.Test;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;

/**
 * @version 	1.0
 * @author 	${user}
 */
public class CompatibleTestSetup extends CVSTestSetup {

	public static final String REFERENCE_CLIENT_REPOSITORY=System.getProperty("eclipse.cvs.repository1");
	public static final String ECLIPSE_CLIENT_REPOSITORY=System.getProperty("eclipse.cvs.repository2");
	
	public static CVSRepositoryLocation referenceClientRepository;
	public static CVSRepositoryLocation eclipseClientRepository;
	
	/**
	 * Constructor for CompatibleTestSetup.
	 */
	public CompatibleTestSetup(Test test) {
		super(test);
	}
	
	/**
	 * For compatibility testing, we need to set up two repositories
	 */
	public void setUp() throws CVSException {
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(false);
		if ((referenceClientRepository != null) && (eclipseClientRepository != null))
			return;
		referenceClientRepository = setupRepository(REFERENCE_CLIENT_REPOSITORY);
		eclipseClientRepository = setupRepository(ECLIPSE_CLIENT_REPOSITORY);
	}
	
	public void tearDown() throws CVSException {
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(true);
	}
}
