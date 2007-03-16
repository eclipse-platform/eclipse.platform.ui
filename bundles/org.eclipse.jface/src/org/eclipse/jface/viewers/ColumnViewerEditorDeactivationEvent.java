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

package org.eclipse.jface.viewers;

import java.util.EventObject;

/**
 * This event is fired when an editor deactivated
 * 
 * @since 3.3
 * 
 */
public class ColumnViewerEditorDeactivationEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param source
	 */
	public ColumnViewerEditorDeactivationEvent(Object source) {
		super(source);
	}

}
