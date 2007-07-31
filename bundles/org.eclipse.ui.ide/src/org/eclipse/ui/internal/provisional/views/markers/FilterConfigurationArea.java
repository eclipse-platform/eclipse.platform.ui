/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.swt.widgets.Composite;

/**
 * FilterConfigurationArea is the area that the user can configure
 * a filter in.
 * @since 3.4
 *
 */
public abstract class FilterConfigurationArea {
	
	private MarkerField field;

	/**
	 * Create the contents of the configuration area in the parent.
	 * @param parent
	 */
	public abstract void createContents(Composite parent);

	/**
	 * Get the title for the receiver.
	 * @return
	 */
	public String getTitle() {
		return field.getColumnHeaderText();
	}

}
