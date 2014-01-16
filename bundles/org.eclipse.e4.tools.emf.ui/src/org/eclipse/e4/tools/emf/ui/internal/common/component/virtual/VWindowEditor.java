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
import javax.annotation.PostConstruct;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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

public abstract class VWindowEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private List<Action> actions = new ArrayList<Action>();
	private EStructuralFeature targetFeature;

	public VWindowEditor(EStructuralFeature targetFeature) {
		super();
		this.targetFeature = targetFeature;
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.VWindowEditor_AddDialog, createImageDescriptor(ResourceProvider.IMG_Dialog)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.DIALOG);
			}
		});
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
		actions.add(new Action(Messages.VWindowEditor_AddWizardDialog, createImageDescriptor(ResourceProvider.IMG_WizardDialog)) {
			@Override
			public void run() {
				handleAdd(BasicPackageImpl.Literals.WIZARD_DIALOG);
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
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

		Composite buttonCompTop = new Composite(parent, SWT.NONE);
		buttonCompTop.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false, 3, 1));
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonCompTop.setLayout(gl);

		final ComboViewer childrenDropDown = new ComboViewer(buttonCompTop);
		childrenDropDown.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		childrenDropDown.setContentProvider(new ArrayContentProvider());
		childrenDropDown.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				EClass eclass = (EClass) element;
				return eclass.getName();
			}
		});
		childrenDropDown.setInput(new EClass[] { BasicPackageImpl.Literals.DIALOG, BasicPackageImpl.Literals.TRIMMED_WINDOW, BasicPackageImpl.Literals.WINDOW, BasicPackageImpl.Literals.WIZARD_DIALOG });
		childrenDropDown.setSelection(new StructuredSelection(BasicPackageImpl.Literals.TRIMMED_WINDOW));

		Button b = new Button(buttonCompTop, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_AddEllipsis);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EClass eClass = (EClass) ((IStructuredSelection) childrenDropDown.getSelection()).getFirstElement();
				handleAdd(eClass);
			}
		});

		viewer = new TableViewer(parent);
		ObservableListContentProvider cp = new ObservableListContentProvider();
		viewer.setContentProvider(cp);
		viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1);
		viewer.getControl().setLayoutData(gd);

		Composite buttonCompBot = new Composite(parent, SWT.NONE);
		buttonCompBot.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false, 3, 1));
		GridLayout gl1 = new GridLayout(3, false);
		gl1.marginLeft = 0;
		gl1.marginRight = 0;
		gl1.marginWidth = 0;
		gl1.marginHeight = 0;
		buttonCompBot.setLayout(gl1);

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);

		b.setText(Messages.ModelTooling_Common_Up);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_up));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						EObject container = (EObject) getMaster().getValue();
						List<Object> l = (List<Object>) container.eGet(targetFeature);
						int idx = l.indexOf(obj) - 1;
						if (idx >= 0) {
							if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(), idx, targetFeature)) {
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Down);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_arrow_down));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						EObject container = (EObject) getMaster().getValue();
						List<Object> l = (List<Object>) container.eGet(targetFeature);
						int idx = l.indexOf(obj) + 1;
						if (idx < l.size()) {
							if (Util.moveElementByIndex(getEditingDomain(), (MUIElement) obj, getEditor().isLiveModel(), idx, targetFeature)) {
								viewer.setSelection(new StructuredSelection(obj));
							}
						}
					}
				}
			}
		});

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Remove);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_delete));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					List<?> windows = ((IStructuredSelection) viewer.getSelection()).toList();
					MElementContainer<?> container = (MElementContainer<?>) getMaster().getValue();
					Command cmd = RemoveCommand.create(getEditingDomain(), container, targetFeature, windows);
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						if (container.getChildren().size() > 0) {
							viewer.setSelection(new StructuredSelection(container.getChildren().get(0)));
						}
					}
				}
			}
		});

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}

	protected void handleAdd(EClass eClass) {
		EObject handler = EcoreUtil.create(eClass);
		setElementId(handler);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), targetFeature, handler);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(handler);
		}
	}
}