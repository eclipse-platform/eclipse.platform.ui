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

package org.eclipse.ui.presentations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.presentations.DefaultEditorPresentation;
import org.eclipse.ui.internal.presentations.DefaultViewPresentation;
import org.eclipse.ui.internal.presentations.DefaultStandalonePresentation;

/**
 * The default presentation factory for the Workbench.
 * 
 * @since 3.0
 */
public class WorkbenchPresentationFactory extends AbstractPresentationFactory {

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
