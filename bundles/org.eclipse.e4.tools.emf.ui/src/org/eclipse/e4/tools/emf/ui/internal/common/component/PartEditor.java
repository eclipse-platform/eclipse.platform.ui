/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 437951, Ongoing Maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.ContributionURIValidator;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.PartIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.objectdata.ObjectViewer;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

public class PartEditor extends AbstractComponentEditor {

	private Composite composite;
	private EMFDataBindingContext context;

	private final IListProperty PART__MENUS = EMFProperties.list(BasicPackageImpl.Literals.PART__MENUS);
	private final IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties
		.list(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
	private final IValueProperty PART__TOOLBAR = EMFProperties.value(BasicPackageImpl.Literals.PART__TOOLBAR);
	private Button createRemoveToolBar;
	private StackLayout stackLayout;

	@Inject
	@Optional
	private IProject project;

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public PartEditor() {
		super();
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			if (((MUIElement) element).isToBeRendered()) {
				return createImage(ResourceProvider.IMG_Part);
			}
			return createImage(ResourceProvider.IMG_Tbr_Part);
		}

		return null;
	}

	@Override
	public String getLabel(Object element) {
		if (element == BasicPackageImpl.Literals.INPUT_PART) {
			return Messages.PartEditor_InputPart;
		}
		return Messages.PartEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PartEditor_Description;
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

		if (createRemoveToolBar != null) {
			createRemoveToolBar.setSelection(((MPart) object).getToolbar() != null);
		}

		getMaster().setValue(object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL,
			ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);

		return composite;
	}

	protected Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue master,
		boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.BORDER);
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
		ControlFactory.createTextField(parent, Messages.PartEditor_LabelLabel, master, context, textProp,
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL));
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, master, context,
			textProp,
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE));
		ControlFactory.createTextField(parent, Messages.PartEditor_Tooltip, master, context, textProp,
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__TOOLTIP));

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartEditor_IconURI);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			l.setToolTipText(Messages.PartEditor_IconURI_Tooltip);

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(
				textProp.observeDelayed(200, t),
				EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__ICON_URI).observeDetail(
					master));

			new ImageTooltip(t, Messages) {

				@Override
				protected URI getImageURI() {
					final MUILabel part = (MUILabel) getMaster().getValue();
					final String uri = part.getIconURI();
					if (uri == null || uri.trim().length() == 0) {
						return null;
					}
					return URI.createURI(part.getIconURI());
				}
			};

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final PartIconDialogEditor dialog = new PartIconDialogEditor(b.getShell(), eclipseContext, project,
						getEditingDomain(), (MPart) getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		final Link lnk;
		{
			final IContributionClassCreator c = getEditor().getContributionCreator(BasicPackageImpl.Literals.PART);
			if (project != null && c != null) {
				lnk = new Link(parent, SWT.NONE);
				lnk.setText("<A>" + Messages.PartEditor_ClassURI + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
				lnk.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				lnk.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						c.createOpen((MContribution) getMaster().getValue(), getEditingDomain(), project,
							lnk.getShell());
					}
				});
			} else {
				// Dispose the lnk widget, which is unused in this else branch
				// and screws up the layout: see https://bugs.eclipse.org/421369
				lnk = null;

				final Label l = new Label(parent, SWT.NONE);
				l.setText(Messages.PartEditor_ClassURI);
				l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			}

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					// lnk might be disposed if else branch above taken
					if (lnk != null) {
						lnk.setToolTipText(((Text) e.getSource()).getText());
					}
				}
			});
			final Binding binding = context.bindValue(
				textProp.observeDelayed(200, t),
				EMFEditProperties.value(getEditingDomain(),
					ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(master),
				new UpdateValueStrategy().setAfterConvertValidator(new ContributionURIValidator()),
				new UpdateValueStrategy());
			Util.addDecoration(t, binding);

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(), eclipseContext,
						getEditingDomain(), (MContribution) getMaster().getValue(),
						ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI, Messages);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		ControlFactory.createTextField(parent, Messages.PartEditor_ContainerData, master, context, textProp,
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__CONTAINER_DATA));

		createSubformElements(parent, context, master);

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartEditor_ToolBar);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			createRemoveToolBar = new Button(parent, SWT.CHECK);
			createRemoveToolBar.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final MPart window = (MPart) getMaster().getValue();
					if (window.getToolbar() == null) {
						addToolBar();
					} else {
						removeToolBar();
					}
				}
			});
			createRemoveToolBar.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		}

		ControlFactory.createCheckBox(parent, Messages.PartEditor_Closeable, Messages.PartEditor_Closeable_Tooltip,
			getMaster(), context, WidgetProperties.selection(),
			EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.PART__CLOSEABLE));

		// ------------------------------------------------------------
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
			WidgetProperties.selection(),
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
			WidgetProperties.selection(),
			EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

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

	private void addToolBar() {
		final MToolBar menu = MMenuFactory.INSTANCE.createToolBar();
		setElementId(menu);

		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
			BasicPackageImpl.Literals.PART__TOOLBAR, menu);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	private void removeToolBar() {
		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
			BasicPackageImpl.Literals.PART__TOOLBAR, null);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	protected void createSubformElements(Composite parent, EMFDataBindingContext context, IObservableValue master) {

	}

	@Override
	public IObservableList getChildList(final Object element) {
		final WritableList list = new WritableList();

		if (getEditor().isModelFragment() && Util.isImport((EObject) element)) {
			return list;
		}

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_PART_MENU, PART__MENUS, element,
			Messages.PartEditor_Menus) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_HANDLER, HANDLER_CONTAINER__HANDLERS, element,
			Messages.PartEditor_Handlers) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		final MPart window = (MPart) element;
		if (window.getToolbar() != null) {
			list.add(0, window.getToolbar());
		}

		PART__TOOLBAR.observe(element).addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
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
			FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}
}