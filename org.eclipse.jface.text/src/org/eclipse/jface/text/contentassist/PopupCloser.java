/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;


import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;


/**
 * A generic closer class used to monitor various
 * interface events in order to determine whether
 * a content assist should be terminated and all
 * associated windows be closed.
 */
class PopupCloser implements FocusListener, SelectionListener {
	
	private ContentAssistant fContentAssistant;
	private Table fTable;
	private ScrollBar fScrollbar;
	private boolean fScrollbarClicked= false;
	
	public void install(ContentAssistant contentAssistant, Table table) {
		fContentAssistant= contentAssistant;
		fTable= table;
		if (Helper.okToUse(fTable)) {
			fTable.addFocusListener(this);
			fScrollbar= fTable.getVerticalBar();
			if (fScrollbar != null)
				fScrollbar.addSelectionListener(this);
		}
	}
	
	public void uninstall() {
		if (Helper.okToUse(fTable)) {
			fTable.removeFocusListener(this);
			if (fScrollbar != null)
				fScrollbar.removeSelectionListener(this);
		}
	}
	
	// SelectionListener
	public void widgetSelected(SelectionEvent e) {
		fScrollbarClicked= true;
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
		fScrollbarClicked= true;
	}
	
	// FocusListener
	public void focusGained(FocusEvent e) {
	}
	
	public void focusLost(final FocusEvent e) {
		fScrollbarClicked= false;
		Display d= fTable.getDisplay();
		d.asyncExec(new Runnable() {
			public void run() {
				if (Helper.okToUse(fTable) && !fTable.isFocusControl() && !fScrollbarClicked)
					fContentAssistant.popupFocusLost(e);
			}
		});
	}
}
