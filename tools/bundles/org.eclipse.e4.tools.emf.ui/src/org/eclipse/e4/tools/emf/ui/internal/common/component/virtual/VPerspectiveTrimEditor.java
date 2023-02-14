/*******************************************************************************
 * Copyright (c) 2019 Airbus Defence and Space GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Benedikt Kuntz <benedikt.kuntz@airbus.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.List;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl.Literals;
import org.eclipse.emf.ecore.EReference;

public class VPerspectiveTrimEditor extends AbstractVTrimEditor<MPerspective> {

	@Override
	protected String getActionLabel() {
		return Messages.VPerspectiveTrimEditor_AddTrim;
	}

	@Override
	protected List<?> getTrimBars(Object master) {
		if (master instanceof MPerspective) {
			return ((MPerspective) master).getTrimBars();
		}
		return null;
	}

	@Override
	protected EReference getTrimBarFeature() {
		return Literals.PERSPECTIVE__TRIM_BARS;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VPerspectiveTrimEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VPerspectiveTrimEditor_TreeLabelDescription;
	}

}