/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.net.URL;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.BundleUtility;

/**
 * The DeclarativeDecorator is a decorator that is made entirely from an XML
 * specification.
 */
public class DeclarativeDecorator implements ILightweightLabelDecorator {
    private static LightweightDecoratorListener updateListener;

	private String iconLocation;

    private IConfigurationElement configElement;

    private ImageDescriptor descriptor;

    DeclarativeDecorator(IConfigurationElement definingElement, String iconPath) {
        this.iconLocation = iconPath;
        this.configElement = definingElement;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
    	getUpdateListener().disableFor(getId());
    }

    /**
     * Return the id for this decorator.
     * @return String
     */
    private String getId() {
    	return configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
	}

	/**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
    }

    /**
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
     *      org.eclipse.jface.viewers.IDecoration)
     */
    public void decorate(Object element, IDecoration decoration) {
        if (descriptor == null) {
            URL url = BundleUtility.find(configElement.getDeclaringExtension()
                    .getNamespace(), iconLocation);
            if (url == null)
                return;
            descriptor = ImageDescriptor.createFromURL(url);
        }
        decoration.addOverlay(descriptor);
    }
	
	/**
	 * Return the listener for updates.
	 * @return LightweightDecoratorListener
	 */
	private static LightweightDecoratorListener getUpdateListener(){
		return updateListener;
	}

	/**
	 * Set the update listener for the receiver.
	 * @param updateListener
	 */
	public static void setUpdateListener(LightweightDecoratorListener updateListener) {
		DeclarativeDecorator.updateListener = updateListener;
	}

	public static void setEnabledBy(String id, ActionExpression enablement) {
		Collection natureValues = enablement.valuesForExpression("nature");//$NON-NLS-1$
		if(natureValues != null){
			String[] natures = new String[natureValues.size()];
			natureValues.toArray(natures);
			getUpdateListener().listenFor(id, natures);
		}
		
		
	}
}
