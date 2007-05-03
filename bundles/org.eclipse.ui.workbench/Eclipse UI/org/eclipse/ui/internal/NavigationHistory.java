/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;

/**
 * Implementation of the back and forward actions.
 */
public class NavigationHistory implements INavigationHistory {

    private static final int CAPACITY = 50;

    private NavigationHistoryAction backwardAction;

    private NavigationHistoryAction forwardAction;

    private int ignoreEntries;

    private ArrayList history = new ArrayList(CAPACITY);
    
    Map perTabHistoryMap = new HashMap();

    private ArrayList editors = new ArrayList(CAPACITY);

    private WorkbenchPage page;

    private int activeEntry = 0;

    /**
     * Creates a new NavigationHistory to keep the NavigationLocation
     * entries of the specified page.
     */
    public NavigationHistory(final WorkbenchPage page) {
        this.page = page;
        page.addPartListener(new IPartListener2() {
            public void partActivated(IWorkbenchPartReference partRef) {
            }

            public void partBroughtToTop(IWorkbenchPartReference partRef) {
            }

            public void partDeactivated(IWorkbenchPartReference partRef) {
            }

            public void partOpened(IWorkbenchPartReference partRef) {
            }
			
            public void partHidden(IWorkbenchPartReference partRef) {
            }
			
            public void partVisible(IWorkbenchPartReference partRef) {
            }

            public void partClosed(IWorkbenchPartReference partRef) {
            	if (isPerTabHistoryEnabled() && partRef instanceof EditorReference) {
            		if (!((EditorReference)partRef).isDisposed()) {
	            		Object editorTabCookie = ((EditorReference)partRef).getPane();
	            		disposeHistoryForTab(editorTabCookie);
	            		updateActions();
            		}
            	}
				updateNavigationHistory(partRef, true);
            }
			
			public void partInputChanged(IWorkbenchPartReference partRef) {
				updateNavigationHistory(partRef, false);
			}
			
			private void updateNavigationHistory(IWorkbenchPartReference partRef, boolean partClosed) {
                if (partRef != null && partRef.getPart(false) instanceof IEditorPart) {
                    IEditorPart editor = (IEditorPart) partRef.getPart(false);
                    IEditorInput input = editor.getEditorInput();
                    String id = editor.getSite().getId();
                    Iterator e = editors.iterator();
                    NavigationHistoryEditorInfo info = null;
                    NavigationHistoryEditorInfo currentInfo = null;
                    NavigationHistoryEntry current = getEntry(activeEntry);
                    if (current != null) {
						currentInfo = current.editorInfo;
					}
                    while (e.hasNext()) {
                        info = (NavigationHistoryEditorInfo) e.next();
                        if (id.equals(info.editorID)
                                && input.equals(info.editorInput)) {
                            if (partClosed && info != currentInfo) {
								info.handlePartClosed();
							}
                            break;
                        }
						info = null;
                    }
                    if (info == null) {
						return;
					}
                    boolean isEntryDisposed = false;
                    e = history.iterator();
                    int i = 0;
                    while (e.hasNext()) {
                        NavigationHistoryEntry entry = (NavigationHistoryEntry) e
                                .next();
                        if (entry.editorInfo == info) {
							if (!entry.handlePartClosed()) {
                                // update the active entry since we are removing an item
                                if (i < activeEntry) {
                                    activeEntry--;
                                } else if (i == activeEntry) {
                                    if (i != 0) {
										activeEntry--;
									}
                                } else {
                                    // activeEntry is before item we deleted
                                    i++;
                                }
                                isEntryDisposed = true;
                                e.remove();
                                disposeEntry(entry);
                            } else {
                                i++;
                            }
						}
                    }
                    
                    /*
                     * Promote the entry of the last closed editor to be the active
                     * one, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=154431 
                     */ 
                    if (!isEntryDisposed && page.getActiveEditor() == null && activeEntry < history.size())
                    	activeEntry++;
                    
                    updateActions();
                }
            }
        });
    }

    private Display getDisplay() {
        return page.getWorkbenchWindow().getShell().getDisplay();
    }
    
    private boolean isPerTabHistoryEnabled() {
    	return ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).isPerTabHistoryEnabled();
    }

    /*
     * Adds an editor to the editor history without getting its location.
     */
    public void markEditor(final IEditorPart part) {
        if (ignoreEntries > 0 || part == null) {
			return;
		}
        /* Ignore all entries until the async exec runs. Workaround to avoid 
         * extra entry when using Open Declaration (F3) that opens another editor. */
        ignoreEntries++;
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (--ignoreEntries == 0) {
	                if (part.getEditorSite() instanceof EditorSite) {
						EditorSite site = (EditorSite) part.getEditorSite();
		                Control c = site.getPane().getControl();
		                if (c == null || c.isDisposed()) {
							return;
						}
		                NavigationHistoryEntry e = getEntry(activeEntry);
		                if (e != null
		                        && part.getEditorInput() != e.editorInfo.editorInput) {
							updateEntry(e);
						}
		                addEntry(part);
	                }
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * Method declared on INavigationHistory.
     */
    public void markLocation(IEditorPart part) {
        addEntry(part);
    }

    /*
     * Return the backward history entries.  Return in restore order (i.e., the
     * first entry is the entry that would become active if the "Backward" action 
     * was executed).
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    NavigationHistoryEntry[] getBackwardEntries() {
    	if (isPerTabHistoryEnabled()) {
    		return getEntriesForTab(false);
    	}
        int length = activeEntry;
        NavigationHistoryEntry[] entries = new NavigationHistoryEntry[length];
        for (int i = 0; i < activeEntry; i++) {
            entries[activeEntry - 1 - i] = getEntry(i);
        }
        return entries;
    }

    /*
     * Return the forward history entries.  Return in restore order (i.e., the first
     * entry is the entry that would become active if the "Forward" action was
     * executed).
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    NavigationHistoryEntry[] getForwardEntries() {
    	if (isPerTabHistoryEnabled()) {
    		return getEntriesForTab(true);
    	}
        int length = history.size() - activeEntry - 1;
        length = Math.max(0, length);
        NavigationHistoryEntry[] entries = new NavigationHistoryEntry[length];
        for (int i = activeEntry + 1; i < history.size(); i++) {
            entries[i - activeEntry - 1] = getEntry(i);
        }
        return entries;
    }

    /*
     * (non-Javadoc)
     * Method declared on INavigationHistory.
     */
    public INavigationLocation[] getLocations() {
        INavigationLocation result[] = new INavigationLocation[history.size()];
        for (int i = 0; i < result.length; i++) {
            NavigationHistoryEntry e = (NavigationHistoryEntry) history.get(i);
            result[i] = e.location;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * Method declared on INavigationHistory.
     */
    public INavigationLocation getCurrentLocation() {
        NavigationHistoryEntry entry = getEntry(activeEntry);
        return entry == null ? null : entry.location;
    }

    /**
     * Disposes this NavigationHistory and all entries.
     */
    public void dispose() {
    	disposeHistoryForTabs();
        Iterator e = history.iterator();
        while (e.hasNext()) {
            NavigationHistoryEntry entry = (NavigationHistoryEntry) e.next();
            disposeEntry(entry);
        }
    }

    /**
     * Keeps a reference to the forward action to update its state
     * whenever needed.
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    void setForwardAction(NavigationHistoryAction action) {
        forwardAction = action;
        updateActions();
    }

    /**
     * Keeps a reference to the backward action to update its state
     * whenever needed.
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    void setBackwardAction(NavigationHistoryAction action) {
        backwardAction = action;
        updateActions();
    }

    /*
     * Returns the history entry indexed by <code>index</code>
     */
    private NavigationHistoryEntry getEntry(int index) {
        if (0 <= index && index < history.size()) {
			return (NavigationHistoryEntry) history.get(index);
		}
        return null;
    }

    /*
     * Adds the specified entry to the history.
     */
    private void add(NavigationHistoryEntry entry) {
        removeForwardEntries();
        if (history.size() == CAPACITY) {
            NavigationHistoryEntry e = (NavigationHistoryEntry) history
                    .remove(0);
            disposeEntry(e);
        }
        history.add(entry);
        activeEntry = history.size() - 1;
    }

    /*
     * Remove all entries after the active entry.
     */
    private void removeForwardEntries() {
        int length = history.size();
        for (int i = activeEntry + 1; i < length; i++) {
            NavigationHistoryEntry e = (NavigationHistoryEntry) history
                    .remove(activeEntry + 1);
            disposeEntry(e);
        }
    }

    /*
     * Adds a location to the history.
     */
    private void addEntry(IEditorPart part) {
        if (ignoreEntries > 0 || part == null) {
			return;
		}

        if (isPerTabHistoryEnabled()) {
        	markLocationForTab(part);
        }
        INavigationLocation location = null;
        if (part instanceof INavigationLocationProvider) {
			location = ((INavigationLocationProvider) part)
                    .createNavigationLocation();
		}

        NavigationHistoryEntry current = getEntry(activeEntry);
        if (current != null && current.editorInfo.memento != null) {
            current.editorInfo.restoreEditor();
            checkDuplicates(current.editorInfo);
        }
        NavigationHistoryEntry e = createEntry(page, part, location);
        if (current == null) {
            add(e);
        } else {
            if (e.mergeInto(current)) {
                disposeEntry(e);
                removeForwardEntries();
            } else {
                add(e);
            }
        }
        printEntries("added entry"); //$NON-NLS-1$
        updateActions();
    }

    /*
     * Prints all the entries in the console. For debug only.
     */
    private void printEntries(String label) {
        if (false) {
            System.out.println("+++++ " + label + "+++++ "); //$NON-NLS-1$ //$NON-NLS-2$
            int size = history.size();
            for (int i = 0; i < size; i++) {
                String append = activeEntry == i ? ">>" : ""; //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println(append
                        + "Index: " + i + " " + history.get(i)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /*
     * Returns true if the forward action can be performed otherwise returns false.
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    /* package */boolean canForward() {
    	if (isPerTabHistoryEnabled()) {
    		return hasEntriesForTab(true);
    	}
        return (0 <= activeEntry + 1) && (activeEntry + 1 < history.size());
    }

    /*
     * Returns true if the backward action can be performed otherwise returns false.
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    /* package */boolean canBackward() {
    	if (isPerTabHistoryEnabled()) {
    		return hasEntriesForTab(false);
    	}
        return (0 <= activeEntry - 1) && (activeEntry - 1 < history.size());
    }

    /*
     * Update the actions enable/disable and tooltip state.
     */
    private void updateActions() {
        if (backwardAction != null) {
			backwardAction.update();
		}
        if (forwardAction != null) {
			forwardAction.update();
		}
    }

    /*
     * Restore the specified entry
     */
    private void gotoEntry(NavigationHistoryEntry entry) {
        if (entry == null) {
			return;
		}
        try {
            ignoreEntries++;
            if (entry.editorInfo.memento != null) {
                entry.editorInfo.restoreEditor();
                checkDuplicates(entry.editorInfo);
            }
            entry.restoreLocation();
            updateActions();
            printEntries("goto entry"); //$NON-NLS-1$
        } finally {
            ignoreEntries--;
        }
    }

    /*
     * update the active entry
     */
    private void updateEntry(NavigationHistoryEntry activeEntry) {
        if (activeEntry == null || activeEntry.location == null) {
			return;
		}
        activeEntry.location.update();
        printEntries("updateEntry"); //$NON-NLS-1$
    }

    /*
     * Perform the forward action by getting the next location and restoring
     * its context.
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    void forward() {
    	if (isPerTabHistoryEnabled()) {
    		forwardForTab();
    		return;
    	}
        if (canForward()) {
			shiftEntry(true);
		}
    }

    /*
     * Perform the backward action by getting the previous location and restoring
     * its context.
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    void backward() {
    	if (isPerTabHistoryEnabled()) {
    		backwardForTab();
    		return;
    	}
        if (canBackward()) {
			shiftEntry(false);
		}
    }

    /*
     * Shift the history back or forward
     */
    private void shiftEntry(boolean forward) {
        updateEntry(getEntry(activeEntry));
        if (forward) {
			activeEntry++;
		} else {
			activeEntry--;
		}
        NavigationHistoryEntry entry = getEntry(activeEntry);
        if (entry != null) {
			gotoEntry(entry);
		}
    }

    /*
     * Shift the history to the given entry.
     * <p>
     * (Called by NavigationHistoryAction)
     * </p>
     */
    void shiftCurrentEntry(NavigationHistoryEntry entry, boolean forward) {
    	if (isPerTabHistoryEnabled()) {
    		gotoEntryForTab(entry, forward);
    		return;
    	}
        updateEntry(getEntry(activeEntry));
        activeEntry = history.indexOf(entry);
        gotoEntry(entry);
    }

    /**
     * Save the state of this history into the memento.
     */
    void saveState(IMemento memento) {
        NavigationHistoryEntry cEntry = getEntry(activeEntry);
        if (cEntry == null || !cEntry.editorInfo.isPersistable()) {
			return;
		}

        ArrayList editors = (ArrayList) this.editors.clone();
        for (Iterator iter = editors.iterator(); iter.hasNext();) {
            NavigationHistoryEditorInfo info = (NavigationHistoryEditorInfo) iter
                    .next();
            if (!info.isPersistable()) {
				iter.remove();
			}
        }
        IMemento editorsMem = memento
                .createChild(IWorkbenchConstants.TAG_EDITORS);
        for (Iterator iter = editors.iterator(); iter.hasNext();) {
            NavigationHistoryEditorInfo info = (NavigationHistoryEditorInfo) iter
                    .next();
            info.saveState(editorsMem
                    .createChild(IWorkbenchConstants.TAG_EDITOR));
        }

        ArrayList list = new ArrayList(history.size());
        int size = history.size();
        for (int i = 0; i < size; i++) {
            NavigationHistoryEntry entry = (NavigationHistoryEntry) history
                    .get(i);
            if (entry.editorInfo.isPersistable()) {
				list.add(entry);
			}
        }
        size = list.size();
        for (int i = 0; i < size; i++) {
            NavigationHistoryEntry entry = (NavigationHistoryEntry) list.get(i);
            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_ITEM);
            if (entry == cEntry) {
				childMem.putString(IWorkbenchConstants.TAG_ACTIVE, "true"); //$NON-NLS-1$
			}
            entry.saveState(childMem, list);
            childMem.putInteger(IWorkbenchConstants.TAG_INDEX, editors
                    .indexOf(entry.editorInfo));
        }
    }

    /**
     * Restore the state of this history from the memento.
     */
    void restoreState(IMemento memento) {
        IMemento editorsMem = memento.getChild(IWorkbenchConstants.TAG_EDITORS);
        IMemento items[] = memento.getChildren(IWorkbenchConstants.TAG_ITEM);
        if (items.length == 0 || editorsMem == null) {
            if (page.getActiveEditor() != null) {
				markLocation(page.getActiveEditor());
			}
            return;
        }

        IMemento children[] = editorsMem
                .getChildren(IWorkbenchConstants.TAG_EDITOR);
        NavigationHistoryEditorInfo editorsInfo[] = new NavigationHistoryEditorInfo[children.length];
        for (int i = 0; i < editorsInfo.length; i++) {
            editorsInfo[i] = new NavigationHistoryEditorInfo(children[i]);
            editors.add(editorsInfo[i]);
        }

        for (int i = 0; i < items.length; i++) {
            IMemento item = items[i];
            int index = item.getInteger(IWorkbenchConstants.TAG_INDEX)
                    .intValue();
            NavigationHistoryEditorInfo info = editorsInfo[index];
            info.refCount++;
            NavigationHistoryEntry entry = new NavigationHistoryEntry(info,
                    page, null, null);
            history.add(entry);
            entry.restoreState(item);
            if (item.getString(IWorkbenchConstants.TAG_ACTIVE) != null) {
				activeEntry = i;
			}
        }

        final NavigationHistoryEntry entry = getEntry(activeEntry);
        if (entry != null && entry.editorInfo.editorInput != null) {
            if (page.getActiveEditor() == page
                    .findEditor(entry.editorInfo.editorInput)) {
            	StartupThreading.runWithoutExceptions(new StartupRunnable() {

					public void runWithException() throws Throwable {
						gotoEntry(entry);
					}});		
			}
        }
    }

    public NavigationHistoryEntry createEntry(IWorkbenchPage page,
            IEditorPart part, INavigationLocation location) {
        String editorID = part.getSite().getId();
        IEditorInput editorInput = part.getEditorInput();
        NavigationHistoryEditorInfo info = null;
        for (Iterator iter = editors.iterator(); iter.hasNext();) {
            info = (NavigationHistoryEditorInfo) iter.next();
            if (editorID.equals(info.editorID)
                    && editorInput.equals(info.editorInput)) {
                info.refCount++;
                break;
            } else {
                info = null;
            }
        }
        if (info == null) {
            info = new NavigationHistoryEditorInfo(part);
            info.refCount++;
            editors.add(info);
        }
        return new NavigationHistoryEntry(info, page, part, location);
    }

    public void disposeEntry(NavigationHistoryEntry entry) {
        if (entry.editorInfo == null) {
			return;
		}
        entry.editorInfo.refCount--;
        if (entry.editorInfo.refCount == 0) {
			editors.remove(entry.editorInfo);
		}
        entry.dispose();
    }

    void checkDuplicates(NavigationHistoryEditorInfo info) {
        NavigationHistoryEditorInfo dup = null;
        if (info.editorInput == null) {
			return;
		}
        for (Iterator iter = editors.iterator(); iter.hasNext();) {
            dup = (NavigationHistoryEditorInfo) iter.next();
            if (info != dup && info.editorID.equals(dup.editorID)
                    && info.editorInput.equals(dup.editorInput)) {
				break;
			} else {
				dup = null;
			}
        }
        if (dup == null) {
			return;
		}
        for (Iterator iter = history.iterator(); iter.hasNext();) {
            NavigationHistoryEntry entry = (NavigationHistoryEntry) iter.next();
            if (entry.editorInfo == dup) {
                entry.editorInfo = info;
                info.refCount++;
            }
        }
        editors.remove(dup);
    }
    
    /*********************************************************/
    /*** new per-tab history code                          ***/
    /*********************************************************/
    
    
    private static class PerTabHistory {
    	LinkedList backwardEntries = new LinkedList();
    	NavigationHistoryEntry currentEntry = null;
    	LinkedList forwardEntries = new LinkedList();
    }
    
    private void setNewCurrentEntryForTab(PerTabHistory perTabHistory, NavigationHistoryEntry entry) {
    	if (perTabHistory.currentEntry != null) {
    		perTabHistory.backwardEntries.addFirst(perTabHistory.currentEntry);
    	}
    	perTabHistory.currentEntry = entry;
    	removeEntriesForTab(perTabHistory.forwardEntries);
    }
    
    private Object getCookieForTab(IEditorPart part) {
    	if (part != null) {
	        IWorkbenchPartSite site = part.getSite();
	        if (site instanceof PartSite) {
	        	PartSite partSite = (PartSite) site;
	        	WorkbenchPartReference ref = (WorkbenchPartReference) partSite.getPartReference();
	        	if (!ref.isDisposed()) {
	        		return partSite.getPane();
	        	}
	        }
    	}
    	return null;
    }
    
    private void markLocationForTab(IEditorPart part) {
    	if (part instanceof ErrorEditorPart) {
    		updateActions();
    		return;
    	}
		Object tabCookie = getCookieForTab(part);
		if (tabCookie != null) {
			INavigationLocation location = null;
			if (part instanceof INavigationLocationProvider) {
				location = ((INavigationLocationProvider) part)
						.createNavigationLocation();
			}
			PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap
					.get(tabCookie);
			if (perTabHistory == null) {
				perTabHistory = new PerTabHistory();
				perTabHistoryMap.put(tabCookie, perTabHistory);
			}
			NavigationHistoryEntry current = perTabHistory.currentEntry;
			if (current != null && current.editorInfo.memento != null) {
				current.editorInfo.restoreEditor();
				checkDuplicates(current.editorInfo);
			}
			NavigationHistoryEntry entry = createEntry(page, part, location);
			if (current != null && entry.mergeInto(current)) {
				disposeEntry(entry);
				removeEntriesForTab(perTabHistory.forwardEntries);
			} else {
				setNewCurrentEntryForTab(perTabHistory, entry);
			}
		}
		updateActions();
	}
    
    public void updateCookieForTab(Object oldCookie, Object newCookie) {
    	if (newCookie.equals(oldCookie)) {
    		return;
    	}
    	PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap.remove(oldCookie);
    	if (perTabHistory != null) {
    		perTabHistoryMap.put(newCookie, perTabHistory);
    	}
    }
    
    private void gotoEntryForTab(NavigationHistoryEntry target, boolean forward) {
    	Object editorTabCookie = getCookieForTab(page.getActiveEditor());
    	if (editorTabCookie!=null) {
	    	PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap.get(editorTabCookie);
	    	if (perTabHistory != null) {
	    		LinkedList source = forward ? perTabHistory.forwardEntries : perTabHistory.backwardEntries;
	    		LinkedList destination = forward ? perTabHistory.backwardEntries : perTabHistory.forwardEntries;
				if (perTabHistory.currentEntry != null) {
					if (perTabHistory.currentEntry.location != null) {
						perTabHistory.currentEntry.location.update();
					}
					destination.addFirst(perTabHistory.currentEntry);
				}
				NavigationHistoryEntry newCurrent = null;
	    		while (!source.isEmpty() && newCurrent==null) {
		    		NavigationHistoryEntry entry = (NavigationHistoryEntry) source
							.removeFirst();
		    		if (entry.equals(target)) {
		    			newCurrent = entry;
		    		} else {
		    			destination.addFirst(entry);
		    		}
	    		}
	    		Assert.isTrue(newCurrent != null);
	    		perTabHistory.currentEntry = newCurrent;
	            try {
	                ignoreEntries++;
	                if (newCurrent.editorInfo.memento != null) {
	                	newCurrent.editorInfo.restoreEditor();
	                	checkDuplicates(newCurrent.editorInfo);
	                }
	                newCurrent.restoreLocation();
	                updateActions();
	            } finally {
	            	ignoreEntries--;
	            }
	    	}
    	}
    }
    
	private void forwardForTab() {
    	Object editorTabCookie = getCookieForTab(page.getActiveEditor());
    	if (editorTabCookie!=null) {
	    	PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap.get(editorTabCookie);
	    	if (perTabHistory != null && !perTabHistory.forwardEntries.isEmpty()) {
	    		NavigationHistoryEntry newCurrent = (NavigationHistoryEntry) perTabHistory.forwardEntries
						.removeFirst();
	    		if (perTabHistory.currentEntry != null) {
	    			final INavigationLocation location = perTabHistory.currentEntry.location;
	    			if (location!=null) {
	    				location.update();
	    			}
	    			perTabHistory.backwardEntries.addFirst(perTabHistory.currentEntry);
	    		}
	    		perTabHistory.currentEntry = newCurrent;
	            try {
	                ignoreEntries++;
	                if (newCurrent.editorInfo.memento != null) {
	                	newCurrent.editorInfo.restoreEditor();
	                	checkDuplicates(newCurrent.editorInfo);
	                }
	                newCurrent.restoreLocation();
	                updateActions();
	            } finally {
	            	ignoreEntries--;
	            }
	    	}
    	}
    }
    
    private void backwardForTab() {
    	Object editorTabCookie = getCookieForTab(page.getActiveEditor());
    	if (editorTabCookie!=null) {
	    	PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap.get(editorTabCookie);
	    	if (perTabHistory != null && !perTabHistory.backwardEntries.isEmpty()) {
	    		NavigationHistoryEntry newCurrent = (NavigationHistoryEntry) perTabHistory.backwardEntries
	    				.removeFirst();
	    		if (perTabHistory.currentEntry != null) {
	    			perTabHistory.currentEntry.location.update();
	    			perTabHistory.forwardEntries.addFirst(perTabHistory.currentEntry);
	    		}
	    		perTabHistory.currentEntry = newCurrent;
	            try {
	                ignoreEntries++;
	                if (newCurrent.editorInfo.memento != null) {
	                	newCurrent.editorInfo.restoreEditor();
	                	checkDuplicates(newCurrent.editorInfo);
	                }
	                newCurrent.restoreLocation();
	                updateActions();
	            } finally {
	            	ignoreEntries--;
	            }
	    	}
    	}
    }
    
    private boolean hasEntriesForTab(boolean forward) {
    	Object editorTabCookie = getCookieForTab(page.getActiveEditor());
    	if (editorTabCookie!=null) {
	    	PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap.get(editorTabCookie);
	    	if (perTabHistory != null) {
		    	LinkedList entries = forward ? perTabHistory.forwardEntries : perTabHistory.backwardEntries;
		    	return !entries.isEmpty();
	    	}
    	}
    	return false;
    }

    /**
     * Returns entries in restore order.
     * @param editorTabCookie
     * @param forward
     * @return
     */
    private NavigationHistoryEntry[] getEntriesForTab(boolean forward) {
		Object editorTabCookie = getCookieForTab(page.getActiveEditor());
		if (editorTabCookie != null) {
			PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap
					.get(editorTabCookie);
			if (perTabHistory != null) {
				LinkedList entries = forward ? perTabHistory.forwardEntries
						: perTabHistory.backwardEntries;
				return (NavigationHistoryEntry[]) entries
				.toArray(new NavigationHistoryEntry[entries.size()]);
			}
		}
		return new NavigationHistoryEntry[0];
	}
    
    private void disposeHistoryForTabs() {
    	Object[] keys = perTabHistoryMap.keySet().toArray();
    	for (int i = 0; i < keys.length; i++) {
			disposeHistoryForTab(keys[i]);
		}
    }

    void disposeHistoryForTab(Object editorTabCookie) {
    	PerTabHistory perTabHistory = (PerTabHistory) perTabHistoryMap.remove(editorTabCookie);
    	if (perTabHistory != null) {
    		if (perTabHistory.currentEntry != null) {
    			disposeEntry(perTabHistory.currentEntry);
    			perTabHistory.currentEntry = null;
    		}
    		removeEntriesForTab(perTabHistory.backwardEntries);
    		removeEntriesForTab(perTabHistory.forwardEntries);
    	}
    }

	private void removeEntriesForTab(LinkedList entries) {
		for (Iterator it = entries.iterator(); it.hasNext();) {
			NavigationHistoryEntry entry = (NavigationHistoryEntry) it.next();
			disposeEntry(entry);
			it.remove();
		}
	}
}
