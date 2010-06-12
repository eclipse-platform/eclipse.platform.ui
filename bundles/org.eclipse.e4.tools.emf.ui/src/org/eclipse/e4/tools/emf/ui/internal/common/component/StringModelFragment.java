package org.eclipse.e4.tools.emf.ui.internal.common.component;

import org.eclipse.emf.ecore.EPackage;

import java.util.ArrayList;

import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;

import org.eclipse.emf.ecore.EClassifier;

import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;

import org.eclipse.e4.ui.model.fragment.MModelFragment;

import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;

import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.emf.databinding.EMFProperties;

import org.eclipse.e4.tools.emf.ui.internal.Messages;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.emf.databinding.EMFDataBindingContext;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.emf.edit.domain.EditingDomain;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;

public class StringModelFragment extends AbstractComponentEditor {
	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;
	private ModelEditor editor;

	private IListProperty MODEL_FRAGMENT__ELEMENTS = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS);

	public StringModelFragment(EditingDomain editingDomain, ModelEditor editor) {
		super(editingDomain);
		this.editor = editor;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/StringModelFragment.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.StringModelFragment_Label;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.StringModelFragment_Description;
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent);
		}
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_ParentId);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID).observeDetail(getMaster()));
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_Featurename);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__FEATURENAME).observeDetail(getMaster()));
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_PositionInList);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__POSITION_IN_LIST).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.StringModelFragment_Elements);
			l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

			final TableViewer viewer = new TableViewer(parent);
			viewer.setContentProvider(new ObservableListContentProvider());
			viewer.setLabelProvider(new ComponentLabelProvider(editor));
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 300;
			viewer.getControl().setLayoutData(gd);

			IEMFListProperty prop = EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS);
			viewer.setInput(prop.observeDetail(getMaster()));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout(2, false);
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.StringModelFragment_Up);
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MModelFragment container = (MModelFragment) getMaster().getValue();
							int idx = container.getElements().indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS, obj, idx);

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
			b.setText(Messages.StringModelFragment_Down);
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MModelFragment container = (MModelFragment) getMaster().getValue();
							int idx = container.getElements().indexOf(obj) + 1;
							if (idx < container.getElements().size()) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS, obj, idx);

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
					EClass eclass = (EClass) element;
					return eclass.getName();
				}
			});

			List<EClass> list = new ArrayList<EClass>();
			addClasses(ApplicationPackageImpl.eINSTANCE, list);

			childrenDropDown.setInput(list);

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					EClass eClass = (EClass) ((IStructuredSelection) childrenDropDown.getSelection()).getFirstElement();
					EObject eObject = EcoreUtil.create(eClass);

					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS, eObject);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						editor.setSelection(eObject);
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.StringModelFragment_Remove);
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						List<?> elements = ((IStructuredSelection) viewer.getSelection()).toList();

						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS, elements);
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}

		return parent;
	}
	
	public void addClasses(EPackage ePackage, List<EClass> list) {
		for (EClassifier c : ePackage.getEClassifiers()) {
			if (c instanceof EClass) {
				EClass eclass = (EClass) c;
				if ( eclass != ApplicationPackageImpl.Literals.APPLICATION && !eclass.isAbstract() && !eclass.isInterface() && eclass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
					list.add(eclass);
				}
			}
		}
		
		for (EPackage eSubPackage : ePackage.getESubpackages()) {
			addClasses(eSubPackage, list);
		}
	}

	public void dispose() {
		if (image != null) {
			image.dispose();
			image = null;
		}

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

}
