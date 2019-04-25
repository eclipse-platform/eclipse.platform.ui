/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 *     Sebastian Lohmeier <sebastian@monochromata.de> - Bug 484310
 ******************************************************************************/
package org.eclipse.ui.tests.filteredtree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class PatternFilterTest {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	private class MockViewer extends ContentViewer {

		private Object input;

		@Override
		public Control getControl() {
			return null;
		}

		@Override
		public ISelection getSelection() {
			return null;
		}

		@Override
		public void refresh() {
		}

		@Override
		public void setSelection(ISelection selection, boolean reveal) {
		}

		/*
		 * Override to not require a control to work
		 */
		@Override
		public void setInput(Object input) {
			Assert.isTrue(getContentProvider() != null,
					"Instances of ContentViewer must have a content provider assigned before the setInput method is called."); //$NON-NLS-1$

			Object oldInput = getInput();
			getContentProvider().inputChanged(this, oldInput, input);
			this.input = input;

			// call input hook
			inputChanged(input, oldInput);
		}

		@Override
		public Object getInput() {
			return this.input;
		}
	}

	private ContentViewer viewer;

	@Before
	public void setup() {
		viewer = new MockViewer();
	}

	@Test
	public void testBasicFilter() {
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(new String[] {});
		PatternFilter filter = new PatternFilter();

		filter.setPattern("b");
		assertTrue(filter.select(viewer, null, "a b c"));

		filter.setPattern("jre");
		assertTrue(filter.select(viewer, null, "Java build path buildpath problem exclusion inclusion pattern folder outputfolder filtered resource output compiler 1.5 5.0 J2SE5 project specific projectspecific strictly compatible JRE execution environment"));
	}

	@Test
	public void testPatternFilterOrder() {
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(new String[] {});
		PatternFilter filter = new PatternFilter();
		filter.setPattern("a b");
		// simple match prefix
		assertTrue(filter.select(viewer, null, "a b"));
		assertTrue(filter.select(viewer, null, "ac b"));
		assertTrue(filter.select(viewer, null, "a bc"));
		assertTrue(filter.select(viewer, null, "ac bc"));
		// don't match if not prefix
		assertFalse(filter.select(viewer, null, "ca b"));
		assertFalse(filter.select(viewer, null, "a cb"));
		// unmatched pattern word ('b' here) filters out
		assertFalse(filter.select(viewer, null, "a c"));

		// word order doesn't matter
		assertTrue(filter.select(viewer, null, "b a"));
		assertTrue(filter.select(viewer, null, "bc a"));
		assertTrue(filter.select(viewer, null, "b ac"));
		assertTrue(filter.select(viewer, null, "bc ac"));
		// additional word at any place still match
		assertTrue(filter.select(viewer, null, "a b c"));
		assertTrue(filter.select(viewer, null, "c a b"));
		assertTrue(filter.select(viewer, null, "c a b c"));
		assertTrue(filter.select(viewer, null, "a c b c"));
		assertTrue(filter.select(viewer, null, "a c b"));
		assertTrue(filter.select(viewer, null, "c a c b"));
	}

	@Test
	public void testEmptyItemNotMatched() {
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(new String[] {});
		PatternFilter filter = new PatternFilter();
		filter.setPattern("a");
		assertFalse(filter.select(viewer, null, ""));
	}

	@Test
	public void testWildcardAndCaseInsensitive() {
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(new String[] {});
		PatternFilter filter = new PatternFilter();

		filter.setPattern("a*b");
		assertTrue(filter.select(viewer, null, "ab"));
		assertTrue(filter.select(viewer, null, "acb"));
		assertTrue(filter.select(viewer, null, "acdbe"));
		// upper case
		assertTrue(filter.select(viewer, null, "Ab"));
		assertTrue(filter.select(viewer, null, "Acb"));
		assertTrue(filter.select(viewer, null, "Acdbe"));

		filter.setPattern("*CdE");
		assertTrue(filter.select(viewer, null, "AbCdEfGh"));

		filter.setPattern("*CdE");
		assertTrue(filter.select(viewer, null, "AbCdEfGh"));

		filter.setPattern("**cDe");
		assertTrue(filter.select(viewer, null, "AbCdEfGh"));

		filter.setPattern("**c*e*i");
		assertTrue(filter.select(viewer, null, "AbCdEfGi"));
	}
}
