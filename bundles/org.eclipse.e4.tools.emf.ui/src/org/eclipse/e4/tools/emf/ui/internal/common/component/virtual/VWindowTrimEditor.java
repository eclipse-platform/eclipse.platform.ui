/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.List;

import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.ecore.EReference;

public class VWindowTrimEditor extends AbstractVTrimEditor<MTrimmedWindow> {

	@Override
	protected String getActionLabel() {
		return Messages.VWindowTrimEditor_AddTrim;
	}

	@Override
	protected List<?> getTrimBars(Object master) {
		if (master instanceof MTrimmedWindow) {
			return ((MTrimmedWindow) master).getTrimBars();
		}
		return null;
	}

	@Override
	protected EReference getTrimBarFeature() {
		return BasicPackageImpl.Literals.TRIMMED_WINDOW__TRIM_BARS;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VWindowTrimEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VWindowTrimEditor_TreeLabelDescription;
	}

}