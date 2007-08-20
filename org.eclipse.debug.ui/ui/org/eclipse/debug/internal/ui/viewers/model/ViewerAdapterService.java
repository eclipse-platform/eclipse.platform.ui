/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.internal.ui.views.launch.DebugElementAdapterFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Helper class to retrieve adapters associated with viewers.
 * 
 * @since 3.4
 */
public class ViewerAdapterService {

	
	 /**
     * Returns the content provider for the given element or
     * <code>null</code> if none.
     * 
     * @param element element to retrieve adapter for
     * @return content provider or <code>null</code>
     */
    public static IElementContentProvider getContentProvider(Object element) { 
    	return (IElementContentProvider)getAdapter(element, IElementContentProvider.class);
    }
    
    /**
     * Returns the label provider for the given element or
     * <code>null</code> if none.
     * 
     * @param element element to retrieve adapter for
     * @return label provider or <code>null</code>
     */
    public static IElementLabelProvider getLabelProvider(Object element) {        
    	return (IElementLabelProvider)getAdapter(element, IElementLabelProvider.class);
    }		    
    
    /**
     * Returns the column presentation factory for the given element or <code>null</code>.
     * 
     * @param element element to retrieve adapter for
     * @return column presentation factory of <code>null</code>
     */
    public static IColumnPresentationFactory getColumnPresentationFactory(Object element) {
    	return (IColumnPresentationFactory)getAdapter(element, IColumnPresentationFactory.class);
    }    
    
	/**
	 * Returns the model proxy factory for the given element or
	 * <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapter for
	 * @return model proxy factory or <code>null</code>
	 */
	public static IModelProxyFactory getModelProxyFactory(Object element) {
		return (IModelProxyFactory)getAdapter(element, IModelProxyFactory.class);
	}    
    
	/**
	 * Returns the memento provider for the given element or
	 * <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapter for
	 * @return memento provider or <code>null</code>
	 */
	public static IElementMementoProvider getMementoProvider(Object element) {
		return (IElementMementoProvider)getAdapter(element, IElementMementoProvider.class);
	}	
	
    /**
     * Returns the element editor for the given element or <code>null</code>.
     * 
     * @param element element to retrieve adapter for
     * @return element editor or <code>null</code>
     */
    public static IElementEditor getElementEditor(Object element) {
    	return (IElementEditor)getAdapter(element, IElementEditor.class);
    } 	
    
	/**
	 * Creates and returns the selection policy associated with the given selection
	 * or <code>null</code> if none.
	 * 
	 * @param selection or <code>null</code>
	 * @param context presentation context
	 * @return selection policy or <code>null</code>
	 */
	public static IModelSelectionPolicy getSelectionPolicy(ISelection selection, IPresentationContext context) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			IModelSelectionPolicyFactory factory = (IModelSelectionPolicyFactory) getAdapter(element, IModelSelectionPolicyFactory.class);
			if (factory != null) {
				return factory.createModelSelectionPolicyAdapter(element, context);
			}
		}
		return null;
	}    
	
    /**
     * Returns the viewer input provider for the given element or
     * <code>null</code> if none.
     * 
     * @param element element to retrieve adapter for
     * @return viewer input provider or <code>null</code>
     */
    public static IViewerInputProvider getInputProvider(Object element) {        
    	return (IViewerInputProvider)getAdapter(element, IViewerInputProvider.class);
    }			
	
	/**
	 * Returns an adapter of the specified type for the given object or <code>null</code>
	 * if none. The object itself is returned if it is an instance of the specified type.
	 * If the object is adaptable but does not subclass <code>PlatformObject</code>, the default
	 * debug element adapter factory is consulted for an adapter.
	 * 
	 * @param element element to retrieve adapter for
	 * @param type adapter type
	 * @return adapter or <code>null</code>
	 */
	private static Object getAdapter(Object element, Class type) {
    	Object adapter = null;
    	if (type.isInstance(element)) {
			return element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = adaptable.getAdapter(type);
			if (adapter == null && !(element instanceof PlatformObject)) {
    	    	// for objects that don't properly subclass PlatformObject to inherit default
        		// adapters, just delegate to the adapter factory
	        	adapter = new DebugElementAdapterFactory().getAdapter(element, type);
	        }
		}
    	return adapter;		
	}
}
