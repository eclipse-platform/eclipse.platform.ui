/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;


/**
 * @since 3.1
 */
public class ViewWidgetFactory extends TestWidgetFactory {

	private final String viewId;
	private Control ctrl;
	private IWorkbenchWindow window;

	public ViewWidgetFactory(String viewId) {
		this.viewId = viewId;
		assertNotNull(viewId);
	}

	@Override
	public Point getMaxSize() {
		return new Point(1024, 768);
	}

	public static Composite getControl(IViewPart part) {
		ViewSite site = (ViewSite)part.getSite();
		MPart modelPart = site.getModel();
		return (Composite) modelPart.getWidget();
	}

	@Override
	public void init() throws WorkbenchException {
		// open the view in a new window
		window = PlatformUI.getWorkbench().openWorkbenchWindow(EmptyPerspective.PERSP_ID, getPageInput());
		IWorkbenchPage page = window.getActivePage();
		assertNotNull(page);

		IViewPart part = page.showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);

		BasicPerformanceTest.waitForBackgroundJobs();

		ctrl = getControl(part);

		Point size = getMaxSize();
		ctrl.setBounds(0,0,size.x, size.y);
		window.getShell().setSize(size);
	}

	@Override
	public String getName() {
		return "View " + viewId;
	}

	@Override
	public Composite getControl() {
		return (Composite)ctrl;
	}

	@Override
	public void done() {
		window.close();
	}

}
