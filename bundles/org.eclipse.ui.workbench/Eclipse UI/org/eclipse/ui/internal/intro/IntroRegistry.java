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
package org.eclipse.ui.internal.intro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Registry for introduction elements.
 * 
 * @since 3.0
 */
public class IntroRegistry implements IIntroRegistry {
    private static final String ATT_INTROID = "introId"; //$NON-NLS-1$

    private static final String ATT_PRODUCTID = "productId"; //$NON-NLS-1$	

    private Map bindingMap = new HashMap(7);

    private ArrayList intros = new ArrayList(10);

    /**
     * Add a descriptor to this registry.
     * 
     * @param descriptor the descriptor
     */
    public void add(IIntroDescriptor descriptor) {
        intros.add(descriptor);
    }

    /**
     * Add a binding between a product and an introduction.
     * 
     * @param element the element to parse
     * @throws CoreException if the binding could not be created
     */
    public void addBinding(IConfigurationElement element) throws CoreException {
        String introId = element.getAttribute(ATT_INTROID);
        String productId = element.getAttribute(ATT_PRODUCTID);

        if (introId == null || productId == null) {
            IStatus status = new Status(
                    IStatus.ERROR,
                    element.getDeclaringExtension().getNamespace(),
                    IStatus.ERROR,
                    "introId and productId must be defined.", new IllegalArgumentException()); //$NON-NLS-1$
            throw new CoreException(status);
        }
        if (bindingMap.containsKey(productId)) {
            IStatus status = new Status(
                    IStatus.WARNING,
                    element.getDeclaringExtension().getNamespace(),
                    IStatus.WARNING,
                    productId
                            + " already has an intro binding.  Omitting binding to" + introId, new IllegalArgumentException()); //$NON-NLS-1$
            throw new CoreException(status);
        }
        bindingMap.put(productId, introId);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.intro.IIntroRegistry#getIntroCount()
     */
    public int getIntroCount() {
        return intros.size();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.intro.IIntroRegistry#getIntros()
     */
    public IIntroDescriptor[] getIntros() {
        return (IIntroDescriptor[]) intros.toArray(new IIntroDescriptor[intros
                .size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.intro.IIntroRegistry#getIntroForProduct(java.lang.String)
     */
    public IIntroDescriptor getIntroForProduct(String productId) {
        IIntroDescriptor descriptor = null;
        String introId = (String) bindingMap.get(productId);
        if (introId != null) {
            IIntroDescriptor[] introDescs = getIntros();
            for (int i = 0; i < introDescs.length; i++) {
                if (introDescs[i].getId().equals(introId)) {
                    descriptor = introDescs[i];
                    break;
                }
            }
        }
        return descriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.intro.IIntroRegistry#getIntro(java.lang.String)
     */
    public IIntroDescriptor getIntro(String id) {
        for (Iterator i = intros.iterator(); i.hasNext();) {
            IIntroDescriptor desc = (IIntroDescriptor) i.next();
            if (desc.getId().equals(id))
                return desc;
        }
        return null;
    }
}