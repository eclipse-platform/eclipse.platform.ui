/*******************************************************************************
 * Copyright (c) 2014, 2020 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445723, 445600
 *     Christoph Läubrich - Bug 570216
 ******************************************************************************/

package org.eclipse.ui.internal.help;

import org.eclipse.e4.ui.services.help.EHelpService;
import org.eclipse.help.HelpSystem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class HelpServiceImpl implements EHelpService {

	@Override
	public void displayHelp(String contextId) {
		if (contextId != null) {
			WorkbenchHelpSystem.getInstance().displayHelp(contextId);
		}
	}

	/**
	 * IDE implementation delegates to {@link WorkbenchHelpSystem}
	 */
	@Override
	public void setHelp(Object helpTarget, String helpContextId) {
		if (helpTarget instanceof Control) {
			WorkbenchHelpSystem.getInstance().setHelp((Control) helpTarget, helpContextId);
		} else if (helpTarget instanceof IAction) {
			WorkbenchHelpSystem.getInstance().setHelp((IAction) helpTarget, helpContextId);
		} else if (helpTarget instanceof Menu) {
			WorkbenchHelpSystem.getInstance().setHelp((Menu) helpTarget, helpContextId);
		} else if (helpTarget instanceof MenuItem) {
			WorkbenchHelpSystem.getInstance().setHelp((MenuItem) helpTarget, helpContextId);
		}

	}

	@Override
	public boolean hasHelp(String helpContextId) {
		return HelpSystem.getContext(helpContextId) != null;
	}
}
