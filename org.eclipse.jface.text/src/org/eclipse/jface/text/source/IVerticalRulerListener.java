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
package org.eclipse.jface.text.source;

import org.eclipse.swt.widgets.Menu;


/**
 * Interface for listening to annotation related events happening on a vertical ruler.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 */
public interface IVerticalRulerListener {

	/**
	 * Called when an annotation is selected in the vertical ruler.
	 *
	 * @param event the annotation event that occurred
	 */
	void annotationSelected(VerticalRulerEvent event);

	/**
	 * Called when a default selection occurs on an
	 * annotation in the vertical ruler.
	 *
	 * @param event the annotation event that occurred
	 */
	void annotationDefaultSelected(VerticalRulerEvent event);

	/**
	 * Called when the context menu is opened on an annotation in the
	 * vertical ruler.
	 *
	 * @param event the annotation event that occurred
	 * @param menu the menu that is about to be shown
	 */
	void annotationContextMenuAboutToShow(VerticalRulerEvent event, Menu menu);
}
