/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.util;


import java.util.*;

import junit.framework.TestCase;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.*;


/**
 * <code>UITestCase</code> is a useful super class for most
 * UI tests cases.  It contains methods to create new windows 
 * and pages.  It will also automatically close the test 
 * windows when the tearDown method is called.
 */
public abstract class UITestCase extends TestCase 
{
	protected IWorkbench fWorkbench;
	private List testWindows;


	public UITestCase(String testName) {
		super(testName);
//		ErrorDialog.NO_UI = true;
		fWorkbench = PlatformUI.getWorkbench();
		testWindows = new ArrayList(3);
	}


	/**
	 * Tear down.  May be overridden.
	 */
	protected void tearDown() throws Exception {
		closeAllTestWindows();
	}


	/** 
	 * Open a test window with the empty perspective.
	 */
	public IWorkbenchWindow openTestWindow(String id) {
		try {
			IWorkbenchWindow win =
				fWorkbench.openWorkbenchWindow(
					id,
					ResourcesPlugin.getWorkspace());
			testWindows.add(win);
			return win;
		} catch (WorkbenchException e) {
			fail();
			return null;
		}
	}
	
	/**
	 * Added for performance tests.
	 * 
	 * @return
	 */
	public IWorkbenchWindow openTestWindow() {
		return openTestWindow(EmptyPerspective.PERSP_ID);
	}


	/**
	 * Close all test windows.
	 */
	public void closeAllTestWindows() {
		Iterator iter = testWindows.iterator();
		IWorkbenchWindow win;
		while (iter.hasNext()) {
			win = (IWorkbenchWindow) iter.next();
			win.close();
		}
		testWindows.clear();
	}


	/**
	 * Open a test page with the empty perspective in a window.
	 */
	public IWorkbenchPage openTestPage(IWorkbenchWindow win)
		{
		IWorkbenchPage[] pages = openTestPage(win, 1);
		if( pages != null )
			return pages[0];
		else
			return null;
	}


	/**
	 * Open "n" test pages with the empty perspectie in a window.
	 */
	public IWorkbenchPage[] openTestPage(IWorkbenchWindow win, int pageTotal) {		
		try{
			IWorkbenchPage[] pages = new IWorkbenchPage[pageTotal];
			IWorkspace work = ResourcesPlugin.getWorkspace();


			for (int i = 0; i < pageTotal; i++) 			
				pages[i] = win.openPage(EmptyPerspective.PERSP_ID, work);
			return pages;
		}
		catch( WorkbenchException e )
		{
			fail();
			return null;
		}		
	}


	/**
	 * Close all pages within a window.
	 */
	public void closeAllPages(IWorkbenchWindow window) {
		IWorkbenchPage[] pages = window.getPages();
		for (int i = 0; i < pages.length; i++)
			pages[i].close();
	}
}