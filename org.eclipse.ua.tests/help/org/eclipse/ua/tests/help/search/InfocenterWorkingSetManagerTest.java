/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.search;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.help.internal.criteria.CriterionResource;
import org.eclipse.help.internal.webapp.servlet.InfocenterWorkingSetManager;
import org.eclipse.help.internal.workingset.AdaptableHelpResource;
import org.eclipse.help.internal.workingset.AdaptableToc;
import org.eclipse.help.internal.workingset.AdaptableTopic;
import org.eclipse.help.internal.workingset.WorkingSet;
import org.eclipse.ua.tests.help.webapp.MockServletRequest;
import org.eclipse.ua.tests.help.webapp.MockServletResponse;

public class InfocenterWorkingSetManagerTest extends TestCase {
	
	public void testIWSMWithToc() throws IOException {
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager
		    (new MockServletRequest(), new MockServletResponse(), "en");
		WorkingSet wset = new WorkingSet("test");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		mgr.addWorkingSet(wset);
		WorkingSet[] readWsets = mgr.getWorkingSets();
		assertEquals(1, readWsets.length);
		AdaptableHelpResource[] resources = readWsets[0].getElements();
		assertEquals(1, resources.length);
		assertTrue(resources[0].equals(toc));
	};

	public void testSaveRestoreIWSMWithToc() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset = new WorkingSet("test");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		wset.setElements(new AdaptableHelpResource[] { toc });
		mgr.addWorkingSet(wset);
		MockServletRequest req2 = new MockServletRequest();
		MockServletResponse resp2 = new MockServletResponse();
		req2.setCookies(resp.getCookies());
		InfocenterWorkingSetManager mgr2 = new InfocenterWorkingSetManager(req2, resp2, "en");
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		assertEquals(1, readWsets.length);
		AdaptableHelpResource[] resources = readWsets[0].getElements();
		assertEquals(1, resources.length);
		assertTrue(resources[0].equals(toc));
	};

	public void testIWSMWithTopics() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset = new WorkingSet("test2");
		AdaptableTopic topic1 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_1_");
		AdaptableTopic topic3 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_3_");
		assertNotNull(topic1);
		assertNotNull(topic3);
		wset.setElements(new AdaptableHelpResource[] { topic1, topic3 });
		mgr.addWorkingSet(wset);
		WorkingSet[] readWsets = mgr.getWorkingSets();
		assertEquals(1, readWsets.length);
		AdaptableHelpResource[] resources = readWsets[0].getElements();
		assertEquals(2, resources.length);
		if (resources[0].equals(topic1)) {
			assertEquals(topic3, resources[1]);
			assertNotSame(topic3, resources[0]);
		} else {
			assertEquals(topic3, resources[0]);
			assertEquals(topic1, resources[1]);
			assertNotSame(topic3, resources[1]);
		}
	};

	public void testSaveRestoreIWSMWithTopics() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset = new WorkingSet("test2");
		AdaptableTopic topic1 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_1_");
		AdaptableTopic topic3 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_3_");
		assertNotNull(topic1);
		assertNotNull(topic3);
		wset.setElements(new AdaptableHelpResource[] { topic1, topic3 });
		mgr.addWorkingSet(wset);

		MockServletRequest req2 = new MockServletRequest();
		MockServletResponse resp2 = new MockServletResponse();
		req2.setCookies(resp.getCookies());
		InfocenterWorkingSetManager mgr2 = new InfocenterWorkingSetManager(req2, resp2, "en");
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		assertEquals(1, readWsets.length);
		AdaptableHelpResource[] resources = readWsets[0].getElements();
		assertEquals(2, resources.length);
		if (resources[0].equals(topic1)) {
			assertEquals(topic3, resources[1]);
			assertNotSame(topic3, resources[0]);
		} else {
			assertEquals(topic3, resources[0]);
			assertEquals(topic1, resources[1]);
			assertNotSame(topic3, resources[1]);
		}
	};

	public void testIWSMWithMultipleWsets() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset1 = new WorkingSet("test3");
		WorkingSet wset2 = new WorkingSet("test4");
		AdaptableTopic topic1 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_1_");
		AdaptableTopic topic3 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_3_");
		assertNotNull(topic1);
		assertNotNull(topic3);
		wset1.setElements(new AdaptableHelpResource[] { topic1 });
		wset2.setElements(new AdaptableHelpResource[] { topic3 });
		mgr.addWorkingSet(wset1);
		mgr.addWorkingSet(wset2);
		WorkingSet[] readWsets = mgr.getWorkingSets();
		assertEquals(2, readWsets.length);
		AdaptableHelpResource[] resourcesT3 = mgr.getWorkingSet("test3").getElements();		
		assertEquals(1, resourcesT3.length);
		assertEquals(topic1, resourcesT3[0]);
		AdaptableHelpResource[] resourcesT4 = mgr.getWorkingSet("test4").getElements();		
		assertEquals(1, resourcesT4.length);
		assertEquals(topic3, resourcesT4[0]);
	};
	
	public void testSaveRestoreIWSMWithMultipleWsets() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset1 = new WorkingSet("test3");
		WorkingSet wset2 = new WorkingSet("test4");
		AdaptableTopic topic1 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_1_");
		AdaptableTopic topic3 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_3_");
		assertNotNull(topic1);
		assertNotNull(topic3);
		wset1.setElements(new AdaptableHelpResource[] { topic1 });
		wset2.setElements(new AdaptableHelpResource[] { topic3 });
		mgr.addWorkingSet(wset1);
		mgr.addWorkingSet(wset2);
		
		MockServletRequest req2 = new MockServletRequest();
		MockServletResponse resp2 = new MockServletResponse();
		req2.setCookies(resp.getCookies());
		InfocenterWorkingSetManager mgr2 = new InfocenterWorkingSetManager(req2, resp2, "en");
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		
		assertEquals(2, readWsets.length);
		AdaptableHelpResource[] resourcesT3 = mgr2.getWorkingSet("test3").getElements();		
		assertEquals(1, resourcesT3.length);
		assertEquals(topic1, resourcesT3[0]);
		AdaptableHelpResource[] resourcesT4 = mgr2.getWorkingSet("test4").getElements();		
		assertEquals(1, resourcesT4.length);
		assertEquals(topic3, resourcesT4[0]);
	};

	public void testIWSMWithCriteria() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset = new WorkingSet("test5");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		CriterionResource[] criteria =  { new CriterionResource("version") };
		criteria[0].addCriterionValue("1.0");	
		wset.setCriteria(criteria);
		mgr.addWorkingSet(wset);
		WorkingSet[] readWsets = mgr.getWorkingSets();
		assertEquals(1, readWsets.length);
		CriterionResource[] readResources = readWsets[0].getCriteria();
		assertEquals(1, readResources.length);
	};
	
	public void testSaveRestoreIWSMWithMCriteria() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset = new WorkingSet("test6");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		CriterionResource[] criteria =  { new CriterionResource("version") };
		criteria[0].addCriterionValue("1.0");	
		wset.setCriteria(criteria);
		mgr.addWorkingSet(wset);
		
		MockServletRequest req2 = new MockServletRequest();
		MockServletResponse resp2 = new MockServletResponse();
		req2.setCookies(resp.getCookies());
		InfocenterWorkingSetManager mgr2 = new InfocenterWorkingSetManager(req2, resp2, "en");
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		
		assertEquals(1, readWsets.length);
		CriterionResource[] readResources = readWsets[0].getCriteria();
		assertEquals(1, readResources.length);
	};

	public void testIWSMWithMultipleCriteria() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset = new WorkingSet("test7");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		CriterionResource[] criteria =  { new CriterionResource("version"), new CriterionResource("platform") };
		criteria[0].addCriterionValue("1.0");	
		criteria[1].addCriterionValue("linux");	
		criteria[1].addCriterionValue("MacOS");	
		wset.setCriteria(criteria);
		mgr.addWorkingSet(wset);
		WorkingSet[] readWsets = mgr.getWorkingSets();
		assertEquals(1, readWsets.length);
		CriterionResource[] readResources = readWsets[0].getCriteria();
		checkResourceWithTwoChildren(readResources);
	};

	public void testSaveRestoreIWSMWithMultipleCriteria() throws IOException {
		MockServletRequest req = new MockServletRequest();
		MockServletResponse resp = new MockServletResponse();
		InfocenterWorkingSetManager mgr = new InfocenterWorkingSetManager(req, resp, "en");
		WorkingSet wset = new WorkingSet("test8");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		CriterionResource[] criteria = createResourceWithTwoCriteria();	
		wset.setCriteria(criteria);
		mgr.addWorkingSet(wset);
        MockServletRequest req2 = new MockServletRequest();
		MockServletResponse resp2 = new MockServletResponse();
		req2.setCookies(resp.getCookies());
		InfocenterWorkingSetManager mgr2 = new InfocenterWorkingSetManager(req2, resp2, "en");
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		
		assertEquals(1, readWsets.length);
		CriterionResource[] readResources = readWsets[0].getCriteria();
		checkResourceWithTwoChildren(readResources);
	}

	private void checkResourceWithTwoChildren(CriterionResource[] readResources) {
		assertEquals(2, readResources.length);
		CriterionResource readVersion;
		CriterionResource readPlatform;
		if (readResources[0].getCriterionName().equals("version")) {
			readVersion = readResources[0];
			readPlatform = readResources[1];
		} else {
			readVersion = readResources[0];
			readPlatform = readResources[1];
		}
		assertEquals("version", readVersion.getCriterionName());
		assertEquals(1, readVersion.getCriterionValues().size());
		assertTrue(readVersion.getCriterionValues().contains("1.0"));
		assertEquals("platform", readPlatform.getCriterionName());
		assertEquals(2, readPlatform.getCriterionValues().size());
		assertTrue(readPlatform.getCriterionValues().contains("linux"));
		assertTrue(readPlatform.getCriterionValues().contains("MacOS"));
	}

	private CriterionResource[] createResourceWithTwoCriteria() {
		CriterionResource[] criteria;
		criteria = new CriterionResource[2];
		criteria[0] = new CriterionResource("version");
		criteria[0].addCriterionValue("1.0");
		criteria[1] = new CriterionResource("platform");
		criteria[1].addCriterionValue("linux");	
		criteria[1].addCriterionValue("MacOS");
		return criteria;
	};

}
