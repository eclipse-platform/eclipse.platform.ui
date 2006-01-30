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
package org.eclipse.ui.tests.views.properties.tabbed.views;

import java.util.Iterator;

import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;

/**
 * Label provider for the title bar for the tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */
public class LabelProvider
    extends org.eclipse.jface.viewers.LabelProvider {

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object obj) {
        if (obj == null || obj.equals(StructuredSelection.EMPTY)) {
            return null;
        }
        if (obj instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) obj;
            if (areDifferentTypes(structuredSelection)) {
                return null;
            }
            obj = structuredSelection.getFirstElement();
        }
        Element element = (Element) ((TreeNode) obj).getValue();
        return element.getImage();
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object obj) {
        if (obj == null || obj.equals(StructuredSelection.EMPTY)) {
            return null;
        }
        int size = 1;
        if (obj instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) obj;
            if (areDifferentTypes(structuredSelection)) {
                return structuredSelection.size() + " items selected";//$NON-NLS-1$
            }
            obj = structuredSelection.getFirstElement();
            size = structuredSelection.size();
        }
        StringBuffer ret = new StringBuffer();
        Element element = (Element) ((TreeNode) obj).getValue();
        String type = element.getClass().getName();
        String name = element.getName();
        ret.append('\u00AB');
        ret.append(type.substring(type.lastIndexOf('.') + 1));
        ret.append('\u00BB');
        if (size == 1) {
            ret.append(' ');
            ret.append(name);
        } else {
            ret.append(' ');
            ret.append(Integer.toString(size));
            ret.append(" selected");//$NON-NLS-1$
        }
        return ret.toString();
    }

    /**
     * Determine there are objects in the structured selection of different
     * types.
     * 
     * @param structuredSelection
     *            the structured selection.
     * @return true if there are objects of different types in the selection.
     */
    private boolean areDifferentTypes(IStructuredSelection structuredSelection) {
        if (structuredSelection.size() == 1) {
            return false;
        }
        Iterator i = structuredSelection.iterator();
        Element element = (Element) ((TreeNode) i.next()).getValue();
        for (; i.hasNext();) {
            Element next = (Element) ((TreeNode) i.next()).getValue();
            if (next.getClass() != element.getClass()) {
                return true;
            }
        }

        return false;
    }
}