/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ui.tests.forms.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.forms.widgets.FormFonts;

public class FormFontsTests extends TestCase {
	public void testSingleton() {
		Display display = Display.getCurrent();
		FormFonts instance = FormFonts.getInstance();
		// ensure the singleton is returning the same instance
		Assert.assertTrue("getInstance() returned a different FormFonts instance", instance.equals(FormFonts.getInstance()));
		Font boldSystemFont = instance.getBoldFont(display, display.getSystemFont());
		instance.markFinished(boldSystemFont, display);
		// ensure the singleton is returning the same instance after creating and disposing one gradient
		Assert.assertTrue("getInstance() returned a different FormFonts instance after creation and disposal of one bold font", instance.equals(FormFonts.getInstance()));
	}
	
	public void testDisposeOne() {
		Display display = Display.getCurrent();
		Font boldSystemFont = FormFonts.getInstance().getBoldFont(display, display.getSystemFont());
		FormFonts.getInstance().markFinished(boldSystemFont, display);
		// ensure that getting a single gradient and marking it as finished disposed it
		Assert.assertTrue("markFinished(...) did not dispose a font after a single getBoldFont()", boldSystemFont.isDisposed());
	}
	
	public void testMultipleInstances() {
		Display display = Display.getCurrent();
		Font boldSystemFont = FormFonts.getInstance().getBoldFont(display, display.getSystemFont());
		int count;
		// ensure that the same image is returned for many calls with the same parameter
		for (count = 1; count < 20; count ++)
			Assert.assertEquals("getBoldFont(...) returned a different font for the same params on iteration "+count,
					boldSystemFont, FormFonts.getInstance().getBoldFont(display, display.getSystemFont()));
		for ( ;count > 0; count--) {
			FormFonts.getInstance().markFinished(boldSystemFont, display);
			if (count != 1)
				// ensure that the gradient is not disposed early
				Assert.assertFalse("markFinished(...) disposed a shared font early on iteration "+count,boldSystemFont.isDisposed());
			else
				// ensure that the gradient is disposed on the last markFinished
				Assert.assertTrue("markFinished(...) did not dispose a shared font on the last call",boldSystemFont.isDisposed());
		}
	}
	
	public void testMultipleFonts() {
		Display display = Display.getCurrent();
		Font veranda = new Font(display, "Veranda",12,SWT.NORMAL);
		Font arial = new Font(display, "Arial",12,SWT.NORMAL);
		Font boldVeranda = FormFonts.getInstance().getBoldFont(display, veranda);
		Font boldArial = FormFonts.getInstance().getBoldFont(display, arial);
		assertFalse(boldVeranda.equals(boldArial));
		FormFonts.getInstance().markFinished(boldVeranda, display);
		assertTrue(boldVeranda.isDisposed());
		assertFalse(boldArial.isDisposed());
		FormFonts.getInstance().markFinished(boldArial, display);
		assertTrue(boldArial.isDisposed());
		veranda.dispose();
		arial.dispose();
	}
	
	public void testDisposeUnknown() {
		Display display = Display.getCurrent();
		Font system = new Font(display, display.getSystemFont().getFontData());
		FormFonts.getInstance().markFinished(system, display);
		Assert.assertTrue("markFinished(...) did not dispose of an unknown font", system.isDisposed());
	}
}
