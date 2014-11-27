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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ApplicationEditor extends AbstractComponentEditor {

	private Composite composite;
	private EMFDataBindingContext context;

	private final IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties
		.list(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
	private final IListProperty BINDING_CONTAINER__BINDINGS = EMFProperties
		.list(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES);
	private final IListProperty APPLICATION__COMMANDS = EMFProperties
		.list(ApplicationPackageImpl.Literals.APPLICATION__COMMANDS);
	// private IListProperty APPLICATION__DIALOGS =
	// EMFProperties.list(ApplicationPackageImpl.Literals.APPLICATION__DIALOGS);
	private final IListProperty PART_DESCRIPTOR_CONTAINER__DESCRIPTORS = EMFProperties
		.list(BasicPackageImpl.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);
	private final IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties
		.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	private final IListProperty APPLICATION__ADDONS = EMFProperties
		.list(ApplicationPackageImpl.Literals.APPLICATION__ADDONS);
	private final IListProperty MENU_CONTRIBUTIONS = EMFProperties
		.list(MenuPackageImpl.Literals.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS);
	private final IListProperty TOOLBAR_CONTRIBUTIONS = EMFProperties
		.list(MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS);
	private final IListProperty TRIM_CONTRIBUTIONS = EMFProperties
		.list(MenuPackageImpl.Literals.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS);
	private final IListProperty APPLICATION__SNIPPETS = EMFProperties
		.list(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS);
	private final IListProperty APPLICATION__CATEGORIES = EMFProperties
		.list(ApplicationPackageImpl.Literals.APPLICATION__CATEGORIES);

	private final IListProperty BINDING_TABLE_CONTAINER__ROOT_CONTEXT = EMFProperties
		.list(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__ROOT_CONTEXT);

	@Inject
	@Optional
	private IProject project;

	private final List<Action> actions = new ArrayList<Action>();

	@Inject
	public ApplicationEditor() {
		super();
	}

	@PostConstruct
	void init() {
		// actions.add(new Action("Command Wizard ...") {
		// @Override
		// public void run() {
		// doCreateCommandWizard();
		// }
		// });
	}

	@Override
	public List<Action> getActions(Object element) {
		return actions;
	}

	void doCreateCommandWizard() {
		final WizardDialog dialog = new WizardDialog(composite.getShell(), new CommandWizard((MApplication) getMaster()
			.getValue()));
		dialog.open();
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			if (((MUIElement) element).isToBeRendered()) {
				return createImage(ResourceProvider.IMG_Application);
			}
			return createImage(ResourceProvider.IMG_Tbr_Application);
		}
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.ApplicationEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.ApplicationEditor_Description;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context);
		}
		getMaster().setValue(object);

		return composite;
	}

	protected Composite createForm(Composite parent, EMFDataBindingContext context) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, getMaster(), context, textProp,
			EMFEditProperties
				.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID));

		ControlFactory.createBindingContextWiget(parent, Messages, this, Messages.ApplicationEditor_BindingContexts);

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
			WidgetProperties.selection(),
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
			WidgetProperties.selection(),
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(),
			context, textProp,
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE));
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_Context_Variables,
			UiPackageImpl.Literals.CONTEXT__VARIABLES, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.AddonsEditor_Tags,
			ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
			ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MApplication.class);

		folder.setSelection(0);

		return folder;
	}

	private void createUITreeInspection(CTabFolder folder) {
		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_RuntimeWidgetTree);
		final Composite container = new Composite(folder, SWT.NONE);
		container.setLayout(new GridLayout());
		item.setControl(container);

		final UIViewer objectViewer = new UIViewer();
		final TreeViewer viewer = objectViewer.createViewer(container, UiPackageImpl.Literals.UI_ELEMENT__WIDGET,
			getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
	public IObservableList getChildList(final Object element) {
		final WritableList list = new WritableList();
		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_ADDONS, APPLICATION__ADDONS, element,
			Messages.ApplicationEditor_Addons) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_ROOT_CONTEXTS, BINDING_TABLE_CONTAINER__ROOT_CONTEXT,
			element, Messages.ApplicationEditor_RootContexts) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_BINDING_TABLE, BINDING_CONTAINER__BINDINGS, element,
			Messages.ApplicationEditor_BindingTables) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_HANDLER, HANDLER_CONTAINER__HANDLERS, element,
			Messages.ApplicationEditor_Handlers) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_COMMAND, APPLICATION__COMMANDS, element,
			Messages.ApplicationEditor_Commands) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_CATEGORIES, APPLICATION__CATEGORIES, element,
			Messages.ApplicationEditor_Categories) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_APPLICATION_WINDOWS, ELEMENT_CONTAINER__CHILDREN,
			element, Messages.ApplicationEditor_Windows) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_PART_DESCRIPTORS, PART_DESCRIPTOR_CONTAINER__DESCRIPTORS,
			element, Messages.ApplicationEditor_PartDescriptors) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_MENU_CONTRIBUTIONS, MENU_CONTRIBUTIONS, element,
			Messages.ApplicationEditor_MenuContributions) {
			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_TOOLBAR_CONTRIBUTIONS, TOOLBAR_CONTRIBUTIONS, element,
			Messages.ApplicationEditor_ToolBarContributions) {
			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_TRIM_CONTRIBUTIONS, TRIM_CONTRIBUTIONS, element,
			Messages.ApplicationEditor_TrimContributions) {
			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});
		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_SNIPPETS, APPLICATION__SNIPPETS, element,
			Messages.ApplicationEditor_Snippets) {
			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});
		//
		// MApplication application = (MApplication) element;
		// if (application.getRootContext() != null) {
		// list.add(0, application.getRootContext());
		// }

		// BINDING_TABLE_CONTAINER__ROOT_CONTEXT.observe(element).addValueChangeListener(new
		// IValueChangeListener() {
		//
		// public void handleValueChange(ValueChangeEvent event) {
		// if (event.diff.getOldValue() != null) {
		// list.remove(event.diff.getOldValue());
		// if (getMaster().getValue() == element) {
		// createRemoveRootContext.setSelection(false);
		// }
		// }
		//
		// if (event.diff.getNewValue() != null) {
		// list.add(0, event.diff.getNewValue());
		// if (getMaster().getValue() == element) {
		// createRemoveRootContext.setSelection(true);
		// }
		// }
		// }
		// });

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	class CommandWizard extends Wizard {
		private final MApplication application;

		private HandlerCommandPage handlerPage;
		private KeybindingPage keyPage;
		private MenuWizardPage menuPage;
		private ToolbarWizardPage toolbarPage;

		public CommandWizard(MApplication application) {
			this.application = application;
		}

		@Override
		public void addPages() {
			getShell().setText(Messages.CommandWizard_ShellTitle);
			setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(resourcePool
				.getImageUnchecked(ResourceProvider.IMG_Wizban16_newexp_wiz)));
			handlerPage = new HandlerCommandPage(Messages.ApplicationEditor_HandlerAndCommand);
			addPage(handlerPage);

			keyPage = new KeybindingPage(Messages.ApplicationEditor_Keybinding, application);
			addPage(keyPage);

			menuPage = new MenuWizardPage(Messages.ApplicationEditor_Menu, application);
			addPage(menuPage);

			toolbarPage = new ToolbarWizardPage(Messages.ApplicationEditor_Toolbar, application);
			addPage(toolbarPage);
		}

		@Override
		public boolean performFinish() {

			final MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
			final MHandler handler = CommandsFactoryImpl.eINSTANCE.createHandler();
			MKeyBinding keyBinding = null;

			final String parentId = application.getElementId();

			final String prefix = parentId != null && parentId.trim().length() > 0 ? parentId + "." : ""; //$NON-NLS-1$ //$NON-NLS-2$

			if (handlerPage.idField.getText().trim().length() > 0) {
				command.setElementId(prefix + "commands." + handlerPage.idField.getText().trim()); //$NON-NLS-1$
				handler.setElementId(prefix + "handlers." + handlerPage.idField.getText().trim()); //$NON-NLS-1$
			}

			if (application.getBindingTables().size() != 0) {
				if (keyPage.keyField.getText().trim().length() > 0 && !keyPage.bindtableViewer.getSelection().isEmpty()) {
					keyBinding = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
					keyBinding.setKeySequence(keyPage.keyField.getText().trim());
					keyBinding.setCommand(command);

				}
			}

			command.setCommandName(handlerPage.nameField.getText());
			handler.setCommand(command);

			final CompoundCommand cmd = new CompoundCommand();
			cmd.append(AddCommand.create(getEditingDomain(), application,
				ApplicationPackageImpl.Literals.APPLICATION__COMMANDS, command));
			cmd.append(AddCommand.create(getEditingDomain(), application,
				CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS, handler));

			if (keyBinding != null) {
				cmd.append(AddCommand.create(getEditingDomain(),
					((IStructuredSelection) keyPage.bindtableViewer.getSelection()).getFirstElement(),
					CommandsPackageImpl.Literals.BINDING_TABLE__BINDINGS, keyBinding));
			}

			if (cmd.canExecute()) {
				getEditingDomain().getCommandStack().execute(cmd);
				return true;
			}

			return false;
		}
	}

	class HandlerCommandPage extends WizardPage {
		public Text idField;
		public Text nameField;

		public HandlerCommandPage(String pageName) {
			super(pageName);
		}

		@Override
		public void createControl(Composite parent) {
			setTitle(Messages.ApplicationEditor_Command_Slash_Handler);
			setMessage(Messages.ApplicationEditor_InsertInfosForCommandAndHandler);

			final Composite group = new Composite(parent, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setLayout(new GridLayout(3, false));

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Id);

				idField = new Text(group, SWT.BORDER);
				idField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Name + "*"); //$NON-NLS-1$

				nameField = new Text(group, SWT.BORDER);
				nameField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
				nameField.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						setPageComplete(nameField.getText().trim().length() > 0);
					}
				});
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Class);

				final Text t = new Text(group, SWT.BORDER);
				t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				final Button b = new Button(group, SWT.PUSH | SWT.FLAT);
				b.setText(Messages.ModelTooling_Common_FindEllipsis);
			}

			setControl(group);
		}

		@Override
		public boolean isPageComplete() {
			return nameField.getText().trim().length() > 0;
		}
	}

	class KeybindingPage extends WizardPage {

		private Text keyField;
		private TableViewer bindtableViewer;
		private final MApplication application;

		public KeybindingPage(String pageName, MApplication application) {
			super(pageName);
			this.application = application;
		}

		@Override
		public void createControl(Composite parent) {
			setTitle(Messages.ApplicationEditor_Keybinding);
			setMessage(Messages.ApplicationEditor_InsertInfosForKeybinding);

			final Composite group = new Composite(parent, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setLayout(new GridLayout(2, false));

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Sequence);

				keyField = new Text(group, SWT.BORDER);
				keyField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				keyField.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						bindtableViewer.getControl().setEnabled(isPageComplete());
						setPageComplete(isPageComplete());
					}
				});
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_BindingTable);
				l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

				bindtableViewer = new TableViewer(group);
				bindtableViewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));
				bindtableViewer.setContentProvider(new ArrayContentProvider());
				bindtableViewer.setInput(application.getBindingTables());
				bindtableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						setPageComplete(isPageComplete());
					}
				});
				bindtableViewer.setSelection(new StructuredSelection(application.getBindingTables().get(0)));
				final GridData gd = new GridData(GridData.FILL_BOTH);
				gd.heightHint = bindtableViewer.getTable().getItemHeight() * 5;
				bindtableViewer.getControl().setLayoutData(gd);
				bindtableViewer.getControl().setEnabled(false);
			}

			setControl(group);
		}

		@Override
		public boolean isPageComplete() {
			if (keyField.getText().trim().length() == 0) {
				return true;
			}
			return !bindtableViewer.getSelection().isEmpty();
		}
	}

	class MenuWizardPage extends WizardPage {
		private final MApplication application;
		private Text labelField;
		private Text iconField;
		private ComboViewer typeViewer;
		private TableViewer menuViewer;

		public MenuWizardPage(String pageName, MApplication application) {
			super(pageName);
			this.application = application;
		}

		@Override
		public void createControl(Composite parent) {
			setTitle(Messages.ApplicationEditor_HandledMenuItem);
			setMessage(Messages.ApplicationEditor_InertInfosForAHandledMenuItem);

			final Composite group = new Composite(parent, SWT.NONE);
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Label);

				labelField = new Text(group, SWT.BORDER);
				labelField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Icon);

				iconField = new Text(group, SWT.BORDER);
				iconField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Type);

				typeViewer = new ComboViewer(group, SWT.READ_ONLY);
				typeViewer.setContentProvider(new ArrayContentProvider());
				typeViewer.setInput(ItemType.values());
				typeViewer.setSelection(new StructuredSelection(ItemType.PUSH));
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Parent);
				l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

				menuViewer = new TableViewer(group);
				menuViewer.setLabelProvider(new HiearchyLabelProvider());
				menuViewer.setContentProvider(new ArrayContentProvider());

				final List<MMenu> menuList = new ArrayList<MMenu>();
				final Iterator<EObject> it = EcoreUtil.getAllContents(Collections.singleton(application));
				while (it.hasNext()) {
					final EObject o = it.next();
					if (MenuPackageImpl.Literals.MENU.isSuperTypeOf(o.eClass())) {
						menuList.add((MMenu) o);
					}
				}
				menuViewer.setInput(menuList);
				menuViewer.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						setPageComplete(isPageComplete());
					}
				});
				final GridData gd = new GridData(GridData.FILL_BOTH);
				gd.heightHint = menuViewer.getTable().getItemHeight() * 5;
				menuViewer.getControl().setLayoutData(gd);
				menuViewer.setSelection(new StructuredSelection(menuList.get(0)));
			}

			setControl(group);
		}

		@Override
		public boolean isPageComplete() {
			if (labelField.getText().trim().length() == 0 && iconField.getText().trim().length() == 0) {
				return true;
			}
			return !menuViewer.getSelection().isEmpty();
		}
	}

	class ToolbarWizardPage extends WizardPage {
		private final MApplication application;
		private Text labelField;
		private Text iconField;
		private ComboViewer typeViewer;
		private TableViewer toolbarViewer;

		public ToolbarWizardPage(String pageName, MApplication application) {
			super(pageName);
			this.application = application;
		}

		@Override
		public void createControl(Composite parent) {
			setTitle(Messages.ApplicationEditor_HandledToolbarItem);
			setMessage(Messages.ApplicationEditor_InsertInfosForAToolbarItem);

			final Composite group = new Composite(parent, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
			group.setLayout(new GridLayout(2, false));

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Label);

				labelField = new Text(group, SWT.BORDER);
				labelField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Icon);

				iconField = new Text(group, SWT.BORDER);
				iconField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Type);

				typeViewer = new ComboViewer(group, SWT.READ_ONLY);
				// viewer.setLabelProvider(labelProvider)
				typeViewer.setContentProvider(new ArrayContentProvider());
				typeViewer.setInput(ItemType.values());
			}

			{
				final Label l = new Label(group, SWT.NONE);
				l.setText(Messages.ApplicationEditor_Parent);
				l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

				toolbarViewer = new TableViewer(group);
				toolbarViewer.setLabelProvider(new HiearchyLabelProvider());
				toolbarViewer.setContentProvider(new ArrayContentProvider());

				final List<MToolBar> toolbarList = new ArrayList<MToolBar>();
				final Iterator<EObject> it = EcoreUtil.getAllContents(Collections.singleton(application));
				while (it.hasNext()) {
					final EObject o = it.next();
					if (MenuPackageImpl.Literals.TOOL_BAR.isSuperTypeOf(o.eClass())) {
						toolbarList.add((MToolBar) o);
					}
				}
				toolbarViewer.setInput(toolbarList);
				toolbarViewer.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						setPageComplete(isPageComplete());
					}
				});
				final GridData gd = new GridData(GridData.FILL_BOTH);
				gd.heightHint = toolbarViewer.getTable().getItemHeight() * 5;
				toolbarViewer.getControl().setLayoutData(gd);
				toolbarViewer.setSelection(new StructuredSelection(toolbarList.get(0)));
			}

			setControl(group);
		}

		@Override
		public boolean isPageComplete() {
			if (labelField.getText().trim().length() == 0 && iconField.getText().trim().length() == 0) {
				return true;
			}
			return !toolbarViewer.getSelection().isEmpty();
		}
	}

	class HiearchyLabelProvider extends StyledCellLabelProvider {

		@Override
		public void update(ViewerCell cell) {
			EObject o = (EObject) cell.getElement();

			String label = ""; //$NON-NLS-1$
			Image img = null;
			AbstractComponentEditor elementEditor = getEditor().getEditor(o.eClass());
			if (elementEditor != null) {
				label = elementEditor.getDetailLabel(o);
				label = label == null ? elementEditor.getLabel(o) : label;
				img = elementEditor.getImage(o, composite.getDisplay());
			}

			final List<String> parentPath = new ArrayList<String>();
			while (o.eContainer() != null) {
				o = o.eContainer();
				elementEditor = getEditor().getEditor(o.eClass());
				if (elementEditor != null) {
					parentPath.add(0, elementEditor.getLabel(o));
				}
			}

			String parentString = ""; //$NON-NLS-1$
			for (final String p : parentPath) {
				parentString += "/" + p; //$NON-NLS-1$
			}

			final StyledString s = new StyledString(label);
			s.append(" - " + parentString, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			cell.setStyleRanges(s.getStyleRanges());
			cell.setText(s.getString());
			cell.setImage(img);
		}
	}
}
