/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Ongoing maintenance, Bug 443945
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
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class VModelImportsEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	@Inject
	IEclipseContext eclipseContext;
	private TableViewer viewer;

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
		viewer.setInput(o.getList());
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

		E4PickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this, FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
			@Override
			protected void addPressed() {

				Object firstElement = ((IStructuredSelection) getSelection()).getFirstElement();

				if (firstElement != null) {
					FeatureClass featureClass = (FeatureClass) firstElement;
					EClass eClass = featureClass.eClass;
					EObject eObject = EcoreUtil.create(eClass);

					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, eObject);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						getEditor().setSelection(eObject);
					}
				}
			}

			@Override
			protected List<?> getContainerChildren(Object master) {
				return ((ModelFragmentsImpl) master).getImports();
			}
		};
		viewer = pickList.getList();

		pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		pickList.setText(""); //$NON-NLS-1$

		pickList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				FeatureClass eclass = (FeatureClass) element;
				return eclass.label;
			}
		});

		pickList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				FeatureClass eClass1 = (FeatureClass) e1;
				FeatureClass eClass2 = (FeatureClass) e2;
				return eClass1.label.compareTo(eClass2.label);
			}
		});

		List<FeatureClass> list = new ArrayList<FeatureClass>();
		Util.addClasses(ApplicationPackageImpl.eINSTANCE, list);
		list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT, FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS));

		pickList.setInput(list);
		if (list.size() > 0) {
			pickList.setSelection(new StructuredSelection(list.get(0)));
		}

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}