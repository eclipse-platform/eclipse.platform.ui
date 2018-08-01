/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.navigator.resources.workbench.ResourceExtensionLabelProvider;

public class NestedProjectsLabelProvider extends ResourceExtensionLabelProvider {

	@Override
	protected String decorateText(String input, Object element) {
		super.decorateText(input, element);
		if (! (element instanceof IProject)) {
			return input;
		}
		IProject project = (IProject)element;
		IPath location = project.getLocation();
		if (location != null && !location.lastSegment().equals(project.getName())) {
			return input + " (in " + location.lastSegment() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return input;
	}

}
