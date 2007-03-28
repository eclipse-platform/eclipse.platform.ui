/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.intro.contentdetect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ui.internal.intro.universal.contentdetect.ContentDetectHelper;
import org.eclipse.ui.internal.intro.universal.contentdetect.ContentDetector;

public class ContentDetectorTest extends TestCase {
	
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
		helper.saveContributors(new HashSet());
		assertTrue(helper.getContributors().size() == 0);
	}

	public void testContributorSaveThreeContributors() {
		ContentDetectHelper helper = new ContentDetectHelper();
		HashSet contributors = new HashSet();
		contributors.add("one");
		contributors.add("two");
		contributors.add("three");
		helper.saveContributors(contributors);
		List savedContributors = helper.getContributors();
		assertTrue(savedContributors.size() == 3);
		assertTrue(savedContributors.contains("one"));
		assertTrue(savedContributors.contains("two"));
		assertTrue(savedContributors.contains("three"));
	}
	
	public void testForNewContent() {
		ContentDetectHelper helper = new ContentDetectHelper();
		HashSet contributors = new HashSet();
		contributors.add("one");
		contributors.add("two");
		contributors.add("three");
		contributors.add("four");
		List previous = new ArrayList();
		previous.add("five");
		previous.add("two");
		previous.add("one");
		String[] newContributors = helper.findNewContributors(contributors, previous);
		Arrays.sort(newContributors);
		assertTrue(newContributors.length == 2);
		assertTrue(newContributors[0] == "four");
		assertTrue(newContributors[1] == "three");
	}

	public void testNoSavedState() {
		ContentDetectHelper helper = new ContentDetectHelper();
		helper.deleteStateFiles();
		assertTrue(helper.getContributors().isEmpty());
		assertEquals(ContentDetectHelper.NO_STATE, helper.getExtensionCount());	
		ContentDetector detector = new ContentDetector();
		assertFalse(detector.isNewContentAvailable());
	}
	
	public void testStateChanges() {
		ContentDetectHelper helper = new ContentDetectHelper();
		helper.deleteStateFiles();
		ContentDetector detector = new ContentDetector();
		assertFalse(detector.isNewContentAvailable());
		// Calling the detector should save the state
		int extensionCount = helper.getExtensionCount();
		assertTrue(extensionCount > 0);
		assertEquals(extensionCount, helper.getContributors().size());
		helper.saveExtensionCount(extensionCount + 1);
		assertFalse(detector.isNewContentAvailable());
		helper.saveExtensionCount(extensionCount - 1);
		assertTrue(detector.isNewContentAvailable());
	}

}
