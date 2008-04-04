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
package org.eclipse.ui;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Description of a workbench part. The part descriptor contains 
 * the information needed to create part instances.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchPartDescriptor {
    /**
     * Returns the part id.
     *
     * @return the id of the part
     */
    public String getId();

    /**
     * Returns the descriptor of the image for this part.
     *
     * @return the descriptor of the image to display next to this part
     */
    public ImageDescriptor getImageDescriptor();

    /**
     * Returns the label to show for this part.
     *
     * @return the part label
     */
    public String getLabel();
}
