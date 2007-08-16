/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.Util;

/**
 * QuickFixPage is a page for the quick fixes of a marker.
 * 
 * @since 3.4
 * 
 */
public class QuickFixPage extends WizardPage {

	private IMarker marker;
	private IMarkerResolution[] resolutions;
	private ListViewer resolutionsList;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param marker
	 * @param resolutions
	 */
	public QuickFixPage(IMarker marker, IMarkerResolution[] resolutions) {
		super(marker.getAttribute(MarkerUtilities.ATTRIBUTE_NAME,
				MarkerUtilities.EMPTY_STRING));
		this.marker = marker;
		this.resolutions = resolutions;
	}

	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		// Create a new composite as there is the title bar seperator
		// to deal with
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(control);
		control.setLayout(new GridLayout());

		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel
				.setText(MarkerMessages.MarkerResolutionDialog_Resolutions_List_Title);

		resolutionsList = new ListViewer(control, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL);
		resolutionsList.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return resolutions;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}
		});

		resolutionsList.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((IMarkerResolution) element).getLabel();
			}
		});

		resolutionsList.setInput(this);

		resolutionsList.setComparator(new ViewerComparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((IMarkerResolution) e1).getLabel().compareTo(
						((IMarkerResolution) e1).getLabel());
			}
		});

		resolutionsList.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		String message = NLS.bind(
				MarkerMessages.MarkerResolutionDialog_Description, Util
						.getProperty(IMarker.MESSAGE, marker));
		if (message.length() > 50) {
			// Add a carriage return in the middle if we can
			int insertionIndex = chooseWhitespace(message);
			if (insertionIndex > 0) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(message.substring(0, insertionIndex));
				buffer.append("\n"); //$NON-NLS-1$
				buffer.append(message.substring(insertionIndex, message
						.length()));
				message = buffer.toString();
			}
		}

		setTitle(MarkerMessages.MarkerResolutionDialog_Title);
		setMessage(message);
		Dialog.applyDialogFont(control);
	}

	/**
	 * Choose a good whitespace position for a page break. Start in the middle
	 * of the message.
	 * 
	 * @param message
	 * @return int -1 if there is no whitespace to choose.
	 */
	private int chooseWhitespace(String message) {

		for (int i = message.length() / 2; i < message.length(); i++) {
			if (Character.isWhitespace(message.charAt(i)))
				return i;
		}
		return -1;
	}

	/**
	 * Return the marker being edited.
	 * 
	 * @return IMarker
	 */
	public IMarker getMarker() {
		return marker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		return true;
	}

	/**
	 * Finish has been pressed. Process the resolutions. monitor the monitor to
	 * report to.
	 */
	void performFinish(IProgressMonitor monitor) {

		ISelection selected = resolutionsList.getSelection();
		if (selected.isEmpty())
			return;

		IMarkerResolution resolution = (IMarkerResolution) ((IStructuredSelection) selected)
				.getFirstElement();

		if (resolution instanceof WorkbenchMarkerResolution) {

			((WorkbenchMarkerResolution) resolution).run(
					new IMarker[] { marker },
					new SubProgressMonitor(monitor, 1));
		} else {

			// Allow paint events and wake up the button
			getShell().getDisplay().readAndDispatch();
			if (monitor.isCanceled()) {

				monitor.subTask(Util.getProperty(IMarker.MESSAGE, marker));
				resolution.run(marker);
				monitor.worked(1);
			}

		}

	}

}
