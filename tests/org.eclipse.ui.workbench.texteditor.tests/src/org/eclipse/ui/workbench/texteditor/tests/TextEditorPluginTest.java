/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import org.eclipse.ui.internal.texteditor.HistoryTracker;

/**
 * Tests the FindReplaceDialog.
 *
 * @since 3.1
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TextEditorPluginTest {

	@Rule
	public TestName testName = new TestName();

	Random rand = new Random(55); //pseudo-random for repeatability

	@Test
	public void testEditPositionHistory() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5, true);

		assertEquals(0, history.getSize());
		history.addOrReplace(10);
		assertEquals(1, history.getSize());
		assertEquals(10, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(20);
		assertEquals(2, history.getSize());
		assertEquals(20, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(30);
		assertEquals(3, history.getSize());
		assertEquals(30, history.getCurrentBrowsePoint().intValue());

		checkContent(history, new Integer[] { 10, 20, 30 });


		int replaced= history.addOrReplace(40).intValue();
		assertEquals(3, history.getSize());
		assertEquals(10, replaced);
		assertEquals(Integer.valueOf(40), history.getCurrentBrowsePoint());

		HistoryTracker.Navigator<Integer> nav = history.navigator();
		assertEquals(Integer.valueOf(40), nav.currentItem());
		assertEquals(Integer.valueOf(30), nav.priorItem());
		assertEquals(Integer.valueOf(20), nav.priorItem());
		assertEquals(Integer.valueOf(40), nav.priorItem());
		checkContent(history, new Integer[] { 20, 30, 40 });

		assertFalse(history.contains(10));

		replaced = history.addOrReplace(22);
		assertTrue(history.contains(22));
		assertFalse(history.contains(20));
		assertEquals(20, replaced);
		checkContent(history, new Integer[] { 30, 40, 22 });
		//assertTrue(22 == history.getCurrentBrowsePoint());

		replaced = history.addOrReplace(31);
		assertTrue(history.contains(31));
		assertFalse(history.contains(30));
		assertEquals(30, replaced);
		assertEquals(31, history.getCurrentBrowsePoint().intValue());
		checkContent(history, new Integer[] { 40, 22, 31 });

		replaced = history.addOrReplace(60);
		assertTrue(history.contains(60));
		assertEquals(60, history.getCurrentBrowsePoint().intValue());
		assertEquals(3, history.getSize());
		checkContent(history, new Integer[] { 22, 31, 60 });

		assertEquals(31, history.browseBackward().intValue());
		assertEquals(31, history.getCurrentBrowsePoint().intValue());
		assertEquals(3, history.getSize());

		//consuming size times should bring you full circle back to origin
		testBacktrackCycle(history);

		//try editing after backtracking less than full cycle
		history.browseBackward();
		Integer last = history.getCurrentBrowsePoint();
		assertEquals(Integer.valueOf(22), last);
		history.addOrReplace(11);
		checkContent(history, new Integer[] { 31, 60, 11 });

		testBacktrackCycle(history);
		history.browseBackward();
		assertEquals(Integer.valueOf(60), history.getCurrentBrowsePoint());
	}

	<T> void checkContent(HistoryTracker<T> history, T[] data) {
		HistoryTracker.Navigator<T> nav = history.navigator();
		assertEquals(data[data.length - 1], nav.currentItem());

		for(int i=data.length - 2; i>0; i--) {
			assertEquals(data[i], nav.priorItem());
		}
		assertEquals(history.getSize(), data.length);
	}

	@Test
	public void testEditPositionHistory2() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5, true);

		assertEquals(0, history.getSize());
		history.addOrReplace(10);
		assertEquals(1, history.getSize());
		assertEquals(10, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(20);
		assertEquals(2, history.getSize());
		assertEquals(20, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(30);
		assertEquals(3, history.getSize());
		assertEquals(30, history.getCurrentBrowsePoint().intValue());

		int replaced= history.addOrReplace(22).intValue();
		assertEquals(3, history.getSize());
		assertEquals(20, replaced);
		assertEquals(Integer.valueOf(22), history.getCurrentBrowsePoint());

		assertEquals(Integer.valueOf(30), history.browseBackward());
		assertEquals(Integer.valueOf(10), history.browseBackward());
		assertEquals(Integer.valueOf(22), history.browseBackward());

		testBacktrackCycle(history);
	}

	@Test
	public void testHistoryEviction() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5,
				true);

		assertEquals(0, history.getSize());
		history.addOrReplace(10);
		assertEquals(1, history.getSize());
		assertEquals(10, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(10);
		assertEquals(1, history.getSize());
		assertEquals(10, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(11);
		assertEquals(1, history.getSize());
		assertEquals(11, history.getCurrentBrowsePoint().intValue());
	}

	@Test
	public void testHistoryEviction2() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5,
				true);

		history.addOrReplace(10);
		history.addOrReplace(20);
		history.addOrReplace(30);
		assertEquals(Integer.valueOf(20), history.browseBackward());
		assertEquals(Integer.valueOf(10), history.addOrReplace(40));
	}

	@Test
	public void testHistoryEviction3() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5,
				true);

		history.addOrReplace(10);
		history.addOrReplace(11);
		history.addOrReplace(20);
		history.addOrReplace(21);
		history.addOrReplace(12);
		checkContent(history, new Integer[] { 21, 12 });
	}

	@Test
	public void testLinearEditPositionHistory() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5,
				false);

		assertEquals(0, history.getSize());
		history.addOrReplace(10);
		assertEquals(1, history.getSize());
		assertEquals(10, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(20);
		assertEquals(2, history.getSize());
		assertEquals(20, history.getCurrentBrowsePoint().intValue());

		history.addOrReplace(30);
		assertEquals(3, history.getSize());
		assertEquals(30, history.getCurrentBrowsePoint().intValue());

		int replaced = history.addOrReplace(22);
		assertEquals(3, history.getSize());
		assertEquals(20, replaced);
		assertEquals(Integer.valueOf(22), history.getCurrentBrowsePoint());

		assertEquals(Integer.valueOf(30), history.browseBackward());
		assertEquals(Integer.valueOf(10), history.browseBackward());
		assertEquals(Integer.valueOf(10), history.browseBackward());

		assertEquals(Integer.valueOf(30), history.browseForward());
		assertEquals(Integer.valueOf(22), history.browseForward());
		assertEquals(Integer.valueOf(22), history.browseForward());

	}

	@Test
	public void testLinearEditPositionHistory2() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5,
				false);

		history.addOrReplace(10);
		history.addOrReplace(20);
		history.addOrReplace(30);

		int replaced= history.addOrReplace(22).intValue();
		assertEquals(3, history.getSize());
		assertEquals(20, replaced);
		assertEquals(Integer.valueOf(22), history.getCurrentBrowsePoint());

		//end reached, go no further
		assertEquals(Integer.valueOf(22), history.getNext());

		assertEquals(Integer.valueOf(30), history.browseBackward());
		assertEquals(Integer.valueOf(10), history.browseBackward());

		//beginning reached, go no further
		assertEquals(Integer.valueOf(10), history.browseBackward());
	}

	@Test
	public void testMRUOrderAlwaysPreserved() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5,
				false);

		history.addOrReplace(10);
		history.addOrReplace(20);
		history.addOrReplace(30);

		assertEquals(Integer.valueOf(20), history.browseBackward());
		history.addOrReplace(11);
		assertEquals(Integer.valueOf(11), history.getCurrentBrowsePoint());

		assertEquals(Integer.valueOf(30), history.browseBackward());
		assertEquals(Integer.valueOf(20), history.browseBackward());
	}

	@Test
	public void testMRUOrderAlwaysPreserved2() {
		HistoryTracker<Integer> history= new HistoryTracker<>(3,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5,
				false);

		history.addOrReplace(10);
		history.addOrReplace(20);
		history.addOrReplace(30);

		assertEquals(Integer.valueOf(20), history.browseBackward());
		history.addOrReplace(40);
		assertEquals(Integer.valueOf(40), history.getCurrentBrowsePoint());

		assertEquals(Integer.valueOf(30), history.browseBackward());
		assertEquals(Integer.valueOf(20), history.browseBackward());
		assertEquals(Integer.valueOf(20), history.browseBackward());

	}

	private <T> void testBacktrackCycle(HistoryTracker<T> history) {
		T last = history.getCurrentBrowsePoint();
		for(int i=0; i<history.getSize() -1; i++) {
			history.browseBackward();
			assertNotEquals(last, history.getCurrentBrowsePoint());
		}
		history.browseBackward();
		assertEquals(last, history.getCurrentBrowsePoint());
	}

	@Test
	public void testEditPositionHistoryChaos() {
		final int HISTORY_SIZE= 10;
		HistoryTracker<Integer> history= new HistoryTracker<>(HISTORY_SIZE,
				Integer.class,
				(a, b) -> Math.abs(a - b) < 5, true);

		for(int i=0;i<100;i++) {
			if(rand.nextBoolean()) {
				addRandom(history);
			} else {
				goBack(history);
			}
			assertTrue(history.isHealthy());
			assertTrue(history.getSize() <= HISTORY_SIZE);
		}
	}

	@Test
	public void testLinearEditPositionHistoryChaos() {
		final int HISTORY_SIZE= 10;
		HistoryTracker<Integer> history= new HistoryTracker<>(HISTORY_SIZE,
				Integer.class,
				(a,b) -> Math.abs(a - b) < 5,
				false
				);

		int backsInARow = 0;
		for(int i=0;i<100;i++) {
			if(rand.nextBoolean()) {
				backsInARow = 0;
				addRandom(history);
			} else {
				backsInARow ++;
				goBackLinear(history, backsInARow < history.getSize());
			}
			assertTrue(history.isHealthy());
			assertTrue(history.getSize() <= HISTORY_SIZE);
		}
	}


	private void addRandom(HistoryTracker<Integer> history) {
		Integer latest = rand.nextInt(50);
		history.addOrReplace(latest);
		assertEquals(latest, history.getCurrentBrowsePoint());
	}

	private void goBack(HistoryTracker<Integer> history) {
		int size = history.getSize();
		Integer latest = history.getCurrentBrowsePoint();
		Integer prior = history.browseBackward();
		if (size > 1) {
			assertNotEquals(latest, prior);
		}
	}

	private void goBackLinear(HistoryTracker<Integer> history, boolean shouldMove) {
		Integer latest= history.getCurrentBrowsePoint();
		Integer prior = history.browseBackward();
		if(shouldMove)
			assertNotEquals(latest, prior);
		else
			assertEquals(latest, prior);
	}

}
