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
import java.util.Arrays;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

public class ToolItemEditor extends AbstractComponentEditor {
	private Composite composite;
	private WritableValue master = new WritableValue();
	private Image image;
	private DataBindingContext context;

	public ToolItemEditor(EditingDomain editingDomain) {
		super(editingDomain);
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/ToolItem_separator.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Separator";
	}

	@Override
	public String getDescription(Object element) {
		return "Separator bla bla bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new DataBindingContext();
			composite = createForm(parent, context, master);
		}
		master.setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, DataBindingContext context, WritableValue master) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IValueProperty textProp = WidgetProperties.text();

		Label l = new Label(parent, SWT.NONE);
		l.setText("Id");

		Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(master));

		if (this.getClass() != ToolItemEditor.class) {
			createFormSubTypeForm(parent, context, master);
		}

		return parent;
	}

	private void createFormSubTypeForm(Composite parent, DataBindingContext context, final WritableValue master) {
		IValueProperty textProp = WidgetProperties.text();

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Type");

			ComboViewer viewer = new ComboViewer(parent);
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setInput(new ItemType[] { ItemType.CHECK, ItemType.PUSH, ItemType.RADIO });
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			viewer.getControl().setLayoutData(gd);
			IObservableValue itemTypeObs = EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.ITEM__TYPE).observeDetail(master);
			context.bindValue(ViewerProperties.singleSelection().observe(viewer), itemTypeObs);
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Label");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__LABEL).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Tooltip");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__TOOLTIP).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Icon URI");

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__ICON_URI).observeDetail(master));

			Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText("Find ...");
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		}

		// ------------------------------------------------------------

		createSubTypeFormElements(parent, context, master);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Tags");
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			final Text tagText = new Text(parent, SWT.BORDER);
			tagText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			tagText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
						handleAddText(tagText);
					}
				}
			});

			Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText("Add");
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleAddText(tagText);
				}
			});
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

			l = new Label(parent, SWT.NONE);
			final TableViewer viewer = new TableViewer(parent);
			viewer.setContentProvider(new ObservableListContentProvider());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return element.toString();
				}
			});
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 120;
			viewer.getControl().setLayoutData(gd);

			IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS);
			viewer.setInput(prop.observeDetail(master));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout();
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Up");
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Down");
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Remove");
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if( ! s.isEmpty() ) {
						MApplicationElement appEl = (MApplicationElement) master.getValue();
						Command cmd = RemoveCommand.create(getEditingDomain(), appEl, MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS, s.toList());
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}
	}

	private void handleAddText(Text tagText) {
		if (tagText.getText().trim().length() > 0) {
			String[] tags = tagText.getText().split(";");
			for( int i = 0; i < tags.length;i++ ) {
				tags[i] = tags[i].trim();
			}

			MApplicationElement appEl = (MApplicationElement) master.getValue();
			Command cmd = AddCommand.create(getEditingDomain(), appEl, MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS, Arrays.asList(tags));
			if (cmd.canExecute()) {
				getEditingDomain().getCommandStack().execute(cmd);
			}
			tagText.setText("");
		}
	}

	protected void createSubTypeFormElements(Composite parent, DataBindingContext context, WritableValue master) {

	}

	@Override
	public IObservableList getChildList(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		MToolItem item = (MToolItem) element;
		if (item.getType() == ItemType.SEPARATOR) {
			return null;
		} else {
			if (item.getLabel() != null && item.getLabel().trim().length() > 0) {
				return item.getLabel();
			} else if (item.getTooltip() != null && item.getTooltip().trim().length() > 0) {
				return item.getTooltip();
			}
		}
		return null;
	}

}
