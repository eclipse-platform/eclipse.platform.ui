package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
 
/**
 * Manages debug selection providers for all pages and windows
 */
public class DebugSelectionManager implements IPageListener {
	
	/**
	 * Table of selection providers by page
	 * Map of {<code>IWorkbenchPage</code> ->
	 * {<code>Map</code> of
	 * <code>String</code> (view id) ->
	 * <code>DebugPageSelectionProvider</code>}}
	 */
	private Map fProvidersByPage;
	
	/**
	 * Table of selection providers by window
	 * Map of {<code>IWorkbenchWindow</code> ->
	 * {<code>Map</code> of
	 * <code>String</code> (view id) ->
	 * <code>DebugWindowSelectionProvider</code>}}
	 */
	private Map fProvidersByWindow;	
	
	/**
	 * Singleton selection manager
	 */
	private static DebugSelectionManager fgDefault;
	
	/**
	 * Constructs a new selection manager
	 */
	private DebugSelectionManager() {
		fProvidersByPage = new HashMap(4);
		fProvidersByWindow = new HashMap(4);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPageListener(this);
	}
	
	/**
	 * Returns the selection manager
	 */
	public static DebugSelectionManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new DebugSelectionManager();
		}
		return fgDefault;
	}
	
	/**
	 * Add tge given listener to the selection provider for the specified
	 * page and view.
	 * 
	 * @param listener selection listener
	 * @param page workbench page
	 * @param viewId view identifier
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener, IWorkbenchPage page, String viewId) {
		Map map = getSelectionProviders(page);
		if (map == null) {
			map = new HashMap(2);
			setSelectionProviders(page, map);
		}
		ISelectionProvider provider = (ISelectionProvider)map.get(viewId);
		if (provider == null) {
			provider = new DebugPageSelectionProvider(page, viewId);
			map.put(viewId, provider);
		}
		provider.addSelectionChangedListener(listener);
	}
	
	/**
	 * Removes the given listener from the selection provider for
	 * the specified page and view
	 * 
	 * @param listener selection listener
	 * @param page workbench page
	 * @param viewId view identifier
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener, IWorkbenchPage page, String viewId) {
		Map map = getSelectionProviders(page);
		if (map != null) {
			ISelectionProvider provider = (ISelectionProvider)map.get(viewId);
			if (provider != null) {
				provider.removeSelectionChangedListener(listener);
			}
		}
	}		
	
	/**
	 * Adds the given listener to the selection provider for
	 * the specified window and view (i.e. all pages in the window)
	 * 
	 * @param listener selection listener
	 * @param window workbench window
	 * @param viewId view identifier
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener, IWorkbenchWindow window, String viewId) {
		Map map = getSelectionProviders(window);
		if (map == null) {
			map = new HashMap(2);
			setSelectionProviders(window, map);
		}
		ISelectionProvider provider = (ISelectionProvider)map.get(viewId);
		if (provider == null) {
			provider = new DebugWindowSelectionProvider(window, viewId);
			map.put(viewId, provider);
		}
		provider.addSelectionChangedListener(listener);
	}
	
	/**
	 * Removes the given listener from the selection provider for
	 * the specified window and view (i.e. all pages in the window)
	 * 
	 * @param listener selection listener
	 * @param window workbench window
	 * @param viewId view identifier
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener, IWorkbenchWindow window, String viewId) {
		Map map = getSelectionProviders(window);
		if (map != null) {
			ISelectionProvider provider = (ISelectionProvider)map.get(viewId);
			if (provider != null) {
				provider.removeSelectionChangedListener(listener);
			}
		}
	}		
	
	/**
	 * Returns the known selection providers for the specified
	 * page, or <code>null</code> if none.
	 * 
	 * @param page workbench page
	 * @return map of selection providers, keyed by view id,
	 * 	or <code>null</code>
	 */
	protected Map getSelectionProviders(IWorkbenchPage page) {
		return (Map)fProvidersByPage.get(page);
	}
	
	/**
	 * Sets the selection providers for the specified
	 * page, or <code>null</code> if none.
	 * 
	 * @param page workbench page
	 * @param providers map of selection providers, keyed by view id,
	 * 	or <code>null</code>
	 */
	private void setSelectionProviders(IWorkbenchPage page, Map providers) {
		if (providers == null) {
			fProvidersByPage.remove(page);
		} else {
			fProvidersByPage.put(page, providers);
		}
	}	
	
	/**
	 * Returns the known selection providers for the specified
	 * window, or <code>null</code> if none.
	 * 
	 * @param window workbench window
	 * @return map of selection providers, keyed by view id,
	 * 	or <code>null</code>
	 */
	protected Map getSelectionProviders(IWorkbenchWindow window) {
		return (Map)fProvidersByWindow.get(window);
	}
	
	/**
	 * Sets the selection providers for the specified
	 * window, or <code>null</code> if none.
	 * 
	 * @param window workbench window
	 * @param providers map of selection providers, keyed by view id,
	 * 	or <code>null</code>
	 */
	private void setSelectionProviders(IWorkbenchWindow window, Map providers) {
		if (providers == null) {
			fProvidersByWindow.remove(window);
		} else {
			fProvidersByWindow.put(window, providers);
		}
	}		
	

	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		Map map = getSelectionProviders(page);
		if (map != null) {
			Iterator providers = map.values().iterator();
			while (providers.hasNext()) {
				((DebugPageSelectionProvider)providers.next()).dispose();
			}
			map.clear();
			setSelectionProviders(page, null);
		}
	}

	/*
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
	}
	
	/**
	 * Notifies this manager that the given view has been
	 * created (and unerlying view), and should now have
	 * its selection provider registered if any listeners
	 * have registered for its selection.
	 * 
	 * @param view debug view
	 */
	public void registerView(IDebugViewAdapter view) {
		IWorkbenchPage page = view.getSite().getPage();
		Map map = getSelectionProviders(page);
		if (map != null) {
			DebugPageSelectionProvider sp = (DebugPageSelectionProvider)map.get(view.getSite().getId());
			if (sp != null) {
				sp.partOpened(view);
			}
		}
			
	}
	
	/**
	 * Returns the current selection in a page in the specified
	 * view type.
	 * 
	 * @param page workbench page
	 * @param viewId view identifier
	 * @return selection
	 */
	public ISelection getSelection(IWorkbenchPage page, String viewId) {
		Map map = getSelectionProviders(page);
		if (map != null) {
			ISelectionProvider sp = (ISelectionProvider)map.get(viewId);
			if (sp != null) {
				return sp.getSelection();
			}
		}
		return new StructuredSelection();
	}

}
