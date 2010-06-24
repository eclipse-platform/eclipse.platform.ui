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

import org.eclipse.help.IIndexEntry2;
import org.eclipse.help.IIndexSee;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.scope.ScopeUtils;
import org.eclipse.help.internal.index.Index;
import org.eclipse.ua.tests.help.other.UserIndex;
import org.eclipse.ua.tests.help.other.UserIndexEntry;
import org.eclipse.ua.tests.help.other.UserIndexSee;
import org.eclipse.ua.tests.help.other.UserToc;
import org.eclipse.ua.tests.help.other.UserTopic;

public class ScopeHierarchy extends TestCase {

	public void testHierarchicalToc() {
		UserToc toc = new UserToc("ab", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		toc.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(toc, scope));
	}

	public void testHierarchicalTocDepth3() {
		UserToc toc = new UserToc("c", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("b", "http://www.eclipse.org", true);
		toc.addTopic(child);
		UserTopic grandChild = new UserTopic("c", "http://www.eclipse.org", true);
		child.addTopic(grandChild);
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(toc, scope));
	}
	
	public void testNonHierarchicalTocDepth3() {
		UserToc toc = new UserToc("a", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("b", "http://www.eclipse.org", true);
		toc.addTopic(child);
		UserTopic grandChild = new UserTopic("c", "http://www.eclipse.org", true);
		child.addTopic(grandChild);
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(toc, scope));
	}

	public void testNonHierarchicalToc() {
		UserToc toc = new UserToc("ab", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		toc.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(toc, scope));
	}
	
	public void testHierarchicalTopic() {
		UserTopic topic = new UserTopic("ab", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		topic.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(topic, scope));
	}

	public void testNonHierarchicalTopic() {
		UserTopic topic = new UserTopic("ab", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		topic.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(topic, scope));
	}
	
	public void testNonHierarchicalTopicThreeDeep() {
		UserTopic topic = new UserTopic("ab", "http://www.eclipse.org", true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		UserTopic grandChild = new UserTopic("d", "http://www.eclipse.org", true);
		topic.addTopic(child);
		child.addTopic(grandChild);
		AbstractHelpScope scope = new MockScope('d', false);
		assertTrue(ScopeUtils.showInTree(topic, scope));
	}

	public void testHierarchicalEntry() {
		UserIndexEntry entry = new UserIndexEntry("ab", true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		entry.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}

	public void testNonHierarchicalEntry() {
		UserIndexEntry entry = new UserIndexEntry("ab", true);
		UserTopic child = new UserTopic("c", "http://www.eclipse.org", true);
		entry.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(entry, scope));
	}
	
	public void testNonHierarchicalEntryThreeDeep() {
		UserIndexEntry entry = new UserIndexEntry("a", true);
		UserIndexEntry childEntry = new UserIndexEntry("b", true);
		entry.addEntry(childEntry);
		UserTopic grandChild = new UserTopic("c", "http://www.eclipse.org", true);
		childEntry.addTopic(grandChild);
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(entry, scope));
	}
	
	public void testHierarchicalEntryThreeDeep() {
		UserIndexEntry entry = new UserIndexEntry("a", true);
		UserIndexEntry childEntry = new UserIndexEntry("c", true);
		entry.addEntry(childEntry);
		UserTopic grandChild = new UserTopic("c", "http://www.eclipse.org", true);
		childEntry.addTopic(grandChild);
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}
	
	public void testHierarchicalEntryNoInScopeSubtopic() {
		UserIndexEntry entry = new UserIndexEntry("c", true);
		UserTopic child = new UserTopic("a", "http://www.eclipse.org", true);
		entry.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}

	public void testNonHierarchicalEntryNoInScopeSubtopic() {
		UserIndexEntry entry = new UserIndexEntry("c", true);
		UserTopic child = new UserTopic("a", "http://www.eclipse.org", true);
		entry.addTopic(child);
		AbstractHelpScope scope = new MockScope('c', false);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}

	public void testSeeTargetInScopeNonHierarchical() {
		IIndexSee see = createSee("compile", "c++");
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(see, scope));
	}
	
	public void testSeeParentTargetInScopeNonHierarchical() {
		IIndexEntry2 entry = createSeeParentEntry("compile", "c++");
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(entry, scope));
	}

	public void testSeeTargetOutOfScopeNonHierarchical() {
		IIndexSee see = createSee("build", "c++");
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(see, scope));
	}
	
	public void testSeeParentTargetOutOfScopeNonHierarchical() {
		IIndexEntry2 entry = createSeeParentEntry("build", "c++");
		AbstractHelpScope scope = new MockScope('c', false);
		assertTrue(ScopeUtils.showInTree(entry, scope));
	}

	public void testSeeTargetWithChildOutOfScopeNonHierarchical() {
		IIndexSee see = createSee("compilation", "build");
		AbstractHelpScope scope = new MockScope('c', false);
		assertFalse(ScopeUtils.showInTree(see, scope));
	}
	
	public void testSeeParentTargetWithChildOutOfScopeNonHierarchical() {
		IIndexEntry2 entry = createSeeParentEntry("compilation", "build");
		AbstractHelpScope scope = new MockScope('c', false);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}

	public void testSeeTargetInScopeHierarchical() {
		IIndexSee see = createSee("compile", "c++");
		AbstractHelpScope scope = new MockScope('c', true);
		assertTrue(ScopeUtils.showInTree(see, scope));
	}
	
	public void testSeeParentTargetInScopeHierarchical() {
		IIndexEntry2 entry = createSeeParentEntry("compile", "c++");
		AbstractHelpScope scope = new MockScope('c', true);
		assertTrue(ScopeUtils.showInTree(entry, scope));
	}

	public void testSeeTargetOutOfScopeHierarchical() {
		IIndexSee see = createSee("build", "c++");
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(see, scope));
	}
	
	public void testSeeParentTargetOutOfScopeHierarchical() {
		IIndexEntry2 entry = createSeeParentEntry("build", "c++");
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}

	public void testSeeTargetWithChildOutOfScopeHierarchical() {
		IIndexSee see = createSee("compilation", "build");
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(see, scope));
	}
	
	public void testSeeParentTargetWithChildOutOfScopeHierarchical() {
		IIndexEntry2 entry = createSeeParentEntry("compilation", "build");
		AbstractHelpScope scope = new MockScope('c', true);
		assertFalse(ScopeUtils.showInTree(entry, scope));
	}
	
	private IIndexSee createSee(String targetEntryName, String targetTopicName) {
		IIndexEntry2 entry = createSeeParentEntry(targetEntryName, targetTopicName);
		return entry.getSees()[0];
	}

	private IIndexEntry2 createSeeParentEntry(String targetEntryName, String targetTopicName) {
		IIndexEntry2 entry;
		UserIndex userIndex = new UserIndex(true);
		UserIndexEntry targetEntry = new UserIndexEntry(targetEntryName, true);
		UserIndexEntry seeParentEntry = new UserIndexEntry("compilation", true);
		userIndex.addEntry(targetEntry);
		userIndex.addEntry(seeParentEntry);
		UserTopic child = new UserTopic(targetTopicName, "http://www.eclipse.org", true);
		targetEntry.addTopic(child);
		UserIndexSee userSee = new UserIndexSee(targetEntryName, false);
		seeParentEntry.addSee(userSee);	
		Index index = new Index(userIndex);
		entry = (IIndexEntry2)index.getEntries()[1];
		return entry;
	}	
	
}
