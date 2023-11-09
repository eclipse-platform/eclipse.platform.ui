/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ParameterIdSelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import jakarta.inject.Inject;

public class ParameterEditor extends AbstractComponentEditor<MParameter> {
	private Composite composite;
	private EMFDataBindingContext context;

	private StackLayout stackLayout;

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	public ParameterEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_Parameter);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.ParameterEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		final MParameter param = (MParameter) element;
		if (param.getName() != null && param.getName().trim().length() > 0) {
			return param.getName().trim();
		} else if (param.getElementId() != null && param.getElementId().trim().length() > 0) {
			return param.getElementId().trim();
		}
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.ParameterEditor_TreeLabelDescription;
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
				composite.requestLayout();
			}
		}

		getMaster().setValue((MParameter) object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue<MParameter> master,
			boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp,
				E4Properties.elementId(getEditingDomain()));
		createParameterNameRow(parent, textProp);
		ControlFactory.createTextField(parent, Messages.ParameterEditor_Value, master, context, textProp,
				E4Properties.parameterValue(getEditingDomain()));

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MParameter.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	private void createParameterNameRow(Composite parent, IWidgetValueProperty<Text, String> textProp) {
		{
			final Label commandParameterIdLabel = new Label(parent, SWT.NONE);
			commandParameterIdLabel.setText(Messages.ParameterEditor_Command_Parameter_ID);
			commandParameterIdLabel.setLayoutData(new GridData());

			final Text commandParameterIdValue = new Text(parent, SWT.BORDER);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			commandParameterIdValue.setLayoutData(gd);
			TextPasteHandler.createFor(commandParameterIdValue);
			context.bindValue(textProp.observeDelayed(200, commandParameterIdValue),
					E4Properties.parameterName(getEditingDomain()).observeDetail(getMaster()));
		}

		Button b = ControlFactory.createFindButton(parent, resourcePool);
		b.addSelectionListener(new ChooseParameterButtonSelectionListener());
	}

	private final class ChooseParameterButtonSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final WritableValue<MParameter> master = getMaster();
			if (master == null || master.getValue() == null) {
				return;
			}

			final IEclipseContext staticContext = EclipseContextFactory.create("ParameterIdSelectionDialog static context"); //$NON-NLS-1$
			staticContext.set(MParameter.class, master.getValue());
			final ParameterIdSelectionDialog dialog = ContextInjectionFactory.make(ParameterIdSelectionDialog.class, eclipseContext, staticContext);
			dialog.open();
		}
	}
}
