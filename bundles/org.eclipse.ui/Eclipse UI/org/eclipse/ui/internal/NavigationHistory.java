package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;

/**
 */
public class NavigationHistory {
	
	private static int NavigationHistoryCapacity = 10;
	
	//Separator between back and forward item. Allways between 0 and size - 1;
	//history[index] is the current item.
	private int index = -1;
	//Always between 0 and NavigationHistoryCapacity;
	//history[size] always == null
	private int size = 0;
	
	private NavigationHistoryEntry[] history = new NavigationHistoryEntry[NavigationHistoryCapacity];
	
	private NavigationHistoryAction backwardAction;
	private NavigationHistoryAction forwardAction;
	private WorkbenchPage page;
	boolean ignoreEntries = false;
	
	/**
	 * Constructor for NavigationHistory.
	 */
	public NavigationHistory(WorkbenchPage page) {
		super();
		this.page = page;
	}
	public void forward() {
		if(index < (size - 1)) {
			index++;	
			gotoEntry();
			enableActions();
		}
	}
	public void backward() {
		if(index > 0) {
			index--;			
			gotoEntry();
			enableActions();
		}
	}
	public void setForwardAction(NavigationHistoryAction action) {
		forwardAction = action;
		enableActions();
	}
	public void setBackwardAction(NavigationHistoryAction action) {
		backwardAction = action;
		enableActions();
	}
	public void add(IEditorPart part) {
		NavigationHistoryEntry e = new NavigationHistoryEntry();
		e.part = part;
		add(e);
	}
	public void add(ISelection selection) {
		NavigationHistoryEntry e = new NavigationHistoryEntry();
		e.part = page.getActiveEditor();
		e.selection = selection;
		add(e);
	}
	private void add(NavigationHistoryEntry o) {
		if(ignoreEntries)
			return;
		size = index + 1;
		if(size >= history.length) {
			System.arraycopy(history,1,history,0,history.length - 2);
			history[size - 1] = o;
			size = history.length;
		} else {
			index++;
			history[size] = o;
			size++;
			for (int i = size; i < history.length; i++) {
				history[i] = null;	
			}
		}
		printEntries();
		enableActions();
	}
	private void gotoEntry() {
		printEntries();
		try {
			ignoreEntries = true;
			history[index].gotoEntry(page);
		} finally {
			ignoreEntries = false;
		}
	}
	private void printEntries() {
		for (int i = 0; i < size; i++) {
			String append = "";
			if(index == i)
				append = ">>";
			System.out.println(append + "Index: " + i + " " + history[i]);	
		};
	}
	private void enableActions() {
		boolean backward = index > 0;
		boolean forward = index < (size - 1);
		if(backwardAction != null)
			backwardAction.setEnabled(backward);
		if(forwardAction != null)
			forwardAction.setEnabled(forward);			
	}
	
	/**
	 * 	 */
	private static class NavigationHistoryEntry {
		private IEditorPart part;
		private ISelection selection;
		public void gotoEntry(WorkbenchPage page) {
			if(part != null) {
				page.activate(part);
				if(selection != null) {
					ISelectionProvider prov = part.getEditorSite().getSelectionProvider();
					prov.setSelection(selection);
				}
			}
		}
		public String toString() {
			return "Part<" + part + "> Selection<" + selection + ">";
		}
	}
}
