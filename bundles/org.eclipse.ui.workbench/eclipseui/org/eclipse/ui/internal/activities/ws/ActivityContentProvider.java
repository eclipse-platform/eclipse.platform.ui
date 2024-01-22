/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.Collection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.ui.activities.IActivityManager;

/**
 * @since 3.0
 */
public class ActivityContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		Object[] activities = new Object[0];
		if (inputElement instanceof IActivityManager) {
			activities = ((IActivityManager) inputElement).getDefinedActivityIds().toArray();
		} else if (inputElement instanceof Collection) {
			activities = ((Collection<?>) inputElement).toArray();
		}
		return activities;
	}
}
