/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import java.util.Comparator;

import org.eclipse.core.runtime.IPath;


/**
 * @since 3.3
 *
 */
public class PathComparator implements Comparator<IPath> {

	@Override
	public int compare(IPath arg0, IPath arg1) {
		return arg0.toString().compareTo(arg1.toString());
	}

}
