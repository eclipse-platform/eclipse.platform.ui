package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.InspectItem;
import org.eclipse.debug.internal.ui.InspectorView;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * This class provides utilities for clients of the debug UI.
 * <p>
 * Images retrieved from this facility should not be disposed.
 * The images will be disposed when this plugin is shutdown.
 * </p>
 * <p>
 * This class is not intended to be subclassed or instantiated.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public class DebugUITools {

	private static DefaultLabelProvider fgDefaultLabelProvider= new DefaultLabelProvider();

	/**
	 * Shows and selects the given value in an inspector view with the specified label.
	 * If an inspector view is not in the current workbench page, an inspector view
	 * is created. This method must be called in the UI thread.
	 *
	 * @param label a localized label to associate with the value in the inspector view 
	 * @param value the value to display  
	 */
	public static void inspect(String label, IValue value) {
		IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p == null) {
			return;
		}
		IWorkbenchPart activePart= p.getActivePart();
		InspectorView view= (InspectorView) p.findView(IDebugUIConstants.ID_INSPECTOR_VIEW);
		if (view == null) {
			// open a new view
			try {
				view= (InspectorView) p.showView(IDebugUIConstants.ID_INSPECTOR_VIEW);
			} catch (PartInitException e) {
				DebugUIPlugin.logError(e);
				return;
			} finally {
				p.activate(activePart);
			}
		}
		InspectItem item = new InspectItem(label, value);
		view.addToInspector(item);
		p.bringToTop(view);
	}
	
	/**
	 * Returns the shared image managed under the given key. If there isn't any image
	 * associated with the given key, <code>null</code> is returned. <p>
	 * Note that clients <br>MUST NOT</br> dispose the image returned by this method.
	 * <p>
	 * See <code>IDebugUIConstants</code> for available images.
	 * </p>
	 *
	 * @param key the image key
	 * @return the image, or <code>null</code> if none
	 * @see IDebugUIConstants
	 */
	public static Image getImage(String key) {
		return DebugPluginImages.getImage(key);
	}
	
	/**
	 * Returns the shared image descriptor managed under the given key. If there isn't any image
	 * descriptor associated with the given key, <code>null</code> is returned. <p>
	 * <p>
	 * See <code>IDebugUIConstants</code> for available image descriptors.
	 * </p>
	 *
	 * @param key the image descriptor key
	 * @return the image descriptor, or <code>null</code> if none
	 * @see IDebugUIConstants
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return DebugPluginImages.getImageDescriptor(key);
	}
	
	/**
	 * Returns the default label provider for the debug UI.
	 */
	public static ILabelProvider getDefaultLabelProvider() {
		return fgDefaultLabelProvider;
	}
	
	/**
	 * Returns the default image descriptor for the given element
	 * or <code>null</code> if none is defined.
	 */
	public static ImageDescriptor getDefaultImageDescriptor(Object element) {
		String imageKey= getDefaultImageKey(element);
		if (imageKey == null) {
			return null;
		}
		return DebugPluginImages.getImageDescriptor(imageKey);
	}
	
	private static String getDefaultImageKey(Object element) {
		return fgDefaultLabelProvider.getDefaultImageKey(element);
	}
	
	/**
	 * Returns the preference store for the debug UI plugin.
	 *
	 * @return preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * Adds the given filter to the list of registered filters. Has
	 * no effect if an identical filter is already registered.
	 */
	public static void addEventFilter(IDebugUIEventFilter filter) {
		DebugUIPlugin.getDefault().addEventFilter(filter);
	}
	
	/**
	 * Removes the given filter from the list of registered filters. Has
	 * no effect if an identical filter is not already registered.
	 */
	public static void removeEventFilter(IDebugUIEventFilter filter) {
		DebugUIPlugin.getDefault().removeEventFilter(filter);
	}
	
	/**
	 * Returns a new debug model presentation that delegates to
	 * appropriate debug models.
	 * 
	 * It is the client's responsibility to ensure to call the dispose
	 * method of the returned debug model presentation.
	 * 
	 * @see IBaseLabelProvider#dispose()
	 * @return a debug model presentation
	 */
	public static IDebugModelPresentation newDebugModelPresentation() {
		return new DelegatingModelPresentation();
	}
	
	/**
	 * Returns the currently selected debug element in the 
	 * debug view of the current workbench page, or <code>null</code>
	 * if there is no current debug context.
	 * 
	 * @return the currently selected debug context, or <code>null</code>
	 */
	public static IDebugElement getDebugContext() {
		return DebugUIPlugin.getDefault().getDebugContext();
	}
	
	/**
	 * Returns the process associated with the current debug context.
	 * If there is no debug context currently, the most recently
	 * launched process is returned. If there is no current process
	 * <code>null</code> is returned.
	 * 
	 * @return the current process, or <code>null</code>
	 */
	public static IProcess getCurrentProcess() {
		return DebugUIPlugin.getDefault().getCurrentProcess();
	}
}

