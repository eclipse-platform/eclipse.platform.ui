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
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BindingContextSelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class BindingTableEditor extends AbstractComponentEditor<MBindingTable> {

	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private final List<Action> actions = new ArrayList<>();

	@Inject
	public BindingTableEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.BindingTableEditor_AddKeyBinding,
				createImageDescriptor(ResourceProvider.IMG_KeyBinding)) {
			@Override
			public void run() {
				handleAddKeyBinding();
			}
		});
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_BindingTable);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.BindingTableEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.BindingTableEditor_Description;
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

		getMaster().setValue((MBindingTable) object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			IObservableValue<MBindingTable> master,
			boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ModelTooling_Common_Id);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t),
					E4Properties.elementId(getEditingDomain()).observeDetail(getMaster()));
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.BindingTableEditor_ContextId);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEditable(false);

			//			@SuppressWarnings("unchecked")
			//			IValueProperty<MBindingTable, String> elemId = EMFEditProperties.value(getEditingDomain(),
			//					FeaturePath.fromList(CommandsPackageImpl.Literals.BINDING_TABLE__BINDING_CONTEXT,
			//							ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID));
			//			context.bindValue(textProp.observeDelayed(200, t), elemId.observeDetail(getMaster()));

			// TODO: Verify that this works
			context.bindValue(textProp.observeDelayed(200, t), E4Properties.bindingContext()
					.value(E4Properties.elementId(getEditingDomain())).observeDetail(getMaster()));

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final BindingContextSelectionDialog dialog = new BindingContextSelectionDialog(b.getShell(),
							getEditor().getModelProvider(), Messages);
					if (dialog.open() == Window.OK) {
						final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
								CommandsPackageImpl.Literals.BINDING_TABLE__BINDING_CONTEXT, dialog.getSelectedContext());
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}

		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_PICKER),
					this, CommandsPackageImpl.Literals.BINDING_TABLE__BINDINGS) {
				@Override
				protected void addPressed() {
					handleAddKeyBinding();
				}

				@Override
				protected List<?> getContainerChildren(Object master) {
					return ((MBindingTable) master).getBindings();
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(Messages.BindingTableEditor_Keybindings);

			final TableViewer viewer = pickList.getList();

			viewer.setInput(E4Properties.bindings().observeDetail(getMaster()));
		}

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_ApplicationElement_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MBindingTable.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return E4Properties.bindings().observe((MBindingTable) element);
	}

	@Override
	public String getDetailLabel(Object element) {
		final MBindingTable cmd = (MBindingTable) element;
		if (cmd.getBindingContext() != null && cmd.getBindingContext().getName() != null
				&& cmd.getBindingContext().getName().trim().length() > 0) {
			return cmd.getBindingContext().getName();
		}

		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.BINDING_TABLE__BINDING_CONTEXT,
				CommandsPackageImpl.Literals.BINDING_CONTEXT__NAME) };
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	protected void handleAddKeyBinding() {
		final MKeyBinding handler = MCommandsFactory.INSTANCE.createKeyBinding();
		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
				CommandsPackageImpl.Literals.BINDING_TABLE__BINDINGS, handler);
		setElementId(handler);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(handler);
		}
	}
}
