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
package org.eclipse.ui.internal.decorators;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.Assert;

/**
 * A DecorationReference is a class that holds onto the starting
 * text and image of a decoration.
 */
class DecorationReference {
    Object element;

    Object adaptedElement;

    String undecoratedText;

    boolean forceUpdate = false;

    DecorationReference(Object object) {
        Assert.isNotNull(object);
        element = object;
    }

    DecorationReference(Object object, Object adaptedObject) {
        this(object);
        this.adaptedElement = adaptedObject;
    }

    /**
     * Returns the adaptedElement.
     * @return Object
     */
    Object getAdaptedElement() {
        return adaptedElement;
    }

    /**
     * Returns the element.
     * @return Object
     */
    Object getElement() {
        return element;
    }

    /**
     * Return true if an update should occur whether or 
     * not there is a result.
     * @return boolean
     */
    boolean shouldForceUpdate() {
        return forceUpdate;
    }

    /**
     * Sets the forceUpdate flag. If true an update 
     * occurs whether or not a decoration has resulted.
     * @param forceUpdate The forceUpdate to set
     */
    void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    /**
     * Set the text that will be used to label the decoration
     * calculation.
     * @param text
     */
    void setUndecoratedText(String text) {
        undecoratedText = text;
    }

    /**
     * Return the string for the subtask for this element.
     * @return
     */
    String getSubTask() {
        if (undecoratedText == null)
            return WorkbenchMessages.DecorationReference_EmptyReference;
	return NLS.bind(WorkbenchMessages.DecorationScheduler_DecoratingSubtask, undecoratedText );
    }
}
