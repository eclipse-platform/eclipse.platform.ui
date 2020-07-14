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
 * Marco Descher <marco@descher.at> - Bug 397650
 * Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 * Nicolaj Hoess <nicohoess@gmail.com> - Bug 396975
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList.Struct;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ToolBarIdDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarContributionImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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

public class ToolBarContributionEditor extends AbstractComponentEditor<MToolBarContribution> {
	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private final List<Action> actions = new ArrayList<>();

	@Inject
	@Optional
	private IProject project;

	@Inject
	private IModelResource resource;

	@Inject
	private EModelService modelService;

	@Inject
	public ToolBarContributionEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.ToolBarEditor_AddHandledToolItem,
				createImageDescriptor(ResourceProvider.IMG_HandledToolItem)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, false);
			}
		});
		actions.add(new Action(Messages.ToolBarEditor_AddDirectToolItem,
				createImageDescriptor(ResourceProvider.IMG_DirectToolItem)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, false);
			}
		});
		actions.add(new Action(Messages.ToolBarEditor_AddToolControl,
				createImageDescriptor(ResourceProvider.IMG_ToolControl)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.TOOL_CONTROL, false);
			}
		});
		actions.add(new Action(Messages.ToolBarEditor_AddToolBarSeparator,
				createImageDescriptor(ResourceProvider.IMG_ToolBarSeparator)) {
			@Override
			public void run() {
				handleAddChild(MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, true);
			}
		});
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_ToolBarContribution);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.ToolBarContributionEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {

		if (element instanceof ToolBarContributionImpl) {
			String pid = ((ToolBarContributionImpl) element).getParentId();
			if (E.notEmpty(pid)) {
				return pid;
			}
		}
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

		getMaster().setValue((MToolBarContribution) object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MToolBarContribution> master, boolean isImport) {
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

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ToolBarContributionEditor_ParentId);
			l.setToolTipText(Messages.ToolBarContributionEditor_ParentIdTooltip);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t),
					E4Properties.toolBarParentId(getEditingDomain()).observeDetail(getMaster()));

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ToolBarIdDialog dialog = new ToolBarIdDialog(t.getShell(), resource,
							getMaster().getValue(), getEditingDomain(), modelService, Messages);
					dialog.open();
				}
			});
		}

		ControlFactory.createTextField(parent, Messages.ToolBarContributionEditor_Position,
				Messages.ToolBarContributionEditor_PositionTooltip, master, context, textProp,
				E4Properties.toolBarPositionInParent(getEditingDomain()));

		// ------------------------------------------------------------
		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, null, this,
					UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
				@Override
				protected void addPressed() {
					final Struct struct = (Struct) getSelection().getFirstElement();
					final EClass eClass = struct.eClass;
					final MToolBarElement eObject = (MToolBarElement) EcoreUtil.create(eClass);
					setElementId(eObject);

					final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
							UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						if (!struct.separator) {
							getEditor().setSelection(eObject);
						}
					}
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(Messages.ToolBarEditor_ToolbarItems);
			final TableViewer viewer = pickList.getList();

			viewer.setInput(E4Properties.<MToolBarElement>children(getEditingDomain()).observeDetail(master));

			final Struct defaultStruct = new Struct(Messages.ToolBarEditor_HandledToolItem,
					MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, false);
			pickList.setInput(new Struct[] { defaultStruct,
					new Struct(Messages.ToolBarEditor_DirectToolItem, MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, false),
					new Struct(Messages.ToolBarEditor_ToolControl, MenuPackageImpl.Literals.TOOL_CONTROL, false),
					new Struct(Messages.ToolBarEditor_Separator, MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, true) });
			pickList.setSelection(new StructuredSelection(defaultStruct));
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

		createContributedEditorTabs(folder, context, getMaster(), MToolBarContribution.class);

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
		return E4Properties.<MToolBarElement>children().observe((MToolBarContribution) element);
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	protected void handleAddChild(EClass eClass, boolean separator) {
		final MToolBarElement eObject = (MToolBarElement) EcoreUtil.create(eClass);
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
}
