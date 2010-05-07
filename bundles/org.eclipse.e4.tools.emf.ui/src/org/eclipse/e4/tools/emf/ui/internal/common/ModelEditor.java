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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.ISelectionProviderService;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ShadowComposite;
import org.eclipse.e4.tools.emf.ui.internal.common.component.AddonsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ApplicationEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.BindingTableEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.InputPartEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.KeyBindingEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuSeparatorEditor;
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
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarSeparatorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimmedWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationAddons;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VBindingTableEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VCommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VHandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VModelComponentBindingEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartDescriptor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowTrimEditor;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.modeling.ESelectionService;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ModelEditor {
	private static final String CSS_CLASS_KEY = "org.eclipse.e4.ui.css.CssClassName"; //$NON-NLS-1$
	
	public static final int VIRTUAL_PART_MENU = 0;
	public static final int VIRTUAL_PART = 1;
	public static final int VIRTUAL_HANDLER = 2;
	public static final int VIRTUAL_BINDING_TABLE = 3;
	public static final int VIRTUAL_COMMAND = 4;
	public static final int VIRTUAL_WINDOWS = 5;
	public static final int VIRTUAL_WINDOW_CONTROLS = 6;
	public static final int VIRTUAL_PART_DESCRIPTORS = 7;
	public static final int VIRTUAL_MODEL_COMP_COMMANDS = 8;
	public static final int VIRTUAL_MODEL_COMP_BINDINGS = 9;
	public static final int VIRTUAL_PARTDESCRIPTOR_MENU = 10;
	public static final int VIRTUAL_TRIMMED_WINDOW_TRIMS = 11;
	public static final int VIRTUAL_ADDONS = 12;

	private Map<EClass, AbstractComponentEditor> editorMap = new HashMap<EClass, AbstractComponentEditor>();
	private AbstractComponentEditor[] virtualEditors;
	private List<FeaturePath> labelFeaturePaths = new ArrayList<FeaturePath>();

	// private List<AbstractComponentEditor> editors = new
	// ArrayList<AbstractComponentEditor>();

	private TreeViewer viewer;
	private IModelResource modelProvider;
	private IProject project;
	private ISelectionProviderService selectionService;

	public ModelEditor(Composite composite, IModelResource modelProvider, IProject project) {
		this.modelProvider = modelProvider;
		this.project = project;
		registerDefaultEditors();
		registerVirtualEditors();
		SashForm form = new SashForm(composite, SWT.HORIZONTAL);
		form.setBackground(form.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		viewer = createTreeViewerArea(form);

		Composite parent = new Composite(form, SWT.NONE);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
		// parent.setData("org.eclipse.e4.ui.css.CssClassName","contentContainer");
		FillLayout l = new FillLayout();
		l.marginWidth = 5;
		parent.setLayout(l);

		ShadowComposite editingArea = new ShadowComposite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginTop = 0;
		gl.marginHeight = 0;
		editingArea.setLayout(gl);
		editingArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
		// editingArea.setData("org.eclipse.e4.ui.css.CssClassName","contentContainer");

		Composite headerContainer = new Composite(editingArea, SWT.NONE);
		headerContainer.setBackgroundMode(SWT.INHERIT_DEFAULT);
		headerContainer.setData(CSS_CLASS_KEY, "headerSectionContainer"); //$NON-NLS-1$
		headerContainer.setLayout(new GridLayout(2, false));
		headerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label iconLabel = new Label(headerContainer, SWT.NONE);
		iconLabel.setLayoutData(new GridData(20, 20));

		final Label textLabel = new Label(headerContainer, SWT.NONE);
		textLabel.setData(CSS_CLASS_KEY, "sectionHeader"); //$NON-NLS-1$
		textLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final ScrolledComposite scrolling = new ScrolledComposite(editingArea, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolling.setBackgroundMode(SWT.INHERIT_DEFAULT);
		scrolling.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$
		
		final Composite contentContainer = new Composite(scrolling, SWT.NONE);
		contentContainer.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$
		scrolling.setExpandHorizontal(true);
		scrolling.setExpandVertical(true);
		scrolling.setContent(contentContainer);
		
		scrolling.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolling.getClientArea();
				scrolling.setMinSize(contentContainer.computeSize(r.width, SWT.DEFAULT));
			}
		});
		
		scrolling.setLayoutData(new GridData(GridData.FILL_BOTH));
		final StackLayout layout = new StackLayout();
		contentContainer.setLayout(layout);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) event.getSelection();
					if (s.getFirstElement() instanceof EObject) {
						EObject obj = (EObject) s.getFirstElement();
						AbstractComponentEditor editor = editorMap.get(obj.eClass());
						if (editor != null) {
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
						if (editor != null) {
							textLabel.setText(editor.getLabel(entry));
							iconLabel.setImage(editor.getImage(entry, iconLabel.getDisplay()));
							Composite comp = editor.getEditor(contentContainer, s.getFirstElement());
							comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
							layout.topControl = comp;
							contentContainer.layout(true);
						}
					}
					
					Rectangle r = scrolling.getClientArea();
					scrolling.setMinSize(contentContainer.computeSize(r.width, SWT.DEFAULT));
					
					if( selectionService != null ) {
						selectionService.setSelection(s.getFirstElement());
					}
					
				}
			}
		});

		form.setWeights(new int[] { 1, 2 });
		viewer.setSelection(new StructuredSelection(modelProvider.getRoot()));
		
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
				if( ! s.isEmpty() ) {
					List<Action> actions;
					if( s.getFirstElement() instanceof VirtualEntry<?> ) {
						actions = virtualEditors[((VirtualEntry<?>)s.getFirstElement()).getId()].getActions(s.getFirstElement());
					} else {
						EObject o = (EObject) s.getFirstElement();
						AbstractComponentEditor editor = editorMap.get(o.eClass());
						if( editor != null ) {
							actions = editor.getActions(s.getFirstElement());
						} else {
							actions = Collections.emptyList();
						}
					}
					
					for( Action a : actions ) {
						manager.add(a);
					}
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
	}

	@Inject @Optional
	public void setSelectionService(ISelectionProviderService selectionService) {
		this.selectionService = selectionService;
		if( viewer != null && ! viewer.getControl().isDisposed() ) {
			if( ! viewer.getSelection().isEmpty() ) {
				selectionService.setSelection(((IStructuredSelection)viewer.getSelection()).getFirstElement());	
			}
		}
	}
	
	@Inject
	public void updateSelection(@Optional @Named(IServiceConstants.SELECTION) Object selection ) {
		System.err.println("The selection: " + selection);
	}
	
	private TreeViewer createTreeViewerArea(Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		parent.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		FillLayout l = new FillLayout();
		l.marginWidth = 5;
		parent.setLayout(l);
		ShadowComposite editingArea = new ShadowComposite(parent, SWT.NONE);
		editingArea.setLayout(new FillLayout());
		final TreeViewer viewer = new TreeViewer(editingArea, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setLabelProvider(new ComponentLabelProvider(this));
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(new ObservableFactoryImpl(), new TreeStructureAdvisorImpl());
		viewer.setContentProvider(contentProvider);

		final WritableSet clearedSet = new WritableSet();

		contentProvider.getKnownElements().addSetChangeListener(new ISetChangeListener() {

			public void handleSetChange(SetChangeEvent event) {
				for (Object o : event.diff.getAdditions()) {
					if (o instanceof EObject) {
						clearedSet.add(o);
					}
				}

				for (Object o : event.diff.getRemovals()) {
					if (o instanceof EObject) {
						clearedSet.remove(o);
					}
				}
			}
		});

		for (FeaturePath p : labelFeaturePaths) {
			IObservableMap map = EMFProperties.value(p).observeDetail(clearedSet);
			map.addMapChangeListener(new IMapChangeListener() {

				public void handleMapChange(MapChangeEvent event) {
					viewer.update(event.diff.getChangedKeys().toArray(), null);
				}
			});
		}

		viewer.setInput(modelProvider.getRoot());
		viewer.expandAll();

		return viewer;
	}

	private void registerVirtualEditors() {
		virtualEditors = new AbstractComponentEditor[] { new VMenuEditor(modelProvider.getEditingDomain(), this, BasicPackageImpl.Literals.PART__MENUS), // V-Menu
				null, // V-Part
				new VHandlerEditor(modelProvider.getEditingDomain(), this), 
				new VBindingTableEditor(modelProvider.getEditingDomain(), this), 
				new VCommandEditor(modelProvider.getEditingDomain(), this, ApplicationPackageImpl.Literals.APPLICATION__COMMANDS), 
				new VWindowEditor(modelProvider.getEditingDomain(), this), 
				new VWindowControlEditor(modelProvider.getEditingDomain(), this), 
				new VPartDescriptor(modelProvider.getEditingDomain(), this),
				new VCommandEditor(modelProvider.getEditingDomain(), this, ApplicationPackageImpl.Literals.MODEL_COMPONENT__COMMANDS),
				new VModelComponentBindingEditor(modelProvider.getEditingDomain(), this),
				new VMenuEditor(modelProvider.getEditingDomain(), this, org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR__MENUS),
				new VWindowTrimEditor(modelProvider.getEditingDomain(), this),
				new VApplicationAddons(modelProvider.getEditingDomain(), this)
		};
	}

	public void setSelection(Object element) {
		viewer.setSelection(new StructuredSelection(element));
	}

	private void registerDefaultEditors() {
		registerEditor(ApplicationPackageImpl.Literals.APPLICATION, new ApplicationEditor(modelProvider.getEditingDomain()));
		registerEditor(ApplicationPackageImpl.Literals.MODEL_COMPONENTS, new ModelComponentsEditor(modelProvider.getEditingDomain(),this));
		registerEditor(ApplicationPackageImpl.Literals.MODEL_COMPONENT, new ModelComponentEditor(modelProvider.getEditingDomain()));
		registerEditor(ApplicationPackageImpl.Literals.ADDON, new AddonsEditor(modelProvider.getEditingDomain(),project));
		
		
		registerEditor(CommandsPackageImpl.Literals.KEY_BINDING, new KeyBindingEditor(modelProvider.getEditingDomain(),modelProvider));
		registerEditor(CommandsPackageImpl.Literals.HANDLER, new HandlerEditor(modelProvider.getEditingDomain(),modelProvider,project));
		registerEditor(CommandsPackageImpl.Literals.COMMAND, new CommandEditor(modelProvider.getEditingDomain()));
		registerEditor(CommandsPackageImpl.Literals.BINDING_TABLE, new BindingTableEditor(modelProvider.getEditingDomain(), this));
		
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR, new ToolBarEditor(modelProvider.getEditingDomain(), this));
		registerEditor(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, new DirectToolItemEditor(modelProvider.getEditingDomain(),project));
		registerEditor(MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, new HandledToolItemEditor(modelProvider.getEditingDomain(),modelProvider));
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, new ToolBarSeparatorEditor(modelProvider.getEditingDomain()));
		registerEditor(MenuPackageImpl.Literals.TOOL_CONTROL, new ToolControlEditor(modelProvider.getEditingDomain(),project));
		
		registerEditor(MenuPackageImpl.Literals.MENU, new MenuEditor(modelProvider.getEditingDomain(), this));
		registerEditor(MenuPackageImpl.Literals.MENU_SEPARATOR, new MenuSeparatorEditor(modelProvider.getEditingDomain()));
		registerEditor(MenuPackageImpl.Literals.HANDLED_MENU_ITEM, new HandledMenuItemEditor(modelProvider.getEditingDomain(), modelProvider));
		registerEditor(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, new DirectMenuItemEditor(modelProvider.getEditingDomain(), this, project));
		
		registerEditor(BasicPackageImpl.Literals.PART, new PartEditor(modelProvider.getEditingDomain(),project));
		registerEditor(BasicPackageImpl.Literals.WINDOW, new WindowEditor(modelProvider.getEditingDomain()));
		registerEditor(BasicPackageImpl.Literals.TRIMMED_WINDOW, new TrimmedWindowEditor(modelProvider.getEditingDomain()));
		registerEditor(BasicPackageImpl.Literals.PART_SASH_CONTAINER, new PartSashContainerEditor(modelProvider.getEditingDomain(), this));
		registerEditor(BasicPackageImpl.Literals.PART_STACK, new PartStackEditor(modelProvider.getEditingDomain(),this));
		registerEditor(BasicPackageImpl.Literals.INPUT_PART, new InputPartEditor(modelProvider.getEditingDomain(), project));
		registerEditor(BasicPackageImpl.Literals.TRIM_BAR, new TrimBarEditor(modelProvider.getEditingDomain(), this));
		
		registerEditor(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR, new PartDescriptorEditor(modelProvider.getEditingDomain(),project));
		
		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK, new PerspectiveStackEditor(modelProvider.getEditingDomain(),this));
		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE, new PerspectiveEditor(modelProvider.getEditingDomain(),this));
		registerEditor(AdvancedPackageImpl.Literals.PLACEHOLDER, new PlaceholderEditor(modelProvider.getEditingDomain()));
	}

	public void registerEditor(EClass eClass, AbstractComponentEditor editor) {
		editorMap.put(eClass, editor);

		for (FeaturePath p : editor.getLabelProperties()) {
			boolean found = false;
			for (FeaturePath tmp : labelFeaturePaths) {
				if (equalsPaths(p, tmp)) {
					found = true;
					break;
				}
			}

			if (!found) {
				labelFeaturePaths.add(p);
			}
		}
	}

	private boolean equalsPaths(FeaturePath p1, FeaturePath p2) {
		if (p1.getFeaturePath().length == p2.getFeaturePath().length) {
			for (int i = 0; i < p1.getFeaturePath().length; i++) {
				if (!p1.getFeaturePath()[i].equals(p2.getFeaturePath()[i])) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	public AbstractComponentEditor getEditor(EClass eClass) {
		return editorMap.get(eClass);
	}

	public void doSave(@Optional IProgressMonitor monitor) {
		if (modelProvider.isSaveable()) {
			modelProvider.save();
		}
	}
		
	public void setFocus() {
		viewer.getControl().setFocus();
	}


	private static class TreeStructureAdvisorImpl extends TreeStructureAdvisor {

	}

	private class ObservableFactoryImpl implements IObservableFactory {

		public IObservable createObservable(Object target) {
			if (target instanceof IObservableList) {
				return (IObservable) target;
			} else if (target instanceof VirtualEntry<?>) {
				return ((VirtualEntry<?>) target).getList();
			} else {
				AbstractComponentEditor editor = editorMap.get(((EObject) target).eClass());
				if (editor != null) {
					return editor.getChildList(target);
				}
			}

			return null;
		}
	}
}