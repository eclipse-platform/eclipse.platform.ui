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

/**
 * @since 3.4
 * 
 */
public class DecoratorCacheTest extends AbstractNavigatorTest {

	protected DecoratingLabelProvider dlp;

	public DecoratorCacheTest(String name) {
		super(name);
	}

	protected StructuredViewer createViewer(Composite parent) {
		dlp = new DecoratingLabelProvider(new LabelProvider(), PlatformUI
				.getWorkbench().getDecoratorManager());

		TreeViewer v = new TreeViewer(parent);
		v.setContentProvider(new TestTreeContentProvider());
		v.setLabelProvider(dlp);
		return v;

	}

	public void testDecoratorCacheIsDisposed() {

		Display fDisplay = Display.getCurrent();
		if (fDisplay == null) {
			fDisplay = new Display();
		}
		Shell fShell = new Shell(fDisplay, SWT.SHELL_TRIM);
		fShell.setSize(500, 500);
		fShell.setLayout(new FillLayout());
		StructuredViewer fViewer = createViewer(fShell);
		fViewer.setUseHashlookup(true);

		try {
			createTestFile();
		} catch (CoreException e) {
			fail(e.getLocalizedMessage(), e);
		}
		fViewer.setInput(testFile);
		fShell.open();

		dlp.dispose();
		assertTrue("The resource manager exists", dlp.getDecorationContext()
				.getProperty("RESOURCE_MANAGER") == null);
		fShell.close();
	}

}
