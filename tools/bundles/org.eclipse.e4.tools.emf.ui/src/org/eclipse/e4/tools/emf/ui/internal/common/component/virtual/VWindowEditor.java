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
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import jakarta.annotation.PostConstruct;

public abstract class VWindowEditor<M> extends AbstractComponentEditor<M> {
	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private final List<Action> actions = new ArrayList<>();
	private final EStructuralFeature targetFeature;

	public VWindowEditor(EStructuralFeature targetFeature) {
		super();
		this.targetFeature = targetFeature;
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.VWindowEditor_AddTrimmedWindow, createImageDescriptor(ResourceProvider.IMG_Window)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.TRIMMED_WINDOW);
			}
		});
		actions.add(new Action(Messages.VWindowEditor_AddWindow, createImageDescriptor(ResourceProvider.IMG_Window)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.WINDOW);
			}
		});
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VWindowEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VWindowEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		@SuppressWarnings("unchecked")
		final VirtualEntry<M, ?> o = (VirtualEntry<M, ?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue<M> master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final AbstractPickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_GROUP),
				this, targetFeature) {
			@Override
			protected void addPressed() {
				final EClass eClass = (EClass) getSelection().getFirstElement();
				handleAdd(eClass);
			}

			@Override
			protected List<?> getContainerChildren(Object container) {
				return ((MApplication) container).getChildren();
			}
		};
		pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		viewer = pickList.getList();

		pickList.setLabelProvider(new EClassLabelProvider(getEditor()));
		pickList.setInput(new EClass[] { BasicPackageImpl.Literals.TRIMMED_WINDOW, BasicPackageImpl.Literals.WINDOW });
		pickList.setSelection(new StructuredSelection(BasicPackageImpl.Literals.TRIMMED_WINDOW));

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	protected void handleAdd(EClass eClass) {
		final EObject handler = EcoreUtil.create(eClass);
		setElementId(handler);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), targetFeature, handler);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(handler);
		}
	}
}