package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import java.util.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.custom.*;


public class CustomWorkbook implements IFormWorkbook {
	private Hashtable pages;
	private IFormPage sourcePage;
	private IFormPage lastFormPage;
	private boolean firstPageSelected=true;
	private CTabFolder tabFolder;
	private Vector listeners=new Vector();
	private IFormPage currentPage;

public CustomWorkbook() {
	pages = new Hashtable();
}
public void addFormSelectionListener(IFormSelectionListener listener) {
	listeners.addElement(listener);
}
public void addPage(IFormPage page) {
	CTabItem item = new CTabItem(tabFolder, SWT.NULL);
	item.setText(page.getLabel());
	item.setToolTipText(page.getTitle());
	item.setData(page);
	pages.put(page, item);
	if (page.isSource()) sourcePage = page;
	
	if (firstPageSelected && currentPage == null)
		selectPage(page);
}
public void createControl(Composite parent) {
	tabFolder = new CTabFolder(parent, SWT.BOTTOM);
	tabFolder.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			CTabItem item = (CTabItem) e.item;
			IFormPage page = (IFormPage) item.getData();
			if (page != null)
				selectPage(page);
		}
	});
	// listener to resize visible components
	tabFolder.addListener(SWT.Resize, new Listener() {
		public void handleEvent(Event e) {
			if (currentPage != null)
				setControlSize(currentPage.getControl());
		}
	});
}
private void fireSelectionChanged(IFormPage page) {
	for (Iterator iter = listeners.iterator(); iter.hasNext();) {
		IFormSelectionListener listener = (IFormSelectionListener) iter.next();
		listener.formSelected(page);
	}
}
public Control getControl() {
	return tabFolder;
}
public IFormPage getCurrentPage() {
	return currentPage;
}
public boolean isFirstPageSelected() {
	return firstPageSelected;
}
public void removeFormSelectionListener(IFormSelectionListener listener) {
	listeners.removeElement(listener);
}
public void removePage(IFormPage page) {
	CTabItem item = (CTabItem) pages.get(page);
	if (item != null)
		item.dispose();
}
private void reselectPage(final IFormPage page) {
	tabFolder.getDisplay().asyncExec(new Runnable() {
		public void run() {
		selectPage(page);
		}
	});
}
public void selectPage(final IFormPage page) {
	final IFormPage oldPage = currentPage;
	currentPage = page;

	// It may take a while
	BusyIndicator.showWhile(tabFolder.getDisplay(), new Runnable() {
		public void run() {
			switchPages(oldPage, page);
		}
	});
}
private void setControlSize(Control control) {
	Rectangle bounds = tabFolder.getBounds();
	Rectangle offset = tabFolder.getClientArea();
	bounds.x += offset.x;
	bounds.y += offset.y;
	bounds.width = offset.width;
	bounds.height = offset.height;
	control.setBounds(bounds);
	control.moveAbove(tabFolder);
}
private void setControlVisible(Control control) {
	if (control == null)
		return;
	setControlSize(control);
	control.setVisible(true);
}
public void setFirstPageSelected(boolean newFirstPageSelected) {
	firstPageSelected = newFirstPageSelected;
}
private void switchPages(IFormPage oldPage, IFormPage newPage) {
	if (oldPage != null && oldPage!=newPage) {
		boolean okToSwitch = oldPage.becomesInvisible(newPage);
		if (!okToSwitch) {
			// We must try to go back to the source page
			reselectPage(oldPage);
			return;
		}
	}
	if (newPage.getControl() == null)
		newPage.createControl(tabFolder);
	tabFolder.setSelection((CTabItem) pages.get(newPage));
	if (oldPage != null && oldPage != newPage) {
		Control oldControl = oldPage.getControl();
		if (oldControl!=null) oldControl.setVisible(false);
	}
	Control newControl = newPage.getControl();
	newPage.becomesVisible(oldPage);
	setControlVisible(newControl);
	fireSelectionChanged(newPage);
}
}
