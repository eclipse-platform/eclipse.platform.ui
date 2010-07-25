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
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.ImageTooltip;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.MenuItemIconDialogEditor;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class MenuItemEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image image;
	private Image tbrImage;
	private EMFDataBindingContext context;
	protected IProject project;
	private EStackLayout stackLayout;

	public MenuItemEditor(EditingDomain editingDomain, ModelEditor editor, IProject project) {
		super(editingDomain, editor);
		this.project = project;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			if (((MUIElement) element).isToBeRendered()) {
				if (image == null) {
					try {
						image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/MenuItem.gif")); //$NON-NLS-1$
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return image;
			} else {
				if (tbrImage == null) {
					try {
						tbrImage = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/tbr/MenuItem.gif")); //$NON-NLS-1$
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return tbrImage;
			}
		}

		return null;
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			if (getEditor().isModelFragment()) {
				composite = new Composite(parent, SWT.NONE);
				stackLayout = new EStackLayout();
				composite.setLayout(stackLayout);
				createForm(composite, context, getMaster(), false);
				createForm(composite, context, getMaster(), true);
			} else {
				composite = createForm(parent, context, getMaster(), false);
			}
		}

		if (getEditor().isModelFragment()) {
			Control topControl;
			if (Util.isImport((EObject) object)) {
				topControl = composite.getChildren()[1];
			} else {
				topControl = composite.getChildren()[0];
			}

			if (stackLayout.topControl != topControl) {
				stackLayout.topControl = topControl;
				composite.layout(true, true);
			}
		}

		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master, boolean isImport) {
		parent = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 10;
		parent.setLayout(gl);

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);
		IWidgetValueProperty checkProp = WidgetProperties.selection();
		IWidgetValueProperty enabled = WidgetProperties.enabled();

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		if (isImport) {
			ControlFactory.createFindImport(parent, this, context);
			return parent;
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ModelTooling_Common_Id);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
		}

		if (this.getClass() != MenuItemEditor.class) {
			// ------------------------------------------------------------
			{
				Label l = new Label(parent, SWT.NONE);
				l.setText(Messages.MenuItemEditor_Type);
				l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

				ComboViewer viewer = new ComboViewer(parent);
				viewer.setContentProvider(new ArrayContentProvider());
				viewer.setInput(new ItemType[] { ItemType.CHECK, ItemType.PUSH, ItemType.RADIO });
				GridData gd = new GridData();
				gd.horizontalSpan = 2;
				viewer.getControl().setLayoutData(gd);
				IObservableValue itemTypeObs = EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.ITEM__TYPE).observeDetail(master);
				context.bindValue(ViewerProperties.singleSelection().observe(viewer), itemTypeObs);
			}
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_Label);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_Mnemonics);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.MENU_ITEM__MNEMONICS).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_Tooltip);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__TOOLTIP).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_IconURI);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__ICON_URI).observeDetail(master));

			new ImageTooltip(t) {

				@Override
				protected URI getImageURI() {
					MUILabel part = (MUILabel) getMaster().getValue();
					String uri = part.getIconURI();
					if (uri == null || uri.trim().length() == 0) {
						return null;
					}
					return URI.createURI(part.getIconURI());
				}
			};

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setImage(getImage(t.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MenuItemIconDialogEditor dialog = new MenuItemIconDialogEditor(b.getShell(), project, getEditingDomain(), (MMenuItem) getMaster().getValue());
					dialog.open();
				}
			});
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_Enabled);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Button b = new Button(parent, SWT.CHECK);
			b.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
			context.bindValue(checkProp.observe(b), EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.ITEM__ENABLED).observeDetail(getMaster()));
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.MenuItemEditor_Selected);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Button b = new Button(parent, SWT.CHECK);
			b.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
			context.bindValue(checkProp.observe(b), EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.ITEM__SELECTED).observeDetail(getMaster()));

			UpdateValueStrategy t2m = new UpdateValueStrategy();
			t2m.setConverter(new Converter(boolean.class, ItemType.class) {

				public Object convert(Object fromObject) {
					return null;
				}
			});
			UpdateValueStrategy m2t = new UpdateValueStrategy();
			m2t.setConverter(new Converter(ItemType.class, boolean.class) {

				public Object convert(Object fromObject) {
					return fromObject == ItemType.CHECK || fromObject == ItemType.RADIO;
				}
			});

			context.bindValue(enabled.observe(b), EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.ITEM__TYPE).observeDetail(getMaster()), t2m, m2t);

		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ModelTooling_UIElement_VisibleWhen);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			ComboViewer combo = new ComboViewer(parent);
			combo.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			combo.setContentProvider(new ArrayContentProvider());
			combo.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof EClass) {
						EClass eClass = (EClass) element;
						return eClass.getName();
					}

					return super.getText(element);
				}
			});
			List<Object> list = new ArrayList<Object>();
			list.add(Messages.MenuItemEditor_NoExpression);
			list.add(UiPackageImpl.Literals.CORE_EXPRESSION);
			list.addAll(getEditor().getFeatureClasses(UiPackageImpl.Literals.EXPRESSION, UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN));
			combo.setInput(list);
			context.bindValue(ViewerProperties.singleSelection().observe(combo), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN).observeDetail(getMaster()), new UpdateValueStrategy().setConverter(new EClass2EObject()), new UpdateValueStrategy().setConverter(new EObject2EClass()));
		}

		createFormSubTypeForm(parent, context, master);

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		ControlFactory.createStringListWidget(parent, this, Messages.ModelTooling_ApplicationElement_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);

		return parent;
	}

	protected abstract void createFormSubTypeForm(Composite parent, EMFDataBindingContext context, WritableValue master);

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		MUILabel label = (MUILabel) element;
		if (label.getLabel() != null && label.getLabel().trim().length() > 0) {
			return label.getLabel();
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL), FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED) };
	}

	static class EObject2EClass extends Converter {
		public EObject2EClass() {
			super(EObject.class, EClass.class);
		}

		public Object convert(Object fromObject) {
			if (fromObject == null) {
				return Messages.MenuItemEditor_NoExpression;
			}
			return ((EObject) fromObject).eClass();
		}
	}

	static class EClass2EObject extends Converter {

		public EClass2EObject() {
			super(EClass.class, EObject.class);
		}

		public Object convert(Object fromObject) {
			if (fromObject == null || fromObject.toString().equals(Messages.MenuItemEditor_NoExpression)) {
				return null;
			}
			return EcoreUtil.create((EClass) fromObject);
		}
	}
}