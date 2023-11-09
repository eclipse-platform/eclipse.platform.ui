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
 * Steven Spungin <steven@spungin.tv> - Bug 424730
 * Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 412567
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.objectdata.ObjectViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import jakarta.inject.Inject;

public class DirectMenuItemEditor extends MenuItemEditor<MDirectMenuItem> {
	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public DirectMenuItemEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_DirectMenuItem);
	}

	@Override
	protected CTabFolder createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MDirectMenuItem> master,
			boolean isImport) {
		if (!isImport) {
			final CTabFolder folder = super.createForm(parent, context, master, isImport);
			createInstanceInspection(folder);
			return folder;
		}
		return super.createForm(parent, context, master, isImport);
	}

	private void createInstanceInspection(CTabFolder folder) {
		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_RuntimeContributionInstance);
		final Composite container = new Composite(folder, SWT.NONE);
		container.setLayout(new GridLayout());
		item.setControl(container);

		final ObjectViewer objectViewer = new ObjectViewer();
		final TreeViewer viewer = objectViewer.createViewer(container,
				ApplicationPackageImpl.Literals.CONTRIBUTION__OBJECT, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

	}

	@Override
	public String getLabel(Object element) {
		return Messages.DirectMenuItemEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.DirectMenuItemEditor_Description;
	}

	@Override
	protected void createFormSubTypeForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MDirectMenuItem> master) {

		ControlFactory.createClassURIField(parent, Messages, this, Messages.DirectMenuItemEditor_ClassURI,
				ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI,
				getEditor().getContributionCreator(MenuPackageImpl.Literals.DIRECT_MENU_ITEM), project, context,
				eclipseContext);

	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		final WritableList<MExpression> list = new WritableList<>();
		MUIElement uiElement = (MUIElement) element;

		if (uiElement.getVisibleWhen() != null) {
			list.add(0, uiElement.getVisibleWhen());
		}

		E4Properties.visibleWhen().observe(uiElement).addValueChangeListener(event -> {
			if (event.diff.getOldValue() != null) {
				list.remove(event.diff.getOldValue());
			}

			if (event.diff.getNewValue() != null) {
				list.add(0, event.diff.getNewValue());
			}
		});

		return list;
	}
}
