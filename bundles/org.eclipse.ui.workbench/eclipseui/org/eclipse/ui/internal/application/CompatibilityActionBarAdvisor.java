/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * An implementation of <code>ActionBarAdvisor</code> that calls back to the 3.0
 * legacy methods on <code>WorkbenchAdvisor</code> for backwards compatibility.
 *
 * @since 3.1
 */
public class CompatibilityActionBarAdvisor extends ActionBarAdvisor {

	private WorkbenchAdvisor wbAdvisor;

	/**
	 * Creates a new compatibility action bar advisor.
	 *
	 * @param wbAdvisor  the workbench advisor
	 * @param configurer the action bar configurer
	 */
	public CompatibilityActionBarAdvisor(WorkbenchAdvisor wbAdvisor, IActionBarConfigurer configurer) {
		super(configurer);
		this.wbAdvisor = wbAdvisor;
	}

	@Override
	public void fillActionBars(int flags) {
		IActionBarConfigurer abc = getActionBarConfigurer();
		wbAdvisor.fillActionBars(abc.getWindowConfigurer().getWindow(), abc, flags);
	}

	@Override
	public boolean isApplicationMenu(String menuId) {
		IActionBarConfigurer abc = getActionBarConfigurer();
		return wbAdvisor.isApplicationMenu(abc.getWindowConfigurer(), menuId);
	}
}
