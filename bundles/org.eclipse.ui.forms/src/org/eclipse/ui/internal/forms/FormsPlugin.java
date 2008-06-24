/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms;

import org.eclipse.ui.internal.forms.widgets.FormsResources;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class FormsPlugin extends AbstractUIPlugin {

	public FormsPlugin() {
	}
	
	public void stop(BundleContext context) throws Exception {
		try {
			FormsResources.shutdown();
		} finally {
			super.stop(context);
		}
	}

}
