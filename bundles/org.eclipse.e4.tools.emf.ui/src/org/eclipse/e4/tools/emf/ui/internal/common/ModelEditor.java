/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Wim Jongman <wim.jongman@remainsoftware.com> - Maintenance
 *     Marco Descher <marco@descher.at> - Bug395982, 426653, 422465
 *     Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 *     Steven Spungin <steven@spungin.tv> - Bug 396902, 431755, 431735, 424730, 424730, 391089, 437236, 437552
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.tools.emf.ui.common.AbstractElementEditorContribution;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.IEditorDescriptor;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.IExtensionLookup;
import org.eclipse.e4.tools.emf.ui.common.IModelExtractor;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.IScriptingSupport;
import org.eclipse.e4.tools.emf.ui.common.MemoryTransfer;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.AddonsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ApplicationEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.AreaEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.BindingContextEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.BindingTableEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CategoryEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CommandParameterEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CompositePartEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.CoreExpressionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DefaultEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DynamicMenuContributionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandlerEditor;
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
import org.eclipse.e4.tools.emf.ui.internal.common.component.StringModelFragment;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarContributionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolBarSeparatorEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ToolControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimBarEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimContributionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.TrimmedWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.WizardDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.EmfUtil;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IGotoObject;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.ListTab;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.XmiTab;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationAddons;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationCategoriesEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VBindingTableEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VCommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VHandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VItemParametersEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuContributionsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VModelFragmentsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VModelImportsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartDescriptor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartDescriptorMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPerspectiveControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPerspectiveWindowsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VRootBindingContexts;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VSnippetsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VToolBarContributionsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VTrimContributionsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowSharedElementsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowTrimEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VWindowWindowsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ExportIdsHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ExternalizeStringHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ProjectOSGiTranslationProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.EMFDocumentResourceMediator;
import org.eclipse.e4.tools.services.IClipboardService;
import org.eclipse.e4.tools.services.IClipboardService.Handler;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.tools.services.Translation;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.internal.copy.FilteredTree;
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
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

public class ModelEditor implements IGotoObject {
	private static final String ORG_ECLIPSE_E4_TOOLS_MODELEDITOR_FILTEREDTREE_ENABLED_XMITAB_DISABLED = "org.eclipse.e4.tools.modeleditor.filteredtree.enabled.xmitab.disabled";//$NON-NLS-1$

	public static final String CSS_CLASS_KEY = "org.eclipse.e4.ui.css.CssClassName"; //$NON-NLS-1$

	public static final String VIRTUAL_PART_MENU = ModelEditor.class.getName() + ".VIRTUAL_PART_MENU"; //$NON-NLS-1$
	public static final String VIRTUAL_HANDLER = ModelEditor.class.getName() + ".VIRTUAL_HANDLER"; //$NON-NLS-1$
	public static final String VIRTUAL_BINDING_TABLE = ModelEditor.class.getName() + ".VIRTUAL_BINDING_TABLE"; //$NON-NLS-1$
	public static final String VIRTUAL_COMMAND = ModelEditor.class.getName() + ".VIRTUAL_COMMAND"; //$NON-NLS-1$
	public static final String VIRTUAL_APPLICATION_WINDOWS = ModelEditor.class.getName() + ".VIRTUAL_APPLICATION_WINDOWS"; //$NON-NLS-1$
	public static final String VIRTUAL_PERSPECTIVE_WINDOWS = ModelEditor.class.getName() + ".VIRTUAL_PERSPECTIVE_WINDOWS"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOW_WINDOWS = ModelEditor.class.getName() + ".VIRTUAL_WINDOW_WINDOWS"; //$NON-NLS-1$
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
	public static final String VIRTUAL_PERSPECTIVE_CONTROLS = ModelEditor.class.getName() + "VIRTUAL_PERSPECTIVE_CONTROLS"; //$NON-NLS-1$
	public static final String VIRTUAL_SNIPPETS = ModelEditor.class.getName() + "VIRTUAL_SNIPPETS"; //$NON-NLS-1$

	public static final int TAB_FORM = 0;
	public static final int TAB_XMI = 1;
	public static final int TAB_LIST = 2;

	private Map<EClass, AbstractComponentEditor> editorMap = new HashMap<EClass, AbstractComponentEditor>();
	private Map<String, AbstractComponentEditor> virtualEditors = new HashMap<String, AbstractComponentEditor>();
	private Map<Class<?>, List<AbstractElementEditorContribution>> tabContributions = new HashMap<Class<?>, List<AbstractElementEditorContribution>>();
	private List<FeaturePath> labelFeaturePaths = new ArrayList<FeaturePath>();
	private List<IEditorFeature> editorFeatures = new ArrayList<IEditorFeature>();
	private List<IContributionClassCreator> contributionCreator = new ArrayList<IContributionClassCreator>();

	private TreeViewer viewer;
	private IModelResource modelProvider;
	private IProject project;

	@Inject
	ESelectionService selectionService;

	@Inject
	MApplication app;

	private IEclipseContext context;
	private boolean fragment;
	private Handler clipboardHandler;

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
	@Preference(nodePath = "org.eclipse.e4.tools.emf.ui")
	IEclipsePreferences preferences;

	@Inject
	@Optional
	private IExtensionLookup extensionLookup;

	@Inject
	@Translation
	private Messages messages;

	@Inject
	@Optional
	private IModelExtractor modelExtractor;

	private ObservablesManager obsManager;

	private final IResourcePool resourcePool;

	private EMFDocumentResourceMediator emfDocumentProvider;

	private AbstractComponentEditor currentEditor;

	private Listener keyListener;

	private CTabFolder editorTabFolder;

	private boolean mod1Down = false;

	private boolean saving;

	private ListTab listTab;

	private CTabItem tabItemXmi;

	private CTabItem tabItemList;

	private CTabItem tabItemTree;

	private XmiTab xmiTab;

	public ModelEditor(Composite composite, IEclipseContext context, IModelResource modelProvider, IProject project, final IResourcePool resourcePool) {
		this.resourcePool = resourcePool;
		this.modelProvider = modelProvider;
		this.project = project;
		this.context = context;
		this.context.set(ModelEditor.class, this);
		this.obsManager = new ObservablesManager();
		if (project != null) {
			ProjectOSGiTranslationProvider translationProvider = new ProjectOSGiTranslationProvider(project, (String) this.context.get(TranslationService.LOCALE)) {
				@Override
				protected void updateResourceBundle() {
					super.updateResourceBundle();
					if (viewer != null) {
						viewer.getControl().getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								viewer.refresh();
							}
						});
					}
				}
			};
			context.set(ProjectOSGiTranslationProvider.class, translationProvider);
		}
		labelFeaturePaths.add(FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		labelFeaturePaths.add(FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		// This is a workaround until Bug 437207 is merged.
		// @PersistState will not be invoked.
		composite.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				persistState();
			}
		});
	}

	@PersistState
	protected void persistState() {
		if (listTab != null) {
			listTab.saveSettings();
		}
	}

	@PostConstruct
	void postCreate(Composite composite) {
		if (project == null) {
			keyListener = new Listener() {

				@Override
				public void handleEvent(Event event) {
					if ((event.stateMask & SWT.ALT) == SWT.ALT) {
						findAndHighlight(context.get(Display.class).getFocusControl());
					}
				}
			};
			context.get(Display.class).addFilter(SWT.MouseUp, keyListener);
		}

		context.set(ModelEditor.class, this);
		context.set(IResourcePool.class, resourcePool);
		context.set(EditingDomain.class, modelProvider.getEditingDomain());
		context.set(IModelResource.class, modelProvider);

		if (project != null) {
			context.set(IProject.class, project);
		}

		loadEditorFeatures();
		registerDefaultEditors();
		registerVirtualEditors();

		registerContributedEditors();
		registerContributedVirtualEditors();
		registerContributedEditorTabs();
		loadContributionCreators();

		fragment = modelProvider.getRoot().get(0) instanceof MModelFragments;

		// For Bug 396902, create this before creating the Form tab
		emfDocumentProvider = new EMFDocumentResourceMediator(modelProvider);

		editorTabFolder = new CTabFolder(composite, SWT.BOTTOM);
		tabItemTree = new CTabItem(editorTabFolder, SWT.NONE);
		tabItemTree.setText(messages.ModelEditor_Form);
		tabItemTree.setControl(createFormTab(editorTabFolder));
		tabItemTree.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_application_form));

		tab_list_show(true);

		tabItemXmi = new CTabItem(editorTabFolder, SWT.NONE);
		tabItemXmi.setText(messages.ModelEditor_XMI);
		xmiTab = createXMITab(editorTabFolder);
		tabItemXmi.setControl(xmiTab);
		tabItemXmi.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_chart_organisation));
		editorTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (editorTabFolder.getSelectionIndex() == getTabIndex(tabItemXmi)) {
					emfDocumentProvider.updateFromEMF();
				}
			}
		});

		editorTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// When the list tab is visible, register the IViewEObjects
				// interface
				// This allows external commands to interact with the view.
				// Eventually, all 3 tabs, or even the ModelEditor itself, could
				// implement the interface.
				String key = "org.eclipse.e4.tools.active-object-viewer"; //$NON-NLS-1$
				if (listTab != null && editorTabFolder.getSelectionIndex() == getTabIndex(listTab.getTabItem())) {
					app.getContext().set(key, listTab);
				} else {
					app.getContext().set(key, null);
				}
			}
		});

		editorTabFolder.setSelection(0);
	}

	/**
	 * @param tabItem
	 * @return The index of the tab item. Should never return -1.
	 */
	static public int getTabIndex(CTabItem tabItem) {
		return Arrays.asList(tabItem.getParent().getItems()).indexOf(tabItem);
	}

	private void findAndHighlight(Control control) {
		if (control != null) {
			MApplicationElement m = findModelElement(control);
			MApplicationElement o = m;
			if (m != null) {
				List<MApplicationElement> l = new ArrayList<MApplicationElement>();
				do {
					l.add(m);
					m = (MApplicationElement) ((EObject) m).eContainer();
				} while (m != null);

				if (o instanceof MPart) {
					System.err.println(getClass().getName() + ".findAndHighLight: " + o); //$NON-NLS-1$
					System.err.println(getClass().getName() + ".findAndHighLight: " + ((EObject) o).eContainingFeature()); //$NON-NLS-1$
				}

				viewer.setSelection(new StructuredSelection(o));
			}
		}
	}

	private MApplicationElement findModelElement(Control control) {
		do {
			if (control.getData("modelElement") != null) { //$NON-NLS-1$
				return (MApplicationElement) control.getData("modelElement"); //$NON-NLS-1$
			}
			control = control.getParent();
		} while (control != null);

		return null;
	}

	private XmiTab createXMITab(Composite composite) {
		IEclipseContext childContext = context.createChild();
		childContext.set(Composite.class, composite);
		childContext.set(EMFDocumentResourceMediator.class, emfDocumentProvider);
		childContext.set(IEclipsePreferences.class, preferences);
		childContext.set(IResourcePool.class, resourcePool);
		XmiTab ret = ContextInjectionFactory.make(XmiTab.class, childContext);
		return ret;
	}

	private Composite createFormTab(Composite composite) {
		SashForm form = new SashForm(composite, SWT.HORIZONTAL);
		form.setBackground(form.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		viewer = createTreeViewerArea(form);

		Composite parent = new Composite(form, SWT.NONE);
		FillLayout l = new FillLayout();
		parent.setLayout(l);

		Composite editingArea = new Composite(parent, SWT.BORDER);
		GridLayout gl = new GridLayout();
		editingArea.setLayout(gl);

		Composite headerContainer = new Composite(editingArea, SWT.NONE);
		headerContainer.setLayout(new GridLayout(3, false));
		headerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label iconLabel = new Label(headerContainer, SWT.NONE);
		iconLabel.setLayoutData(new GridData(20, 20));

		final Label sectionHeaderLabel = new Label(headerContainer, SWT.NONE);
		sectionHeaderLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Label separatorLabel = new Label(headerContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		separatorLabel.setLayoutData(gridData);

		final StackLayout layout = new StackLayout();
		final Composite contentContainer = new Composite(editingArea, SWT.NONE);
		contentContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		contentContainer.setLayout(layout);

		viewer.getTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					List<EObject> list = new ArrayList<EObject>();
					final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					for (Object o : ((StructuredSelection) selection).toList()) {
						if (o instanceof EObject) {
							list.add((EObject) o);
						}
					}
					if (list.isEmpty() == false) {
						Command cmd = DeleteCommand.create(ModelEditor.this.modelProvider.getEditingDomain(), list);
						if (cmd.canExecute()) {
							ModelEditor.this.modelProvider.getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					final IStructuredSelection s = (IStructuredSelection) event.getSelection();
					if (s.getFirstElement() instanceof EObject) {
						EObject obj = (EObject) s.getFirstElement();
						final AbstractComponentEditor editor = getEditor(obj.eClass());
						if (editor != null) {
							currentEditor = editor;
							sectionHeaderLabel.setText(editor.getLabel(obj));
							iconLabel.setImage(editor.getImage(obj, iconLabel.getDisplay()));
							obsManager.runAndCollect(new Runnable() {

								@Override
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
							currentEditor = editor;
							sectionHeaderLabel.setText(editor.getLabel(entry));
							iconLabel.setImage(editor.getImage(entry, iconLabel.getDisplay()));
							obsManager.runAndCollect(new Runnable() {

								@Override
								public void run() {
									Composite comp = editor.getEditor(contentContainer, s.getFirstElement());
									comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
									layout.topControl = comp;
									contentContainer.layout(true);
								}

							});
						}
					}

					// Rectangle r = scrolling.getClientArea();
					// scrolling.setMinSize(contentContainer.computeSize(r.width,
					// SWT.DEFAULT));
					// scrolling.setOrigin(0, 0);
					// scrolling.layout(true, true);

					if (selectionService != null) {
						selectionService.setSelection(s.getFirstElement());
					}

				}
			}
		});

		form.setWeights(new int[] { 3, 4 });

		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
				final List listOfSelections = s.toList();
				int noSelected = listOfSelections.size();

				boolean addSeparator = false;
				if (!s.isEmpty() && noSelected == 1) {
					List<Action> actions;
					if (s.getFirstElement() instanceof VirtualEntry<?>) {
						actions = virtualEditors.get(((VirtualEntry<?>) s.getFirstElement()).getId()).getActions(s.getFirstElement());
						if (actions.size() > 0) {
							MenuManager addMenu = new MenuManager(messages.ModelEditor_AddChild);
							for (Action a : actions) {
								addSeparator = true;
								addMenu.add(a);
							}
							manager.add(addMenu);
						}

						actions = virtualEditors.get(((VirtualEntry<?>) s.getFirstElement()).getId()).getActionsImport(s.getFirstElement());
						if (actions.size() > 0) {
							MenuManager menu = new MenuManager("Import 3x");
							for (Action a : actions) {
								addSeparator = true;
								menu.add(a);
							}
							manager.add(menu);
						}

						if (addSeparator)
							manager.add(new Separator());

					} else {

						final EObject o = (EObject) s.getFirstElement();
						AbstractComponentEditor editor = getEditor(o.eClass());

						// Build Add Child menu
						if (editor != null) {
							actions = new ArrayList<Action>(editor.getActions(s.getFirstElement()));
						} else {
							actions = new ArrayList<Action>();
						}

						if (actions.size() > 0) {
							MenuManager addMenu = new MenuManager(messages.ModelEditor_AddChild);
							for (Action a : actions) {
								addSeparator = true;
								addMenu.add(a);
							}
							manager.add(addMenu);
						}

						// Build import menu
						if (editor != null) {
							actions = new ArrayList<Action>(editor.getActionsImport(s.getFirstElement()));
						} else {
							actions = new ArrayList<Action>();
						}

						if (actions.size() > 0) {
							// TODO WIM - extract nls
							MenuManager menu = new MenuManager("Import 3x");
							for (Action a : actions) {
								addSeparator = true;
								menu.add(a);
							}
							manager.add(menu);
						}

						if (o.eContainer() != null) {
							addSeparator = true;
							manager.add(new Action(messages.ModelEditor_Delete, ImageDescriptor.createFromImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_cross))) {
								@Override
								public void run() {
									Command cmd = DeleteCommand.create(ModelEditor.this.modelProvider.getEditingDomain(), o);
									if (cmd.canExecute()) {
										ModelEditor.this.modelProvider.getEditingDomain().getCommandStack().execute(cmd);
									}
								}
							});
						}
					}
				}
				// multi selection
				if (noSelected > 0) {
					if ((!isModelFragment()) && modelExtractor != null)
						manager.add(new Action(messages.ModelEditor_ExtractFragment, ImageDescriptor.createFromImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_ModelFragments))) {
							@Override
							public void run() {
								ArrayList<MApplicationElement> maes = new ArrayList<MApplicationElement>();
								for (Object objSelect : listOfSelections) {
									EObject container = null;
									if (objSelect instanceof VirtualEntry<?>) {

										VirtualEntry<?> ve = (VirtualEntry<?>) objSelect;
										container = (EObject) ve.getOriginalParent();
										IObservableList list = ve.getList();
										Iterator iterator = list.iterator();
										while (iterator.hasNext()) {
											maes.add((MApplicationElement) iterator.next());
										}

									} else {
										container = ((EObject) objSelect).eContainer();
										MApplicationElement objSelect2 = (MApplicationElement) objSelect;
										if (!(objSelect2 instanceof MApplication))
											maes.add(objSelect2);
										else {
											// can't extract application
											return;
										}

									}

									String containerId = ((MApplicationElement) container).getElementId();
									if (containerId == null || containerId.length() == 0) {
										MessageDialog.openError(viewer.getControl().getShell(), null, messages.ModelEditor_ExtractFragment_NoParentId);

										return;
									}

								}

								if (modelExtractor.extract(viewer.getControl().getShell(), project, maes)) {
									Command cmd = DeleteCommand.create(ModelEditor.this.modelProvider.getEditingDomain(), maes);
									if (cmd.canExecute()) {
										ModelEditor.this.modelProvider.getEditingDomain().getCommandStack().execute(cmd);
									}
								}

							};
						});

				}

				IExtensionRegistry registry = RegistryFactory.getRegistry();
				IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.scripting"); //$NON-NLS-1$
				final IConfigurationElement[] elements = extPoint.getConfigurationElements();

				if (elements.length > 0 && !s.isEmpty() && s.getFirstElement() instanceof MApplicationElement && noSelected == 1) {
					if (addSeparator) {
						manager.add(new Separator());
					}

					addSeparator = false;

					MenuManager scriptExecute = new MenuManager(messages.ModelEditor_Script);
					manager.add(scriptExecute);
					for (IConfigurationElement e : elements) {
						final IConfigurationElement le = e;
						scriptExecute.add(new Action(e.getAttribute("label")) { //$NON-NLS-1$
							@Override
							public void run() {
								try {
									MApplicationElement o = (MApplicationElement) s.getFirstElement();
									IScriptingSupport support = (IScriptingSupport) le.createExecutableExtension("class"); //$NON-NLS-1$
									IEclipseContext ctx = null;
									if (project == null) {
										if (o instanceof MContext) {
											ctx = ((MContext) o).getContext();
										} else {
											ctx = ModelUtils.getContainingContext(o);
										}
									}

									support.openEditor(viewer.getControl().getShell(), s.getFirstElement(), ctx);
								} catch (CoreException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
					}
				}

				if (project != null) {

					if (addSeparator) {
						manager.add(new Separator());
					}

					Action nlsAction = new Action(messages.ModelEditor_ExternalizeStrings) {
						@Override
						public void run() {
							ExternalizeStringHandler h = ContextInjectionFactory.make(ExternalizeStringHandler.class, context);
							ContextInjectionFactory.invoke(h, Execute.class, context);
						}
					};

					Action extIdAction = new Action(messages.ModelEditor_ExportIds) {
						@Override
						public void run() {
							ExportIdsHandler h = ContextInjectionFactory.make(ExportIdsHandler.class, context);
							ContextInjectionFactory.invoke(h, Execute.class, context);
						}
					};

					manager.add(nlsAction);
					manager.add(extIdAction);
				} else {
					if (addSeparator) {
						manager.add(new Separator());
					}

					if (s.getFirstElement() instanceof MUIElement) {
						final MUIElement el = (MUIElement) s.getFirstElement();
						if (el.getWidget() instanceof Control) {
							manager.add(new Action(messages.ModelEditor_ShowControl) {

								@Override
								public void run() {
									ControlHighlighter.show((Control) el.getWidget());
								}
							});

						}
					}

				}

				if (addSeparator) {
					manager.add(new Separator());
				}

				Action expandAction = new Action(messages.ModelEditor_ExpandSubtree) {
					@Override
					public void run() {
						if (!s.isEmpty()) {
							if (viewer.getExpandedState(s.getFirstElement())) {
								viewer.collapseToLevel(s.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
							} else {
								viewer.expandToLevel(s.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
							}
						}
					}
				};

				manager.add(expandAction);

				if (s.getFirstElement() instanceof EObject) {
					manager.add(new Separator());
					final EObject el = (EObject) s.getFirstElement();
					Action gotoXmiAction = new Action(Messages.ModelEditor_goto_xmi) {
						@Override
						public void run() {
							gotoEObject(TAB_XMI, el);
						}
					};
					manager.add(gotoXmiAction);

					if (listTab != null) {
						if (EmfUtil.getAttribute(el, "elementId") != null) { //$NON-NLS-1$
							Action gotoListAction = new Action(Messages.ModelEditor_goto_list) {
								@Override
								public void run() {
									gotoEObject(TAB_LIST, el);
								}
							};
							manager.add(gotoListAction);
						}
					}

				}
			}
		});

		// Save the stateMask
		viewer.getTree().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (mod1Down && (e.keyCode & SWT.MOD1) == SWT.MOD1) {
					mod1Down = false;
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (!mod1Down && (e.keyCode & SWT.MOD1) == SWT.MOD1) {
					mod1Down = true;
				}
			}
		});

		viewer.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				if (mod1Down) {
					viewer.getTree().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							viewer.expandToLevel(event.getElement(), AbstractTreeViewer.ALL_LEVELS);
						}
					});
				}
			}

			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				if (mod1Down) {
					viewer.getTree().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							viewer.collapseToLevel(event.getElement(), AbstractTreeViewer.ALL_LEVELS);
						}
					});
				}
			}
		});

		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		viewer.setSelection(new StructuredSelection(modelProvider.getRoot()));

		return form;
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

	private TreeViewer createTreeViewerArea(Composite parent) {

		final Composite treeArea = new Composite(parent, SWT.BORDER);

		treeArea.setLayout(new FillLayout());
		treeArea.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$
		treeArea.setBackgroundMode(SWT.INHERIT_DEFAULT);

		TreeViewer tempViewer = null;
		String property = System.getProperty(ORG_ECLIPSE_E4_TOOLS_MODELEDITOR_FILTEREDTREE_ENABLED_XMITAB_DISABLED);
		if (property != null || preferences.getBoolean("tab-form-search-show", false)) { //$NON-NLS-1$
			FilteredTree viewParent = new FilteredTree(treeArea, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(), true);
			tempViewer = viewParent.getViewer();
		} else {
			tempViewer = new TreeViewerEx(treeArea, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, emfDocumentProvider, modelProvider);
		}
		final TreeViewer viewer = tempViewer;

		viewer.setLabelProvider(new ComponentLabelProvider(this, messages));
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(new ObservableFactoryImpl(), new TreeStructureAdvisor() {
		});
		viewer.setContentProvider(contentProvider);

		final WritableSet clearedSet = new WritableSet();

		contentProvider.getKnownElements().addSetChangeListener(new ISetChangeListener() {

			@Override
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

				@Override
				public void handleMapChange(MapChangeEvent event) {
					viewer.update(event.diff.getChangedKeys().toArray(), null);
				}
			});
		}

		viewer.setInput(modelProvider.getRoot());
		viewer.setAutoExpandLevel(2);
		viewer.expandToLevel(viewer.getAutoExpandLevel());
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				TreeViewer viewer = (TreeViewer) event.getViewer();
				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
				Object selectedNode = thisSelection.getFirstElement();
				if (mod1Down) {
					if (viewer.getExpandedState(selectedNode))
						viewer.setExpandedState(selectedNode, false);
					else
						viewer.expandToLevel(selectedNode, AbstractTreeViewer.ALL_LEVELS);
				} else {
					viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
				}

			}
		});

		// Effect of filtered tree implementation (bug 391086)
		viewer.getTree().addTreeListener(new TreeAdapter() {
			@Override
			public void treeCollapsed(TreeEvent e) {
				viewer.expandToLevel(viewer.getAutoExpandLevel());
			}
		});

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
			AbstractComponentEditor editor = (AbstractComponentEditor) fact.create("bundleclass://" + el.getContributor().getName() + "/" + el.getAttribute("class"), context); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			registerVirtualEditor(el.getAttribute("id"), editor); //$NON-NLS-1$
		}
	}

	private void registerVirtualEditors() {
		registerVirtualEditor(VIRTUAL_PART_MENU, ContextInjectionFactory.make(VPartMenuEditor.class, context));
		registerVirtualEditor(VIRTUAL_HANDLER, ContextInjectionFactory.make(VHandlerEditor.class, context));
		registerVirtualEditor(VIRTUAL_BINDING_TABLE, ContextInjectionFactory.make(VBindingTableEditor.class, context));
		registerVirtualEditor(VIRTUAL_COMMAND, ContextInjectionFactory.make(VCommandEditor.class, context));
		registerVirtualEditor(VIRTUAL_APPLICATION_WINDOWS, ContextInjectionFactory.make(VApplicationWindowEditor.class, context));
		registerVirtualEditor(VIRTUAL_WINDOW_WINDOWS, ContextInjectionFactory.make(VWindowWindowsEditor.class, context));
		registerVirtualEditor(VIRTUAL_PERSPECTIVE_WINDOWS, ContextInjectionFactory.make(VPerspectiveWindowsEditor.class, context));
		registerVirtualEditor(VIRTUAL_WINDOW_CONTROLS, ContextInjectionFactory.make(VWindowControlEditor.class, context));
		registerVirtualEditor(VIRTUAL_PART_DESCRIPTORS, ContextInjectionFactory.make(VPartDescriptor.class, context));
		registerVirtualEditor(VIRTUAL_PARTDESCRIPTOR_MENU, ContextInjectionFactory.make(VPartDescriptorMenuEditor.class, context));
		registerVirtualEditor(VIRTUAL_TRIMMED_WINDOW_TRIMS, ContextInjectionFactory.make(VWindowTrimEditor.class, context));
		registerVirtualEditor(VIRTUAL_ADDONS, ContextInjectionFactory.make(VApplicationAddons.class, context));
		registerVirtualEditor(VIRTUAL_MENU_CONTRIBUTIONS, ContextInjectionFactory.make(VMenuContributionsEditor.class, context));
		registerVirtualEditor(VIRTUAL_TOOLBAR_CONTRIBUTIONS, ContextInjectionFactory.make(VToolBarContributionsEditor.class, context));
		registerVirtualEditor(VIRTUAL_TRIM_CONTRIBUTIONS, ContextInjectionFactory.make(VTrimContributionsEditor.class, context));
		registerVirtualEditor(VIRTUAL_WINDOW_SHARED_ELEMENTS, ContextInjectionFactory.make(VWindowSharedElementsEditor.class, context));
		registerVirtualEditor(VIRTUAL_MODEL_FRAGEMENTS, ContextInjectionFactory.make(VModelFragmentsEditor.class, context));
		registerVirtualEditor(VIRTUAL_MODEL_IMPORTS, ContextInjectionFactory.make(VModelImportsEditor.class, context));
		registerVirtualEditor(VIRTUAL_CATEGORIES, ContextInjectionFactory.make(VApplicationCategoriesEditor.class, context));
		registerVirtualEditor(VIRTUAL_PARAMETERS, ContextInjectionFactory.make(VItemParametersEditor.class, context));
		registerVirtualEditor(VIRTUAL_ROOT_CONTEXTS, ContextInjectionFactory.make(VRootBindingContexts.class, context));
		registerVirtualEditor(VIRTUAL_PERSPECTIVE_CONTROLS, ContextInjectionFactory.make(VPerspectiveControlEditor.class, context));
		registerVirtualEditor(VIRTUAL_SNIPPETS, ContextInjectionFactory.make(VSnippetsEditor.class, context));
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
				AbstractComponentEditor editor = (AbstractComponentEditor) fact.create("bundleclass://" + el.getContributor().getName() + "/" + desc.getEditorClass().getName(), context); //$NON-NLS-1$ //$NON-NLS-2$
				registerEditor(eClass, editor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void registerContributedEditorTabs() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$
		for (IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"editorTab".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				final Object o = el.createExecutableExtension("contribution"); //$NON-NLS-1$
				if (o instanceof AbstractElementEditorContribution) {
					AbstractElementEditorContribution contribution = (AbstractElementEditorContribution) o;
					ContextInjectionFactory.inject(contribution, context);
					Class<?> contribElem = contribution.getContributableTo();
					if (contribElem == null)
						continue;
					if (!tabContributions.containsKey(contribElem)) {
						tabContributions.put(contribElem, new ArrayList<AbstractElementEditorContribution>());
					}
					List<AbstractElementEditorContribution> res = tabContributions.get(contribElem);
					res.add(contribution);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public List<AbstractElementEditorContribution> getTabContributionsForClass(Class<?> clazz) {
		List<AbstractElementEditorContribution> ret = new ArrayList<AbstractElementEditorContribution>();
		for (Class<?> clasz : tabContributions.keySet()) {
			if (clasz.isAssignableFrom(clazz))
				ret.addAll(tabContributions.get(clasz));
		}
		return ret;
	}

	private void registerDefaultEditors() {
		System.err.println(getClass().getName() + ".registerDefaultEditors: " + resourcePool);

		registerEditor(ApplicationPackageImpl.Literals.APPLICATION, ContextInjectionFactory.make(ApplicationEditor.class, context));
		registerEditor(ApplicationPackageImpl.Literals.ADDON, ContextInjectionFactory.make(AddonsEditor.class, context));

		registerEditor(CommandsPackageImpl.Literals.KEY_BINDING, ContextInjectionFactory.make(KeyBindingEditor.class, context));
		registerEditor(CommandsPackageImpl.Literals.HANDLER, ContextInjectionFactory.make(HandlerEditor.class, context));
		registerEditor(CommandsPackageImpl.Literals.COMMAND, ContextInjectionFactory.make(CommandEditor.class, context));
		registerEditor(CommandsPackageImpl.Literals.COMMAND_PARAMETER, ContextInjectionFactory.make(CommandParameterEditor.class, context));
		registerEditor(CommandsPackageImpl.Literals.PARAMETER, ContextInjectionFactory.make(ParameterEditor.class, context));
		registerEditor(CommandsPackageImpl.Literals.BINDING_TABLE, ContextInjectionFactory.make(BindingTableEditor.class, context));
		registerEditor(CommandsPackageImpl.Literals.BINDING_CONTEXT, ContextInjectionFactory.make(BindingContextEditor.class, context));
		registerEditor(CommandsPackageImpl.Literals.CATEGORY, ContextInjectionFactory.make(CategoryEditor.class, context));

		registerEditor(MenuPackageImpl.Literals.TOOL_BAR, ContextInjectionFactory.make(ToolBarEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, ContextInjectionFactory.make(DirectToolItemEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, ContextInjectionFactory.make(HandledToolItemEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, ContextInjectionFactory.make(ToolBarSeparatorEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.TOOL_CONTROL, ContextInjectionFactory.make(ToolControlEditor.class, context));

		registerEditor(MenuPackageImpl.Literals.MENU, ContextInjectionFactory.make(MenuEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.POPUP_MENU, ContextInjectionFactory.make(PopupMenuEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.MENU_SEPARATOR, ContextInjectionFactory.make(MenuSeparatorEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.HANDLED_MENU_ITEM, ContextInjectionFactory.make(HandledMenuItemEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, ContextInjectionFactory.make(DirectMenuItemEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.MENU_CONTRIBUTION, ContextInjectionFactory.make(MenuContributionEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION, ContextInjectionFactory.make(ToolBarContributionEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.TRIM_CONTRIBUTION, ContextInjectionFactory.make(TrimContributionEditor.class, context));
		registerEditor(MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION, ContextInjectionFactory.make(DynamicMenuContributionEditor.class, context));

		registerEditor(UiPackageImpl.Literals.CORE_EXPRESSION, ContextInjectionFactory.make(CoreExpressionEditor.class, context));

		registerEditor(BasicPackageImpl.Literals.COMPOSITE_PART, ContextInjectionFactory.make(CompositePartEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.PART, ContextInjectionFactory.make(PartEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.WINDOW, ContextInjectionFactory.make(WindowEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.TRIMMED_WINDOW, ContextInjectionFactory.make(TrimmedWindowEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.PART_SASH_CONTAINER, ContextInjectionFactory.make(PartSashContainerEditor.class, context));
		registerEditor(AdvancedPackageImpl.Literals.AREA, ContextInjectionFactory.make(AreaEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.PART_STACK, ContextInjectionFactory.make(PartStackEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.TRIM_BAR, ContextInjectionFactory.make(TrimBarEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.DIALOG, ContextInjectionFactory.make(DialogEditor.class, context));
		registerEditor(BasicPackageImpl.Literals.WIZARD_DIALOG, ContextInjectionFactory.make(WizardDialogEditor.class, context));

		registerEditor(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR, ContextInjectionFactory.make(PartDescriptorEditor.class, context));

		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK, ContextInjectionFactory.make(PerspectiveStackEditor.class, context));
		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE, ContextInjectionFactory.make(PerspectiveEditor.class, context));
		registerEditor(AdvancedPackageImpl.Literals.PLACEHOLDER, ContextInjectionFactory.make(PlaceholderEditor.class, context));

		registerEditor(FragmentPackageImpl.Literals.MODEL_FRAGMENTS, ContextInjectionFactory.make(ModelFragmentsEditor.class, context));
		registerEditor(FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT, ContextInjectionFactory.make(StringModelFragment.class, context));
	}

	public void tab_list_show(Boolean show) {
		if (editorTabFolder == null) {
			return;
		}
		if (show == false) {
			if (listTab != null) {
				// remove the tab from the folder
				listTab.getTabItem().dispose();
				ContextInjectionFactory.uninject(listTab, listTab.getContext());
				listTab = null;
			}
		} else {
			if (listTab == null) {
				IEclipseContext child = context.createChild();
				child.set(CTabFolder.class, editorTabFolder);
				child.set(EMFDocumentResourceMediator.class, emfDocumentProvider);
				child.set(IGotoObject.class, this);
				child.set(Messages.class, messages);
				listTab = ContextInjectionFactory.make(ListTab.class, child);
				tabItemList = listTab.getTabItem();
			}
		}
	}

	@Inject
	public void setNotVisibleColor(@Preference("notVisibleColor") String color) {
		RGB current = JFaceResources.getColorRegistry().getRGB(ComponentLabelProvider.NOT_VISIBLE_KEY);

		if (current == null || !current.equals(color)) {
			JFaceResources.getColorRegistry().put(ComponentLabelProvider.NOT_VISIBLE_KEY, StringConverter.asRGB(color, new RGB(200, 200, 200)));
		}

		if (viewer != null) {
			viewer.refresh();
			viewer.getControl().redraw();
		}
	}

	@Inject
	public void setNotRenderedColor(@Preference("notRenderedColor") String color) {
		RGB current = JFaceResources.getColorRegistry().getRGB(ComponentLabelProvider.NOT_RENDERED_KEY);

		if (current == null || !current.equals(color)) {
			JFaceResources.getColorRegistry().put(ComponentLabelProvider.NOT_RENDERED_KEY, StringConverter.asRGB(color, new RGB(200, 200, 200)));
		}

		if (viewer != null) {
			viewer.refresh();
			viewer.getControl().redraw();
		}
	}

	@Inject
	public void setNotVisibleRenderedColor(@Preference("notVisibleAndRenderedColor") String color) {
		RGB current = JFaceResources.getColorRegistry().getRGB(ComponentLabelProvider.NOT_VISIBLE_AND_RENDERED_KEY);

		if (current == null || !current.equals(color)) {
			JFaceResources.getColorRegistry().put(ComponentLabelProvider.NOT_VISIBLE_AND_RENDERED_KEY, StringConverter.asRGB(color, new RGB(200, 200, 200)));
		}

		if (viewer != null) {
			viewer.refresh();
			viewer.getControl().redraw();
		}
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
			if (editor == null)
				editor = ContextInjectionFactory.make(DefaultEditor.class, context);
		}
		return editor;
	}

	@Persist
	public void doSave(@Optional IProgressMonitor monitor) {

		try {
			setSaving(true);
			if (modelProvider.isSaveable()) {
				modelProvider.save();
			}
		} finally {
			setSaving(false);
		}
	}

	private void setSaving(boolean saving) {
		this.saving = saving;
	}

	/**
	 * @return true if the editor is currently in the progress of saving.
	 */
	protected boolean isSaving() {
		return saving;
	}

	@Focus
	public void setFocus() {
		if (clipboardHandler == null) {
			clipboardHandler = new ClipboardHandler();
		}
		if (clipboardService != null) {
			clipboardService.setHandler(clipboardHandler);
		}
		// See bug 432095.
		// viewer.getControl().setFocus();
	}

	@PreDestroy
	void dispose() {
		try {
			obsManager.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (project == null) {
			context.get(Display.class).removeFilter(SWT.MouseUp, keyListener);
		}
		ContextInjectionFactory.uninject(xmiTab, xmiTab.getContext());
	}

	public IModelResource getModelProvider() {
		return modelProvider;
	}

	class ClipboardHandler implements Handler {

		@Override
		public void paste() {
			if (editorTabFolder.getSelectionIndex() == 0) {
				if (viewer.getControl().getDisplay().getFocusControl() == viewer.getControl()) {
					handleStructurePaste();
				} else if (currentEditor != null) {
					currentEditor.handlePaste();
				}
			} else {
				xmiTab.paste();
			}
		}

		@SuppressWarnings("unchecked")
		private void handleStructurePaste() {
			Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
			Object o = clip.getContents(MemoryTransfer.getInstance());
			clip.dispose();
			if (o == null) {
				return;
			}

			o = EcoreUtil.copy((EObject) o);

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

		@Override
		public void copy() {
			if (editorTabFolder.getSelectionIndex() == 0) {
				if (viewer.getControl().getDisplay().getFocusControl() == viewer.getControl()) {
					handleStructureCopy();
				} else if (currentEditor != null) {
					currentEditor.handleCopy();
				}
			} else {
				xmiTab.copy();
			}
		}

		private void handleStructureCopy() {
			Object o = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			if (o != null && o instanceof EObject) {
				Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
				clip.setContents(new Object[] { EcoreUtil.copy((EObject) o) }, new Transfer[] { MemoryTransfer.getInstance() });
				clip.dispose();
			}
		}

		@Override
		public void cut() {
			if (editorTabFolder.getSelectionIndex() == 0) {
				if (viewer.getControl().getDisplay().getFocusControl() == viewer.getControl()) {
					handleStructureCut();
				} else if (currentEditor != null) {
					currentEditor.handleCut();
				}
			} else {
				xmiTab.cut();
			}
		}

		private void handleStructureCut() {
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

	class ObservableFactoryImpl implements IObservableFactory {

		@Override
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

	@Override
	public void gotoEObject(int targetHint, EObject object) {
		if (object == null) {
			// do nothing
		} else {
			switch (targetHint) {
			case TAB_FORM:
				// make sure tree node has been instantiated
				ObservableListTreeContentProvider provider = (ObservableListTreeContentProvider) viewer.getContentProvider();
				getFirstMatchingItem(object, provider, provider.getChildren(viewer.getInput()));

				viewer.reveal(object);
				viewer.setSelection(new StructuredSelection(object));
				editorTabFolder.setSelection(getTabIndex(tabItemTree));
				break;
			case TAB_XMI:
				editorTabFolder.setSelection(getTabIndex(tabItemXmi));
				// model was not updating in XMI document (selection listener
				// was not firing from programmatic setSelection()
				emfDocumentProvider.updateFromEMF();

				try {
					xmiTab.gotoEObject(object);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case TAB_LIST:
				if (tabItemList != null && listTab != null) {
					editorTabFolder.setSelection(getTabIndex(tabItemList));
					listTab.getViewer().setSelection(new StructuredSelection(object), true);
				}
				break;
			default:
				break;
			}
			// }
		}
	}

	// This will ensure the provider has created the tree node (so we can reveal
	// it).
	private Object getFirstMatchingItem(EObject target, ObservableListTreeContentProvider provider, Object[] items) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == target) {
				return items[i];
			}
			Object found = getFirstMatchingItem(target, provider, provider.getChildren(items[i]));
			if (found != null) {
				return found;
			}
		}
		return null;
	}

}
