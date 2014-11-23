/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

@SuppressWarnings("restriction")
public class CommandParameterEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	private StackLayout stackLayout;

	@Inject
	public CommandParameterEditor() {
		super();
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.COMMAND_PARAMETER__NAME),
			FeaturePath.fromList(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID) };
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_CommandParameter);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.CommandParameterEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		final MCommandParameter param = (MCommandParameter) element;
		if (param.getName() != null && param.getName().trim().length() > 0) {
			return param.getName().trim();
		} else if (param.getElementId() != null && param.getElementId().trim().length() > 0) {
			return param.getElementId().trim();
		}
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.CommandParameterEditor_TreeLabelDescritpion;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			if (getEditor().isModelFragment()) {
				composite = new Composite(parent, SWT.NONE);
				stackLayout = new StackLayout();
				composite.setLayout(stackLayout);
				createForm(composite, context, getMaster(), false);
				createForm(composite, context, getMaster(), true);
			} else {
				composite = createForm(parent, context, getMaster(), false);
			}
		}

		if (getEditor().isModelFragment()) {
			Control topControl;
			if (Util.isImport((EObject) object)) {
				topControl = composite.getChildren()[1];
			} else {
				topControl = composite.getChildren()[0];
			}

			if (stackLayout.topControl != topControl) {
				stackLayout.topControl = topControl;
				composite.layout(true, true);
			}
		}

		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master, boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp,
			EMFEditProperties
				.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID),
			Messages.ModelTooling_Empty_Warning);
		ControlFactory.createTextField(parent, Messages.CommandParameterEditor_Name, master, context, textProp,
			EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND_PARAMETER__NAME),
			Messages.ModelTooling_Empty_Warning);
		ControlFactory.createTextField(parent, Messages.CommandParameterEditor_TypeId, master, context, textProp,
			EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND_PARAMETER__TYPE_ID));

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandParameterEditor_Optional);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			final Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));

			final IEMFEditValueProperty mprop = EMFEditProperties.value(getEditingDomain(),
				CommandsPackageImpl.Literals.COMMAND_PARAMETER__OPTIONAL);
			final IWidgetValueProperty uiProp = WidgetProperties.selection();

			context.bindValue(uiProp.observe(checkbox), mprop.observeDetail(master));
		}

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
			ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
			ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MCommandParameter.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}
