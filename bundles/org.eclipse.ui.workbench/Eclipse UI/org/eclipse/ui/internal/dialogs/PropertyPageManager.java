/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - bug 219273
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExpressionNode;

/**
 * This class is created to avoid mentioning preferences
 * in this context. Ideally, JFace preference classes should be
 * renamed into something more generic (for example,
 * 'TreeNavigationDialog').
 */

public class PropertyPageManager extends PreferenceManager {
	/**
	 * The constructor.
	 */
	public PropertyPageManager() {
		super(WorkbenchPlugin.PREFERENCE_PAGE_CATEGORY_SEPARATOR,
				new WorkbenchPreferenceExpressionNode("")); //$NON-NLS-1$
	}

}
