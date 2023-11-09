/*******************************************************************************
 * Copyright (c) 2010, 2022 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Wim Jongman <wim.jongman@remainsoftware.com> - Maintenance
 * Marco Descher <marco@descher.at> - Bug395982, 426653, 422465, 429674
 * Lars Vogel <Lars.Vogel@gmail.com> - Ongoing maintenance
 * Steven Spungin <steven@spungin.tv> - Bug 396902, 431755, 431735, 424730, 424730, 391089, 437236, 437552, Ongoing
 * Maintenance
 * Simon Scholz <simon.scholz@vogella.com> - Bug 475365
 * Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 472706, 429684, 531451
 * Dmitry Spiridenok <d.spiridenok@gmail.com> - Bug 429684
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
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
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
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
import org.eclipse.e4.tools.emf.ui.common.ModelEditorPreferences;
import org.eclipse.e4.tools.emf.ui.common.Plugin;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
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
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DirectToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.DynamicMenuContributionEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledMenuItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandledToolItemEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.HandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ImperativeExpressionEditor;
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
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.EmfUtil;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IGotoObject;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.ListTab;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.XmiTab;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationAddons;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationCategoriesEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VApplicationWindowEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VBindingTableEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VCommandEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VControlsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VHandlerEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VItemParametersEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VMenuContributionsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VModelFragmentsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VModelImportsEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartDescriptor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartDescriptorMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartDescriptorTrimEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartMenuEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPartTrimEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPerspectiveControlEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.virtual.VPerspectiveTrimEditor;
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
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class ModelEditor implements IGotoObject {
	private static final String ORG_ECLIPSE_E4_TOOLS_MODELEDITOR_FILTEREDTREE_ENABLED_XMITAB_DISABLED = "org.eclipse.e4.tools.modeleditor.filteredtree.enabled.xmitab.disabled";//$NON-NLS-1$

	public static final String CSS_CLASS_KEY = "org.eclipse.e4.ui.css.CssClassName"; //$NON-NLS-1$

	public static final String VIRTUAL_PART_MENU = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PART_MENU"; //$NON-NLS-1$
	public static final String VIRTUAL_HANDLER = "org.eclipse.e4.tools.emf.ui.VIRTUAL_HANDLER"; //$NON-NLS-1$
	public static final String VIRTUAL_CONTROLS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_CONTROLS"; //$NON-NLS-1$
	public static final String VIRTUAL_BINDING_TABLE = "org.eclipse.e4.tools.emf.ui.VIRTUAL_BINDING_TABLE"; //$NON-NLS-1$
	public static final String VIRTUAL_COMMAND = "org.eclipse.e4.tools.emf.ui.VIRTUAL_COMMAND"; //$NON-NLS-1$
	public static final String VIRTUAL_APPLICATION_WINDOWS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_APPLICATION_WINDOWS"; //$NON-NLS-1$
	public static final String VIRTUAL_PERSPECTIVE_WINDOWS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PERSPECTIVE_WINDOWS"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOW_WINDOWS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_WINDOW_WINDOWS"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOW_CONTROLS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_WINDOW_CONTROLS"; //$NON-NLS-1$
	public static final String VIRTUAL_PART_TRIMS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PART_TRIMS"; //$NON-NLS-1$
	public static final String VIRTUAL_PART_DESCRIPTORS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PART_DESCRIPTORS"; //$NON-NLS-1$
	public static final String VIRTUAL_PARTDESCRIPTOR_MENU = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PARTDESCRIPTOR_MENU"; //$NON-NLS-1$
	public static final String VIRTUAL_PARTDESCRIPTOR_TRIMS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PART_DESCRIPTOR_TRIMS"; //$NON-NLS-1$
	public static final String VIRTUAL_TRIMMED_WINDOW_TRIMS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_TRIMMED_WINDOW_TRIMS"; //$NON-NLS-1$
	public static final String VIRTUAL_ADDONS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_ADDONS"; //$NON-NLS-1$
	public static final String VIRTUAL_MENU_CONTRIBUTIONS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_MENU_CONTRIBUTIONS"; //$NON-NLS-1$
	public static final String VIRTUAL_TOOLBAR_CONTRIBUTIONS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_TOOLBAR_CONTRIBUTIONS"; //$NON-NLS-1$
	public static final String VIRTUAL_TRIM_CONTRIBUTIONS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_TRIM_CONTRIBUTIONS"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOW_SHARED_ELEMENTS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_WINDOW_SHARED_ELEMENTS"; //$NON-NLS-1$
	public static final String VIRTUAL_WINDOW_SNIPPETS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_WINDOW_SNIPPETS"; //$NON-NLS-1$

	public static final String VIRTUAL_MODEL_FRAGEMENTS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_MODEL_FRAGEMENTS"; //$NON-NLS-1$
	public static final String VIRTUAL_MODEL_IMPORTS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_MODEL_IMPORTS"; //$NON-NLS-1$
	public static final String VIRTUAL_CATEGORIES = "org.eclipse.e4.tools.emf.ui.VIRTUAL_CATEGORIES"; //$NON-NLS-1$
	public static final String VIRTUAL_PARAMETERS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PARAMETERS"; //$NON-NLS-1$
	public static final String VIRTUAL_MENUELEMENTS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_MENUELEMENTS"; //$NON-NLS-1$
	public static final String VIRTUAL_ROOT_CONTEXTS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_ROOT_CONTEXTS"; //$NON-NLS-1$
	public static final String VIRTUAL_PERSPECTIVE_CONTROLS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PERSPECTIVE_CONTROLS"; //$NON-NLS-1$
	public static final String VIRTUAL_PERSPECTIVE_TRIMS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_PERSPECTIVE_TRIMS"; //$NON-NLS-1$
	public static final String VIRTUAL_SNIPPETS = "org.eclipse.e4.tools.emf.ui.VIRTUAL_SNIPPETS"; //$NON-NLS-1$

	public static final int TAB_FORM = 0;
	public static final int TAB_XMI = 1;
	public static final int TAB_LIST = 2;
	static final String key = "org.eclipse.e4.tools.active-object-viewer"; //$NON-NLS-1$



	/**
	 * A map with key = eClass name or virtual key, value is an
	 * AbstractComponentEditor instance This map is filled on the fly when getting
	 * editors
	 */
	private final Map<String, AbstractComponentEditor<?>> editors = new HashMap<>();

	/**
	 * A map with key = eClass name or virtual key, value is a class of
	 * AbstractComponentEditor. This map is filled on init by registerEditor
	 */
	private final Map<String, Class<? extends AbstractComponentEditor<?>>> editorsClasses = new HashMap<>();

	private final Map<Class<?>, List<AbstractElementEditorContribution>> tabContributions = new HashMap<>();
	private final List<FeaturePath> labelFeaturePaths = new ArrayList<>();
	private final List<IEditorFeature> editorFeatures = new ArrayList<>();
	private final List<IContributionClassCreator> contributionCreator = new ArrayList<>();

	private TreeViewer viewer;
	private final IModelResource modelProvider;
	private final IProject project;

	/** An imageRegistry for dynamic component images (see bug #403583) */
	private final ImageRegistry componentImages = new ImageRegistry();

	@Inject
	ESelectionService selectionService;

	@Inject
	MApplication app;

	private final IEclipseContext context;
	private boolean fragment;
	private Handler clipboardHandler;

	@Inject
	@Optional
	private IClipboardService clipboardService;

	@Inject
	@Preference(nodePath = Plugin.ID, value = ModelEditorPreferences.AUTO_CREATE_ELEMENT_ID)
	private boolean autoCreateElementId;

	@Inject
	@Preference(nodePath = Plugin.ID, value = ModelEditorPreferences.SHOW_XMI_ID)
	private boolean showXMIId;

	@Inject
	@Preference(nodePath = Plugin.ID)
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

	@Inject
	@Optional
	MPart currentPart;

	@Inject
	private Logger logger;

	private final ObservablesManager obsManager;

	private final IResourcePool resourcePool;

	private EMFDocumentResourceMediator emfDocumentProvider;

	private AbstractComponentEditor<?> currentEditor;

	private Listener keyListener;

	private CTabFolder editorTabFolder;

	private boolean mod1Down = false;

	private boolean saving;

	private ListTab listTab;

	private CTabItem tabItemXmi;

	private CTabItem tabItemList;

	private CTabItem tabItemTree;

	private XmiTab xmiTab;

	private Section headerContainer;

	public ModelEditor(Composite composite, IEclipseContext context, IModelResource modelProvider, IProject project,
			final IResourcePool resourcePool) {
		this.resourcePool = resourcePool;
		this.modelProvider = modelProvider;
		this.project = project;
		this.context = context;
		this.context.set(ModelEditor.class, this);
		obsManager = new ObservablesManager();
		if (project != null) {
			String localeString = ""; //$NON-NLS-1$
			final Object object = this.context.get(TranslationService.LOCALE);
			// TODO simplify once we break e4 tools
			// compatibility with Luna, in Luna we representation has changed to
			// be Locale instead of String
			if (object != null) {
				localeString = object.toString();
			}
			final ProjectOSGiTranslationProvider translationProvider = new ProjectOSGiTranslationProvider(project,
					localeString) {
				@Override
				protected void updateResourceBundle() {
					super.updateResourceBundle();
					if (viewer != null) {
						viewer.getControl().getDisplay().asyncExec(() -> viewer.refresh());
					}
				}
			};
			context.set(ProjectOSGiTranslationProvider.class, translationProvider);
		}
		labelFeaturePaths.add(FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED));
		labelFeaturePaths.add(FeaturePath.fromList(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE));

		// This is a workaround until Bug 437207 is merged.
		// @PersistState will not be invoked.
		composite.addDisposeListener(arg0 -> persistState());
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
			keyListener = event -> {
				if ((event.stateMask & SWT.ALT) == SWT.ALT) {
					findAndHighlight(context.get(Display.class).getFocusControl());
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

		tabListShow(true);

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
					gotoEObject(TAB_XMI, null);
				}
				// When the list tab is visible, register the IViewEObjects
				// interface
				// This allows external commands to interact with the view.
				// Eventually, all 3 tabs, or even the ModelEditor itself, could
				// implement the interface.
				else if (listTab != null && editorTabFolder.getSelectionIndex() == getTabIndex(listTab.getTabItem())) {
					gotoEObject(TAB_LIST, null);
					app.getContext().set(key, listTab);
				} else {
					gotoEObject(TAB_FORM, null);
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
	public static int getTabIndex(CTabItem tabItem) {
		return Arrays.asList(tabItem.getParent().getItems()).indexOf(tabItem);
	}

	private void findAndHighlight(Control control) {
		if (control != null) {
			MApplicationElement m = findModelElement(control);
			final MApplicationElement o = m;
			if (m != null) {
				final List<MApplicationElement> l = new ArrayList<>();
				do {
					l.add(m);
					m = (MApplicationElement) ((EObject) m).eContainer();
				} while (m != null);

				if (o instanceof MPart) {
					System.err.println(getClass().getName() + ".findAndHighLight: " + o); //$NON-NLS-1$
					System.err
					.println(getClass().getName() + ".findAndHighLight: " + ((EObject) o).eContainingFeature()); //$NON-NLS-1$
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
		final IEclipseContext childContext = context.createChild();
		childContext.set(Composite.class, composite);
		childContext.set(EMFDocumentResourceMediator.class, emfDocumentProvider);
		childContext.set(IEclipsePreferences.class, preferences);
		childContext.set(IResourcePool.class, resourcePool);
		return ContextInjectionFactory.make(XmiTab.class, childContext);
	}

	private Composite createFormTab(Composite composite) {
		SashForm form = new SashForm(composite, SWT.HORIZONTAL);

		viewer = createTreeViewerArea(form);

		FormToolkit toolkit = new FormToolkit(form.getDisplay());

		headerContainer = toolkit.createSection(form, ExpandableComposite.TITLE_BAR);
		headerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		// Composite for storing the data
		Composite contentContainer = toolkit.createComposite(headerContainer, SWT.NONE);
		headerContainer.setClient(contentContainer);

		StackLayout layout = new StackLayout();
		contentContainer.setLayout(layout);

		form.setWeights(2, 5);

		viewer.getTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					final List<EObject> list = new ArrayList<>();
					final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					for (final Object o : ((StructuredSelection) selection).toList()) {
						if (o instanceof EObject) {
							list.add((EObject) o);
						}
					}
					if (!list.isEmpty()) {
						final Command cmd = DeleteCommand.create(modelProvider.getEditingDomain(), list);
						if (cmd.canExecute()) {
							modelProvider.getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			}
		});
		viewer.addSelectionChangedListener(event -> {
			if (!event.getSelection().isEmpty()) {
				final IStructuredSelection s = (IStructuredSelection) event.getSelection();
				if (s.getFirstElement() instanceof EObject) {
					final EObject obj = (EObject) s.getFirstElement();
					final AbstractComponentEditor<?> editor1 = getEditor(obj.eClass());
					if (editor1 != null) {
						currentEditor = editor1;
						headerContainer.setText(editor1.getLabel(obj));
						obsManager.runAndCollect(() -> {
							final Composite comp = editor1.getEditor(contentContainer, s.getFirstElement());
							layout.topControl = comp;
							contentContainer.layout(true);
						});
					}
				} else {
					final VirtualEntry<?, ?> entry = (VirtualEntry<?, ?>) s.getFirstElement();
					final AbstractComponentEditor<?> editor2 = getEditor(entry.getId());
					if (editor2 != null) {
						currentEditor = editor2;
						headerContainer.setText(editor2.getLabel(entry));
						obsManager.runAndCollect(() -> {
							final Composite comp = editor2.getEditor(contentContainer, s.getFirstElement());
							layout.topControl = comp;
							contentContainer.layout(true);
						});
					}
				}

				if (selectionService != null) {
					selectionService.setSelection(s.getFirstElement());
				}
			}
		});

		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(manager -> {
			final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			final List<?> listOfSelections = s.toList();
			final int noSelected = listOfSelections.size();

			boolean addSeparator = false;
			// single selection
			if (!s.isEmpty() && noSelected == 1) {
				List<Action> actions;
				if (s.getFirstElement() instanceof VirtualEntry) {
					actions = getEditor(((VirtualEntry<?, ?>) s.getFirstElement()).getId())
							.getActions(s.getFirstElement());
					if (!actions.isEmpty()) {
						final MenuManager addMenu1 = new MenuManager(messages.ModelEditor_AddChild);
						for (final Action a1 : actions) {
							addSeparator = true;
							addMenu1.add(a1);
						}
						manager.add(addMenu1);
					}

					actions = getEditor(((VirtualEntry<?, ?>) s.getFirstElement()).getId())
							.getActionsImport(s.getFirstElement());
					if (!actions.isEmpty()) {
						final MenuManager menu1 = new MenuManager(messages.ModelEditor_Import3x);
						for (final Action a2 : actions) {
							addSeparator = true;
							menu1.add(a2);
						}
						manager.add(menu1);
					}

					if (addSeparator) {
						manager.add(new Separator());
					}

				} else {

					final EObject o = (EObject) s.getFirstElement();
					final AbstractComponentEditor<?> editor = getEditor(o.eClass());

					// Build Add Child menu
					if (editor != null) {
						actions = new ArrayList<>(editor.getActions(s.getFirstElement()));
					} else {
						actions = new ArrayList<>();
					}

					if (!actions.isEmpty()) {
						final MenuManager addMenu2 = new MenuManager(messages.ModelEditor_AddChild);
						for (final Action a3 : actions) {
							addSeparator = true;
							addMenu2.add(a3);
						}
						manager.add(addMenu2);
					}

					// Build import menu
					if (editor != null) {
						actions = new ArrayList<>(editor.getActionsImport(s.getFirstElement()));
					} else {
						actions = new ArrayList<>();
					}

					if (!actions.isEmpty()) {
						// TODO WIM - extract nls
						final MenuManager menu2 = new MenuManager(messages.ModelEditor_Import3x);
						for (final Action a4 : actions) {
							addSeparator = true;
							menu2.add(a4);
						}
						manager.add(menu2);
					}

				}
			}

			// single & multi selection
			if (noSelected > 0) {

				// add delete entry if there are no virtual entries in selection all selected
				// elements have a container
				if (listOfSelections.stream().noneMatch(VirtualEntry.class::isInstance)
						&& listOfSelections.stream().filter(EObject.class::isInstance).map(EObject.class::cast)
						.map(EObject::eContainer).allMatch(Objects::nonNull)) {
					addSeparator = true;
					manager.add(new Action(messages.ModelEditor_Delete, ImageDescriptor
							.createFromImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_cross))) {
						@Override
						public void run() {
							final CompoundCommand cmd = new CompoundCommand();
							EditingDomain editingDomain = modelProvider.getEditingDomain();
							listOfSelections.forEach(o -> cmd.append(DeleteCommand.create(editingDomain, o)));
							if (cmd.canExecute() && !cmd.isEmpty()) {
								editingDomain.getCommandStack().execute(cmd);
							}
						}
					});
				}

				if (!isModelFragment() && modelExtractor != null) {
					manager.add(new Action(messages.ModelEditor_ExtractFragment, ImageDescriptor
							.createFromImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_ModelFragments))) {
						@Override
						public void run() {
							final ArrayList<MApplicationElement> maes = new ArrayList<>();
							for (final Object objSelect : listOfSelections) {
								EObject container = null;
								if (objSelect instanceof VirtualEntry) {

									@SuppressWarnings("unchecked")
									final VirtualEntry<EObject, MApplicationElement> ve = (VirtualEntry<EObject, MApplicationElement>) objSelect;
									container = ve.getOriginalParent();
									final IObservableList<MApplicationElement> list = ve.getList();
									final Iterator<MApplicationElement> iterator = list.iterator();
									while (iterator.hasNext()) {
										maes.add(iterator.next());
									}

								} else {
									container = ((EObject) objSelect).eContainer();
									final MApplicationElement objSelect2 = (MApplicationElement) objSelect;
									if (!(objSelect2 instanceof MApplication)) {
										maes.add(objSelect2);
									} else {
										// can't extract application
										return;
									}

								}

								final String containerId = ((MApplicationElement) container).getElementId();
								if (containerId == null || containerId.length() == 0) {
									MessageDialog.openError(viewer.getControl().getShell(), null,
											messages.ModelEditor_ExtractFragment_NoParentId);

									return;
								}

							}

							if (modelExtractor.extract(viewer.getControl().getShell(), project, maes)) {
								final Command cmd = DeleteCommand.create(modelProvider.getEditingDomain(), maes);
								if (cmd.canExecute()) {
									modelProvider.getEditingDomain().getCommandStack().execute(cmd);
								}
							}

						}
					});
				}


			}

			final IExtensionRegistry registry = RegistryFactory.getRegistry();
			final IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.scripting"); //$NON-NLS-1$
			final IConfigurationElement[] elements = extPoint.getConfigurationElements();

			if (elements.length > 0 && !s.isEmpty() && s.getFirstElement() instanceof MApplicationElement
					&& noSelected == 1) {
				if (addSeparator) {
					manager.add(new Separator());
				}

				addSeparator = false;

				final MenuManager scriptExecute = new MenuManager(messages.ModelEditor_Script);
				manager.add(scriptExecute);
				for (final IConfigurationElement e : elements) {
					final IConfigurationElement le = e;
					scriptExecute.add(new Action(e.getAttribute("label")) { //$NON-NLS-1$
						@Override
						public void run() {
							try {
								final MApplicationElement o = (MApplicationElement) s.getFirstElement();
								final IScriptingSupport support = (IScriptingSupport) le
										.createExecutableExtension("class"); //$NON-NLS-1$
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
								logger.warn(e);
							}
						}
					});
				}
			}

			if (project != null) {

				if (addSeparator) {
					manager.add(new Separator());
				}

				final Action nlsAction = new Action(messages.ModelEditor_ExternalizeStrings) {
					@Override
					public void run() {
						final ExternalizeStringHandler h = ContextInjectionFactory.make(ExternalizeStringHandler.class,
								context);
						ContextInjectionFactory.invoke(h, Execute.class, context);
					}
				};

				final Action extIdAction = new Action(messages.ModelEditor_ExportIds) {
					@Override
					public void run() {
						final ExportIdsHandler h = ContextInjectionFactory.make(ExportIdsHandler.class, context);
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
					final MUIElement el1 = (MUIElement) s.getFirstElement();
					if (el1.getWidget() instanceof Control) {
						manager.add(new Action(messages.ModelEditor_ShowControl) {

							@Override
							public void run() {
								ControlHighlighter.show((Control) el1.getWidget());
							}
						});

					}
				}

			}

			if (addSeparator) {
				manager.add(new Separator());
			}

			final Action expandAction = new Action(messages.ModelEditor_ExpandSubtree) {
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
				final EObject el2 = (EObject) s.getFirstElement();
				final Action gotoXmiAction = new Action(messages.ModelEditor_goto_xmi) {
					@Override
					public void run() {
						gotoEObject(TAB_XMI, el2);
					}
				};
				manager.add(gotoXmiAction);

				if (listTab != null) {
					if (EmfUtil.getAttribute(el2, "elementId") != null) { //$NON-NLS-1$
						final Action gotoListAction = new Action(messages.ModelEditor_goto_list) {
							@Override
							public void run() {
								gotoEObject(TAB_LIST, el2);
							}
						};
						manager.add(gotoListAction);
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
					viewer.getTree().getDisplay()
					.asyncExec(() -> viewer.expandToLevel(event.getElement(), AbstractTreeViewer.ALL_LEVELS));
				}
			}

			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				if (mod1Down) {
					viewer.getTree().getDisplay()
					.asyncExec(() -> viewer.collapseToLevel(event.getElement(), AbstractTreeViewer.ALL_LEVELS));
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

	public ImageRegistry getComponentImages() {
		return componentImages;
	}

	public boolean isShowXMIId() {
		return showXMIId;
	}

	private void loadContributionCreators() {
		final IExtensionRegistry registry = RegistryFactory.getRegistry();
		final IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (final IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"contributionClassCreator".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				contributionCreator.add((IContributionClassCreator) el.createExecutableExtension("class")); //$NON-NLS-1$
			} catch (final CoreException e) {
				logger.warn(e);
			}
		}
	}

	public IContributionClassCreator getContributionCreator(EClass eClass) {
		for (final IContributionClassCreator c : contributionCreator) {
			if (c.isSupported(eClass)) {
				return c;
			}
		}
		return null;
	}

	private void loadEditorFeatures() {
		final IExtensionRegistry registry = RegistryFactory.getRegistry();
		final IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (final IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"editorfeature".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				editorFeatures.add((IEditorFeature) el.createExecutableExtension("class")); //$NON-NLS-1$
			} catch (final CoreException e) {
				logger.warn(e);
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
		final List<FeatureClass> list = new ArrayList<>();

		for (final IEditorFeature f : editorFeatures) {
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
		final String property = System
				.getProperty(ORG_ECLIPSE_E4_TOOLS_MODELEDITOR_FILTEREDTREE_ENABLED_XMITAB_DISABLED);
		if (property != null || preferences.getBoolean(ModelEditorPreferences.TAB_FORM_SEARCH_SHOW, false)) {
			final FilteredTree viewParent = new FilteredTree(treeArea,
					SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(true));
			tempViewer = viewParent.getViewer();
		} else {
			tempViewer = new TreeViewerEx(treeArea, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL,
					emfDocumentProvider, modelProvider);
		}
		final TreeViewer viewer = tempViewer;

		final FontDescriptor fontDescriptor = FontDescriptor.createFrom(viewer.getControl().getFont())
				.setStyle(SWT.NORMAL);
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ComponentLabelProvider(this, messages, fontDescriptor)));
		final ObservableListTreeContentProvider<Object> contentProvider = new ObservableListTreeContentProvider<>(
				new ObservableFactoryImpl(), new TreeStructureAdvisor<>() {
				});
		viewer.setContentProvider(contentProvider);

		final WritableSet<EObject> clearedSet = new WritableSet<>();

		contentProvider.getKnownElements().addSetChangeListener(event -> {
			for (final Object o1 : event.diff.getAdditions()) {
				if (o1 instanceof EObject) {
					clearedSet.add((EObject) o1);
				}
			}

			for (final Object o2 : event.diff.getRemovals()) {
				if (o2 instanceof EObject) {
					clearedSet.remove(o2);
				}
			}
		});

		for (final FeaturePath p : labelFeaturePaths) {
			@SuppressWarnings("unchecked")
			final IObservableMap<EObject, Object> map = EMFProperties.value(p).observeDetail(clearedSet);
			map.addMapChangeListener(event -> viewer.update(event.diff.getChangedKeys().toArray(), null));
		}

		viewer.setInput(modelProvider.getRoot());
		viewer.setAutoExpandLevel(2);
		viewer.expandToLevel(viewer.getAutoExpandLevel());
		viewer.addDoubleClickListener(event -> {
			final TreeViewer viewer1 = (TreeViewer) event.getViewer();
			final IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
			final Object selectedNode = thisSelection.getFirstElement();
			if (mod1Down) {
				if (viewer1.getExpandedState(selectedNode)) {
					viewer1.setExpandedState(selectedNode, false);
				} else {
					viewer1.expandToLevel(selectedNode, AbstractTreeViewer.ALL_LEVELS);
				}
			} else {
				viewer1.setExpandedState(selectedNode, !viewer1.getExpandedState(selectedNode));
			}

		});

		// Effect of filtered tree implementation (bug 391086)
		viewer.getTree().addTreeListener(new TreeAdapter() {
			@Override
			public void treeCollapsed(TreeEvent e) {
				viewer.expandToLevel(viewer.getAutoExpandLevel());
			}
		});

		final int ops = DND.DROP_MOVE;
		viewer.addDragSupport(ops, new Transfer[] { MemoryTransfer.getInstance() }, new DragListener(viewer));
		viewer.addDropSupport(ops, new Transfer[] { MemoryTransfer.getInstance() },
				new DropListener(viewer, modelProvider.getEditingDomain()));

		return viewer;
	}

	private void registerContributedVirtualEditors() {
		final IExtensionRegistry registry = RegistryFactory.getRegistry();
		final IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (final IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"virtualeditor".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			final IContributionFactory fact = context.get(IContributionFactory.class);
			final AbstractComponentEditor<?> editor = (AbstractComponentEditor<?>) fact
					.create("bundleclass://" + el.getContributor().getName() + "/" + el.getAttribute("class"), context); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			registerEditor(el.getAttribute("id"), editor); //$NON-NLS-1$
		}
	}

	private void registerVirtualEditors() {
		registerEditor(VIRTUAL_PART_MENU, VPartMenuEditor.class);
		registerEditor(VIRTUAL_PART_TRIMS, VPartTrimEditor.class);
		registerEditor(VIRTUAL_HANDLER, VHandlerEditor.class);
		registerEditor(VIRTUAL_CONTROLS, VControlsEditor.class);
		registerEditor(VIRTUAL_BINDING_TABLE, VBindingTableEditor.class);
		registerEditor(VIRTUAL_COMMAND, VCommandEditor.class);
		registerEditor(VIRTUAL_APPLICATION_WINDOWS, VApplicationWindowEditor.class);
		registerEditor(VIRTUAL_WINDOW_WINDOWS, VWindowWindowsEditor.class);
		registerEditor(VIRTUAL_PERSPECTIVE_WINDOWS, VPerspectiveWindowsEditor.class);
		registerEditor(VIRTUAL_WINDOW_CONTROLS, VWindowControlEditor.class);
		registerEditor(VIRTUAL_WINDOW_SNIPPETS, VSnippetsEditor.class);
		registerEditor(VIRTUAL_PART_DESCRIPTORS, VPartDescriptor.class);
		registerEditor(VIRTUAL_PARTDESCRIPTOR_MENU, VPartDescriptorMenuEditor.class);
		registerEditor(VIRTUAL_PARTDESCRIPTOR_TRIMS, VPartDescriptorTrimEditor.class);
		registerEditor(VIRTUAL_TRIMMED_WINDOW_TRIMS, VWindowTrimEditor.class);
		registerEditor(VIRTUAL_ADDONS, VApplicationAddons.class);
		registerEditor(VIRTUAL_MENU_CONTRIBUTIONS, VMenuContributionsEditor.class);
		registerEditor(VIRTUAL_TOOLBAR_CONTRIBUTIONS, VToolBarContributionsEditor.class);
		registerEditor(VIRTUAL_TRIM_CONTRIBUTIONS, VTrimContributionsEditor.class);
		registerEditor(VIRTUAL_WINDOW_SHARED_ELEMENTS, VWindowSharedElementsEditor.class);
		registerEditor(VIRTUAL_MODEL_FRAGEMENTS, VModelFragmentsEditor.class);
		registerEditor(VIRTUAL_MODEL_IMPORTS, VModelImportsEditor.class);
		registerEditor(VIRTUAL_CATEGORIES, VApplicationCategoriesEditor.class);
		registerEditor(VIRTUAL_PARAMETERS, VItemParametersEditor.class);
		registerEditor(VIRTUAL_ROOT_CONTEXTS, VRootBindingContexts.class);
		registerEditor(VIRTUAL_PERSPECTIVE_CONTROLS, VPerspectiveControlEditor.class);
		registerEditor(VIRTUAL_PERSPECTIVE_TRIMS, VPerspectiveTrimEditor.class);
		registerEditor(VIRTUAL_SNIPPETS, VSnippetsEditor.class);
	}

	public void setSelection(Object element) {
		viewer.setSelection(new StructuredSelection(element));
	}

	private void registerContributedEditors() {
		final IExtensionRegistry registry = RegistryFactory.getRegistry();
		final IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$

		for (final IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"editor".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				final IEditorDescriptor desc = (IEditorDescriptor) el.createExecutableExtension("descriptorClass"); //$NON-NLS-1$
				final EClass eClass = desc.getEClass();
				final IContributionFactory fact = context.get(IContributionFactory.class);
				final AbstractComponentEditor<?> editor = (AbstractComponentEditor<?>) fact.create(
						"bundleclass://" + el.getContributor().getName() + "/" + desc.getEditorClass().getName(), //$NON-NLS-1$ //$NON-NLS-2$
						context);
				registerEditor(eClass, editor);
			} catch (final CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void registerContributedEditorTabs() {
		final IExtensionRegistry registry = RegistryFactory.getRegistry();
		final IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.editors"); //$NON-NLS-1$
		for (final IConfigurationElement el : extPoint.getConfigurationElements()) {
			if (!"editorTab".equals(el.getName())) { //$NON-NLS-1$
				continue;
			}

			try {
				final Object o = el.createExecutableExtension("contribution"); //$NON-NLS-1$
				if (o instanceof AbstractElementEditorContribution) {
					final AbstractElementEditorContribution contribution = (AbstractElementEditorContribution) o;
					ContextInjectionFactory.inject(contribution, context);
					final Class<?> contribElem = contribution.getContributableTo();
					if (contribElem == null) {
						continue;
					}
					if (!tabContributions.containsKey(contribElem)) {
						tabContributions.put(contribElem, new ArrayList<>());
					}
					final List<AbstractElementEditorContribution> res = tabContributions.get(contribElem);
					res.add(contribution);
				}
			} catch (final CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public List<AbstractElementEditorContribution> getTabContributionsForClass(Class<?> clazz) {
		final List<AbstractElementEditorContribution> ret = new ArrayList<>();
		for (Map.Entry<Class<?>, List<AbstractElementEditorContribution>> entry : tabContributions.entrySet()) {
			Class<?> clasz = entry.getKey();
			if (clasz.isAssignableFrom(clazz)) {
				ret.addAll(entry.getValue());
			}
		}
		return ret;
	}

	private void registerDefaultEditors() {

		registerEditor(ApplicationPackageImpl.Literals.APPLICATION, ApplicationEditor.class);
		registerEditor(ApplicationPackageImpl.Literals.ADDON, AddonsEditor.class);

		registerEditor(CommandsPackageImpl.Literals.KEY_BINDING, KeyBindingEditor.class);
		registerEditor(CommandsPackageImpl.Literals.HANDLER, HandlerEditor.class);
		registerEditor(CommandsPackageImpl.Literals.COMMAND, CommandEditor.class);
		registerEditor(CommandsPackageImpl.Literals.COMMAND_PARAMETER, CommandParameterEditor.class);
		registerEditor(CommandsPackageImpl.Literals.PARAMETER, ParameterEditor.class);
		registerEditor(CommandsPackageImpl.Literals.BINDING_TABLE, BindingTableEditor.class);
		registerEditor(CommandsPackageImpl.Literals.BINDING_CONTEXT, BindingContextEditor.class);
		registerEditor(CommandsPackageImpl.Literals.CATEGORY, CategoryEditor.class);

		registerEditor(MenuPackageImpl.Literals.TOOL_BAR, ToolBarEditor.class);
		registerEditor(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, DirectToolItemEditor.class);
		registerEditor(MenuPackageImpl.Literals.HANDLED_TOOL_ITEM, HandledToolItemEditor.class);
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR_SEPARATOR, ToolBarSeparatorEditor.class);
		registerEditor(MenuPackageImpl.Literals.TOOL_CONTROL, ToolControlEditor.class);
		registerEditor(MenuPackageImpl.Literals.MENU, MenuEditor.class);
		registerEditor(MenuPackageImpl.Literals.POPUP_MENU, PopupMenuEditor.class);
		registerEditor(MenuPackageImpl.Literals.MENU_SEPARATOR, MenuSeparatorEditor.class);
		registerEditor(MenuPackageImpl.Literals.HANDLED_MENU_ITEM, HandledMenuItemEditor.class);
		registerEditor(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, DirectMenuItemEditor.class);
		registerEditor(MenuPackageImpl.Literals.MENU_CONTRIBUTION, MenuContributionEditor.class);
		registerEditor(MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION, ToolBarContributionEditor.class);
		registerEditor(MenuPackageImpl.Literals.TRIM_CONTRIBUTION, TrimContributionEditor.class);
		registerEditor(MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION, DynamicMenuContributionEditor.class);

		registerEditor(UiPackageImpl.Literals.CORE_EXPRESSION, CoreExpressionEditor.class);
		registerEditor(UiPackageImpl.Literals.IMPERATIVE_EXPRESSION, ImperativeExpressionEditor.class);

		registerEditor(BasicPackageImpl.Literals.COMPOSITE_PART, CompositePartEditor.class);
		registerEditor(BasicPackageImpl.Literals.PART, PartEditor.class);
		registerEditor(BasicPackageImpl.Literals.WINDOW, WindowEditor.class);
		registerEditor(BasicPackageImpl.Literals.TRIMMED_WINDOW, TrimmedWindowEditor.class);
		registerEditor(BasicPackageImpl.Literals.PART_SASH_CONTAINER, PartSashContainerEditor.class);
		registerEditor(AdvancedPackageImpl.Literals.AREA, AreaEditor.class);
		registerEditor(BasicPackageImpl.Literals.PART_STACK, PartStackEditor.class);
		registerEditor(BasicPackageImpl.Literals.TRIM_BAR, TrimBarEditor.class);

		registerEditor(
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR,
				PartDescriptorEditor.class);

		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE_STACK, PerspectiveStackEditor.class);
		registerEditor(AdvancedPackageImpl.Literals.PERSPECTIVE, PerspectiveEditor.class);
		registerEditor(AdvancedPackageImpl.Literals.PLACEHOLDER, PlaceholderEditor.class);

		registerEditor(FragmentPackageImpl.Literals.MODEL_FRAGMENTS, ModelFragmentsEditor.class);
		registerEditor(FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT, StringModelFragment.class);
	}

	public void tabListShow(Boolean show) {
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
				final IEclipseContext child = context.createChild();
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
	public void setNotVisibleColor(@Preference(ModelEditorPreferences.NOT_VISIBLE_COLOR) String prefColorText) {
		final RGB current = JFaceResources.getColorRegistry().getRGB(ComponentLabelProvider.NOT_VISIBLE_KEY);
		RGB prefColor = StringConverter.asRGB(prefColorText, new RGB(200, 200, 200));

		if (current == null || !current.equals(prefColor)) {
			JFaceResources.getColorRegistry().put(ComponentLabelProvider.NOT_VISIBLE_KEY, prefColor);
		}

		if (viewer != null) {
			viewer.refresh();
			viewer.getControl().redraw();
		}
	}

	@Inject
	public void setNotRenderedColor(@Preference(ModelEditorPreferences.NOT_RENDERED_COLOR) String prefColorText) {
		RGB prefColor = StringConverter.asRGB(prefColorText, new RGB(200, 200, 200));
		final RGB current = JFaceResources.getColorRegistry().getRGB(ComponentLabelProvider.NOT_RENDERED_KEY);

		if (current == null || !current.equals(prefColor)) {
			JFaceResources.getColorRegistry().put(ComponentLabelProvider.NOT_RENDERED_KEY, prefColor);
		}

		if (viewer != null) {
			viewer.refresh();
			viewer.getControl().redraw();
		}
	}

	@Inject
	public void setNotVisibleRenderedColor(
			@Preference(ModelEditorPreferences.NOT_VISIBLE_AND_RENDERED_COLOR) String prefColorText) {
		RGB prefColor = StringConverter.asRGB(prefColorText, new RGB(200, 200, 200));
		final RGB current = JFaceResources.getColorRegistry()
				.getRGB(ComponentLabelProvider.NOT_VISIBLE_AND_RENDERED_KEY);

		if (current == null || !current.equals(prefColor)) {
			JFaceResources.getColorRegistry().put(ComponentLabelProvider.NOT_VISIBLE_AND_RENDERED_KEY, prefColor);
		}

		if (viewer != null) {
			viewer.refresh();
			viewer.getControl().redraw();
		}
	}

	private void registerEditor(EClass eClass, Class<? extends AbstractComponentEditor<?>> clazz) {
		registerEditor(eClass.getInstanceClassName(), clazz);
	}

	/**
	 * Register a class to use to create an editor for a given key
	 *
	 * @param ley
	 * @param clazz
	 */
	private void registerEditor(String key, Class<? extends AbstractComponentEditor<?>> clazz) {
		editorsClasses.put(key, clazz);
	}

	/**
	 * Register directly a created editor for a given key
	 *
	 * @param instanceClassName
	 * @param clazz
	 */
	private void registerEditor(String key, AbstractComponentEditor<?> editor) {
		editors.put(key, editor);
	}

	/**
	 * Get editor from an eClass. May return the registered editor for this eclass,
	 * or the editor for a parent EClass or the default editor
	 *
	 * @param eClass the eClass to get editor for
	 * @return the {@link AbstractComponentEditor} found (never null).
	 */
	public AbstractComponentEditor<?> getEditor(EClass eClass) {
		AbstractComponentEditor<?> editor = getEditor(eClass.getInstanceClassName(), false);

		if (editor == null) {
			// May be can try to use the ancestor editor if not found or the default editor
			for (final EClass cl : eClass.getESuperTypes()) {
				editor = getEditor(cl);
				if (editor != null) {
					break;
				}
			}

			// Editor is still null ? , will use the default editor
			if (editor == null) {
				editor = ContextInjectionFactory.make(DefaultEditor.class, context);
			}

			// register the parent or default editor
			editors.put(eClass.getInstanceClassName(), editor);
		}

		return editor;

	}

	public AbstractComponentEditor<?> getEditor(String key) {
		return getEditor(key, true);
	}

	/**
	 * get editor from a string key.
	 *
	 * @param key                 : the editor string key
	 * @param createDefaultIfNull if true, returns the default editor if no editor
	 *                            found
	 * @return the {@link AbstractComponentEditor} if exists. Never null if
	 *         createDefaultIfNull is true
	 */
	private AbstractComponentEditor<?> getEditor(String key, boolean createDefaultIfNull) {
		AbstractComponentEditor<?> editor = editors.get(key);

		if (editor == null) {

			// Editor not yet created in the map... must create instance using registered
			// class
			Class<? extends AbstractComponentEditor<?>> cz = editorsClasses.get(key);
			if (cz != null) {
				editor = ContextInjectionFactory.make(cz, context);
				editors.put(key, editor);

				// Then manage the feature maps...
				manageFeatureMap(editor);
			}

		}

		return editor;
	}

	private void manageFeatureMap(AbstractComponentEditor<?> editor) {
		for (final FeaturePath p : editor.getLabelProperties()) {
			boolean found = false;
			for (final FeaturePath tmp : labelFeaturePaths) {
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

	public void registerEditor(EClass eClass, AbstractComponentEditor<?> editor) {
		editors.put(eClass.getInstanceClassName(), editor);
		manageFeatureMap(editor);
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
	}

	public void setHeaderTitle(String title) {
		headerContainer.setText(title);
	}

	@PreDestroy
	void dispose() {
		try {
			obsManager.dispose();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (project == null) {
			context.get(Display.class).removeFilter(SWT.MouseUp, keyListener);
		}
		if (xmiTab != null) {
			ContextInjectionFactory.uninject(xmiTab, xmiTab.getContext());
		}
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
			final Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
			Object contents = clip.getContents(MemoryTransfer.getInstance());
			clip.dispose();
			if (contents == null) {
				return;
			}
			List<EObject> toCopy = new ArrayList<>();
			if (contents instanceof EObject) {
				toCopy.add(EcoreUtil.copy((EObject) contents));
			} else if (contents instanceof List<?>) {
				List<Object> list = (List<Object>) contents;
				for (Object object : list) {
					if (object instanceof EObject) {
						toCopy.add(EcoreUtil.copy((EObject) object));
					}
				}
			}

			if (toCopy.isEmpty()) {
				return;
			}

			final Object parent = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			EObject probe = toCopy.get(0);

			EStructuralFeature feature = null;
			EObject container = null;
			if (parent instanceof VirtualEntry) {
				final VirtualEntry<EObject, ?> v = (VirtualEntry<EObject, ?>) parent;
				feature = ((IEMFProperty) v.getProperty()).getStructuralFeature();
				container = v.getOriginalParent();
			} else if (parent instanceof EObject) {
				container = (EObject) parent;
				if (container instanceof MElementContainer<?>) {
					feature = UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN;
				} else {
					feature = determineTargetFeature(probe, container);
					if (feature == null && container.eClass().equals(probe.eClass())
							&& container.eContainer() != null) {
						// it seems the user has still the original selection active,
						// try to find the target feature using the container's container
						container = container.eContainer();
						feature = determineTargetFeature(probe, container);
					}
				}
			}

			if (container == null) {
				// no container selected that we can paste into
				return;
			}

			if (feature == null) {
				// no target feature derivable from current state of the editor
				return;
			}

			List<EClass> targetChildrenClasses = new ArrayList<>();

			if (container instanceof MStringModelFragment) {
				MStringModelFragment stringModelFragment = (MStringModelFragment) container;
				EClass targetType = StringModelFragment.findContainerType(stringModelFragment);
				if (targetType != null) {
					EStructuralFeature targetFeature = targetType
							.getEStructuralFeature(stringModelFragment.getFeaturename());
					if (targetFeature != null) {
						List<FeatureClass> classes = StringModelFragment.getTargetChildrenClasses(targetType,
								targetFeature.getName());
						for (FeatureClass fclass : classes) {
							targetChildrenClasses.add(fclass.eClass);
						}
					}
				}
			}

			CompoundCommand cc = new CompoundCommand();
			Object pastedObject = null; // The single pasted object if single paste (for undo/redo message)
			for (EObject eObject : toCopy) {
				if (!isValidTarget(parent, eObject, false)) {
					// the object to paste does not fit into the target feature
					continue;
				}
				if (!targetChildrenClasses.isEmpty() && !targetChildrenClasses.contains(eObject.eClass())) {
					// there is a limited list of allowed target types
					// and a fragment that is pointing to a different feature
					continue;
				}

				if (feature == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
					final MApplicationElement el = (MApplicationElement) EcoreUtil.create(eObject.eClass());
					el.setElementId(((MApplicationElement) eObject).getElementId());
					final Command cmd = AddCommand.create(getModelProvider().getEditingDomain(), container, feature,
							el);
					if (cmd.canExecute()) {
						pastedObject = el;
						cc.append(cmd);
					}
					return;
				}

				final Command cmd = AddCommand.create(getModelProvider().getEditingDomain(), container, feature,
						eObject);
				pastedObject = eObject;
				if (cmd.canExecute()) {
					cc.append(cmd);
					if (isLiveModel()) {
						if (container instanceof MElementContainer<?> && probe instanceof MUIElement) {
							// the last selection wins
							((MElementContainer<MUIElement>) container).setSelectedElement((MUIElement) eObject);
						}
					}
				}
			}
			if (!cc.isEmpty()) {
				if (cc.getCommandList().size() == 1) {
					cc.setLabel(messages.ModelEditor_Paste + " " + getObjectNameForCommand(pastedObject)); //$NON-NLS-1$
				} else {
					cc.setLabel(messages.ModelEditor_PasteObjects);
				}

				getModelProvider().getEditingDomain().getCommandStack().execute(cc);
			}
		}

		private EStructuralFeature determineTargetFeature(EObject probe, EObject container) {
			final EClass eClass = container.eClass();
			for (final EStructuralFeature f : eClass.getEAllReferences()) {
				if (ModelUtils.getTypeArgument(eClass, f.getEGenericType()).isInstance(probe)) {
					return f;
				}
			}
			return null;
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
			IStructuredSelection structuredSelection = (IStructuredSelection) viewer.getSelection();
			List<EObject> toCopy = new ArrayList<>();
			for (Object obj : structuredSelection.toList()) {
				if (obj instanceof EObject) {
					EObject copy = EcoreUtil.copy((EObject) obj);
					toCopy.add(copy);
				}
			}
			if (toCopy.isEmpty()) {
				return;
			}
			final Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
			clip.setContents(new Object[] { toCopy }, new Transfer[] { MemoryTransfer.getInstance() });
			clip.dispose();
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

			// Manage multiple cut objects (bug #532070)
			Collection<EObject> objectsToCut = new ArrayList<>();
			final Clipboard clip = new Clipboard(viewer.getControl().getDisplay());
			for (Object o : ((IStructuredSelection) viewer.getSelection()).toList()) {
				if (o instanceof EObject) {
					objectsToCut.add((EObject) o);
				}
			}

			Command cmd = null;
			if (objectsToCut.isEmpty()) {
				return;
			} else if (objectsToCut.size() == 1) {
				// Only 1 object to cut... create a removeCommand
				EObject o = objectsToCut.iterator().next();
				cmd = RemoveCommand.create(getModelProvider().getEditingDomain(), o.eContainer(),
						o.eContainingFeature(), o);
				((AbstractCommand) cmd).setLabel(messages.ModelEditor_Cut + " " + getObjectNameForCommand(o)); //$NON-NLS-1$
			} else {
				// There are more than one object to remove -> Compound command.
				CompoundCommand cc = new CompoundCommand();
				cc.setLabel(messages.ModelEditor_CutObjects);
				for (EObject o : objectsToCut) {
					cc.append(RemoveCommand.create(getModelProvider().getEditingDomain(), o.eContainer(),
							o.eContainingFeature(), o));
				}
				cmd = cc;
			}

			if (cmd.canExecute()) {
				// Now can set the clipboard...
				clip.setContents(new Object[] { objectsToCut }, new Transfer[] { MemoryTransfer.getInstance() });
				getModelProvider().getEditingDomain().getCommandStack().execute(cmd);
			}

			clip.dispose();
		}
	}

	public class ObservableFactoryImpl implements IObservableFactory<Object, IObservableList<Object>> {

		@SuppressWarnings("unchecked")
		@Override
		public IObservableList<Object> createObservable(Object target) {
			if (target instanceof IObservableList) {
				return new WritableList<>((List<Object>) target, Object.class);
			} else if (target instanceof VirtualEntry) {
				return ((VirtualEntry<?, Object>) target).getList();
			} else {
				final AbstractComponentEditor<EObject> editor = (AbstractComponentEditor<EObject>) getEditor(
						((EObject) target).eClass());
				if (editor != null) {
					return (IObservableList<Object>) editor.getChildList(target);
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
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			event.doit = !selection.isEmpty() && (selection.getFirstElement() instanceof MApplicationElement
					|| selection.getFirstElement() instanceof MModelFragment);
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			event.data = selection.toArray();
		}
	}

	class DropListener extends ViewerDropAdapter {
		private final EditingDomain domain;

		protected DropListener(Viewer viewer, EditingDomain domain) {
			super(viewer);
			this.domain = domain;
		}

		@Override
		public boolean performDrop(Object data) {
			if (!(data instanceof Object[])) {
				return false;
			}
			final Object[] dropDataArray = (Object[]) data;
			for (final Object object : dropDataArray) {
				final boolean result = performSingleDrop(object);
				if (!result) {
					return false;
				}
			}
			return true;
		}

		@SuppressWarnings("unchecked")
		public boolean performSingleDrop(Object data) {
			if (getCurrentLocation() == LOCATION_ON) {
				EStructuralFeature feature = null;
				EObject parent = null;
				if (getCurrentTarget() instanceof MElementContainer<?>) {
					feature = UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN;
					parent = (EObject) getCurrentTarget();
				} else if (getCurrentTarget() instanceof VirtualEntry) {

					final VirtualEntry<EObject, ?> entry = (VirtualEntry<EObject, ?>) getCurrentTarget();
					final IListProperty<?, ?> prop = entry.getProperty();
					if (prop instanceof IEMFProperty) {
						feature = ((IEMFProperty) prop).getStructuralFeature();
						parent = entry.getOriginalParent();

					}
				} else if (getCurrentTarget() instanceof EObject) {
					parent = (EObject) getCurrentTarget();
					for (final EStructuralFeature f : parent.eClass().getEAllStructuralFeatures()) {
						final EClassifier cl = ModelUtils.getTypeArgument(parent.eClass(), f.getEGenericType());
						if (cl.isInstance(data)) {
							feature = f;
							break;
						}
					}
				}

				if (feature != null && parent != null) {
					final Command cmd = createRemoveAddCommand(data, feature, parent, CommandParameter.NO_INDEX);
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
			} else if (getCurrentLocation() == LOCATION_AFTER || getCurrentLocation() == LOCATION_BEFORE) {
				EStructuralFeature feature = null;
				EObject parent = null;

				final TreeItem item = (TreeItem) getCurrentEvent().item;
				if (item != null) {
					final TreeItem parentItem = item.getParentItem();
					if (parentItem != null) {
						if (parentItem.getData() instanceof VirtualEntry) {
							final VirtualEntry<EObject, ?> vE = (VirtualEntry<EObject, ?>) parentItem.getData();
							parent = vE.getOriginalParent();
							feature = ((IEMFProperty) vE.getProperty()).getStructuralFeature();
						} else if (parentItem.getData() instanceof MElementContainer<?>) {
							parent = (EObject) parentItem.getData();
							feature = UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN;
						} else if (parentItem.getData() instanceof EObject) {
							parent = (EObject) parentItem.getData();
							for (final EStructuralFeature f : parent.eClass().getEAllStructuralFeatures()) {
								final EClassifier cl = ModelUtils.getTypeArgument(parent.eClass(), f.getEGenericType());
								if (cl.isInstance(data)) {
									feature = f;
									break;
								}
							}
						}
					}
				}

				if (feature == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS && parent != null) {
					final MApplicationElement el = (MApplicationElement) EcoreUtil.create(((EObject) data).eClass());
					el.setElementId(((MApplicationElement) data).getElementId());
					final Command cmd = createRemoveAddCommand(data, feature, parent, CommandParameter.NO_INDEX);
					if (cmd.canExecute()) {
						domain.getCommandStack().execute(cmd);
					}
					return true;
				}

				if (feature != null && parent != null && parent.eGet(feature) instanceof List<?>) {
					final List<Object> list = (List<Object>) parent.eGet(feature);
					int index = list.indexOf(getCurrentTarget());

					if (index >= list.size()) {
						index = CommandParameter.NO_INDEX;
					}

					if (parent == ((EObject) data).eContainer()) {
						if (parent instanceof MElementContainer<?> && data instanceof MUIElement) {
							Util.moveElementByIndex(domain, (MUIElement) data, isLiveModel(), index);
						} else {
							final Command cmd = MoveCommand.create(domain, parent, feature, data, index);
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

						final Command cmd = createRemoveAddCommand(data, feature, parent, index);
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

		/**
		 * Create an internal Compound command containing a Remove and a Add so as to
		 * allow the Undo
		 *
		 * @param data        the object to be dragged and dropped
		 * @param destFeature the target feature in the model where data must be dropped
		 * @param parent      the destination parent
		 * @param index       the index in the parent list
		 * @see bug 429684
		 * @return the compound command
		 */
		private Command createRemoveAddCommand(Object data, EStructuralFeature destFeature, EObject parent, int index) {

			// Remark : this code could be replaced by a MoveCommand, but unfortunately I
			// could not make it working (its canExecute() method always returns false
			// because it is not prepared (see canExecute())...)...

			List<Command> listOfCommands = new ArrayList<>();
			EStructuralFeature sourceFeature = null;
			if (data instanceof EObject) {
				sourceFeature = ((EObject) data).eContainmentFeature();
			}
			Command removeCommand = RemoveCommand.create(domain, ((EObject) data).eContainer(), sourceFeature, data);
			if (removeCommand.canExecute()) {
				listOfCommands.add(removeCommand);
			}

			Command addCommand = AddCommand.create(domain, parent, destFeature, data, index);
			listOfCommands.add(addCommand);
			CompoundCommand compoundCommand = new CompoundCommand(listOfCommands);
			compoundCommand.setLabel(messages.ModelEditor_Move + " " + getObjectNameForCommand(data)); //$NON-NLS-1$
			return compoundCommand;

		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			boolean rv = true;
			if (getSelectedObject() instanceof MApplicationElement || getSelectedObject() instanceof MModelFragment) {
				if (getCurrentLocation() == LOCATION_ON) {
					rv = isValidTarget(target, getSelectedObject(), false);
				} else if (getCurrentLocation() == LOCATION_AFTER || getCurrentLocation() == LOCATION_BEFORE) {
					TreeItem item = (TreeItem) getCurrentEvent().item;
					if (item != null) {
						item = item.getParentItem();
						if (item != null) {
							rv = isValidTarget(item.getData(), getSelectedObject(), true);
						}
					}
				}
			}

			return rv;
		}

	}

	/**
	 * This method checks if the target object is a valid target for the current
	 * instance. It used both for paste and for drag and drop behavior
	 *
	 * @param target   the target object where instance should be pasted of dropped
	 * @param instance the instance of object to be pasted or dropped
	 * @param isIndex  if true, means that target is an object in a container
	 * @return
	 */
	private boolean isValidTarget(Object target, Object instance, boolean isIndex) {
		if (target instanceof MElementContainer<?>) {
			@SuppressWarnings("unchecked")
			final MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) target;

			if (isIndex || !container.getChildren().contains(instance)) {
				final EClassifier classifier = ModelUtils.getTypeArgument(((EObject) container).eClass(),
						UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN.getEGenericType());
				return classifier.isInstance(instance);
			}
		} else if (target instanceof VirtualEntry) {
			@SuppressWarnings("unchecked")
			final VirtualEntry<EObject, ?> vTarget = (VirtualEntry<EObject, ?>) target;
			if (isIndex || !vTarget.getList().contains(instance)) {
				if (vTarget.getProperty() instanceof IEMFProperty) {
					final EStructuralFeature feature = ((IEMFProperty) vTarget.getProperty()).getStructuralFeature();
					final EObject parent = vTarget.getOriginalParent();
					final EClassifier classifier = ModelUtils.getTypeArgument(parent.eClass(),
							feature.getEGenericType());
					return classifier.isInstance(instance);
				}

			}
		} else if (target instanceof EObject) {
			final EObject eObj = (EObject) target;
			for (final EStructuralFeature f : eObj.eClass().getEAllStructuralFeatures()) {
				final EClassifier cl = ModelUtils.getTypeArgument(eObj.eClass(), f.getEGenericType());
				if (cl.isInstance(instance)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * compute a valid name for the undo/redo/paste of move commands
	 *
	 * @param data the object concerned by the command
	 * @return a representative string for the object or 'Object' if nothing found
	 */
	private String getObjectNameForCommand(Object data) {
		String clname = (data instanceof ApplicationElementImpl) ? ((ApplicationElementImpl) data).eClass().getName()
				: "Object"; //$NON-NLS-1$
		String dname = (data instanceof MUILabel) ? ((MUILabel) data).getLabel() : ""; //$NON-NLS-1$
		return clname + " " + dname; //$NON-NLS-1$
	}

	@Override
	public void gotoEObject(int targetHint, EObject object) {
		// Try to find current selection if any
		if (object == null) {
			switch (targetHint) {
			case TAB_FORM:
				if (app.getContext().get(key) == listTab) {
					Iterator<EObject> it = listTab.getSelectedEObjects().iterator();
					if (it.hasNext()) {
						object = it.next();
					}
				}
				break;
			case TAB_XMI:
				if (app.getContext().get(key) == listTab) {
					Iterator<EObject> it = listTab.getSelectedEObjects().iterator();
					if (it.hasNext()) {
						object = it.next();
					}
				} else if (app.getContext().get(key) == null) {
					final Object parent = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					if (parent instanceof EObject) {
						object = (EObject) parent;
					}
				}
				break;
			case TAB_LIST:
				if (app.getContext().get(key) == null) {
					final Object parent = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					if (parent instanceof EObject) {
						object = (EObject) parent;
					}
				}
				break;
			default:
				break;
			}
		}
		if (object != null) {
			switch (targetHint) {
			case TAB_FORM:
				// make sure tree node has been instantiated
				final ObservableListTreeContentProvider<?> provider = (ObservableListTreeContentProvider<?>) viewer
				.getContentProvider();
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
				} catch (final Exception e) {
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
		}
	}

	// This will ensure the provider has created the tree node (so we can reveal
	// it).
	private static Object getFirstMatchingItem(EObject target, ObservableListTreeContentProvider<?> provider,
			Object[] items) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == target) {
				return items[i];
			}
			final Object found = getFirstMatchingItem(target, provider, provider.getChildren(items[i]));
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public void refreshViewer() {
		viewer.refresh(true);
	}

	@Inject
	@Optional
	public void refreshOnSave(@UIEventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) org.osgi.service.event.Event event,
			@Named(IServiceConstants.ACTIVE_PART) MPart part) {
		// If the application model is saved (-> becomes undirty) we must
		// refresh tree (bug 472706)
		// Must react only if editor is the current one... (bug 509598)
		if (part != currentPart) {
			return;
		}

		final Object type = event.getProperty(EventTags.TYPE);
		final Object newValue = event.getProperty(EventTags.NEW_VALUE);

		if (UIEvents.EventTypes.SET.equals(type) && Boolean.FALSE.equals(newValue) && viewer != null) {
			viewer.refresh(true);
		}

	}

}
