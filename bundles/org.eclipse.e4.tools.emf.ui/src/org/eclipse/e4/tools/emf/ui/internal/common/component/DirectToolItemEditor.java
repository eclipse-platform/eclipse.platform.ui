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

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.objectdata.ObjectViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
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

public class DirectToolItemEditor extends ToolItemEditor<MDirectToolItem> {
	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public DirectToolItemEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_DirectToolItem);
	}

	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context,
			WritableValue<MDirectToolItem> master) {

		ControlFactory.createClassURIField(parent, Messages, this, Messages.DirectMenuItemEditor_ClassURI,
				ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI,
				getEditor().getContributionCreator(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM), project, context,
				eclipseContext);

	}

	@Override
	protected CTabFolder createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MDirectToolItem> master,
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
		return Messages.DirectToolItemEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.DirectToolItemEditor_Description;
	}

	@Override
	public IObservableList<Object> getChildList(Object element) {
		final IObservableList<Object> list = super.getChildList(element);
		MDirectToolItem toolElement = (MDirectToolItem) element;

		if (toolElement.getVisibleWhen() != null) {
			list.add(0, toolElement.getVisibleWhen());
		}

		E4Properties.visibleWhen().observe(toolElement).addValueChangeListener(event -> {
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