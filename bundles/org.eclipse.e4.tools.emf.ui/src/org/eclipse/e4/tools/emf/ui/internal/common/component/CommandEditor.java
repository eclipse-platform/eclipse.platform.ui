/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.CommandCategorySelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
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
import org.eclipse.swt.custom.StackLayout;
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
import org.eclipse.swt.widgets.Text;

public class CommandEditor extends AbstractComponentEditor {

	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private List<Action> actions = new ArrayList<Action>();
	private MessageFormat newCommandParameterName;

	private IEMFEditListProperty COMMAND__PARAMETERS = EMFEditProperties.list(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS);

	@Inject
	public CommandEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.CommandEditor_AddCommandParameter, createImageDescriptor(ResourceProvider.IMG_CommandParameter)) {
			@Override
			public void run() {
				handleAddCommandParameter();
			}
		});

		newCommandParameterName = new MessageFormat(Messages.CommandEditor_NewCommandParameter);
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_Command);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.CommandEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.CommandEditor_Description;
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

	private Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue master, boolean isImport) {
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

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, Messages.ModelTooling_CommandId_tooltip, master, context, textProp, EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID), Messages.ModelTooling_Empty_Warning);
		ControlFactory.createTextField(parent, Messages.CommandEditor_Name, master, context, textProp, EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME));
		ControlFactory.createTextField(parent, Messages.CommandEditor_LabelDescription, master, context, textProp, EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND__DESCRIPTION));

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_Category);
			l.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));

			Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			t.setEditable(false);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FeaturePath.fromList(CommandsPackageImpl.Literals.COMMAND__CATEGORY, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID)).observeDetail(getMaster()));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					CommandCategorySelectionDialog dialog = new CommandCategorySelectionDialog(b.getShell(), getEditor().getModelProvider(), (MCommand) getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_Parameters);
			l.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));

			final TableViewer viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
			ObservableListContentProvider cp = new ObservableListContentProvider();
			viewer.setContentProvider(cp);
			viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));

			GridData gd = new GridData(GridData.FILL_BOTH);
			viewer.getControl().setLayoutData(gd);

			IEMFEditListProperty mProp = EMFEditProperties.list(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS);
			viewer.setInput(mProp.observeDetail(getMaster()));

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
							MCommand container = (MCommand) getMaster().getValue();
							int idx = container.getParameters().indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS, obj, idx);

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
							MCommand container = (MCommand) getMaster().getValue();
							int idx = container.getParameters().indexOf(obj) + 1;
							if (idx < container.getParameters().size()) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS, obj, idx);

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
					handleAddCommandParameter();
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
						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS, keybinding);
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_ApplicationElement_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MCommand.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return COMMAND__PARAMETERS.observe(element);
	}

	@Override
	public String getDetailLabel(Object element) {
		MCommand cmd = (MCommand) element;
		if (cmd.getCommandName() != null && cmd.getCommandName().trim().length() > 0) {
			return translate(cmd.getCommandName());
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME) };
	}

	protected void handleAddCommandParameter() {
		MCommandParameter param = MCommandsFactory.INSTANCE.createCommandParameter();
		setElementId(param);
		param.setName(newCommandParameterName.format(new Object[] { getParameterCount() }));

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS, param);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(param);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	/**
	 * Returns the current amount of {@link MCommandParameter}s the edited
	 * {@link MCommand} has.
	 *
	 * @return the amount of {@link MCommandParameter}s of the edited
	 *         {@link MCommand}
	 */
	private int getParameterCount() {
		// getChildList() will always create a new IObservableList and
		// this method uses the normal EMF way to retrieve the count manually.
		EObject command = (EObject) getMaster().getValue();
		List<?> commandParameters = (List<?>) command.eGet(CommandsPackageImpl.Literals.COMMAND__PARAMETERS);
		return commandParameters.size();
	}
}
