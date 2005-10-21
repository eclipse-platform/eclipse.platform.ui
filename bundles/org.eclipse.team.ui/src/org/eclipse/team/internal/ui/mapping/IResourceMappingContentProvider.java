/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * A model content provider is a tree content provider that is used to display
 * specific elements of a logical model. The only additional attribute of a model
 * content provider is that it also provides the root of the model being displayed.
 * This root may be used as the viewer input (in cases where the viewer is only displaying
 * a single model) or may be used as one of multiple elements appearing at the top
 * level in the viewer (in cases where the viewer contains multiple models).
 */
public interface IResourceMappingContentProvider extends ITreeContentProvider {
    
    /**
     * Returns the root element of the model tree being displayed.
     * This element may or may not appear in the viewer depending on
     * whether the viewer is displaying a single logical model (in which case
     * the root need not be displayed) or multiple logical models (in which
     * case the root needs to be displayed to separate the mappings of different
     * models).
     * @return the root element of the model tree being displayed.
     */
    public Object getRoot();

}
