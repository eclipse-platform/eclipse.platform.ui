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

import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.scope.ScopeUtils;
import org.eclipse.ua.tests.help.other.UserIndexEntry;
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
	
}
