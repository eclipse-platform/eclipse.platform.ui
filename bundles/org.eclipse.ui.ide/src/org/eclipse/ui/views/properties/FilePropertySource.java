/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * The FilePropertySource gives the extra information that is shown for files
 */
public class FilePropertySource extends ResourcePropertySource {

    private static PropertyDescriptor fileDescriptor;
    static {
        fileDescriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_SIZE_RES,
                IResourcePropertyConstants.P_DISPLAY_SIZE);
        fileDescriptor.setAlwaysIncompatible(true);
        fileDescriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
    }

    /**
     * Creates an property source for a file resource.
     * @param file the file resource
     */
    public FilePropertySource(IFile file) {
        super(file);
    }

    /**
     * Get a PropertyDescriptor that defines the size property
     * @return the PropertyDescriptor
     */
    private static PropertyDescriptor getInitialPropertyDescriptor() {
        return fileDescriptor;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource.
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] superDescriptors = super.getPropertyDescriptors();
        int superLength = superDescriptors.length;
        IPropertyDescriptor[] fileDescriptors = new IPropertyDescriptor[superLength + 1];
        System.arraycopy(superDescriptors, 0, fileDescriptors, 0, superLength);
        fileDescriptors[superLength] = getInitialPropertyDescriptor();

        return fileDescriptors;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource.
     */
    public Object getPropertyValue(Object key) {
    	Object returnValue = (key.equals(IBasicPropertyConstants.P_TEXT)) ? TextProcessor
				.process(element.getName())
				: super.getPropertyValue(key);

        if (returnValue != null) {
			return returnValue;
		}

        if (key.equals(IResourcePropertyConstants.P_SIZE_RES)) {
			return IDEResourceInfoUtils.getSizeString((IFile) element);
		}
        return null;
    }
}
