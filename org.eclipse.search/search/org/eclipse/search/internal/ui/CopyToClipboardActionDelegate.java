/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class CopyToClipboardActionDelegate implements IViewActionDelegate {
	
	public CopyToClipboardActionDelegate() {
	}

	/*
	 * Implements method from IActionDelegate
	 */	
	public void run(IAction action) {
		Shell shell= SearchPlugin.getActiveWorkbenchShell();
		if (shell == null)
			return;
		
		ILabelProvider labelProvider= SearchUI.getSearchResultView().getLabelProvider();
		String lineDelim= System.getProperty("line.separator");
		StringBuffer buf= new StringBuffer();
		Iterator iter= getSelection();
		while (iter.hasNext()) {
			if (buf.length() > 0) {
				buf.append(lineDelim);
			}
			buf.append(labelProvider.getText(iter.next()));
		}
		
		if (buf.length() > 0)
			copyToClipbard(shell.getDisplay(), buf.toString());
	}

	/*
	 * Implements method from IViewActionDelegate
	 */
	public void init(IViewPart view) {
	}

	/*
	 * Implements method from IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	private Iterator getSelection() {
		if (SearchPlugin.getActivePage() != null) {
			ISelection s= SearchPlugin.getActivePage().getSelection();
			if (s instanceof IStructuredSelection)
				return ((IStructuredSelection)s).iterator();
		}
		return Collections.EMPTY_LIST.iterator();
	}

	private void copyToClipbard(Display display, String str) {
		Clipboard clipboard = new Clipboard(display);
		clipboard.setContents(new String[] { str },	new Transfer[] { TextTransfer.getInstance()});			
	}
}