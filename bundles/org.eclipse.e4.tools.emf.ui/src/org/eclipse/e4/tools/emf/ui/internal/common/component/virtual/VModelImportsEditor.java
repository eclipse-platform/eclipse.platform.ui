/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.ViewerElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class VModelImportsEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private ViewerElement tableElement;

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public VModelImportsEditor() {
		super();
	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VModelImportsEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VModelImportsEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		VirtualEntry<?> o = (VirtualEntry<?>) object;
		tableElement.getViewer().setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
		composite = folder;

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		tableElement = ViewerElement.create(eclipseContext, parent, this);
		tableElement.getViewer().setContentProvider(new ObservableListContentProvider());
		tableElement.getViewer().setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));

		tableElement.getDropDown().setContentProvider(new ArrayContentProvider());
		tableElement.getDropDown().setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				FeatureClass eclass = (FeatureClass) element;
				return eclass.label;
			}
		});
		tableElement.getDropDown().setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				FeatureClass eClass1 = (FeatureClass) e1;
				FeatureClass eClass2 = (FeatureClass) e2;
				return eClass1.label.compareTo(eClass2.label);
			}
		});

		tableElement.getButtonAdd().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EClass eClass = ((FeatureClass) ((IStructuredSelection) tableElement.getDropDown().getSelection()).getFirstElement()).eClass;
				EObject eObject = EcoreUtil.create(eClass);

				Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, eObject);

				if (cmd.canExecute()) {
					getEditingDomain().getCommandStack().execute(cmd);
					getEditor().setSelection(eObject);
				}
			}
		});

		tableElement.getButtonRemove().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!tableElement.getViewer().getSelection().isEmpty()) {
					List<?> elements = ((IStructuredSelection) tableElement.getViewer().getSelection()).toList();

					Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, elements);
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			}
		});

		tableElement.getButtonDown().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!tableElement.getViewer().getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) tableElement.getViewer().getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						MModelFragments container = (MModelFragments) getMaster().getValue();
						int idx = container.getImports().indexOf(obj) + 1;
						if (idx < container.getImports().size()) {
							Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, obj, idx);

							if (cmd.canExecute()) {
								getEditingDomain().getCommandStack().execute(cmd);
								tableElement.getViewer().setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		tableElement.getButtonUp().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!tableElement.getViewer().getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) tableElement.getViewer().getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						MModelFragments container = (MModelFragments) getMaster().getValue();
						int idx = container.getImports().indexOf(obj) - 1;
						if (idx >= 0) {
							Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, obj, idx);

							if (cmd.canExecute()) {
								getEditingDomain().getCommandStack().execute(cmd);
								tableElement.getViewer().setSelection(new StructuredSelection(obj));
							}
						}
					}
				}
			}
		});

		List<FeatureClass> list = new ArrayList<FeatureClass>();
		Util.addClasses(ApplicationPackageImpl.eINSTANCE, list);
		list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT, FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS));

		tableElement.getDropDown().setInput(list);
		tableElement.getDropDown().getCombo().select(0);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}