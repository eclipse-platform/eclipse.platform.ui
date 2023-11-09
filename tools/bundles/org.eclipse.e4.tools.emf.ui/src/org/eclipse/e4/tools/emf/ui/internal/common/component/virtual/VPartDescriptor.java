/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
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
 *     Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.imp.ModelImportWizard;
import org.eclipse.e4.tools.emf.ui.internal.imp.RegistryUtil;
import org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class VPartDescriptor extends AbstractComponentEditor<MPartDescriptorContainer> {
	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private final List<Action> actions = new ArrayList<>();
	private final List<Action> actionsImport = new ArrayList<>();

	@Inject
	public VPartDescriptor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.VPartDescriptor_AddPartDescriptor, createImageDescriptor(ResourceProvider.IMG_PartDescriptor)) {
			@Override
			public void run() {
				handleAdd();
			}
		});

		// --- Import Actions ---
		actionsImport.add(new Action(Messages.VPartDescriptor_Views, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART_DESCRIPTOR, RegistryUtil.HINT_VIEW);
			}
		});
		actionsImport.add(new Action(Messages.VPartDescriptor_Editors, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART_DESCRIPTOR, RegistryUtil.HINT_EDITOR);
			}
		});

		actionsImport.add(new Action(Messages.VPartDescriptor_ViewAsCompatibilityView, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART_DESCRIPTOR, RegistryUtil.HINT_COMPAT_VIEW);
			}
		});
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VPartDescriptor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VPartDescriptor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		@SuppressWarnings("unchecked")
		final VirtualEntry<MPartDescriptorContainer, ?> o = (VirtualEntry<MPartDescriptorContainer, ?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MPartDescriptorContainer> master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		{
			final AbstractPickList pickList = new E4PickList(parent, SWT.NONE,
					Arrays.asList(PickListFeatures.NO_PICKER), this,
					BasicPackageImpl.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS) {
				@Override
				protected void addPressed() {
					handleAdd();
				}

				@Override
				protected List<?> getContainerChildren(Object container) {
					final MPartDescriptorContainer c = (MPartDescriptorContainer) container;
					return c.getDescriptors();
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			viewer = pickList.getList();
		}

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	protected void handleAdd() {
		final MPartDescriptor partDescription = MBasicFactory.INSTANCE.createPartDescriptor();
		addToModel(partDescription);
	}

	private void addToModel(MPartDescriptor partDescription) {
		setElementId(partDescription);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS, partDescription);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(partDescription);
		}
	}

	protected void handleImport(EClass eClass, String hint) {

		if (eClass == BasicPackageImpl.Literals.PART_DESCRIPTOR) {
			final ModelImportWizard wizard = new ModelImportWizard(MPartDescriptor.class, this, hint, resourcePool);
			final WizardDialog wizardDialog = new WizardDialog(viewer.getControl().getShell(), wizard);
			if (wizardDialog.open() == Window.OK) {
				final MPartDescriptor[] parts = (MPartDescriptor[]) wizard.getElements(MPartDescriptor.class);
				for (final MPartDescriptor part : parts) {
					addToModel(part);
				}
			}
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	@Override
	public List<Action> getActionsImport(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActionsImport(element));
		l.addAll(actionsImport);
		return l;
	}
}
