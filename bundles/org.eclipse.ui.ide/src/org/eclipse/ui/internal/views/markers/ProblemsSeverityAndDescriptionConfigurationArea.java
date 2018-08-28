/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ProblemsSeverityAndDescriptionConfigurationArea} is the
 * configuration area for the problems view.
 * @since 3.4
 *
 */
public class ProblemsSeverityAndDescriptionConfigurationArea extends
		SeverityAndDescriptionConfigurationArea {

	@Override
	public void createContents(Composite parent) {

		super.createContents(parent);
		createSeverityGroup(parent);

	}

}
