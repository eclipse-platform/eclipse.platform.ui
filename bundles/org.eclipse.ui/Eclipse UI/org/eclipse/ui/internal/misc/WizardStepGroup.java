package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WizardStep;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * A group of controls used to view the list of
 * wizard steps to be done.
 */
public class WizardStepGroup {
	private Image doneImage;
	private Image currentImage;
	private int numberColWidth = 8;
	private WizardStep currentStep;
	private TableViewer stepViewer;
	private ISelectionChangedListener selectionListener;

	/**
	 * Creates a new instance of the <code>WizardStepGroup</code>
	 * 
	 * @param numberColWidth the width in pixel for the number column
	 */
	public WizardStepGroup(int numberColWidth) {
		super();
		this.numberColWidth = numberColWidth;
	}

	/**
	 * Create the contents of this group. The basic layout is a table
	 * with a label above it.
	 */
	public Control createContents(Composite parent) {
		// Create a composite to hold everything together
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (doneImage != null) {
					doneImage.dispose();
					doneImage = null;
				}
				if (currentImage != null) {
					currentImage.dispose();
					currentImage = null;
				}
			}
		});
		
		// Add a label to identify the step list field
		Label label = new Label(composite, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("WizardStepGroup.stepsLabel")); //$NON-NLS-1$
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
		layout.addColumnData(new ColumnPixelData(numberColWidth, false));
		layout.addColumnData(new ColumnWeightData(100, false));
		table.setLayout(layout);
		
		// Table viewer of all the steps
		stepViewer = new TableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		stepViewer.getTable().setLayoutData(data);
		stepViewer.setContentProvider(getStepProvider());
		stepViewer.setLabelProvider(new StepLabelProvider());
		
		if (selectionListener != null)
			stepViewer.addSelectionChangedListener(selectionListener);
		
		return composite;
	}

	/**
	 * Creates an image descriptor.
	 */
	private Image createImage(String iconFileName) {
		String iconPath = "icons/full/clcl16/"; //$NON-NLS-1$
		ImageDescriptor desc = null;
		try {
			URL url_basic = WorkbenchPlugin.getDefault().getDescriptor().getInstallURL();
			URL url = new URL(url_basic, iconPath + iconFileName);
			desc = ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return null;
		}
		
		return desc.createImage();
	}

	/**
	 * Return the image indicating a step is current
	 */
	private Image getCurrentImage() {
		if (currentImage == null) {
			currentImage = createImage("step_current.gif"); //$NON-NLS-1$
		}
		return currentImage;
	}
	
	/**
	 * Return the image indicating a step is done
	 */
	private Image getDoneImage() {
		if (doneImage == null) {
			doneImage = createImage("step_done.gif"); //$NON-NLS-1$
		}
		return doneImage;
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
	 * Returns the steps.
	 */
	public WizardStep[] getSteps() {
		if (stepViewer != null) {
			StepRoot root = (StepRoot)stepViewer.getInput();
			if (root != null)
				return root.getSteps();
		}
		
		return new WizardStep[0];
	}
	
	/**
	 * Marks the current step as being done
	 */
	public void markStepAsDone() {
		if (currentStep != null)
			currentStep.markAsDone();
	}
	
	/**
	 * Sets the current step being worked on. Assumes
	 * the step provided exist in the steps within the
	 * group's viewer.
	 * 
	 * @param step the wizard step being worked on
	 */
	public void setCurrentStep(WizardStep step) {
		WizardStep oldStep = currentStep;
		currentStep = step;
		if (stepViewer != null) {
			if (oldStep != null)
				stepViewer.update(oldStep, null);
			if (currentStep != null)
				stepViewer.update(currentStep, null);
		}
	}
	
	/**
	 * Set the current listener interested when the selection
	 * state changes.
	 * 
	 * @param listener The selection listener to set
	 */
	public void setSelectionListener(ISelectionChangedListener listener) {
		if (selectionListener != null && stepViewer != null)
			stepViewer.removeSelectionChangedListener(selectionListener);
		selectionListener = listener;
		if (selectionListener != null && stepViewer != null)
			stepViewer.addSelectionChangedListener(selectionListener);
	}

	/**
	 * Sets the steps to be displayed. Ignored is the
	 * method createContents has not been called yet.
	 * 
	 * @param steps the collection of steps
	 */
	public void setSteps(WizardStep[] steps) {
		if (stepViewer != null)
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
						if (step.isDone())
							image = getDoneImage();
						else if (step == currentStep)
							image = getCurrentImage();
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
