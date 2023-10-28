/*******************************************************************************
 * Copyright (c) 2010, 2017 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 *     Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 437951
 *     Olivier Prouvost <olivier@opcoach.com> - Bug 472658, 412567
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ToolItemIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import jakarta.inject.Inject;

public abstract class ToolItemEditor<M extends MToolItem> extends AbstractComponentEditor<M> {
	private Composite composite;
	private EMFDataBindingContext context;

	private StackLayout stackLayout;

	@Inject
	@Optional
	protected IProject project;

	@Inject
	IEclipseContext eclipseContext;

	private Button createRemoveMenu;

	public ToolItemEditor() {
		super();
	}

	@SuppressWarnings("unchecked")
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

		if (createRemoveMenu != null) {
			createRemoveMenu.setSelection(((MToolItem) object).getMenu() != null);
		}

		getMaster().setValue((M) object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);

		return composite;
	}

	protected CTabFolder createForm(Composite parent, EMFDataBindingContext context, WritableValue<M> master,
			boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
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

		createFormSubTypeForm(parent, folder, context, master);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MToolItem.class);

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
		final TreeViewer viewer = objectViewer.createViewer(container, UiPackageImpl.Literals.UI_ELEMENT__WIDGET, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	protected void createFormSubTypeForm(Composite parent, CTabFolder folder, EMFDataBindingContext context,
			final WritableValue<M> master) {
		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);
		final IWidgetValueProperty<Button, Boolean> checkProp = WidgetProperties.buttonSelection();
		final IWidgetValueProperty<Button, Boolean> enabled = WidgetProperties.enabled();

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ToolItemEditor_Type);
			l.setLayoutData(new GridData());

			final ComboViewer viewer = new ComboViewer(parent);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if ((ItemType) element == ItemType.CHECK) {
						return Messages.ItemType_Check;
					} else if ((ItemType) element == ItemType.PUSH) {
						return Messages.ItemType_Push;
					}
					return Messages.ItemType_Radio;
				}
			});
			viewer.setInput(new ItemType[] { ItemType.CHECK, ItemType.PUSH, ItemType.RADIO });
			final GridData gd = new GridData();
			gd.horizontalSpan = 2;
			viewer.getControl().setLayoutData(gd);
			final IObservableValue<ItemType> itemTypeObs = E4Properties.type(getEditingDomain()).observeDetail(master);
			context.bindValue(ViewerProperties.singleSelection().observe(viewer), itemTypeObs);
		}

		ControlFactory.createTextField(parent, Messages.ToolItemEditor_Label, master, context, textProp,
				E4Properties.label(getEditingDomain()));
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(),
				context, textProp, E4Properties.accessibilityPhrase(getEditingDomain()));
		ControlFactory.createTextField(parent, Messages.ToolItemEditor_ToolTip, master, context, textProp,
				E4Properties.tooltip(getEditingDomain()));

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ToolItemEditor_IconURI);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observe(t), E4Properties.iconUri(getEditingDomain()).observeDetail(master));

			new ImageTooltip(t, Messages, this);

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ToolItemIconDialogEditor dialog = new ToolItemIconDialogEditor(b.getShell(), eclipseContext,
							project, getEditingDomain(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ToolItemEditor_Menu);
			l.setLayoutData(new GridData());

			createRemoveMenu = new Button(parent, SWT.CHECK);
			createRemoveMenu.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (getMaster().getValue().getMenu() == null) {
						addMenu();
					} else {
						removeMenu();
					}
				}
			});
			createRemoveMenu.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ToolItemEditor_Enabled);
			l.setLayoutData(new GridData());

			final Button b = new Button(parent, SWT.CHECK);
			b.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
			context.bindValue(checkProp.observe(b),
					E4Properties.enabled(getEditingDomain()).observeDetail(getMaster()));
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ToolItemEditor_Selected);
			l.setLayoutData(new GridData());

			final Button b = new Button(parent, SWT.CHECK);
			b.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
			context.bindValue(checkProp.observe(b),
					E4Properties.selected(getEditingDomain()).observeDetail(getMaster()));

			final UpdateValueStrategy<Boolean, ItemType> t2m = new UpdateValueStrategy<>();
			t2m.setConverter(new Converter<Boolean, ItemType>(boolean.class, ItemType.class) {
				@Override
				public ItemType convert(Boolean fromObject) {
					return null;
				}
			});
			final UpdateValueStrategy<ItemType, Boolean> m2t = new UpdateValueStrategy<>();
			m2t.setConverter(new Converter<ItemType, Boolean>(ItemType.class, boolean.class) {

				@Override
				public Boolean convert(ItemType fromObject) {
					return fromObject == ItemType.CHECK || fromObject == ItemType.RADIO;
				}
			});

			context.bindValue(enabled.observe(b), E4Properties.type(getEditingDomain()).observeDetail(getMaster()),
					t2m, m2t);

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
			list.add(Messages.ToolItemEditor_NoExpression);
			list.add(UiPackageImpl.Literals.CORE_EXPRESSION);
			list.add(UiPackageImpl.Literals.IMPERATIVE_EXPRESSION);
			list.addAll(getEditor().getFeatureClasses(UiPackageImpl.Literals.EXPRESSION, UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN));
			combo.setInput(list);
			context.bindValue(ViewerProperties.singleSelection().observe(combo),
					E4Properties.visibleWhen(getEditingDomain()).observeDetail(getMaster()),
					UpdateValueStrategy.create(new EClass2EObject<>(Messages)),
					UpdateValueStrategy.create(new EObject2EClass<>(Messages)));
		}
		// ------------------------------------------------------------

		createSubTypeFormElements(parent, context, master);

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.toBeRendered(getEditingDomain()));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.visible(getEditingDomain()));

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_ApplicationElement_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);
	}

	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, WritableValue<M> master) {

	}

	void removeMenu() {
		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(), MenuPackageImpl.Literals.TOOL_ITEM__MENU, null);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	void addMenu() {
		final MMenu menu = MMenuFactory.INSTANCE.createMenu();
		setElementId(menu);

		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(), MenuPackageImpl.Literals.TOOL_ITEM__MENU, menu);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	@Override
	public IObservableList<Object> getChildList(final Object element) {
		final WritableList<Object> list = new WritableList<>();
		final MToolItem item = (MToolItem) element;

		if (item.getMenu() != null) {
			list.add(0, item.getMenu());
		}

		E4Properties.menu().observe(item).addValueChangeListener(event -> {
			if (event.diff.getOldValue() != null) {
				list.remove(event.diff.getOldValue());
				if (getMaster().getValue() == element && !createRemoveMenu.isDisposed()) {
					createRemoveMenu.setSelection(false);
				}
			}

			if (event.diff.getNewValue() != null) {
				list.add(0, event.diff.getNewValue());
				if (getMaster().getValue() == element && !createRemoveMenu.isDisposed()) {
					createRemoveMenu.setSelection(true);
				}
			}
		});

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		final MToolItem item = (MToolItem) element;
		final String l = getLocalizedLabel(item);

		if (l != null && l.trim().length() > 0) {
			return l;
		} else if (item.getTooltip() != null && item.getTooltip().trim().length() > 0) {
			return item.getTooltip();
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__ICON_URI),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	static class EObject2EClass<T> extends Converter<T, Object> {
		private final Messages Messages;

		public EObject2EClass(Messages Messages) {
			super(EObject.class, EClass.class);
			this.Messages = Messages;
		}

		@Override
		public Object convert(T fromObject) {
			if (fromObject == null) {
				return Messages.MenuItemEditor_NoExpression;
			}
			return ((EObject) fromObject).eClass();
		}
	}

	static class EClass2EObject<T> extends Converter<Object, T> {
		private final Messages Messages;

		public EClass2EObject(Messages Messages) {
			super(EClass.class, EObject.class);
			this.Messages = Messages;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T convert(Object fromObject) {
			if (fromObject == null || fromObject.toString().equals(Messages.MenuItemEditor_NoExpression)) {
				return null;
			}
			return (T) EcoreUtil.create((EClass) fromObject);
		}
	}
}
