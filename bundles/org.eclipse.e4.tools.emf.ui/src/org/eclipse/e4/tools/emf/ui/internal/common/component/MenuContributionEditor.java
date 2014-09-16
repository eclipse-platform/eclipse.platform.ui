/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Marco Descher <marco@descher.at> - Bug 397650, Bug 395982, Bug 396975
 *     Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 *     Nicolaj Hoess <nicohoess@gmail.com> - Bug 396975
 *     Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList.Struct;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.MenuIdDialog;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ComboViewer;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MenuContributionEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	private StackLayout stackLayout;
	private List<Action> actions = new ArrayList<Action>();

	@Inject
	private IModelResource resource;

	@Inject
	private EModelService modelService;

	@Inject
	public MenuContributionEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.MenuEditor_AddHandledMenuItem, createImageDescriptor(ResourceProvider.IMG_HandledMenuItem)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.HANDLED_MENU_ITEM, false);
			}
		});
		actions.add(new Action(Messages.MenuEditor_AddMenu, createImageDescriptor(ResourceProvider.IMG_Menu)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.MENU, false);
			}
		});
		actions.add(new Action(Messages.MenuEditor_AddDirectMenuItem, createImageDescriptor(ResourceProvider.IMG_DirectMenuItem)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, false);
			}
		});
		actions.add(new Action(Messages.MenuEditor_AddSeparator, createImageDescriptor(ResourceProvider.IMG_MenuSeparator)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.MENU_SEPARATOR, true);
			}
		});
		actions.add(new Action(Messages.MenuEditor_AddDynamicMenuContribution, createImageDescriptor(ResourceProvider.IMG_DynamicMenuContribution)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION, false);
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			if (((MUIElement) element).isToBeRendered()) {
				return createImage(ResourceProvider.IMG_MenuContribution);
			} else {
				return createImage(ResourceProvider.IMG_Tbr_MenuContribution);
			}
		}

		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.MenuContributionEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.MenuContributionEditor_TreeLabelDescription;
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
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(), context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE));

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuContributionEditor_ParentId);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.MENU_CONTRIBUTION__PARENT_ID).observeDetail(getMaster()));

			Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MenuIdDialog dialog = new MenuIdDialog(t.getShell(), resource, (MMenuContribution) getMaster().getValue(), getEditingDomain(), modelService, Messages);
					dialog.open();
				}
			});

		}

		ControlFactory.createTextField(parent, Messages.MenuContributionEditor_Position, master, context, textProp, EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.MENU_CONTRIBUTION__POSITION_IN_PARENT));

		// ------------------------------------------------------------
		{
			E4PickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
				@Override
				protected void addPressed() {
					Struct struct = (Struct) ((IStructuredSelection) getPicker().getSelection()).getFirstElement();
					EClass eClass = struct.eClass;
					MMenuElement eObject = (MMenuElement) EcoreUtil.create(eClass);

					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						if (!struct.separator) {
							getEditor().setSelection(eObject);
						}
					}
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(Messages.MenuContributionEditor_MenuItems);

			final TableViewer viewer = pickList.getList();
			IEMFListProperty prop = EMFEditProperties.list(getEditingDomain(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
			viewer.setInput(prop.observeDetail(master));

			final ComboViewer childrenDropDown = pickList.getPicker();
			Struct defaultStruct = new Struct(Messages.MenuContributionEditor_HandledMenuItem, MenuPackageImpl.Literals.HANDLED_MENU_ITEM, false);
			childrenDropDown.setInput(new Struct[] { new Struct(Messages.MenuContributionEditor_Separator, MenuPackageImpl.Literals.MENU_SEPARATOR, true), new Struct(Messages.MenuContributionEditor_Menu, MenuPackageImpl.Literals.MENU, false), defaultStruct, new Struct(Messages.MenuContributionEditor_DirectMenuItem, MenuPackageImpl.Literals.DIRECT_MENU_ITEM, false), new Struct(Messages.MenuContributionEditor_DynamicMenuContribution, MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION, false) });
			childrenDropDown.setSelection(new StructuredSelection(defaultStruct));

		}

		ControlFactory.createCheckBox(parent, "To Be Rendered", getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED)); //$NON-NLS-1$
		ControlFactory.createCheckBox(parent, "Visible", getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE)); //$NON-NLS-1$

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MMenuContribution.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return ELEMENT_CONTAINER__CHILDREN.observe(element);
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	protected void handleAdd(EClass eClass, boolean separator) {
		MMenuElement eObject = (MMenuElement) EcoreUtil.create(eClass);
		setElementId(eObject);
		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			if (!separator) {
				getEditor().setSelection(eObject);
			}
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
