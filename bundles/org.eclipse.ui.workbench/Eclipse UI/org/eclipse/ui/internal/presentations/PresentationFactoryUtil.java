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
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentationSerializer;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * 
 */
public class PresentationFactoryUtil {

    public static final int ROLE_EDITOR = 0x01;

    public static final int ROLE_VIEW = 0x02;

    public static final int ROLE_STANDALONE = 0x03;

    public static final int ROLE_STANDALONE_NOTITLE = 0x04;
    
    public static StackPresentation createPresentation(
            AbstractPresentationFactory factory, int role, Composite parent,
            IStackPresentationSite site, IPresentationSerializer serializer,
            IMemento memento) {

        StackPresentation presentation = null;

        switch (role) {
        case ROLE_EDITOR:
            presentation = factory.createEditorPresentation(parent, site);
            break;
        case ROLE_STANDALONE:
            presentation = factory.createStandaloneViewPresentation(parent,
                    site, true);
            break;
        case ROLE_STANDALONE_NOTITLE:
            presentation = factory.createStandaloneViewPresentation(parent,
                    site, false);
            break;
        default:
            presentation = factory.createViewPresentation(parent, site);
        }

        //don't initialize editors at creation time - it will not contain any parts
        if (role != ROLE_EDITOR && memento != null && serializer != null) {
            presentation.restoreState(serializer, memento);
        }

        return presentation;
    }

    private PresentationFactoryUtil() {

    }
}
