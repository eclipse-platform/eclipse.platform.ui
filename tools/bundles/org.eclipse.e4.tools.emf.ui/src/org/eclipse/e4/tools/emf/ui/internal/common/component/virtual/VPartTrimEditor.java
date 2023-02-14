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

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals;
import org.eclipse.emf.ecore.EReference;

public class VPartTrimEditor extends AbstractVTrimEditor<MPart> {

	@Override
	protected String getActionLabel() {
		return Messages.VPartTrimEditor_AddTrim;
	}

	@Override
	protected List<?> getTrimBars(Object master) {
		if (master instanceof MPart) {
			return ((MPart) master).getTrimBars();
		}
		return null;
	}

	@Override
	protected EReference getTrimBarFeature() {
		return Literals.PART__TRIM_BARS;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VPartTrimEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VPartTrimEditor_TreeLabelDescription;
	}

}