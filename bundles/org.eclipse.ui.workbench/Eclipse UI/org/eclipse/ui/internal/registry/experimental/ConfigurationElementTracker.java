/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry.experimental;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.1
 */
public class ConfigurationElementTracker implements IConfigurationElementTracker, IRegistryChangeListener {

	
	private Map configElementToObjectSetMap = new HashMap();
	
	private Set removalHandlerSet = new HashSet();
	
	private Set additionHandlerSet = new HashSet();
	
	/**
	 * 
	 */
	public ConfigurationElementTracker() {
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}
	
	public void registerAdditionHandler(IConfigurationElementAdditionHandler handler) {
		additionHandlerSet.add(handler);
	}
	
	public void unregisterAdditionHandler(IConfigurationElementAdditionHandler handler) {
		additionHandlerSet.remove(handler);
	}
	
	public void registerRemovalHandler(IConfigurationElementRemovalHandler handler) {
		removalHandlerSet.add(handler);
	}
	
	public void unregisterRemovalHandler(IConfigurationElementRemovalHandler handler) {
		removalHandlerSet.remove(handler);
	}
	
	public void registerObject(IConfigurationElement element, Object object) {
		Set objectSet = (Set) configElementToObjectSetMap.get(element);
		if (objectSet == null) {
			objectSet = new HashSet();
			configElementToObjectSetMap.put(element, objectSet);
		}
		
		//weakly refer to the object here so that when remove is called we dont need to search the config map
		objectSet.add(new WeakReference(object));
	}
	
	public void registryChanged(IRegistryChangeEvent event) {
        if (!PlatformUI.isWorkbenchRunning())
            return;
        int numDeltas = 0;
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display == null || display.isDisposed())
            return;
        try {
            // Just retrieve any changes relating to the extension point
            // org.eclipse.ui.perspectives
            IExtensionDelta delta[] = event.getExtensionDeltas();
            int len = delta.length;
            for (int i = 0; i < len; i++)
                switch (delta[i].getKind()) {
                case IExtensionDelta.ADDED:
                    doAdd(delta[i]);
                	++numDeltas;
                	break;
                case IExtensionDelta.REMOVED:
                    doRemove(display, delta[i]);
                	++numDeltas;
                	break;
                default:
                	break;
                }
        } finally {
            if (numDeltas > 0) {
                // Only do the post-change processing if something was
                // actually changed in the registry.  If there were no
                // deltas of relevance to this registry, there should be
                // no need to do any extra post-change processing.
                //postChangeProcessing();
            }
        }
	}

	/**
	 * @param display
	 * @param delta
	 */
	private void doRemove(Display display, IExtensionDelta delta) {
		IConfigurationElement [] elements = delta.getExtension().getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			final IConfigurationElement element = elements[i];
			Set objectSet = (Set) configElementToObjectSetMap.get(element);
			if (objectSet == null)
				continue; 
			
			for (Iterator j = objectSet.iterator(); j.hasNext();) {
				WeakReference reference = (WeakReference) j.next();
				final Object object = reference.get();
				if (object == null) {
					j.remove(); //drop empty references					
				}
				else {
					for (Iterator k = removalHandlerSet.iterator(); k.hasNext();) {
						final IConfigurationElementRemovalHandler handler = (IConfigurationElementRemovalHandler) k.next();
						display.syncExec(new Runnable() {

							public void run() { 
								handler.removeInstance(element, object);	
							}
						});						
					}
				}				
			}
			// if we've cleaned the last of the references, clean the config key
			if (objectSet.isEmpty()) { 
				configElementToObjectSetMap.remove(elements[i]);
			}
		}	
	}

	/**
	 * @param delta
	 */
	private void doAdd(IExtensionDelta delta) {
		IConfigurationElement [] elements = delta.getExtension().getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			for (Iterator j = additionHandlerSet.iterator(); j.hasNext();) {
				IConfigurationElementAdditionHandler handler = (IConfigurationElementAdditionHandler) j.next();
				handler.addInstance(this, elements[i]);
			}
		}		
	}
}
