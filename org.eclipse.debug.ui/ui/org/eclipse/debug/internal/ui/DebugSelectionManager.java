package org.eclipse.debug.internal.ui;

import java.util.HashMap;
import java.util.Hashtable;

import java.util.Iterator;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Manages the collection of debug selection providers
 * per workbench page. There is one selection manager
 * for the debug ui plug-in, which acts as a selection
 * service for all debug views. There is a debug selection
 * provider per debug view per workbench page. A debug
 * selection provider provides selection change notification
 * for a debug view in that page (if any).
 */
public class DebugSelectionManager implements IPageListener, ISelectionService, ISelectionChangedListener {
	
	/**
	 * Collection of debug view selection providers.
	 * table of <code>IWorkbenchPage</code> ->
	 * 	table of
	 * 		view identifiers (<code>String</code>) ->
	 * 			<code>ISelectionProvider</code>
	 */
	private HashMap fSelectionProviders = new HashMap(2);
	
	/**
	 * Collection of selection listerners (i.e. selection service)
	 */
	private ListenerList fListeners = new ListenerList(2);
	
	/** 
	 * The single debug selection manager.
	 */
	private static DebugSelectionManager fgManager;
	
	/**
	 * Constructs a new selection manager.
	 */
	private DebugSelectionManager() {
		fgManager = this;
	}
	
	/**
	 * Returns the debug selection manager
	 */
	public static DebugSelectionManager getDefault() {
		if (fgManager == null) {
			fgManager = new DebugSelectionManager();
		}
		return fgManager;
	}
	
	/**
	 * Diposes this debug selection manager and selection
	 * providers.
	 */
	public void dispose() {
		// remove page listeners and providers
		Iterator iter = fSelectionProviders.keySet().iterator();
		while (iter.hasNext()) {
			IWorkbenchPage page = (IWorkbenchPage)iter.next();
			page.getWorkbenchWindow().removePageListener(this);
			disposeSelectionProviders(page);
		}
		fSelectionProviders.clear();
	}

	/**
	 * When a page activates, add myself as a listener for
	 * debug selection in that page (to implement selection
	 * service for all pages). (Must to do this on open and
	 * activate, as a page that is open when the workbench
	 * starts is not re-opened).
	 * 
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		registerListeners(page);
	}

	/**
	 * Registeres selection listeners for add debug views
	 * in the given page.
	 * 
	 * @param page workbench page
	 */
	protected void registerListeners(IWorkbenchPage page) {
		//DebugUITools.getDebugViewSelectionProvider(page, IDebugUIConstants.ID_DEBUG_VIEW).addSelectionChangedListener(this);
		//DebugUITools.getDebugViewSelectionProvider(page, IDebugUIConstants.ID_PROCESS_VIEW).addSelectionChangedListener(this);
	}
	
	/**
	 * When a page closes, dispose its selection provider (if any)
	 * 
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		disposeSelectionProviders(page);
	}

	/**
	 * When a page opens, add myself as a listener for
	 * debug selection in that page (to implement selection
	 * service for all pages).
	 * 
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		registerListeners(page);
	}
	
	/**
	 * Sets a selection providers for the specified page.
	 * 
	 * @param page workbench page
	 * @param providers table of selection providers
	 */
	protected void setSelectionProviders(IWorkbenchPage page, HashMap providers) {
		fSelectionProviders.put(page, providers);
	}	

	/**
	 * Disposes the selection providerd for the given page, if any
	 * 
	 * @param page workbench page
	 */
	protected void disposeSelectionProviders(IWorkbenchPage page) {
		HashMap map = findSelectionProviders(page);
		if (map != null) {
			Iterator sps = map.values().iterator();
			while (sps.hasNext()) {
				((DebugSelectionProvider)sps.next()).dispose();
			}
			map.clear();
		}		
	}
	
	/**
	 * Creates and returns a selection provider for the given
	 * page and view
	 * 
	 * @param page workbench page
	 * @param id view identifier
	 * @return selection provider
	 */
	protected DebugSelectionProvider createSelectionProvider(IWorkbenchPage page, String id) {
		DebugSelectionProvider sp = new DebugSelectionProvider(page, id);
		HashMap map = findSelectionProviders(page);
		if (map == null) {
			map = new HashMap(2);
			setSelectionProviders(page, map);	
		}
		map.put(id, sp);
		page.getWorkbenchWindow().addPageListener(this);
		return sp;
	}
	
	/**
	 * Returns the selection providers for the given page,
	 * or <code>null</code> if none.
	 * 
	 * @param page
	 * @return table of selection providers
	 */
	protected HashMap findSelectionProviders(IWorkbenchPage page) {
		return (HashMap)fSelectionProviders.get(page);
	}
	
	/**
	 * Returns the debug selection provider for the given page and
	 * view, creating one if one does not yet exist.
	 * 
	 * @param page workbench page
	 * @param id view identifier
	 * @return selection provider for the debug view in the given
	 *  page
	 */
	public ISelectionProvider getSelectionProvider(IWorkbenchPage page, String id) {
		HashMap map = findSelectionProviders(page);
		DebugSelectionProvider sp = null;
		if (map != null) {
			sp = (DebugSelectionProvider)map.get(id);
		}
		if (sp == null) {
			sp = createSelectionProvider(page, id);
		}
		return sp;
	}
	
	/**
	 * @see ISelectionService#addSelectionListener(ISelectionListener)
	 */
	public void addSelectionListener(ISelectionListener listener) {
		fListeners.add(listener);
	}

	/**
	 * Not supported
	 * 
	 * @see ISelectionService#getSelection()
	 */
	public ISelection getSelection() {
		return null;
	}

	/**
	 * @see ISelectionService#removeSelectionListener(ISelectionListener)
	 */
	public void removeSelectionListener(ISelectionListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * This manager listeners for selection change events in all debug
	 * views, to act as a selection service.
	 * 
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		IWorkbenchPart part = ((LaunchesViewer)event.getSelectionProvider()).fView;
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((ISelectionListener)listeners[i]).selectionChanged(part, event.getSelection());
		}
		
	}

}
