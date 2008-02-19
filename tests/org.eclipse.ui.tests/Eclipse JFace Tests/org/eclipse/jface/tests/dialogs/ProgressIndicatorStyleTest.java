/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.dialogs;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Test case to assert proper styles have been set for ProgressIndicator.
 * 
 * @since 3.4
 * 
 */
public class ProgressIndicatorStyleTest extends TestCase {

	protected ProgressIndicator progress;
	protected ProgressBar deter, indeter;
	protected int style;

	public ProgressIndicatorStyleTest(String name) {
		super(name);

	}

	/**
	 * Test the indicator styles.
	 */
	public void testProgressIndicator() {
		style = SWT.SMOOTH;
		verifyIndicator();

		style = SWT.VERTICAL;
		verifyIndicator();		

		style = SWT.HORIZONTAL;
		verifyIndicator();
		
	}

	/**
	 * Verify the indicator is working by opening it and doing something.
	 */
	private void verifyIndicator() {
		Shell shell = new Shell();
		progress = new ProgressIndicator(shell, style);
		progress.setSize(175,175);
		shell.setSize(200,200);
		shell.open();
		shell.forceActive();
		progress.beginTask(100);
		progress.worked(50);
		loader("determinateProgressBar", deter);
		loader("indeterminateProgressBar", indeter);
		long timeout = System.currentTimeMillis() + 1000;
		while(System.currentTimeMillis() < timeout){
			shell.getDisplay().readAndDispatch();
		}
		shell.close();
		progress.dispose();
	}

	/**
	 * Loads, using reflection, the internal ProgressBars from inside the
	 * ProgressIndicator and tests to assert the proper style has been set on
	 * them.
	 */
	private void loader(String field, ProgressBar p) {
		Class c = progress.getClass();
		try {
			Field f = c.getDeclaredField(field);
			f.setAccessible(true);
			p = (ProgressBar) f.get(progress);

			assertEquals(style, p.getStyle() & style);
		} catch (NoSuchFieldException e) {
			System.err.println("No such field");
		} catch (IllegalAccessException e) {
			System.err.println("Illegal Access");
		}

	}
}
