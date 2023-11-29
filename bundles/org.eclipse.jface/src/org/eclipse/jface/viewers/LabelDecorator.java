/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * The LabelDecorator is an abstract superclass of ILabelDecorators
 * that support IDecorationContext.
 * @see IDecorationContext
 * @since 3.2
 */
public abstract class LabelDecorator implements ILabelDecorator {

	 /**
	 * Returns an image that is based on the given image,
	 * but decorated with additional information relating to the state
	 * of the provided element taking into account the provided context.
	 *
	 * Text and image decoration updates can occur as a result of other updates
	 * within the workbench including deferred decoration by background processes.
	 * Clients should handle labelProviderChangedEvents for the given element to get
	 * the complete decoration.
	 * @see LabelProviderChangedEvent
	 * @see IBaseLabelProvider#addListener
	 *
	 * @param image the input image to decorate, or <code>null</code> if the element has no image
	 * @param element the element whose image is being decorated
	 * @param context additional context information about the element being decorated
	 * @return the decorated image, or <code>null</code> if no decoration is to be applied
	 *
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor
	 */
	public abstract Image decorateImage(Image image, Object element, IDecorationContext context);

	/**
	 * Returns a text label that is based on the given text label,
	 * but decorated with additional information relating to the state
	 * of the provided element taking into account the provided context.
	 *
	 * Text and image decoration updates can occur as a result of other updates
	 * within the workbench including deferred decoration by background processes.
	 * Clients should handle labelProviderChangedEvents for the given element to get
	 * the complete decoration.
	 * @see LabelProviderChangedEvent
	 * @see IBaseLabelProvider#addListener
	 *
	 * @param text the input text label to decorate
	 * @param element the element whose image is being decorated
	 * @param context additional context information about the element being decorated
	 * @return the decorated text label, or <code>null</code> if no decoration is to be applied
	 */
	public abstract String decorateText(String text, Object element, IDecorationContext context);

	/**
	 * Prepare the element for decoration. If it is already decorated and ready for update
	 * return true. If decoration is pending return false.
	 * @param element The element to be decorated
	 * @param originalText The starting text.
	 * @param context The decoration context
	 * @return boolean <code>true</code> if the decoration is ready for this element
	 */
	public abstract boolean prepareDecoration(Object element, String originalText, IDecorationContext context);
}
