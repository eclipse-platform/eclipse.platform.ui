/*
 * Created on May 15, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.views.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ConfigurationManagerWindow
	extends ApplicationWindow
	implements IWorkbenchWindow {
	private ManagerActionBars bars;
	private SubActionBars viewBars;
	private ConfigurationView view;
	private GlobalAction propertiesAction;

	class ManagerActionBars implements IActionBars {
		public void clearGlobalActionHandlers() {
		}

		public IAction getGlobalActionHandler(String actionId) {
			return null;
		}

		public IMenuManager getMenuManager() {
			return ConfigurationManagerWindow.this.getMenuBarManager();
		}

		public IStatusLineManager getStatusLineManager() {
			return ConfigurationManagerWindow.this.getStatusLineManager();
		}

		public IToolBarManager getToolBarManager() {
			return ConfigurationManagerWindow.this.getToolBarManager();
		}

		public void setGlobalActionHandler(String actionId, IAction handler) {
		}

		public void updateActionBars() {
			ConfigurationManagerWindow.this.updateActionBars();
		}
	}

	class GlobalAction extends Action implements IPropertyChangeListener {
		private IAction handler;

		public GlobalAction() {
		}

		public void setActionHandler(IAction action) {
			if (handler != null) {
				handler.removePropertyChangeListener(this);
				handler = null;
			}
			if (action != null) {
				this.handler = action;
				action.addPropertyChangeListener(this);
			}
			if (handler != null) {
				setEnabled(handler.isEnabled());
				setChecked(handler.isChecked());
			}
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(Action.ENABLED)) {
				Boolean bool = (Boolean) event.getNewValue();
				setEnabled(bool.booleanValue());
			} else if (event.getProperty().equals(Action.CHECKED)) {
				Boolean bool = (Boolean) event.getNewValue();
				setChecked(bool.booleanValue());
			} else if (
				event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS)) {
				setActionHandler(
					((IActionBars) event.getSource()).getGlobalActionHandler(
						getId()));
			}
		}

		public void run() {
			if (handler != null)
				handler.run();
		}
	}

	class ManagerSite implements IViewSite {
		public IActionBars getActionBars() {
			return viewBars;
		}

		public String getId() {
			return null;
		}

		public IKeyBindingService getKeyBindingService() {
			return null;
		}

		public String getPluginId() {
			return UpdateUI.PLUGIN_ID;
		}

		public String getRegisteredName() {
			return "Configuration Manager"; //$NON-NLS-1$
		}

		public void registerContextMenu(
			MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		}

		public void registerContextMenu(
			String menuId,
			MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		}

		public IWorkbenchPage getPage() {
			return null;
		}

		public ISelectionProvider getSelectionProvider() {
			return null;
		}

		public Shell getShell() {
			return ConfigurationManagerWindow.this.getShell();
		}

		public IWorkbenchWindow getWorkbenchWindow() {
			return ConfigurationManagerWindow.this;
		}

		public void setSelectionProvider(ISelectionProvider provider) {
		}
	}
	/**
	 * @param parentShell
	 */
	public ConfigurationManagerWindow(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
		bars = new ManagerActionBars();
		viewBars = new SubActionBars(bars);
		// Setup window.
		addMenuBar();
		addActions();
		addToolBar(SWT.FLAT);
		addStatusLine();
	}

	private void addActions() {
		IMenuManager menuBar = getMenuBarManager();
		IMenuManager fileMenu = new MenuManager(UpdateUI.getString("ConfigurationManagerWindow.fileMenu")); //$NON-NLS-1$
		menuBar.add(fileMenu);

		propertiesAction = new GlobalAction();
		propertiesAction.setText(UpdateUI.getString("ConfigurationManagerWindow.properties")); //$NON-NLS-1$
		propertiesAction.setEnabled(false);

		fileMenu.add(propertiesAction);
		fileMenu.add(new Separator());

		Action closeAction = new Action() {
			public void run() {
				close();
			}
		};
		closeAction.setText(UpdateUI.getString("ConfigurationManagerWindow.close")); //$NON-NLS-1$
		fileMenu.add(closeAction);
	}
	
	private void hookGlobalActions() {
		propertiesAction.setActionHandler(viewBars.getGlobalActionHandler(IWorkbenchActionConstants.PROPERTIES));
	}
	
	public int open() {
		view.setViewName(getShell().getText());
		return super.open();
	}

	protected Control createContents(Composite parent) {
		view = new ConfigurationView();
		try {
			view.init(new ManagerSite());
		} catch (PartInitException e) {
			UpdateUI.logException(e);
		}
		view.addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int id) {
				if (id==IWorkbenchPart.PROP_TITLE &&view.getTitle()!=null)
					getShell().setText(view.getTitle());
			}
		});
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);

		GridData gd;
		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = 1;
		separator.setLayoutData(gd);

		view.createPartControl(container);
		Control viewControl = view.getControl();
		gd = new GridData(GridData.FILL_BOTH);
		viewControl.setLayoutData(gd);
		hookGlobalActions();
		viewBars.activate();
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			view.getTreeViewer().setSelection(new StructuredSelection(localSite));
		}
		catch (CoreException e) {
		}
		
		UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
		getShell().setImage(provider.get(UpdateUIImages.DESC_CONFIGS_VIEW, 0));
		
		return container;
	}

	public void updateActionBars() {
		getMenuBarManager().updateAll(false);
		getToolBarManager().update(false);
		getStatusLineManager().update(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#getActivePage()
	 */
	public IWorkbenchPage getActivePage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getPages()
	 */
	public IWorkbenchPage[] getPages() {
		return new IWorkbenchPage[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getPartService()
	 */
	public IPartService getPartService() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getSelectionService()
	 */
	public ISelectionService getSelectionService() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getWorkbench()
	 */
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#isApplicationMenu(java.lang.String)
	 */
	public boolean isApplicationMenu(String menuId) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#openPage(org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage openPage(IAdaptable input)
		throws WorkbenchException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#openPage(java.lang.String, org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage openPage(String perspectiveId, IAdaptable input)
		throws WorkbenchException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#setActivePage(org.eclipse.ui.IWorkbenchPage)
	 */
	public void setActivePage(IWorkbenchPage page) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#addPageListener(org.eclipse.ui.IPageListener)
	 */
	public void addPageListener(IPageListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#addPerspectiveListener(org.eclipse.ui.IPerspectiveListener)
	 */
	public void addPerspectiveListener(IPerspectiveListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#removePageListener(org.eclipse.ui.IPageListener)
	 */
	public void removePageListener(IPageListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#removePerspectiveListener(org.eclipse.ui.IPerspectiveListener)
	 */
	public void removePerspectiveListener(IPerspectiveListener listener) {
	}
}
