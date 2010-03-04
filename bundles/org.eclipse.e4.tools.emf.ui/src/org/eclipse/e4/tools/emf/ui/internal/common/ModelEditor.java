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
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ShadowComposite;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ApplicationEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.BindingEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ModelComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ModelComponentsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartDescriptorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartSashContainerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartStackEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WindowTrimEditor;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
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
	public static final int VIRTUAL_MENU    = 1;
	public static final int VIRTUAL_PART    = 2;
	public static final int VIRTUAL_HANDLER = 3;
	public static final int VIRTUAL_BINDING = 4;
	public static final int VIRTUAL_COMMAND = 5;
	public static final int VIRTUAL_WINDOWS = 6;

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
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
//		parent.setData("org.eclipse.e4.ui.css.CssClassName","contentContainer");
		FillLayout l = new FillLayout();
		l.marginWidth=5;
		parent.setLayout(l);

		ShadowComposite editingArea = new ShadowComposite(parent,SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginTop=0;
		gl.marginHeight=0;
		editingArea.setLayout(gl);
		editingArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
//		editingArea.setData("org.eclipse.e4.ui.css.CssClassName","contentContainer");

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
							textLabel.setText(editor.getLabel(obj));
							iconLabel.setImage(editor.getImage(obj, iconLabel.getDisplay()));
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
		viewer.setLabelProvider(new ComponentLabelProvider(this));
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				new ObservableFactoryImpl(), new TreeStructureAdvisorImpl());
		viewer.setContentProvider(contentProvider);
		viewer.setInput(modelProvider.getRoot());
		viewer.expandAll();

		return viewer;
	}

	private void registerDefaultEditors() {
		registerEditor( MApplicationPackage.Literals.APPLICATION, new ApplicationEditor());
		registerEditor( MApplicationPackage.Literals.MODEL_COMPONENTS, new ModelComponentsEditor());
		registerEditor( MApplicationPackage.Literals.MODEL_COMPONENT, new ModelComponentEditor());
		registerEditor( MApplicationPackage.Literals.PART, new PartEditor());
		registerEditor( MApplicationPackage.Literals.PART_DESCRIPTOR, new PartDescriptorEditor());
		registerEditor( MApplicationPackage.Literals.KEY_BINDING, new BindingEditor());
		registerEditor( MApplicationPackage.Literals.HANDLER, new HandlerEditor());
		registerEditor( MApplicationPackage.Literals.COMMAND,new CommandEditor());
		registerEditor( MApplicationPackage.Literals.WINDOW, new WindowEditor());
		registerEditor( MApplicationPackage.Literals.PART_SASH_CONTAINER, new PartSashContainerEditor());
		registerEditor( MApplicationPackage.Literals.PART_STACK, new PartStackEditor());
		registerEditor( MApplicationPackage.Literals.WINDOW_TRIM, new WindowTrimEditor());
		registerEditor( MApplicationPackage.Literals.TOOL_BAR, new ToolBarEditor());
		registerEditor( MApplicationPackage.Literals.DIRECT_TOOL_ITEM, new DirectToolItemEditor());
		registerEditor( MApplicationPackage.Literals.HANDLED_TOOL_ITEM, new HandledToolItemEditor());
		registerEditor( MApplicationPackage.Literals.TOOL_ITEM, new ToolItemEditor());
	}

	public void registerEditor(EClass eClass, AbstractComponentEditor editor) {
		editorMap.put(eClass, editor);
	}

	public AbstractComponentEditor getEditor(EClass eClass) {
		return editorMap.get(eClass);
	}

	private static class TreeStructureAdvisorImpl extends TreeStructureAdvisor {

	}

	private class ObservableFactoryImpl implements IObservableFactory {

		public IObservable createObservable(Object target) {
			if( target instanceof IObservableList ) {
				return (IObservable) target;
			} else if( target instanceof VirtualEntry<?> ) {
				return ((VirtualEntry<?>)target).getList();
			} else {
				AbstractComponentEditor editor = editorMap.get(((EObject)target).eClass());
				if( editor != null ) {
					return editor.getChildList(target);
				}
			}

			return null;
		}
	}
}