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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;


public class NavigationHistory {
	
	
			private class NavigationHistoryEntry {
				
				private IEditorPart fPart;
				private XMLMemento fMemento;
				private ISelection fSelection;
				
				
				public NavigationHistoryEntry(IEditorPart part) {
					fPart= part;
				}
				
				public void gotoEntry() {
					try {
						
						++fIgnoreEntries;
						
						if (fPart != null) {
							fPage.activate(fPart);
						} else if (fMemento != null) {
							IEditorPart part= restoreEditor(fMemento);
							if (part != null) {
								fPart= part;
								fMemento= null;
							}
						}
						
						if (fPart != null && fSelection != null) {
							ISelectionProvider prov = fPart.getSite().getSelectionProvider();
							prov.setSelection(fSelection);
						}
					} finally {
						-- fIgnoreEntries;
					}
				}
				
				public boolean saveEditor() {
					IEditorInput input = fPart.getEditorInput();
					IPersistableElement persistable = input.getPersistable();
					if (persistable == null)
						return false;
						
					fMemento=  XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
					fMemento.putString(IWorkbenchConstants.TAG_ID, fPart.getSite().getId());
					fMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
					persistable.saveState(fMemento);
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
					return "Part<" + fPart + "> Selection<" + fSelection + ">";
				}
				
				public boolean samePart(IEditorPart part) {
					return fPart == part && part != null;
				}
			};
			
			
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
							NavigationHistoryEntry entry= (NavigationHistoryEntry) e.next();
							if (entry.samePart(editor)) {
								if (!entry.saveEditor())
									e.remove();
							}
						}
					}
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
	
	public void setForwardAction(NavigationHistoryAction action) {
		fForwardAction= action;
		updateActions();
	}
	
	public void setBackwardAction(NavigationHistoryAction action) {
		fBackwardAction= action;
		updateActions();
	}
	
	private void commit(NavigationHistoryEntry entry) {
		
		int length= fHistory.size();
		for (int i= fCounter + 1; i < length; i++)
			fHistory.remove(fCounter + 1);
			
		fHistory.add(entry);
		
		int delta= fHistory.size() - CAPACITY;
		for (int i= 0; i < delta; i++)
			fHistory.remove(0);
		
		fCounter= fHistory.size() - 1;
	}
	
	private NavigationHistoryEntry getCurrent() {
		if (0 <= fCounter && fCounter < fHistory.size())
			return (NavigationHistoryEntry) fHistory.get(fHistory.size() -1);
		return null;
	}
	
	private boolean merge(IEditorPart part) {
		NavigationHistoryEntry entry= getCurrent();
		if (entry != null)
			return entry.samePart(part);
		return false;
	}
	
	public void add(IEditorPart part) {
		
		if (fIgnoreEntries > 0)
			return;
		
		if (!merge(part)) {
			if (part != null)
				commit(new NavigationHistoryEntry((IEditorPart) part));
		}
		updateActions();
	}
	
	private boolean merge(IEditorPart part, ISelection selection) {
		NavigationHistoryEntry entry= getCurrent();
		if (entry != null && entry.samePart(part)) {
			if (entry.fSelection == null) {
				entry.fSelection= selection;
				return true;
			} else {
				return entry.fSelection.equals(selection);
			}
		}
		return false;
	}
	
	public void add(ISelection selection) {
		
		if (selection == null || fIgnoreEntries > 0)
			return;

		IEditorPart part= fPage.getActiveEditor();
		if (!merge(part, selection)) {
			if (part != null) {
				NavigationHistoryEntry entry= new NavigationHistoryEntry(part);
				entry.fSelection= selection;
				commit(entry);
			}
		}
		
		updateActions();
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
	
	private boolean canBackward() {
		IEditorPart editor= fPage.getActiveEditor();
		boolean activateEditor= (editor != null && editor != fPage.getActivePart());
		return activateEditor || (0 <= fCounter - 1) && (fCounter - 1 < fHistory.size());
	}
	
	private void updateActions() {
		if (fBackwardAction != null)
			fBackwardAction.setEnabled(canBackward());
		if (fForwardAction != null)
			fForwardAction.setEnabled(canForward());
	}
	
	public void forward() {
		if (canForward()) {
			NavigationHistoryEntry e= (NavigationHistoryEntry) fHistory.get(++fCounter);
			e.gotoEntry();
			updateActions();
			printEntries();
		}
	}
	
	public void backward() {
		if (canBackward()) {
			IEditorPart editor= fPage.getActiveEditor();
			boolean activateEditor= (editor != null && editor != fPage.getActivePart());
			if(activateEditor) {
				NavigationHistoryEntry e= new NavigationHistoryEntry(editor);
				e.gotoEntry();	
			} 	else {		
				NavigationHistoryEntry e= (NavigationHistoryEntry) fHistory.get(--fCounter);
				e.gotoEntry();
				updateActions();
				printEntries();
			}
		}
	}
}
