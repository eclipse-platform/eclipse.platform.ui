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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.IEditorDescriptor;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.IExtensionLookup;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.ISelectionProviderService;
import org.eclipse.e4.tools.emf.ui.common.MemoryTransfer;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ShadowComposite;
import org.eclipse.e4.tools.emf.ui.internal.common.component.AddonsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ApplicationEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.AreaEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.BindingContextEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.BindingTableEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CategoryEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CommandParameterEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CoreExpressionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.InputPartEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.KeyBindingEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuContributionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.MenuSeparatorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ModelFragmentsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ParameterEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartDescriptorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartSashContainerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PartStackEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PerspectiveEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PerspectiveStackEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PlaceholderEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.PopupMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.RenderedMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.RenderedMenuItem;
import org.eclipse.e4.tools.emf.ui.internal.common.component.RenderedToolBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.StringModelFragment;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarContributionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarSeparatorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimContributionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimmedWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationAddons;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationCategoriesEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VBindingTableEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VCommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VHandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VItemParametersEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuContributionsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuElementsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VModelFragmentsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VModelImportsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartDescriptor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VRootBindingContexts;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VToolBarContributionsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VTrimContributionsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowSharedElementsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowTrimEditor;
import org.eclipse.e4.tools.services.IClipboardService;
import org.eclipse.e4.tools.services.IClipboardService.Handler;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.IEMFProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;

public class ModelEditor {
	private static final String CSS_CLASS_KEY = "org.eclipse.e4.ui.css.CssClassName"; //$NON-NLS-1$

	public static final String VIRTUAL_PART_MENU = ModelEditor.class.getName() + ".VIRTUAL_PART_MENU"; //$NON-NLS-1$
	public static final String VIRTUAL_HANDLER = ModelEditor.class.getName() + ".VIRTUAL_HANDLER"; //$NON-NLS-1$
	public static final String VIRTUAL_BINDING_TABLE = ModelEditor.class.getName() + ".VIRTUAL_BINDING_TABLE"; //$NON-NLS-1$
	public static final String VIRTUAL_COMMAND = ModelEditor.class.getName() + ".VIRTUAL_COMMAND"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOWS = ModelEditor.class.getName() + ".VIRTUAL_WINDOWS"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOW_CONTROLS = ModelEditor.class.getName() + ".VIRTUAL_WINDOW_CONTROLS"; //$NON-NLS-1$
	public static final String VIRTUAL_PART_DESCRIPTORS = ModelEditor.class.getName() + ".VIRTUAL_PART_DESCRIPTORS"; //$NON-NLS-1$
	public static final String VIRTUAL_PARTDESCRIPTOR_MENU = ModelEditor.class.getName() + ".VIRTUAL_PARTDESCRIPTOR_MENU"; //$NON-NLS-1$
	public static final String VIRTUAL_TRIMMED_WINDOW_TRIMS = ModelEditor.class.getName() + ".VIRTUAL_TRIMMED_WINDOW_TRIMS"; //$NON-NLS-1$
	public static final String VIRTUAL_ADDONS = ModelEditor.class.getName() + ".VIRTUAL_ADDONS"; //$NON-NLS-1$
	public static final String VIRTUAL_MENU_CONTRIBUTIONS = ModelEditor.class.getName() + ".VIRTUAL_MENU_CONTRIBUTIONS"; //$NON-NLS-1$
	public static final String VIRTUAL_TOOLBAR_CONTRIBUTIONS = ModelEditor.class.getName() + ".VIRTUAL_TOOLBAR_CONTRIBUTIONS"; //$NON-NLS-1$
	public static final String VIRTUAL_TRIM_CONTRIBUTIONS = ModelEditor.class.getName() + ".VIRTUAL_TRIM_CONTRIBUTIONS"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOW_SHARED_ELEMENTS = ModelEditor.class.getName() + ".VIRTUAL_WINDOW_SHARED_ELEMENTS"; //$NON-NLS-1$
	public static final String VIRTUAL_MODEL_FRAGEMENTS = ModelEditor.class.getName() + ".VIRTUAL_MODEL_FRAGEMENTS"; //$NON-NLS-1$
	public static final String VIRTUAL_MODEL_IMPORTS = ModelEditor.class.getName() + ".VIRTUAL_MODEL_IMPORTS"; //$NON-NLS-1$
	public static final String VIRTUAL_CATEGORIES = ModelEditor.class.getName() + ".VIRTUAL_CATEGORIES"; //$NON-NLS-1$
	public static final String VIRTUAL_PARAMETERS = ModelEditor.class.getName() + ".VIRTUAL_PARAMETERS"; //$NON-NLS-1$
	public static final String VIRTUAL_MENUELEMENTS = ModelEditor.class.getName() + ".VIRTUAL_MENUELEMENTS"; //$NON-NLS-1$
	public static final String VIRTUAL_ROOT_CONTEXTS = ModelEditor.class.getName() + ".VIRTUAL_ROOT_CONTEXTS"; //$NON-NLS-1$

	private Map<EClass, AbstractComponentEditor> editorMap = new HashMap<EClass, AbstractComponentEditor>();
	private Map<String, AbstractComponentEditor> virtualEditors = new HashMap<String, AbstractComponentEditor>();
	private List<FeaturePath> labelFeaturePaths = new ArrayList<FeaturePath>();
	private List<IEditorFeature> editorFeatures = new ArrayList<IEditorFeature>();
	private List<IContributionClassCreator> contributionCreator = new ArrayList<IContributionClassCreator>();

	private TreeViewer viewer;
	private IModelResource modelProvider;
	private IProject project;
	private ISelectionProviderService selectionService;
	private IEclipseContext context;
	private boolean fragment;
	private Handler clipboardHandler;

	private Image removeIcon;

	@Inject
	@Optional
	private IClipboardService clipboardService;

	@Inject
	@Preference(nodePath = "org.eclipse.e4.tools.emf.ui", value = "autoCreateElementId")
	private boolean autoCreateElementId;

	@Inject
	@Preference(nodePath = "org.eclipse.e4.tools.emf.ui", value = "showXMIId")
	private boolean showXMIId;

	@Inject
	@Optional
	private IExtensionLookup extensionLookup;

	private ObservablesManager obsManager;

	public ModelEditor(Composite composite, IEclipseContext context, IModelResource modelProvider, IProject project) {
		this.modelProvider = modelProvider;
		this.project = project;
		this.context = context;
		this.context.set(ModelEditor.class, this);
		this.obsManager = new ObservablesManager();
		this.removeIcon = new Image(composite.getDisplay(), ModelEditor.class.getResourceAsStream("/icons/full/obj16/cross.png"));

		registerDefaultEditors();
		registerVirtualEditors();

		registerContributedEditors();
		registerContributedVirtualEditors();
		loadEditorFeatures();
		loadContributionCreators();

		fragment = modelProvider.getRoot().get(0) instanceof MModelFragments;

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
		headerContainer.setLayout(new GridLayout(3, false));
		headerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label iconLabel = new Label(headerContainer, SWT.NONE);
		iconLabel.setLayoutData(new GridData(20, 20));

		final Label textLabel = new Label(headerContainer, SWT.NONE);
		textLabel.setData(CSS_CLASS_KEY, "sectionHeader"); //$NON-NLS-1$
		textLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final ScrolledComposite scrolling = new ScrolledComposite(editingArea, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolling.setBackgroundMode(SWT.INHERIT_DEFAULT);
		scrolling.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$

		final EStackLayout layout = new EStackLayout();
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
		contentContainer.setLayout(layout);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					final IStructuredSelection s = (IStructuredSelection) event.getSelection();
					if (s.getFirstElement() instanceof EObject) {
						EObject obj = (EObject) s.getFirstElement();
						final AbstractComponentEditor editor = getEditor(obj.eClass());
						if (editor != null) {
							textLabel.setText(editor.getLabel(obj));
							iconLabel.setImage(editor.getImage(obj, iconLabel.getDisplay()));
							obsManager.runAndCollect(new Runnable() {

								public void run() {
									Composite comp = editor.getEditor(contentContainer, s.getFirstElement());
									comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
									layout.topControl = comp;
									contentContainer.layout(true);
								}
							});
						}
					} else {
						VirtualEntry<?> entry = (VirtualEntry<?>) s.getFirstElement();
						final AbstractComponentEditor editor = virtualEditors.get(entry.getId());
						if (editor != null) {
							textLabel.setText(editor.getLabel(entry));
							iconLabel.setImage(editor.getImage(entry, iconLabel.getDisplay()));
							obsManager.runAndCollect(new Runnable() {

								public void run() {
									Composite comp = editor.getEditor(contentContainer, s.getFirstElement());
									comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
									layout.topControl = comp;
									contentContainer.layout(true);
								}

							});
						}
					}

					Rectangle r = scrolling.getClientArea();
					scrolling.setMinSize(contentContainer.computeSize(r.width, SWT.DEFAULT));
					scrolling.setOrigin(0, 0);
					scrolling.layout(true, true);

					if (selectionService != null) {
						selectionService.setSelection(s.getFirstElement());
					}

				}
			}
		});

		form.setWeights(new int[] { 1, 2 });

		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
				if (!s.isEmpty()) {
					List<Action> actions;
					if (s.getFirstElement() instanceof VirtualEntry<?>) {
						actions = virtualEditors.get(((VirtualEntry<?>) s.getFirstElement()).getId()).getActions(s.getFirstElement());
					} else {
						final EObject o = (EObject) s.getFirstElement();
						AbstractComponentEditor editor = getEditor(o.eClass());
						if (editor != null) {
							actions = new ArrayList<Action>(editor.getActions(s.getFirstElement()));
						} else {
							actions = new ArrayList<Action>();
						}

						if (o.eContainer() != null) {
							actions.add(new Action(Messages.ModelEditor_Delete, ImageDescriptor.createFromImage(removeIcon)) {
								public void run() {
									Command cmd = RemoveCommand.create(ModelEditor.this.modelProvider.getEditingDomain(), o.eContainer(), o.eContainingFeature(), o);
									if (cmd.canExecute()) {
										ModelEditor.this.modelProvider.getEditingDomain().getCommandStack().execute(cmd);
									}
								}
							});
						}

					}

					for (Action a : actions) {
						manager.add(a);
					}
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
	}

	@PostConstruct
	void postCreate() {
		viewer.setSelection(new StructuredSelection(modelProvider.getRoot()));
	}

	public IExtensionLookup getExtensionLookup() {
		return extensionLookup;
	}

	public boolean isAutoCreateElementId() {
		return autoCreateElementId && project != null;
	}

	public IProject getProject() {
		return project;
	}

	public boolean isShowXMIId() {
		return showXMIId;
	}

	private void loadContributionCreators() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"contributionClassCreator".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				contributionCreator.add((IContributionClassCreator) el.createExecutableExtension("class")); //$NON-NLS-1$
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public IContributionClassCreator getContributionCreator(EClass eClass) {
		for (IContributionClassCreator c : contributionCreator) {
			if (c.isSupported(eClass)) {
				return c;
			}
		}
		return null;
	}

	private void loadEditorFeatures() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"editorfeature".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				editorFeatures.add((IEditorFeature) el.createExecutableExtension("class")); //$NON-NLS-1$
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean isModelFragment() {
		return fragment;
	}

	public boolean isLiveModel() {
		return !modelProvider.isSaveable();
	}

	public List<FeatureClass> getFeatureClasses(EClass eClass, EStructuralFeature feature) {
		List<FeatureClass> list = new ArrayList<IEditorFeature.FeatureClass>();

		for (IEditorFeature f : editorFeatures) {
			list.addAll(f.getFeatureClasses(eClass, feature));
		}

		return list;
	}

	@Inject
	public void setSelectionService(@Optional ISelectionProviderService selectionService) {
		this.selectionService = selectionService;
		if (viewer != null && !viewer.getControl().isDisposed()) {
			if (!viewer.getSelection().isEmpty() && selectionService != null) {
				selectionService.setSelection(((IStructuredSelection) viewer.getSelection()).getFirstElement());
			}
		}
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
		viewer.expandToLevel(2);
		// ViewerDropAdapter adapter = new ViewerDropAdapter(viewer) {
		//
		// @Override
		// public boolean validateDrop(Object target, int operation,
		// TransferData transferType) {
		// // TODO Auto-generated method stub
		// return false;
		// }
		//
		// @Override
		// public boolean performDrop(Object data) {
		// // TODO Auto-generated method stub
		// return false;
		// }
		// };
		// adapter.setFeedbackEnabled(true);

		int ops = DND.DROP_MOVE;
		viewer.addDragSupport(ops, new Transfer[] { MemoryTransfer.getInstance() }, new DragListener(viewer));
		viewer.addDropSupport(ops, new Transfer[] { MemoryTransfer.getInstance() }, new DropListener(viewer, modelProvider.getEditingDomain()));

		return viewer;
	}

	private void registerContributedVirtualEditors() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"virtualeditor".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			IContributionFactory fact = context.get(IContributionFactory.class);
			AbstractComponentEditor editor = (AbstractComponentEditor) fact.create("platform:/plugin/" + el.getContributor().getName() + "/" + el.getAttribute("class"), context); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			registerVirtualEditor(el.getAttribute("id"), editor); //$NON-NLS-1$
		}
	}

	private void registerVirtualEditors() {
		registerVirtualEditor(VIRTUAL_PART_MENU, new VMenuEditor(modelProvider.getEditingDomain(), this, BasicPackageImpl.Literals.PART__MENUS));
		registerVirtualEditor(VIRTUAL_HANDLER, new VHandlerEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_BINDING_TABLE, new VBindingTableEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_COMMAND, new VCommandEditor(modelProvider.getEditingDomain(), this, ApplicationPackageImpl.Literals.APPLICATION__COMMANDS));
		registerVirtualEditor(VIRTUAL_WINDOWS, new VWindowEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_WINDOW_CONTROLS, new VWindowControlEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_PART_DESCRIPTORS, new VPartDescriptor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_PARTDESCRIPTOR_MENU, new VMenuEditor(modelProvider.getEditingDomain(), this, org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR__MENUS));
		registerVirtualEditor(VIRTUAL_TRIMMED_WINDOW_TRIMS, new VWindowTrimEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_ADDONS, new VApplicationAddons(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_MENU_CONTRIBUTIONS, new VMenuContributionsEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_TOOLBAR_CONTRIBUTIONS, new VToolBarContributionsEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_TRIM_CONTRIBUTIONS, new VTrimContributionsEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_WINDOW_SHARED_ELEMENTS, new VWindowSharedElementsEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_MODEL_FRAGEMENTS, new VModelFragmentsEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_MODEL_IMPORTS, new VModelImportsEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_CATEGORIES, new VApplicationCategoriesEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_PARAMETERS, new VItemParametersEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_MENUELEMENTS, new VMenuElementsEditor(modelProvider.getEditingDomain(), this));
		registerVirtualEditor(VIRTUAL_ROOT_CONTEXTS, new VRootBindingContexts(modelProvider.getEditingDomain(), this));
	}

	private void registerVirtualEditor(String id, AbstractComponentEditor editor) {
		virtualEditors.put(id, editor);
	}

	public void setSelection(Object element) {
		viewer.setSelection(new StructuredSelection(element));
	}

	private void registerContributedEditors() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"editor".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				IEditorDescriptor desc = (IEditorDescriptor) el.createExecutableExtension("descriptorClass"); //$NON-NLS-1$
				EClass eClass = desc.getEClass();
				IContributionFactory fact = context.get(IContributionFactory.class);
				AbstractComponentEditor editor = (AbstractComponentEditor) fact.create("platform:/plugin/" + el.getContributor().getName() + "/" + desc.getEditorClass().getName(), context); //$NON-NLS-1$ //$NON-NLS-2$
				registerEditor(eClass, editor);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void registerDefaultEditors() {
		registerEditor(ApplicationPackageImpl.Literals.APPLICATION, new ApplicationEditor(modelProvider.getEditingDomain(), this));
		registerEditor(ApplicationPackageImpl.Literals.ADDON, new AddonsEditor(modelProvider.getEditingDomain(), this, project));

		registerEditor(CommandsPackageImpl.Literals.KEY_BINDING, new KeyBindingEditor(modelProvider.getEditingDomain(), this, modelProvider));
		registerEditor(CommandsPackageImpl.Literals.HANDLER, new HandlerEditor(modelProvider.getEditingDomain(), this, modelProvider, project));
		registerEditor(CommandsPackageImpl.Literals.COMMAND, new CommandEditor(modelProvider.getEditingDomain(), this));
		registerEditor(CommandsPackageImpl.Literals.COMMAND_PARAMETER, new CommandParameterEditor(modelProvider.getEditingDomain(), this));
		registerEditor(CommandsPackageImpl.Literals.PARAMETER, new ParameterEditor(modelProvider.getEditingDomain(), this));
		registerEditor(CommandsPackageImpl.Literals.BINDING_TABLE, new BindingTableEditor(modelProvider.getEditingDomain(), this));
		registerEditor(CommandsPackageImpl.Literals.BINDING_CONTEXT, new BindingContextEditor(modelProvider.getEditingDomain(), this));
		registerEditor(CommandsPackageImpl.Literals.CATEGORY, new CategoryEditor(modelProvider.getEditingDomain(), this));

		registerEditor(MenuPackageImpl.Literals.TOOL_BAR, new ToolBarEditor(modelProvider.getEditingDomain(), this));
		registerEditor(MenuPackageImpl.Literals.RENDERED_TOOL_BAR, new RenderedToolBarEditor(modelProvider.getEditingDomain(), this));
		registerEditor(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, new DirectToolItemEditor(modelProvider.getEditingDomain(), this, project));
		registerEditor(MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, new HandledToolItemEditor(modelProvider.getEditingDomain(), this, project, modelProvider));
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, new ToolBarSeparatorEditor(modelProvider.getEditingDomain(), this));
		registerEditor(MenuPackageImpl.Literals.TOOL_CONTROL, new ToolControlEditor(modelProvider.getEditingDomain(), this, project));

		registerEditor(MenuPackageImpl.Literals.MENU, new MenuEditor(modelProvider.getEditingDomain(), project, this));
		registerEditor(MenuPackageImpl.Literals.RENDERED_MENU, new RenderedMenuEditor(modelProvider.getEditingDomain(), project, this));
		registerEditor(MenuPackageImpl.Literals.POPUP_MENU, new PopupMenuEditor(modelProvider.getEditingDomain(), project, this));
		registerEditor(MenuPackageImpl.Literals.MENU_SEPARATOR, new MenuSeparatorEditor(modelProvider.getEditingDomain(), this));
		registerEditor(MenuPackageImpl.Literals.HANDLED_MENU_ITEM, new HandledMenuItemEditor(modelProvider.getEditingDomain(), this, project, modelProvider));
		registerEditor(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, new DirectMenuItemEditor(modelProvider.getEditingDomain(), this, project));
		registerEditor(MenuPackageImpl.Literals.RENDERED_MENU_ITEM, new RenderedMenuItem(modelProvider.getEditingDomain(), this, project));
		registerEditor(MenuPackageImpl.Literals.MENU_CONTRIBUTION, new MenuContributionEditor(modelProvider.getEditingDomain(), project, this));
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION, new ToolBarContributionEditor(modelProvider.getEditingDomain(), this));
		registerEditor(MenuPackageImpl.Literals.TRIM_CONTRIBUTION, new TrimContributionEditor(modelProvider.getEditingDomain(), this));

		registerEditor(UiPackageImpl.Literals.CORE_EXPRESSION, new CoreExpressionEditor(modelProvider.getEditingDomain(), this));

		registerEditor(BasicPackageImpl.Literals.PART, new PartEditor(modelProvider.getEditingDomain(), this, project));
		registerEditor(BasicPackageImpl.Literals.WINDOW, new WindowEditor(modelProvider.getEditingDomain(), this, project));
		registerEditor(BasicPackageImpl.Literals.TRIMMED_WINDOW, new TrimmedWindowEditor(modelProvider.getEditingDomain(), this, project));
		registerEditor(BasicPackageImpl.Literals.PART_SASH_CONTAINER, new PartSashContainerEditor(modelProvider.getEditingDomain(), this));
		registerEditor(AdvancedPackageImpl.Literals.AREA, new AreaEditor(modelProvider.getEditingDomain(), this, project));
		registerEditor(BasicPackageImpl.Literals.PART_STACK, new PartStackEditor(modelProvider.getEditingDomain(), this));
		registerEditor(BasicPackageImpl.Literals.INPUT_PART, new InputPartEditor(modelProvider.getEditingDomain(), this, project));
		registerEditor(BasicPackageImpl.Literals.TRIM_BAR, new TrimBarEditor(modelProvider.getEditingDomain(), this));

		registerEditor(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR, new PartDescriptorEditor(modelProvider.getEditingDomain(), this, project));

		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK, new PerspectiveStackEditor(modelProvider.getEditingDomain(), this));
		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE, new PerspectiveEditor(modelProvider.getEditingDomain(), project, this));
		registerEditor(AdvancedPackageImpl.Literals.PLACEHOLDER, new PlaceholderEditor(modelProvider.getEditingDomain(), this, modelProvider));

		registerEditor(FragmentPackageImpl.Literals.MODEL_FRAGMENTS, new ModelFragmentsEditor(modelProvider.getEditingDomain(), this));
		registerEditor(FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT, new StringModelFragment(modelProvider.getEditingDomain(), this));
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
		AbstractComponentEditor editor = editorMap.get(eClass);
		if (editor == null) {
			for (EClass cl : eClass.getESuperTypes()) {
				editor = getEditor(cl);
				if (editor != null) {
					return editor;
				}
			}
		}
		return editor;
	}

	@Persist
	public void doSave(@Optional IProgressMonitor monitor) {
		if (modelProvider.isSaveable()) {
			modelProvider.save();
		}
	}

	@Focus
	public void setFocus() {
		if (clipboardHandler == null) {
			clipboardHandler = new ClipboardHandler();
		}
		if (clipboardService != null) {
			clipboardService.setHandler(clipboardHandler);
		}
		viewer.getControl().setFocus();
	}

	@PreDestroy
	void dispose() {
		try {
			removeIcon.dispose();
			obsManager.dispose();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public IModelResource getModelProvider() {
		return modelProvider;
	}

	class ClipboardHandler implements Handler {

		@SuppressWarnings("unchecked")
		public void paste() {
			Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
			Object o = clip.getContents(MemoryTransfer.getInstance());
			clip.dispose();
			if (o == null) {
				return;
			}

			Object parent = ((IStructuredSelection) viewer.getSelection()).getFirstElement();

			EStructuralFeature feature = null;
			EObject container = null;
			if (parent instanceof VirtualEntry<?>) {
				VirtualEntry<?> v = (VirtualEntry<?>) parent;
				feature = ((IEMFProperty) v.getProperty()).getStructuralFeature();
				container = (EObject) v.getOriginalParent();
			} else {
				if (parent instanceof MElementContainer<?>) {
					feature = UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN;
					container = (EObject) parent;
				} else if (parent instanceof EObject) {
					container = (EObject) parent;
					EClass eClass = container.eClass();

					for (EStructuralFeature f : eClass.getEAllStructuralFeatures()) {
						if (ModelUtils.getTypeArgument(eClass, f.getEGenericType()).isInstance(o)) {
							feature = f;
							break;
						}
					}
				}
			}

			if (feature == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS && container != null) {
				MApplicationElement el = (MApplicationElement) EcoreUtil.create(((EObject) o).eClass());
				el.setElementId(((MApplicationElement) o).getElementId());
				Command cmd = AddCommand.create(getModelProvider().getEditingDomain(), container, feature, el);
				if (cmd.canExecute()) {
					getModelProvider().getEditingDomain().getCommandStack().execute(cmd);
				}
				return;
			}

			if (feature != null && container != null) {
				Command cmd = AddCommand.create(getModelProvider().getEditingDomain(), container, feature, o);
				if (cmd.canExecute()) {
					getModelProvider().getEditingDomain().getCommandStack().execute(cmd);
					if (isLiveModel()) {
						if (container instanceof MElementContainer<?> && o instanceof MUIElement) {
							((MElementContainer<MUIElement>) container).setSelectedElement((MUIElement) o);
						}
					}
				}
			}
		}

		public void copy() {
			Object o = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			if (o != null && o instanceof EObject) {
				Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
				clip.setContents(new Object[] { EcoreUtil.copy((EObject) o) }, new Transfer[] { MemoryTransfer.getInstance() });
				clip.dispose();
			}
		}

		public void cut() {
			Object o = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			if (o != null && o instanceof EObject) {
				Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
				clip.setContents(new Object[] { o }, new Transfer[] { MemoryTransfer.getInstance() });
				clip.dispose();
				EObject eObj = (EObject) o;
				Command cmd = RemoveCommand.create(getModelProvider().getEditingDomain(), eObj.eContainer(), eObj.eContainingFeature(), eObj);
				if (cmd.canExecute()) {
					getModelProvider().getEditingDomain().getCommandStack().execute(cmd);
				}
			}
		}
	}

	static class TreeStructureAdvisorImpl extends TreeStructureAdvisor {

	}

	class ObservableFactoryImpl implements IObservableFactory {

		public IObservable createObservable(Object target) {
			if (target instanceof IObservableList) {
				return (IObservable) target;
			} else if (target instanceof VirtualEntry<?>) {
				return ((VirtualEntry<?>) target).getList();
			} else {
				AbstractComponentEditor editor = getEditor(((EObject) target).eClass());
				if (editor != null) {
					return editor.getChildList(target);
				}
			}

			return null;
		}
	}

	static class DragListener extends DragSourceAdapter {
		private final TreeViewer viewer;

		public DragListener(TreeViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			event.doit = !selection.isEmpty() && selection.getFirstElement() instanceof MApplicationElement;
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			Object o = selection.getFirstElement();
			event.data = o;
		}
	}

	class DropListener extends ViewerDropAdapter {
		private EditingDomain domain;

		protected DropListener(Viewer viewer, EditingDomain domain) {
			super(viewer);
			this.domain = domain;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean performDrop(Object data) {
			if (getCurrentLocation() == LOCATION_ON) {
				EStructuralFeature feature = null;
				EObject parent = null;
				if (getCurrentTarget() instanceof MElementContainer<?>) {
					feature = UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN;
					parent = (EObject) getCurrentTarget();
				} else if (getCurrentTarget() instanceof VirtualEntry<?>) {
					VirtualEntry<?> entry = (VirtualEntry<?>) getCurrentTarget();
					IListProperty prop = entry.getProperty();
					if (prop instanceof IEMFProperty) {
						feature = ((IEMFProperty) prop).getStructuralFeature();
						parent = (EObject) entry.getOriginalParent();

					}
				} else if (getCurrentTarget() instanceof EObject) {
					parent = (EObject) getCurrentTarget();
					for (EStructuralFeature f : parent.eClass().getEAllStructuralFeatures()) {
						EClassifier cl = ModelUtils.getTypeArgument(parent.eClass(), f.getEGenericType());
						if (cl.isInstance(data)) {
							feature = f;
							break;
						}
					}
				}

				if (feature != null && parent != null) {
					Command cmd = AddCommand.create(domain, parent, feature, data);
					if (cmd.canExecute()) {
						domain.getCommandStack().execute(cmd);
						if (isLiveModel()) {
							if (parent instanceof MElementContainer<?> && data instanceof MUIElement) {
								((MElementContainer<MUIElement>) parent).setSelectedElement((MUIElement) data);
							}
						}
					}
				}
			} else if (getCurrentLocation() == LOCATION_AFTER || getCurrentLocation() == LOCATION_BEFORE) {
				EStructuralFeature feature = null;
				EObject parent = null;

				TreeItem item = (TreeItem) getCurrentEvent().item;
				if (item != null) {
					TreeItem parentItem = item.getParentItem();
					if (item != null) {
						if (parentItem.getData() instanceof VirtualEntry<?>) {
							VirtualEntry<?> vE = (VirtualEntry<?>) parentItem.getData();
							parent = (EObject) vE.getOriginalParent();
							feature = ((IEMFProperty) vE.getProperty()).getStructuralFeature();
						} else if (parentItem.getData() instanceof MElementContainer<?>) {
							parent = (EObject) parentItem.getData();
							feature = UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN;
						} else if (parentItem.getData() instanceof EObject) {
							parent = (EObject) parentItem.getData();
							for (EStructuralFeature f : parent.eClass().getEAllStructuralFeatures()) {
								EClassifier cl = ModelUtils.getTypeArgument(parent.eClass(), f.getEGenericType());
								if (cl.isInstance(data)) {
									feature = f;
									break;
								}
							}
						}
					}
				}

				if (feature == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS && parent != null) {
					MApplicationElement el = (MApplicationElement) EcoreUtil.create(((EObject) data).eClass());
					el.setElementId(((MApplicationElement) data).getElementId());
					Command cmd = AddCommand.create(domain, parent, feature, el);
					if (cmd.canExecute()) {
						domain.getCommandStack().execute(cmd);
					}
					return true;
				}

				if (feature != null && parent != null && parent.eGet(feature) instanceof List<?>) {
					List<Object> list = (List<Object>) parent.eGet(feature);
					int index = list.indexOf(getCurrentTarget());

					if (index >= list.size()) {
						index = CommandParameter.NO_INDEX;
					}

					if (parent == ((EObject) data).eContainer()) {
						if (parent instanceof MElementContainer<?> && data instanceof MUIElement) {
							Util.moveElementByIndex(domain, (MUIElement) data, isLiveModel(), index);
						} else {
							Command cmd = MoveCommand.create(domain, parent, feature, data, index);
							if (cmd.canExecute()) {
								domain.getCommandStack().execute(cmd);
								return true;
							}
						}
					} else {
						// Moving between different sources is always a copy
						if (parent.eResource() != ((EObject) data).eResource()) {
							data = EcoreUtil.copy((EObject) data);
						}

						Command cmd = AddCommand.create(domain, parent, feature, data, index);
						if (cmd.canExecute()) {
							domain.getCommandStack().execute(cmd);
							if (isLiveModel()) {
								if (parent instanceof MElementContainer<?> && data instanceof MUIElement) {
									((MElementContainer<MUIElement>) parent).setSelectedElement((MUIElement) data);
								}
							}

							return true;
						}
					}
				}
			}

			return false;
		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			boolean rv = false;
			if (getSelectedObject() instanceof MApplicationElement) {
				if (getCurrentLocation() == LOCATION_ON) {
					rv = isValidDrop(target, (MApplicationElement) getSelectedObject(), false);
				} else if (getCurrentLocation() == LOCATION_AFTER || getCurrentLocation() == LOCATION_BEFORE) {
					TreeItem item = (TreeItem) getCurrentEvent().item;
					if (item != null) {
						item = item.getParentItem();
						if (item != null) {
							rv = isValidDrop(item.getData(), (MApplicationElement) getSelectedObject(), true);
						}
					}
				}
			}

			return rv;
		}

		private boolean isValidDrop(Object target, MApplicationElement instance, boolean isIndex) {
			if (target instanceof MElementContainer<?>) {
				@SuppressWarnings("unchecked")
				MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) target;

				if (isIndex || !container.getChildren().contains(instance)) {
					EClassifier classifier = ModelUtils.getTypeArgument(((EObject) container).eClass(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN.getEGenericType());
					return classifier.isInstance(instance);
				}
			} else if (target instanceof VirtualEntry<?>) {
				@SuppressWarnings("unchecked")
				VirtualEntry<Object> vTarget = (VirtualEntry<Object>) target;
				if (isIndex || !vTarget.getList().contains(instance)) {
					if (vTarget.getProperty() instanceof IEMFProperty) {
						EStructuralFeature feature = ((IEMFProperty) vTarget.getProperty()).getStructuralFeature();
						EObject parent = (EObject) vTarget.getOriginalParent();
						EClassifier classifier = ModelUtils.getTypeArgument(parent.eClass(), feature.getEGenericType());
						return classifier.isInstance(instance);
					}

				}
			} else if (target instanceof EObject) {
				EObject eObj = (EObject) target;
				for (EStructuralFeature f : eObj.eClass().getEAllStructuralFeatures()) {
					EClassifier cl = ModelUtils.getTypeArgument(eObj.eClass(), f.getEGenericType());
					if (cl.isInstance(instance)) {
						return true;
					}
				}
			}

			return false;
		}
	}

}