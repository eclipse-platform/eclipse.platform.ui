/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Marco Descher <marco@descher.at> - https://bugs.eclipse.org/397650
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
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
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
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

public class ToolBarContributionEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	private EStackLayout stackLayout;
	private List<Action> actions = new ArrayList<Action>();

	@Inject
	@Optional
	private IProject project;

	private static class Struct {
		private final String label;
		private final EClass eClass;
		private final boolean separator;

		public Struct(String label, EClass eClass, boolean separator) {
			this.label = label;
			this.eClass = eClass;
			this.separator = separator;
		}
	}

	@Inject
	public ToolBarContributionEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.ToolBarEditor_AddHandledToolItem, createImageDescriptor(ResourceProvider.IMG_HandledToolItem)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, false);
			}
		});
		actions.add(new Action(Messages.ToolBarEditor_AddDirectToolItem, createImageDescriptor(ResourceProvider.IMG_DirectToolItem)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, false);
			}
		});
		actions.add(new Action(Messages.ToolBarEditor_AddToolControl, createImageDescriptor(ResourceProvider.IMG_ToolControl)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.TOOL_CONTROL, false);
			}
		});
		actions.add(new Action(Messages.ToolBarEditor_AddToolBarSeparator, createImageDescriptor(ResourceProvider.IMG_ToolBarSeparator)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, true);
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				return createImage(ResourceProvider.IMG_ToolBarContribution);
			} else {
				return createImage(ResourceProvider.IMG_Tbr_ToolBarContribution);
			}
		}

		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.ToolBarContributionEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.ToolBarContributionEditor_TreeLabelDescription;
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
		ControlFactory.createTextField(parent, Messages.MenuContributionEditor_ParentId, master, context, textProp, EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION__PARENT_ID));
		ControlFactory.createTextField(parent, Messages.MenuContributionEditor_Position, master, context, textProp, EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION__POSITION_IN_PARENT));

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ToolBarEditor_ToolbarItems);
			l.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));

			final TableViewer viewer = new TableViewer(parent);
			ObservableListContentProvider cp = new ObservableListContentProvider();
			viewer.setContentProvider(cp);

			GridData gd = new GridData(GridData.FILL_BOTH);
			viewer.getControl().setLayoutData(gd);
			viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));

			IEMFListProperty prop = EMFEditProperties.list(getEditingDomain(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
			viewer.setInput(prop.observeDetail(master));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout(2, false);
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Up);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_up));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MElementContainer<?> container = (MElementContainer<?>) getMaster().getValue();
							int idx = container.getChildren().indexOf(obj) - 1;
							if (idx >= 0) {
								if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(), idx)) {
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
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MElementContainer<?> container = (MElementContainer<?>) getMaster().getValue();
							int idx = container.getChildren().indexOf(obj) + 1;
							if (idx < container.getChildren().size()) {
								if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(), idx)) {
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			final ComboViewer childrenDropDown = new ComboViewer(buttonComp);
			childrenDropDown.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			childrenDropDown.setContentProvider(new ArrayContentProvider());
			childrenDropDown.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					Struct struct = (Struct) element;
					return struct.label;
				}
			});

			Struct defaultStruct = new Struct(Messages.ToolBarEditor_HandledToolItem, MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, false);
			childrenDropDown.setInput(new Struct[] { defaultStruct, new Struct(Messages.ToolBarEditor_DirectToolItem, MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, false), new Struct(Messages.ToolBarEditor_ToolControl, MenuPackageImpl.Literals.TOOL_CONTROL, false), new Struct(Messages.ToolBarEditor_Separator, MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, true) });
			childrenDropDown.setSelection(new StructuredSelection(defaultStruct));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!childrenDropDown.getSelection().isEmpty()) {
						Struct struct = (Struct) ((IStructuredSelection) childrenDropDown.getSelection()).getFirstElement();
						EClass eClass = struct.eClass;
						MToolBarElement eObject = (MToolBarElement) EcoreUtil.create(eClass);
						setElementId(eObject);

						Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
							if (!struct.separator) {
								getEditor().setSelection(eObject);
							}
						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Remove);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_table_delete));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						List<?> keybinding = ((IStructuredSelection) viewer.getSelection()).toList();
						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, keybinding);
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MToolBarContribution.class);

		folder.setSelection(0);

		return folder;
	}

	private void createUITreeInspection(CTabFolder folder) {
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_RuntimeWidgetTree);
		Composite container = new Composite(folder, SWT.NONE);
		container.setLayout(new GridLayout());
		item.setControl(container);

		UIViewer objectViewer = new UIViewer();
		TreeViewer viewer = objectViewer.createViewer(container, UiPackageImpl.Literals.UI_ELEMENT__WIDGET, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
	public IObservableList getChildList(Object element) {
		return ELEMENT_CONTAINER__CHILDREN.observe(element);
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	protected void handleAddChild(EClass eClass, boolean separator) {
		MToolBarElement eObject = (MToolBarElement) EcoreUtil.create(eClass);
		setElementId(eObject);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			if (!separator) {
				getEditor().setSelection(eObject);
			}
		}
	}
}
