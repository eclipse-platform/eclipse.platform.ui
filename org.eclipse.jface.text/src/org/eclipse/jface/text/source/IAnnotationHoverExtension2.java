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
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.IInformationControlCreator;

/**
 * 
 * 
 * @since 3.0
 */
public interface IAnnotationHoverExtension2 {
	
	Object getHoverInfo2(ISourceViewer viewer, int line);
	/**
	 * Returns the information control creator of this annotation hover.
	 * 
	 * @return the information control creator
	 */
	IInformationControlCreator getInformationControlCreator();
	
	
}
