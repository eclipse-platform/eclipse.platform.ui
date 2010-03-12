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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ShadowComposite;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ApplicationEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.KeyBindingEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ModelComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ModelComponentsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartDescriptorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartSashContainerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartStackEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PerspectiveEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PerspectiveStackEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PlaceholderEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WindowTrimEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VCommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VHandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VKeyBindingEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowTrimEditor;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
	public static final int VIRTUAL_MENU    = 0;
	public static final int VIRTUAL_PART    = 1;
	public static final int VIRTUAL_HANDLER = 2;
	public static final int VIRTUAL_BINDING = 3;
	public static final int VIRTUAL_COMMAND = 4;
	public static final int VIRTUAL_WINDOWS = 5;
	public static final int VIRTUAL_WINDOW_CONTROLS = 6;
	public static final int VIRTUAL_WINDOW_TRIMS = 7;

	private Map<EClass, AbstractComponentEditor> editorMap = new HashMap<EClass, AbstractComponentEditor>();
	private AbstractComponentEditor[] virtualEditors;

//	private List<AbstractComponentEditor> editors = new ArrayList<AbstractComponentEditor>();

	private TreeViewer viewer;
	private IModelResource modelProvider;

	@Inject
	public ModelEditor(Composite composite, IModelResource modelProvider) {
		this.modelProvider = modelProvider;
		registerDefaultEditors();
		registerVirtualEditors();
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
					} else {
						VirtualEntry<?> entry = (VirtualEntry<?>) s.getFirstElement();
						AbstractComponentEditor editor = virtualEditors[entry.getId()];
						if( editor != null ) {
							textLabel.setText(editor.getLabel(entry));
							iconLabel.setImage(editor.getImage(entry, iconLabel.getDisplay()));
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
		viewer.setSelection(new StructuredSelection(modelProvider.getRoot()));
	}

	private TreeViewer createTreeViewerArea(Composite parent) {
		parent = new Composite(parent,SWT.NONE);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		FillLayout l = new FillLayout();
		l.marginWidth=5;
		parent.setLayout(l);
		ShadowComposite editingArea = new ShadowComposite(parent,SWT.NONE);
		editingArea.setLayout(new FillLayout());
		TreeViewer viewer = new TreeViewer(editingArea,SWT.FULL_SELECTION|SWT.H_SCROLL|SWT.V_SCROLL);
		viewer.setLabelProvider(new ComponentLabelProvider(this));
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				new ObservableFactoryImpl(), new TreeStructureAdvisorImpl());
		viewer.setContentProvider(contentProvider);
		viewer.setInput(modelProvider.getRoot());
		viewer.expandAll();

		return viewer;
	}

	private void registerVirtualEditors() {
		virtualEditors = new AbstractComponentEditor[] {
				new VMenuEditor(modelProvider.getEditingDomain(),this), // V-Menu
				null, // V-Part
				new VHandlerEditor(modelProvider.getEditingDomain(),this),
				new VKeyBindingEditor(modelProvider.getEditingDomain(), this),
				new VCommandEditor(modelProvider.getEditingDomain(), this),
				new VWindowEditor(modelProvider.getEditingDomain(), this),
				new VControlEditor(modelProvider.getEditingDomain(), this),
				new VWindowTrimEditor(modelProvider.getEditingDomain(), this)
			};

	}
	
	public void setSelection(Object element) {
		viewer.setSelection(new StructuredSelection(element));
	}

	private void registerDefaultEditors() {
		registerEditor( MApplicationPackage.Literals.APPLICATION, new ApplicationEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.MODEL_COMPONENTS, new ModelComponentsEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.MODEL_COMPONENT, new ModelComponentEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.PART, new PartEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.PART_DESCRIPTOR, new PartDescriptorEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.KEY_BINDING, new KeyBindingEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.HANDLER, new HandlerEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.COMMAND,new CommandEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.WINDOW, new WindowEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.PART_SASH_CONTAINER, new PartSashContainerEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.PART_STACK, new PartStackEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.WINDOW_TRIM, new WindowTrimEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.TOOL_BAR, new ToolBarEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.DIRECT_TOOL_ITEM, new DirectToolItemEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.HANDLED_TOOL_ITEM, new HandledToolItemEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.TOOL_ITEM, new ToolItemEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.PERSPECTIVE_STACK, new PerspectiveStackEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.PERSPECTIVE, new PerspectiveEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.PLACEHOLDER, new PlaceholderEditor(modelProvider.getEditingDomain()));
		registerEditor( MApplicationPackage.Literals.MENU, new MenuEditor(modelProvider.getEditingDomain(), this));
		registerEditor( MApplicationPackage.Literals.MENU_ITEM, new MenuItemEditor(modelProvider.getEditingDomain(), this));
		registerEditor( MApplicationPackage.Literals.HANDLED_MENU_ITEM, new HandledMenuItemEditor(modelProvider.getEditingDomain(), this));
		registerEditor( MApplicationPackage.Literals.DIRECT_MENU_ITEM, new DirectMenuItemEditor(modelProvider.getEditingDomain(), this));
	}

	public void registerEditor(EClass eClass, AbstractComponentEditor editor) {
		editorMap.put(eClass, editor);
	}

	public AbstractComponentEditor getEditor(EClass eClass) {
		return editorMap.get(eClass);
	}
	
	public IStatus save() {
		if( modelProvider.isSaveable() ) {
			return modelProvider.save();
		}
		
		return Status.CANCEL_STATUS;
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