/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class MockWorkbenchPart extends MockPart implements IWorkbenchPart {

	private IWorkbenchPartSite site;

	private String title;

	public MockWorkbenchPart() {
		super();
	}

	public void setSite(IWorkbenchPartSite site) {
		this.site = site;
		site.setSelectionProvider(selectionProvider);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		parent.setLayout(new GridLayout());

		Label label = new Label(parent, SWT.NONE);
		label.setText(title);
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return site;
	}

	/**
	 * @see IWorkbenchPart#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @see IWorkbenchPart#getTitleToolTip()
	 */
	@Override
	public String getTitleToolTip() {
		return title;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		// TODO Auto-generated method stub
		super.setInitializationData(config, propertyName, data);
		title = config.getAttribute("name");
	}

	protected void setSiteInitialized() {
		setSiteInitialized(getSite().getKeyBindingService() != null & getSite().getPage() != null
				& getSite().getSelectionProvider() != null & getSite().getWorkbenchWindow() != null
				& testActionBars(getActionBars()));
	}

	private boolean testActionBars(IActionBars bars) {
		return bars != null && bars.getMenuManager() != null && bars.getToolBarManager() != null
				&& bars.getStatusLineManager() != null;

	}

	protected abstract IActionBars getActionBars();
}
