/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import org.eclipse.core.internal.databinding.observable.Queue;

import junit.framework.TestCase;

/**
 * @since 3.2
 *
 */
public class QueueTest extends TestCase {

	private Queue queue;

	@Override
	protected void setUp() throws Exception {
		this.queue = new Queue();
	}

	public void testIsEmpty() {
		assertTrue(queue.isEmpty());
		queue.enqueue("foo");
		assertFalse(queue.isEmpty());
		queue.enqueue("bar");
		assertFalse(queue.isEmpty());
		queue.dequeue();
		assertFalse(queue.isEmpty());
		queue.dequeue();
		assertTrue(queue.isEmpty());
	}

	public void testEnqueueAndDequeue() {
		try {
			queue.dequeue();
			fail("expected IllegalStateException");
		} catch(IllegalStateException ex) {
			// expected
		}
		queue.enqueue("foo");
		assertEquals("foo", queue.dequeue());
		try {
			queue.dequeue();
			fail("expected IllegalStateException");
		} catch(IllegalStateException ex) {
			// expected
		}
		queue.enqueue("foo");
		queue.enqueue("bar");
		queue.dequeue();
		queue.enqueue("bas");
		queue.enqueue("moo");
		assertEquals("bar", queue.dequeue());
		assertEquals("bas", queue.dequeue());
		assertEquals("moo", queue.dequeue());
		assertTrue(queue.isEmpty());
	}

}
