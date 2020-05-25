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
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Bug 404166, 424730, 437951
 * Olivier Prouvost <olivier@opcoach.com> - Bug 472658, 530887, 412567
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.PartDescriptorIconDialogEditor;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PartDescriptorEditor extends AbstractComponentEditor<MPartDescriptor> {

	private Composite composite;
	private EMFDataBindingContext context;

	@Inject
	@Optional
	private IProject project;

	@Inject
	IEclipseContext eclipseContext;

	private Button createRemoveToolBar;
	private StackLayout stackLayout;

	@Inject
	public PartDescriptorEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_PartDescriptor);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PartDescriptorEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PartDescriptorEditor_Descriptor;
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

		if (createRemoveToolBar != null) {
			createRemoveToolBar.setSelection(((MPartDescriptor) object).getToolbar() != null);
		}

		getMaster().setValue((MPartDescriptor) object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);
		return composite;
	}

	protected Composite createForm(Composite parent, EMFDataBindingContext context,
			IObservableValue<MPartDescriptor> master, boolean isImport) {
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
		ControlFactory.createTranslatedTextField(parent, Messages.PartDescriptorEditor_LabelLabel, master, context,
				textProp, E4Properties.label(getEditingDomain()),
				resourcePool, project);
		ControlFactory.createTranslatedTextField(parent, Messages.PartDescriptorEditor_Tooltip, master, context,
				textProp, E4Properties.tooltip(getEditingDomain()), resourcePool, project);

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_IconURI);
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
					final PartDescriptorIconDialogEditor dialog = new PartDescriptorIconDialogEditor(b.getShell(),
							eclipseContext, project, getEditingDomain(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		IContributionClassCreator contributionCreator = getEditor().getContributionCreator(
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.PART);

		ControlFactory.createClassURIField(parent, Messages, this, Messages.PartEditor_ClassURI,
				BasicPackageImpl.Literals.PART_DESCRIPTOR__CONTRIBUTION_URI,
				contributionCreator,
				project, context,
				eclipseContext, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final MPart dummyPart = MBasicFactory.INSTANCE.createPart();
				final String contributionURI = getMaster().getValue().getContributionURI();
				dummyPart.setContributionURI(contributionURI);
				contributionCreator.createOpen(dummyPart, getEditingDomain(), project, folder.getShell());
				master.getValue().setContributionURI(dummyPart.getContributionURI());
			}
		});

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartEditor_ToolBar);
			l.setLayoutData(new GridData());

			createRemoveToolBar = new Button(parent, SWT.CHECK);
			createRemoveToolBar.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final MPartDescriptor window = getMaster().getValue();
					if (window.getToolbar() == null) {
						addToolBar();
					} else {
						removeToolBar();
					}
				}
			});
			createRemoveToolBar.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		}

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_ContainerData);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			// TODO: Is it correct to use containerData on an MPartDescriptor?
			@SuppressWarnings({ "unchecked", "rawtypes" })
			IValueProperty<MPartDescriptor, String> containerData = (IValueProperty) E4Properties
			.containerData(getEditingDomain());
			context.bindValue(textProp.observeDelayed(200, t),
					containerData.observeDetail(master));
		}


		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Closeable);
			l.setLayoutData(new GridData());

			final Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));

			context.bindValue(WidgetProperties.buttonSelection().observe(checkbox),
					E4Properties.closable(getEditingDomain()).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Multiple);
			l.setLayoutData(new GridData());

			final Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));

			context.bindValue(WidgetProperties.buttonSelection().observe(checkbox),
					E4Properties.allowMultiple(getEditingDomain()).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Category);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
			context.bindValue(textProp.observeDelayed(200, t),
					E4Properties.category(getEditingDomain()).observeDetail(master));
		}

		ControlFactory.createBindingContextWiget(parent, Messages, this, Messages.PartEditor_BindingContexts);

		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Context_Properties,
				BasicPackageImpl.Literals.PART_DESCRIPTOR__PROPERTIES, VERTICAL_LIST_WIDGET_INDENT);

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_Context_Variables,
				Messages.ModelTooling_Context_Variables_Tooltip, BasicPackageImpl.Literals.PART_DESCRIPTOR__VARIABLES,
				VERTICAL_LIST_WIDGET_INDENT);

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);

		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MPartDescriptor.class);

		folder.setSelection(0);

		return folder;
	}

	private void addToolBar() {
		final MToolBar menu = MMenuFactory.INSTANCE.createToolBar();
		setElementId(menu);

		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
				BasicPackageImpl.Literals.PART_DESCRIPTOR__TOOLBAR, menu);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	private void removeToolBar() {
		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
				BasicPackageImpl.Literals.PART_DESCRIPTOR__TOOLBAR, null);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	@Override
	public IObservableList<?> getChildList(final Object element) {
		final WritableList<Object> list = new WritableList<>();
		final MPartDescriptor partDescriptor = (MPartDescriptor) element;

		if (getEditor().isModelFragment() && Util.isImport((EObject) element)) {
			return list;
		}

		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_PARTDESCRIPTOR_MENU, E4Properties.partDescriptorMenus(), partDescriptor,
				Messages.PartDescriptorEditor_Menus));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_HANDLER, E4Properties.handlers(), partDescriptor,
				Messages.PartDescriptorEditor_Handlers));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_PARTDESCRIPTOR_TRIMS, E4Properties.partDescriptorTrimBars(),
				partDescriptor, Messages.PartDescriptorEditor_TrimBars));

		if (partDescriptor.getToolbar() != null) {
			list.add(0, partDescriptor.getToolbar());
		}

		E4Properties.partDescriptorToolbar().observe(partDescriptor).addValueChangeListener(event -> {
			if (event.diff.getOldValue() != null) {
				list.remove(event.diff.getOldValue());
				if (getMaster().getValue() == element && !createRemoveToolBar.isDisposed()) {
					createRemoveToolBar.setSelection(false);
				}

			}

			if (event.diff.getNewValue() != null) {
				list.add(0, event.diff.getNewValue());
				if (getMaster().getValue() == element && !createRemoveToolBar.isDisposed()) {
					createRemoveToolBar.setSelection(true);
				}
			}
		});

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		return getLocalizedLabel((MUILabel) element);
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__ICON_URI) };
	}
}