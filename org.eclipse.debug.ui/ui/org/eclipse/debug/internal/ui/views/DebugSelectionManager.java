package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ISelectionProvider;
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
	 * Returns a selection provider for the specified
	 * page and view.
	 * 
	 * @param page workbench page
	 * @param viewId view identifier
	 * @return selection provider
	 */
	public ISelectionProvider getSelectionProvider(IWorkbenchPage page, String viewId) {
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
		return provider;
	}
	
	/**
	 * Returns a selection provider for the specified
	 * window and view.
	 * 
	 * @param window workbench window
	 * @param viewId view identifier
	 * @return selection provider
	 */
	public ISelectionProvider getSelectionProvider(IWorkbenchWindow window, String viewId) {
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
		return provider;
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

}
