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
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.1
 */
public class OldDefaultPresentationFactory extends AbstractPresentationFactory {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createEditorPresentation(Composite parent,
            IStackPresentationSite site) {
        return new DefaultEditorPresentation(parent, site);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createViewPresentation(Composite parent,
            IStackPresentationSite site) {
        return new DefaultViewPresentation(parent, site);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createStandaloneViewPresentation(Composite parent,
            IStackPresentationSite site, boolean showTitle) {
        return new DefaultStandalonePresentation(parent, site, showTitle);
    }
}

