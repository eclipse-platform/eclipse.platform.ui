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
	
	protected ContentViewer fViewer;

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
		append(element, buffer, (ILabelProvider)fViewer.getLabelProvider(), 0);
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
	protected void append(Object e, StringBuffer buffer, ILabelProvider lp, int indent) {
		for (int i= 0; i < indent; i++) {
			buffer.append('\t');
		}
		buffer.append(lp.getText(e));
		buffer.append(System.getProperty("line.separator"));
		if (shouldAppendChildren(e)) {
			Object[] children= new Object[0];
			children= getChildren(e);
			for (int i = 0;i < children.length; i++) {
				Object de= children[i];
				append(de, buffer, lp, indent + 1);
			}
		}
	}

	protected String getHelpContextId() {
		return IDebugHelpContextIds.COPY_TO_CLIPBOARD_ACTION;
	}
	
	protected Object getParent(Object e) {
		return ((ITreeContentProvider) fViewer.getContentProvider()).getParent(e);
	}
	
	/**
	 * Returns the children of the parent after applying the filters
	 * that are present in the viewer.
	 */
	protected Object[] getChildren(Object parent) {
		Object[] children= ((ITreeContentProvider)fViewer.getContentProvider()).getChildren(parent);
		ViewerFilter[] filters= ((StructuredViewer)fViewer).getFilters();
		if (filters != null) {
			for (int i= 0; i < filters.length; i++) {
				ViewerFilter f = filters[i];
				children = f.filter(fViewer, parent, children);
			}
		}
		return children;
	}
	
	/**
	 * Do the specific action using the current selection.
	 */
	public void run() {
		final Iterator iter= pruneSelection();
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
	protected Iterator pruneSelection() {
		IStructuredSelection selection= (IStructuredSelection)fViewer.getSelection();
		List elements= new ArrayList(selection.size());
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object element= iter.next();
			if (isEnabledFor(element)) {
				if(walkHierarchy(element, elements)) {
					elements.add(element);
				}
			}
		}
		return elements.iterator();
	}
	
	/**
	 * Returns whether the parent of the specified
	 * element is already contained in the collection.
	 */
	protected boolean walkHierarchy(Object element, List elements) {
		Object parent= getParent(element);
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
	
	protected boolean shouldAppendChildren(Object e) {
		return e instanceof IDebugElement && ((IDebugElement)e).getElementType() < IDebugElement.STACK_FRAME;
	}
}