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
package org.eclipse.ui.internal.misc;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * An image descriptor that loads its data from a program file.
 */
public class ProgramImageDescriptor extends ImageDescriptor {
    private String filename;

    private int offset;

    /**
     * Creates a new ImageDescriptor. The image is loaded 
     * from a file with the given name <code>name</code>.
     */
    public ProgramImageDescriptor(String fullPath, int offsetInFile) {
        filename = fullPath;
        offset = offsetInFile;
    }

    /**
     * @see Object#equals
     */
    public boolean equals(Object o) {
        if (!(o instanceof ProgramImageDescriptor)) {
            return false;
        }
        ProgramImageDescriptor other = (ProgramImageDescriptor) o;
        return filename.equals(other.filename) && offset == other.offset;
    }

    /**
     * Returns an SWT Image that is described by the information
     * in this descriptor.  Each call returns a new Image.
     */
    public Image getImage() {
        return createImage();
    }

    /**
     * Returns an SWT Image that is described by the information
     * in this descriptor. 
     */
    public ImageData getImageData() {
        /*This is a user defined offset into the file which always
         *returns us the defualt - return the default regardless*/

        return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE)
                .getImageData();
    }

    /**
     * @see Object#hashCode
     */
    public int hashCode() {
        return filename.hashCode() + offset;
    }
}
