/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.decorators;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.4
 */
@RunWith(JUnit4.class)
public class DecoratorCacheTest extends AbstractNavigatorTest {

	protected DecoratingLabelProvider dlp;

	public DecoratorCacheTest() {
		super(DecoratorCacheTest.class.getSimpleName());
	}

	protected StructuredViewer createViewer(Composite parent) {
		dlp = new DecoratingLabelProvider(new LabelProvider(), PlatformUI
				.getWorkbench().getDecoratorManager());

		TreeViewer v = new TreeViewer(parent);
		v.setContentProvider(new TestTreeContentProvider());
		v.setLabelProvider(dlp);
		return v;

	}

	@Test
	public void testDecoratorCacheIsDisposed() throws CoreException {

		Display fDisplay = Display.getCurrent();
		if (fDisplay == null) {
			fDisplay = new Display();
		}
		Shell fShell = new Shell(fDisplay, SWT.SHELL_TRIM);
		fShell.setSize(500, 500);
		fShell.setLayout(new FillLayout());
		StructuredViewer fViewer = createViewer(fShell);
		fViewer.setUseHashlookup(true);

		createTestFile();
		fViewer.setInput(testFile);
		fShell.open();

		dlp.dispose();
		assertTrue("The resource manager exists", dlp.getDecorationContext()
				.getProperty("RESOURCE_MANAGER") == null);
		fShell.close();
	}

}
