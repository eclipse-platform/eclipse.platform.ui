/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Instances represent registered wizards.
 */
public class WorkbenchWizardElement extends WorkbenchAdapter implements
        IAdaptable, IPluginContribution {
    private String id;
    
    private ImageDescriptor imageDescriptor;

    private SelectionEnabler selectionEnabler;

    private IConfigurationElement configurationElement;

    private ImageDescriptor descriptionImage;

    /**
     * Create a new instance of this class
     * 
     * @param name
     *            java.lang.String
     */
    public WorkbenchWizardElement(IConfigurationElement configurationElement) {
        this.configurationElement = configurationElement;
        id = configurationElement.getAttribute(WizardsRegistryReader.ATT_ID);
    }

    /**
     * Answer a boolean indicating whether the receiver is able to handle the
     * passed selection
     * 
     * @return boolean
     * @param selection
     *            IStructuredSelection
     */
    public boolean canHandleSelection(IStructuredSelection selection) {
        return getSelectionEnabler().isEnabledForSelection(selection);
    }

    /**
     * Answer the selection for the reciever based on whether the it can handle
     * the selection. If it can return the selection. If it can handle the
     * adapted to IResource value of the selection. If it satisfies neither of
     * these conditions return an empty IStructuredSelection.
     * 
     * @return IStructuredSelection
     * @param selection
     *            IStructuredSelection
     */
    public IStructuredSelection adaptedSelection(IStructuredSelection selection) {
        if (canHandleSelection(selection))
            return selection;

        IStructuredSelection adaptedSelection = convertToResources(selection);
        if (canHandleSelection(adaptedSelection))
            return adaptedSelection;

        //Couldn't find one that works so just return
        return StructuredSelection.EMPTY;
    }

    /**
     * Create an the instance of the object described by the configuration
     * element. That is, create the instance of the class the isv supplied in
     * the extension point.
     */
    public Object createExecutableExtension() throws CoreException {
        return WorkbenchPlugin.createExtension(configurationElement,
                WizardsRegistryReader.ATT_CLASS);
    }

    /**
     * Returns an object which is an instance of the given class associated
     * with this object. Returns <code>null</code> if no such object can be
     * found.
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class
                || adapter == IWorkbenchAdapter2.class) {
            return this;
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * @return IConfigurationElement
     */
    public IConfigurationElement getConfigurationElement() {
        return configurationElement;
    }

    /**
     * Answer the description parameter of this element
     * 
     * @return java.lang.String
     */
    public String getDescription() {
        return RegistryReader.getDescription(configurationElement);
    }

    /**
     * Answer the id as specified in the extension.
     * 
     * @return java.lang.String
     */
    public String getID() {
        return id;
    }

    /**
     * Answer the icon of this element.
     */
    public ImageDescriptor getImageDescriptor() {
    	if (imageDescriptor == null) {
    		String iconName = configurationElement.getAttribute(WizardsRegistryReader.ATT_ICON);
	        if (iconName == null) 
	        	return null;
            imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                    configurationElement.getNamespace(), iconName);    
    	}
        return imageDescriptor;
    }

    /**
     * Returns the name of this wizard element.
     */
    public ImageDescriptor getImageDescriptor(Object element) {
        return getImageDescriptor();
    }
    
    /**
     * Returns the name of this wizard element.
     */
    public String getLabel(Object element) {
        return configurationElement.getAttribute(WizardsRegistryReader.ATT_NAME);
    }

    /**
     * Answer self's action enabler, creating it first iff necessary
     */
    protected SelectionEnabler getSelectionEnabler() {
        if (selectionEnabler == null)
            selectionEnabler = new SelectionEnabler(configurationElement);

        return selectionEnabler;
    }

    /**
     * Attempt to convert the elements in the passed selection into resources
     * by asking each for its IResource property (iff it isn't already a
     * resource). If all elements in the initial selection can be converted to
     * resources then answer a new selection containing these resources;
     * otherwise answer an empty selection.
     * 
     * @param originalSelection the original selection
     * @return the converted selection or an empty selection
     */
    private IStructuredSelection convertToResources(
            IStructuredSelection originalSelection) {
        //	@issue resource-specific code should be pushed into IDE
        Class resourceClass = LegacyResourceSupport.getResourceClass();
        if (resourceClass == null) {
            return originalSelection;
        }

        List result = new ArrayList();
        Iterator elements = originalSelection.iterator();

        while (elements.hasNext()) {
            Object currentElement = elements.next();
            if (resourceClass.isInstance(currentElement)) { // already a resource
                result.add(currentElement);
            } else if (!(currentElement instanceof IAdaptable)) { // cannot be converted to resource
                return StructuredSelection.EMPTY; // so fail
            } else {
                Object adapter = ((IAdaptable) currentElement)
                        .getAdapter(resourceClass);
                if (!(resourceClass.isInstance(adapter))) // chose not to be converted to resource
                    return StructuredSelection.EMPTY; // so fail
                result.add(adapter); // add the converted resource
            }
        }

        // all converted fine, answer new selection
        return new StructuredSelection(result.toArray());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getID();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return (configurationElement != null) ? configurationElement
                .getDeclaringExtension().getNamespace() : null;
    }

    /**
     * Return the description image for this wizard.
     * 
     * @return the description image for this wizard
     * @since 3.0
     */
    public ImageDescriptor getDescriptionImage() {
    	if (descriptionImage == null) {
    		String descImage = configurationElement.getAttribute(NewWizardsRegistryReader.ATT_DESCRIPTION_IMAGE);
    		if (descImage == null)
    			return null;
            descriptionImage = AbstractUIPlugin.imageDescriptorFromPlugin(
                    configurationElement.getNamespace(), descImage);;
    	}
        return descriptionImage;
    }

    /**
     * Return the help system href for this wizard.
     * 
     * @return the help system href for this wizard
     * @since 3.0
     */
    public String getHelpHref() {
        return configurationElement.getAttribute(NewWizardsRegistryReader.ATT_HELP_HREF);
    }
}