/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Wim Jongman <wim.jongman@remainsoftware.com> - bug 400804
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.ViewerElement;
import org.eclipse.e4.tools.emf.ui.internal.imp.ModelImportWizard;
import org.eclipse.e4.tools.emf.ui.internal.imp.RegistryUtil;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class VWindowSharedElementsEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private StructuredViewer viewer;
	private List<Action> actions = new ArrayList<Action>();
	private List<Action> actionsImport = new ArrayList<Action>();

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

		actions.add(new Action(Messages.VWindowSharedElementsEditor_AddInputPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.INPUT_PART);
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
		actionsImport.add(new Action("Views", createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_VIEW);
			}
		});

		actionsImport.add(new Action("Editors", createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.INPUT_PART, RegistryUtil.HINT_EDITOR);
			}
		});

		actionsImport.add(new Action("View as CompatibilityView", createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleImport(BasicPackageImpl.Literals.PART, RegistryUtil.HINT_COMPAT_VIEW);
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
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
		VirtualEntry<?> o = (VirtualEntry<?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		{

			final ViewerElement tableElement = ViewerElement.create(eclipseContext, parent, this);
			viewer = tableElement.getViewer();
			viewer.setContentProvider(new ObservableListContentProvider());
			viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));

			IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS);
			viewer.setInput(prop.observeDetail(getMaster()));

			tableElement.getButtonUp().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							EObject container = (EObject) getMaster().getValue();
							int idx = ((List<?>) container.eGet(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS)).indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}
						}
					}
				}
			});

			tableElement.getButtonDown().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							EObject container = (EObject) getMaster().getValue();
							List<?> list = (List<?>) container.eGet(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS);
							int idx = list.indexOf(obj) + 1;
							if (idx < list.size()) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}
						}
					}
				}
			});

			tableElement.getDropDown().setContentProvider(new ArrayContentProvider());
			tableElement.getDropDown().setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					EClass eclass = (EClass) element;
					return eclass.getName();
				}
			});
			tableElement.getDropDown().setInput(new EClass[] { BasicPackageImpl.Literals.PART_SASH_CONTAINER, BasicPackageImpl.Literals.PART, BasicPackageImpl.Literals.INPUT_PART, BasicPackageImpl.Literals.PART_STACK, AdvancedPackageImpl.Literals.AREA });
			tableElement.getDropDown().setSelection(new StructuredSelection(BasicPackageImpl.Literals.PART));

			tableElement.getButtonAdd().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!tableElement.getDropDown().getSelection().isEmpty()) {
						EClass eClass = (EClass) ((IStructuredSelection) tableElement.getDropDown().getSelection()).getFirstElement();
						handleAdd(eClass);
					}
				}
			});

			tableElement.getButtonRemove().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						List<?> elements = ((IStructuredSelection) viewer.getSelection()).toList();

						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS, elements);
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	protected void handleAdd(EClass eClass) {
		EObject eObject = EcoreUtil.create(eClass);
		addToModel(eObject);
	}

	private void addToModel(EObject eObject) {
		setElementId(eObject);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	protected void handleImport(EClass eClass, String hint) {
		if (eClass == BasicPackageImpl.Literals.PART) {
			ModelImportWizard wizard = new ModelImportWizard(MPart.class, this, hint, resourcePool);
			WizardDialog wizardDialog = new WizardDialog(viewer.getControl().getShell(), wizard);
			if (wizardDialog.open() == Window.OK) {
				MPart[] parts = (MPart[]) wizard.getElements(MPart.class);
				for (MPart part : parts) {
					addToModel((EObject) part);
				}
			}
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	@Override
	public List<Action> getActionsImport(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActionsImport(element));
		l.addAll(actionsImport);
		return l;
	}
}
