/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steve@spungin.tv> - Ongoing Maintenance, Bug 439532, Bug 443945
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FeatureSelectionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FindParentReferenceElementDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Text;

public class StringModelFragment extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	private IListProperty MODEL_FRAGMENT__ELEMENTS = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS);

	private List<Action> actions = new ArrayList<Action>();

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public StringModelFragment() {
		super();
	}

	@PostConstruct
	public void init() {
		List<FeatureClass> list = new ArrayList<FeatureClass>();
		Util.addClasses(ApplicationPackageImpl.eINSTANCE, list);
		list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT, FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS));
		for (final FeatureClass featureClass : list) {
			actions.add(new Action(featureClass.label) {

				@Override
				public void run() {
					handleAdd(featureClass.eClass, false);
				}
			});
		}
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_StringModelFragment);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.StringModelFragment_Label;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__FEATURENAME), FeaturePath.fromList(FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID) };
	}

	@Override
	public String getDetailLabel(Object element) {
		if (element instanceof StringModelFragmentImpl) {
			StringModelFragmentImpl fragment = (StringModelFragmentImpl) element;
			String ret = ""; //$NON-NLS-1$
			if (E.notEmpty(fragment.getFeaturename())) {
				ret += fragment.getFeaturename();
			}
			if (E.notEmpty(fragment.getParentElementId())) {
				ret += " (" + fragment.getParentElementId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return ret;
		} else {
			return null;
		}
	}

	@Override
	public String getDescription(Object element) {
		return Messages.StringModelFragment_Description;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
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

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_ParentId);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Composite comp = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			comp.setLayoutData(gd);
			GridLayout gl = new GridLayout(2, false);
			gl.marginWidth = gl.marginHeight = 0;
			gl.verticalSpacing = 0;
			gl.marginLeft = gl.marginBottom = gl.marginRight = gl.marginTop = 0;
			comp.setLayout(gl);

			Text t = new Text(comp, SWT.BORDER);
			TextPasteHandler.createFor(t);
			// t.setEditable(false);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID).observeDetail(getMaster()));

			final Button b = new Button(comp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FindParentReferenceElementDialog dialog = new FindParentReferenceElementDialog(b.getShell(), StringModelFragment.this, (MStringModelFragment) getMaster().getValue(), Messages);
					dialog.open();
				}
			});
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_Featurename);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Composite comp = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			comp.setLayoutData(gd);
			GridLayout gl = new GridLayout(2, false);
			gl.marginWidth = gl.marginHeight = 0;
			gl.verticalSpacing = 0;
			gl.marginLeft = gl.marginBottom = gl.marginRight = gl.marginTop = 0;
			comp.setLayout(gl);

			Text t = new Text(comp, SWT.BORDER);
			TextPasteHandler.createFor(t);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__FEATURENAME).observeDetail(getMaster()));

			final Button button = new Button(comp, SWT.PUSH | SWT.FLAT);
			button.setText(Messages.ModelTooling_Common_FindEllipsis);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FeatureSelectionDialog dialog = new FeatureSelectionDialog(button.getShell(), getEditingDomain(), (MStringModelFragment) getMaster().getValue(), Messages);
					dialog.open();
				}
			});

		}

		ControlFactory.createTextField(parent, Messages.StringModelFragment_PositionInList, getMaster(), context, textProp, EMFEditProperties.value(getEditingDomain(), FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__POSITION_IN_LIST));

		// ------------------------------------------------------------
		{

			E4PickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this, FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS) {
				@Override
				protected void addPressed() {
					EClass eClass = ((FeatureClass) ((IStructuredSelection) getPicker().getSelection()).getFirstElement()).eClass;
					handleAdd(eClass, false);
				}

				@Override
				protected List<?> getContainerChildren(Object master) {
					return ((StringModelFragmentImpl) master).getElements();
				}
			};

			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(""); //$NON-NLS-1$

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
			list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT, FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS));

			picker.setInput(list);
			picker.getCombo().select(0);

			IEMFListProperty prop = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS);
			pickList.getList().setInput(prop.observeDetail(getMaster()));

		}

		createContributedEditorTabs(folder, context, getMaster(), MStringModelFragment.class);

		folder.setSelection(0);

		return folder;
	}

	public void dispose() {
		if (composite != null) {
			composite.dispose();
			composite = null;
		}

		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@Override
	public IObservableList getChildList(Object element) {
		return MODEL_FRAGMENT__ELEMENTS.observe(element);
	}

	protected void handleAdd(EClass eClass, boolean separator) {
		EObject eObject = EcoreUtil.create(eClass);
		setElementId(eObject);
		Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		ArrayList<Action> l = new ArrayList<Action>(super.getActions(element));
		l.addAll(actions);
		Collections.sort(l, new Comparator<Action>() {
			@Override
			public int compare(Action o1, Action o2) {

				return o1.getText().compareTo(o2.getText());
			}
		});
		return l;
	}

}
