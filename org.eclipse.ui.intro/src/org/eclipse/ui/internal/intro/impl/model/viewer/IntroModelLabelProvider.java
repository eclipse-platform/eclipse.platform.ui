/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;

public class IntroModelLabelProvider extends LabelProvider {

    // Images created in this ILabelProvider

    static {
        ImageUtil.registerImage(ImageUtil.INTRO_MODEL_CONTAINER,
                "container_obj.gif");
        ImageUtil.registerImage(ImageUtil.INTRO_MODEL_LEAF, "topic.gif");
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
        case AbstractIntroElement.DIV:
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
        case AbstractIntroElement.DIV:
            label = "DIV: " + ((IntroDiv) introElement).getLabel();
            break;
        case AbstractIntroElement.LINK:
            label = "LINK: " + ((IntroLink) introElement).getLabel();
            break;
        case AbstractIntroElement.TEXT:
            label = "TEXT: " + ((IntroText) introElement).getText();
            break;
        case AbstractIntroElement.IMAGE:
            label = "IMAGE: " + ((IntroImage) introElement).getId();
            break;
        case AbstractIntroElement.HTML:
            label = "HTML: " + ((IntroHTML) introElement).getId();
            break;
        case AbstractIntroElement.INCLUDE:
            label = "Unresolved INCLUDE: "
                    + ((IntroInclude) introElement).getPath();
            break;
        case AbstractIntroElement.PAGE:
            label = "PAGE: " + ((AbstractIntroPage) introElement).getTitle();
            break;
        case AbstractIntroElement.HOME_PAGE:
            label = "HOME PAGE: "
                    + ((AbstractIntroPage) introElement).getTitle();
            break;
        case AbstractIntroElement.PRESENTATION:
            label = "PRESENTATION: "
                    + ((IntroPartPresentation) introElement).getTitle();
            break;
        case AbstractIntroElement.CONTAINER_EXTENSION:
            label = "Unresolved ConfigExtension: "
                    + ((IntroExtensionContent) introElement).getPath();
            break;
        default:
            label = super.getText(element);
            break;
        }
        return label;
    }

}