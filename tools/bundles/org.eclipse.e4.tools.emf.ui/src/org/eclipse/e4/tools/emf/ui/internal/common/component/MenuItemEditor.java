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
 * Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 437951
 * Olivier Prouvost <olivier@opcoach.com> - Bug 472658, 412567
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.MenuItemIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.MUiFactory;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.action.Action;
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

public abstract class MenuItemEditor<M extends MMenuItem> extends AbstractComponentEditor<M> {

	private Composite composite;
	private EMFDataBindingContext context;

	private StackLayout stackLayout;

	@Inject
	@Optional
	protected IProject project;

	@Inject
	IEclipseContext eclipseContext;

	private Action addExpression;

	public MenuItemEditor() {
		super();
	}

	@PostConstruct
	void init() {
		addExpression = new Action(Messages.MenuItemEditor_AddCoreExpression,
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
		return getImage(element, ResourceProvider.IMG_MenuItem);
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

		getMaster().setValue((M) object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);

		return composite;
	}

	protected CTabFolder createForm(Composite parent, EMFDataBindingContext context, WritableValue<M> master,
			boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);
		final IWidgetValueProperty<Button, Boolean> checkProp = WidgetProperties.buttonSelection();
		final IWidgetValueProperty<Button, Boolean> enabled = WidgetProperties.enabled();

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp,
				E4Properties.elementId(getEditingDomain()));

		if (this.getClass() != MenuItemEditor.class) {
			// ------------------------------------------------------------
			{
				final Label l = new Label(parent, SWT.NONE);
				l.setText(Messages.MenuItemEditor_Type);
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
				context.bindValue(ViewerProperties.singleSelection().observe(viewer),
						E4Properties.type(getEditingDomain()).observeDetail(master));
			}
		}

		ControlFactory.createTranslatedTextField(parent, Messages.MenuItemEditor_Label, master, context, textProp,
				E4Properties.label(getEditingDomain()), resourcePool, project);
		ControlFactory.createTextField(parent, Messages.MenuItemEditor_Mnemonics, getMaster(), context, textProp,
				E4Properties.mnemonics(getEditingDomain()));
		ControlFactory.createTranslatedTextField(parent, Messages.MenuItemEditor_Tooltip, master, context, textProp,
				E4Properties.tooltip(getEditingDomain()), resourcePool, project);

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_IconURI);
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
					final MenuItemIconDialogEditor dialog = new MenuItemIconDialogEditor(b.getShell(), eclipseContext,
							project, getEditingDomain(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_Enabled);
			l.setLayoutData(new GridData());

			final Button b = new Button(parent, SWT.CHECK);
			b.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
			context.bindValue(checkProp.observe(b),
					E4Properties.enabled(getEditingDomain()).observeDetail(getMaster()));
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_Selected);
			l.setLayoutData(new GridData());

			final Button b = new Button(parent, SWT.CHECK);
			b.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
			context.bindValue(
					checkProp.observe(b),
					E4Properties.selected(getEditingDomain()).observeDetail(getMaster()));

			final UpdateValueStrategy<Boolean, ItemType> t2m = new UpdateValueStrategy<>();
			t2m.setConverter(new Converter<>(boolean.class, ItemType.class) {

				@Override
				public ItemType convert(Boolean fromObject) {
					return null;
				}
			});
			final UpdateValueStrategy<ItemType, Boolean> m2t = new UpdateValueStrategy<>();
			m2t.setConverter(new Converter<>(ItemType.class, boolean.class) {

				@Override
				public Boolean convert(ItemType fromObject) {
					return fromObject == ItemType.CHECK || fromObject == ItemType.RADIO;
				}
			});

			context.bindValue(
					enabled.observe(b),
					E4Properties.type(getEditingDomain()).observeDetail(getMaster()), t2m, m2t);

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

		createFormSubTypeForm(parent, context, master);

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

		createContributedEditorTabs(folder, context, getMaster(), MMenuItem.class);

		folder.setSelection(0);

		return folder;
	}

	protected abstract void createFormSubTypeForm(Composite parent, EMFDataBindingContext context,
			WritableValue<M> master);

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
		return null;
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

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		if (((MUIElement) getMaster().getValue()).getVisibleWhen() == null) {
			l.add(addExpression);
		}
		return l;
	}

	static class EObject2EClass<T> extends Converter<T, Object> {
		Messages Messages;

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
		Messages Messages;

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