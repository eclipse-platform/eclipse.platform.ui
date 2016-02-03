/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer - initial API and implementation
 *******************************************************************************/

package org.eclipse.text.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.CopyOnWriteTextStore;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.ITextStore;

/**
 * Test suite for the CopyOnWriteTextStore.
 *
 * @since 3.2
 */
public class CopyOnWriteTextStoreTest {

	private static class COWTextStore extends CopyOnWriteTextStore {
		COWTextStore() {
			super(new GapTextStore());
		}
		ITextStore getStore() {
			return fTextStore;
		}
		String get() {
			return get(0, getLength());
		}
	}

	private static final String INITIAL_CONTENT= "xxxxx";

	private COWTextStore fText;

	@Before
	public void setUp() {

		fText= new COWTextStore();
		fText.set(INITIAL_CONTENT);
	}

	@After
	public void tearDown () {
		fText= null;
	}
	
	@Test
	public void testInitialContent() {

		assertEquals(INITIAL_CONTENT, fText.get());
		// check that underlying text store is not modifiable
		boolean failed= false;
		try {
			fText.getStore().replace(0,0,null);
		} catch (UnsupportedOperationException uoe) {
			failed= true;
		}
		assertTrue(failed);

	}
	
	@Test
	public void testFirstModification() {

		checkReplace(1, 1, "y");
		assertEquals(GapTextStore.class, fText.getStore().getClass());

	}
	
	@Test
	public void testSet() {

		fText.replace(1, 1, "y");
		fText.set(INITIAL_CONTENT);
		assertEquals(INITIAL_CONTENT, fText.get());
		// check that underlying text store is not modifiable
		boolean failed= false;
		try {
			fText.getStore().replace(0,0,null);
		} catch (UnsupportedOperationException uoe) {
			failed= true;
		}
		assertTrue(failed);
	}
	
	@Test
	public void testInsert1() {

		for (int i= 1; i < 5; i++) {
			checkReplace(2 * i - 1, 0, "y");
		}

	}
	
	@Test
	public void testInsert2() {

		for (int i= 1; i < 5; i++) {
			checkReplace(2 * (i - 1), 0, "y");
		}

	}
	
	@Test
	public void testDelete1() {

		for (int i= 1; i < 5; i++) {
			checkReplace(0, 1, "");
		}

	}
	
	@Test
	public void testDelete2() {

		for (int i= 1; i < 5; i++) {
			checkReplace(fText.getLength()-1, 1, "");
		}

	}
	
	@Test
	public void testAppend() {

		for (int i= 1; i < 5; i++) {
			checkReplace(fText.getLength(), 0, "y");
		}

	}

	private void checkReplace(int offset, int length, String text) {

		StringBuffer buf= new StringBuffer(fText.get());
		buf.replace(offset, offset + length, text);
		fText.replace(offset, length, text);
		assertEquals(buf.toString(), fText.get());

	}

}
