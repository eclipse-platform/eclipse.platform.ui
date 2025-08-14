/*******************************************************************************
 * Copyright (c) 2015, 2022 Tasktop Technologies and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Ignore
public class WorkbenchPageTest extends UITestCase {

	public WorkbenchPageTest() {
		super(WorkbenchPageTest.class.getSimpleName());
	}

	@Test
	public void test1() {
		assertOrderAfterPerformedShowIn(asList("a", "b", "c"));
	}

	@Test
	public void test2() {
		assertOrderAfterPerformedShowIn(asList("c", "a", "b"), "c");
	}

	@Test
	public void test3() {
		assertOrderAfterPerformedShowIn(asList("b", "c", "a"), "c", "b");
	}

	@Test
	public void test4() {
		assertOrderAfterPerformedShowIn(asList("a", "b", "c"), "a");
	}

	@Test
	public void test5() {
		assertOrderAfterPerformedShowIn(asList("b", "a", "c"), "b", "b");
	}

	@Test
	public void test6() {
		assertOrderAfterPerformedShowIn(asList("b", "c", "a"), "b", "c", "b");
	}

	@Test
	public void test7() {
		assertOrderAfterPerformedShowIn(asList("a", "b", "c"), "d");
	}

	@Test
	public void test8() {
		assertOrderAfterPerformedShowIn(asList("b", "a", "c"), "d", "b");
	}

	private static ArrayList<String> asList(String... ids) {
		return new ArrayList<>(Arrays.asList(ids));
	}

	private void assertOrderAfterPerformedShowIn(List<String> expected, String... showIn) {
		WorkbenchPage page = getWorkbenchPage();
		ArrayList<String> partIds = asList("a", "b", "c");
		for (String id : showIn) {
			page.performedShowIn(id);
		}
		page.sortShowInPartIds(partIds);
		assertEquals(partIds, expected);
	}

	private WorkbenchPage getWorkbenchPage() {
		return (WorkbenchPage) getWorkbench().getActiveWorkbenchWindow().getActivePage();
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
