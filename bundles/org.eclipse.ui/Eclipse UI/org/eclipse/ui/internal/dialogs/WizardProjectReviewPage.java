package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Third page for the new project creation wizard. This page
 * allows the user to review the capabilities of the new project.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardProjectReviewPage("wizardProjectReviewPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Review project.");
 * </pre>
 * </p>
 */
public class WizardProjectReviewPage extends WizardPage {
	private Text detailsField;
	private TableViewer stepViewer;

	/**
	 * Creates a new project review wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public WizardProjectReviewPage(String pageName) {
		super(pageName);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		// Already know there is a next page...
		return isPageComplete();
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_REVIEW_WIZARD_PAGE);

		createStepGroup(composite);
		createDetailsGroup(composite);
		createInstructionsGroup(composite);
		
		setControl(composite);
	}
	
	/**
	 * Creates the control for the details
	 */
	private void createDetailsGroup(Composite parent) {
		// Create a composite to hold everything together
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add a label to identify the details text field
		Label label = new Label(composite, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("WizardProjectReviewPage.detailsLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
		
		// Text field to display the step's details
		detailsField = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		detailsField.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"); // prefill to show 15 lines
		detailsField.setEditable(false);
		detailsField.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	/**
	 * Creates the control for the instructions
	 */
	private void createInstructionsGroup(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("WizardProjectReviewPage.instructionLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}
	
	/**
	 * Creates the control for the step list
	 */
	private void createStepGroup(Composite parent) {
		initializeDialogUnits(parent);
		
		// Create a composite to hold everything together
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add a label to identify the step list field
		Label label = new Label(composite, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("WizardProjectReviewPage.stepsLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
		
		// Create the table for the viewer
		Table table = new Table(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		new TableColumn(table, SWT.NULL);	// done icon
		new TableColumn(table, SWT.NULL);	// step number
		new TableColumn(table, SWT.NULL);	// step label
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnPixelData(16, false));
		layout.addColumnData(new ColumnPixelData(convertWidthInCharsToPixels(2), false));
		layout.addColumnData(new ColumnWeightData(100, false));
		table.setLayout(layout);
		
		// Table viewer of all the steps
		stepViewer = new TableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		stepViewer.getTable().setLayoutData(data);
		stepViewer.setContentProvider(getStepProvider());
		stepViewer.setLabelProvider(new StepLabelProvider());
		stepViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)event.getSelection();
					WizardStep step = (WizardStep)sel.getFirstElement();
					if (step != null)
						detailsField.setText(step.getDetails());
				}
			}
		});
	}
	
	/**
	 * Returns the content provider for the step viewer
	 */
	private IContentProvider getStepProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof StepRoot)
					return ((StepRoot) parentElement).getSteps();
				else
					return null;
			}

		};
	}
	
	/**
	 * Sets the steps to be displayed.
	 * 
	 * @param steps the collection of steps
	 */
	/* package */ void setSteps(WizardStep[] steps) {
		stepViewer.setInput(new StepRoot(steps));
	}
	
	/**
	 * Holder of all the steps within the viewer
	 */
	private class StepRoot {
		private WizardStep[] steps;
		
		public StepRoot(WizardStep[] steps) {
			super();
			this.steps = steps;
		}
		
		public WizardStep[] getSteps() {
			if (steps == null)
				return new WizardStep[0];
			else
				return steps;
		}
	}
	
	/**
	 * Label provider for step table viewer
	 */
	private class StepLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			String label = ""; //$NON-NLS-1$
			if (element instanceof WizardStep) {
				WizardStep step = (WizardStep) element;
				switch (columnIndex) {
					case 0 :	// Done image column
						break;
					case 1 :	// Step number column
						label = String.valueOf(step.getNumber());
						break;
					case 2 :	// Step label column
						label = step.getLabel();
					default :
						break;
				}
			}
			
			return label;
		}
		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null;
			if (element instanceof WizardStep) {
				WizardStep step = (WizardStep) element;
				switch (columnIndex) {
					case 0 :	// Done image column
						image = null;
						break;
					case 1 :	// Step number column
						break;
					case 2 :	// Step label column
					default :
						break;
				}
			}
			
			return image;
		}
	}
}
