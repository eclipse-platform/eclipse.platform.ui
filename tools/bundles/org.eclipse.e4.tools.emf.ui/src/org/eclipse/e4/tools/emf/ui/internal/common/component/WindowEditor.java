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
 * Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 * Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 437951
 * Olivier Prouvost <olivier@opcoach.com> - Bug 472658
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.WindowIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
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

public class WindowEditor extends AbstractComponentEditor<MWindow> {

	private Composite composite;
	private EMFDataBindingContext context;
	private Action addMainMenu;
	private Button createRemoveMainMenu;
	private StackLayout stackLayout;

	@Inject
	@Optional
	private IProject project;

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public WindowEditor() {
		super();
	}

	@PostConstruct
	void init() {
		addMainMenu = new Action(Messages.WindowEditor_AddMainMenu) {
			@Override
			public void run() {
				addMenu();
			}
		};
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_Window);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.WindowEditor_TreeLabel;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.WindowEditor_TreeLabelDescription;
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

		if (createRemoveMainMenu != null) {
			createRemoveMainMenu.setSelection(((MWindow) object).getMainMenu() != null);
		}

		getMaster().setValue((MWindow) object);
		enableIdGenerator(UiPackageImpl.Literals.UI_LABEL__LABEL,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, null);

		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue<MWindow> master,
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

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, getMaster(), context, textProp,
				E4Properties.elementId(getEditingDomain()));

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.WindowEditor_Bounds);
			l.setLayoutData(new GridData());

			final Composite comp = new Composite(parent, SWT.NONE);
			final GridLayout layout = new GridLayout(4, true);
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			comp.setLayout(layout);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			comp.setLayoutData(gd);

			Text t = new Text(comp, SWT.BORDER | SWT.TRAIL);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(
					textProp.observeDelayed(200, t),
					E4Properties.windowX(getEditingDomain()).observeDetail(getMaster()),
					UnsettableUpdateValueStrategy.create(), UnsettableUpdateValueStrategy.create());

			t = new Text(comp, SWT.BORDER | SWT.TRAIL);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(
					textProp.observeDelayed(200, t),
					E4Properties.windowY(getEditingDomain()).observeDetail(getMaster()),
					UnsettableUpdateValueStrategy.create(), UnsettableUpdateValueStrategy.create());

			t = new Text(comp, SWT.BORDER | SWT.TRAIL);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(
					textProp.observeDelayed(200, t),
					E4Properties.width(getEditingDomain()).observeDetail(getMaster()),
					UnsettableUpdateValueStrategy.create(), UnsettableUpdateValueStrategy.create());

			t = new Text(comp, SWT.BORDER | SWT.TRAIL);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(
					textProp.observeDelayed(200, t),
					E4Properties.height(getEditingDomain()).observeDetail(getMaster()),
					UnsettableUpdateValueStrategy.create(), UnsettableUpdateValueStrategy.create());
		}

		ControlFactory.createTranslatedTextField(parent, Messages.WindowEditor_Label, getMaster(), context, textProp,
				E4Properties.label(getEditingDomain()), resourcePool, project);
		ControlFactory.createTranslatedTextField(parent, Messages.WindowEditor_Tooltip, getMaster(), context, textProp,
				E4Properties.tooltip(getEditingDomain()), resourcePool, project);

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.WindowEditor_IconURI);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(
					textProp.observeDelayed(200, t),
					E4Properties.iconUri(getEditingDomain()).observeDetail(master));

			new ImageTooltip(t, Messages, this);

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final WindowIconDialogEditor dialog = new WindowIconDialogEditor(b.getShell(), eclipseContext,
							project, getEditingDomain(), getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.WindowEditor_MainMenu);
			l.setLayoutData(new GridData());

			createRemoveMainMenu = new Button(parent, SWT.CHECK);
			createRemoveMainMenu.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final MWindow window = getMaster().getValue();
					if (window.getMainMenu() == null) {
						addMenu();
					} else {
						removeMenu();
					}
				}
			});
			createRemoveMainMenu.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		}

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.toBeRendered(getEditingDomain()));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.visible(getEditingDomain()));

		ControlFactory.createSelectedElement(parent, this, context, Messages.WindowEditor_SelectedElement);
		ControlFactory.createBindingContextWiget(parent, Messages, this, Messages.WindowEditor_BindingContexts);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Context_Properties,
				UiPackageImpl.Literals.CONTEXT__PROPERTIES, VERTICAL_LIST_WIDGET_INDENT);

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createTranslatedTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase,
				getMaster(), context, textProp, E4Properties.accessibilityPhrase(getEditingDomain()),
				resourcePool, project);
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.ModelTooling_Context_Variables,
				UiPackageImpl.Literals.CONTEXT__VARIABLES, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MWindow.class);

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

	void removeMenu() {
		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
				BasicPackageImpl.Literals.WINDOW__MAIN_MENU, null);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	void addMenu() {
		final MMenu menu = MMenuFactory.INSTANCE.createMenu();
		setElementId(menu);

		final Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(),
				BasicPackageImpl.Literals.WINDOW__MAIN_MENU, menu);
		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
		}
	}

	@Override
	public IObservableList<Object> getChildList(final Object element) {
		final WritableList<Object> list = new WritableList<>();
		if (getEditor().isModelFragment() && Util.isImport((EObject) element)) {
			return list;
		}

		final MWindow window = (MWindow) element;

		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_HANDLER, E4Properties.handlers(), window,
				Messages.WindowEditor_Handlers));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_WINDOW_WINDOWS, E4Properties.windowWindows(), window,
				Messages.WindowEditor_Windows));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_WINDOW_CONTROLS, E4Properties.children(), window,
				Messages.WindowEditor_Controls));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_WINDOW_SHARED_ELEMENTS, E4Properties.sharedElements(), window,
				Messages.WindowEditor_SharedElements));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_WINDOW_SNIPPETS, E4Properties.snippets(), window,
				Messages.WindowEditor_Snippets));

		if (window.getMainMenu() != null) {
			list.add(0, window.getMainMenu());
		}

		E4Properties.mainMenu().observe(window).addValueChangeListener(event -> {
			if (createRemoveMainMenu.isDisposed() || getMaster().isDisposed()) {
				return;
			}

			if (event.diff.getOldValue() != null) {
				list.remove(event.diff.getOldValue());
				if (getMaster().getValue() == element) {
					createRemoveMainMenu.setSelection(false);
				}
			}

			if (event.diff.getNewValue() != null) {
				list.add(0, event.diff.getNewValue());
				if (getMaster().getValue() == element) {
					createRemoveMainMenu.setSelection(true);
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
				FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__ICON_URI),
				FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	@Override
	public List<Action> getActions(Object element) {
		final List<Action> actions = new ArrayList<>();

		final MWindow window = (MWindow) element;
		if (window.getMainMenu() == null) {
			actions.add(getActionAddMainMenu());
		}

		return actions;
	}

	protected Action getActionAddMainMenu() {
		return addMainMenu;
	}
}
