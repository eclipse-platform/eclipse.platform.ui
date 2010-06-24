/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.scope;

import junit.framework.TestCase;

/**
 * Test that leaf topics with no href do not show in the tree
 * In this test empty means has no href
 */

import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.scope.ScopeUtils;
import org.eclipse.ua.tests.help.other.UserIndexEntry;
import org.eclipse.ua.tests.help.other.UserToc;
import org.eclipse.ua.tests.help.other.UserTopic;

public class EmptyLeafRemoval extends TestCase {

	public void testEmptyLeafTopic() {
		UserTopic topic = new UserTopic("ab", null, true);
		assertFalse(ScopeUtils.showInTree(topic, new MockScope('a', false)));
	}

	public void testEmptyParentOfEmptyLeafTopic() {
		UserTopic topic = new UserTopic("ab", null, true);
		UserTopic child = new UserTopic("ac", null, true);
		topic.addTopic(child);
		assertFalse(ScopeUtils.showInTree(topic, new MockScope('a', false)));
	}

	public void testEmptyParentOfNonEmptyLeafTopic() {
		UserTopic topic = new UserTopic("ab", null, true);
		UserTopic child = new UserTopic("ac", "http://www.eclipse.org", true);
		topic.addTopic(child);
		assertTrue(ScopeUtils.showInTree(topic, new MockScope('a', false)));
	}

	public void testEmptyParentOfNonEmptyLeafTopicHierarchical() {
		UserTopic topic = new UserTopic("ab", null, true);
		UserTopic child = new UserTopic("ac", "http://www.eclipse.org", true);
		topic.addTopic(child);
		assertTrue(ScopeUtils.showInTree(topic, new MockScope('a', true)));
	}
	
	public void testEmptyGrandParentWithOutOfScopeChildTopicHierarchical() {
		UserTopic topic = new UserTopic("ab", null, true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		UserTopic grandChild = new UserTopic("ac", "http://www.eclipse.org", true);
		topic.addTopic(child);
		child.addTopic(grandChild);
		assertFalse(ScopeUtils.showInTree(topic, new MockScope('a', true)));
	}
	
	public void testEmptyGrandparentOfNonEmptyLeafTopicHierarchical() {
		UserTopic topic = new UserTopic("ab", null, true);
		UserTopic child = new UserTopic("ac", null, true);
		UserTopic grandChild = new UserTopic("ac", "http://www.eclipse.org", true);
		topic.addTopic(child);
		child.addTopic(grandChild);
		assertTrue(ScopeUtils.showInTree(topic, new MockScope('a', true)));
	}

	public void testTocParentOfEmptyLeaf() {
		UserToc toc = new UserToc("ab", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("ac", null, true);
		toc.addTopic(child);
		AbstractHelpScope scope = new MockScope('a', false);
		assertFalse(ScopeUtils.showInTree(toc, scope));
	}
	
	public void testTocParentOfNonEmptyLeaf() {
		UserToc toc = new UserToc("ab", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("ac", "http://www.eclipse.org", true);
		toc.addTopic(child);
		AbstractHelpScope scope = new MockScope('a', true);
		assertTrue(ScopeUtils.showInTree(toc, scope));
	}

	public void testEntryParentOfEmptyLeaf() {
		UserIndexEntry entry = new UserIndexEntry("ab", true);
		UserTopic child = new UserTopic("ac", null, true);
		entry.addTopic(child);
		AbstractHelpScope scope = new MockScope('a', true);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}

	public void testEntryParentOfNonEmptyLeaf() {
		UserIndexEntry entry = new UserIndexEntry("ab", true);
		UserTopic child = new UserTopic("ac", "http://www.eclipse.org", true);
		entry.addTopic(child);
		AbstractHelpScope scope = new MockScope('a', true);
		assertTrue(ScopeUtils.showInTree(entry, scope));
	}
	
	public void testEntryGrandParentOfNonEmptyLeaf() {
		UserIndexEntry entry = new UserIndexEntry("ab", true);
		UserIndexEntry child = new UserIndexEntry("ab", true);
		UserTopic grandhild = new UserTopic("ac", "http://www.eclipse.org", true);
		entry.addEntry(child);
		child.addTopic(grandhild);
		AbstractHelpScope scope = new MockScope('a', true);
		assertTrue(ScopeUtils.showInTree(entry, scope));
	}
	
}
