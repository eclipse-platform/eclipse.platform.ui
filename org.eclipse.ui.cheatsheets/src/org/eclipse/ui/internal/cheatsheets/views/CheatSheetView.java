package org.eclipse.ui.internal.cheatsheets.views;

import java.net.*;
import java.net.URL;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.actions.CheatSheetMenu;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;
import org.eclipse.ui.part.ViewPart;

public class CheatSheetView extends ViewPart {

	//booleans
	private boolean actionBarContributed = false;
	private URL contentURL;
	private CheatSheetExpandRestoreAction expandRestoreAction;

	private IMemento memento;

	//Composites
	private Composite parent;
	private CheatSheetViewer viewer;

	private void contributeToActionBars() {
		//here you have to assemble the same list as the list added to the help menu bar.
		//so an external class should do it so it can be shared with something that
		//both these classes can use.  I will call it CheatSheetActionGetter.
		//	System.out.println("Inside of contribute to action bars!!!!");
	
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menuManager = bars.getMenuManager();
		IToolBarManager tbmanager = bars.getToolBarManager();
	
		// fields
		String skipfileName = "icons/full/elcl16/collapse_expand_all.gif"; //$NON-NLS-1$
		URL skipurl = CheatSheetPlugin.getPlugin().find(new Path(skipfileName));
		ImageDescriptor skipTask = ImageDescriptor.createFromURL(skipurl);
	
		expandRestoreAction = new CheatSheetExpandRestoreAction(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP), false, viewer);
		expandRestoreAction.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP));
		expandRestoreAction.setImageDescriptor(skipTask);
		tbmanager.add(expandRestoreAction);

		viewer.setExpandRestoreAction(expandRestoreAction);
	
		CheatSheetMenu cheatsheetMenuMenuItem = new CheatSheetMenu();
		menuManager.add(cheatsheetMenuMenuItem);
	}


	/**
	 * Creates the SWT controls for this workbench part.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 * <p>
	 * For implementors this is a multi-step process:
	 * <ol>
	 *   <li>Create one or more controls within the parent.</li>
	 *   <li>Set the parent layout as needed.</li>
	 *   <li>Register any global actions with the <code>IActionService</code>.</li>
	 *   <li>Register any popup menus with the <code>IActionService</code>.</li>
	 *   <li>Register a selection provider with the <code>ISelectionService</code>
	 *     (optional). </li>
	 * </ol>
	 * </p>
	 *
	 * @param parent the parent control
	 */
	public void createPartControl(Composite parent) {
		this.parent = parent;
		
		viewer = new CheatSheetViewer();
		viewer.createPartControl(parent);
	
		if (!actionBarContributed) {
			contributeToActionBars();
			actionBarContributed = true;
		}
		if (memento != null) {
			restoreState(memento);
//TODO: need to handle memento 
//			initCheatSheetView();
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
	
		if (parent != null)
			parent.dispose();

		if(viewer != null)
			viewer.dispose();
	}
	
	public CheatSheetElement getContent() {
		if(viewer != null) {
			return viewer.getContent();
		}
		return null;
	}

	public String getgetCheatSheetID() {
		if(viewer != null) {
			return viewer.getCheatSheetID();
		}
		return null;
	}

	/* (non-Javadoc)
	 * Initializes this view with the given view site.  A memento is passed to
	 * the view which contains a snapshot of the views state from a previous
	 * session.  Where possible, the view should try to recreate that state
	 * within the part controls.
	 * <p>
	 * This implementation will ignore the memento and initialize the view in
	 * a fresh state.  Subclasses may override the implementation to perform any
	 * state restoration as needed.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;
	}

	/**
	 * Restore the view state
	 */
	private void restoreState(IMemento memento) {
		IMemento contentMemento = memento.getChild(ICheatSheetResource.URL_MEMENTO);
		if (contentMemento != null) {
			try {
				URL fileURL = new URL(contentMemento.getString(ICheatSheetResource.URL_ID));
				contentURL = fileURL;
			} catch (MalformedURLException mue) {
			}
		}
	}

	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void saveState(IMemento memento) {
		//System.out.println("Saving the state of the cheat Sheet view!!!");
		if (contentURL != null) {
			IMemento contentMemento = memento.createChild(ICheatSheetResource.URL_MEMENTO);
			contentMemento.putString(ICheatSheetResource.URL_ID, contentURL.toString());
			//System.out.println("The memento got the string.");
			//System.out.println("Here is teh memento String saved: "+contentMemento.getString("contentURL"));
			//Get the plugin save location:
			//			IPath savePath = Platform.getPluginStateLocation(CheatSheetPlugin.getPlugin());

			if(viewer != null) {
				viewer.saveCurrentSheet();
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {		
		if(viewer != null) {
			viewer.setFocus();
		}
	}
	
	public void setInput(String id) {
		if(viewer != null) {
			viewer.setInput(id);
		}
	}

	public void setInput(String id, String name, URL url) {
		if(viewer != null) {
			viewer.setInput(id, name, url);
		}
	}
}
