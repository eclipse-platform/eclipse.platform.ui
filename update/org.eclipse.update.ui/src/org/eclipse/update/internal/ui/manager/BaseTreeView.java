package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public abstract class BaseTreeView extends ViewPart {
protected TreeViewer viewer;
/**
 * The constructor.
 */
public BaseTreeView() {
}

public abstract void initProviders();

public void createPartControl(Composite parent)  {
	viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	viewer.setUseHashlookup(true);
	initProviders();
	//viewer.setInput(getInitialInput());
	//initDragAndDrop();
	//initRefreshKey();
	//initRenameKey();
	//updateTitle();
	
	MenuManager menuMgr = new MenuManager("#PopupMenu");//$NON-NLS-1$
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.add(new GroupMarker("additions"));
			BaseTreeView.this.fillContextMenu(manager);
		}
	});
	Menu menu = menuMgr.createContextMenu(viewer.getTree());
	viewer.getTree().setMenu(menu);
	getSite().registerContextMenu(menuMgr, viewer);
	makeActions();

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
	viewer.getControl().addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			handleKeyPressed(e);
		}
	});

	fillActionBars();
	
	getSite().setSelectionProvider(viewer);

/*
	//if(memento != null) restoreState(memento);
	//memento = null;	
	// Set help for the view 
	WorkbenchHelp.setHelp(viewer.getControl(), new ViewContextComputer(this, INavigatorHelpContextIds.RESOURCE_VIEW));
*/
}

public void setFocus()  {
}

protected void fillContextMenu(IMenuManager manager) {
}

protected void makeActions() {
}

protected void handleSelectionChanged(SelectionChangedEvent e) {
}

protected void handleDoubleClick(DoubleClickEvent e) {
}

protected void handleKeyPressed(KeyEvent e) {
}

protected void fillActionBars() {
}
}
