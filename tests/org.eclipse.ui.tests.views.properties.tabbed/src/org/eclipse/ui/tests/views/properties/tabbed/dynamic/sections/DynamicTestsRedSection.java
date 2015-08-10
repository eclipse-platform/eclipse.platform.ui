/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
