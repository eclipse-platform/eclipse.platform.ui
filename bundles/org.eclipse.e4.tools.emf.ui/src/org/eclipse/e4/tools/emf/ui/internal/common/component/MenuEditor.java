/*******************************************************************************
f * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Marco Descher <marco@descher.at> - Bug 395982
 *     Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 *     Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 437951
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList.Struct;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuItemEditor.EClass2EObject;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuItemEditor.EObject2EClass;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.MenuIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.MUiFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.databinding.IEMFValueProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
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

public class MenuEditor extends AbstractComponentEditor {

	private Composite composite;
	private EMFDataBindingContext context;

	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	private IEMFValueProperty UI_ELEMENT__VISIBLE_WHEN = EMFProperties.value(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);
	private StackLayout stackLayout;
	private List<Action> actions = new ArrayList<Action>();

	@Inject
	@Optional
	private IProject project;

	@Inject
	IEclipseContext eclipseContext;

	private Action addExpression;

	@Inject
	public MenuEditor() {
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
		addExpression = new Action(Messages.MenuEditor_AddCoreExpression, createImageDescriptor(ResourceProvider.IMG_CoreExpression)) {
			@Override
			public void run() {
				MUIElement e = (MUIElement) getMaster().getValue();
				Command cmd = SetCommand.create(getEditingDomain(), e, UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN, MUiFactory.INSTANCE.createCoreExpression());
				if (cmd.canExecute()) {
					getEditingDomain().getCommandStack().execute(cmd);
				}
			}
		};
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				return createImage(ResourceProvider.IMG_Menu);
			} else {
				return createImage(ResourceProvider.IMG_Tbr_Menu);
			}
		}

		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.MenuEditor_Description;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = new Composite(parent, SWT.NONE);
			stackLayout = new StackLayout();
			composite.setLayout(stackLayout);

			createForm(composite, context, getMaster(), false, false);
			createForm(composite, context, getMaster(), true, false);
			if (getEditor().isModelFragment()) {
				createForm(composite, context, getMaster(), false, true);
			}
		}
		EObject o = (EObject) object;
		Control topControl;
		if (Util.isImport(o) && getEditor().isModelFragment()) {
			topControl = composite.getChildren()[2];
		} else if (o.eContainer() instanceof MWindow || o.eContainer() == null) {
			topControl = composite.getChildren()[1];
		} else {
			topControl = composite.getChildren()[0];
		}

		if (stackLayout.topControl != topControl) {
			stackLayout.topControl = topControl;
			composite.layout(true, true);
		}

		getMaster().setValue(object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);
		return composite;
	}

	protected Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master, boolean rootMenu, boolean isImport) {
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
		if (!rootMenu) {
			ControlFactory.createTranslatedTextField(parent, Messages.MenuEditor_LabelLabel, master, context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL), resourcePool, project);
		}

		ControlFactory.createTextField(parent, Messages.MenuEditor_Mnemonics, master, context, textProp, EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.MENU_ELEMENT__MNEMONICS));

		{

			// Label l = new Label(parent, SWT.NONE);
			// l.setText(Messages.MenuEditor_Children);
			// l.setLayoutData(new GridData(GridData.END, GridData.BEGINNING,
			// false, false));
			//
			// new Label(parent, SWT.NONE);
			// new Label(parent, SWT.NONE);

			E4PickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
			pickList.setText(Messages.MenuEditor_Children);
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

			Struct defaultStruct = new Struct(Messages.MenuEditor_HandledMenuItem, MenuPackageImpl.Literals.HANDLED_MENU_ITEM, false);
			pickList.getPicker().setInput(new Struct[] { new Struct(Messages.MenuEditor_Separator, MenuPackageImpl.Literals.MENU_SEPARATOR, true), new Struct(Messages.MenuEditor_Menu, MenuPackageImpl.Literals.MENU, false), defaultStruct, new Struct(Messages.MenuEditor_DirectMenuItem, MenuPackageImpl.Literals.DIRECT_MENU_ITEM, false), new Struct(Messages.MenuEditor_DynamicMenuContribution, MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION, false) });
			pickList.getPicker().setSelection(new StructuredSelection(defaultStruct));

			IEMFListProperty prop = EMFEditProperties.list(getEditingDomain(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
			pickList.getList().setInput(prop.observeDetail(master));

		}

		// ------------------------------------------------------------
		if (!rootMenu) {
			ControlFactory.createTranslatedTextField(parent, Messages.MenuEditor_Tooltip, master, context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__TOOLTIP), resourcePool, project);
		}

		// ------------------------------------------------------------
		if (!rootMenu) {
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuEditor_IconURI);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__ICON_URI).observeDetail(master));

			new ImageTooltip(t, Messages) {

				@Override
				protected URI getImageURI() {
					MUILabel part = (MUILabel) getMaster().getValue();
					String uri = part.getIconURI();
					if (uri == null || uri.trim().length() == 0) {
						return null;
					}
					return URI.createURI(part.getIconURI());
				}
			};

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MenuIconDialogEditor dialog = new MenuIconDialogEditor(b.getShell(), eclipseContext, project, getEditingDomain(), (MMenu) getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ModelTooling_UIElement_VisibleWhen);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			ComboViewer combo = new ComboViewer(parent);
			combo.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			combo.setContentProvider(new ArrayContentProvider());
			combo.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof EClass) {
						EClass eClass = (EClass) element;
						return eClass.getName();
					}

					return super.getText(element);
				}
			});
			List<Object> list = new ArrayList<Object>();
			list.add(Messages.MenuItemEditor_NoExpression);
			list.add(UiPackageImpl.Literals.CORE_EXPRESSION);
			list.addAll(getEditor().getFeatureClasses(UiPackageImpl.Literals.EXPRESSION, UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN));
			combo.setInput(list);
			context.bindValue(ViewerProperties.singleSelection().observe(combo), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN).observeDetail(getMaster()), new UpdateValueStrategy().setConverter(new EClass2EObject(Messages)), new UpdateValueStrategy().setConverter(new EObject2EClass(Messages)));
		}

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(), context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE));
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MMenu.class);

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
		final WritableList list = new WritableList();

		if (((MUIElement) element).getVisibleWhen() != null) {
			list.add(0, ((MUIElement) element).getVisibleWhen());
		}

		UI_ELEMENT__VISIBLE_WHEN.observe(element).addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (event.diff.getOldValue() != null) {
					list.remove(event.diff.getOldValue());
				}

				if (event.diff.getNewValue() != null) {
					list.add(0, event.diff.getNewValue());
				}
			}
		});

		IObservableList l = ELEMENT_CONTAINER__CHILDREN.observe(element);
		l.addListChangeListener(new IListChangeListener() {

			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {

					@Override
					public void handleRemove(int index, Object element) {
						list.remove(element);
					}

					@Override
					public void handleMove(int oldIndex, int newIndex, Object element) {
						if (list.get(0) instanceof MExpression) {
							oldIndex += 1;
							newIndex += 1;
						}
						list.move(oldIndex, newIndex);
					}

					@Override
					public void handleAdd(int index, Object element) {
						list.add(element);
					}
				});
			}
		});
		list.addAll(l);

		return list;
	}

	@Override
	public String getLabel(Object element) {
		MMenu menu = (MMenu) element;
		if (menu.getParent() == null) {
			EObject o = (EObject) element;
			if (o.eContainer() instanceof MWindow) {
				return Messages.MenuEditor_MainMenu;
			} else if (menu.getTags().contains(VMenuEditor.VIEW_MENU_TAG)) {
				return Messages.MenuEditor_Label_ViewMenu;
			}
		} else if (menu.getTags().contains(VMenuEditor.VIEW_MENU_TAG)) {
			return Messages.MenuEditor_Label_ViewMenu;
		}
		return Messages.MenuEditor_Label;
	}

	@Override
	public String getDetailLabel(Object element) {
		return getLocalizedLabel((MUILabel) element);
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
		if (((MUIElement) getMaster().getValue()).getVisibleWhen() == null) {
			l.add(addExpression);
		}
		return l;
	}
}