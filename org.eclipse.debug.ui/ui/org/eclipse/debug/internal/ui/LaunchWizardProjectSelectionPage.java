package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Used to select the project that will be used as a launch context.
 */
public class LaunchWizardProjectSelectionPage extends WizardPage {
	/**
	 * Viewer for the projects to provide the context for the launch
	 */
	protected TableViewer fElementsList;

	/**
	 * A text field to perform pattern matching
	 */
	protected Text fPatternText;

	/**
	 * The filtered array
	 */
	protected Object[] fFilteredElements;
	
	/**
	 * The selected project, or <code>null</code> if none.
	 */
	protected IProject fProject;
	
	/**
	 * A content provider for the elements list
	 */
	class ElementsContentProvider implements IStructuredContentProvider {

		protected IWorkspaceRoot fWorkspaceRoot;

		/**
		 * @see IContentProvider#inputChanged
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			fWorkspaceRoot= (IWorkspaceRoot)newInput;
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (fWorkspaceRoot != null) {
				return fWorkspaceRoot.getProjects();
			} 

			return new Object[]{};
		}
	}

	class PatternFilter extends ViewerFilter {
		protected StringMatcher fMatcher= null;

		/**
		 * @see ViewerFilter
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (fMatcher == null) {
				return true;
			}
			ILabelProvider lp= (ILabelProvider) fElementsList.getLabelProvider();
			return fMatcher.match(lp.getText(element));
		}

		public void setPattern(String pattern) {
			fMatcher= new StringMatcher(pattern + "*", true, false); //$NON-NLS-1$
		}

		/**
		 * Cache the filtered elements so we can single-select.
		 *
		 * @see ViewerFilter
		 */
		public Object[] filter(Viewer viewer, Object parent, Object[] input) {
			fFilteredElements= super.filter(viewer, parent, input);
			return fFilteredElements;
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

	/**
	 * Constructs a this page for the given mode
	 */
	public LaunchWizardProjectSelectionPage(String mode, IProject initialSelection) {
		super(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Select_Project_2")); //$NON-NLS-1$
		// Set the image for the wizard based on the mode
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_DEBUG));
		} else {
			setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_RUN));
		}
		fProject = initialSelection;
	}

	/**
	 * Creates the contents of the page - a sorted list of 
	 * the projects in the workspace and text area to enter
	 * a pattern to match.
	 */
	public void createControl(Composite ancestor) {
		Composite root= new Composite(ancestor, SWT.NONE);
		GridLayout l= new GridLayout();
		l.numColumns= 1;
		l.makeColumnsEqualWidth= true;
		root.setLayout(l);
		
		createElementsGroup(root);

		setDescription(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Select_Project_2")); //$NON-NLS-1$

		setPageComplete(false);
		setTitle(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Select_Project_2")); //$NON-NLS-1$
		setControl(root);
		WorkbenchHelp.setHelp(
			ancestor,
			new Object[] { IDebugHelpContextIds.PROJECT_SELECTION_WIZARD_PAGE });
	}

	public void createElementsGroup(Composite root) {
		Label elementsLabel= new Label(root, SWT.NONE);
		elementsLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		elementsLabel.setText(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Enter_a_pattern_to_select_a_range_of_projects_5")); //$NON-NLS-1$

		fPatternText= new Text(root, SWT.BORDER);
		fPatternText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		fElementsList= new TableViewer(root, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER) {
			protected void handleDoubleSelect(SelectionEvent event) {
				getContainer().showPage(getNextPage());
			}
		};

		Table list= fElementsList.getTable();
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gd.heightHint= 200;
		gd.grabExcessVerticalSpace= true;
		list.setLayoutData(gd);

		fElementsList.setContentProvider(new ElementsContentProvider());
		fElementsList.setLabelProvider(new WorkbenchLabelProvider());
		fElementsList.setSorter(new SimpleSorter());

		final PatternFilter filter= new PatternFilter();
		fElementsList.addFilter(filter);
		fPatternText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				filter.setPattern(((Text) (e.widget)).getText());
				fElementsList.refresh();
				if (fFilteredElements.length >= 1) {
					fElementsList.setSelection(new StructuredSelection(fFilteredElements[0]), true);
					setMessage(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Select_a_project_for_the_launch_context._6"));						 //$NON-NLS-1$
						setPageComplete(true);
						return;
				} else {
					setMessage(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.No_projects_available._7")); //$NON-NLS-1$
					setPageComplete(false);
				}
			}
		});

		fElementsList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				setMessage(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Select_a_project_for_the_launch_context._6")); //$NON-NLS-1$
				IStructuredSelection ss = null;
				if (e.getSelection() instanceof IStructuredSelection) {
					ss = (IStructuredSelection) e.getSelection();
				}
				if (ss != null && ss.size() == 1) {
					fProject = (IProject)ss.getFirstElement();
					setPageComplete(true);
				} else {
					setPageComplete(false);
					fProject = null;
				}
			}
		});

		fElementsList.setInput(ResourcesPlugin.getWorkspace().getRoot());
		if (fProject != null) {
			fElementsList.setSelection(new StructuredSelection(fProject));
		}
	}

	/**
	 * Returns the selected Java project for the context of the launch or <code>null</code> if
	 * no Java project is selected.
	 */
	protected Object[] getElements() {
		ISelection s= fElementsList.getSelection();
		if (s.isEmpty()) {
			return null;
		}

		if (s instanceof IStructuredSelection) {
			return ((IStructuredSelection) s).toArray();
		}

		return null;
	}

	/**
	 * Convenience method to set the message line
	 */
	public void setMessage(String message) {
		super.setErrorMessage(null);
		super.setMessage(message);
	}

	/**
	 * Convenience method to set the error line
	 */
	public void setErrorMessage(String message) {
		super.setMessage(null);
		super.setErrorMessage(message);
	}

	/**
	 * Initialize the settings:<ul>
	 * <li>If there is only one project, select it
	 * <li>Put the cursor in the pattern text area
	 * </ul>
	 */
	protected void initializeSettings() {
		
		Object[] children= ResourcesPlugin.getWorkspace().getRoot().getProjects();
		if (children.length == 1) {
			fElementsList.setSelection(new StructuredSelection(children[0]));
			setMessage(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Select_a_project_for_the_launch_context._6")); //$NON-NLS-1$
			setPageComplete(true);
		} else if (children.length > 0) {
			setMessage(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.Select_a_project_for_the_launch_context._6")); //$NON-NLS-1$
			if (fElementsList.getSelection().isEmpty()) {
				fElementsList.setSelection(new StructuredSelection(children[0]));
			}
			setPageComplete(true);
		} else {
			// no elements to select
			setErrorMessage(DebugUIMessages.getString("LaunchWizardProjectSelectionPage.No_projects_available._7")); //$NON-NLS-1$
			setPageComplete(false);
		}
		fPatternText.setFocus();
	}
	
	/**
	 * @IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}
	
	public IWizardContainer getContainer() {
		return super.getContainer();
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			initializeSettings();
		}
	}
	
	protected IProject getProject() {
		return fProject;
	}
}


