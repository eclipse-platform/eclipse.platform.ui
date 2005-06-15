/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.presentations.util.StandardEditorSystemMenu;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.1
 */
public class NativePresentationFactory extends AbstractPresentationFactory {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createEditorPresentation(Composite parent,
            IStackPresentationSite site) {        
        return new TabbedStackPresentation(site, new NativeTabFolder(parent), new StandardEditorSystemMenu(site));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createViewPresentation(Composite parent,
            IStackPresentationSite site) {
        return new TabbedStackPresentation(site, new NativeTabFolder(parent), new StandardViewSystemMenu(site));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createStandaloneViewPresentation(Composite parent,
            IStackPresentationSite site, boolean showTitle) {
        // TODO honour showTitle
        return new TabbedStackPresentation(site, new NativeTabFolder(parent), 
                new StandardViewSystemMenu(site));
    }

}
