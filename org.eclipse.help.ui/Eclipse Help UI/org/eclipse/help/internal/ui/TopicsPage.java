package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions.*;

/**
 * Page to hold a topics view
 */
public class TopicsPage extends NavigationPage implements IMenuListener {

	private Object view;
	private TreeViewer viewer;
	private NavigationWorkbook navWorkbook;
	// Listeners to register later, because we use lazy control creation
	private Collection selectionChangedListeners = new ArrayList();
	public TopicsPage(NavigationWorkbook workbook, Object view) {
		super(workbook, ((Contribution) view).getLabel());
		this.navWorkbook = workbook;
		this.view = view;
	}
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (viewer != null)
			viewer.addSelectionChangedListener(listener);
		selectionChangedListeners.add(listener);
	}
	protected Control createControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(TreeContentProvider.getDefault());
		viewer.setLabelProvider(ElementLabelProvider.getDefault());

		// add all listeners registered before actual control was created
		for (Iterator it = selectionChangedListeners.iterator(); it.hasNext();) {
			viewer.addSelectionChangedListener((ISelectionChangedListener) it.next());
		}

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});

		viewer.setInput(view);
		// create the pop-up menus in the viewer
		// For now, do this only for win32. 
		if (System.getProperty("os.name").startsWith("Win")) {
			createPopUpMenus();
		}
		WorkbenchHelp.setHelp(
			viewer.getControl(),
			new String[] {
				IHelpUIConstants.TOPICS_VIEWER,
				IHelpUIConstants.NAVIGATION_VIEWER,
				IHelpUIConstants.EMBEDDED_HELP_VIEW });
		return viewer.getControl();
	}
	private void createPopUpMenus() {
		// create the Menu Manager for this control. and do
		// proper initialization
		Menu shellMenu;
		MenuManager mgr = new MenuManager();
		shellMenu = mgr.createContextMenu(viewer.getControl());
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(this);
		viewer.getControl().setMenu(shellMenu);

	}
	public void dispose() {
		if (viewer != null) // can be null if page has never been activated
			 ((Tree) viewer.getControl()).removeAll();
		super.dispose();
	}
	public ISelection getSelection() {
		if (viewer != null)
			return viewer.getSelection();
		return null;
	}
	/**
	 * Handles double clicks in viewer.
	 * Opens editor if file double-clicked.
	 */
	void handleDoubleClick(DoubleClickEvent event) {

		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		// Double-clicking in navigator should expand/collapse containers
		if (viewer != null && viewer.isExpandable(element)) {
			viewer.setExpandedState(element, !viewer.getExpandedState(element));
		}
	}
	public void menuAboutToShow(IMenuManager mgr) {
		// Add pop-up Menus depending on current selection
		// if multiple topics are selected, the Nested print menu is not showed.
		ISelection selection = getSelection();
		if (!(selection instanceof IStructuredSelection))
			return; // should never be here. This is gauranteed by Viewer.

		// Show nested printing and only if one topic is selected.
		// make sure to have lazy creation of the printing Browser. 
		if (((IStructuredSelection) selection).size() == 1) {
			mgr.add(new NestedPrintAction((IStructuredSelection) selection));
			mgr.add(new Separator());
			mgr.update(true);
		}

	}
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (viewer != null)
			viewer.removeSelectionChangedListener(listener);
		selectionChangedListeners.remove(listener);
	}
	public void setSelection(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return;
		Object o = ((IStructuredSelection) selection).getFirstElement();
		if (o == null)
			return;
		Topic t = null;
		if (o instanceof Topic) { // one of the related links
			// Check if topic belongs to current view
			Contribution ancestor = ((Topic) o).getParent();
			while (ancestor instanceof Topic) {
				ancestor = ancestor.getParent();
			}
			// ancestor is a view
			if (ancestor == view) {
				t = ((Topic) o);
			}
		} else
			if (o instanceof String) { // Synchronization to given url
				String url = (String) o;
				Topic[] topics =
					HelpSystem.getNavigationManager().getCurrentNavigationModel().getTopicsWithURL(
						url);
				if (topics == null)
					return;
				// Check if topic belongs to current view
				for (int i = 0; i < topics.length; i++) {
					Contribution ancestor = topics[i].getParent();
					while (ancestor instanceof Topic) {
						ancestor = ancestor.getParent();
					}
					// ancestor is a view
					if (ancestor == view) {
						t = topics[i];
						break;
					}
				}
			}
		if (t != null) {
			navWorkbook.setSelectedPage(this);
			// Expand all nodes between root of model and the given element, exclusive
			viewer.expandToLevel(t, 0);
			// Select given element
			viewer.setSelection(new StructuredSelection(t), true);
		}
	}
}
