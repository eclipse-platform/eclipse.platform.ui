/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class MemoryViewPrefAction implements IViewActionDelegate {

	public void init(IViewPart view) {

	}

	public void run(IAction action) {
		
		PreferenceManager prefManager = new PreferenceManager();
		
		ResetMemoryBlockPreferencePage page = new ResetMemoryBlockPreferencePage();
		IPreferenceNode node = new PreferenceNode("org.eclipse.debug.ui.memory.resetMemoryBlock", page);  //$NON-NLS-1$
		prefManager.addToRoot(node);
		
		SetPaddedStringPreferencePage page2 = new SetPaddedStringPreferencePage();
		IPreferenceNode node2 = new PreferenceNode("org.eclipse.debug.ui.memory.setPaddedString", page2);  //$NON-NLS-1$
		prefManager.addToRoot(node2);

		CodePagesPreferencePage page3 = new CodePagesPreferencePage();
		IPreferenceNode node3 = new PreferenceNode("org.eclipse.debug.ui.memory.codePages", page3);  //$NON-NLS-1$
		prefManager.addToRoot(node3);
		
		final PreferenceDialog dialog = new PreferenceDialog(DebugUIPlugin.getShell(), prefManager);

		BusyIndicator.showWhile(DebugUIPlugin.getStandardDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				dialog.open();
			}
		});		

	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
