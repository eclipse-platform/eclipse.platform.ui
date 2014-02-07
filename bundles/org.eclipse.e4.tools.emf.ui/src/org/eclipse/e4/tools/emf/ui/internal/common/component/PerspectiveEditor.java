/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.PerspectiveIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
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
import org.eclipse.swt.widgets.Text;

public class PerspectiveEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	private IListProperty PERSPECTIVE__WINDOWS = EMFProperties.list(AdvancedPackageImpl.Literals.PERSPECTIVE__WINDOWS);
	private EStackLayout stackLayout;
	private List<Action> actions = new ArrayList<Action>();

	@Inject
	@Optional
	private IProject project;

	@Inject
	public PerspectiveEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.PerspectiveEditor_AddPartSashContainer, createImageDescriptor(ResourceProvider.IMG_Perspective)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART_SASH_CONTAINER);
			}
		});
		actions.add(new Action(Messages.PerspectiveEditor_AddPartStack, createImageDescriptor(ResourceProvider.IMG_PartStack)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART_STACK);
			}
		});
		actions.add(new Action(Messages.PerspectiveEditor_AddPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.PART);
			}
		});
		actions.add(new Action(Messages.PerspectiveEditor_AddInputPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAddChild(BasicPackageImpl.Literals.INPUT_PART);
			}
		});
		actions.add(new Action(Messages.PerspectiveEditor_AddArea, createImageDescriptor(ResourceProvider.IMG_Area)) {
			@Override
			public void run() {
				handleAddChild(AdvancedPackageImpl.Literals.AREA);
			}
		});
		actions.add(new Action(Messages.PerspectiveEditor_AddPlaceholder, createImageDescriptor(ResourceProvider.IMG_Placeholder)) {
			@Override
			public void run() {
				handleAddChild(AdvancedPackageImpl.Literals.PLACEHOLDER);
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				return createImage(ResourceProvider.IMG_Perspective);
			} else {
				return createImage(ResourceProvider.IMG_Tbr_Perspective);
			}
		}

		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PerspectiveEditor_Label;
	}

	@Override
	public String getDetailLabel(Object element) {
		return getLocalizedLabel((MUILabel) element);
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL), FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PerspectiveEditor_Description;
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

	private Composite createForm(Composite parent, final EMFDataBindingContext context, WritableValue master, boolean isImport) {
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
		ControlFactory.createSelectedElement(parent, this, context, Messages.PerspectiveEditor_SelectedElement);
		ControlFactory.createTranslatedTextField(parent, Messages.PerspectiveEditor_LabelLabel, getMaster(), context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL), resourcePool, project);
		ControlFactory.createTranslatedTextField(parent, Messages.PerspectiveEditor_Tooltip, getMaster(), context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__TOOLTIP), resourcePool, project);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PerspectiveEditor_IconURI);
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
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PerspectiveIconDialogEditor dialog = new PerspectiveIconDialogEditor(b.getShell(), project, getEditingDomain(), (MPerspective) getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Context_Properties, UiPackageImpl.Literals.CONTEXT__PROPERTIES, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_Context_Variables, UiPackageImpl.Literals.CONTEXT__VARIABLES, VERTICAL_LIST_WIDGET_INDENT);

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MPerspective.class);

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
		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_PERSPECTIVE_WINDOWS, PERSPECTIVE__WINDOWS, element, Messages.WindowEditor_Windows) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_PERSPECTIVE_CONTROLS, ELEMENT_CONTAINER__CHILDREN, element, Messages.PerspectiveEditor_Controls) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});

		return list;
	}

	protected void handleAddChild(EClass eClass) {
		EObject eObject = EcoreUtil.create(eClass);
		setElementId(eObject);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
