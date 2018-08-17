/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.internal.databinding.observable.Queue;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class QueueTest {

	private Queue queue;

	@Before
	public void setUp() throws Exception {
		this.queue = new Queue();
	}

	@Test
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

	@Test
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
