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
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ExtensionEventHandlerMessages;

/**
 * TODO: clean up method names and visibility
 * TODO: clean up access to 'real objects' in the registry object.  Should the collection be mutable directly or should it all be done via accessors?
 * TODO: examine Collections API usage to determine proper interfaces to use 
 * TODO: usage of syncExec is scary.  Can we get away with asyncExec?
 * TODO: determine whether the reset perspective code belongs here or in a subclass
 * TODO: rename to AbstractRegistryManager ?  Extract a meaningful interface?
 * TODO: can we remove the REGISTRY_CACHE_* flags?  The only one that seems interesting ATM is deleted
 * TODO: why is null being returned from getRegistryObjects when the array is empty?  It'd be easier to return the empty array - then the caller doesnt need to handle the null case
 * TODO: add exception handling in the do* methods so that one bad extension doesn't kill processing of an entire delta
 * TODO: add documentation!
 * TODO: add Comparator so that getRegistryObjects returns an ordered array?
 * 
 * @since 3.1
 */
public abstract class RegistryManager implements IRegistryChangeListener {
    private String elementId;

    private String extPtId;

    private Map cache;

    public static final int REGISTRY_CACHE_STATE_UNKNOWN = 0;

    public static final int REGISTRY_CACHE_STATE_ACTIVE = 1;

    public static final int REGISTRY_CACHE_STATE_DELETED = 2;

    public static final int REGISTRY_CACHE_STATE_MAX = 2;

    public static final String INTERNAL_REGISTRY_ADDITION = "InternalRegistryAddition"; //$NON-NLS-1$

    protected List changeList = new ArrayList(10);

    public RegistryManager(String elementId, String extPtId) {
        this.elementId = elementId;
        this.extPtId = extPtId;
        cache = new HashMap();
    }

    public void addResetMessage(String msg) {
        changeList.add(msg);
    }

    public Object[] getRegistryObjects() {
        Object[] regElements = cache.values().toArray();
        if (regElements.length == 0)
            return null;
        ArrayList retList = new ArrayList();
        for (int i = 0; i < regElements.length; i++) {
            List listElement = ((RegistryElement) regElements[i])
                    .getRealObjects();
            if (listElement != null) {
                ListIterator iter = listElement.listIterator();
                while (iter.hasNext()) {
                    retList.add(iter.next());
                }
            }
        }
        retList.trimToSize();
        Object[] ret = retList.toArray();
        return ret;
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
            IExtensionDelta delta[] = event.getExtensionDeltas(elementId,
                    extPtId);
            int len = delta.length;
            for (int i = 0; i < len; i++)
                switch (delta[i].getKind()) {
                case IExtensionDelta.ADDED:
                    doAdd(display, delta[i]);
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
                postChangeProcessing();
            }
        }
    }

    private void doAdd(Display display, final IExtensionDelta delta) {
        Runnable run = new Runnable() {
            public void run() {
                if (!PlatformUI.isWorkbenchRunning())
                    return;
                add(delta);
            }
        };
        display.syncExec(run);
    }

    public void add(IExtensionDelta delta) {
        IExtension ext = delta.getExtension();
        // Get the name of the plugin that is adding this extension.  The
        // name of the plugin that adds the extension point is us.
        String pluginId = ext.getNamespace();
        add(buildNewCacheObject(delta), pluginId);
    }

    public void add(Object element, String pluginId) {
        if (element == null)
            // Nothing to add, so just return.
            return;
        String toUsePluginId = pluginId;
        if (pluginId == null || pluginId.length() == 0) {
            // This element is being added to the registry but is not
            // associated with a regular plug-in.  Likely, the element
            // was created programmatically and needs to exist in the
            // registry.  An example is the 'Other' category for views.
            // Use the key INTERNAL_REGISTRY_ADDITION for these elements so
            // they will not be removed from the registry with any plug-in
            // removal.
            toUsePluginId = INTERNAL_REGISTRY_ADDITION;
        }
        RegistryElement regElement = (RegistryElement) cache.get(toUsePluginId);
        if (regElement == null) {
            regElement = new RegistryElement(element);
            cache.put(toUsePluginId, regElement);
        } else {
            regElement.addNewObject(element);
        }
    }

    public void add(Object[] elements, String pluginId) {
        if (elements != null)
	        for (int i = 0; i < elements.length; i++)
	            add(elements[i], pluginId);
    }

    /**
     * This is a generic method that is expected to be over-written by the
     * parent class.  It should return an array of new elements with all 
     * the relevant information from the delta.
     * @param delta a delta from a listener on extension events
     * @return a new object to be added to the registry cache
     */
    abstract public Object[] buildNewCacheObject(IExtensionDelta delta);

	/**
	 * @since 3.1
	 */
    private void doRemove(Display display, final IExtensionDelta delta) {
        Runnable run = new Runnable() {
            public void run() {
                if (!PlatformUI.isWorkbenchRunning())
                    return;
                remove(delta);
            }
        };
        display.syncExec(run);
    }

    public void remove(IExtensionDelta delta) {
        IExtension ext = delta.getExtension();
        // Get the name of the plugin that is adding this extension.  The
        // name of the plugin that adds the extension point is us.
        String pluginId = ext.getNamespace();
        removeBundle(pluginId);
    }

    /**
     * Subclasses can override this to be given an opportunity to cleanup after a
     * registry removal.  E.g., the PerspectiveRegistry has chosen to remove all
     * open perspectives contributed by the removed bundle.  The object will have
     * been removed from the registry by the time this method is called.
	 * @since 3.1
     */
    protected void handleBundleRemoved(String bundleId, Object registryObject) {
        // subclasses to override
    }

    /**
     * This is a generic method that is expected to be implemented by the
     * parent class.  It should do any processing necessary once deltas
     * have been processed and the registry modified.  Note that in some
     * cases there may be no extra processing required.
     */
    public void postChangeProcessing() {
        resetCurrentPerspective(PlatformUI.getWorkbench().getDisplay());
    }

    private void resetCurrentPerspective(Display display) {
        if (display == null || changeList.isEmpty())
            return;

        final StringBuffer message = new StringBuffer(
                ExtensionEventHandlerMessages
                        .getString("ExtensionEventHandler.following_changes")); //$NON-NLS-1$

        for (Iterator i = changeList.iterator(); i.hasNext();) {
            message.append(i.next());
        }

        message.append(ExtensionEventHandlerMessages
                .getString("ExtensionEventHandler.need_to_reset")); //$NON-NLS-1$

        display.asyncExec(new Runnable() {
            public void run() {
                Shell parentShell = null;
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null) {
                    if (workbench.getWorkbenchWindowCount() == 0)
                        return;
                    window = workbench.getWorkbenchWindows()[0];
                }

                parentShell = window.getShell();

                if (MessageDialog
                        .openQuestion(
                                parentShell,
                                ExtensionEventHandlerMessages
                                        .getString("ExtensionEventHandler.reset_perspective"), message.toString())) { //$NON-NLS-1$
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null)
                        page.resetPerspective();
                }

                // the message was displayed so reset the change list
                changeList.clear();
            }
        });

    }

    /**
     * Flag a series of elements in this registry cache as 'to be removed'.
     * This does not actually remove these elements as some processing may
     * be required before they are removed.
     * 
     * @param plugins a list of plug-in ids for each plug-in that is being
     * removed.  Only those elements in the registry cache that correspond
     * to one of these plug-ins will be flagged for removal.
     */
    public void remove(String[] plugins) {
        for (int i = 0; i < plugins.length; i++)
            remove(plugins[i]);
    }

    public void remove(String pluginId) {
        RegistryElement element = (RegistryElement) cache.get(pluginId);
        if (element != null)
            element.changeState(REGISTRY_CACHE_STATE_DELETED);
    }

	/**
	 * @since 3.1
	 */
    public void removeBundle(String pluginId) {
        if (pluginId == null)
            pluginId = INTERNAL_REGISTRY_ADDITION;

        RegistryElement regElement = (RegistryElement) cache.get(pluginId);
        if (regElement == null)
            return;

        // remove the elements before invoking the callback
        cache.remove(pluginId);

        // let the subclasses take action as needed
        Iterator i = regElement.getRealObjects().iterator();
        while (i.hasNext())
            handleBundleRemoved(pluginId, i.next());
    }

    public void remove(String pluginId, Object object) {
        if (pluginId == null)
            pluginId = INTERNAL_REGISTRY_ADDITION;
        RegistryElement element = (RegistryElement) cache.get(pluginId);
        if (element != null) {
            // Find the registry element that contains this object and
            // remove only that one.
            List realObjects = element.getRealObjects();
            realObjects.remove(object);
        }
    }

    /**
     * Actually go through and remove any elements from the registry that
     * are flagged for removal.  This assumes that any processing required
     * by the parent (child?) prior to removal of this element has been 
     * completed.
     */
    public void cleanRegistry() {
        Set elements = cache.keySet();
        Set keysToRemove = new HashSet();
        Iterator iter = elements.iterator();

        while (iter.hasNext()) {
            Object pluginId = iter.next();
            RegistryElement elem = (RegistryElement) cache.get(pluginId);
            if (elem != null && elem.getState() == REGISTRY_CACHE_STATE_DELETED) {
                keysToRemove.add(pluginId);
            }
        }

        // Now remove the deleted ones
        Iterator removeIterator = keysToRemove.iterator();
        while (removeIterator.hasNext()) {
            cache.remove(removeIterator.next());
        }
    }
}

class RegistryElement {
    private int state;

    private List realObjects = null;

    public RegistryElement(Object obj) {
        state = RegistryManager.REGISTRY_CACHE_STATE_UNKNOWN;
        if (realObjects == null) {
            realObjects = new ArrayList();
        }
        realObjects.add(obj);
    }

    public void addNewObject(Object obj) {
        if (realObjects == null) {
            realObjects = new ArrayList();
        }
        realObjects.add(obj);
    }

    public int getState() {
    	return state;
    }
    
    public void changeState(int newState) {
        if ((newState > RegistryManager.REGISTRY_CACHE_STATE_UNKNOWN)
                && (newState <= RegistryManager.REGISTRY_CACHE_STATE_MAX))
            state = newState;
    }

    public List getRealObjects() {
        return realObjects;
    }
}
