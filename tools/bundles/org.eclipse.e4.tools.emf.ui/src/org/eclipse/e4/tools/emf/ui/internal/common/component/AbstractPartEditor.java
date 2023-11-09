/*******************************************************************************
 * Copyright (c) 2019 Airbus Defence and Space GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Benedikt Kuntz <benedikt.kuntz@airbus.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

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
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.PartIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.objectdata.ObjectViewer;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
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

public abstract class AbstractPartEditor<M extends MPart> extends AbstractComponentEditor<M> {
	private Composite composite;
	private EMFDataBindingContext context;
	private Button createRemoveToolBarButton;
	private StackLayout stackLayout;

	@Inject
	@Optional
	private IProject project;

	@Inject
	private IEclipseContext eclipseContext;

	@Inject
	public AbstractPartEditor() {
		super();
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.GENERIC_TILE__HORIZONTAL),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
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

		if (createRemoveToolBarButton != null) {
			createRemoveToolBarButton.setSelection(((MPart) object).getToolbar() != null);
		}

		return composite;
	}

	protected Composite createForm(Composite parent, final EMFDataBindingContext context,
			WritableValue<M> master, boolean isImport) {

		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.BORDER);
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
		ControlFactory.createTextField(parent, Messages.PartEditor_LabelLabel, master, context, textProp,
				E4Properties.label(getEditingDomain()));
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, master, context,
				textProp, E4Properties.accessibilityPhrase(getEditingDomain()));
		ControlFactory.createTextField(parent, Messages.PartEditor_Tooltip, master, context, textProp,
				E4Properties.tooltip(getEditingDomain()));

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartEditor_IconURI);
			l.setLayoutData(new GridData());
			l.setToolTipText(Messages.PartEditor_IconURI_Tooltip);

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
					final PartIconDialogEditor dialog = new PartIconDialogEditor(b.getShell(), eclipseContext, project,
							getEditingDomain(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}


		ControlFactory.createClassURIField(parent, Messages, this, Messages.PartEditor_ClassURI,
				ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI,
				getEditor().getContributionCreator(BasicPackageImpl.Literals.PART), project, context, eclipseContext);

		// ------------------------------------------------------------
		ControlFactory.createTextField(parent, Messages.PartEditor_ContainerData, master, context, textProp,
				E4Properties.containerData(getEditingDomain()));

		createSubformElements(parent, context, master);

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartEditor_ToolBar);
			l.setLayoutData(new GridData());

			createRemoveToolBarButton = new Button(parent, SWT.CHECK);
			createRemoveToolBarButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final MPart window = getMaster().getValue();
					if (window.getToolbar() == null) {
						addToolBar();
					} else {
						removeToolBar();
					}
				}
			});
			createRemoveToolBarButton
			.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		}

		ControlFactory.createCheckBox(parent, Messages.PartEditor_Closeable, Messages.PartEditor_Closeable_Tooltip,
				getMaster(), context, WidgetProperties.buttonSelection(),
				E4Properties.partClosable(getEditingDomain()));

		// ------------------------------------------------------------
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.toBeRendered(getEditingDomain()));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.visible(getEditingDomain()));

		ControlFactory.createBindingContextWiget(parent, Messages, this, Messages.PartEditor_BindingContexts);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Context_Properties,
				UiPackageImpl.Literals.CONTEXT__PROPERTIES, VERTICAL_LIST_WIDGET_INDENT);

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_Context_Variables,
				Messages.ModelTooling_Context_Variables_Tooltip, UiPackageImpl.Literals.CONTEXT__VARIABLES,
				VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createInstanceInspection(folder);
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MPart.class);

		folder.setSelection(0);

		return folder;
	}

	protected void createSubformElements(Composite parent, EMFDataBindingContext context,
			IObservableValue<M> master) {
		// add nothing as default, allow to be overwritten
	}

	protected void addToolBar() {
		final MToolBar menu = MMenuFactory.INSTANCE.createToolBar();
		setElementId(menu);

		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
				BasicPackageImpl.Literals.PART__TOOLBAR, menu);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	protected void removeToolBar() {
		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
				BasicPackageImpl.Literals.PART__TOOLBAR, null);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	private void createInstanceInspection(CTabFolder folder) {
		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_RuntimeContributionInstance);
		final Composite container = new Composite(folder, SWT.NONE);
		container.setLayout(new GridLayout());
		item.setControl(container);

		final ObjectViewer objectViewer = new ObjectViewer();
		final TreeViewer viewer = objectViewer.createViewer(container,
				ApplicationPackageImpl.Literals.CONTRIBUTION__OBJECT, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
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
		final WritableList<Object> list = new WritableList<>();
		final MPart part = (MPart) element;

		if (getEditor().isModelFragment() && Util.isImport((EObject) element)) {
			return list;
		}

		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_PART_MENU, E4Properties.partMenus(), part,
				Messages.PartEditor_Menus));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_HANDLER, E4Properties.handlers(), part,
				Messages.PartEditor_Handlers));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_PART_TRIMS, E4Properties.partTrimBars(), part,
				Messages.PartEditor_TrimBars));

		if (part.getToolbar() != null) {
			list.add(0, part.getToolbar());
		}

		E4Properties.partToolbar().observe(part).addValueChangeListener(event -> {
			if (event.diff.getOldValue() != null) {
				list.remove(event.diff.getOldValue());
				if (getMaster().getValue() == element && !createRemoveToolBarButton.isDisposed()) {
					createRemoveToolBarButton.setSelection(false);
				}

			}

			if (event.diff.getNewValue() != null) {
				list.add(0, event.diff.getNewValue());
				if (getMaster().getValue() == element && !createRemoveToolBarButton.isDisposed()) {
					createRemoveToolBarButton.setSelection(true);
				}
			}
		});

		addChildListEntries(part, list);

		return list;
	}


	protected void addChildListEntries(MPart part, IObservableList<Object> list) {
		// add nothing by default, allow to be overwritten
	}


	@Override
	public void dispose() {
		super.dispose();
		if (context != null) {
			context.dispose();
		}
	}

}
