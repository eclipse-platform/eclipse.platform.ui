/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.components.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @since 3.1
 */
public class ExtensionPointManager {
    
    private String namespace;
    private Map monitors = new HashMap();
    private IRegistryChangeListener listener = new IRegistryChangeListener() {

        public void registryChanged(IRegistryChangeEvent event) {
            IExtensionDelta[] deltas = event.getExtensionDeltas();
            
            for (int i = 0; i < deltas.length; i++) {
                IExtensionDelta delta = deltas[i];
                
                // Ignore extension points for other plugins
                IExtensionPoint point = delta.getExtensionPoint();
                if (!point.getNamespace().equals(namespace)) {
                    continue;
                }
                
                String extensionPoint = point.getSimpleIdentifier();
                
                List listeners = (List)monitors.get(extensionPoint);
                if (listeners != null) {
                    for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                        IExtensionPointMonitor monitor = (IExtensionPointMonitor) iter.next();
                        
                        notifyMonitor(monitor, delta.getExtension(), delta.getKind());                        
                    }
                }
            }
        }
        
    };
    
    private static void notifyMonitor(IExtensionPointMonitor monitor, IExtension extension, int kind) {
        if (kind == IExtensionDelta.ADDED) {
            monitor.added(extension);
        } else if (kind == IExtensionDelta.REMOVED) {
            monitor.removed(extension);
        }
    }
    
    public ExtensionPointManager(Bundle bundle) {
        this.namespace = bundle.getSymbolicName();
        Platform.getExtensionRegistry().addRegistryChangeListener(listener);
    }
    
    /**
     * Cleans up this object
     * 
     * @since 3.1
     */
    public void dispose() {
        Collection extensionPoints = monitors.keySet();
        for (Iterator iter = extensionPoints.iterator(); iter.hasNext();) {
            String pointId = (String) iter.next();
            
            List listeners = (List)monitors.get(pointId);
            
            for (Iterator iter2 = listeners.iterator(); iter2.hasNext();) {
                IExtensionPointMonitor monitor = (IExtensionPointMonitor) iter2.next();
                
                loadExtensionPoint(pointId, monitor, IExtensionDelta.REMOVED);
            }
        }
        
        Platform.getExtensionRegistry().removeRegistryChangeListener(listener);
    }
    
    /**
     * Adds the given monitor. The monitor will be notified about all extensions that extend the
     * given extension point.
     * 
     * @param extensionPoint
     * @param newMonitor
     * @since 3.1
     */
    public void addMonitor(String extensionPoint, IExtensionPointMonitor newMonitor) {
        List listeners = (List)monitors.get(extensionPoint);
        
        if (listeners == null) {
            listeners = new ArrayList();
        }
        
        if (listeners.contains(newMonitor)) {
            return;
        }
        
        listeners.add(newMonitor);
        loadExtensionPoint(extensionPoint, newMonitor, IExtensionDelta.ADDED);
    }
    
    /**
     * Removes the monitor. oldMonitor.removed(...) will be called for
     * any extension on which oldMonitor.added(...) had previously been called.
     * 
     * @param extensionPoint
     * @param oldMonitor
     * @since 3.1
     */
    public void removeMonitor(String extensionPoint, IExtensionPointMonitor oldMonitor) {
        List listeners = (List)monitors.get(extensionPoint);
        
        if (listeners == null) {
            return;
        }
        
        if (!listeners.contains(oldMonitor)) {
            return;
        }
        
        listeners.remove(oldMonitor);
        if (listeners.isEmpty()) {
            monitors.remove(extensionPoint);
        }
        
        loadExtensionPoint(extensionPoint, oldMonitor, IExtensionDelta.REMOVED);
    }
    
    private void loadExtensionPoint(String pointId, IExtensionPointMonitor monitor, int kind) {
        
        // Parse the extension point to construct a factory for all services
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(namespace, pointId);
        
        IExtension[] extensions = extensionPoint.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            IExtension extension = extensions[i];

            notifyMonitor(monitor, extension, kind);            
        }
    }
    
}
