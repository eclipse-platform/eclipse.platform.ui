/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class BindingContextEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	private EStackLayout stackLayout;
	private IListProperty BINDING_CONTEXT__CHILDREN = EMFProperties.list(CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN);

	private List<Action> actions = new ArrayList<Action>();

	@Inject
	public BindingContextEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.BindingContextEditor_AddContext, createImageDescriptor(ResourceProvider.IMG_BindingContext)) {
			@Override
			public void run() {
				handleAddContext();
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_BindingContext);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.BindingContextEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		MBindingContext context = (MBindingContext) element;
		if (context.getName() != null && context.getName().trim().length() > 0) {
			return context.getName().trim();
		} else if (context.getElementId() != null && context.getElementId().trim().length() > 0) {
			return context.getElementId().trim();
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.BINDING_CONTEXT__NAME), FeaturePath.fromList(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID) };
	}

	@Override
	public String getDescription(Object element) {
		return Messages.BindingContextEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			if (getEditor().isModelFragment()) {
				composite = new Composite(parent, SWT.NONE);
				stackLayout = new EStackLayout();
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
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp, EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID));
		ControlFactory.createTextField(parent, Messages.BindingContextEditor_Name, master, context, textProp, EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.BINDING_CONTEXT__NAME), Messages.BindingContextEditor_NameWarning);
		ControlFactory.createTextField(parent, Messages.BindingContextEditor_Description, master, context, textProp, EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.BINDING_CONTEXT__DESCRIPTION));

		Label l = new Label(parent, SWT.NONE);
		l.setText(Messages.BindingContextEditor_Subcontexts);
		l.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));

		final TableViewer viewer = new TableViewer(parent);
		ObservableListContentProvider cp = new ObservableListContentProvider();
		viewer.setContentProvider(cp);

		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);
		viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));

		IEMFListProperty prop = EMFEditProperties.list(getEditingDomain(), CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN);
		viewer.setInput(prop.observeDetail(master));

		Composite buttonComp = new Composite(parent, SWT.NONE);
		buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
		GridLayout gl = new GridLayout();
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonComp.setLayout(gl);

		Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Up);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_up));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						MBindingContext container = (MBindingContext) getMaster().getValue();
						int idx = container.getChildren().indexOf(obj) - 1;
						if (idx >= 0) {
							Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN, obj, idx);

							if (cmd.canExecute()) {
								getEditingDomain().getCommandStack().execute(cmd);
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Down);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_down));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						MBindingContext container = (MBindingContext) getMaster().getValue();
						int idx = container.getChildren().indexOf(obj) + 1;
						if (idx < container.getChildren().size()) {
							Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN, obj, idx);

							if (cmd.canExecute()) {
								getEditingDomain().getCommandStack().execute(cmd);
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_AddEllipsis);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddContext();
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Remove);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_delete));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					List<?> keybinding = ((IStructuredSelection) viewer.getSelection()).toList();
					Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN, keybinding);
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			}
		});

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_ApplicationElement_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MBindingContext.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return BINDING_CONTEXT__CHILDREN.observe(element);
	}

	protected void handleAddContext() {
		MBindingContext eObject = MCommandsFactory.INSTANCE.createBindingContext();
		setElementId(eObject);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);

		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
