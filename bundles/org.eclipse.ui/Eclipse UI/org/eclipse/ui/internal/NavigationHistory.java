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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.NavigationLocation;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.dialogs.WorkInProgressPreferencePage;



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
						if (entry.refersToSamePart(editor)) {
							if (!entry.handlePartClosed())
								e.remove();
						}
					}
				}
			}
		};
		
		private class HistoryEntry {
			
			private IEditorPart fPart;
			private XMLMemento fMemento;
			private NavigationLocation fLocation;
			
			public HistoryEntry(IEditorPart part) {
				fPart= part;
			}
			
			public void gotoEntry() {
				
				if (fPart != null) {
					fPage.activate(fPart);
				} else if (fMemento != null) {
					IEditorPart part= restoreEditor(fMemento);
					if (part != null) {
						fPart= part;
						if (fLocation != null) {
							IMemento child=  fMemento.getChild(IWorkbenchConstants.TAG_POSITION);
							fLocation.restoreAndActivate(fPart, child);
						}
					}
				}
				
				if (fPart != null && fLocation != null)
					fLocation.restoreLocation(fPart);
			}
			
			public boolean handlePartClosed() {
				IEditorInput input = fPart.getEditorInput();
				IPersistableElement persistable = input.getPersistable();
				if (persistable == null)
					return false;
					
				fMemento=  XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
				fMemento.putString(IWorkbenchConstants.TAG_ID, fPart.getSite().getId());
				fMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
				persistable.saveState(fMemento);
				
				if (fLocation != null) {
					IMemento child= fMemento.createChild(IWorkbenchConstants.TAG_POSITION);
					fLocation.saveAndDeactivate(fPart, child);
				}
				
				fPart= null;
				return true;
			}
			
			private IEditorPart restoreEditor(IMemento memento) {
				String factoryID= memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
				IElementFactory factory= WorkbenchPlugin.getDefault().getElementFactory(factoryID);
				if (factory != null) {
					IAdaptable element= factory.createElement(memento);
					if (element instanceof IEditorInput) {
						IEditorInput input= (IEditorInput) element;
						String editorID= memento.getString(IWorkbenchConstants.TAG_ID);
						try {
							return fPage.openEditor(input, editorID, true);
						} catch (PartInitException e) {
							// ignore for now
						}
					}
				}
				return null;
			}
			
			public String toString() {
				return "Part<" + fPart + "> Details<" + fLocation + ">";
			}
			
			public boolean refersToSamePart(IEditorPart part) {
				return fPart == part && part != null;
			}
			
			public boolean locationChanged(IEditorPart part) {
				if (part == null || fPart != part)
					return true;
					
				if (fLocation == null)
					return false;
					
				return fLocation.differsFromCurrentLocation(part);
			}
			
			public void dispose() {
				if (fLocation != null)
					fLocation.dispose();
			}
			
			public boolean mergeInto(HistoryEntry entry) {
				if (refersToSamePart(entry.fPart)) {
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
		
		if (part != null)  {
			HistoryEntry entry= getEntry(fCounter);
			if (entry != null && !entry.refersToSamePart(part))
				commit(new HistoryEntry((IEditorPart) part));
		}
		
		updateActions();
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
	
	public NavigationLocation[] getEntries(IEditorPart part) {
		List list= new ArrayList(5);
		Iterator e= fHistory.iterator();
		while (e.hasNext()) {
			HistoryEntry entry= (HistoryEntry) e.next();
			if (entry.refersToSamePart(part) && entry.fLocation != null)
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
