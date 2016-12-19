/*******************************************************************************
 * Copyright (c) 2010, 2016 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steve@spungin.tv> - Ongoing Maintenance, Bug 439532, Bug 443945
 * Patrik Suzzi <psuzzi@gmail.com> - Bug 467262
 * Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 509488
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.emf.xpath.EcoreXPathContextFactory;
import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.ModelResultHandler;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FeatureSelectionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FindParentReferenceElementDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ModelResultHandlerImpl;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.StringModelFragmentImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class StringModelFragment extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	// The local collector used by both dialogs (findParentReference and
	// FeaturesSelection)
	private ClassContributionCollector collector;
	// The selected Container is the class that match the ID.
	// It can be get from the FindParentReferenceDialog or computed from the ID.
	private EClass selectedContainer;

	private final IListProperty MODEL_FRAGMENT__ELEMENTS = EMFProperties
			.list(FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS);

	private final List<Action> actions = new ArrayList<>();

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public StringModelFragment() {
		super();
		collector = getCollector();
	}

	@PostConstruct
	public void init() {
		final List<FeatureClass> list = new ArrayList<>();
		Util.addClasses(ApplicationPackageImpl.eINSTANCE, list);
		list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT,
				FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS));
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
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_StringModelFragment);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.StringModelFragment_Label;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
				FeaturePath.fromList(FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__FEATURENAME),
				FeaturePath.fromList(FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID) };
	}

	@Override
	public String getDetailLabel(Object element) {
		if (element instanceof StringModelFragmentImpl) {
			final StringModelFragmentImpl fragment = (StringModelFragmentImpl) element;
			String ret = ""; //$NON-NLS-1$
			if (E.notEmpty(fragment.getFeaturename())) {
				ret += fragment.getFeaturename();
			}
			if (E.notEmpty(fragment.getParentElementId())) {
				ret += " (" + fragment.getParentElementId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return ret;
		}
		return null;
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

	/**
	 * Returns the selectedContainer, which is the EClass behind the Extended
	 * Element ID. It can be known thanks to the dialog or must be computed from
	 * the ID value
	 *
	 * @return
	 */
	private EClass getSelectedContainer() {

		if (selectedContainer != null) {
			return selectedContainer;
		}

		// we get the StringModelFragment
		StringModelFragmentImpl modelFragment = getStringModelFragment();

		Filter filter = new Filter(ApplicationPackageImpl.eINSTANCE.getApplication(), ""); //$NON-NLS-1$
		WritableList list = new WritableList<>();
		ModelResultHandler resultHandler = new ModelResultHandlerImpl(list, filter, this,
				modelFragment.eResource());

		collector.findModelElements(filter, resultHandler);
		List<EClass> globalResult = new ArrayList<>();

		for (Object o : list) {
			if (o instanceof MApplication) {
				MApplication mApp = (MApplication) o;
				List<EClass> targetClass = getTargetClass(mApp);
				globalResult.addAll(targetClass);
			}
		}
		selectedContainer = globalResult.isEmpty() ? null : globalResult.get(0);

		return selectedContainer;
	}

	private StringModelFragmentImpl getStringModelFragment() {
		return ((StringModelFragmentImpl) getMaster().getValue());
	}

	private ClassContributionCollector getCollector() {
		final Bundle bundle = FrameworkUtil.getBundle(FindParentReferenceElementDialog.class);
		final BundleContext context = bundle.getBundleContext();
		final ServiceReference<?> ref = context.getServiceReference(ClassContributionCollector.class.getName());
		if (ref != null) {
			return (ClassContributionCollector) context.getService(ref);
		}
		return null;
	}

	private Composite createForm(Composite parent) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		final CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);
		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_ParentId);
			l.setToolTipText(Messages.StringModelFragment_ParentIdTooltip);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			final Composite comp = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			comp.setLayoutData(gd);
			final GridLayout gl = new GridLayout(2, false);
			gl.marginWidth = gl.marginHeight = 0;
			gl.verticalSpacing = 0;
			gl.marginLeft = gl.marginBottom = gl.marginRight = gl.marginTop = 0;
			comp.setLayout(gl);

			final Text t = new Text(comp, SWT.BORDER);
			TextPasteHandler.createFor(t);
			// t.setEditable(false);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(
					textProp.observeDelayed(200, t),
					EMFEditProperties.value(getEditingDomain(),
							FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID).observeDetail(getMaster()));

			// Add a modify listener to control the change of the ID -> Must
			// force the computation of selectedContainer.
			t.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					selectedContainer = null;
				}
			});

			final Button b = new Button(comp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final FindParentReferenceElementDialog dialog = new FindParentReferenceElementDialog(b.getShell(),
							StringModelFragment.this, (MStringModelFragment) getMaster().getValue(), Messages,
							collector);
					dialog.open();
					selectedContainer = dialog.getSelectedContainer();
				}
			});
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_Featurename);
			l.setToolTipText(Messages.StringModelFragment_FeaturenameTooltip);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			final Composite comp = new Composite(parent, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			comp.setLayoutData(gd);
			final GridLayout gl = new GridLayout(2, false);
			gl.marginWidth = gl.marginHeight = 0;
			gl.verticalSpacing = 0;
			gl.marginLeft = gl.marginBottom = gl.marginRight = gl.marginTop = 0;
			comp.setLayout(gl);

			final Text t = new Text(comp, SWT.BORDER);
			TextPasteHandler.createFor(t);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(
					textProp.observeDelayed(200, t),
					EMFEditProperties.value(getEditingDomain(),
							FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__FEATURENAME).observeDetail(getMaster()));

			final Button button = new Button(comp, SWT.PUSH | SWT.FLAT);
			button.setText(Messages.ModelTooling_Common_FindEllipsis);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final FeatureSelectionDialog dialog = new FeatureSelectionDialog(button.getShell(),
							getEditingDomain(), (MStringModelFragment) getMaster().getValue(), Messages,
							getSelectedContainer());
					dialog.open();
				}
			});

		}

		ControlFactory.createTextField(parent, Messages.StringModelFragment_PositionInList, getMaster(), context,
				textProp, EMFEditProperties.value(getEditingDomain(),
						FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__POSITION_IN_LIST));

		// ------------------------------------------------------------
		{

			final E4PickList pickList = new E4PickList(parent, SWT.NONE, null, Messages, this,
					FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS) {
				@Override
				protected void addPressed() {
					final EClass eClass = ((FeatureClass) ((IStructuredSelection) getSelection())
							.getFirstElement()).eClass;
					handleAdd(eClass, false);
				}

				@Override
				protected List<?> getContainerChildren(Object master) {
					return ((StringModelFragmentImpl) master).getElements();
				}
			};

			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			pickList.setText(""); //$NON-NLS-1$

			pickList.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					final FeatureClass eclass = (FeatureClass) element;
					return eclass.label;
				}
			});

			pickList.setComparator(new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2) {
					final FeatureClass eClass1 = (FeatureClass) e1;
					final FeatureClass eClass2 = (FeatureClass) e2;
					return eClass1.label.compareTo(eClass2.label);
				}
			});

			final List<FeatureClass> list = new ArrayList<>();
			Util.addClasses(ApplicationPackageImpl.eINSTANCE, list);
			list.addAll(getEditor().getFeatureClasses(FragmentPackageImpl.Literals.MODEL_FRAGMENT,
					FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS));

			pickList.setInput(list);
			if (list.size() > 0) {
				pickList.setSelection(new StructuredSelection(list.get(0)));
			}

			final IEMFListProperty prop = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS);
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
		final EObject eObject = EcoreUtil.create(eClass);
		setElementId(eObject);
		final Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(),
				FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS, eObject);

		if (cmd.canExecute()) {
			getEditingDomain().getCommandStack().execute(cmd);
			getEditor().setSelection(eObject);
		}
	}

	@Override
	public List<Action> getActions(Object element) {
		final ArrayList<Action> l = new ArrayList<>(super.getActions(element));
		l.addAll(actions);
		Collections.sort(l, new Comparator<Action>() {
			@Override
			public int compare(Action o1, Action o2) {

				return o1.getText().compareTo(o2.getText());
			}
		});
		return l;
	}

	/**
	 * This method returns the target class of the element pointed by the
	 * 'extended element ID' value In case of several matches it returns a list
	 * of possible classes (but this case means that the ID is used for
	 * different objects in different models present in the workspace). If the
	 * parent element ID value is xpath:/ or
	 * 'org.eclipse.e4.legacy.ide.application', it returns MApplication EClass
	 *
	 * @param application
	 *            : the application to be parsed
	 * @return the list of EClass
	 */
	public List<EClass> getTargetClass(MApplication application) {
		List<EClass> ret = Collections.emptyList();
		StringModelFragmentImpl modelFragment = getStringModelFragment();


		String idsOrXPath = modelFragment.getParentElementId();
		if ("xpath:/".equals(idsOrXPath) || "org.eclipse.e4.legacy.ide.application".equals(idsOrXPath)) {
			ret = new ArrayList<>();
			ret.add(ApplicationPackageImpl.eINSTANCE.getApplication());
		} else {
			// Deal with non default MApplication IDs.
			if (idsOrXPath.startsWith("xpath:")) {
				String xPath = idsOrXPath.substring(6);
				ret = getTargetClassFromXPath(application, xPath);
			} else {

				MApplicationElement o = ModelUtils.findElementById(application, idsOrXPath);
				if (o != null) {
					ret = new ArrayList<>();
					ret.add(((EObject) o).eClass());
				}
			}
		}

		return ret;

	}



	/**
	 * Returns the EClass of the Application element(s) referenced by the xpath
	 * value (without prefix)
	 *
	 * @param application
	 *            : the application to be parsed
	 * @param xpath
	 *            : the xpath value without the 'xpath:' prefix
	 * @return the list of EClass(es) matching this xpath
	 */
	private List<EClass> getTargetClassFromXPath(MApplication application, String xpath) {
		List<EClass> ret = new ArrayList<>();

		XPathContextFactory<EObject> f = EcoreXPathContextFactory.newInstance();
		XPathContext xpathContext = f.newContext((EObject) application);
		Iterator<Object> i = xpathContext.iterate(xpath);

		try {
			while (i.hasNext()) {
				Object obj = i.next();
				if (obj instanceof MApplicationElement) {
					ApplicationElementImpl ae = (ApplicationElementImpl) obj;
					ret.add(ae.eClass());
				}
			}
		} catch (Exception ex) {
			// custom xpath functions will throw exceptions
			ex.printStackTrace();
		}

		return ret;
	}



}
