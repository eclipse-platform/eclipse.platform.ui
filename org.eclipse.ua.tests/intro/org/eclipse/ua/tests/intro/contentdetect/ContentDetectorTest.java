/*******************************************************************************
 *  Copyright (c) 2007, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.intro.contentdetect;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.test.OrderedTestSuite;

import org.eclipse.ui.internal.intro.impl.model.ExtensionMap;
import org.eclipse.ui.internal.intro.universal.contentdetect.ContentDetectHelper;
import org.eclipse.ui.internal.intro.universal.contentdetect.ContentDetector;

public class ContentDetectorTest extends TestCase {
	
	public static Test suite() {
		return new OrderedTestSuite(ContentDetectorTest.class, new String[] {
			"testContributorCount",
			"testContributorSaveNoNames",
			"testContributorSaveThreeContributors",
			"testExtensionMapping",
			"testExtensionMapSingleton",
			"testForNewContent",
			"testNoSavedState",
			"testStartPage",
			"testStateChanges"
		});
	}
	
	public void testContributorCount() {
		ContentDetectHelper helper = new ContentDetectHelper();
		helper.saveExtensionCount(4);
		assertEquals(4, helper.getExtensionCount());
		assertEquals(4, helper.getExtensionCount());
		helper.saveExtensionCount(5);
		helper.saveExtensionCount(6);
		assertEquals(6, helper.getExtensionCount());
	}

	public void testContributorSaveNoNames() {
		ContentDetectHelper helper = new ContentDetectHelper();
		helper.saveContributors(new HashSet<String>());
		assertTrue(helper.getContributors().size() == 0);
	}

	public void testContributorSaveThreeContributors() {
		ContentDetectHelper helper = new ContentDetectHelper();
		HashSet<String> contributors = new HashSet<String>();
		contributors.add("one");
		contributors.add("two");
		contributors.add("three");
		helper.saveContributors(contributors);
		@SuppressWarnings("unchecked")
		Set<String> savedContributors = helper.getContributors();
		assertTrue(savedContributors.size() == 3);
		assertTrue(savedContributors.contains("one"));
		assertTrue(savedContributors.contains("two"));
		assertTrue(savedContributors.contains("three"));
	}
	
	public void testForNewContent() {
		ContentDetectHelper helper = new ContentDetectHelper();
		HashSet<String> contributors = new HashSet<String>();
		contributors.add("one");
		contributors.add("two");
		contributors.add("three");
		contributors.add("four");
		Set<String> previous = new HashSet<String>();
		previous.add("five");
		previous.add("two");
		previous.add("one");
		Set newContributors = helper.findNewContributors(contributors, previous);
		assertTrue(newContributors.size() == 2);
		assertTrue(newContributors.contains("four"));
		assertTrue(newContributors.contains("three"));
	}

	public void testNoSavedState() {
		ContentDetectHelper helper = new ContentDetectHelper();
		helper.deleteStateFiles();
		assertTrue(helper.getContributors().isEmpty());
		assertEquals(ContentDetectHelper.NO_STATE, helper.getExtensionCount());	
		ContentDetector detector = new ContentDetector();
		assertFalse(detector.isNewContentAvailable());
		Set newContent = ContentDetector.getNewContributors();
		assertTrue(newContent == null || newContent.size() == 0);
		String firstContribution = (String) helper.getContributors().iterator().next();
		assertFalse(ContentDetector.isNew(firstContribution));
	}
	
	public void testStateChanges() {
		ContentDetectHelper helper = new ContentDetectHelper();
		helper.deleteStateFiles();
		ContentDetector detector = new ContentDetector();
		assertFalse(detector.isNewContentAvailable());
		// Calling the detector should save the state
		int extensionCount = helper.getExtensionCount();
		assertTrue(extensionCount > 0);
		// Simulate removing an extension
		helper.saveExtensionCount(extensionCount + 1);
		assertFalse(detector.isNewContentAvailable());
		// Make the first extension appear new
		helper.saveExtensionCount(extensionCount - 1);
		@SuppressWarnings("unchecked")
		Set<String> contributors = helper.getContributors();
		String firstContribution = contributors.iterator().next();
		String copyOfFirstContribution = "" + firstContribution;
		contributors.remove(firstContribution);
		helper.saveContributors(contributors);
		assertTrue(detector.isNewContentAvailable());
		assertEquals(1, ContentDetector.getNewContributors().size());
		assertTrue(ContentDetector.isNew(firstContribution));
		assertTrue(ContentDetector.isNew(copyOfFirstContribution));
		// Calling a new detector should yield the same result
		ContentDetector detector2 = new ContentDetector();
		assertTrue(detector2.isNewContentAvailable());
		assertEquals(1, ContentDetector.getNewContributors().size());
		assertTrue(ContentDetector.isNew(firstContribution));
		assertTrue(ContentDetector.isNew(copyOfFirstContribution));
	}
	
	public void testExtensionMapSingleton() {
		ExtensionMap map1 = ExtensionMap.getInstance();
		ExtensionMap map2 = ExtensionMap.getInstance();
		assertEquals(map1, map2);
	}
	
	public void testExtensionMapping() {
		ExtensionMap map = ExtensionMap.getInstance();
		map.clear();
		map.putPluginId("anchor1", "org.eclipse.test");
		map.putPluginId("anchor2", "org.eclipse.test");
		map.putPluginId("anchor3", "org.eclipse.test3");
		assertEquals("org.eclipse.test", map.getPluginId("anchor1"));
		assertEquals("org.eclipse.test", map.getPluginId("anchor2"));
		assertEquals("org.eclipse.test3", map.getPluginId("anchor3"));
		map.clear();
		assertNull(map.getPluginId("anchor1"));
	}
	
	public void testStartPage() {
		ExtensionMap map = ExtensionMap.getInstance();
		map.setStartPage("tutorials");
		map.setStartPage("whats-new");
		assertEquals("whats-new", map.getStartPage());
		map.clear();
		assertNull(map.getStartPage());
	}

	protected void finalize() throws Throwable {
		// Delete state files so that if we start Eclipse we don't see all content as new
		ContentDetectHelper helper = new ContentDetectHelper();
		helper.deleteStateFiles();
		super.finalize();
	}

}
