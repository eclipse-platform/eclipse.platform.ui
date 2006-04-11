/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import org.eclipse.ui.Saveable;
import org.eclipse.ui.navigator.ISaveablesSourceHelper;

/**
 * @since 3.2
 * @deprecated
 * 
 */
public class SaveablesSourceHelper implements ISaveablesSourceHelper {

	public Saveable[] getActiveSaveables() {
		return new Saveable[0];
	}

	/**
	 * @return the saveables
	 */
	public Saveable[] getSaveables() {
		return new Saveable[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.ISaveablesSourceHelper#dispose()
	 */
	public void dispose() {
	}

}
