/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
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
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class VWindowControlEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private List<Action> actions = new ArrayList<Action>();
	private TableViewer viewer;

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public VWindowControlEditor() {
		super();
	}

	@PostConstruct
	void init() {

		actions.add(new Action(Messages.VWindowControlEditor_AddPerspectiveStack, createImageDescriptor(ResourceProvider.IMG_PerspectiveStack)) {
			@Override
			public void run() {
				handleAdd(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK);
			}
		});

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

		Collections.sort(actions, new Comparator<Action>() {
			@Override
			public int compare(Action o1, Action o2) {
				return o1.getText().compareTo(o2.getText());
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
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
			AbstractPickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
				@Override
				protected void addPressed() {
					EClass eClass = (EClass) ((IStructuredSelection) getPicker().getSelection()).getFirstElement();
					handleAdd(eClass);
				};
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			viewer = pickList.getList();

			ComboViewer picker = pickList.getPicker();

			picker.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					EClass eclass = (EClass) element;
					return eclass.getName();
				}
			});

			picker.setInput(new EClass[] { AdvancedPackageImpl.Literals.PERSPECTIVE_STACK, BasicPackageImpl.Literals.PART_SASH_CONTAINER, BasicPackageImpl.Literals.PART_STACK, BasicPackageImpl.Literals.PART, BasicPackageImpl.Literals.INPUT_PART, AdvancedPackageImpl.Literals.AREA });
			picker.setSelection(new StructuredSelection(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK));
		}

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	private void handleAdd(EClass eClass) {
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