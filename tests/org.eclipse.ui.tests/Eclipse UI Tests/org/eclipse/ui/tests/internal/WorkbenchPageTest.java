/*******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class WorkbenchPageTest extends UITestCase {

	public WorkbenchPageTest(String testName) {
		super(testName);
	}

	public void test1() {
		assertOrderAfterPerformedShowIn(asList("a", "b", "c"));
	}

	public void test2() {
		assertOrderAfterPerformedShowIn(asList("c", "a", "b"), "c");
	}

	public void test3() {
		assertOrderAfterPerformedShowIn(asList("b", "c", "a"), "c", "b");
	}

	public void test4() {
		assertOrderAfterPerformedShowIn(asList("a", "b", "c"), "a");
	}

	public void test5() {
		assertOrderAfterPerformedShowIn(asList("b", "a", "c"), "b", "b");
	}

	public void test6() {
		assertOrderAfterPerformedShowIn(asList("b", "c", "a"), "b", "c", "b");
	}

	public void test7() {
		assertOrderAfterPerformedShowIn(asList("a", "b", "c"), "d");
	}

	public void test8() {
		assertOrderAfterPerformedShowIn(asList("b", "a", "c"), "d", "b");
	}

	private static ArrayList<String> asList(String... ids) {
		return new ArrayList(Arrays.asList(ids));
	}

	private void assertOrderAfterPerformedShowIn(List<String> expected, String... showIn) {
		WorkbenchPage page = getWorkbenchPage();
		ArrayList<String> partIds = asList("a", "b", "c");
		for (String id : showIn) {
			page.performedShowIn(id);
		}
		page.sortShowInPartIds(partIds);
		assertThat(partIds, is(expected));
	}

	private WorkbenchPage getWorkbenchPage() {
		return (WorkbenchPage) fWorkbench.getActiveWorkbenchWindow().getActivePage();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		clearMruPartIds();
	}

	/**
	 * Loads, using reflection, the internal MRU part ids list from inside the
	 * WorkbenchPage and clears it.
	 */
	private void clearMruPartIds() throws Exception {
		Field f = WorkbenchPage.class.getDeclaredField("mruPartIds");
		f.setAccessible(true);
		((List) f.get(getWorkbenchPage())).clear();
	}
}
