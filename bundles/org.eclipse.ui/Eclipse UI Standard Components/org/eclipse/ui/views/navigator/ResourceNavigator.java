package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.framelist.FrameList;

/**
 * Implements the Resource Navigator view.
 */
public class ResourceNavigator
	extends ViewPart
	implements ISetSelectionTarget, IResourceTreeNavigatorPart {
	private TreeViewer viewer;
	private IDialogSettings settings;
	private IMemento memento;
	private NavigatorFrameSource frameSource;
	
	/**
	 * @since 2.0
	 */
	protected FrameList frameList;

	/**
	 * @since 2.0
	 */
	protected ResourceNavigatorActionGroup actionGroup;
	
	//The filter the resources are cleared up on
	private ResourcePatternFilter patternFilter = new ResourcePatternFilter();
	private ResourceWorkingSetFilter workingSetFilter = new ResourceWorkingSetFilter();

	/** Property store constant for sort order. */
	private static final String STORE_SORT_TYPE = "ResourceViewer.STORE_SORT_TYPE"; //$NON-NLS-1$
	//$NON-NLS-1$

	/**
	 * No longer used but preserved to avoid an api change.
	 */
	public static final String NAVIGATOR_VIEW_HELP_ID =
		INavigatorHelpContextIds.RESOURCE_VIEW;

	/**
	 * Preference name constant for linking editor switching to navigator selection.
	 * 
	 * [Issue: We're cheating here, by referencing a preference which is actually defined
	 * on the Workbench's preference page.  The Navigator should eventually have its own
	 * preference page with this preference on it, instead of on the Workbench's.
	 * The value must be the same as IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR.]
	 */
	private static final String LINK_NAVIGATOR_TO_EDITOR =
		"LINK_NAVIGATOR_TO_EDITOR"; //$NON-NLS-1$

	// Persistance tags.
	private static final String TAG_SORTER = "sorter"; //$NON-NLS-1$
	private static final String TAG_FILTERS = "filters"; //$NON-NLS-1$
	private static final String TAG_FILTER = "filter"; //$NON-NLS-1$
	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	private static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$
	private static final String TAG_ELEMENT = "element"; //$NON-NLS-1$
	private static final String TAG_PATH = "path"; //$NON-NLS-1$
	private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
	//$NON-NLS-1$
	private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$
	//$NON-NLS-1$

	//$NON-NLS-1$

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart)
				editorActivated((IEditorPart) part);
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partClosed(IWorkbenchPart part) {
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
		public void partOpened(IWorkbenchPart part) {
		}
	};
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			Object newValue = event.getNewValue();
			IWorkingSet filterWorkingSet = workingSetFilter.getWorkingSet();
			
			if (IWorkbenchPage.CHANGE_WORKING_SET_REPLACE.equals(property)) {
				workingSetFilter.setWorkingSet((IWorkingSet) newValue);
				getResourceViewer().refresh();
				updateTitle();
			}
			else
			if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property) && 
				newValue == filterWorkingSet) {
				updateTitle();
			}
			else
			if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property) &&
				newValue == filterWorkingSet) {
				getResourceViewer().refresh();			
			}
		}
	};
	/**
	 * Creates a new ResourceNavigator.
	 */
	public ResourceNavigator() {
		IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
		settings = workbenchSettings.getSection("ResourceNavigator"); //$NON-NLS-1$
		if (settings == null)
			settings = workbenchSettings.addNewSection("ResourceNavigator"); //$NON-NLS-1$
	}
	/**
	 * Converts the given selection into a form usable by the viewer,
	 * where the elements are resources.
	 */
	StructuredSelection convertSelection(ISelection selection) {
		ArrayList list = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			for (Iterator i = ssel.iterator(); i.hasNext();) {
				Object o = i.next();
				IResource resource = null;
				if (o instanceof IResource) {
					resource = (IResource) o;
				} else {
					if (o instanceof IAdaptable) {
						resource = (IResource) ((IAdaptable) o).getAdapter(IResource.class);
					}
				}
				if (resource != null) {
					list.add(resource);
				}
			}
		}
		return new StructuredSelection(list);
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		//	initDrillDownAdapter(viewer);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(
			new CombinedDecoratingLabelProvider(
				new WorkbenchLabelProvider(), 
				getPlugin().getWorkbench().getDecoratorManager()));
		viewer.addFilter(this.patternFilter);
		viewer.addFilter(workingSetFilter);
		
		IWorkingSet workingSet = getSite().getPage().getWorkingSet();
		if (workingSet != null) {
			workingSetFilter.setWorkingSet(workingSet);		
		}		
		IWorkingSetManager workingSetManager = getPlugin().getWorkbench().getWorkingSetManager();
		workingSetManager.addPropertyChangeListener(propertyChangeListener);				
		if (memento != null)
			restoreFilters();
		viewer.setInput(getInitialInput());
		initFrameList();
		initDragAndDrop();
		updateTitle();

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ResourceNavigator.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

		makeActions();
		initResourceSorter();

		// Fill the action bars and update the global action handlers'
		// enabled state to match the current selection.
		actionGroup.fillActionBars(getViewSite().getActionBars());
		updateActionBars((IStructuredSelection) viewer.getSelection());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen(event);
			}
		});
		viewer.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				handleKeyPressed(event);
			}
			public void keyReleased(KeyEvent event) {
				handleKeyReleased(event);
			}
		});

		getSite().setSelectionProvider(viewer);

		IWorkbenchPage page = getSite().getPage();
		page.addPartListener(partListener);
		page.addPropertyChangeListener(propertyChangeListener);

		if (memento != null)
			restoreState(memento);
		memento = null;
		// Set help for the view 
		WorkbenchHelp.setHelp(
			viewer.getControl(),
			INavigatorHelpContextIds.RESOURCE_VIEW);
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void dispose() {
		IWorkbenchPage page = getSite().getPage();
		
		page.removePartListener(partListener);
		page.removePropertyChangeListener(propertyChangeListener);

		IWorkingSetManager workingSetManager = getPlugin().getWorkbench().getWorkingSetManager();
		workingSetManager.removePropertyChangeListener(propertyChangeListener);

		if (actionGroup != null) {
			actionGroup.dispose();
		}
		super.dispose();
	}
	/**
	 * An editor has been activated.  Set the selection in this navigator
	 * to be the editor's input, if linking is enabled.
	 */
	void editorActivated(IEditorPart editor) {
		if (!isLinkingEnabled())
			return;

		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile file = fileInput.getFile();
			ISelection newSelection = new StructuredSelection(file);
			if (!viewer.getSelection().equals(newSelection)) {
				viewer.setSelection(newSelection);
			}
		}

	}

	/**
	 * Called when the context menu is about to open.
	 */
	void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getResourceViewer().getSelection();
		actionGroup.setContext(new ActionContext(selection));
		actionGroup.fillContextMenu(menu);
	}

	/**
	 * @see IResourceNavigatorPart
	 */
	public FrameList getFrameList() {
		return frameList;
	}
	
	/** 
	 * Returns the initial input for the viewer.
	 * Tries to convert the input to a resource, either directly or via IAdaptable.
	 * If the resource is a container, it uses that.
	 * If the resource is a file, it uses its parent folder.
	 * If a resource could not be obtained, it uses the workspace root.
	 */
	IContainer getInitialInput() {
		IAdaptable input = getSite().getPage().getInput();
		IResource resource = null;
		if (input instanceof IResource) {
			resource = (IResource) input;
		} else {
			resource = (IResource) input.getAdapter(IResource.class);
		}
		if (resource != null) {
			switch (resource.getType()) {
				case IResource.FILE :
					return resource.getParent();
				case IResource.FOLDER :
				case IResource.PROJECT :
				case IResource.ROOT :
					return (IContainer) resource;
				default :
					// Unknown resource type.  Fall through.
					break;
			}
		}
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	/**
	 * Returns the pattern filter for this view.
	 *
	 * @return the pattern filter
	 */
	public ResourcePatternFilter getPatternFilter() {
		return this.patternFilter;
	}
	/**
	 * Returns the navigator's plugin.
	 */
	public AbstractUIPlugin getPlugin() {
		return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	}
	/**
	 * Returns the current sorter.
	 * @since 2.0
	 */
	public ResourceSorter getResourceSorter() {
		return (ResourceSorter) getTreeViewer().getSorter();
	}
	/**
	 * Returns the resource viewer which shows the resource hierarchy.
	 * @since 2.0
	 */
	public Viewer getResourceViewer() {
		return getTreeViewer();
	}
	
	/**
	 * Returns the tree viewer which shows the resource hierarchy.
	 * @since 2.0
	 */
	public TreeViewer getTreeViewer() {
		return viewer;
	}
	/**
	 * Returns the shell to use for opening dialogs.
	 * Used in this class, and in the actions.
	 */
	public Shell getShell() {
		return getViewSite().getShell();
	}
	/**
	 * Returns the message to show in the status line.
	 *
	 * @param selection the current selection
	 * @return the status line message
	 */
	String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object o = selection.getFirstElement();
			if (o instanceof IResource) {
				return ((IResource) o).getFullPath().makeRelative().toString();
			} else {
				return ResourceNavigatorMessages.getString("ResourceNavigator.oneItemSelected"); //$NON-NLS-1$
			}
		}
		if (selection.size() > 1) {
			return ResourceNavigatorMessages.format(
				"ResourceNavigator.statusLine", //$NON-NLS-1$
				new Object[] { new Integer(selection.size())});
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns the name for the given element.
	 */
	String getName(Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getName();
		} else {
			return ((ILabelProvider) getTreeViewer().getLabelProvider()).getText(
				element);
		}
	}
	
	/**
	 * Returns the tool tip text for the given element.
	 */
	String getToolTipText(Object element) {
		if (element instanceof IResource) {
			IPath path = ((IResource) element).getFullPath();
			if (path.isRoot()) {
				return ResourceNavigatorMessages.getString("ResourceManager.toolTip"); //$NON-NLS-1$
			} else {
				return path.makeRelative().toString();
			}
		} else {
			return ((ILabelProvider) getTreeViewer().getLabelProvider()).getText(
				element);
		}
	}
	/**
	 * Handles double clicks in the viewer.
	 * Opens the editor if file double-clicked.
	 * @since 2.0
	 */
	protected void handleOpen(OpenEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		
		actionGroup.runDefaultAction(selection);
	}	
	/**
	 * Handles double clicks in the viewer.
	 * Opens the editor if file double-clicked.
	 * @since 2.0
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();

		// 1GBZIA0: ITPUI:WIN2000 - Double-clicking in navigator should expand/collapse containers
		if (viewer.isExpandable(element)) {
			viewer.setExpandedState(element, !viewer.getExpandedState(element));
		}

	}
	/**
	 * Handles selection changed in viewer.
	 * Updates global actions.
	 * Links to editor (if option enabled)
	 * @since 2.0
	 */
	protected void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		updateStatusLine(sel);
		updateActionBars(sel);
		linkToEditor(sel);
	}
	
	/**
	 * Handles a key press in viewer.
	 * By default, delegate to the action group.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		actionGroup.handleKeyPressed(event);		
	}
	
	/**
	 * Handles a key release in viewer.  By default do nothing.
	 */
	protected void handleKeyReleased(KeyEvent event) {
	}
	
	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}
	/**
	 * Adds drag and drop support to the navigator.
	 */
	protected void initDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers =
			new Transfer[] {
				ResourceTransfer.getInstance(),
				FileTransfer.getInstance(),
				PluginTransfer.getInstance()};
		viewer.addDragSupport(
			ops,
			transfers,
			new NavigatorDragAdapter((ISelectionProvider) viewer));
		NavigatorDropAdapter adapter = 	new NavigatorDropAdapter(viewer);
		adapter.setFeedbackEnabled(false);
		viewer.addDropSupport(ops, transfers, adapter);
	}
	/**
	 * Initializes a drill down adapter on the viewer.
	 */
	void initDrillDownAdapter(TreeViewer viewer) {
		DrillDownAdapter drillDownAdapter = new DrillDownAdapter(viewer) {
				// need to update title whenever input changes;
		// updateNavigationButtons is called whenever any of the drill down buttons are used
	protected void updateNavigationButtons() {
				super.updateNavigationButtons();
				updateTitle();
			}
		};
		drillDownAdapter.addNavigationActions(
			getViewSite().getActionBars().getToolBarManager());
	}
	protected void initFrameList() {
		frameSource = new NavigatorFrameSource(this);
		frameList = new FrameList(frameSource);
		frameSource.connectTo(frameList);
	}

	/**
	 * Init the current sorter.
	 */
	void initResourceSorter() {
		int sortType = ResourceSorter.NAME;
		try {
			int sortInt = 0;
			if (memento != null) {
				String sortStr = memento.getString(TAG_SORTER);
				if (sortStr != null)
					sortInt = new Integer(sortStr).intValue();
			} else {
				sortInt = settings.getInt(STORE_SORT_TYPE);
			}
			if (sortInt == ResourceSorter.NAME || sortInt == ResourceSorter.TYPE)
				sortType = sortInt;
		} catch (NumberFormatException e) {
		}
		setResourceSorter(new ResourceSorter(sortType));
	}
	/**
	 * Returns whether the preference to link navigator selection to active editor is enabled.
	 * @since 2.0
	 */
	protected boolean isLinkingEnabled() {
		IPreferenceStore store = getPlugin().getPreferenceStore();
		return store.getBoolean(LINK_NAVIGATOR_TO_EDITOR);
	}
	/**
	 * Links to editor (if option enabled)
	 * @since 2.0
	 */
	protected void linkToEditor(IStructuredSelection selection) {
		if (!isLinkingEnabled())
			return;

		Object obj = selection.getFirstElement();
		if (obj instanceof IFile && selection.size() == 1) {
			IFile file = (IFile) obj;
			IWorkbenchPage page = getSite().getPage();
			IEditorPart editorArray[] = page.getEditors();
			for (int i = 0; i < editorArray.length; ++i) {
				IEditorPart editor = editorArray[i];
				IEditorInput input = editor.getEditorInput();
				if (input instanceof IFileEditorInput
					&& file.equals(((IFileEditorInput) input).getFile())) {
					page.bringToTop(editor);
					return;
				}
			}
		}
	}
	/**
	 * Creates the action group for 
	 */
	protected void makeActions() {
		actionGroup = new ResourceNavigatorActionGroup(this);
	}
	
	public void restoreFilters() {
		IMemento filtersMem = memento.getChild(TAG_FILTERS);
		if (filtersMem != null) {
			IMemento children[] = filtersMem.getChildren(TAG_FILTER);
			String filters[] = new String[children.length];
			for (int i = 0; i < children.length; i++) {
				filters[i] = children[i].getString(TAG_ELEMENT);
			}
			getPatternFilter().setPatterns(filters);
		} else {
			getPatternFilter().setPatterns(new String[0]);
		}
	}
	
	/**
	 * Restore the state of the receiver to the state described in
	 * momento.
	 * @since 2.0
	 */
	
	protected void restoreState(IMemento memento) {
		IContainer container = ResourcesPlugin.getWorkspace().getRoot();
		IMemento childMem = memento.getChild(TAG_EXPANDED);
		if (childMem != null) {
			ArrayList elements = new ArrayList();
			IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
			for (int i = 0; i < elementMem.length; i++) {
				Object element = container.findMember(elementMem[i].getString(TAG_PATH));
				elements.add(element);
			}
			viewer.setExpandedElements(elements.toArray());
		}
		childMem = memento.getChild(TAG_SELECTION);
		if (childMem != null) {
			ArrayList list = new ArrayList();
			IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
			for (int i = 0; i < elementMem.length; i++) {
				Object element = container.findMember(elementMem[i].getString(TAG_PATH));
				list.add(element);
			}
			viewer.setSelection(new StructuredSelection(list));
		}

		Tree tree = viewer.getTree();
		//save vertical position
		ScrollBar bar = tree.getVerticalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_VERTICAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e) {
			}
		}
		bar = tree.getHorizontalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e) {
			}
		}
	}
	public void saveState(IMemento memento) {
		if (viewer == null) {
			if (this.memento != null) //Keep the old state;
				memento.putMemento(this.memento);
			return;
		}

		//save sorter
		memento.putInteger(TAG_SORTER, getResourceSorter().getCriteria());
		//save filters
		String filters[] = getPatternFilter().getPatterns();
		if (filters.length > 0) {
			IMemento filtersMem = memento.createChild(TAG_FILTERS);
			for (int i = 0; i < filters.length; i++) {
				IMemento child = filtersMem.createChild(TAG_FILTER);
				child.putString(TAG_ELEMENT, filters[i]);
			}
		}
		//save visible expanded elements
		Object expandedElements[] = viewer.getVisibleExpandedElements();
		if (expandedElements.length > 0) {
			IMemento expandedMem = memento.createChild(TAG_EXPANDED);
			for (int i = 0; i < expandedElements.length; i++) {
				IMemento elementMem = expandedMem.createChild(TAG_ELEMENT);
				elementMem.putString(
					TAG_PATH,
					((IResource) expandedElements[i]).getFullPath().toString());
			}
		}

		//save selection
		Object elements[] = ((IStructuredSelection) viewer.getSelection()).toArray();
		if (elements.length > 0) {
			IMemento selectionMem = memento.createChild(TAG_SELECTION);
			for (int i = 0; i < elements.length; i++) {
				IMemento elementMem = selectionMem.createChild(TAG_ELEMENT);
				elementMem.putString(
					TAG_PATH,
					((IResource) elements[i]).getFullPath().toString());
			}
		}

		Tree tree = viewer.getTree();
		//save vertical position
		ScrollBar bar = tree.getVerticalBar();
		int position = bar != null ? bar.getSelection() : 0;
		memento.putString(TAG_VERTICAL_POSITION, String.valueOf(position));
		//save horizontal position
		bar = tree.getHorizontalBar();
		position = bar != null ? bar.getSelection() : 0;
		memento.putString(TAG_HORIZONTAL_POSITION, String.valueOf(position));
	}
	/**
	 *	Reveal and select the passed element selection in self's visual component
	 */
	public void selectReveal(ISelection selection) {
		StructuredSelection ssel = convertSelection(selection);
		if (!ssel.isEmpty()) {
			getResourceViewer().getControl().setRedraw(false);
			getResourceViewer().setSelection(ssel, true);
			getResourceViewer().getControl().setRedraw(true);
		}
	}
	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		getTreeViewer().getTree().setFocus();
	}
	
	/**
	 * Note: For experimental use only.
	 * Sets the decorator for the navigator.
	 * <p>
	 * As of 2.0, this method no longer has any effect.
	 * </p>
	 *
	 * @param decorator a label decorator or <code>null</code> for no decorations.
	 * @deprecated use the decorators extension point instead; see IWorkbench.getDecoratorManager()
	 */
	public void setLabelDecorator(ILabelDecorator decorator) {
		// do nothing
	}
	
	/**
	 * Set the current sorter.
	 * @since 2.0
	 */
	public void setResourceSorter(ResourceSorter sorter) {
		TreeViewer viewer = getTreeViewer();
		viewer.getControl().setRedraw(false);
		viewer.setSorter(sorter);
		viewer.getControl().setRedraw(true);
		settings.put(STORE_SORT_TYPE, sorter.getCriteria());
		
		// update the sort actions' checked state
		updateActionBars((IStructuredSelection) viewer.getSelection());
	}

	/**
	 * Updates the action bar actions for the given selection.
	 * 
	 * @since 2.0
	 */	
	protected void updateActionBars(IStructuredSelection selection) {
		actionGroup.setContext(new ActionContext(selection));
		actionGroup.updateActionBars();
	}
	
	/**
	 * Updates the message shown in the status line.
	 *
	 * @param selection the current selection
	 */
	void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}
	/**
	 * Updates the title text and title tool tip.
	 * Called whenever the input of the viewer changes.
	 */
	void updateTitle() {
		Object input = getResourceViewer().getInput();
		String viewName = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkingSet workingSet = getSite().getPage().getWorkingSet();
					
		if (workingSet != null) {
			setTitle(ResourceNavigatorMessages.format(
				"ResourceNavigator.title", //$NON-NLS-1$
				new Object[] {viewName, workingSet.getName()}));
			setTitleToolTip(getToolTipText(input));
		}
		else
		if (input == null
			|| input.equals(workspace)
			|| input.equals(workspace.getRoot())) {
			setTitle(viewName);
			setTitleToolTip(""); //$NON-NLS-1$
		} 
		else {
			ILabelProvider labelProvider = (ILabelProvider) getTreeViewer().getLabelProvider();

			setTitle(ResourceNavigatorMessages.format(
				"ResourceNavigator.title", //$NON-NLS-1$
				new Object[] {viewName, labelProvider.getText(input)}));
			setTitleToolTip(getToolTipText(input));
		}
	}

	/**
 	* Set the values of the filter preference to be the 
 	* strings in preference values
 	*/

	public void setFiltersPreference(String[] patterns){
	
		StringWriter writer = new StringWriter();

		for (int i = 0; i < patterns.length; i++) {
			if (i != 0)
				writer.write(ResourcePatternFilter.COMMA_SEPARATOR);
			writer.write(patterns[i]);
		}

		getPlugin().getPreferenceStore().setValue(
			ResourcePatternFilter.FILTERS_TAG,
			writer.toString());
	
	}
		
}