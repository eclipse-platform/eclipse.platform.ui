/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.AbstractIconDialogWithScopeAndFilter;
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