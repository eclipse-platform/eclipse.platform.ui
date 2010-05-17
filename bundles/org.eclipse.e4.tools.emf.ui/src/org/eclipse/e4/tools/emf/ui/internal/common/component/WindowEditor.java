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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ToolItemIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.WindowIconDialogEditor;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WindowEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;
	private IProject project;

	private IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties.list(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
	private IListProperty WINDOW__WINDOWS = EMFProperties.list(BasicPackageImpl.Literals.WINDOW__WINDOWS);
	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	private IValueProperty WINDOW__MAIN_MENU = EMFProperties.value(BasicPackageImpl.Literals.WINDOW__MAIN_MENU);

	private Action addMainMenu; 
	
	public WindowEditor(EditingDomain editingDomain, IProject project) {
		super(editingDomain);
		this.project = project;
		addMainMenu = new Action("Add Main Menu") {
			@Override
			public void run() {
				MMenu menu = MMenuFactory.INSTANCE.createMenu();
				Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(), BasicPackageImpl.Literals.WINDOW__MAIN_MENU, menu);
				if( cmd.canExecute() ) {
					getEditingDomain().getCommandStack().execute(cmd);
				}
			}
		};
	}

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/Window.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Window";
	}

	@Override
	public String getDescription(Object element) {
		return "Window bla bla bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if( composite == null ) {
			context = new EMFDataBindingContext();
			composite = createForm(parent,context, getMaster());
		}
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue master) {
		parent = new Composite(parent,SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Id");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("X");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.WINDOW__X).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Y");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.WINDOW__Y).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Width");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.WINDOW__WIDTH).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Height");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.WINDOW__HEIGHT).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Label");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Tooltip");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__TOOLTIP).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Icon URI");

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__ICON_URI).observeDetail(master));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText("Find ...");
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					WindowIconDialogEditor dialog = new WindowIconDialogEditor(b.getShell(), project,getEditingDomain(),(MWindow) getMaster().getValue());
					dialog.open();
				}
			});

		}

		ControlFactory.createBindingsWidget(parent, this);
		ControlFactory.createVariablesWidget(parent, this);
		ControlFactory.createTagsWidget(parent, this);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		final WritableList list = new WritableList();
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_HANDLER, HANDLER_CONTAINER__HANDLERS, element, "Handlers") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});
		
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_WINDOWS, WINDOW__WINDOWS, element, "Windows") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_WINDOW_CONTROLS, ELEMENT_CONTAINER__CHILDREN, element, "Controls") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});
		
		MWindow window = (MWindow) element;
		if( window.getMainMenu() != null ) {
			list.add(0,window.getMainMenu());
		}
		
		WINDOW__MAIN_MENU.observe(element).addValueChangeListener(new IValueChangeListener() {
			
			public void handleValueChange(ValueChangeEvent event) {
				if( event.diff.getOldValue() != null ) {
					list.remove(event.diff.getOldValue());
				}
				
				if( event.diff.getNewValue() != null ) {
					list.add(0,event.diff.getNewValue());
				}
			}
		});

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		MWindow window = (MWindow)element;
		if( window.getLabel() != null && window.getLabel().trim().length() > 0 ) {
			return window.getLabel();
		}
		return null;
	}
	
	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
			FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL)	
		};
	}
	
	@Override
	public List<Action> getActions(Object element) {
		List<Action> actions = new ArrayList<Action>();
		
		MWindow window = (MWindow) element;
		if( window.getMainMenu() == null ) {
			actions.add(addMainMenu);
		}
		
		return actions;
	}
}