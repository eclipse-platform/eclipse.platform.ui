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

import org.eclipse.swt.widgets.Menu;


/**
 * Interface for listening to annotation selection events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * TODO Note that this is work in progress and the interface is still subject to change.
 * </p>
 * 
 * @since 3.0
 */
public interface IAnnotationListener {
	/**
	 * Called when an annotation is selected (e.g. hovering or selecting via keyboard) in the UI.
	 * 
	 * @param event the annotation event that occurred
	 */
	void annotationSelected(AnnotationEvent event);
	
	/**
	 * Called when default selection (e.g. double clicking or pressing enter) occurs on an 
	 * annotation.
	 * 
	 * @param event the annotation event that occurred
	 */
	void annotationDefaultSelected(AnnotationEvent event);
	
	/** 
	 * Called when the context menu is opened on an annotation.
	 * 
	 * @param event the annotation event that occurred
	 * @param menu the menu that is about to be shown
	 */
	void annotationContextMenuAboutToShow(AnnotationEvent event, Menu menu);
}
