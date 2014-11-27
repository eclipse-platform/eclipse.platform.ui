/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
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
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.FeatureClassLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.tools.emf.ui.internal.imp.ModelImportWizard;
import org.eclipse.e4.tools.emf.ui.internal.imp.RegistryUtil;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
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
import org.eclipse.jface.databinding.viewers.IViewerValueProperty;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class PartSashContainerEditor extends AbstractComponentEditor {

	private Composite composite;
	private EMFDataBindingContext context;

	private final IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties
		.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	private StackLayout stackLayout;
	private final List<Action> actions = new ArrayList<Action>();
	private final List<Action> actionsImport = new ArrayList<Action>();

	@Inject
	@Optional
	private IProject project;

	@Inject
	private Shell shell;

	@Inject
	public PartSashContainerEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.PartSashContainerEditor_AddPartSashContainer,
			createImageDescriptor(ResourceProvider.IMG_PartSashContainer)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART_SASH_CONTAINER);
			}
		});
		actions.add(new Action(Messages.PartSashContainerEditor_AddPartStack,
			createImageDescriptor(ResourceProvider.IMG_PartStack)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART_STACK);
			}
		});
		actions.add(new Action(Messages.PartSashContainerEditor_AddPart,
			createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART);
			}
		});

		actions.add(new Action(Messages.PartSashContainerEditor_AddArea,
			createImageDescriptor(ResourceProvider.IMG_Area)) {
			@Override
			public void run() {
				handleAddChild(AdvancedPackageImpl.Literals.AREA);
			}
		});
		actions.add(new Action(Messages.PartSashContainerEditor_AddPlaceholder,
			createImageDescriptor(ResourceProvider.IMG_Placeholder)) {
			@Override
			public void run() {
				handleAddChild(AdvancedPackageImpl.Literals.PLACEHOLDER);
			}
		});
		for (final FeatureClass c : getEditor().getFeatureClasses(BasicPackageImpl.Literals.PART_STACK,
			UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN)) {
			final EClass ec = c.eClass;
			actions.add(new Action(c.label, createImageDescriptor(c.iconId)) {
				@Override
				public void run() {
					handleAddChild(ec);
				}
			});
		}

		// --- Import Actions ---
		actionsImport.add(new Action("Views", createImageDescriptor(ResourceProvider.IMG_Part)) { //$NON-NLS-1$
			@Override
			public void run() {
				handleImportChild(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_VIEW);
			}
		});
		actionsImport.add(new Action("Editors", createImageDescriptor(ResourceProvider.IMG_Part)) { //$NON-NLS-1$
			@Override
			public void run() {
				handleImportChild(BasicPackageImpl.Literals.INPUT_PART, RegistryUtil.HINT_EDITOR);
			}
		});

	}

	@Override
	public Image getImage(Object element, Display display) {
		final boolean horizontal = ((MPartSashContainer) element).isHorizontal();

		if (!horizontal) {
			final MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				return createImage(ResourceProvider.IMG_PartSashContainer_vertical);
			}
			return createImage(ResourceProvider.IMG_Tbr_PartSashContainer_vertical);
		}

		if (horizontal) {
			final MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				return createImage(ResourceProvider.IMG_PartSashContainer);
			}
			return createImage(ResourceProvider.IMG_Tbr_PartSashContainer);
		}
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PartSashContainerEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PartSashContainerEditor_Description;
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

	private Composite createForm(Composite parent, final EMFDataBindingContext context, WritableValue master,
		boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp,
			EMFEditProperties
			.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID));
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(),
			context, textProp,
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE));

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartSashContainerEditor_Orientation);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			final ComboViewer viewer = new ComboViewer(parent);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			viewer.getControl().setLayoutData(gd);
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((Boolean) element).booleanValue() ? Messages.PartSashContainerEditor_Horizontal
						: Messages.PartSashContainerEditor_Vertical;
				}
			});
			viewer.setInput(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
			final IViewerValueProperty vProp = ViewerProperties.singleSelection();
			context.bindValue(vProp.observe(viewer),
				EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.GENERIC_TILE__HORIZONTAL)
				.observeDetail(getMaster()));
		}

		ControlFactory.createSelectedElement(parent, this, context, Messages.PartSashContainerEditor_SelectedElement);
		ControlFactory.createTextField(parent, Messages.PartSashContainerEditor_ContainerData, master, context,
			textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__CONTAINER_DATA));

		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this,
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
				@Override
				protected void addPressed() {
					final EClass eClass = ((FeatureClass) ((IStructuredSelection) getSelection()).getFirstElement()).eClass;
					handleAddChild(eClass);
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(Messages.PartSashContainerEditor_Controls);

			pickList.setLabelProvider(new FeatureClassLabelProvider(getEditor()));

			final List<FeatureClass> eClassList = new ArrayList<FeatureClass>();
			eClassList.add(new FeatureClass("PartSashContainer", BasicPackageImpl.Literals.PART_SASH_CONTAINER)); //$NON-NLS-1$
			eClassList.add(new FeatureClass("PartStack", BasicPackageImpl.Literals.PART_STACK)); //$NON-NLS-1$
			eClassList.add(new FeatureClass("Part", BasicPackageImpl.Literals.PART)); //$NON-NLS-1$
			eClassList.add(new FeatureClass("InputPart", BasicPackageImpl.Literals.INPUT_PART)); //$NON-NLS-1$
			eClassList.add(new FeatureClass("Area", AdvancedPackageImpl.Literals.AREA)); //$NON-NLS-1$
			eClassList.add(new FeatureClass("Placeholder", AdvancedPackageImpl.Literals.PLACEHOLDER)); //$NON-NLS-1$
			eClassList.addAll(getEditor().getFeatureClasses(BasicPackageImpl.Literals.PART_SASH_CONTAINER,
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN));
			pickList.setInput(eClassList);
			pickList.setSelection(new StructuredSelection(eClassList.get(0)));

			final TableViewer viewer = pickList.getList();
			final IEMFListProperty prop = EMFProperties.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
			viewer.setInput(prop.observeDetail(getMaster()));
		}

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

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
			ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
			ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MPartSashContainer.class);

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
	public IObservableList getChildList(Object element) {
		return ELEMENT_CONTAINER__CHILDREN.observe(element);
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.GENERIC_TILE__HORIZONTAL),
			FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	protected void handleAddChild(EClass eClass) {
		final EObject eObject = EcoreUtil.create(eClass);
		addToModel(eObject);
	}

	private void addToModel(EObject eObject) {
		setElementId(eObject);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
			UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	protected void handleImportChild(EClass eClass, String hint) {

		if (eClass == BasicPackageImpl.Literals.PART) {
			final ModelImportWizard wizard = new ModelImportWizard(MPart.class, this, hint, resourcePool);
			final WizardDialog wizardDialog = new WizardDialog(shell, wizard);
			if (wizardDialog.open() == Window.OK) {
				final MPart[] parts = (MPart[]) wizard.getElements(MPart.class);
				for (final MPart part : parts) {
					addToModel((EObject) part);
				}
			}
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	@Override
	public List<Action> getActionsImport(Object element) {
		final ArrayList<Action> l = new ArrayList<Action>(super.getActionsImport(element));
		l.addAll(actionsImport);
		return l;
	}
}
