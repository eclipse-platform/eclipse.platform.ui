/*******************************************************************************
 * Copyright (c) 2004, 2026 IBM Corporation and others.
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
package org.eclipse.ui.internal.application;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * An implementation of <code>WorkbenchWindowAdvisor</code> that calls back to
 * the 3.0 legacy methods on <code>WorkbenchAdvisor</code> for backwards
 * compatibility.
 *
 * @since 3.1
 */
public class CompatibilityWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private final WorkbenchAdvisor wbAdvisor;

	/**
	 * Creates a new compatibility workbench window advisor.
	 *
	 * @param wbAdvisor        the workbench advisor
	 * @param windowConfigurer the window configurer
	 */
	public CompatibilityWorkbenchWindowAdvisor(WorkbenchAdvisor wbAdvisor,
			IWorkbenchWindowConfigurer windowConfigurer) {
		super(windowConfigurer);
		this.wbAdvisor = wbAdvisor;
	}

	@Override
	@Deprecated
	public void preWindowOpen() {
		wbAdvisor.preWindowOpen(getWindowConfigurer());
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new CompatibilityActionBarAdvisor(wbAdvisor, configurer);
	}

	@Override
	@Deprecated
	public void postWindowRestore() throws WorkbenchException {
		wbAdvisor.postWindowRestore(getWindowConfigurer());
	}

	@Override
	@Deprecated
	public void openIntro() {
		wbAdvisor.openIntro(getWindowConfigurer());
	}

	@Override
	@Deprecated
	public void postWindowCreate() {
		wbAdvisor.postWindowCreate(getWindowConfigurer());
	}

	@Override
	@Deprecated
	public void postWindowOpen() {
		wbAdvisor.postWindowOpen(getWindowConfigurer());
	}

	@Override
	@Deprecated
	public boolean preWindowShellClose() {
		return wbAdvisor.preWindowShellClose(getWindowConfigurer());
	}

	@Override
	@Deprecated
	public void postWindowClose() {
		wbAdvisor.postWindowClose(getWindowConfigurer());
	}

	@Deprecated
	public boolean isApplicationMenu(String menuId) {
		return wbAdvisor.isApplicationMenu(getWindowConfigurer(), menuId);
	}

	public IAdaptable getDefaultPageInput() {
		return wbAdvisor.getDefaultPageInput();
	}

	@Override
	@Deprecated
	public void createWindowContents(Shell shell) {
		wbAdvisor.createWindowContents(getWindowConfigurer(), shell);
	}

}
