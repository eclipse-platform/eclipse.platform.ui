/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willian Mitsuda <wmitsuda@gmail.com> - initial implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.actions.ActionFactory;

import com.ibm.icu.util.StringTokenizer;

/**
 * Try to paste a CVS connection string from clipboard as a repository location
 */
public class PasteConnectionStringAction extends ActionDelegate implements
		IViewActionDelegate {

	private IAction action;

	public void run(IAction action) {
		Clipboard clipboard = new Clipboard(PlatformUI.getWorkbench()
				.getDisplay());
		try {
			Object contents = clipboard.getContents(TextTransfer.getInstance());
			if (contents != null && contents instanceof String) {
				StringTokenizer st = new StringTokenizer((String) contents,
						System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
				while (st.hasMoreTokens()) {
					String connectionString = st.nextToken().trim();
					CVSRepositoryLocation location = CVSRepositoryLocation
							.fromString(connectionString);
					if (location != null) {
						KnownRepositories.getInstance().addRepository(location,
								true);
					}
				}
			}
		} catch (Exception e) {
			// Fail silently
		} finally {
			clipboard.dispose();
		}
	}

	public void init(IAction action) {
		super.init(action);
		this.action = action;
	}

	public void init(IViewPart view) {
		IActionBars actionBars = view.getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), action);
		actionBars.updateActionBars();
	}

}
