package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Used to select the project that will be used as a launch context.
 */
public class LaunchWizardProjectSelectionPage extends WizardPage {

	private static final String PREFIX= "launch_wizard_project_page.";
	private static final String LAUNCHER= PREFIX + "launcher";
	private static final String SELECT_ELEMENTS= PREFIX + "select_elements";
	private static final String SELECT_ERROR_ELEMENTS= PREFIX + "select_error_elements";
	private static final String PATTERN_LABEL= PREFIX + "pattern_label";

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
			fMatcher= new StringMatcher(pattern + "*", true, false);
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
	public LaunchWizardProjectSelectionPage(String mode) {
		super(DebugUIUtils.getResourceString(PREFIX + "title"));
		// Set the image for the wizard based on the mode
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_DEBUG));
		} else {
			setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_RUN));
		}
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

		setDescription(DebugUIUtils.getResourceString(PREFIX + "title"));

		setPageComplete(false);
		setTitle(DebugUIUtils.getResourceString(PREFIX + "title"));
		setControl(root);
		WorkbenchHelp.setHelp(
			ancestor,
			new Object[] { IDebugHelpContextIds.PROJECT_SELECTION_WIZARD_PAGE });
	}

	public void createElementsGroup(Composite root) {
		Label elementsLabel= new Label(root, SWT.NONE);
		elementsLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		elementsLabel.setText(DebugUIUtils.getResourceString(PATTERN_LABEL));

		fPatternText= new Text(root, SWT.BORDER);
		fPatternText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		fElementsList= new TableViewer(root) {
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
				if (fFilteredElements.length == 1) {
					fElementsList.setSelection(new StructuredSelection(fFilteredElements[0]), true);
					setMessage(null);
					setPageComplete(true);
				} else {
					fElementsList.setSelection(null);
					// this should get done in the selection changed callback -  but it does not work
					if (fFilteredElements.length == 0) {
						setMessage(DebugUIUtils.getResourceString(SELECT_ERROR_ELEMENTS));
					} else {
						setMessage(DebugUIUtils.getResourceString(SELECT_ELEMENTS));
					}

					setPageComplete(false);
				}
			}
		});

		fElementsList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				if (e.getSelection().isEmpty()) {
					setMessage(DebugUIUtils.getResourceString(SELECT_ELEMENTS));
					setPageComplete(false);
				} else if (e.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ss= (IStructuredSelection) e.getSelection();
					if (!ss.isEmpty()) {
						((LaunchWizard)getWizard()).setProjectSelection(ss);
						setMessage(null);
						setPageComplete(true);
					}
				}
			}
		});

		fElementsList.setInput(ResourcesPlugin.getWorkspace().getRoot());
		initializeSettings();
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
		
		Runnable runnable= new Runnable() {
			public void run() {
				if (getControl().isDisposed()) {
					return;
				}
				Object[] children= ResourcesPlugin.getWorkspace().getRoot().getProjects();
				if (children.length == 1) {
					fElementsList.setSelection(new StructuredSelection(children[0]), true);
					setMessage(null);
					setPageComplete(true);
				} else if (children.length > 0) {
					setMessage(DebugUIUtils.getResourceString(SELECT_ELEMENTS));
					setPageComplete(false);
				} else {
					// no elements to select
					setErrorMessage(DebugUIUtils.getResourceString(SELECT_ERROR_ELEMENTS));
					setPageComplete(false);
				}
				fPatternText.setFocus();
			}
		};
		Display.getCurrent().asyncExec(runnable);
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
}


