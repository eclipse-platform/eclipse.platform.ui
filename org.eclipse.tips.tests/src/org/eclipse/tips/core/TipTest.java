/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.core;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TipTest {

	private static final String HTML = "<head></head>";
	private static final String SUBJECT_TIP = "Tip Subject";
	private TestTipManager fManager;
	private TestTipProvider fProvider;
	private TestTip fTip;

	@Before
	public void setup() {
		fManager = new TestTipManager();
		fProvider = (TestTipProvider) new TestTipProvider().setManager(fManager);
		createTestDate();
		fTip = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP) {
			@Override
			public List<TipAction> getActions() {
				ArrayList<TipAction> actions = new ArrayList<>();
				ArrayList<String> result = new ArrayList<>();
				Runnable runner = () -> result.add("entry");

				actions.add(new TipAction("text", "tooltip", runner, null));
				return actions;
			}
		};
	}

	@Test
	public void testHashCode() {
		assertTrue(fProvider.getNextTip().hashCode() != 0);
	}

	@Test
	public void testHashCode2() {
		TestTip testTip = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP);
		TestTip testTip2 = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP);
		assertTrue(testTip.hashCode() == testTip2.hashCode());
	}

	@Test
	public void testTip() {
		new TestTip(fProvider.getID(), HTML, SUBJECT_TIP);
	}

	@Test
	public void testGetAction() {
		assertTrue(fTip.getActions().size() > 0);
	}

	@Test
	public void testGetCreationDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fTip.getCreationDate());
		assertTrue(calendar.get(Calendar.MONTH) == 11);
		assertTrue(calendar.get(Calendar.YEAR) == 1964);
		assertTrue(calendar.get(Calendar.DAY_OF_MONTH) == 31);
	}

	@Test
	public void testGetHTML() {
		assertTrue(fTip.getHTML() != null);
	}

	@Test
	public void testGetImage() {
		assertTrue(fTip.getImage() == null);
	}

	@Test
	public void testGetSubject() {
		assertTrue(fTip.getSubject() != null);
		assertTrue(fTip.getSubject().equals(SUBJECT_TIP));
	}

	@Test
	public void testEqualsObject() {
		TestTip testTip = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP);
		TestTip testTipx = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP);
		assertTrue(!testTip.equals(null));
		assertTrue(testTip.equals(testTip));
		assertTrue(!testTip.equals(fTip));
		assertTrue(testTip.equals(testTipx));

		TestTipProvider testTipProvider = new TestTipProvider() {
			@Override
			public String getID() {
				return "sss";
			}
		};

		TestTipProvider testTipProvider2 = new TestTipProvider() {
			@Override
			public String getID() {
				return null;
			}
		};

		TestTipProvider testTipProvider3 = new TestTipProvider() {
			@Override
			public String getID() {
				return "sss";
			}
		};

		assertTrue(!testTipProvider.equals(testTipProvider2));
		assertTrue(!testTipProvider.equals(testTipProvider3));
		TestTip testTip2 = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP + "DDD");
		assertTrue(!testTip.equals(testTip2));
		assertTrue(!testTip.equals(testTip2));

		TestTip testTip3 = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP + "DDD");
		assertTrue(!testTip.equals(testTip3));
		assertTrue(!testTip3.equals(testTip));

		TestTip testTip4 = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP + "DDD");
		assertTrue(!testTip.equals(testTip4));
		assertTrue(!testTip4.equals(testTip));

		TestTip testTip5 = new TestTip(fProvider.getID(), HTML, SUBJECT_TIP + "DDDWW");
		assertTrue(!testTip.equals(testTip5));
		assertTrue(!testTip5.equals(testTip));

		TestTip testTip6 = new TestTip(fProvider.getID(), HTML, null);
		assertTrue(!testTip.equals(testTip6));
		assertTrue(!testTip6.equals(testTip));

	}

	@Test
	public void testIsRead() {
		assertTrue(!fManager.isRead(fTip));
		fManager.setAsRead(fTip);
		fManager.setAsRead(fTip);
		assertTrue(fManager.isRead(fTip));
	}

	private void createTestDate() {
		fProvider.setTips(Arrays.asList(new TestTip(fProvider.getID(), "<b>bold</b>", "Tip 1"),
				new TestTip(fProvider.getID(), "<b>bold2</b>", "Tip 2")));
	}
}