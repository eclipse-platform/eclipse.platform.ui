package org.eclipse.team.tests.ccvs.core.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.internal.ccvs.core.CVSProjectSetCapability;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;

/**
 * This class tests an algorithm which suggests alternative repository locations
 * when importing a project set.
 */
public class CVSProjectSetImportTest extends TestCase {

	private KnownRepositories knownRepositories;
	private ICVSRepositoryLocation[] savedRepositories;

	public CVSProjectSetImportTest() {
		super();
	}

	public CVSProjectSetImportTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CVSProjectSetImportTest.class);
		return new CVSTestSetup(suite);
	}

	protected void setUp() throws Exception {
		super.setUp();
		knownRepositories = KnownRepositories.getInstance();
		savedRepositories = knownRepositories.getRepositories();
		// dispose all known repositories
		for (int i = 0; i < savedRepositories.length; i++) {
			knownRepositories.disposeRepository(savedRepositories[i]);
		}
	}

	public void testNullInfoMap() {
		IProject[] projects = new IProject[] { null };
		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				null);
		assertTrue(alternativeMap.isEmpty());
	}

	public void testEmptyInfoMap() {
		IProject[] projects = new IProject[] { null };
		Map infoMap = new HashMap();
		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertTrue(alternativeMap.isEmpty());

		assertEquals(0, knownRepositories.getRepositories().length);
	}

	public void testEmptyReferenceStrings() throws Exception {
		String[] referenceStrings = new String[] {};

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);
		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertTrue(alternativeMap.isEmpty());

		assertEquals(0, knownRepositories.getRepositories().length);
	}

	public void testMalformedReferenceString() throws Exception {
		String[] referenceStrings = new String[] { "Hi, I'm a malformed reference string." };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);
		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertTrue(alternativeMap.isEmpty());

		assertEquals(0, knownRepositories.getRepositories().length);
	}

	public void testUnknownSingleReferenceString() throws Exception {
		String[] referenceStrings = new String[] { "1.0,:pserver:dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.cvs.ssh,org.eclipse.team.cvs.ssh" };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);
		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertFalse(alternativeMap.isEmpty());

		assertEquals(0, knownRepositories.getRepositories().length);
	}

	public void testKnownSingleReferenceString() throws Exception {
		CVSRepositoryLocation repository = CVSRepositoryLocation
				.fromString(":pserver:dev.eclipse.org:/cvsroot/eclipse");
		knownRepositories.addRepository(repository, false);

		String[] referenceStrings = new String[] { "1.0,:pserver:dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.cvs.ssh,org.eclipse.team.cvs.ssh" };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);
		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		// alternativeMap should be empty as there is no need to ask for an
		// additional info
		assertTrue(alternativeMap.isEmpty());

		assertEquals(1, knownRepositories.getRepositories().length);
	}

	// test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=199108
	public void testSingleKnownRepositoryWithUsername() throws Exception {
		CVSRepositoryLocation joesRepository = CVSRepositoryLocation
				.fromString(":pserver:joe@dev.eclipse.org:/cvsroot/eclipse");
		knownRepositories.addRepository(joesRepository, false);

		String[] referenceStrings = new String[] { "1.0,:pserver:dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.cvs.ssh,org.eclipse.team.cvs.ssh" };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);
		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertFalse(alternativeMap.isEmpty());

		List suggestedList = (List) alternativeMap.get(CVSRepositoryLocation
				.fromString(":pserver:dev.eclipse.org:/cvsroot/eclipse"));
		assertEquals(1, suggestedList.size());
		ICVSRepositoryLocation suggestion = (ICVSRepositoryLocation) suggestedList
				.get(0);
		assertEquals(joesRepository, suggestion);

		assertEquals(1, knownRepositories.getRepositories().length);
	}

	public void testOneMatchingReferenceString() throws Exception {
		CVSRepositoryLocation joesRepository = CVSRepositoryLocation
				.fromString(":pserver:joe@dev.eclipse.org:/cvsroot/eclipse");
		knownRepositories.addRepository(joesRepository, false);

		String[] referenceStrings = new String[] {
				"1.0,:pserver:dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.cvs.ssh,org.eclipse.team.cvs.ssh",
				"1.0,:extssh:dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.tests.cvs.core,org.eclipse.team.tests.cvs.core" };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);

		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertFalse(alternativeMap.isEmpty());

		// test suggested repos for the first reference string
		List suggestedList = (List) alternativeMap.get(CVSRepositoryLocation
				.fromString(":pserver:dev.eclipse.org:/cvsroot/eclipse"));
		assertEquals(1, suggestedList.size());
		
		ICVSRepositoryLocation suggestion = (ICVSRepositoryLocation) suggestedList
				.get(0);
		// known repo matched
		assertEquals(joesRepository, suggestion);

		// test suggested repos for the second reference string
		CVSRepositoryLocation extsshRepository = CVSRepositoryLocation
				.fromString(":extssh:dev.eclipse.org:/cvsroot/eclipse");
		suggestedList = (List) alternativeMap.get(extsshRepository);
		assertEquals(2, suggestedList.size());
		
		suggestion = (ICVSRepositoryLocation) suggestedList.get(0);
		// the original repo as default
		assertEquals(extsshRepository, suggestion);
		suggestion = (ICVSRepositoryLocation) suggestedList.get(1);
		// the known repo as a second option
		assertEquals(joesRepository, suggestion);

		assertEquals(1, knownRepositories.getRepositories().length);
	}

	public void testTwoMatchingKnownRepositories() throws Exception {
		CVSRepositoryLocation joesRepository = CVSRepositoryLocation
				.fromString(":pserver:joe@dev.eclipse.org:/cvsroot/eclipse");
		knownRepositories.addRepository(joesRepository, false);
		CVSRepositoryLocation annsRepository = CVSRepositoryLocation
				.fromString(":pserver:ann@dev.eclipse.org:/cvsroot/eclipse");
		knownRepositories.addRepository(annsRepository, false);

		String[] referenceStrings = new String[] { "1.0,:pserver:dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.cvs.ssh,org.eclipse.team.cvs.ssh" };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);

		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertFalse(alternativeMap.isEmpty());

		List suggestedList = (List) alternativeMap.get(CVSRepositoryLocation
				.fromString(":pserver:dev.eclipse.org:/cvsroot/eclipse"));
		assertEquals(2, suggestedList.size());
		
		ICVSRepositoryLocation suggestion = (ICVSRepositoryLocation) suggestedList
				.get(0);
		assertEquals(annsRepository, suggestion);
		suggestion = (ICVSRepositoryLocation) suggestedList.get(1);
		assertEquals(joesRepository, suggestion);

		assertEquals(2, knownRepositories.getRepositories().length);
	}

	public void testThreeGroupsOfSuggestions() throws Exception {
		CVSRepositoryLocation pserverRepository = CVSRepositoryLocation
				.fromString(":pserver:dev.eclipse.org:/cvsroot/eclipse");
		knownRepositories.addRepository(pserverRepository, false);
		CVSRepositoryLocation localRepository = CVSRepositoryLocation
				.fromString(":pserver:localhost:/cvsroot/project");
		knownRepositories.addRepository(localRepository, false);

		String[] referenceStrings = new String[] { "1.0,:extssh:dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.cvs.ssh,org.eclipse.team.cvs.ssh" };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);

		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertFalse(alternativeMap.isEmpty());

		CVSRepositoryLocation extsshRepository = CVSRepositoryLocation
				.fromString(":extssh:dev.eclipse.org:/cvsroot/eclipse");
		List suggestedList = (List) alternativeMap.get(extsshRepository);
		assertEquals(3, suggestedList.size());
		
		ICVSRepositoryLocation suggestion = (ICVSRepositoryLocation) suggestedList
				.get(0);
		assertEquals(extsshRepository, suggestion);
		suggestion = (ICVSRepositoryLocation) suggestedList.get(1);
		assertEquals(pserverRepository, suggestion);
		suggestion = (ICVSRepositoryLocation) suggestedList.get(2);
		assertEquals(localRepository, suggestion);

		assertEquals(2, knownRepositories.getRepositories().length);
	}

	public void testOneCompatibleOfTwoKnown() throws Exception {
		CVSRepositoryLocation eclipseRepository = CVSRepositoryLocation
				.fromString(":pserver:dev.eclipse.org:/cvsroot/eclipse");
		knownRepositories.addRepository(eclipseRepository, false);
		CVSRepositoryLocation technologyRepository = CVSRepositoryLocation
				.fromString(":pserver:joe@dev.eclipse.org:/cvsroot/technology");
		knownRepositories.addRepository(technologyRepository, false);

		String[] referenceStrings = new String[] { "1.0,:extssh:joe@dev.eclipse.org:/cvsroot/eclipse,org.eclipse.team.cvs.ssh,org.eclipse.team.cvs.ssh" };

		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = CVSProjectSetCapability.asProjects(referenceStrings, infoMap);

		Map alternativeMap = CVSProjectSetCapability.isAdditionalRepositoryInformationRequired(projects,
				infoMap);
		assertFalse(alternativeMap.isEmpty());

		CVSRepositoryLocation extsshRepository = CVSRepositoryLocation
				.fromString(":extssh:joe@dev.eclipse.org:/cvsroot/eclipse");
		List suggestedList = (List) alternativeMap.get(extsshRepository);
		assertEquals(3, suggestedList.size());
		
		ICVSRepositoryLocation suggestion = (ICVSRepositoryLocation) suggestedList
				.get(0);
		assertEquals(extsshRepository, suggestion);
		suggestion = (ICVSRepositoryLocation) suggestedList.get(1);
		assertEquals(eclipseRepository, suggestion);
		suggestion = (ICVSRepositoryLocation) suggestedList.get(2);
		assertEquals(technologyRepository, suggestion);

		assertEquals(2, knownRepositories.getRepositories().length);
	}
	
	private static void assertEquals(ICVSRepositoryLocation expected,
			ICVSRepositoryLocation actual) {
		if (expected == actual)
			return;
		assertTrue(expected.equals(actual));
		assertEquals(expected.getLocation(true), actual.getLocation(true));
	}

}
