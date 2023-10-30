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
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.EClassLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MGenericTrimContainer;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class TrimBarEditor extends AbstractComponentEditor<MTrimBar> {

	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private final List<Action> actions = new ArrayList<>();

	@Inject
	@Optional
	protected IProject project;

	@Inject
	private ModelEditor editor;

	@Inject
	@Translation
	protected Messages Messages;

	@Inject
	public TrimBarEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.TrimBarEditor_AddToolBar, createImageDescriptor(ResourceProvider.IMG_ToolBar)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.TOOL_BAR);
			}
		});
		actions.add(new Action(Messages.TrimBarEditor_AddToolControl,
				createImageDescriptor(ResourceProvider.IMG_ToolControl)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.TOOL_CONTROL);
			}
		});
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_WindowTrim);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.TrimBarEditor_TreeLabel;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.TrimBarEditor_TreeLabelDescription;
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

		getMaster().setValue((MTrimBar) object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue<MTrimBar> master,
			boolean isImport) {
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
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(),
				context, textProp, E4Properties.accessibilityPhrase(getEditingDomain()));

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.TrimBarEditor_Side);
			l.setLayoutData(new GridData());

			final ComboViewer viewer = new ComboViewer(parent);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if ((SideValue) element == SideValue.BOTTOM) {
						return Messages.SideValue_Bottom;
					} else if ((SideValue) element == SideValue.LEFT) {
						return Messages.SideValue_Left;
					} else if ((SideValue) element == SideValue.RIGHT) {
						return Messages.SideValue_Right;
					}
					return Messages.SideValue_Top;
				}
			});
			viewer.setInput(SideValue.values());
			final GridData gd = new GridData();
			gd.horizontalSpan = 2;
			viewer.getControl().setLayoutData(gd);
			context.bindValue(ViewerProperties.singleSelection(SideValue.class).observe(viewer),
					E4Properties.<MTrimElement>side(getEditingDomain()).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_PICKER),
					this, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
				@Override
				protected void addPressed() {
					final EClass eClass = (EClass) getSelection().getFirstElement();
					handleAddChild(eClass);
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(Messages.TrimBarEditor_Controls);

			final TableViewer viewer = pickList.getList();

			pickList.setLabelProvider(new EClassLabelProvider(getEditor()));
			pickList
			.setInput(new Object[] { MenuPackageImpl.Literals.TOOL_BAR, MenuPackageImpl.Literals.TOOL_CONTROL });
			pickList.setSelection(new StructuredSelection(MenuPackageImpl.Literals.TOOL_BAR));

			viewer.setInput(E4Properties.<MTrimElement>children().observeDetail(getMaster()));
		}

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.toBeRendered(getEditingDomain()));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.visible(getEditingDomain()));

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MTrimBar.class);

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
		return E4Properties.<MTrimElement>children().observe((MTrimBar) element);
	}

	@Override
	public String getDetailLabel(Object element) {
		final MGenericTrimContainer<?> trim = (MGenericTrimContainer<?>) element;

		if (trim.getSide() != null) {
			return trim.getSide().toString();
		}

		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.GENERIC_TRIM_CONTAINER__SIDE),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	protected void handleAddChild(EClass eClass) {
		final EObject toolbar = EcoreUtil.create(eClass);
		setElementId(toolbar);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, toolbar);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			editor.setSelection(toolbar);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
