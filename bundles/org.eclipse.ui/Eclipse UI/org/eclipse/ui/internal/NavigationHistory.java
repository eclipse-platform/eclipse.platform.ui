package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.*;
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
			gotoEntry(getEntry());
			enableActions();
		}
	}
	
	private void reset() {
		index = -1; 
		size = 0;
		history = new NavigationHistoryEntry[NavigationHistoryCapacity];
	}
	
	public void backward() {
		//If the editor is deactivated the back button should re-activate the editor.
		IEditorPart editor = page.getActiveEditor();
		boolean activateEditor = editor != null && editor != page.getActivePart();
		if(activateEditor) {
			NavigationHistoryEntry e = new NavigationHistoryEntry();
			e.part = editor;
			gotoEntry(e);
		} else if(index > 0) {
			index--;			
			gotoEntry(getEntry());
		}
		enableActions();
	}
	
	public void setForwardAction(NavigationHistoryAction action) {
		forwardAction = action;
		enableActions();
	}
	
	public void setBackwardAction(NavigationHistoryAction action) {
		backwardAction = action;
		enableActions();
	}
	
	private boolean samePart(NavigationHistoryEntry entry, IEditorPart part) {
		return entry.part == part;
	}
	
	public void add(IEditorPart part) {
		NavigationHistoryEntry e= getEntry();
		if (e != null && samePart(e, part))
			return;
			
		if(part != null) {
			e = new NavigationHistoryEntry();
			e.part = (IEditorPart)part;
			add(e);
		} else {
			enableActions();
		}
	}
	
	public void add(ISelection selection) {
		if (selection == null || ignoreEntries)
			return;

		IEditorPart part= page.getActiveEditor();			
		
		NavigationHistoryEntry e= getEntry();
		if (e != null && samePart(e, part)) {
			if (e.selection == null) {
				e.selection= selection;
				return;
			} else if (e.selection.equals(selection)) {
				return;
			}
		}
		
		e = new NavigationHistoryEntry();
		e.part = part;
		e.selection = selection;
		add(e);
	}
	
	private void add(NavigationHistoryEntry o) {
		if(ignoreEntries)
			return;
		
		size = index + 1;
		if(size >= history.length) {
			System.arraycopy(history,1,history,0,history.length - 1);
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
	
	private void gotoEntry(NavigationHistoryEntry entry) {
		printEntries();
		try {
			ignoreEntries = true;
			entry.gotoEntry(page);
		} finally {
			ignoreEntries = false;
		}
	}
	
	private NavigationHistoryEntry getEntry() {
		return (index <= size && size > 0) ? history[index] : null;
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
		IEditorPart editor = page.getActiveEditor();
		boolean backward = index > 0 || (editor != null && editor != page.getActivePart());
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
					ISelectionProvider prov = part.getSite().getSelectionProvider();
					prov.setSelection(selection);
				}
			}
		}
		
		public String toString() {
			return "Part<" + part + "> Selection<" + selection + ">";
		}
	}
}
