/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class CopyToClipboardAction extends Action {

	private SearchResultViewer fViewer;
	
	public CopyToClipboardAction(SearchResultViewer viewer) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
		setText(SearchMessages.getString("CopyToClipboardAction.label")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("CopyToClipboardAction.tooltip")); //$NON-NLS-1$
	}

	/*
	 * Implements method from IAction
	 */	
	public void run() {
		Shell shell= SearchPlugin.getActiveWorkbenchShell();
		if (shell == null)
			return;

		SearchResultLabelProvider labelProvider= (SearchResultLabelProvider)fViewer.getLabelProvider();
		String lineDelim= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buf= new StringBuffer();
		Iterator iter= getSelection();
		while (iter.hasNext()) {
			if (buf.length() > 0) {
				buf.append(lineDelim);
			}
			buf.append(labelProvider.getText(iter.next()));
		}
		
		if (buf.length() > 0) {
			Clipboard clipboard= new Clipboard(shell.getDisplay());
			try {
				copyToClipbard(clipboard, buf.toString(), shell);
			} finally {
				clipboard.dispose();
			}
		}
	}

	private Iterator getSelection() {
		ISelection s= fViewer.getSelection();
		if (s instanceof IStructuredSelection)
			return ((IStructuredSelection)s).iterator();
		return Collections.EMPTY_LIST.iterator();
	}

	private void copyToClipbard(Clipboard clipboard, String str, Shell shell) {
		try {
			clipboard.setContents(new String[] { str },	new Transfer[] { TextTransfer.getInstance() });			
		} catch (SWTError ex) {
			if (ex.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw ex;
			String title= SearchMessages.getString("CopyToClipboardAction.error.title");  //$NON-NLS-1$
			String message= SearchMessages.getString("CopyToClipboardAction.error.message"); //$NON-NLS-1$
			if (MessageDialog.openQuestion(shell, title, message))
				copyToClipbard(clipboard, str, shell);
		}	
	}
}