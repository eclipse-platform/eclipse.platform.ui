package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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