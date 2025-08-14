/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.performance.layout;

import static org.eclipse.ui.tests.harness.util.UITestUtil.getPageInput;
import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;


/**
 * @since 3.1
 */
public class PerspectiveWidgetFactory extends TestWidgetFactory {

	private final String perspectiveId;
	private IWorkbenchWindow window;

	public PerspectiveWidgetFactory(String initialPerspective) {
		perspectiveId = initialPerspective;
	}

	@Override
	public Point getMaxSize() {
		return new Point(1024, 768);
	}

	@Override
	public void init() throws WorkbenchException {
		// open the perspective in a new window
		window = PlatformUI.getWorkbench().openWorkbenchWindow(perspectiveId, getPageInput());
		IWorkbenchPage page = window.getActivePage();
		assertNotNull(page);
	}

	@Override
	public String getName() {
		return "Perspective " + perspectiveId;
	}

	@Override
	public Composite getControl() {
		return window.getShell();
	}

	@Override
	public void done() {
		window.close();
	}
}
