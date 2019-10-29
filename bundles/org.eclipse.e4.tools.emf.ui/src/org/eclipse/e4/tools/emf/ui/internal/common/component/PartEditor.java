/*******************************************************************************
 * Copyright (c) 2010, 2017 BestSolution.at and others.
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
 * Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 437951, Ongoing Maintenance
 * Olivier Prouvost <olivier@opcoach.com> - Bug 472658, 412567
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class PartEditor extends AbstractPartEditor<MPart> {

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_Part);
	}

	@Override
	public String getLabel(Object element) {
		if (element == BasicPackageImpl.Literals.PART) {
			return Messages.PartEditor_Part;
		}
		return Messages.PartEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PartEditor_Description;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		Composite composite = super.doGetEditor(parent, object);

		getMaster().setValue((MPart) object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);

		return composite;
	}

	@Override
	public String getDetailLabel(Object element) {
		return getLocalizedLabel((MUILabel) element);
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__ICON_URI),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}
}
