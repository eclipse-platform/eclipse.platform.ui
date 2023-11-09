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
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance, Bug 443945
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import jakarta.inject.Inject;

public class ModelFragmentsEditor extends AbstractComponentEditor<MModelFragments> {
	private Composite composite;

	@Inject
	IEclipseContext context;

	@Inject
	public ModelFragmentsEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_ModelFragments);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.ModelFragmentsEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.ModelFragmentsEditor_Description;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			composite = createForm(parent);
		}
		getMaster().setValue((MModelFragments) object);
		return composite;
	}

	private Composite createForm(Composite parent) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
		createFragmentsTab(folder);
		createImportsTab(folder);
		folder.setSelection(0);
		return folder;
	}

	private void createFragmentsTab(CTabFolder folder) {

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelFragmentsEditor_ModelFragments);

		final Composite parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		{

			final AbstractPickList pickList = new E4PickList(parent, SWT.NONE,
					Arrays.asList(PickListFeatures.NO_PICKER, PickListFeatures.NO_GROUP), this,
					FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS) {
				@Override
				protected void addPressed() {
					final MModelFragment component = MFragmentFactory.INSTANCE.createStringModelFragment();

					final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
							FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS, component);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						getEditor().setSelection(component);
					}
				}

				@Override
				protected List<?> getContainerChildren(Object container) {
					if (container instanceof MModelFragments) {
						return ((MModelFragments) container).getFragments();
					}
					return null;
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			final TableViewer viewer = pickList.getList();
			viewer.setInput(E4Properties.fragments().observeDetail(getMaster()));
		}

	}

	private void createImportsTab(CTabFolder folder) {

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelFragmentsEditor_Imports);

		final Composite parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		final E4PickList pickList = new E4PickList(parent, SWT.NONE, null, this,
				FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
			@Override
			protected void addPressed() {
				final EClass eClass = ((FeatureClass) getSelection().getFirstElement()).eClass;
				final EObject eObject = EcoreUtil.create(eClass);

				final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
						FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, eObject);

				if (cmd.canExecute()) {
					getEditingDomain().getCommandStack().execute(cmd);
					getEditor().setSelection(eObject);
				}
			}

			@Override
			protected List<?> getContainerChildren(Object master) {
				// TODO What object is master? We need to cast.
				return super.getContainerChildren(master);
			}
		};

		pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		pickList.setText(Messages.PartSashContainerEditor_Controls);

		final TableViewer viewer = pickList.getList();

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
		list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT,
				FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS));

		pickList.setInput(list);
		if (list.size() > 0) {
			pickList.setSelection(new StructuredSelection(list.get(0)));
		}

		viewer.setInput(E4Properties.imports().observeDetail(getMaster()));
	}

	public void addClasses(EPackage ePackage, List<FeatureClass> list) {
		for (final EClassifier c : ePackage.getEClassifiers()) {
			if (c instanceof EClass) {
				final EClass eclass = (EClass) c;
				if (eclass != ApplicationPackageImpl.Literals.APPLICATION && !eclass.isAbstract()
						&& !eclass.isInterface()
						&& eclass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
					list.add(new FeatureClass(eclass.getName(), eclass));
				}
			}
		}

		for (final EPackage eSubPackage : ePackage.getESubpackages()) {
			addClasses(eSubPackage, list);
		}
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		MModelFragments fragments = (MModelFragments) element;

		final WritableList<VirtualEntry<MModelFragments, ?>> list = new WritableList<>();
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_MODEL_IMPORTS, E4Properties.imports(), fragments,
				Messages.ModelFragmentsEditor_Imports));
		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_MODEL_FRAGEMENTS, E4Properties.fragments(), fragments,
				Messages.ModelFragmentsEditor_ModelFragments));

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
