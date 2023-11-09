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
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 *     Steven Spungin <steven@spungin.tv> - Bug 424730
 * Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 412567
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.text.MessageFormat;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.CommandToStringConverter;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.HandlerCommandSelectionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.objectdata.ObjectViewer;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
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

import jakarta.inject.Inject;

public class HandlerEditor extends AbstractComponentEditor<MHandler> {
	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;

	@Inject
	private IModelResource resource;

	@Inject
	@Optional
	private IProject project;

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public HandlerEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_Handler);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.HandlerEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.HandlerEditor_Description;
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

		getMaster().setValue((MHandler) object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue<MHandler> master,
			boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);

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

		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandlerEditor_Command);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEditable(false);
			context.bindValue(textProp.observeDelayed(200, t),
					E4Properties.command(getEditingDomain()).observeDetail(getMaster()),
					new UpdateValueStrategy<>(), UpdateValueStrategy.create(new CommandToStringConverter(Messages)));

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final HandlerCommandSelectionDialog dialog = new HandlerCommandSelectionDialog(b.getShell(),
							getMaster().getValue(), resource, Messages);
					dialog.open();
				}
			});
		}

		ControlFactory.createClassURIField(parent, Messages, this, Messages.HandlerEditor_ClassURI,
				ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI,
				getEditor().getContributionCreator(CommandsPackageImpl.Literals.HANDLER), project, context,
				eclipseContext);

		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createInstanceInspection(folder);

		createContributedEditorTabs(folder, context, getMaster(), MHandler.class);

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
		final TreeViewer viewer = objectViewer.createViewer(container, ApplicationPackageImpl.Literals.CONTRIBUTION__OBJECT, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		final MHandler handler = (MHandler) element;
		if (handler.getCommand() != null && handler.getCommand().getCommandName() != null && handler.getCommand().getCommandName().trim().length() > 0) {
			return MessageFormat.format("{0} -> {1}", handler.getElementId(), //$NON-NLS-1$
					translate(handler.getCommand().getCommandName()));
		}
		return MessageFormat.format("{0} -> {1}", handler.getElementId(), //$NON-NLS-1$
				(handler.getCommand() != null ? handler.getCommand().getElementId() : handler.getCommand()));
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.HANDLER__COMMAND, CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME) };
	}
}
