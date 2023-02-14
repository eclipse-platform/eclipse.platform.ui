/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.AbstractIconDialogWithScopeAndFilter;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;
import org.eclipse.swt.widgets.Shell;

final class FindIconDialog extends AbstractIconDialogWithScopeAndFilter {
	FindIconDialog(Shell parentShell, IEclipseContext context) {
		super(parentShell, context);
	}

	@Override
	protected String getShellTitle() {
		return Messages.FindIconDialog_findIcon;
	}

	@Override
	protected String getDialogTitle() {
		return Messages.FindIconDialog_findIcon;
	}

	@Override
	protected String getDialogMessage() {
		return Messages.FindIconDialog_searchByFilename;
	}
}