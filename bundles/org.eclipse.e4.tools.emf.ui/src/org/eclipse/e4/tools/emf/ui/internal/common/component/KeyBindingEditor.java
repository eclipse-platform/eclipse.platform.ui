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
 *     Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.e4.tools.emf.ui.common.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.tools.emf.ui.common.CommandToStringConverter;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.KeyBindingCommandSelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MKeySequence;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class KeyBindingEditor extends AbstractComponentEditor {

	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private List<Action> actions = new ArrayList<Action>();

	private IEMFEditListProperty KEY_BINDING__PARAMETERS = EMFEditProperties.list(getEditingDomain(), CommandsPackageImpl.Literals.KEY_BINDING__PARAMETERS);

	@Inject
	private IModelResource resource;

	@Inject
	public KeyBindingEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.KeyBindingEditor_AddParameter, createImageDescriptor(ResourceProvider.IMG_Parameter)) {
			@Override
			public void run() {
				handleAddParameter();
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_KeyBinding);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.KeyBindingEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.KeyBindingEditor_Description;
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

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp, EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID));

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.KeyBindingEditor_Sequence);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			Binding binding = context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.KEY_SEQUENCE__KEY_SEQUENCE).observeDetail(getMaster()), new UpdateValueStrategy().setBeforeSetValidator(new BindingValidator()), new UpdateValueStrategy());
			Util.addDecoration(t, binding);
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.KeyBindingEditor_Command);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEditable(false);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.KEY_BINDING__COMMAND).observeDetail(getMaster()), new UpdateValueStrategy(), new UpdateValueStrategy().setConverter(new CommandToStringConverter(Messages)));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					KeyBindingCommandSelectionDialog dialog = new KeyBindingCommandSelectionDialog(b.getShell(), (MKeyBinding) getMaster().getValue(), resource, Messages);
					dialog.open();
				}
			});
		}

		{
			E4PickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_PICKER), Messages, this, CommandsPackageImpl.Literals.KEY_BINDING__PARAMETERS) {
				@Override
				protected void addPressed() {
					handleAddParameter();
				}

				@Override
				protected List<?> getContainerChildren(Object master) {
					return ((MCommand) master).getParameters();
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(Messages.KeyBindingEditor_Parameters);

			final TableViewer viewer = pickList.getList();

			viewer.setInput(KEY_BINDING__PARAMETERS.observeDetail(getMaster()));
		}
		//

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MKeyBinding.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return KEY_BINDING__PARAMETERS.observe(element);
	}

	@Override
	public String getDetailLabel(Object element) {
		MKeySequence seq = (MKeySequence) element;
		if (seq.getKeySequence() != null && seq.getKeySequence().trim().length() > 0) {
			return seq.getKeySequence();
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.KEY_SEQUENCE__KEY_SEQUENCE) };
	}

	class BindingValidator implements IValidator {

		@Override
		public IStatus validate(Object value) {
			int statusCode = getEditor().isLiveModel() ? IStatus.ERROR : IStatus.WARNING;
			if (value != null && value.toString().trim().length() > 0) {
				try {
					KeySequence keySequence = KeySequence.getInstance(value.toString());
					if (!keySequence.isComplete()) {
						return new Status(statusCode, Plugin.ID, Messages.KeyBindingEditor_SequenceNotComplete); 
					}
					if (keySequence.isEmpty()) {
						return new Status(statusCode, Plugin.ID, Messages.KeyBindingEditor_SequenceEmpty); 
					}
					if (!value.toString().toUpperCase().equals(value.toString())) {
						return new Status(IStatus.ERROR, Plugin.ID, Messages.KeyBindingEditor_SequenceLowercase); 
					}

					return Status.OK_STATUS;
				} catch (Exception e) {
					return new Status(statusCode, Plugin.ID, e.getMessage(), e); 
				}
			}

			return new Status(statusCode, Plugin.ID, Messages.KeyBindingEditor_SequenceEmpty); 
		}
	}

	protected void handleAddParameter() {
		MKeyBinding item = (MKeyBinding) getMaster().getValue();
		MParameter param = MCommandsFactory.INSTANCE.createParameter();
		setElementId(param);

		Command cmd = AddCommand.create(getEditingDomain(), item, CommandsPackageImpl.Literals.KEY_BINDING__PARAMETERS, param);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
