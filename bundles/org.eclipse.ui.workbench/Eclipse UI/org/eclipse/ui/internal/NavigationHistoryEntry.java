package org.eclipse.ui.internal;
import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.*;
/*
 * Wraps the INavigationLocation and keeps the info
 * about the editor.
 */
public class NavigationHistoryEntry {

	private IWorkbenchPage page;

	/* The entry will have the pair input and id or a memento that
	 * saves it or the entry that has one of the above.
	 * Only one of them (input, memento, mementoEntry) will be set at a time;
	 * the other two are null.
	 */		
	protected String editorID;
	protected IEditorInput editorInput;
	private IMemento memento;
	protected NavigationHistoryEntry mementoEntry;
	protected String historyText;
	
	/* Both may be set at the same time. */
	protected INavigationLocation location;
	private IMemento locationMemento;
	
	/**
	 * Constructs a new HistoryEntry and intializes its editor input and editor id.
	 */
	public NavigationHistoryEntry(IWorkbenchPage page,IEditorPart part, INavigationLocation location) {
		this.page = page;
		editorID= part.getSite().getId();
		editorInput= part.getEditorInput();
		this.location = location;
		if (location != null) {
			historyText = location.getText();
		}
		// ensure that the historyText is initialized to something
		if (historyText == null || historyText.equals("")) { //$NON-NLS-1$
			historyText = part.getTitle();
		}
	}
	/**
	 * Constructs a new HistoryEntry. <code>restoreState</code> should be called
	 * to intialize the state of this entry.
	 */
	protected NavigationHistoryEntry(IWorkbenchPage page) {
		this.page = page;
	}
	/**
	 * Restores the state of the entry and the location if needed and then
	 * restores the location.
	 */
	/* package */ void restoreLocation() {
		if(editorInput == null) {
			if(memento != null) {
				restoreEditor(memento);
				memento = null;
			} else if(mementoEntry != null) {
				if(mementoEntry.memento != null) {
					mementoEntry.restoreEditor(mementoEntry.memento);
					mementoEntry.memento = null;
				}
				editorID = mementoEntry.editorID;
				editorInput = mementoEntry.editorInput;
				mementoEntry = null;
			}
		}
			
		if (editorInput != null && editorID != null) {
			try {
				IEditorPart editor = page.openEditor(editorInput, editorID, true);
				if (location == null) {
					if(editor instanceof INavigationLocationProvider)
						location = ((INavigationLocationProvider)editor).createEmptyNavigationLocation();
				}
					
				if (location != null) {
					if (locationMemento != null) {
						location.setInput(editorInput);
						location.restoreState(locationMemento);
						locationMemento = null;
					}
					location.restoreLocation();
				}
			} catch (PartInitException e) {
				// ignore for now
			}
		}
	}
	/**
	 * Returns true if this entry can be persisted otherwise returns false;
	 */
	/* package */ boolean isPersistable() {
		if(editorInput != null) {
			IPersistableElement persistable = editorInput.getPersistable();
			return persistable != null;
		}
		return memento != null || mementoEntry != null;
	}
	/**
	 * Return the label to display in the history drop down list.  Use the
	 * history entry text if the location has not been restored yet.
	 */
	/* package */ String getHistoryText() {
		if (location != null) {
			// location exists or has been restored, use its text.
			// Also update the historyText so that this value will
			// be saved.  Doing so handles cases where getText() value 
			// may be dynamic. 
			String text = location.getText();
			if ((text == null) || text.equals("")) { //$NON-NLS-1$
				text = historyText;
			} else {
				historyText = text;
			}
			return text;
		} else {		
			return historyText;
		}
	}
	/** 
	 * Saves the state of this entry and its location.
	 * Returns true if possible otherwise returns false.
	 */
	/* package */ boolean handlePartClosed() {
		if(!isPersistable())
			return false;
		if(mementoEntry == null && memento == null) {	
			IPersistableElement persistable = editorInput.getPersistable();	
			memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
			memento.putString(IWorkbenchConstants.TAG_ID, editorID);
			memento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
			persistable.saveState(memento);
		}
		
		if (location != null) {
			locationMemento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_POSITION);
			location.saveState(locationMemento);
			location.releaseState();
		}
		editorID= null;
		editorInput= null;
		return true;
	}
	/**
	 * Saves the state of this entry and its location.
	 */
	/* package */ void saveState(IMemento mem, ArrayList entries) {
		mem.putString(IWorkbenchConstants.TAG_HISTORY_LABEL, getHistoryText());
		if(editorInput != null) {
			int mementoEntryIndex = -1;
			int size = entries.size();
			for (int i = 0; i < size; i++) {
				NavigationHistoryEntry entry = (NavigationHistoryEntry)entries.get(i);
				if(entry == this)
					break;
				if(editorInput.equals(entry.editorInput) && editorID.equals(entry.editorID)) {
					mementoEntryIndex = i;
					break;
				}
			}
			if(mementoEntryIndex >= 0) { 
				IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_INDEX);			
				childMem.putInteger(IWorkbenchConstants.TAG_INDEX,mementoEntryIndex);
			} else {			
				IPersistableElement persistable = editorInput.getPersistable();
				IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_EDITOR);
				childMem.putString(IWorkbenchConstants.TAG_ID, editorID);
				childMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
				persistable.saveState(childMem);
			}
		} else if(memento != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_EDITOR);
			childMem.putMemento(memento);
		} else {
			int mementoEntryIndex = entries.indexOf(mementoEntry);
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_INDEX);			
			childMem.putInteger(IWorkbenchConstants.TAG_INDEX,mementoEntryIndex);
		}
		
		if(locationMemento != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_POSITION);
			childMem.putMemento(locationMemento);			
		} else if (location != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_POSITION);
			location.saveState(childMem);
		}
	}
	/**
	 * Restore the state of this entry.
	 */
	/* package */ void restoreState(IMemento mem,ArrayList entries) {
		historyText = mem.getString(IWorkbenchConstants.TAG_HISTORY_LABEL);
		memento = mem.getChild(IWorkbenchConstants.TAG_EDITOR);
		locationMemento = mem.getChild(IWorkbenchConstants.TAG_POSITION);
		IMemento childMem = mem.getChild(IWorkbenchConstants.TAG_INDEX);
		if(childMem != null) {
			Integer index = childMem.getInteger(IWorkbenchConstants.TAG_INDEX);
			if(index.intValue() >= 0)
				mementoEntry = (NavigationHistoryEntry)entries.get(index.intValue());
			memento = null;
		}
	}
	/*
	 * Restores the editor input and editor id.
	 */
	private void restoreEditor(IMemento memento) {
		String factoryID= memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
		IElementFactory factory= WorkbenchPlugin.getDefault().getElementFactory(factoryID);
		if (factory != null) {
			IAdaptable element= factory.createElement(memento);
			if (element instanceof IEditorInput) {
				editorInput= (IEditorInput) element;
				editorID= memento.getString(IWorkbenchConstants.TAG_ID);
			}
		}
	}
	/*
 	 * (non-Javadoc)
     * Method declared on Object.
     */	
	public String toString() {
		return "Input<" + editorInput + "> Input<" + editorInput + "> Details<" + location + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	/**
	 * Disposes this entry and its location.
	 */
	/* package */ void dispose() { 
		if (location != null)
			location.dispose();
	}
	/**
	 * Merges this entry into the current entry. Returns true
	 * if the merge was possible otherwise returns false.
	 */
	/* package */ boolean mergeInto(NavigationHistoryEntry currentEntry) {
		if (editorInput != null && editorInput.equals(currentEntry.editorInput)) {
			if (location != null) {
				if (currentEntry.location == null) {
					currentEntry.location= location;
					return true;
				} else {
					return location.mergeInto(currentEntry.location);
				}
			}
		}
		return false; 
	}
};