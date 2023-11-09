/*******************************************************************************
 * f * Copyright (c) 2010, 2017 BestSolution.at and others.
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
 * Marco Descher <marco@descher.at> - Bug 395982
 * Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 * Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 437951
 * Olivier Prouvost <olivier@opcoach.com> - Bug 472658
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList.Struct;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuItemEditor.EClass2EObject;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuItemEditor.EObject2EClass;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.MenuIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.MApplicationElement;
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
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class MenuEditor extends AbstractComponentEditor<MMenu> {
	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private final List<Action> actions = new ArrayList<>();

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
		actions.add(new Action(Messages.MenuEditor_AddHandledMenuItem,
				createImageDescriptor(ResourceProvider.IMG_HandledMenuItem)) {
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
		actions.add(new Action(Messages.MenuEditor_AddDirectMenuItem,
				createImageDescriptor(ResourceProvider.IMG_DirectMenuItem)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, false);
			}
		});
		actions.add(new Action(Messages.MenuEditor_AddSeparator,
				createImageDescriptor(ResourceProvider.IMG_MenuSeparator)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.MENU_SEPARATOR, true);
			}
		});
		actions.add(new Action(Messages.MenuEditor_AddDynamicMenuContribution,
				createImageDescriptor(ResourceProvider.IMG_DynamicMenuContribution)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION, false);
			}
		});
		addExpression = new Action(Messages.MenuEditor_AddCoreExpression,
				createImageDescriptor(ResourceProvider.IMG_CoreExpression)) {
			@Override
			public void run() {
				final MUIElement e = getMaster().getValue();
				final Command cmd = SetCommand.create(getEditingDomain(), e,
						UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN, MUiFactory.INSTANCE.createCoreExpression());
				if (cmd.canExecute()) {
					getEditingDomain().getCommandStack().execute(cmd);
				}
			}
		};
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_Menu);
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
		final EObject o = (EObject) object;
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
			composite.requestLayout();
		}

		getMaster().setValue((MMenu) object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);
		return composite;
	}

	protected Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue<MMenu> master,
			boolean rootMenu, boolean isImport) {
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

		// ------------------------------------------------------------
		if (!rootMenu) {
			ControlFactory.createTranslatedTextField(parent, Messages.MenuEditor_LabelLabel, master, context, textProp,
					E4Properties.label(getEditingDomain()), resourcePool, project);
		}

		ControlFactory.createTextField(parent, Messages.MenuEditor_Mnemonics, master, context, textProp,
				E4Properties.mnemonics(getEditingDomain()));

		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, null, this,
					UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
			pickList.setText(Messages.MenuEditor_Children);
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

			final Struct defaultStruct = new Struct(Messages.MenuEditor_HandledMenuItem,
					MenuPackageImpl.Literals.HANDLED_MENU_ITEM, false);
			pickList.setInput(new Struct[] {
					new Struct(Messages.MenuEditor_Separator, MenuPackageImpl.Literals.MENU_SEPARATOR, true),
					new Struct(Messages.MenuEditor_Menu, MenuPackageImpl.Literals.MENU, false),
					defaultStruct,
					new Struct(Messages.MenuEditor_DirectMenuItem, MenuPackageImpl.Literals.DIRECT_MENU_ITEM, false),
					new Struct(Messages.MenuEditor_DynamicMenuContribution,
							MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION, false) });
			pickList.setSelection(new StructuredSelection(defaultStruct));

			pickList.getList().setInput(E4Properties.<MMenuElement>children(getEditingDomain()).observeDetail(master));

		}

		// ------------------------------------------------------------
		if (!rootMenu) {
			ControlFactory.createTranslatedTextField(parent, Messages.MenuEditor_Tooltip, master, context, textProp,
					E4Properties.tooltip(getEditingDomain()), resourcePool, project);
		}

		// ------------------------------------------------------------
		if (!rootMenu) {
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuEditor_IconURI);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(
					textProp.observeDelayed(200, t),
					E4Properties.iconUri(getEditingDomain()).observeDetail(master));

			new ImageTooltip(t, Messages, this);

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final MenuIconDialogEditor dialog = new MenuIconDialogEditor(b.getShell(), eclipseContext, project,
							getEditingDomain(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ModelTooling_UIElement_VisibleWhen);
			l.setLayoutData(new GridData());

			final ComboViewer combo = new ComboViewer(parent);
			combo.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			combo.setContentProvider(ArrayContentProvider.getInstance());
			combo.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof EClass) {
						final EClass eClass = (EClass) element;
						return eClass.getName();
					}

					return super.getText(element);
				}
			});
			final List<Object> list = new ArrayList<>();
			list.add(Messages.MenuItemEditor_NoExpression);
			list.add(UiPackageImpl.Literals.CORE_EXPRESSION);
			list.add(UiPackageImpl.Literals.IMPERATIVE_EXPRESSION);
			list.addAll(getEditor().getFeatureClasses(UiPackageImpl.Literals.EXPRESSION,
					UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN));
			combo.setInput(list);
			context.bindValue(ViewerProperties.singleSelection().observe(combo),
					E4Properties.visibleWhen(getEditingDomain()).observeDetail(getMaster()),
					UpdateValueStrategy.create(new EClass2EObject<>(Messages)),
					UpdateValueStrategy.create(new EObject2EClass<>(Messages)));
		}

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.toBeRendered(getEditingDomain()));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.visible(getEditingDomain()));

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(),
				context, textProp, E4Properties.accessibilityPhrase(getEditingDomain()));
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MMenu.class);

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
	public IObservableList<?> getChildList(Object element) {
		final WritableList<MApplicationElement> list = new WritableList<>();
		MMenu menu = (MMenu) element;

		if (menu.getVisibleWhen() != null) {
			list.add(0, menu.getVisibleWhen());
		}

		E4Properties.visibleWhen().observe(menu).addValueChangeListener(event -> {
			if (event.diff.getOldValue() != null) {
				list.remove(event.diff.getOldValue());
			}

			if (event.diff.getNewValue() != null) {
				list.add(0, event.diff.getNewValue());
			}
		});

		final IObservableList<MMenuElement> l = E4Properties.<MMenuElement>children().observe(menu);
		l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<MMenuElement>() {

			@Override
			public void handleRemove(int index, MMenuElement element) {
				list.remove(element);
			}

			@Override
			public void handleMove(int oldIndex, int newIndex, MMenuElement element) {
				if (list.get(0) instanceof MExpression) {
					oldIndex += 1;
					newIndex += 1;
				}
				list.move(oldIndex, newIndex);
			}

			@Override
			public void handleAdd(int index, MMenuElement element) {
				list.add(element);
			}
		}));
		list.addAll(l);

		return list;
	}

	@Override
	public String getLabel(Object element) {
		final MMenu menu = (MMenu) element;
		if (menu.getParent() == null) {
			final EObject o = (EObject) element;
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
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__ICON_URI),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	protected void handleAdd(EClass eClass, boolean separator) {
		final MMenuElement eObject = (MMenuElement) EcoreUtil.create(eClass);
		setElementId(eObject);
		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			if (!separator) {
				getEditor().setSelection(eObject);
			}
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		if (((MUIElement) getMaster().getValue()).getVisibleWhen() == null) {
			l.add(addExpression);
		}
		return l;
	}
}