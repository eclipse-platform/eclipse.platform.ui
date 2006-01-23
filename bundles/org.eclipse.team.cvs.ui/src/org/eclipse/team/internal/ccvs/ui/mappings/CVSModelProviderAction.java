/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.mapping.ModelProviderAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public abstract class CVSModelProviderAction extends ModelProviderAction {

	public CVSModelProviderAction(ISynchronizePageConfiguration configuration) {
		super(null, configuration);
		Utils.initAction(this, getBundleKeyPrefix(), Policy.getActionBundle());
	}
	
	/**
	 * Return the key to the action text in the resource bundle.
	 * The default is the fully qualified class name followed by a dot (.).
	 * @return the bundle key prefix
	 */
	protected String getBundleKeyPrefix() {
		return getClass().getName()  + "."; //$NON-NLS-1$
	}

}
