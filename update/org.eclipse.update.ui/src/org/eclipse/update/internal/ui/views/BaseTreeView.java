package org.eclipse.update.internal.ui.views;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.update.internal.ui.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public abstract class BaseTreeView extends ViewPart {
	protected TreeViewer viewer;
	private Action showDetailsAction;
	private static final String KEY_SHOW_DETAILS =
		"BaseTreeView.Popup.ShowDetails";

	private static final String KEY_CONFIRM_DELETE = "ConfirmDelete.title";

	private static final String KEY_CONFIRM_DELETE_MULTIPLE =
		"ConfirmDelete.multiple";

	private static final String KEY_CONFIRM_DELETE_SINGLE =
		"ConfirmDelete.single";
	/**
	 * The constructor.
	 */
	public BaseTreeView() {
	}

	public abstract void initProviders();

	protected TreeViewer createTree(Composite parent, int styles) {
		return new TreeViewer(parent, styles);
	}

	public void createPartControl(Composite parent) {
		viewer = createTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);
		initProviders();
		initDragAndDrop();
		//initRefreshKey();
		//initRenameKey();
		//updateTitle();

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
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

		viewer.getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					deleteKeyPressed(event.widget);
				}
			}
		});

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

		fillActionBars(getViewSite().getActionBars());

		getSite().setSelectionProvider(viewer);

		/*
			//if(memento != null) restoreState(memento);
			//memento = null;	
			// Set help for the view 
			WorkbenchHelp.setHelp(viewer.getControl(), new ViewContextComputer(this, INavigatorHelpContextIds.RESOURCE_VIEW));
		*/
		partControlCreated();
	}

	protected void partControlCreated() {
	}

	public void setFocus() {
		viewer.getTree().setFocus();
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(showDetailsAction);
	}

	protected void makeActions() {
		showDetailsAction = new Action() {
			public void run() {
				IWorkbenchPage page = UpdateUIPlugin.getActivePage();
				try {
					IViewPart part =
						page.showView(UpdatePerspective.ID_DETAILS);
					((DetailsView) part).selectionChanged(
						BaseTreeView.this,
						viewer.getSelection());
				} catch (PartInitException e) {
					UpdateUIPlugin.logException(e);
				}
			}
		};
		WorkbenchHelp.setHelp(
			showDetailsAction,
			"org.eclipse.update.ui.BaseTreeViewer_showDetailsAction");
		showDetailsAction.setText(
			UpdateUIPlugin.getResourceString(KEY_SHOW_DETAILS));
	}

	protected void initDragAndDrop() {
	}

	protected void deleteKeyPressed(Widget widget) {
	}

	protected void handleSelectionChanged(SelectionChangedEvent e) {
	}

	protected void handleDoubleClick(DoubleClickEvent e) {
		showDetailsAction.run();
	}

	protected void handleKeyPressed(KeyEvent e) {
	}

	protected void fillActionBars(IActionBars bars) {
	}

	protected boolean confirmDeletion() {
		IStructuredSelection ssel =
			(IStructuredSelection) viewer.getSelection();
		String title = UpdateUIPlugin.getResourceString(KEY_CONFIRM_DELETE);
		String message;

		if (ssel.size() > 1) {
			message =
				UpdateUIPlugin.getFormattedMessage(
					KEY_CONFIRM_DELETE_MULTIPLE,
					"" + ssel.size());
		} else {
			Object obj = ssel.getFirstElement().toString();
			message =
				UpdateUIPlugin.getFormattedMessage(
					KEY_CONFIRM_DELETE_SINGLE,
					obj.toString());
		}
		return MessageDialog.openConfirm(
			viewer.getControl().getShell(),
			title,
			message);
	}
}