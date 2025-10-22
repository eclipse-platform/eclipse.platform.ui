/*******************************************************************************
 * Copyright (c) 2017 Peter Severin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Peter Severin <peter@wireframesketcher.com> - Bug 518142
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormTest {
	private static Display display;

	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			// this is to run without eclipse
			display = new Display();
		}
	}

	private Shell shell;

	@BeforeEach
	public void setUp() throws Exception {
		shell = new Shell(display);
	}

	@AfterEach
	public void tearDown() throws Exception {
		shell.dispose();
	}

	@Test
	public void testFormLayoutCanIgnoreBody() {
		Form form = new Form(shell, SWT.NULL);
		Label label = new Label(form.getBody(), SWT.NONE);
		label.setText("A very long text to force a large minimum widgth");
		SizeCache headCache = new SizeCache();
		SizeCache bodyCache = new SizeCache();
		headCache.setControl(form.getHead());
		bodyCache.setControl(form.getBody());
		Assertions.assertEquals(Math.max(headCache.computeMinimumWidth(), bodyCache.computeMinimumWidth()),
				((ILayoutExtension) form.getLayout()).computeMinimumWidth(form, true));
		form.setData(FormUtil.IGNORE_BODY, Boolean.TRUE);
		Assertions.assertEquals(Math.max(headCache.computeMinimumWidth(), 0),
				((ILayoutExtension) form.getLayout()).computeMinimumWidth(form, true));
	}
}
