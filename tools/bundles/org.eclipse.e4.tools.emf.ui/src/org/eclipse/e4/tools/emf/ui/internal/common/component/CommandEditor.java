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
 * Steven Spungin <steven@spungin.tv> - Bug 437951
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.CommandCategorySelectionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.CommandIconDialogEditor;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class CommandEditor extends AbstractComponentEditor<MCommand> {

	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private final List<Action> actions = new ArrayList<>();
	private MessageFormat newCommandParameterName;

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	public CommandEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.CommandEditor_AddCommandParameter,
				createImageDescriptor(ResourceProvider.IMG_CommandParameter)) {
			@Override
			public void run() {
				handleAddCommandParameter();
			}
		});

		newCommandParameterName = new MessageFormat(Messages.CommandEditor_NewCommandParameter);
	}

	@Override
	public Image getImage(Object element) {
		Image result = null;

		if (element instanceof MCommand command) {
			result = getImageFromIconURI(command.getCommandIconURI(), shouldBeGrey(command));
		}

		if (result == null) {
			result = getImage(element, ResourceProvider.IMG_Command);
		}
		return result;
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
				composite.requestLayout();
			}
		}

		getMaster().setValue((MCommand) object);
		enableIdGenerator(CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue<MCommand> master,
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

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id,
				Messages.ModelTooling_CommandId_tooltip, master, context, textProp,
				E4Properties.elementId(getEditingDomain()), Messages.ModelTooling_Empty_Warning);
		ControlFactory.createTextField(parent, Messages.CommandEditor_Name, master, context, textProp,
				E4Properties.commandName(getEditingDomain()));
		ControlFactory.createTextField(parent, Messages.CommandEditor_LabelDescription, master, context, textProp,
				E4Properties.commandDescription(getEditingDomain()));

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_Category);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			t.setEditable(false);
			// TODO: Verify that this works
			context.bindValue(textProp.observeDelayed(200, t), E4Properties.category()
					.value(E4Properties.elementId(getEditingDomain())).observeDetail(getMaster()));

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final CommandCategorySelectionDialog dialog = new CommandCategorySelectionDialog(b.getShell(),
							getEditor().getModelProvider(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_IconURI);
			l.setLayoutData(new GridData());
			l.setToolTipText(Messages.CommandEditor_IconURI_Tooltip);

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200, t),
					E4Properties.commandIcon(getEditingDomain()).observeDetail(getMaster()));

			new ImageTooltip(t, Messages, this);

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final CommandIconDialogEditor dialog = new CommandIconDialogEditor(b.getShell(), eclipseContext,
							project,
							getEditingDomain(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_PICKER),
					this, CommandsPackageImpl.Literals.COMMAND__PARAMETERS) {
				@Override
				protected void addPressed() {
					handleAddCommandParameter();
				}

				@Override
				protected List<?> getContainerChildren(Object master) {
					return ((MCommand) master).getParameters();
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

			pickList.setText(Messages.CommandEditor_Parameters);

			final TableViewer viewer = pickList.getList();

			viewer.setInput(E4Properties.commandParameters(getEditingDomain()).observeDetail(getMaster()));
		}

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_ApplicationElement_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MCommand.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return E4Properties.commandParameters(getEditingDomain()).observe((MCommand) element);
	}

	@Override
	public String getDetailLabel(Object element) {
		final MCommand cmd = (MCommand) element;
		if (cmd.getCommandName() != null && cmd.getCommandName().trim().length() > 0) {
			return translate(cmd.getCommandName());
		}
		return cmd.getElementId();
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME) };
	}

	protected void handleAddCommandParameter() {
		final MCommandParameter param = MCommandsFactory.INSTANCE.createCommandParameter();
		setElementId(param);
		param.setName(newCommandParameterName.format(new Object[] { getParameterCount() }));

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
				CommandsPackageImpl.Literals.COMMAND__PARAMETERS, param);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(param);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	/**
	 * Returns the current amount of {@link MCommandParameter}s the edited {@link MCommand} has.
	 *
	 * @return the amount of {@link MCommandParameter}s of the edited {@link MCommand}
	 */
	private int getParameterCount() {
		// getChildList() will always create a new IObservableList and
		// this method uses the normal EMF way to retrieve the count manually.
		final EObject command = (EObject) getMaster().getValue();
		final List<?> commandParameters = (List<?>) command.eGet(CommandsPackageImpl.Literals.COMMAND__PARAMETERS);
		return commandParameters.size();
	}
}
