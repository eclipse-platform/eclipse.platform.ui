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

import javax.inject.Inject;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.SharedElementsDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PlaceholderEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private EStackLayout stackLayout;

	@Inject
	@Optional
	private IProject project;

	@Inject
	private IModelResource resource;

	@Inject
	public PlaceholderEditor() {
		super();
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_Placeholder);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PlaceholderEditor_Label;
	}

	@Override
	public String getDetailLabel(Object element) {
		MPlaceholder pl = (MPlaceholder) element;
		if (pl.getRef() != null) {
			StringBuilder b = new StringBuilder();

			b.append(((EObject) pl.getRef()).eClass().getName());
			if (pl.getRef() instanceof MUILabel) {
				MUILabel label = (MUILabel) pl.getRef();
				String l = getLocalizedLabel(label);

				if (l != null && l.trim().length() > 0) {
					b.append(" (" + l + ")"); //$NON-NLS-1$//$NON-NLS-2$
				} else if (label.getTooltip() != null && label.getTooltip().trim().length() > 0) {
					b.append(" (" + label.getTooltip() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					if (pl.getRef().getElementId() != null && pl.getRef().getElementId().trim().length() > 0) {
						b.append(" (" + pl.getRef().getElementId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else {
				if (pl.getRef().getElementId() != null && pl.getRef().getElementId().trim().length() > 0) {
					b.append(" (" + pl.getRef().getElementId() + ")"); //$NON-NLS-1$//$NON-NLS-2$
				}
			}

			return b.toString();
		}

		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PlaceholderEditor_Descriptor;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
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

	private Composite createForm(Composite parent, final EMFDataBindingContext context, WritableValue master, boolean isImport) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp, EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID));
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(), context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE));
		ControlFactory.createTextField(parent, Messages.PlaceholderEditor_ContainerData, master, context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__CONTAINER_DATA));
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PlaceholderEditor_Reference);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setEditable(false);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);

			UpdateValueStrategy t2m = new UpdateValueStrategy();
			t2m.setConverter(new Converter(String.class, MUIElement.class) {

				@Override
				public Object convert(Object fromObject) {
					return null;
				}
			});
			UpdateValueStrategy m2t = new UpdateValueStrategy();
			m2t.setConverter(new Converter(MUIElement.class, String.class) {

				@Override
				public Object convert(Object fromObject) {
					if (fromObject != null) {
						EObject o = (EObject) fromObject;
						if (o instanceof MUILabel) {
							MUILabel label = (MUILabel) o;
							String l = getLocalizedLabel(label);
							if (!Util.isNullOrEmpty(l)) {
								return o.eClass().getName() + " - " + l; //$NON-NLS-1$
							}
						}

						return o.eClass().getName() + " - " + ((MUIElement) fromObject).getElementId(); //$NON-NLS-1$
					}
					return null;
				}
			});

			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), AdvancedPackageImpl.Literals.PLACEHOLDER__REF).observeDetail(getMaster()), t2m, m2t);

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SharedElementsDialog dialog = new SharedElementsDialog(b.getShell(), getEditor(), (MPlaceholder) getMaster().getValue(), resource, Messages);
					dialog.open();
				}
			});
		}

		ControlFactory.createCheckBox(parent, Messages.PlaceholderEditor_Closeable, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), AdvancedPackageImpl.Literals.PLACEHOLDER__CLOSEABLE));

		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context, WidgetProperties.selection(), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		if (project == null) {
			createUITreeInspection(folder);
		}

		createContributedEditorTabs(folder, context, getMaster(), MPlaceholder.class);

		folder.setSelection(0);

		return folder;
	}

	private void createUITreeInspection(CTabFolder folder) {
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_RuntimeWidgetTree);
		Composite container = new Composite(folder, SWT.NONE);
		container.setLayout(new GridLayout());
		item.setControl(container);

		UIViewer objectViewer = new UIViewer();
		TreeViewer viewer = objectViewer.createViewer(container, UiPackageImpl.Literals.UI_ELEMENT__WIDGET, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}
