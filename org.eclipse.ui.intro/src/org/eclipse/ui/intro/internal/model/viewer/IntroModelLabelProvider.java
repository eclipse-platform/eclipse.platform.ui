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

package org.eclipse.ui.intro.internal.model.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;

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
        IntroElement introElement = null;
        if (element instanceof IntroElement)
            // synch the resource first.
            introElement = (IntroElement) element;
        if (introElement == null)
            return null;

        int elementType = introElement.getType();
        switch (elementType) {
        case IntroElement.DIV:
        case IntroElement.PAGE:
        case IntroElement.HOME_PAGE:
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
        IntroElement introElement = null;
        if (element instanceof IntroElement)
            // synch the resource first.
            introElement = (IntroElement) element;
        if (introElement == null)
            return null;

        int elementType = introElement.getType();
        switch (elementType) {
        case IntroElement.DIV:
            label = "DIV: " + ((IntroDiv) introElement).getLabel();
            break;
        case IntroElement.LINK:
            label = "LINK: " + ((IntroLink) introElement).getLabel();
            break;
        case IntroElement.TEXT:
            label = "TEXT: " + ((IntroText) introElement).getText();
            break;
        case IntroElement.IMAGE:
            label = "IMAGE: " + ((IntroImage) introElement).getId();
            break;
        case IntroElement.HTML:
            label = "HTML: " + ((IntroHTML) introElement).getId();
            break;
        case IntroElement.INCLUDE:
            label = "Unresolved INCLUDE: "
                    + ((IntroInclude) introElement).getPath();
            break;
        case IntroElement.PAGE:
            label = "PAGE: " + ((AbstractIntroPage) introElement).getTitle();
            break;
        case IntroElement.HOME_PAGE:
            label = "HOME PAGE: "
                    + ((AbstractIntroPage) introElement).getTitle();
            break;
        case IntroElement.PRESENTATION:
            label = "PRESENTATION: "
                    + ((IntroPartPresentation) introElement).getTitle();
            break;
        case IntroElement.CONTAINER_EXTENSION:
            label = "Unresolved ConfigExtension: "
                    + ((IntroContainerExtension) introElement).getPath();
            break;
        default:
            label = super.getText(element);
            break;
        }
        return label;
    }

}