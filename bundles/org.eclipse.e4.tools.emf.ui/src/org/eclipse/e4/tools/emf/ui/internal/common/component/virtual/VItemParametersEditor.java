/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class VItemParametersEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private TableViewer viewer;
	private final List<Action> actions = new ArrayList<Action>();

	private final IEMFListProperty HANDLED_ITEM__PARAMETERS = EMFProperties
		.list(MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);

	@Inject
	public VItemParametersEditor() {
		super();
	}

	@PostConstruct
	void init() {
		actions.add(new Action(Messages.VItemParametersEditor_AddParameter,
			createImageDescriptor(ResourceProvider.IMG_Parameter)) {
			@Override
			public void run() {
				handleAdd();
			}
		});
	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VItemParametersEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VItemParametersEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		final VirtualEntry<?> o = (VirtualEntry<?>) object;
		viewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		{
			final AbstractPickList pickList = new E4PickList(parent, SWT.NONE,
				Arrays.asList(PickListFeatures.NO_PICKER), Messages, this,
				MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS) {
				@Override
				protected void addPressed() {
					handleAdd();
				}

				@Override
				protected List<?> getContainerChildren(Object container) {
					if (container instanceof MHandledItem) {
						return ((MHandledItem) container).getParameters();
					}
					return null;
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			viewer = pickList.getList();
		}

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return HANDLED_ITEM__PARAMETERS.observe(element);
	}

	protected void handleAdd() {
		final MParameter handler = MCommandsFactory.INSTANCE.createParameter();
		setElementId(handler);

		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
			MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS, handler);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(handler);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		return l;
	}
}
