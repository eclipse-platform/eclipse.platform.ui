package org.eclipse.ui.views.properties;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.help.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Page;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


/**
 * The standard implementation of property sheet page which presents
 * a table of property names and values obtained from the current selection
 * in the active workbench part.
 * <p>
 * This page obtains the information about what to properties display from 
 * the current selection (which it tracks). 
 * </p>
 * <p>
 * The model for this page is a hierarchy of <code>IPropertySheetEntry</code>.
 * The page may be configured with a custom model by setting the root entry.
 * <p>
 * If no root entry is set then a default model is created which uses the
 * <code>IPropertySource</code> interface to obtain the properties of
 * the current slection. This requires that the selected objects provide an
 * <code>IPropertySource</code> adapter (or implement 
 * <code>IPropertySource</code> directly). This restiction can be overcome
 * by providing this page with an <code>IPropertySourceProvider</code>. If
 * supplied, this provider will be used by the default model to obtain a
 * property source for the current selection 
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see IPropertySource
 */
public class PropertySheetPage extends Page implements IPropertySheetPage {
	/**
	 * Help context id 
	 * (value <code>"org.eclipse.ui.property_sheet_page_help_context"</code>).
	 */
	public static final String HELP_CONTEXT_PROPERTY_SHEET_PAGE = "org.eclipse.ui.property_sheet_page_help_context";
	
	private PropertySheetViewer viewer;
	private IPropertySheetEntry rootEntry;
	private IPropertySourceProvider provider;
	
	private DefaultsAction defaultsAction;
	private FilterAction filterAction;
	private CategoriesAction categoriesAction;
/**
 * Creates a new property sheet page.
 */
public PropertySheetPage() {
	super();
}
/* (non-Javadoc)
 * Method declared on <code>IPage</code>.
 */
public void createControl(Composite parent) {
	// create a new viewer
	viewer = new PropertySheetViewer(parent);

	// set the model for the viewer
	if (rootEntry == null) {
		// create a new root
		PropertySheetEntry root = new PropertySheetEntry();
		if (provider != null)
			// set the property source provider
			root.setPropertySourceProvider(provider);
		rootEntry = root;
	}
	viewer.setRootEntry(rootEntry);

	// add a help listener
	WorkbenchHelp.setHelp(viewer.getControl(), new PropertySheetPageContextComputer(viewer, HELP_CONTEXT_PROPERTY_SHEET_PAGE));
}
/* (non-Javadoc)
 * Method declared on IPage (and Page).
 */
public Control getControl() {
	if (viewer == null)
		return null;
	return viewer.getControl();
}
/**
 * Returns the image descriptor with the given relative path.
 */
private ImageDescriptor getImageDescriptor(String relativePath) {
	String iconPath;
	if(Display.getCurrent().getIconDepth() > 4)
		iconPath = "icons/full/";
	else
		iconPath = "icons/basic/";
		
	try {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		URL installURL = plugin.getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + relativePath);
		return ImageDescriptor.createFromURL(url);
	}
	catch (MalformedURLException e) {
		// Should not happen
		return null;
	}
}
/**
 * Make action objects.
 */
private void makeActions() {
	// Default values
	defaultsAction = new DefaultsAction(viewer, "defaults");
	defaultsAction.setToolTipText("Restore default values");
	defaultsAction.setImageDescriptor(getImageDescriptor("elcl16/defaults_ps.gif"));
	defaultsAction.setHoverImageDescriptor(getImageDescriptor("clcl16/defaults_ps.gif"));
	defaultsAction.setText("Defaults");

	// Filter (expert)
	filterAction = new FilterAction(viewer, "filter");
	filterAction.setToolTipText("Filter properties");
	filterAction.setImageDescriptor(getImageDescriptor("elcl16/filter_ps.gif"));
	filterAction.setHoverImageDescriptor(getImageDescriptor("clcl16/filter_ps.gif"));
	filterAction.setText("Filter");
	filterAction.setChecked(false);

	// Categories
	categoriesAction = new CategoriesAction(viewer, "categories");
	categoriesAction.setToolTipText("Show/Hide categories");
	categoriesAction.setImageDescriptor(getImageDescriptor("elcl16/tree_mode.gif"));
	categoriesAction.setHoverImageDescriptor(getImageDescriptor("clcl16/tree_mode.gif"));
	categoriesAction.setText("Categories");
	categoriesAction.setChecked(false);
}
/* (non-Javadoc)
 * Method declared on IPage (and Page).
 */
public void makeContributions(
	IMenuManager menuManager,
	IToolBarManager toolBarManager,
	IStatusLineManager statusLineManager) {

	// make the actions
	makeActions();

	// add actions to the menu manager
	menuManager.add(categoriesAction);
	menuManager.add(filterAction);
	menuManager.add(defaultsAction);
	
	// add actions to the tool bar
	toolBarManager.add(categoriesAction);
	toolBarManager.add(filterAction);
	toolBarManager.add(defaultsAction);

	// set status line manager into the viewer
	viewer.setStatusLineManager(statusLineManager);
}
/**
 * Updates the model for the viewer.
 * <p>
 * Note that this means ensuring that the model reflects the state
 * of the current viewer input. 
 * </p>
 */
public void refresh() {
	if (viewer == null)
		return;
	// calling setInput on the viewer will cause the model to refresh
	viewer.setInput(viewer.getInput());
}
/* (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	if (viewer == null)
		return;

	// change the viewer input since the workbench selection has changed.
	if (selection instanceof IStructuredSelection) {
		viewer.setInput(((IStructuredSelection)selection).toArray());
	}
}
/**
 * Sets focus to a part in the page.
 */
public void setFocus() {
	viewer.getControl().setFocus();
}
/**
 * Sets the given property source provider as
 * the property source provider
 * <p>
 * Calling this method is only valid if you are using
 * this page's defualt root entry
 * </p>
 * @param newProvider the property source provider
 */
public void setPropertySourceProvider(IPropertySourceProvider newProvider) {
	provider = newProvider;
	if (rootEntry instanceof PropertySheetEntry) {
		((PropertySheetEntry)rootEntry).setPropertySourceProvider(provider);
		// the following will trigger an update
		viewer.setRootEntry(rootEntry);
	}
}
/**
 * Sets the given entry as the model for the page.
 *
 * @param entry the root entry
 */
public void setRootEntry(IPropertySheetEntry entry) {
	rootEntry = entry;
	if (viewer != null)
		// the following will trigger an update
		viewer.setRootEntry(rootEntry); 
}
}
