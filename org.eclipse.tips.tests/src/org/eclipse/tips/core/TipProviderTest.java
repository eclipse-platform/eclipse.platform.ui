/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;

public class TipProviderTest {

	private TestTipManager fManager;
	private TestTipProvider fProvider;

	@Before
	public void testTipProvider() {
		fManager = new TestTipManager();
		fProvider = (TestTipProvider) new TestTipProvider().setManager(fManager);
	}

	@Test
	public void testDispose() {
		fProvider.dispose();
	}

	@Test
	public void testGetDescription() {
		assertTrue(fProvider.getDescription() != null);
	}

	@Test
	public void testGetID() {
		assertTrue(fProvider.getID() != null);
		assertTrue(fProvider.getID().equals(fProvider.getClass().getName()));
	}


	@Test
	public void testGetImage() {
		assertTrue(fProvider.getImage() != null);
	}

	@Test
	public void testGetTips() {
		assertTrue(fProvider.getTips(false).size() == 0);
		createTestDate();
		fManager.setAsRead(fProvider.getNextTip());
		assertTrue(fProvider.getTips(false).size() == 2);
		assertTrue(fProvider.getTips(false).size() == 2);
		List<Tip> tips = fProvider.getTips(true);
		assertTrue(fProvider.getTips(true).size() == 1);
		((TipManager) fProvider.getManager()).setServeReadTips(true);
		assertTrue(fProvider.getTips(false).size() == 2);
	}

	private void createTestDate() {
		fProvider.setTips(Arrays.asList(new TestTip(fProvider.getID(),"<b>bold</b>", "Tip 1"),
				new TestTip(fProvider.getID(),"<b>bold2</b>", "Tip 2")));
	}

	@Test
	public void testGetCurrentTip() {
		assertTrue(fProvider.getNextTip().equals(fProvider.getCurrentTip()));
	}

	@Test
	public void testGetCurrentTip2() {
		assertTrue(fProvider.getCurrentTip().equals(fProvider.getPreviousTip()));
	}

	@Test
	public void testGetNextTip() {
		createTestDate();
		fManager.setAsRead(fProvider.getNextTip());
		assertTrue(fProvider.getNextTip().equals(fProvider.getCurrentTip()));
		Tip nextTip = fProvider.getNextTip();
		fManager.setAsRead(nextTip);
		assertTrue(fManager.isRead(nextTip));
		Tip nextTip2 = fProvider.getNextTip();
		fManager.setAsRead(nextTip2);
		assertTrue(fManager.isRead(nextTip2));
		assertTrue(fProvider.getNextTip().getClass().getSimpleName().equals("FinalTip"));
		((TipManager) fProvider.getManager()).setServeReadTips(true);
		assertFalse(fProvider.getNextTip().getClass().getSimpleName().equals("FinalTip"));
	}

	@Test
	public void testGetPreviousTip() {
		assertTrue(fProvider.getPreviousTip().equals(fProvider.getCurrentTip()));
		assertTrue(fProvider.getPreviousTip().equals(fProvider.getCurrentTip()));
	}

	@Test
	public void testGetPreviousTip2() {
		assertTrue(!fProvider.getPreviousTip().equals(null));
		assertTrue(fProvider.getNextTip().getClass().getSimpleName().equals("FinalTip"));
	}

	@Test
	public void testGetPreviousTip3() {
		((TipManager) fProvider.getManager()).setServeReadTips(true);
		assertTrue(fProvider.getPreviousTip().equals(fProvider.getCurrentTip()));
	}

	@Test
	public void testGetPreviousTip4() {
		createTestDate();
		assertTrue(fProvider.getPreviousTip() != null);
		assertTrue(fProvider.getPreviousTip() != null);
		assertTrue(fProvider.getPreviousTip() != null);
	}

	@Test
	public void testGetTipManager() {
		assertTrue(fProvider.getManager().equals(fManager));
	}

	@Test
	public void testIsReady() {
		TestTipProvider p = (TestTipProvider) new TestTipProvider().setManager(fManager);
		assertTrue(!p.isReady());
		p.setTips(Collections.emptyList());
		assertTrue(p.isReady());
	}

	@Test
	public void testLoad() {
		TestTipProvider p = (TestTipProvider) new TestTipProvider().setManager(fManager);
		assertTrue(!p.isReady());
		p.loadNewTips(new NullProgressMonitor());
		assertTrue(p.isReady());
	}

	@Test
	public void testSetManager() {
		TestTipProvider p = new TestTipProvider();
		assertTrue(p.getManager() == null);
		p.setManager(fManager);
		assertTrue(p.getManager() != null);
	}

	@Test
	public void testSetTips() {
		TestTipProvider p = new TestTipProvider() {
			@Override
			public IStatus loadNewTips(IProgressMonitor pMonitor) {
				assertTrue(getTips(false).size() == 0);
				assertTrue(setTips(Arrays.asList(new Tip[] { new TestTip(getID(),"DDD", "XXX") })).getTips(false)
						.size() == 1);
				return Status.OK_STATUS;
			}
		};
		assertTrue(p.getTips(false).size() == 0);
		fManager.register(p);
		assertTrue(p.getTips(false).size() == 1);
	}

	@Test
	public void testAddTips() {
		TestTipProvider p = new TestTipProvider() {
			@Override
			public IStatus loadNewTips(IProgressMonitor pMonitor) {
				assertTrue(getTips(false).size() == 0);
				assertTrue(setTips(Arrays.asList(new Tip[] { new TestTip(getID(),"DDD", "XXX") })).getTips(false)
						.size() == 1);
				assertTrue(addTips(Arrays.asList(new Tip[] { new TestTip(getID(),"DDD", "XXX") })).getTips(false)
						.size() == 2);
				return Status.OK_STATUS;
			}
		};
		fManager.register(p);

	}
}
