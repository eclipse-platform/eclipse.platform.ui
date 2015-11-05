/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.PluginActionSet;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * ActionSetDescriptor
 */
public class ActionSetDescriptor implements IActionSetDescriptor, IAdaptable,
        IWorkbenchAdapter, IPluginContribution {
    private static final Object[] NO_CHILDREN = new Object[0];

    private static final String INITIALLY_HIDDEN_PREF_ID_PREFIX = "actionSet.initiallyHidden."; //$NON-NLS-1$

    private String id;

    private String pluginId;

    private String label;

    private boolean visible;

    private String description;

    private IConfigurationElement configElement;

    /**
     * Create a descriptor from a configuration element.
     *
     * @param configElement the configuration element
     * @throws CoreException thrown if there is an issue creating the descriptor
     */
    public ActionSetDescriptor(IConfigurationElement configElement)
            throws CoreException {
        super();
        this.configElement = configElement;
        id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        pluginId = configElement.getNamespace();
        label = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
        description = configElement.getAttribute(IWorkbenchRegistryConstants.TAG_DESCRIPTION);
        String str = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_VISIBLE);
        if (str != null && str.equals("true")) { //$NON-NLS-1$
			visible = true;
		}

        // Sanity check.
        if (label == null) {
            throw new CoreException(new Status(IStatus.ERROR,
                    WorkbenchPlugin.PI_WORKBENCH, 0,
                    "Invalid extension (missing label): " + id,//$NON-NLS-1$
                    null));
        }
    }

    /**
     * Returns the action set for this descriptor.
     *
     * @return the action set
     */
    @Override
	public IActionSet createActionSet() throws CoreException {
        return new PluginActionSet(this);
    }

    /**
     * Returns an object which is an instance of the given class
     * associated with this object. Returns <code>null</code> if
     * no such object can be found.
     */
    @Override
	public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
        return null;
    }

    /**
     * @see IWorkbenchAdapter#getChildren
     */
    @Override
	public Object[] getChildren(Object o) {


        return NO_CHILDREN;
    }


    @Override
	public IConfigurationElement getConfigurationElement() {
        return configElement;
    }

    /**
     * Returns this action set's description.
     * This is the value of its <code>"description"</code> attribute.
     *
     * @return the description
     */
    @Override
	public String getDescription() {
        return description;
    }

    /**
     * Returns this action set's id.
     * This is the value of its <code>"id"</code> attribute.
     * <p>
     *
     * @return the action set id
     */
    @Override
	public String getId() {
        return id;
    }

    /**
     * Returns this action set's label.
     * This is the value of its <code>"label"</code> attribute.
     *
     * @return the label
     */
    @Override
	public String getLabel() {
        return label;
    }

    /**
     * @see IWorkbenchAdapter#getLabel
     */
    @Override
	public String getLabel(Object o) {
        if (o == this) {
			return getLabel();
		}
        return "Unknown Label";//$NON-NLS-1$
    }

    /**
     * Returns whether this action set is initially visible.
     */
    @Override
	public boolean isInitiallyVisible() {
        if (id == null) {
			return visible;
		}
        Preferences prefs = WorkbenchPlugin.getDefault().getPluginPreferences();
        String prefId = INITIALLY_HIDDEN_PREF_ID_PREFIX + getId();
        if (prefs.getBoolean(prefId)) {
			return false;
		}
        return visible;
    }

    /**
     * Sets whether this action set is initially visible.
     * If the action set identifier is undefined, then this is ignored.
     *
     * @since 3.0
     */
    @Override
	public void setInitiallyVisible(boolean newValue) {
        if (id == null) {
			return;
		}
        Preferences prefs = WorkbenchPlugin.getDefault().getPluginPreferences();
        String prefId = INITIALLY_HIDDEN_PREF_ID_PREFIX + getId();
        prefs.setValue(prefId, !newValue);
    }

    @Override
	public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    @Override
	public Object getParent(Object o) {
        return null;
    }

    @Override
	public String getLocalId() {
        return id;
    }

    @Override
	public String getPluginId() {
        return pluginId;
    }

    @Override
	public boolean equals(Object arg0) {
        if (!(arg0 instanceof ActionSetDescriptor)) {
            return false;
        }

        ActionSetDescriptor descr = (ActionSetDescriptor) arg0;

        return id.equals(descr.id) && descr.pluginId.equals(pluginId);
    }

    @Override
	public int hashCode() {
        return id.hashCode() + pluginId.hashCode();
    }

	@Override
	public String toString() {
		return "ActionSetDescriptor [id=" + id + ", visible=" + visible + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
