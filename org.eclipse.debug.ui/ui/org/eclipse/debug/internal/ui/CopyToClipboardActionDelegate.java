package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.dnd.*;

public class CopyToClipboardActionDelegate extends ControlActionDelegate {
	
	private ContentViewer fViewer;

	private static final String PREFIX= "copy_to_clipboard_action.";
		
	/**
	 * @see ControlActionDelegate
	 */
	public void initializeForOwner(ControlAction controlAction) {		
		controlAction.setEnabled(!controlAction.getStructuredSelection().isEmpty());
		fViewer = (ContentViewer)controlAction.getSelectionProvider();		
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IDebugElement;
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(final Object element) {
		StringBuffer buffer= new StringBuffer();
		IDebugElement de= (IDebugElement) element;
		append(de, buffer, (ILabelProvider)fViewer.getLabelProvider(), 0);

		RTFTransfer rtfTransfer = RTFTransfer.getInstance();
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		Clipboard clipboard= new Clipboard(fViewer.getControl().getDisplay());		
		clipboard.setContents(
			new String[]{buffer.toString()}, 
			new Transfer[]{plainTextTransfer});
	}

	/** 
	 * Appends the representation of the specified element (using the label provider and indent)
	 * to the buffer.  For elements down to stack frames, children representations
	 * are append to the buffer as well.
	 */
	protected void append(IDebugElement e, StringBuffer buffer, ILabelProvider lp, int indent) {
		for (int i= 0; i < indent; i++) {
			buffer.append('\t');
		}
		buffer.append(lp.getText(e));
		buffer.append(System.getProperty("line.separator"));
		if (e.getElementType() < IDebugElement.STACK_FRAME) {
			IDebugElement[] children= new IDebugElement[]{};
			try {
				children= e.getChildren();
			} catch (DebugException de) {
			}
			for (int i = 0;i < children.length; i++) {
				IDebugElement de= children[i];
				append(de, buffer, lp, indent + 1);
			}
		}
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected boolean enableForMultiSelection() {
		return false;
	}

	protected String getHelpContextId() {
		return IDebugHelpContextIds.COPY_TO_CLIPBOARD_ACTION;
	}
}