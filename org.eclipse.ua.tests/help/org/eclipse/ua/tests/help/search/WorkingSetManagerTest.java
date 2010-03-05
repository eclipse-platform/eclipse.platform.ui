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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.scope.WorkingSetScope;
import org.eclipse.help.internal.criteria.CriterionResource;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.workingset.AdaptableHelpResource;
import org.eclipse.help.internal.workingset.AdaptableToc;
import org.eclipse.help.internal.workingset.AdaptableTopic;
import org.eclipse.help.internal.workingset.WorkingSet;
import org.eclipse.help.internal.workingset.WorkingSetManager;

public class WorkingSetManagerTest extends TestCase {
	
	private WorkingSet[] workingSets;
	
	protected void setUp() throws Exception {
		WorkingSetManager manager = new WorkingSetManager();
		manager.restoreState();
		workingSets = manager.getWorkingSets();
		for (int i = 0; i < workingSets.length; i++) {
			manager.removeWorkingSet(workingSets[i]);
		}
		manager.saveState();
	}

	protected void tearDown() throws Exception {
		WorkingSetManager manager = new WorkingSetManager();
		WorkingSet[] wsetsToRemove = manager.getWorkingSets();
		for (int i = 0; i < wsetsToRemove.length; i++) {
			manager.removeWorkingSet(wsetsToRemove[i]);
		}
		for (int i = 0; i < workingSets.length; i++) {
		    manager.addWorkingSet(workingSets[i]);
		}
		manager.saveState();
	}

	public void testNewWSM() {
		WorkingSetManager mgr = new WorkingSetManager();
		assertEquals(0, mgr.getWorkingSets().length);
		WorkingSetManager mgr2 = new WorkingSetManager();
		assertEquals(mgr, mgr2);
		assertEquals(mgr.hashCode(), mgr2.hashCode());
	}

	public void testWSMWithToc() {
		WorkingSetManager mgr = new WorkingSetManager();
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

	public void testWSMWithTocContainsThatToc() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test");
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		AdaptableToc toc = mgr.getAdaptableToc(tocs[0].getHref());
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		mgr.addWorkingSet(wset);
		WorkingSetScope scope = new WorkingSetScope("test", mgr, "scope");
		assertTrue(scope.inScope(tocs[0]));
	};
	
	public void testWSMWithTocContainsNoOtherToc() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test");
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		AdaptableToc toc = mgr.getAdaptableToc(tocs[0].getHref());
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		mgr.addWorkingSet(wset);
		WorkingSetScope scope = new WorkingSetScope("test", mgr, "scope");
		for (int i = 1; i < tocs.length; i++) {
		    assertFalse(scope.inScope(tocs[i]));
		}
	};

	public void testWSMWithTocContainsThatTocsTopics() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test");
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		AdaptableToc toc = mgr.getAdaptableToc(tocs[0].getHref());
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		mgr.addWorkingSet(wset);
		WorkingSetScope scope = new WorkingSetScope("test", mgr, "scope");
		for (int i = 1; i < tocs.length; i++) {
			ITopic[] topics = tocs[i].getTopics();
		    for (int t = 0; t < topics.length; t++) {
			    assertFalse(scope.inScope(topics[t]));
		    }
		}
	};
	
	public void testWSMWithTocContainsNoOtherTocsTopics() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test");
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		AdaptableToc toc = mgr.getAdaptableToc(tocs[0].getHref());
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		mgr.addWorkingSet(wset);
		WorkingSetScope scope = new WorkingSetScope("test", mgr, "scope");
		ITopic[] topics = tocs[0].getTopics();
		for (int t = 0; t < topics.length; t++) {
			assertTrue(scope.inScope(topics[t]));
		}
	};
	
	public void testSaveRestoreWSMWithToc() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		wset.setElements(new AdaptableHelpResource[] { toc });
		mgr.addWorkingSet(wset);
		mgr.saveState();
		WorkingSetManager mgr2 = new WorkingSetManager();
		mgr2.restoreState();
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		assertEquals(1, readWsets.length);
		AdaptableHelpResource[] resources = readWsets[0].getElements();
		assertEquals(1, resources.length);
		assertTrue(resources[0].equals(toc));
	};
	
	public void testSaveRestoreWSMWithAllTocs() {
		WorkingSetManager mgr = new WorkingSetManager();
		createWsetWithAllTocs(mgr, "test");
		mgr.saveState();
		WorkingSetManager mgr2 = new WorkingSetManager();
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		assertEquals(1, readWsets.length);
		AdaptableHelpResource[] resources = readWsets[0].getElements();
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		assertEquals(tocs.length, resources.length);
	}

	public void testTocInScopeWithAllTocs() {
		WorkingSetManager mgr = new WorkingSetManager();
		createWsetWithAllTocs(mgr, "test1");
		mgr.saveState();
		WorkingSetScope scope = new WorkingSetScope("test1", mgr, "scope");
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		for (int i = 0; i < tocs.length; i++) {
		    assertTrue(scope.inScope(tocs[i]));
		}
	}

	public void testTopLevelTopicsInScopeWithAllTocs() {
		WorkingSetManager mgr = new WorkingSetManager();
		createWsetWithAllTocs(mgr, "test1a");
		WorkingSetScope scope = new WorkingSetScope("test1a", mgr, "scope");
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		for (int i = 0; i < tocs.length; i++) {;
			ITopic[] topics = tocs[i].getTopics();
			for (int j = 0; j < topics.length; j++) {
		         assertTrue(scope.inScope(topics[j]));
			}
		}
	}
	
	public void testSecondLevelTopicsInScopeWithAllTocs() {
		WorkingSetManager mgr = new WorkingSetManager();
		createWsetWithAllTocs(mgr, "test1b");
		WorkingSetScope scope = new WorkingSetScope("test1b", mgr, "scope");
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		for (int i = 0; i < tocs.length; i++) {;
			ITopic[] topics = tocs[i].getTopics();
			for (int j = 0; j < topics.length; j++) {
				ITopic[] subtopics = topics[j].getSubtopics();
				for (int k = 0; k < subtopics.length; k++) {
		            assertTrue(scope.inScope(subtopics[k]));
				}
			}
		}
	}

	private void createWsetWithAllTocs(WorkingSetManager mgr, String name) {
		WorkingSet wset = new WorkingSet(name);
		List tocList = new ArrayList();
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
		for (int i = 0; i < tocs.length; i++) {
			tocList.add(mgr.getAdaptableToc(tocs[i].getHref()));
		}
		wset.setElements((AdaptableHelpResource[]) tocList.toArray(new AdaptableToc[0]));
		mgr.addWorkingSet(wset);
	};

	public void testWSMWithTopics() {
		WorkingSetManager mgr = new WorkingSetManager();
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

	public void testSaveRestoreWSMWithTopics() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test2");
		AdaptableTopic topic1 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_1_");
		AdaptableTopic topic3 = mgr.getAdaptableTopic("/org.eclipse.ua.tests/data/help/toc/root.xml_3_");
		assertNotNull(topic1);
		assertNotNull(topic3);
		wset.setElements(new AdaptableHelpResource[] { topic1, topic3 });
		mgr.addWorkingSet(wset);
		mgr.saveState();
		WorkingSetManager mgr2 = new WorkingSetManager();
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

	public void testWSMWithMultipleWsets() {
		WorkingSetManager mgr = new WorkingSetManager();
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
	
	public void testSaveRestoreWSMWithMultipleWsets() {
		WorkingSetManager mgr = new WorkingSetManager();
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
		
		WorkingSetManager mgr2 = new WorkingSetManager();
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		
		assertEquals(2, readWsets.length);
		AdaptableHelpResource[] resourcesT3 = mgr2.getWorkingSet("test3").getElements();		
		assertEquals(1, resourcesT3.length);
		assertEquals(topic1, resourcesT3[0]);
		AdaptableHelpResource[] resourcesT4 = mgr2.getWorkingSet("test4").getElements();		
		assertEquals(1, resourcesT4.length);
		assertEquals(topic3, resourcesT4[0]);
	};

	public void testWSMWithCriteria() {
		WorkingSetManager mgr = new WorkingSetManager();
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
	
	public void testSaveRestoreWSMWithMCriteria() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test6");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		CriterionResource[] criteria =  { new CriterionResource("version") };
		criteria[0].addCriterionValue("1.0");	
		wset.setCriteria(criteria);
		mgr.addWorkingSet(wset);
		mgr.saveState();
		
		WorkingSetManager mgr2 = new WorkingSetManager();
		WorkingSet[] readWsets = mgr2.getWorkingSets();
		
		assertEquals(1, readWsets.length);
		CriterionResource[] readResources = readWsets[0].getCriteria();
		assertEquals(1, readResources.length);
	};

	public void testWSMWithMultipleCriteria() {
		WorkingSetManager mgr = new WorkingSetManager();
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

	public void testSaveRestoreWSMWithMultipleCriteria() {
		WorkingSetManager mgr = new WorkingSetManager();
		WorkingSet wset = new WorkingSet("test8");
		AdaptableToc toc = mgr.getAdaptableToc("/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertNotNull(toc);
		wset.setElements(new AdaptableHelpResource[] { toc });
		CriterionResource[] criteria = createResourceWithTwoCriteria();	
		wset.setCriteria(criteria);
		mgr.addWorkingSet(wset);
        mgr.saveState();
		
		WorkingSetManager mgr2 = new WorkingSetManager();
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
