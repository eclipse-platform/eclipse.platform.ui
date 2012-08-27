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
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
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
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class ModelFragmentsEditor extends AbstractComponentEditor {

	private IListProperty MODEL_FRAGMENTS__FRAGMENTS = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS);
	private IListProperty MODEL_FRAGMENTS__IMPORTS = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS);

	private Composite composite;

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

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		{
			GridLayout layout = (GridLayout) parent.getLayout();
			layout.numColumns = 2;

			Composite impCompo = new Composite(parent, SWT.NONE);
			impCompo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			impCompo.setLayout(new GridLayout());
			Label l = new Label(impCompo, SWT.NONE);
			l.setText(Messages.ModelFragmentsEditor_Imports);
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
			final TableViewer viewer = new TableViewer(impCompo);
			viewer.setContentProvider(new ObservableListContentProvider());
			viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 200;
			viewer.getControl().setLayoutData(gd);

			IEMFListProperty prop = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS);
			viewer.setInput(prop.observeDetail(getMaster()));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout(2, false);
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Up);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_up));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MModelFragments container = (MModelFragments) getMaster().getValue();
							int idx = container.getImports().indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Down);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_down));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MModelFragments container = (MModelFragments) getMaster().getValue();
							int idx = container.getImports().indexOf(obj) + 1;
							if (idx < container.getImports().size()) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			final ComboViewer childrenDropDown = new ComboViewer(buttonComp);
			childrenDropDown.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			childrenDropDown.setContentProvider(new ArrayContentProvider());
			childrenDropDown.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					FeatureClass eclass = (FeatureClass) element;
					return eclass.label;
				}
			});
			childrenDropDown.setComparator(new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2) {
					FeatureClass eClass1 = (FeatureClass) e1;
					FeatureClass eClass2 = (FeatureClass) e2;
					return eClass1.label.compareTo(eClass2.label);
				}
			});

			List<FeatureClass> list = new ArrayList<FeatureClass>();
			addClasses(ApplicationPackageImpl.eINSTANCE, list);
			list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT, FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS));

			childrenDropDown.setInput(list);
			childrenDropDown.getCombo().select(0);

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
			b.setText(Messages.ModelTooling_Common_AddEllipsis);
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					EClass eClass = ((FeatureClass) ((IStructuredSelection) childrenDropDown.getSelection()).getFirstElement()).eClass;
					EObject eObject = EcoreUtil.create(eClass);

					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, eObject);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						getEditor().setSelection(eObject);
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Remove);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_table_delete));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						List<?> elements = ((IStructuredSelection) viewer.getSelection()).toList();

						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS, elements);
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}

		{

			Composite fragCompo = new Composite(parent, SWT.NONE);
			fragCompo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fragCompo.setLayout(new GridLayout());
			Label l = new Label(fragCompo, SWT.NONE);
			l.setText(Messages.ModelFragmentsEditor_ModelFragments);
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			final TableViewer viewer = new TableViewer(fragCompo);
			viewer.setContentProvider(new ObservableListContentProvider());
			viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 200;
			viewer.getControl().setLayoutData(gd);

			IEMFListProperty prop = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS);
			viewer.setInput(prop.observeDetail(getMaster()));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout();
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Up);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_up));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MModelFragments container = (MModelFragments) getMaster().getValue();
							int idx = container.getFragments().indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Down);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_down));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MModelFragments container = (MModelFragments) getMaster().getValue();
							int idx = container.getFragments().indexOf(obj) + 1;
							if (idx < container.getFragments().size()) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_AddEllipsis);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MModelFragment component = MFragmentFactory.INSTANCE.createStringModelFragment();

					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS, component);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						getEditor().setSelection(component);
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Remove);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_table_delete));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						List<?> elements = ((IStructuredSelection) viewer.getSelection()).toList();

						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS, elements);
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
