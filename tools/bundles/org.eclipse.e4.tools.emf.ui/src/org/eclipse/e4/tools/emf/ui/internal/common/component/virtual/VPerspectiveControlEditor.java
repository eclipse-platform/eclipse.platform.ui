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
import org.eclipse.e4.tools.emf.ui.internal.common.EClassLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class VPerspectiveControlEditor extends AbstractComponentEditor<MElementContainer<MUIElement>> {
	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private final List<Action> actions = new ArrayList<>();

	@Inject
	public VPerspectiveControlEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.VWindowControlEditor_AddPartSashContainer, createImageDescriptor(ResourceProvider.IMG_PartSashContainer_vertical)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_SASH_CONTAINER);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPartStack, createImageDescriptor(ResourceProvider.IMG_PartStack)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART_STACK);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddPart, createImageDescriptor(ResourceProvider.IMG_Part)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.PART);
			}
		});

		actions.add(new Action(Messages.VWindowControlEditor_AddArea, createImageDescriptor(ResourceProvider.IMG_Area_vertical)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.AREA);
			}
		});
		actions.add(new Action(Messages.PerspectiveEditor_AddPlaceholder, createImageDescriptor(ResourceProvider.IMG_Placeholder)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.PLACEHOLDER);
			}
		});
	}


	@Override
	public String getLabel(Object element) {
		return Messages.VWindowControlEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VWindowControlEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		@SuppressWarnings("unchecked")
		final VirtualEntry<MElementContainer<MUIElement>, ?> o = (VirtualEntry<MElementContainer<MUIElement>, ?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MElementContainer<MUIElement>> master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		{
			final AbstractPickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_GROUP),
					this, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
				@Override
				protected void addPressed() {
					final EClass eClass = (EClass) getSelection().getFirstElement();
					handleAdd(eClass);
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			viewer = pickList.getList();

			pickList.setContentProvider(ArrayContentProvider.getInstance());
			pickList.setLabelProvider(new EClassLabelProvider(getEditor()));
			pickList.setInput(new EClass[] { BasicPackageImpl.Literals.PART_SASH_CONTAINER,
					BasicPackageImpl.Literals.PART_STACK, BasicPackageImpl.Literals.PART,
					AdvancedPackageImpl.Literals.AREA, AdvancedPackageImpl.Literals.PLACEHOLDER });
			pickList.setSelection(new StructuredSelection(BasicPackageImpl.Literals.PART_SASH_CONTAINER));
		}

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	private void handleAdd(EClass eClass) {
		final EObject eObject = EcoreUtil.create(eClass);

		setElementId(eObject);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}