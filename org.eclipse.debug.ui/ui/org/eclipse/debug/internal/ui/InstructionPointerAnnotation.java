/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

 
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.swt.graphics.Image;

/**
 * Default instruction pointer annotation.
 */
public class InstructionPointerAnnotation extends DynamicInstructionPointerAnnotation {
	
	/**
	 * The image for this annotation.
	 */
	private Image fImage;
	
	/**
	 * 
	 * @param frame
	 * @param markerAnnotationSpecificationId
	 * @param text
	 */
	public InstructionPointerAnnotation(IStackFrame frame, String annotationType, String text, Image image) {
		super(frame, annotationType, text);
		fImage = image;
	}
		
	/**
	 * Returns this annotation's image.
	 * 
	 * @return image
	 */
	protected Image getImage() {
		return fImage;
	}

}
