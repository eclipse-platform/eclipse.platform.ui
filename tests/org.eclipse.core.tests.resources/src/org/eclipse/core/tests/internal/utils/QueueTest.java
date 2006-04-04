/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.utils;

import java.util.Iterator;
import junit.framework.*;
import org.eclipse.core.internal.utils.Queue;

public class QueueTest extends TestCase {
	public QueueTest() {
		super(null);
	}

	public QueueTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(QueueTest.class);
	}

	public void testElements() {
		/* add elements without removing */
		Queue queue = new Queue(5, false);
		for (int i = 0; i < 10; i++)
			queue.add(String.valueOf(i));

		/* assert elements are correct */
		Iterator elements = queue.iterator();
		for (int i = 0; i < 10; i++)
			assertTrue("1.0", elements.next().equals(String.valueOf(i)));

		/* add and remove elements */
		queue = new Queue(5, false);
		for (int i = 0; i < 5; i++)
			queue.add(String.valueOf(i));
		for (int i = 0; i < 4; i++)
			queue.remove();
		for (int i = 0; i < 10; i++)
			queue.add(String.valueOf(i));

		/* assert elements are correct */
		elements = queue.iterator();
		for (int i = 4; i < 5; i++)
			assertTrue("2.0", elements.next().equals(String.valueOf(i)));
		for (int i = 0; i < 10; i++)
			assertTrue("2.1", elements.next().equals(String.valueOf(i)));
	}

	public void testGrow() {
		/* add elements without removing */
		Queue queue = new Queue(5, false);
		for (int i = 0; i < 10; i++)
			queue.add(String.valueOf(i));
		assertTrue("1.0", queue.size() == 10);

		/* add and remove elements */
		queue = new Queue(5, false);
		for (int i = 0; i < 5; i++)
			queue.add(String.valueOf(i));
		for (int i = 0; i < 4; i++)
			queue.remove();
		for (int i = 0; i < 10; i++) {
			queue.add(String.valueOf(i));
		}
		assertTrue("2.0", queue.size() == 11);
	}

	public void testRemoveTail() {
		/* head < tail */
		Queue queue = new Queue(10, false);
		for (int i = 0; i < 8; i++)
			queue.add(String.valueOf(i));
		assertTrue("1.0", queue.peekTail().equals("7"));
		assertTrue("1.1", queue.removeTail().equals("7"));
		assertTrue("1.2", queue.peekTail().equals("6"));

		/* head > tail */
		queue = new Queue(5, false);
		for (int i = 0; i < 5; i++)
			queue.add(String.valueOf(i));
		for (int i = 0; i < 4; i++)
			queue.remove();
		for (int i = 0; i < 10; i++) {
			queue.add(String.valueOf(i));
		}
		assertTrue("2.0", queue.peekTail().equals("9"));
		assertTrue("2.1", queue.removeTail().equals("9"));
		assertTrue("2.2", queue.peekTail().equals("8"));
	}

	public void testReusableElements() {
		/**/
		class ReusableObject {
		}

		/* add elements */
		Queue queue = new Queue(10, true);
		for (int i = 0; i < 9; i++) {
			ReusableObject o = new ReusableObject();
			queue.add(o);
		}

		/* remove elements */
		for (int i = 0; i < 9; i++)
			queue.remove();

		/* add one more element to avoid null */
		queue.add(new ReusableObject());

		/* add again reusing the elements */
		for (int i = 0; i < 9; i++) {
			ReusableObject o = (ReusableObject) queue.getNextAvailableObject();
			assertTrue("1.0", o != null);
			queue.add(o);
		}
	}
}
