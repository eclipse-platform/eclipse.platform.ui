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
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
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

public class VMenuEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private List<Action> actions = new ArrayList<Action>();
	private EStructuralFeature feature;

	public static final String VIEW_MENU_TAG = "ViewMenu"; //$NON-NLS-1$

	enum Types {
		MENU, POPUP_MENU, VIEW_MENU
	}

	protected VMenuEditor(EStructuralFeature feature) {
		super();
		this.feature = feature;
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.VMenuEditor_AddMenuContribution, createImageDescriptor(ResourceProvider.IMG_Menu)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.MENU);
			}
		});
		actions.add(new Action(Messages.VMenuEditor_AddPopupMenuContribution, createImageDescriptor(ResourceProvider.IMG_Menu)) {
			@Override
			public void run() {
				handleAdd(MenuPackageImpl.Literals.POPUP_MENU);
			}
		});
		actions.add(new Action(Messages.MenuEditor_Label_ViewMenu, createImageDescriptor(ResourceProvider.IMG_Menu)) {
			@Override
			public void run() {
				handleAddViewMenu();
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VMenuEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VMenuEditor_TreeLabelDescription;
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

		viewer = new TableViewer(parent);
		ObservableListContentProvider cp = new ObservableListContentProvider();
		viewer.setContentProvider(cp);
		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);
		viewer.setLabelProvider(new ComponentLabelProvider(getEditor(), Messages));

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
						EObject container = (EObject) getMaster().getValue();
						int idx = ((List<?>) container.eGet(feature)).indexOf(obj) - 1;
						if (idx >= 0) {
							Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), feature, obj, idx);

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
						EObject container = (EObject) getMaster().getValue();
						List<?> list = (List<?>) container.eGet(feature);
						int idx = list.indexOf(obj) + 1;
						if (idx < list.size()) {
							Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS, obj, idx);

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
				if (element == Types.MENU) {
					return Messages.MenuEditor_Label;
				} else if (element == Types.POPUP_MENU) {
					return Messages.PopupMenuEditor_TreeLabel;
				}
				return Messages.MenuEditor_Label_ViewMenu;
			}
		});
		childrenDropDown.setInput(Types.values());
		childrenDropDown.setSelection(new StructuredSelection(Types.MENU));

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_table_add));
		b.setText(Messages.ModelTooling_Common_AddEllipsis);
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Types t = (Types) ((IStructuredSelection) childrenDropDown.getSelection()).getFirstElement();
				if (t == Types.MENU) {
					handleAdd(MenuPackageImpl.Literals.MENU);
				} else if (t == Types.POPUP_MENU) {
					handleAdd(MenuPackageImpl.Literals.POPUP_MENU);
				} else {
					handleAddViewMenu();
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
					List<?> windows = ((IStructuredSelection) viewer.getSelection()).toList();
					Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.PART__MENUS, windows);
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
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

	protected void handleAdd(EClass eClass) {
		EObject handler = EcoreUtil.create(eClass);
		setElementId(handler);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), feature, handler);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(handler);
		}
	}

	protected void handleAddViewMenu() {
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		menu.getTags().add(VIEW_MENU_TAG);
		setElementId(menu);

		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), feature, menu);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(menu);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
