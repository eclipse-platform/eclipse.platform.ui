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
 *     Wim Jongman <wim.jongman@remainsoftware.com> - bug 400804
 *     Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.EClassLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.imp.ModelImportWizard;
import org.eclipse.e4.tools.emf.ui.internal.imp.RegistryUtil;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class VWindowSharedElementsEditor extends AbstractComponentEditor<MWindow> {
	private Composite composite;
	private EMFDataBindingContext context;
	private StructuredViewer viewer;
	private final List<Action> actions = new ArrayList<>();
	private final List<Action> actionsImport = new ArrayList<>();

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public VWindowSharedElementsEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.VWindowSharedElementsEditor_AddPartSashContainer, createImageDescriptor(ResourceProvider.IMG_PartSashContainer)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_SASH_CONTAINER);
			}
		});

		actions.add(new Action(Messages.VWindowSharedElementsEditor_AddPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART);
			}
		});

		actions.add(new Action(Messages.VWindowSharedElementsEditor_AddPartStack, createImageDescriptor(ResourceProvider.IMG_PartStack)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_STACK);
			}
		});

		actions.add(new Action(Messages.VWindowSharedElementsEditor_Area, createImageDescriptor(ResourceProvider.IMG_Area)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.AREA);
			}
		});

		// -- IMPORT ACTIONS --
		actionsImport.add(new Action(Messages.VWindowSharedElementsEditor_Views, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_VIEW);
			}
		});

		actionsImport.add(new Action(Messages.VWindowSharedElementsEditor_Editors, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_EDITOR);
			}
		});

		actionsImport.add(new Action(Messages.VWindowSharedElementsEditor_ViewAsCompatibilityView, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_COMPAT_VIEW);
			}
		});
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VWindowSharedElementsEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VWindowSharedElementsEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		@SuppressWarnings("unchecked")
		final VirtualEntry<MWindow, ?> o = (VirtualEntry<MWindow, ?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue<MWindow> master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		{

			final AbstractPickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_GROUP),
					this, BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS) {
				@Override
				protected void addPressed() {
					final EClass eClass = (EClass) getSelection().getFirstElement();
					handleAdd(eClass);
				}

				@Override
				protected List<?> getContainerChildren(Object container) {
					return ((MWindow) container).getSharedElements();
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			viewer = pickList.getList();
			viewer.setInput(E4Properties.sharedElements(getEditingDomain()).observeDetail(getMaster()));

			pickList.setLabelProvider(new EClassLabelProvider(getEditor()));
			pickList.setInput(
					new EClass[] { BasicPackageImpl.Literals.PART_SASH_CONTAINER, BasicPackageImpl.Literals.PART,
							BasicPackageImpl.Literals.PART_STACK, AdvancedPackageImpl.Literals.AREA });
			pickList.setSelection(new StructuredSelection(BasicPackageImpl.Literals.PART));
		}

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	protected void handleAdd(EClass eClass) {
		final EObject eObject = EcoreUtil.create(eClass);
		addToModel(eObject);
	}

	private void addToModel(EObject eObject) {
		setElementId(eObject);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	protected void handleImport(EClass eClass, String hint) {
		if (eClass == BasicPackageImpl.Literals.PART) {
			final ModelImportWizard wizard = new ModelImportWizard(MPart.class, this, hint, resourcePool);
			final WizardDialog wizardDialog = new WizardDialog(viewer.getControl().getShell(), wizard);
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
