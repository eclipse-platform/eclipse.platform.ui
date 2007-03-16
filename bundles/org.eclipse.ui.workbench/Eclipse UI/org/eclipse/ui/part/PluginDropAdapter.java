/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Adapter for adding handling of the <code>PluginTransfer</code> drag and drop
 * transfer type to a drop action.
 * <p>
 * This class may be instantiated or subclassed.
 * </p>
 */
public class PluginDropAdapter extends ViewerDropAdapter {
    /**
     * The extension point attribute that defines the drop action class.
     */
    public static final String ATT_CLASS = "class";//$NON-NLS-1$

    /**
     * The current transfer data, or <code>null</code> if none.
     */
    private TransferData currentTransfer;

    /** 
     * Creates a plug-in drop adapter for the given viewer.
     *
     * @param viewer the viewer
     */
    public PluginDropAdapter(StructuredViewer viewer) {
        super(viewer);
    }

    /* (non-Javadoc)
     * Method declared on DropTargetAdapter.
     * The user has dropped something on the desktop viewer.
     */
    public void drop(DropTargetEvent event) {
        try {
            if (PluginTransfer.getInstance().isSupportedType(
                    event.currentDataType)) {
                PluginTransferData pluginData = (PluginTransferData) event.data;
                IDropActionDelegate delegate = getPluginAdapter(pluginData);
                if (!delegate.run(pluginData.getData(), getCurrentTarget())) {
                    event.detail = DND.DROP_NONE;
                }
            } else {
                super.drop(event);
            }
        } catch (CoreException e) {
            WorkbenchPlugin.log("Drop Failed", e.getStatus());//$NON-NLS-1$
        }
    }

    /**
     * Returns the current transfer.
     */
    protected TransferData getCurrentTransfer() {
        return currentTransfer;
    }

    /**
     * Loads the class that will perform the action associated with the given drop
     * data.
     *
     * @param data the drop data
     * @return the viewer drop adapter
     */
    protected static IDropActionDelegate getPluginAdapter(
            PluginTransferData data) throws CoreException {

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        String adapterName = data.getExtensionId();
        IExtensionPoint xpt = registry.getExtensionPoint(PlatformUI.PLUGIN_ID,
                IWorkbenchRegistryConstants.PL_DROP_ACTIONS);
        IExtension[] extensions = xpt.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configs = extensions[i]
                    .getConfigurationElements();
            if (configs != null && configs.length > 0) {
                String id = configs[0].getAttribute("id");//$NON-NLS-1$
                if (id != null && id.equals(adapterName)) {
                    return (IDropActionDelegate) WorkbenchPlugin
                            .createExtension(configs[0], ATT_CLASS);
                }
            }
        }
        return null;
    }

    /**
     * @see ViewerDropAdapter#performDrop
     */
    public boolean performDrop(Object data) {
        //should never be called, since we override the drop() method.
        return false;
    }

    /**
     * The <code>PluginDropAdapter</code> implementation of this
     * <code>ViewerDropAdapter</code> method is used to notify the action that some
     * aspect of the drop operation has changed. Subclasses may override.
     */
    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        currentTransfer = transferType;
        if (currentTransfer != null
                && PluginTransfer.getInstance()
                        .isSupportedType(currentTransfer)) {
            //plugin cannot be loaded without the plugin data
            return true;
        }
        return false;
    }
}
