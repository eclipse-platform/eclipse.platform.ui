/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.NavigationLocation;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.dialogs.WorkInProgressPreferencePage;



/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public class NavigationHistory {
	
	
		private class PartListener implements IPartListener {
			
			public void partActivated(IWorkbenchPart part) {}
		
			public void partBroughtToTop(IWorkbenchPart part) {}
			
			public void partDeactivated(IWorkbenchPart part) {}
		
			public void partOpened(IWorkbenchPart part) {}
			
			public void partClosed(IWorkbenchPart part) {
				if (part instanceof IEditorPart) {
					IEditorPart editor= (IEditorPart) part;
					
					Iterator e= fHistory.iterator();
					while (e.hasNext()) {
						HistoryEntry entry= (HistoryEntry) e.next();
						if (entry.fEditorPart == editor) {
							if (!entry.handlePartClosed())
								e.remove();
						}
					}
				}
			}
		};
		
		private class HistoryEntry {
			
			private IEditorInput fEditorInput;
			private String fEditorID;
			private IEditorPart fEditorPart;
			
			private XMLMemento fMemento;
			private NavigationLocation fLocation;
			
			public HistoryEntry(IEditorPart part) {
				fEditorPart= part;
				fEditorID= part.getSite().getId();
				fEditorInput= part.getEditorInput();
			}
			
			public void gotoEntry() {
				
				IMemento memento= fMemento;
				fMemento= null;
				
				if (memento != null)
					restoreEditor(memento);
					
				if (fEditorInput != null && fEditorID != null) {
					try {
						
						fEditorPart= fPage.openEditor(fEditorInput, fEditorID, true);
						if (fLocation != null) {
							if (memento != null) {
								memento=  memento.getChild(IWorkbenchConstants.TAG_POSITION);
								fLocation.clearState();
								fLocation.setEditorPart(fEditorPart);
								fLocation.restoreState(memento);
							}
							
							fLocation.setEditorPart(fEditorPart);
							fLocation.restore();
						}
						
					} catch (PartInitException e) {
						// ignore for now
					}
				}
			}
			
			public boolean handlePartClosed() {
				IPersistableElement persistable = fEditorInput.getPersistable();
				if (persistable == null)
					return false;
					
				fMemento=  XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
				fMemento.putString(IWorkbenchConstants.TAG_ID, fEditorID);
				fMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
				persistable.saveState(fMemento);
				
				if (fLocation != null) {
					IMemento child= fMemento.createChild(IWorkbenchConstants.TAG_POSITION);
					fLocation.saveState(child);
					fLocation.clearState();
				}
				
				fEditorPart= null;
				fEditorID= null;
				fEditorInput= null;
				return true;
			}
			
			private void restoreEditor(IMemento memento) {
				String factoryID= memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
				IElementFactory factory= WorkbenchPlugin.getDefault().getElementFactory(factoryID);
				if (factory != null) {
					IAdaptable element= factory.createElement(memento);
					if (element instanceof IEditorInput) {
						fEditorInput= (IEditorInput) element;
						fEditorID= memento.getString(IWorkbenchConstants.TAG_ID);
					}
				}
			}
			
			public String toString() {
				return "Part<" + fEditorPart + "> Input<" + fEditorInput + "> Details<" + fLocation + ">";
			}
			
			public boolean refersToSameEditorInput(IEditorInput input) {
				return fEditorInput != null && fEditorInput.equals(input);
			}
			
			public boolean locationChanged(IEditorPart part) {
				if (part == null)
					return true;
				if (!refersToSameEditorInput(part.getEditorInput()))
					return true;
				if (fLocation == null)
					return false;
				return !fLocation.equalsLocationOf(part);
			}
			
			public void dispose() { 
				if (fLocation != null)
					fLocation.dispose();
			}
			
			public boolean mergeInto(HistoryEntry entry) {
				if (entry.fEditorPart != null && refersToSameEditorInput(entry.fEditorPart.getEditorInput())) {
					if (fLocation != null) {
						if (entry.fLocation == null) {
							entry.fLocation= fLocation;
							return true;
						} else {
							return fLocation.mergeInto(entry.fLocation);
						}
					}
					return true;
				}
				return false;
			}
		};
	
	
	
	private static final int CAPACITY= 50;
	
	private NavigationHistoryAction fBackwardAction;
	private NavigationHistoryAction fForwardAction;
	private int fIgnoreEntries;
	
	private ArrayList fHistory= new ArrayList(10);
	private WorkbenchPage fPage;
	private int fCounter;
	
	
	
	public NavigationHistory(WorkbenchPage page) {
		super();
		fPage= page;
		fPage.addPartListener(new PartListener());
	}
	
	public void dispose() {
		Iterator e= fHistory.iterator();
		while (e.hasNext()) {
			HistoryEntry entry= (HistoryEntry) e.next();
			entry.dispose();
		}
	}
	
	public void setForwardAction(NavigationHistoryAction action) {
		fForwardAction= action;
		updateActions();
	}
	
	public void setBackwardAction(NavigationHistoryAction action) {
		fBackwardAction= action;
		updateActions();
	}
	
	private void commit(HistoryEntry entry) {
		
		int length= fHistory.size();
		for (int i= fCounter + 1; i < length; i++) {
			HistoryEntry e= (HistoryEntry) fHistory.remove(fCounter + 1);
			e.dispose();
		}
			
		fHistory.add(entry);
		
		int delta= fHistory.size() - CAPACITY;
		for (int i= 0; i < delta; i++) {
			HistoryEntry e= (HistoryEntry) fHistory.remove(0);
			e.dispose();
		}
		
		fCounter= fHistory.size() - 1;
	}
	
	private HistoryEntry getEntry(int index) {
		if (0 <= index && index < fHistory.size())
			return (HistoryEntry) fHistory.get(index);
		return null;
	}
	
	public void add(IEditorPart part) {
		if(!WorkInProgressPreferencePage.useNavigationHistory())
			return;
		
		if (fIgnoreEntries > 0)
			return;
		
		HistoryEntry entry= getEntry(fCounter);
		if (entry != null) {
			captureLocation(entry);
			if (part != null && !entry.refersToSameEditorInput(part.getEditorInput()))
				commit(new HistoryEntry((IEditorPart) part));
		}
		
		updateActions();
	}
	
	private void captureLocation(HistoryEntry entry) {
		if (entry != null && entry.fEditorPart != null) {
			if (entry.locationChanged(entry.fEditorPart)) {
				HistoryEntry newEntry= new HistoryEntry(entry.fEditorPart);
				if (entry.fEditorPart instanceof INavigationLocationProvider) {
					INavigationLocationProvider provider= (INavigationLocationProvider) entry.fEditorPart;
					newEntry.fLocation= provider.createNavigationLocation();
				}
				commit(newEntry);
				entry= getEntry(fCounter);
			}
		}
	}
	
	public void addEntry(IEditorPart part, NavigationLocation location) {
		if(!WorkInProgressPreferencePage.useNavigationHistory())
			return;
		
		if (fIgnoreEntries > 0 || location == null)
			return;
			
		HistoryEntry e= new HistoryEntry(part);
		e.fLocation= location;
		HistoryEntry current= getEntry(fCounter);
		if (current == null || !e.mergeInto(current))
			commit(e);
			
		updateActions();
	}
	
	public NavigationLocation[] getEntries(IEditorInput input) {
		List list= new ArrayList(5);
		Iterator e= fHistory.iterator();
		while (e.hasNext()) {
			HistoryEntry entry= (HistoryEntry) e.next();
			if (entry.refersToSameEditorInput(input) && entry.fLocation != null)
				list.add(entry.fLocation);
		}
		
		NavigationLocation[] result= new NavigationLocation[list.size()];
		list.toArray(result);
		return result;
	}
	
	private void printEntries() {
		if (false) {
			int size= fHistory.size();
			for (int i= 0; i < size; i++) {
				String append= fCounter == i ? ">>" : "";
				System.out.println(append + "Index: " + i + " " + fHistory.get(i));	
			};
		}
	}
	
	private boolean canForward() {
		return (0 <= fCounter + 1) && (fCounter + 1 < fHistory.size());
	}
	
	private boolean locationChanged(IEditorPart part) {
		HistoryEntry entry= getEntry(fCounter);
		if (entry != null)
			return entry.locationChanged(part);
		return false;
	}
	
	private boolean canBackward() {
		IEditorPart editor= fPage.getActiveEditor();
		boolean activateEditor= (editor != null && editor != fPage.getActivePart());			
		return activateEditor || locationChanged(editor) || (0 <= fCounter - 1) && (fCounter - 1 < fHistory.size());
	}
	
	private void updateActions() {
		if (fBackwardAction != null)
			fBackwardAction.setEnabled(canBackward());
		if (fForwardAction != null)
			fForwardAction.setEnabled(canForward());
	}
	
	private void gotoEntry(HistoryEntry entry) {
		try {
			++ fIgnoreEntries;
			entry.gotoEntry();
		} finally {
			-- fIgnoreEntries;
		}
	}
	
	public void forward() {
		if (canForward()) {
			HistoryEntry entry= (HistoryEntry) fHistory.get(++fCounter);
			gotoEntry(entry);
			updateActions();
			printEntries();
		}
	}
	
	public void backward() {
		if (canBackward()) {
			
			HistoryEntry entry= null;
			
			IEditorPart editor= fPage.getActiveEditor();
			boolean activateEditor= (editor != null && editor != fPage.getActivePart());
			if(activateEditor)
				entry= new HistoryEntry(editor);
			else if (locationChanged(editor))
				entry= (HistoryEntry) fHistory.get(fCounter);
			else
				entry= (HistoryEntry) fHistory.get(--fCounter);
			
			if (entry != null) {
				gotoEntry(entry);
				updateActions();
				printEntries();
			}
		}
	}
}
