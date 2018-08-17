/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.sections;

/**
 * A section for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsRedSection extends DynamicTestsAbstractLabelSection {

	@Override
	public String getGroup() {
		return "Color"; //$NON-NLS-1$
	}

	@Override
	public String getLabel() {
		return "A section for red elements."; //$NON-NLS-1$
	}

}
