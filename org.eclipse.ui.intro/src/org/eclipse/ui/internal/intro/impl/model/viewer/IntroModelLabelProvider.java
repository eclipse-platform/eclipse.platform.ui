/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

public class IntroModelLabelProvider extends LabelProvider {

    // Images created in this ILabelProvider

    static {
        ImageUtil.registerImage(ImageUtil.INTRO_MODEL_CONTAINER,
                "container_obj.gif"); //$NON-NLS-1$
        ImageUtil.registerImage(ImageUtil.INTRO_MODEL_LEAF, "topic.gif"); //$NON-NLS-1$
    }

    public IntroModelLabelProvider() {
        super();
    }

    public Image getImage(Object element) {
        Image image = null;
        AbstractIntroElement introElement = null;
        if (element instanceof AbstractIntroElement)
            // synch the resource first.
            introElement = (AbstractIntroElement) element;
        if (introElement == null)
            return null;

        int elementType = introElement.getType();
        switch (elementType) {
        case AbstractIntroElement.GROUP:
        case AbstractIntroElement.PAGE:
        case AbstractIntroElement.HOME_PAGE:
            image = ImageUtil.getImage(ImageUtil.INTRO_MODEL_CONTAINER);
            break;
        default:
            image = ImageUtil.getImage(ImageUtil.INTRO_MODEL_LEAF);
            break;
        }
        return image;
    }

    public String getText(Object element) {

        String label = null;
        AbstractIntroElement introElement = null;
        if (element instanceof AbstractIntroElement)
            // synch the resource first.
            introElement = (AbstractIntroElement) element;
        if (introElement == null)
            return null;

        int elementType = introElement.getType();
        switch (elementType) {
        case AbstractIntroElement.GROUP:
            label = "GROUP: " + ((IntroGroup) introElement).getLabel(); //$NON-NLS-1$
            break;
        case AbstractIntroElement.LINK:
            label = "LINK: " + ((IntroLink) introElement).getLabel(); //$NON-NLS-1$
            break;
        case AbstractIntroElement.TEXT:
            label = "TEXT: " + ((IntroText) introElement).getText(); //$NON-NLS-1$
            break;
        case AbstractIntroElement.IMAGE:
            label = "IMAGE: " + ((IntroImage) introElement).getId(); //$NON-NLS-1$
            break;
        case AbstractIntroElement.HTML:
            label = "HTML: " + ((IntroHTML) introElement).getId(); //$NON-NLS-1$
            break;
        case AbstractIntroElement.INCLUDE:
            label = "Unresolved INCLUDE: " //$NON-NLS-1$
                    + ((IntroInclude) introElement).getPath();
            break;
        case AbstractIntroElement.PAGE:
            label = "PAGE: " + ((AbstractIntroPage) introElement).getId(); //$NON-NLS-1$
            break;
        case AbstractIntroElement.HOME_PAGE:
            label = "HOME PAGE: " //$NON-NLS-1$
                    + ((AbstractIntroPage) introElement).getTitle();
            break;
        case AbstractIntroElement.PRESENTATION:
            label = "PRESENTATION: " //$NON-NLS-1$
                    + ((IntroPartPresentation) introElement)
                            .getImplementationKind();
            break;
        case AbstractIntroElement.CONTENT_PROVIDER:
            label = "CONTENT PROVIDER: " //$NON-NLS-1$
                    + ((IntroContentProvider) introElement).getClassName();
            break;
        case AbstractIntroElement.CONTAINER_EXTENSION:
            label = "Unresolved ConfigExtension: " //$NON-NLS-1$
                    + ((IntroExtensionContent) introElement).getPath();
            break;
        default:
            label = super.getText(element);
            break;
        }
        return label;
    }

}