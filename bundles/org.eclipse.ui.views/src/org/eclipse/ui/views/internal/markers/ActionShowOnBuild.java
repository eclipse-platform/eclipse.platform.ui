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

package org.eclipse.ui.views.internal.markers;

import org.eclipse.jface.preference.BooleanPropertyAction;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

class ActionShowOnBuild extends BooleanPropertyAction {

	protected ActionShowOnBuild() {
		super(Messages.getString("showAction.title"), WorkbenchPlugin.getDefault().getPreferenceStore(), IPreferenceConstants.SHOW_TASKS_ON_BUILD); //$NON-NLS-1$
	}
}
