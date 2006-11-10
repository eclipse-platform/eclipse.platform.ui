/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeModel;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;
import org.osgi.framework.Bundle;

/**
 * Must provide access to legacy workbench and deferred workbench content
 * adapters.
 *
 */
public class LaunchTreeModel extends AsynchronousTreeModel {

    public LaunchTreeModel(AsynchronousTreeViewer viewer) {
        super(viewer);
    }

    protected IAsynchronousContentAdapter getContentAdapter(Object element) {
        IAsynchronousContentAdapter contentAdapter = super.getContentAdapter(element);
        if (contentAdapter != null) {
            return contentAdapter;
        }
        
        AsynchronousContentAdapter legacyAdapter = getLegacyAdapter(element);
        if (legacyAdapter != null) {
            return legacyAdapter;
        }
        
        return null;
    }

    /**
     * Returns a wrapper to the legacy workbench adapter if supported by the given object.
     * 
     * @param element
     * @return
     */
    private AsynchronousContentAdapter getLegacyAdapter(Object element) {
        if (element instanceof IDeferredWorkbenchAdapter) {
            return new WrappedDeferredWorkbenchTreeAdapter((IDeferredWorkbenchAdapter) element, element);
        }

        if (!(element instanceof IAdaptable)) {
            return null;
        }

        IAdaptable adaptable = (IAdaptable) element;
        IDeferredWorkbenchAdapter deferred = (IDeferredWorkbenchAdapter) adaptable.getAdapter(IDeferredWorkbenchAdapter.class);
        if (deferred != null) {
            DebugUIPlugin plugin = DebugUIPlugin.getDefault();
            Bundle bundle = plugin.getBundle(deferred.getClass());
            Bundle debugBundle = plugin.getBundle();
            if (!debugBundle.equals(bundle)) {
                // if client contributed, use it
                return new WrappedDeferredWorkbenchTreeAdapter(deferred, element);
            }
        }
        // if the client provided an IWorkbenchAdapter, use it
        IWorkbenchAdapter nonDeferred = (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
        if (nonDeferred != null) {
            DebugUIPlugin plugin = DebugUIPlugin.getDefault();
            Bundle bundle = plugin.getBundle(nonDeferred.getClass());
            Bundle debugBundle = plugin.getBundle();
            bundle = plugin.getBundle(nonDeferred.getClass());
            if (!debugBundle.equals(bundle)) {
                return new WrappedWorkbenchTreeAdapter(nonDeferred);
            }
        }       
        return null;
    }

    private class ElementCollector implements IElementCollector {
        List children = new ArrayList();

        public void add(Object element, IProgressMonitor monitor) {
            children.add(element);
        }

        public void add(Object[] elements, IProgressMonitor monitor) {
            for (int i = 0; i < elements.length; i++) {
                children.add(elements[i]);
            }
        }

        public void done() {
        }

        public Object[] getChildren() {
            return children.toArray();
        }

    }

    private class WrappedDeferredWorkbenchTreeAdapter extends AsynchronousContentAdapter {
        private IDeferredWorkbenchAdapter fAdapter;

        private Object fElement;

        public WrappedDeferredWorkbenchTreeAdapter(IDeferredWorkbenchAdapter adapter, Object element) {
            fAdapter = adapter;
            fElement = element;
        }

        protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
            ElementCollector elementCollector = new ElementCollector();
            fAdapter.fetchDeferredChildren(fElement, elementCollector, new NullProgressMonitor());
            return elementCollector.getChildren();
        }

        protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
            if (element instanceof IStackFrame) {
                return false;
            }
            return fAdapter.isContainer();
        }
        
        protected boolean supportsPartId(String id) {
            return IDebugUIConstants.ID_DEBUG_VIEW.equals(id);
        }       
    }

    private class WrappedWorkbenchTreeAdapter extends AsynchronousContentAdapter {
        private IWorkbenchAdapter fAdapter;

        public WrappedWorkbenchTreeAdapter(IWorkbenchAdapter adapter) {
            fAdapter = adapter;
        }

        protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
            return fAdapter.getChildren(parent);
        }

        protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
            if (element instanceof IStackFrame) {
                return false;
            }
            return fAdapter.getChildren(element).length > 0;
        }

        protected boolean supportsPartId(String id) {
            return IDebugUIConstants.ID_DEBUG_VIEW.equals(id);
        }

    }
    
}
