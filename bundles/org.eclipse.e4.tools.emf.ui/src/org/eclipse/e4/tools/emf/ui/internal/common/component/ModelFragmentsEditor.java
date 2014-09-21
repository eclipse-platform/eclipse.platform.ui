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
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
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
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
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

public class ModelFragmentsEditor extends AbstractComponentEditor {

	private IListProperty MODEL_FRAGMENTS__FRAGMENTS = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS);
	private IListProperty MODEL_FRAGMENTS__IMPORTS = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS);

	private Composite composite;

	@Inject
	IEclipseContext context;

	@Inject
	public ModelFragmentsEditor() {
		super();
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_ModelFragments);
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
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
		createFragmentsTab(folder);
		createImportsTab(folder);
		folder.setSelection(0);
		return folder;
	}

	private void createFragmentsTab(CTabFolder folder) {

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelFragmentsEditor_ModelFragments);

		Composite parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		{

			AbstractPickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_PICKER), Messages, this, FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS) {
				@Override
				protected void addPressed() {
					MModelFragment component = MFragmentFactory.INSTANCE.createStringModelFragment();

					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS, component);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						getEditor().setSelection(component);
					}
				}

				@Override
				protected List<?> getContainerChildren(Object container) {
					if (container instanceof MModelFragments) {
						return ((MModelFragments) container).getFragments();
					} else {
						return null;
					}
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			TableViewer viewer = pickList.getList();

			IEMFListProperty prop = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS);
			viewer.setInput(prop.observeDetail(getMaster()));
		}

	}

	private void createImportsTab(CTabFolder folder) {

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelFragmentsEditor_Imports);

		Composite parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		E4PickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this, FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
			@Override
			protected void addPressed() {
				EClass eClass = ((FeatureClass) ((IStructuredSelection) getPicker().getSelection()).getFirstElement()).eClass;
				EObject eObject = EcoreUtil.create(eClass);

				Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, eObject);

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

		TableViewer viewer = pickList.getList();
		ComboViewer picker = pickList.getPicker();

		picker.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				FeatureClass eclass = (FeatureClass) element;
				return eclass.label;
			}
		});

		picker.setComparator(new ViewerComparator() {
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

		picker.setInput(list);
		picker.getCombo().select(0);

		IEMFListProperty prop = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS);
		viewer.setInput(prop.observeDetail(getMaster()));
	}

	public void addClasses(EPackage ePackage, List<FeatureClass> list) {
		for (EClassifier c : ePackage.getEClassifiers()) {
			if (c instanceof EClass) {
				EClass eclass = (EClass) c;
				if (eclass != ApplicationPackageImpl.Literals.APPLICATION && !eclass.isAbstract() && !eclass.isInterface() && eclass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
					list.add(new FeatureClass(eclass.getName(), eclass));
				}
			}
		}

		for (EPackage eSubPackage : ePackage.getESubpackages()) {
			addClasses(eSubPackage, list);
		}
	}

	@Override
	public IObservableList getChildList(Object element) {
		WritableList list = new WritableList();
		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_MODEL_IMPORTS, MODEL_FRAGMENTS__IMPORTS, element, Messages.ModelFragmentsEditor_Imports) {
			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});
		list.add(new VirtualEntry<Object>(ModelEditor.VIRTUAL_MODEL_FRAGEMENTS, MODEL_FRAGMENTS__FRAGMENTS, element, Messages.ModelFragmentsEditor_ModelFragments) {
			@Override
			protected boolean accepted(Object o) {
				return true;
			}
		});

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
