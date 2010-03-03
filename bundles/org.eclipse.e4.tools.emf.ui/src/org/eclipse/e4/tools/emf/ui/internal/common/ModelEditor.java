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
package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ShadowComposite;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ApplicationEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ModelComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ModelComponentsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartDescriptorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartEditor;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.e4.ui.model.application.MModelComponents;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ModelEditor {
	private static final int VIRTUAL_MENU = 1;
	private static final int VIRTUAL_PART = 2;
	private static final int VIRTUAL_HANDLER = 3;
	private static final int VIRTUAL_BINDING = 4;

	private Map<EClass, AbstractComponentEditor> editorMap = new HashMap<EClass, AbstractComponentEditor>();
//	private List<AbstractComponentEditor> editors = new ArrayList<AbstractComponentEditor>();

	private TreeViewer viewer;
	private IModelResource modelProvider;

	@Inject
	public ModelEditor(Composite composite, IModelResource modelProvider) {
		this.modelProvider = modelProvider;
		registerDefaultEditors();
		SashForm form = new SashForm(composite, SWT.HORIZONTAL);
		form.setBackground(form.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		viewer = createTreeViewerArea(form);

		Composite parent = new Composite(form,SWT.NONE);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		FillLayout l = new FillLayout();
		l.marginWidth=5;
		parent.setLayout(l);

		ShadowComposite editingArea = new ShadowComposite(parent,SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginTop=0;
		gl.marginHeight=0;
		editingArea.setLayout(gl);
		editingArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
		editingArea.setData("org.eclipse.e4.ui.css.CssClassName","contentContainer");

		Composite headerContainer = new Composite(editingArea,SWT.NONE);
		headerContainer.setBackgroundMode(SWT.INHERIT_DEFAULT);
		headerContainer.setData("org.eclipse.e4.ui.css.CssClassName", "headerSectionContainer");
		headerContainer.setLayout(new GridLayout(2, false));
		headerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label iconLabel = new Label(headerContainer,SWT.NONE);
		iconLabel.setLayoutData(new GridData(20, 20));

		final Label textLabel = new Label(headerContainer, SWT.NONE);
		textLabel.setData("org.eclipse.e4.ui.css.CssClassName", "sectionHeader");
		textLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Composite contentContainer = new Composite(editingArea,SWT.NONE);
		contentContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		final StackLayout layout = new StackLayout();
		contentContainer.setLayout(layout);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if( ! event.getSelection().isEmpty() ) {
					IStructuredSelection s = (IStructuredSelection) event.getSelection();
					if( s.getFirstElement() instanceof EObject ) {
						EObject obj = (EObject) s.getFirstElement();
						AbstractComponentEditor editor = editorMap.get(obj.eClass());
						if( editor != null ) {
							textLabel.setText(editor.getLabel());
							iconLabel.setImage(editor.getImage(iconLabel.getDisplay()));
							Composite comp = editor.getEditor(contentContainer, s.getFirstElement());
							comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
							layout.topControl = comp;
							contentContainer.layout(true);
						}
					}
				}
			}
		});

		form.setWeights(new int[] { 1 , 2 });
	}

	private TreeViewer createTreeViewerArea(Composite parent) {
		parent = new Composite(parent,SWT.NONE);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		FillLayout l = new FillLayout();
		l.marginWidth=5;
		parent.setLayout(l);
		ShadowComposite editingArea = new ShadowComposite(parent,SWT.NONE);
		editingArea.setLayout(new FillLayout());
		TreeViewer viewer = new TreeViewer(editingArea);
		viewer.setLabelProvider(new ComponentLabelProvider());
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				new ObservableFactoryImpl(), new TreeStructureAdvisorImpl());
		viewer.setContentProvider(contentProvider);
		viewer.setInput(modelProvider.getRoot());

		return viewer;
	}

	private void registerDefaultEditors() {
		registerEditor( MApplicationPackage.Literals.APPLICATION, new ApplicationEditor());
		registerEditor( MApplicationPackage.Literals.MODEL_COMPONENTS, new ModelComponentsEditor());
		registerEditor( MApplicationPackage.Literals.MODEL_COMPONENT, new ModelComponentEditor());
		registerEditor( MApplicationPackage.Literals.PART, new PartEditor());
		registerEditor( MApplicationPackage.Literals.PART_DESCRIPTOR, new PartDescriptorEditor());
	}

	public void registerEditor(EClass eClass, AbstractComponentEditor editor) {
		editorMap.put(eClass, editor);
	}

	private static class TreeStructureAdvisorImpl extends TreeStructureAdvisor {

	}

	private static class ObservableFactoryImpl implements IObservableFactory {
		private IListProperty MODEL_COMPONENTS__COMPONENTS = EMFProperties.list(MApplicationPackage.Literals.MODEL_COMPONENTS__COMPONENTS);
		private IListProperty MODEL_COMPONENT__CHILDREN = EMFProperties.list(MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN);
		private IListProperty PART__MENUS = EMFProperties.list(MApplicationPackage.Literals.PART__MENUS);
		private IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties.list(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
		private IListProperty BINDING_CONTAINER__BINDINGS = EMFProperties.list(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS);

		public IObservable createObservable(Object target) {
			if( target instanceof IObservableList ) {
				return (IObservable) target;
			} else if( target instanceof MModelComponents ) {
				return MODEL_COMPONENTS__COMPONENTS.observe(target);
			} else if( target instanceof MModelComponent ) {
				WritableList list = new WritableList();
				list.add(new VirtualEntry<Object>( VIRTUAL_MENU, MODEL_COMPONENT__CHILDREN, target, "Menus") {

					@Override
					protected boolean accepted(Object o) {
						return false;
					}

				});
				list.add(new VirtualEntry<Object>( VIRTUAL_PART, MODEL_COMPONENT__CHILDREN, target, "Parts") {

					@Override
					protected boolean accepted(Object o) {
						return o instanceof MPart;
					}

				});
				return list;
			} else if( target instanceof VirtualEntry<?> ) {
				return ((VirtualEntry<?>)target).getList();
			} else if( target instanceof MPart ) {
				WritableList list = new WritableList();
				list.add(new VirtualEntry<Object>( VIRTUAL_MENU, PART__MENUS, target, "Menus") {

					@Override
					protected boolean accepted(Object o) {
						return true;
					}

				});

				list.add(new VirtualEntry<Object>( VIRTUAL_HANDLER, HANDLER_CONTAINER__HANDLERS, target, "Handlers") {

					@Override
					protected boolean accepted(Object o) {
						return true;
					}

				});

				list.add(new VirtualEntry<Object>( VIRTUAL_BINDING, BINDING_CONTAINER__BINDINGS, target, "Bindings") {

					@Override
					protected boolean accepted(Object o) {
						return true;
					}

				});

				return list;
			}

			// TODO Auto-generated method stub
			return null;
		}
	}
}