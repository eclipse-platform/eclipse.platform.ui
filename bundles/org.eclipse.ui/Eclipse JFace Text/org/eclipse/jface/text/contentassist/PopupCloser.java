package org.eclipse.jface.text.contentassist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	public void widgetDefaultSelected(SelectionEvent e) {
		fScrollbarClicked= true;
	}
	// SelectionListener
	public void widgetSelected(SelectionEvent e) {
		fScrollbarClicked= true;
	}
}
