/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.composite;

import org.eclipse.ui.cheatsheets.CheatSheetListener;
import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;

import junit.framework.TestCase;

public class TestEventing extends TestCase {
	
	private CheatSheetElement element;
	private CheatSheetManager manager;
	private int handler1Calls;
	private int handler2Calls;
	
	private class Handler1 extends CheatSheetListener {
		public void cheatSheetEvent(ICheatSheetEvent event) {
			handler1Calls++;		
		}	
	}
	
	private class Handler2 extends CheatSheetListener {
		public void cheatSheetEvent(ICheatSheetEvent event) {
			handler2Calls++;		
		}	
	}

	protected void setUp() throws Exception {
		element = new CheatSheetElement("Name");
		manager = new CheatSheetManager(element);
		handler1Calls = 0;
		handler2Calls = 0;
	}
	
	public void testNoHandler() {
		manager.fireEvent(ICheatSheetEvent.CHEATSHEET_STARTED);
	}
	
	public void testOneHandler() {
		manager.addListener(new Handler1());
		manager.fireEvent(ICheatSheetEvent.CHEATSHEET_STARTED);
		assertEquals(1, handler1Calls);
	}
	
	public void testTwoHandlers() {
		manager.addListener(new Handler1());
		manager.addListener(new Handler2());
		manager.fireEvent(ICheatSheetEvent.CHEATSHEET_STARTED);
		assertEquals(1, handler1Calls);
		assertEquals(1, handler2Calls);
	}

}
