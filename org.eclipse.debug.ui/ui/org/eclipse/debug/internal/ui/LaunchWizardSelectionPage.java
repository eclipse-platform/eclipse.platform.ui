package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;

public class LaunchWizardSelectionPage extends WizardSelectionPage {
	
	/**
	 * Viewer for the launchers
	 */
	protected TableViewer fLaunchersList;

	/**
	 * List of launchers
	 */
	protected Object[] fLaunchers;

	/**
	 * Check box for setting default launcher
	 */
	protected Button fSetAsDefaultLauncher;
	
	/** 
	 * The mode of the launch.
	 * @see ExecutionAction#getMode()
	 */
	protected String fMode;

	/**
	 * Indicates whether the project context of the launch has
	 * changed since the creation of this wizard.
	 * If the context has changed, the list of launchables
	 * needs updating.
	 */	
	protected boolean fLaunchablesUpdateNeeded;
	
	/**
	 * The selected launcher or <code>null</code> if none.
	 */
	protected ILauncher fLauncher;
	
	/**
	 * The current project context
	 */
	protected IProject fProject= null;

	/**
	 * A content provider for the elements list
	 */
	class ElementsContentProvider implements IStructuredContentProvider {

		/**
		 * @see IContentProvider#inputChanged
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return fLaunchers;
		}
	}

	class SimpleSorter extends ViewerSorter {
		/**
		 * @seeViewerSorter#isSorterProperty(Object, Object)
		 */
		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}
	}
	
	public LaunchWizardSelectionPage(Object[] allLaunchers, String mode, ILauncher initialLauncher) {
		super(DebugUIMessages.getString("LaunchWizardSelectionPage.Select_Launcher_1")); //$NON-NLS-1$
		fLaunchers= allLaunchers;
		fMode= mode;
		fLauncher = initialLauncher;
	}

	public void createControl(Composite ancestor) {
		Composite root= new Composite(ancestor, SWT.NONE);
		GridLayout l= new GridLayout();
		l.numColumns= 1;
		root.setLayout(l);
		createLaunchersGroup(root);
		
		setTitle(DebugUIMessages.getString("LaunchWizardSelectionPage.Select_Launcher_2")); //$NON-NLS-1$
		if (fMode.equals(ILaunchManager.DEBUG_MODE)) {
			setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_DEBUG));
		} else {
			setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_RUN));
		}
		setControl(root);
		WorkbenchHelp.setHelp(
			ancestor,
			IDebugHelpContextIds.LAUNCHER_SELECTION_WIZARD_PAGE);
	}

	public void createLaunchersGroup(Composite root) {

		fSetAsDefaultLauncher= new Button(root, SWT.CHECK);

		Label launchersLabel= new Label(root, SWT.NONE);
		launchersLabel.setText(DebugUIMessages.getString("LaunchWizardSelectionPage.Select_a_launcher__3")); //$NON-NLS-1$

		fLaunchersList= new TableViewer(new Table(root, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER)) {
			protected void handleDoubleSelect(SelectionEvent event) {
				updateSelection(getSelection());
				((LaunchWizard)getWizard()).updateDefaultLauncher();
				getContainer().showPage(getNextPage());
			}
		};
		
		Table list= fLaunchersList.getTable();
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gd.heightHint= 200;
		gd.grabExcessVerticalSpace= true;
		gd.grabExcessHorizontalSpace= true;
		list.setLayoutData(gd);

		fLaunchersList.setContentProvider(new ElementsContentProvider());
		fLaunchersList.setLabelProvider(new DelegatingModelPresentation());
		fLaunchersList.setSorter(new SimpleSorter());
		fLaunchersList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				if (e.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ss= (IStructuredSelection) e.getSelection();
					if (ss.isEmpty()) {
						launcherSelected(null);
					} else {
						launcherSelected((ILauncher)ss.getFirstElement());
					}
				}
			}
		});
		fLaunchersList.setInput(fLaunchersList);
		if (fLauncher != null) {
			fLaunchersList.setSelection(new StructuredSelection(fLauncher));
		}
	}

	/**
	 * Updates the elements list for the given launcher
	 */
	protected void launcherSelected(ILauncher launcher) {
		fLauncher = launcher;
		setMessage(null);
		if (launcher != null) {
			LaunchWizardNode node= new LaunchWizardNode(this, launcher, fMode);
			setSelectedNode(node);
			setDescription(node.getDescription());
		}
		updateDefaultLauncherButton(launcher);
		
	}

	/**
	 * Convenience method to set the error line
	 */
	public void setErrorMessage(String message) {
		super.setMessage(null);
		super.setErrorMessage(message);
	}

	/**
	 * Convenience method to set the message line
	 */
	public void setMessage(String message) {
		super.setErrorMessage(null);
		super.setMessage(message);
	}

	/**
	* Initialize the settings:<ul>
	* <li>If there is only one launcher, select it
	* </ul>
	*/
	protected void initializeSettings() {
		
		if (fLaunchers.length == 0) {
			setErrorMessage(DebugUIMessages.getString("LaunchWizardSelectionPage.No_launchers_registered._4")); //$NON-NLS-1$
		} else {
			IStructuredSelection selection= (IStructuredSelection)fLaunchersList.getSelection();
			if (selection.isEmpty()) {
				fLaunchersList.setSelection(new StructuredSelection(fLaunchers[0]));
			} else if (fLaunchablesUpdateNeeded) {
				//update the list of launchables if the project has changed
				fLaunchersList.setSelection(selection);
				fLaunchablesUpdateNeeded= false;
			}
		}
	}

	protected void updateDefaultLauncherButton(ILauncher launcher) {
		IProject project= ((LaunchWizard)getWizard()).getProject();
		if (project == null) {
			// disable the control, we cannot set it without a project context
			fSetAsDefaultLauncher.setSelection(false);
			fSetAsDefaultLauncher.setEnabled(false);
		} else {
			// if the launcher is not the default launcher, enable the control
			try {
				ILauncher defaultLauncher= DebugPlugin.getDefault().getLaunchManager().getDefaultLauncher(project);
				if (defaultLauncher != null && defaultLauncher.equals(launcher)) {
					// disable the setting, but show that it is set
					fSetAsDefaultLauncher.setSelection(true);
					fSetAsDefaultLauncher.setEnabled(false);
				} else {
					// allow to set as default - in fact, set as default 
					fSetAsDefaultLauncher.setSelection(true);
					fSetAsDefaultLauncher.setEnabled(true);
				}
			} catch (CoreException e) {
				// disable default launcher
				fSetAsDefaultLauncher.setSelection(false);
				fSetAsDefaultLauncher.setEnabled(false);
			}
		}
	}
	
	protected ILauncher getLauncher() {
		return fLauncher;
	}
	
	/**
	 * Implemented here to provide package access
	 */
	protected IWizardContainer getContainer() {
		return super.getContainer();
	}
	
	protected void updateDefaultProject() {
		IProject project= ((LaunchWizard)getWizard()).getProject();
		String projectName= ""; //$NON-NLS-1$
		if (project != null) {
			if (!project.equals(fProject)) {
				fLaunchablesUpdateNeeded= true;
				fProject= project;
			}
			projectName= project.getName();
		} else {
			projectName= DebugUIMessages.getString("LaunchWizardSelectionPage.<unknown>_6"); //$NON-NLS-1$
		}
		fSetAsDefaultLauncher.setText(MessageFormat.format(DebugUIMessages.getString("LaunchWizardSelectionPage.Set_as_&default_launcher_for_project___{0}___7"), new String[] {projectName})); //$NON-NLS-1$
		fSetAsDefaultLauncher.pack();
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			updateDefaultProject();
			initializeSettings();
		} else {
			fLaunchablesUpdateNeeded= false;
		}
	}
}

