/*******************************************************************************
 * Copyright (c) 2007, 2008 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208858)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ListDiffVisitor class
 *
 * @since 1.1
 */
public class ListDiffVisitorTest {
	ListDiffVisitorStub visitor;

	@Before
	public void setUp() throws Exception {
		visitor = new ListDiffVisitorStub();
	}

	@Test
	public void testHandleMove_DelegatesByDefault() {
		visitor.handleMove(0, 1, "element");
		assertEquals(
				"Default ListDiffVisitor.handleMove must delegate to handleRemove and handleAdd",
				"remove(0,element), add(1,element)", visitor.log);
	}

	@Test
	public void testHandleReplace_DelegatesByDefault() {
		visitor.handleReplace(2, "oldElement", "newElement");
		assertEquals(
				"Default ListDiffVisitor.handleReplace must delegate to handleRemove and handleAdd",
				"remove(2,oldElement), add(2,newElement)", visitor.log);
	}

	static class ListDiffVisitorStub extends ListDiffVisitor {
		String log = "";

		private void log(String message) {
			if (log.length() > 0)
				log += ", ";
			log += message;
		}

		@Override
		public void handleAdd(int index, Object element) {
			log("add(" + index + "," + element + ")");
		}

		@Override
		public void handleRemove(int index, Object element) {
			log("remove(" + index + "," + element + ")");
		}
	}
}
