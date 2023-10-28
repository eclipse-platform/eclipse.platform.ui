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
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 472706
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.SharedElementsDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.uistructure.UIViewer;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import jakarta.inject.Inject;

public class PlaceholderEditor extends AbstractComponentEditor<MPlaceholder> {
	private Composite composite;
	private EMFDataBindingContext context;
	private StackLayout stackLayout;

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
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_Placeholder);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PlaceholderEditor_Label;
	}

	@Override
	public String getDetailLabel(Object element) {
		final MPlaceholder pl = (MPlaceholder) element;
		if (pl.getRef() != null) {
			final StringBuilder b = new StringBuilder();

			b.append(((EObject) pl.getRef()).eClass().getName());
			if (pl.getRef() instanceof MUILabel) {
				final MUILabel label = (MUILabel) pl.getRef();
				final String l = getLocalizedLabel(label);

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
				stackLayout = new StackLayout();
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
				composite.requestLayout();
			}
		}

		getMaster().setValue((MPlaceholder) object);
		return composite;
	}

	private Composite createForm(Composite parent, final EMFDataBindingContext context,
			WritableValue<MPlaceholder> master, boolean isImport) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp,
				E4Properties.elementId(getEditingDomain()));
		ControlFactory.createTextField(parent, Messages.ModelTooling_UIElement_AccessibilityPhrase, getMaster(),
				context, textProp, E4Properties.accessibilityPhrase(getEditingDomain()));
		ControlFactory.createTextField(parent, Messages.PlaceholderEditor_ContainerData, master, context, textProp,
				E4Properties.containerData(getEditingDomain()));
		// ------------------------------------------------------------
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PlaceholderEditor_Reference);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setEditable(false);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);

			final UpdateValueStrategy<String, MUIElement> t2m = new UpdateValueStrategy<>();
			t2m.setConverter(new Converter<String, MUIElement>(String.class, MUIElement.class) {
				@Override
				public MUIElement convert(String fromObject) {
					return null;
				}
			});
			final UpdateValueStrategy<MUIElement, String> m2t = new UpdateValueStrategy<>();
			m2t.setConverter(new Converter<MUIElement, String>(MUIElement.class, String.class) {

				@Override
				public String convert(MUIElement fromObject) {
					if (fromObject != null) {
						final EObject o = (EObject) fromObject;
						if (o instanceof MUILabel) {
							final MUILabel label = (MUILabel) o;
							final String l = getLocalizedLabel(label);
							if (!Util.isNullOrEmpty(l)) {
								return o.eClass().getName() + " - " + l; //$NON-NLS-1$
							}
						}

						return o.eClass().getName() + " - " + fromObject.getElementId(); //$NON-NLS-1$
					}
					return null;
				}
			});

			context.bindValue(textProp.observe(t), E4Properties.ref(getEditingDomain()).observeDetail(getMaster()),
					t2m, m2t);

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final SharedElementsDialog dialog = new SharedElementsDialog(b.getShell(), getEditor(),
							getMaster().getValue(), resource, Messages);
					dialog.open();
				}
			});
		}

		ControlFactory.createCheckBox(parent, Messages.PlaceholderEditor_Closeable, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.placeholderClosable(getEditingDomain()));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_ToBeRendered, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.toBeRendered(getEditingDomain()));
		ControlFactory.createCheckBox(parent, Messages.ModelTooling_UIElement_Visible, getMaster(), context,
				WidgetProperties.buttonSelection(), E4Properties.visible(getEditingDomain()));

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
		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_RuntimeWidgetTree);
		final Composite container = new Composite(folder, SWT.NONE);
		container.setLayout(new GridLayout());
		item.setControl(container);

		final UIViewer objectViewer = new UIViewer();
		final TreeViewer viewer = objectViewer.createViewer(container, UiPackageImpl.Literals.UI_ELEMENT__WIDGET, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
	public IObservableList<?> getChildList(Object element) {
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(AdvancedPackageImpl.Literals.PLACEHOLDER__REF) };
	}


	/**
	 * If model is saved (becomes not dirty) must refresh the bindings so as to
	 * have the correct name in the ref editor
	 */
	@Inject
	@Optional
	public void refreshOnSave(
			@UIEventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) org.osgi.service.event.Event event) {
		// When application model is saved, must refresh values (bug 472706)
		final Object type = event.getProperty(EventTags.TYPE);
		final Object newValue = event.getProperty(EventTags.NEW_VALUE);

		if (UIEvents.EventTypes.SET.equals(type) && Boolean.FALSE.equals(newValue) && context != null) {
			context.updateTargets();
		}
	}

}
