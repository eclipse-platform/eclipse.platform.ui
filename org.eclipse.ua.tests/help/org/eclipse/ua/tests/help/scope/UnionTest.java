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
import org.eclipse.help.internal.base.scope.UnionScope;
import org.eclipse.ua.tests.help.other.UserTopic;

public class UnionTest extends TestCase {
	
	private AbstractHelpScope createScope(char char1, char char2) {
		AbstractHelpScope result = new UnionScope(
				new AbstractHelpScope[]{new MockScope(char1, true),
		        new MockScope(char2, true)});
		return result;
	}

	public void testInIn() {
		UserTopic topic = new UserTopic("ab", "http://www.eclipse.org", true);
		AbstractHelpScope scope = createScope('a', 'b');
		assertTrue(scope.inScope(topic));	
	}

	public void testInOut() {
		UserTopic topic = new UserTopic("a", "http://www.eclipse.org", true);
		AbstractHelpScope scope = createScope('a', 'b');
		assertTrue(scope.inScope(topic));	
	}
	
	public void testOutIn() {
		UserTopic topic = new UserTopic("b", "http://www.eclipse.org", true);
		AbstractHelpScope scope = createScope('a', 'b');
		assertTrue(scope.inScope(topic));	
	}
	
	public void testOutOut() {
		UserTopic topic = new UserTopic("c", "http://www.eclipse.org", true);
		AbstractHelpScope scope = createScope('a', 'b');
		assertFalse(scope.inScope(topic));	
	}
	
}
