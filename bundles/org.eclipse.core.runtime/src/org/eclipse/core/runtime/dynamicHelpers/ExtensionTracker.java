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
package org.eclipse.core.runtime.dynamicHelpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

/**
 * Implementation of the IExtensionTracker. 
 * This API is EXPERIMENTAL and provided as early access.
 * @since 3.1
 */
public class ExtensionTracker implements IExtensionTracker, IRegistryChangeListener {
    private Map extensionToObjects = new HashMap();

    private Set removalHandlers = new HashSet();

    private Set additionHandlers = new HashSet();

    private static final Object[] EMPTY_ARRAY = new Object[0];

    public ExtensionTracker() {
        Platform.getExtensionRegistry().addRegistryChangeListener(this);
    }

    public void registerAdditionHandler(IExtensionAdditionHandler handler) {
        additionHandlers.add(handler);
    }

    public void unregisterAdditionHandler(IExtensionAdditionHandler handler) {
        additionHandlers.remove(handler);
    }

    public void registerRemovalHandler(IExtensionRemovalHandler handler) {
        removalHandlers.add(handler);
    }

    public void unregisterRemovalHandler(IExtensionRemovalHandler handler) {
        removalHandlers.remove(handler);
    }

    public void registerObject(IExtension element, Object object, int referenceType) {
        if (element == null || object == null)
            return;
        
        ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.get(element);
        if (associatedObjects == null) {
            associatedObjects = new ReferenceHashSet();
            extensionToObjects.put(element, associatedObjects);
        }

        associatedObjects.add(object, referenceType);
    }

    public void registryChanged(IRegistryChangeEvent event) {
        IExtensionDelta delta[] = event.getExtensionDeltas();
        int len = delta.length;
        for (int i = 0; i < len; i++)
            switch (delta[i].getKind()) {
            case IExtensionDelta.ADDED:
                doAdd(delta[i]);
                break;
            case IExtensionDelta.REMOVED:
                doRemove(delta[i]);
                break;
            default:
                break;
            }
    }

    private void doAdd(IExtensionDelta delta) {
        if (additionHandlers == null || additionHandlers.isEmpty())
            return;

        for (Iterator j = additionHandlers.iterator(); j.hasNext();) {
            IExtensionAdditionHandler handler = (IExtensionAdditionHandler) j.next();
            if (handler.getExtensionPointFilter() == null || handler.getExtensionPointFilter().equals(delta.getExtensionPoint())) {
                applyAdd(handler, delta.getExtension());
            }
        }
    }

    protected void applyAdd(IExtensionAdditionHandler handler, IExtension extension) {
        handler.addInstance(this, extension);
    }

    private void doRemove(IExtensionDelta delta) {
        IExtension removedExtension = delta.getExtension();
        ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.remove(removedExtension);
        if (associatedObjects == null)
            return;

        // No one is listening. Simply remove the objects
        if (removalHandlers == null || removalHandlers.isEmpty())
            return;

        // Find the objects that have not been gc'ed, and notify the handlers
        Object[] removedObjects = associatedObjects.toArray();
        for (Iterator k = removalHandlers.iterator(); k.hasNext();) {
            applyRemove((IExtensionRemovalHandler) k.next(), removedExtension, removedObjects);
        }
    }

    protected void applyRemove(IExtensionRemovalHandler handler, IExtension removedExtension, Object[] removedObjects) {
        handler.removeInstance(removedExtension, removedObjects);
    }

    public Object[] getObjects(IExtension element) {
        ReferenceHashSet objectSet = (ReferenceHashSet) extensionToObjects.get(element);
        if (objectSet == null)
            return EMPTY_ARRAY;

        return objectSet.toArray();
    }

    public void close() {
        Platform.getExtensionRegistry().removeRegistryChangeListener(this);
        extensionToObjects.clear();
    }

    public void unregisterObject(IExtension extension, Object object) {
        ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.get(extension);
        if (associatedObjects != null)
            associatedObjects.remove(object);
    }

    public Object[] unregisterObject(IExtension extension) {
        ReferenceHashSet associatedObjects = (ReferenceHashSet) extensionToObjects.remove(extension);
        if (associatedObjects == null)
            return EMPTY_ARRAY;

        return associatedObjects.toArray();
    }
}
