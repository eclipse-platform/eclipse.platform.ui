package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.*;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;

public class CopyToClipboardActionDelegate extends ControlActionDelegate {
	
	private ContentViewer fViewer;

	private static final String PREFIX= "copy_to_clipboard_action.";
	
	/**
	 * @see ControlActionDelegate
	 */
	public void initializeForOwner(ControlAction controlAction) {		
		controlAction.setEnabled(!controlAction.getStructuredSelection().isEmpty());
		fViewer = (ContentViewer)controlAction.getSelectionProvider();		
		super.initializeForOwner(controlAction);
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
	protected void doAction(Object element, StringBuffer buffer) {
		IDebugElement de= (IDebugElement) element;
		append(de, buffer, (ILabelProvider)fViewer.getLabelProvider(), 0);
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object element) {
		StringBuffer buffer= new StringBuffer();
		doAction(element, buffer);
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

	protected String getHelpContextId() {
		return IDebugHelpContextIds.COPY_TO_CLIPBOARD_ACTION;
	}
	
	/**
	 * Do the specific action using the current selection.
	 */
	public void run() {
		LaunchesView view= getLaunchesView(fMode);
		if (view == null) {
			return;
		}
		final Iterator iter= pruneSelection(view);
		String pluginId= DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier();
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				StringBuffer buffer= new StringBuffer();
				while (iter.hasNext()) {
					doAction(iter.next(), buffer);
				}
				RTFTransfer rtfTransfer = RTFTransfer.getInstance();
				TextTransfer plainTextTransfer = TextTransfer.getInstance();
				Clipboard clipboard= new Clipboard(fViewer.getControl().getDisplay());		
				clipboard.setContents(
					new String[]{buffer.toString()}, 
					new Transfer[]{plainTextTransfer});
			}
		});
	}
	
	/**
	 * Removes the duplicate items from the selection.
	 * That is, if both a parent and a child are in a selection
	 * remove the child.
	 */
	protected Iterator pruneSelection(LaunchesView view) {
		IStructuredSelection selection= (IStructuredSelection)view.getSite().getSelectionProvider().getSelection();
		List elements= new ArrayList(selection.size());
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object element= iter.next();
			if (isEnabledFor(element)) {
				IDebugElement de= (IDebugElement)element;
				IDebugElement parent= de.getParent();
				if(walkHierarchy(de, elements)) {
					elements.add(de);
				}
			}
		}
		return elements.iterator();
	}
	
	/**
	 * Returns whether the parent of the specified
	 * debug element is already contained in the collection.
	 */
	protected boolean walkHierarchy(IDebugElement de, List elements) {
		IDebugElement parent= de.getParent();
		if (parent == null) {
			return true;
		}
		if (elements.contains(parent)) {
			return false;
		}
		return walkHierarchy(parent, elements);
		
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {		
	}
}