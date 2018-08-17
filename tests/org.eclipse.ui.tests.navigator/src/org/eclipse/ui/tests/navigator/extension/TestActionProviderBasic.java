/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class TestActionProviderBasic extends CommonActionProvider {

	private IAction action;

	public TestActionProviderBasic() {

	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		super.init(site);

		action = new Action() {

		};
		action.setId(site.getExtensionId());
		action.setText(site.getExtensionId());
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.add(action);
	}

}
