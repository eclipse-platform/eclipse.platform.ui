package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;

/**
 * Main class for the bookmark navigator for displaying bookmarks on
 * resources and opening an editor on the bookmarked resource when the user
 * commands.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.BookmarkNavigator"</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a bookmark
 * navigator is needed for a workbench window. This class is not intended
 * to be instantiated or subclassed by clients.
 * </p>
 */
public class BookmarkNavigator extends ViewPart {
	private StructuredViewer viewer;
	private OpenBookmarkAction openAction;
	private CopyBookmarkAction copyAction;
	private RemoveBookmarkAction removeAction;
	private SelectAllAction selectAllAction;
	private ShowInNavigatorAction showInNavigatorAction;
	private IMemento memento;
	
	// Persistance tags.
	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	private static final String TAG_ID = "id";//$NON-NLS-1$
	private static final String TAG_MARKER = "marker";//$NON-NLS-1$
	private static final String TAG_RESOURCE = "resource";//$NON-NLS-1$
	private static final String TAG_VERTICAL_POSITION = "verticalPosition";//$NON-NLS-1$
	private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition";//$NON-NLS-1$

/**
 * Creates the bookmarks view.
 */
public BookmarkNavigator() {
	super();
}
/**
 * Adds this views contributions to the workbench.
 */
void addContributions() {
	// Create the actions.
	openAction = new OpenBookmarkAction(this);
	openAction.setHoverImageDescriptor(getImageDescriptor("clcl16/gotoobj_tsk.gif"));//$NON-NLS-1$
	openAction.setImageDescriptor(getImageDescriptor("elcl16/gotoobj_tsk.gif"));//$NON-NLS-1$

	copyAction = new CopyBookmarkAction(this);
	copyAction.setImageDescriptor(getImageDescriptor("ctool16/copy_edit.gif"));//$NON-NLS-1$
	
	removeAction = new RemoveBookmarkAction(this);
	removeAction.setHoverImageDescriptor(getImageDescriptor("clcl16/remtsk_tsk.gif"));//$NON-NLS-1$
	removeAction.setImageDescriptor(getImageDescriptor("elcl16/remtsk_tsk.gif"));//$NON-NLS-1$
	removeAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/remtsk_tsk.gif"));//$NON-NLS-1$
	
	selectAllAction = new SelectAllAction(this);
	showInNavigatorAction = new ShowInNavigatorAction(getViewSite().getPage(), viewer);

	// initializes action enabled state
	handleSelectionChanged(StructuredSelection.EMPTY);

	// Create dynamic menu mgr.  Dynamic is currently required to
	// support action contributions.
	MenuManager mgr = new MenuManager();
	mgr.setRemoveAllWhenShown(true);
	mgr.addMenuListener(new IMenuListener() {
		public void menuAboutToShow(IMenuManager mgr) {
			fillContextMenu(mgr);
		}
	});
	Menu menu = mgr.createContextMenu(viewer.getControl());
	viewer.getControl().setMenu(menu);
	getSite().registerContextMenu(mgr, viewer);
	
	// Add actions to the local tool bar
	IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
	tbm.add(removeAction);
	tbm.add(openAction);
	tbm.update(false);
	
	// Register with action service.
	IActionBars actionBars = getViewSite().getActionBars();
	actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyAction);
	actionBars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, removeAction);
	actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, selectAllAction);
	
	// Set the double click action.
	viewer.addOpenListener(new IOpenListener() {
		public void open(OpenEvent event) {
			openAction.run();
		}
	});
	viewer.addSelectionChangedListener(new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			handleSelectionChanged((IStructuredSelection) event.getSelection());
		}
	});
	viewer.getControl().addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			handleKeyPressed(e);
		}
	});
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void createPartControl(Composite parent) {
	viewer = new TreeViewer(new Tree(parent, SWT.MULTI));
	viewer.setContentProvider(new BookmarkContentProvider(this));
	viewer.setLabelProvider(new BookmarkLabelProvider(this));
	viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
	addContributions();
	initDragAndDrop();

	if(memento != null) restoreState(memento);
	memento = null;

	WorkbenchHelp.setHelp(viewer.getControl(), IBookmarkHelpContextIds.BOOKMARK_VIEW);
}
/**
 * Notifies this listener that the menu is about to be shown by
 * the given menu manager.
 *
 * @param manager the menu manager
 */
void fillContextMenu(IMenuManager manager) {
	manager.add(openAction);
	manager.add(copyAction);
	manager.add(removeAction);
	manager.add(selectAllAction);
	manager.add(showInNavigatorAction);
	manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
}
/**
 * Returns the image descriptor with the given relative path.
 */
ImageDescriptor getImageDescriptor(String relativePath) {
	String iconPath = "icons/full/";//$NON-NLS-1$
	try {
		URL installURL = getPlugin().getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + relativePath);
		return ImageDescriptor.createFromURL(url);
	}
	catch (MalformedURLException e) {
		Assert.isTrue(false);
		return null;
	}
}
/**
 * Returns the UI plugin for the bookmarks view.
 */
static AbstractUIPlugin getPlugin() {
	return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
}
/**
 * Returns the shell.
 */
Shell getShell() {
	return getViewSite().getShell();
}
/**
 * Returns the viewer used to display bookmarks.
 *
 * @return the viewer, or <code>null</code> if this view's controls
 *  have not been created yet
 */
StructuredViewer getViewer() {
	return viewer;
}
/**
 * Returns the workspace.
 */
IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}
/**
 * Handles key events in viewer.
 */
void handleKeyPressed(KeyEvent event) {
	if (event.character == SWT.DEL && event.stateMask == 0 
		&& removeAction.isEnabled())
		removeAction.run();
}
/**
 * Handles a selection change.
 *
 * @param selection the new selection
 */
void handleSelectionChanged(IStructuredSelection selection) {
	//update the actions
	openAction.selectionChanged(selection);
	removeAction.selectionChanged(selection);
	selectAllAction.selectionChanged(selection);
	showInNavigatorAction.selectionChanged(selection);
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public void init(IViewSite site,IMemento memento) throws PartInitException {
	super.init(site,memento);
	this.memento = memento;
}
/**
 * Adds drag and drop support to the bookmark navigator.
 */
protected void initDragAndDrop() {
	int operations = DND.DROP_COPY;
	Transfer[] transferTypes = new Transfer[]{
		MarkerTransfer.getInstance(), 
		TextTransfer.getInstance()};
	DragSourceListener listener = new DragSourceAdapter() {
		public void dragSetData(DragSourceEvent event){
			performDragSetData(event);
		}
		public void dragFinished(DragSourceEvent event){
		}
	};
	viewer.addDragSupport(operations, transferTypes, listener);	
}
/**
 * The user is attempting to drag marker data.  Add the appropriate
 * data to the event depending on the transfer type.
 */
void performDragSetData(DragSourceEvent event) {
	if (MarkerTransfer.getInstance().isSupportedType(event.dataType)) {
		event.data = ((IStructuredSelection) viewer.getSelection()).toArray();
		return;
	}
	if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
		Object[] markers = ((IStructuredSelection) viewer.getSelection()).toArray();
		if (markers != null) {
			StringBuffer buffer = new StringBuffer();
			ILabelProvider provider = (ILabelProvider)getViewer().getLabelProvider();
			for (int i = 0; i < markers.length; i++) {
				if (i > 0)
					buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
				buffer.append(provider.getText((IMarker)markers[i]));
			} 
			event.data = buffer.toString();
		}
		return;
	}
}
void restoreState(IMemento memento) {
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	IMemento selectionMem = memento.getChild(TAG_SELECTION);
	if(selectionMem != null) {
		ArrayList selectionList = new ArrayList();
		IMemento markerMems[] = selectionMem.getChildren(TAG_MARKER);
		for (int i = 0; i < markerMems.length; i++){
			try {
				long id = new Long(markerMems[i].getString(TAG_ID)).longValue();
				IResource resource = root.findMember(markerMems[i].getString(TAG_RESOURCE));
				if(resource != null) {
					IMarker marker = resource.findMarker(id);
					if(marker != null)
						selectionList.add(marker);
				}
			} catch (CoreException e) {}
		}
		viewer.setSelection(new StructuredSelection(selectionList));
	}

	Scrollable scrollable = (Scrollable)viewer.getControl();
	//save vertical position
	ScrollBar bar = scrollable.getVerticalBar();
	if (bar != null) {
		try {
			String posStr = memento.getString(TAG_VERTICAL_POSITION);
			int position;
			position = new Integer(posStr).intValue();
			bar.setSelection(position);
		} catch (NumberFormatException e){}
	}
	bar = scrollable.getHorizontalBar();
	if (bar != null) {
		try {
			String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
			int position;
			position = new Integer(posStr).intValue();
			bar.setSelection(position);
		} catch (NumberFormatException e){}
	}
}
public void saveState(IMemento memento) {
	if(viewer == null) {
		if(this.memento != null) //Keep the old state;
			memento.putMemento(this.memento);
		return;
	}
		
	Scrollable scrollable = (Scrollable)viewer.getControl();
 	Object markers[] = ((IStructuredSelection)viewer.getSelection()).toArray();
 	if(markers.length > 0) {
 		IMemento selectionMem = memento.createChild(TAG_SELECTION);
 		for (int i = 0; i < markers.length; i++) {
	 		IMemento elementMem = selectionMem.createChild(TAG_MARKER);
	 		IMarker marker = (IMarker)markers[i];
 			elementMem.putString(TAG_RESOURCE,marker.getResource().getFullPath().toString());
 			elementMem.putString(TAG_ID,String.valueOf(marker.getId()));
 		}
 	}

 	//save vertical position
	ScrollBar bar = scrollable.getVerticalBar();
	int position = bar != null ? bar.getSelection():0;
	memento.putString(TAG_VERTICAL_POSITION,String.valueOf(position));
	//save horizontal position
	bar = scrollable.getHorizontalBar();
	position = bar != null ? bar.getSelection():0;
	memento.putString(TAG_HORIZONTAL_POSITION,String.valueOf(position));
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void setFocus() {
	if (viewer != null) 
		viewer.getControl().setFocus();
}
}
