/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.jface.internal.text.link.contentassist;


import static org.eclipse.jface.util.Util.isValid;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;


/**
 * A generic closer class used to monitor various
 * interface events in order to determine whether
 * a content assistant should be terminated and all
 * associated windows be closed.
 */
class PopupCloser2 extends ShellAdapter implements FocusListener, SelectionListener {

	/** The content assistant to be monitored */
	private ContentAssistant2 fContentAssistant;
	/** The table of a selector popup opened by the content assistant */
	private Table fTable;
	/** The scrollbar of the table for the selector popup */
	private ScrollBar fScrollbar;
	/** Indicates whether the scrollbar thumb has been grabbed. */
	private boolean fScrollbarClicked= false;
	/** The shell on which some listeners are registered. */
	private Shell fShell;


	/**
	 * Installs this closer on the given table opened by the given content assistant.
	 *
	 * @param contentAssistant the content assistant
	 * @param table the table to be tracked
	 */
	public void install(ContentAssistant2 contentAssistant, Table table) {
		fContentAssistant= contentAssistant;
		fTable= table;
		if (isValid(fTable)) {
			Shell shell= fTable.getShell();
			if (isValid(shell)) {
				fShell= shell;
				fShell.addShellListener(this);
			}
			fTable.addFocusListener(this);
			fScrollbar= fTable.getVerticalBar();
			if (fScrollbar != null) {
				fScrollbar.addSelectionListener(this);
			}
		}
	}

	/**
	 * Uninstalls this closer if previously installed.
	 */
	public void uninstall() {
		fContentAssistant= null;
		if (isValid(fShell)) {
			fShell.removeShellListener(this);
		}
		fShell= null;
		if (isValid(fScrollbar)) {
			fScrollbar.removeSelectionListener(this);
		}
		if (isValid(fTable)) {
			fTable.removeFocusListener(this);
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		fScrollbarClicked= true;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		fScrollbarClicked= true;
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(final FocusEvent e) {
		fScrollbarClicked= false;
		Display d= fTable.getDisplay();
		d.asyncExec(() -> {
			if (isValid(fTable) && !fTable.isFocusControl() && !fScrollbarClicked && fContentAssistant != null) {
				fContentAssistant.popupFocusLost(e);
			}
		});
	}

	@Override
	public void shellDeactivated(ShellEvent e) {
		if (fContentAssistant != null) {
			fContentAssistant.hide();
		}
	}


	@Override
	public void shellClosed(ShellEvent e) {
		if (fContentAssistant != null) {
			fContentAssistant.hide();
		}
	}
}
