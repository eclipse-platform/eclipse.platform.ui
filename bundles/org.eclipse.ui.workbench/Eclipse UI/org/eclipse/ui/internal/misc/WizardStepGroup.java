package org.eclipse.ui.internal.misc;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *   IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 * activated and used by other components.
*********************************************************************/
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.WizardStep;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * A group of controls used to view the list of
 * wizard steps to be done.
 */
public class WizardStepGroup {
	private Image doneImage;
	private Image currentImage;
	private WizardStep currentStep;
	private Composite parentComposite;
	private TableViewer stepViewer;
	private ISelectionChangedListener selectionListener;

	/**
	 * Creates a new instance of the <code>WizardStepGroup</code>
	 * 
	 * @param numberColWidth the width in pixel for the number column
	 */
	public WizardStepGroup() {
		super();
	}

	/**
	 * Create the contents of this group. The basic layout is a table
	 * with a label above it.
	 */
	public Control createContents(Composite parent) {
		Font font = parent.getFont();
		parentComposite = parent;
		
		// Create a composite to hold everything together
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		composite.setFont(font);
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
		label.setFont(font);
		
		// Table viewer of all the steps
		stepViewer = new TableViewer(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_BOTH);
		stepViewer.getTable().setLayoutData(data);
		stepViewer.getTable().setFont(font);
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
			URL url_basic = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
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
				
			// Update the layout so that there is enough
			// room for the icon now.
			if (oldStep == null && currentStep != null)
				parentComposite.layout(true);
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
		if (stepViewer != null) {
			stepViewer.setInput(new StepRoot(steps));
			parentComposite.layout(true);
		}
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
	private class StepLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof WizardStep) {
				WizardStep step = (WizardStep) element;
				return String.valueOf(step.getNumber()) + ". " + step.getLabel(); //$NON-NLS-1$
			}
			
			return ""; //$NON-NLS-1$
		}
		
		public Image getImage(Object element) {
			if (element instanceof WizardStep) {
				WizardStep step = (WizardStep) element;
				if (step.isDone())
					return getDoneImage();
				if (step == currentStep)
					return getCurrentImage();
			}
			
			return null;
		}
	}
}
