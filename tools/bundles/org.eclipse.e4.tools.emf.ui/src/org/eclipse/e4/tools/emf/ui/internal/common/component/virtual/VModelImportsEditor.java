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
 *     Steven Spungin <steven@spungin.tv> - Ongoing maintenance, Bug 443945
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.ModelFragmentsImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import jakarta.inject.Inject;

public class VModelImportsEditor extends AbstractComponentEditor<MModelFragments> {
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
		@SuppressWarnings("unchecked")
		final VirtualEntry<MModelFragments, ?> o = (VirtualEntry<MModelFragments, ?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue<MModelFragments> master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
		composite = folder;

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		final E4PickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_GROUP), this,
				FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
			@Override
			protected void addPressed() {

				final Object firstElement = getSelection().getFirstElement();

				if (firstElement != null) {
					final FeatureClass featureClass = (FeatureClass) firstElement;
					final EClass eClass = featureClass.eClass;
					final EObject eObject = EcoreUtil.create(eClass);

					final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, eObject);

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

		pickList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				final FeatureClass eclass = (FeatureClass) element;
				return eclass.label;
			}
		});

		pickList.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				final FeatureClass eClass1 = (FeatureClass) e1;
				final FeatureClass eClass2 = (FeatureClass) e2;
				return eClass1.label.compareTo(eClass2.label);
			}
		});

		final List<FeatureClass> list = new ArrayList<>();
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
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

}