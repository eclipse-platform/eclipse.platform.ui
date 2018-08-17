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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tips.core.internal.LogUtil;
import org.eclipse.tips.core.internal.TipManager;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class TipManagerTest {

	private TestTipManager fManager;
	private TestTipProvider fProvider1;
	private TestTipProvider fProvider2;

	@Before
	public void testTipManager() {
		fManager = new TestTipManager();
		fManager.open(false);
		fProvider1 = new TestTipProvider();
		fProvider2 = new TestTipProvider();
	}

	@Test
	public void testGetProvider() {
		fManager.register(fProvider1);
		TipProvider provider = fManager.getProvider(fProvider1.getID());
		assertTrue(provider == fProvider1);
	}

	@Test
	public void testRegister() {
		fManager.register(fProvider1);
		TipProvider provider = fManager.getProvider(fProvider1.getID());
		assertTrue(provider == fProvider1);
	}

	/**
	 * Only one provider with the same id can be registered.
	 */
	@Test
	public void testGetProviders() {
		fManager.register(fProvider1);
		fManager.register(fProvider2);
		fManager.register(fProvider2);
		assertTrue(fManager.getProviders().size() + "", fManager.getProviders().size() == 1);
	}

	@Test
	public void testGetStartupBeahvior() {
		assertTrue(fManager.getStartupBehavior() == TipManager.START_DIALOG);
		assertTrue(fManager.getStartupBehavior() != TipManager.START_BACKGROUND);
		assertTrue(fManager.getStartupBehavior() != TipManager.START_DISABLE);

		fManager.setStartupBehavior(TipManager.START_BACKGROUND);
		assertTrue(fManager.getStartupBehavior() == TipManager.START_BACKGROUND);
		assertTrue(fManager.getStartupBehavior() != TipManager.START_DIALOG);
		assertTrue(fManager.getStartupBehavior() != TipManager.START_DISABLE);

		fManager.setStartupBehavior(TipManager.START_DISABLE);
		assertTrue(fManager.getStartupBehavior() != TipManager.START_BACKGROUND);
		assertTrue(fManager.getStartupBehavior() != TipManager.START_DIALOG);
		assertTrue(fManager.getStartupBehavior() == TipManager.START_DISABLE);
	}

	@Test
	public void testLoad() {
		ArrayList<String> test = new ArrayList<>();
		TestTipProvider testTipProvider = new TestTipProvider() {
			@Override
			public IStatus loadNewTips(IProgressMonitor monitor) {
				test.add("fff");
				return Status.OK_STATUS;
			}
		};
		fManager.register(testTipProvider);
		assertTrue(!test.isEmpty());
	}

	@Test
	public void testLoad2() {
		ArrayList<String> test = new ArrayList<>();
		TestTipProvider testTipProvider = new TestTipProvider() {
			@Override
			public IStatus loadNewTips(IProgressMonitor monitor) {
				test.add("fff");
				return Status.OK_STATUS;
			}
		};
		fManager.register(testTipProvider);
		assertTrue(!test.isEmpty());
	}

	@Test
	public void testOpen() {
		ArrayList<String> test = new ArrayList<>();
		TestTipManager m = new TestTipManager() {
			@Override
			public TipManager open(boolean startUp) {
				if (startUp) {
					test.add("1");
				} else {
					test.add("1");
					test.add("2");
				}
				return this;
			}
		};
		m.open(true);
		assertTrue(test.size() == 1);
		test.clear();
		m.open(false);
		assertTrue(test.size() == 2);
	}

	@Test
	public void testDispose() {
		fManager.dispose();
	}

	@Test
	public void testDispose2() {
		fManager.register(fProvider1);
		fManager.dispose();
	}

	@Test
	public void testLogClassOfQException() {
		Exception e = new Exception("FFF");
		fManager.log(LogUtil.error(getClass(), e));
	}

	@Test
	public void testIsRead() {
		fManager.register(fProvider1);
		fProvider1.setTips(Arrays.asList(new TestTip(fProvider1.getID(), "<b>bold</b>", "Tip 1"),
				new TestTip(fProvider1.getID(), "<b>bold2</b>", "Tip 2")));
		fManager.setAsRead(fProvider1.getCurrentTip());
		assertTrue(fManager.isRead(fProvider1.getCurrentTip()));
	}

	@Test
	public void testSetRead() {
		fManager.register(fProvider1);
		fProvider1.setTips(Arrays.asList(new TestTip(fProvider1.getID(), "<b>bold</b>", "Tip 1"),
				new TestTip(fProvider1.getID(), "<b>bold2</b>", "Tip 2")));
		fManager.setAsRead(fProvider1.getCurrentTip());
		assertTrue(fManager.isRead(fProvider1.getCurrentTip()));
	}

	@Test
	public void testSetServeUnread() {
		fManager.register(fProvider1);
		fProvider1.setTips(Arrays.asList(new TestTip(fProvider1.getID(), "<b>bold</b>", "Tip 1"),
				new TestTip(fProvider1.getID(), "<b>bold2</b>", "Tip 2")));
		fManager.setAsRead(fProvider1.getCurrentTip());
		assertTrue(fProvider1.getTips().size() + "", fProvider1.getTips().size() == 1);
		fManager.setServeReadTips(true);
		assertTrue(fProvider1.getTips().size() == 2);
	}

	@Test
	public void testIsServeUnread() {
		assertTrue(fManager.mustServeReadTips() == false);
		fManager.setServeReadTips(true);
		assertTrue(fManager.mustServeReadTips());
	}
}