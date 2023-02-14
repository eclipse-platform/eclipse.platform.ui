/*******************************************************************************
 * Copyright (c) 2010, 2021 BestSolution.at and others.
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
 * Olivier Prouvost <olivier@opcoach.com> - Bug 462818
 * Simon Scholz <simon.scholz@vogella.com> - Bug 475365
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
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.EClassLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.TrimIdDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.TrimContributionImpl;
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
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
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

public class TrimContributionEditor extends AbstractComponentEditor<MTrimContribution> {
	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;
	private final List<Action> actions = new ArrayList<>();

	@Inject
	@Optional
	protected IProject project;

	@Inject
	private IModelResource resource;

	@Inject
	private EModelService modelService;

	@Inject
	public TrimContributionEditor() {
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
		return getImage(element, ResourceProvider.IMG_TrimContribution);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.TrimContributionEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {

		if (element instanceof TrimContributionImpl) {
			String pid = ((TrimContributionImpl) element).getParentId();
			if (E.notEmpty(pid)) {
				return pid;
			}
		}
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.TrimContributionEditor_TreeLabelDescription;
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

		getMaster().setValue((MTrimContribution) object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MTrimContribution> master, boolean isImport) {
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
			l.setText(Messages.MenuContributionEditor_ParentId);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t),
					E4Properties.trimParentId(getEditingDomain()).observeDetail(getMaster()));

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TrimIdDialog dialog = new TrimIdDialog(t.getShell(), resource,
							getMaster().getValue(), getEditingDomain(), modelService, Messages);
					dialog.open();
				}
			});

		}

		ControlFactory.createTextField(parent, Messages.TrimContributionEditor_Position, master, context, textProp,
				E4Properties.trimPositionInParent(getEditingDomain()));

		// ------------------------------------------------------------
		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, null, this,
					UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
				@Override
				protected void addPressed() {
					final EClass eClass = (EClass) getSelection().getFirstElement();
					handleAddChild(eClass);
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(Messages.TrimContributionEditor_Controls);

			final TableViewer viewer = pickList.getList();
			final FontDescriptor italicFontDescriptor = FontDescriptor.createFrom(viewer.getControl().getFont())
					.setStyle(SWT.ITALIC);
			viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
					new ComponentLabelProvider(getEditor(), Messages, italicFontDescriptor)));
			viewer.setContentProvider(new ObservableListContentProvider<>());
			viewer.setInput(E4Properties.<MTrimElement>children().observeDetail(getMaster()));

			pickList.setContentProvider(ArrayContentProvider.getInstance());
			pickList.setLabelProvider(new EClassLabelProvider(getEditor()));
			pickList.setInput(
					new EClass[] { MenuPackageImpl.Literals.TOOL_BAR, MenuPackageImpl.Literals.TOOL_CONTROL });
			pickList.setSelection(new StructuredSelection(MenuPackageImpl.Literals.TOOL_BAR));
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

		createContributedEditorTabs(folder, context, getMaster(), MTrimContribution.class);

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
		return E4Properties.<MTrimElement>children().observe((MTrimContribution) element);
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	protected void handleAddChild(EClass eClass) {
		final EObject toolbar = EcoreUtil.create(eClass);
		setElementId(toolbar);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, toolbar);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(toolbar);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
