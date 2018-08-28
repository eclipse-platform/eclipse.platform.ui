/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		if (arg0 == arg1) {
			return 0;
		}
		int res = 0;
		// First, Device
		String device0 = arg0.getDevice();
		String device1 = arg1.getDevice();
		if (device0 != null && device1 == null) {
			return +1;
		}
		if (device0 == null && device1 != null) {
			return -1;
		}
		if (device0 != null && device1 != null) {
			res = device0.compareTo(device1);
			if (res != 0 && !device0.equalsIgnoreCase(device1)) {
				return res;
			}
		}
		// then, Absolute
		res = Boolean.compare(arg0.isAbsolute(), arg1.isAbsolute());
		if (res != 0) {
			return res;
		}
		// then, UNC
		res = Boolean.compare(arg0.isUNC(), arg1.isUNC());
		if (res != 0) {
			return res;
		}
		// then, Segments
		for (int i = 0; i < Math.min(arg0.segmentCount(), arg1.segmentCount()); i++) {
			res = arg0.segment(i).compareTo(arg1.segment(i));
			if (res != 0) {
				return res;
			}
		}
		return arg0.segmentCount() - arg1.segmentCount();
	}

}
