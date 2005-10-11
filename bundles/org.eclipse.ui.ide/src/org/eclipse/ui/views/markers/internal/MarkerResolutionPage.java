package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;

/**
 * MarkerResolutionPage is the page for marker resolution wizards.
 * 
 * @since 3.2
 */
public class MarkerResolutionPage extends WizardPage {

	private Collection markers = new ArrayList();

	private Collection otherMarkers = new ArrayList();

	private IMarkerResolution[] resolutions;

	private CheckboxTableViewer markersTable;

	private ListViewer resolutionsList;

	/**
	 * Create a new instance of the receiver with the given resolutions.
	 * 
	 * @param newResolutions
	 */
	public MarkerResolutionPage(IMarkerResolution[] newResolutions) {
		super("MarkerPage");//$NON-NLS-1$
		resolutions = newResolutions;
		setTitle(MarkerMessages.MarkerResolutionPage_Title);
		setDescription(MarkerMessages.MarkerResolutionPage_Description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());
		initializeDialogUnits(control);

		Label title = new Label(control, SWT.NONE);
		title.setText(MarkerMessages.MarkerResolutionPage_Problems_List_Title);

		markersTable = CheckboxTableViewer.newCheckList(control, SWT.BORDER
				| SWT.V_SCROLL);
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableData.heightHint = convertHeightInCharsToPixels(10);
		markersTable.getControl().setLayoutData(tableData);

		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel
				.setText(MarkerMessages.MarkerResolutionPage_Resolutions_List_Title);

		resolutionsList = new ListViewer(control, SWT.BORDER | SWT.SINGLE);
		GridData listData = new GridData(SWT.FILL, SWT.NONE, true, false);
		listData.heightHint = convertHeightInCharsToPixels(10);
		resolutionsList.getControl().setLayoutData(listData);

		markersTable.setContentProvider(new IStructuredContentProvider() {
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
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return markers.toArray();
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

		markersTable.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return Util.getProperty(IMarker.MESSAGE, ((IMarker) element));
			}

			public Image getImage(Object element) {
				return Util.getImage(((IMarker) element).getAttribute(
						IMarker.SEVERITY, -1));
			}
		});

		markersTable.setInput(this);

		resolutionsList.setContentProvider(new IStructuredContentProvider() {
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
			public String getText(Object element) {
				return ((IMarkerResolution) element).getLabel();
			}
		});

		resolutionsList
				.addSelectionChangedListener(new ISelectionChangedListener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
					 */
					public void selectionChanged(SelectionChangedEvent event) {
						setPageComplete(!event.getSelection().isEmpty());

					}
				});

		resolutionsList.setInput(this);
		setControl(control);

		Dialog.applyDialogFont(control);

		// If there is only one select it
		if (resolutionsList.getList().getItemCount() == 1) {
			resolutionsList.getList().select(0);
			setPageComplete(true);
		} else
			setPageComplete(false);

		markersTable.setAllChecked(true);

	}

	/**
	 * Add the marker to the main set.
	 * 
	 * @param marker
	 */
	public void addMarker(IMarker marker) {
		markers.add(marker);
	}

	/**
	 * Add marker to the other markers for the receiver.
	 * 
	 * @param marker
	 */
	public void addOtherMarker(IMarker marker) {
		otherMarkers.add(marker);

	}

	/**
	 * Return all of the resolutions to choose from in the receiver.
	 * 
	 * @return IMarkerResolution[]
	 */
	public IMarkerResolution[] getResolutions() {
		return resolutions;
	}

	/**
	 * Get a runnable for running the end of the operation.
	 * @return IRunnableWithProgress
	 */
	public IRunnableWithProgress getCompletionRunnable() {
		return new IRunnableWithProgress() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(IProgressMonitor monitor){

				ISelection selected = resolutionsList.getSelection();
				if (!(selected instanceof IStructuredSelection))
					return;

				Object[] checked = markersTable.getCheckedElements();
				IMarkerResolution resolution = (IMarkerResolution) ((IStructuredSelection) selected)
						.getFirstElement();
				monitor.beginTask(NLS.bind(
						MarkerMessages.MarkerResolutionPage_Fixing, resolution
								.getLabel()), checked.length + 1);
				monitor.worked(1);
				
				for (int i = 0; i < checked.length; i++) {
					IMarker marker = (IMarker) checked[i];
					IMarkerResolution[] newResolutions = IDE
							.getMarkerHelpRegistry().getResolutions(marker);

					if (newResolutions.length == 0) {
						MessageDialog
								.openInformation(
										getShell(),
										MarkerMessages.MarkerResolutionPage_CannotFixTitle,
										NLS
												.bind(
														MarkerMessages.MarkerResolutionPage_NoResolutionsMessage,
														getDescription(marker)));
						return;
					}

					IMarkerResolution matching = getResolutionMatching(
							resolution, newResolutions);
					if (matching == null) {
						MessageDialog
								.openInformation(
										getShell(),
										MarkerMessages.MarkerResolutionPage_CannotFixTitle,
										NLS
												.bind(
														MarkerMessages.MarkerResolutionPage_NoMatchMessage,
														getDescription(marker)));
						return;
					}
					matching.run(marker);
					monitor.worked(1);

				
				}
				monitor.done();

			}
		};
	}


	/**
	 * Return the choice whose label matches allChoices.
	 * 
	 * @param resolution
	 * @param allChoices
	 * @return IMarkerResolution or <code>null</code> if it cannot be found
	 */
	private IMarkerResolution getResolutionMatching(
			IMarkerResolution resolution, IMarkerResolution[] allChoices) {
		Comparator resolutionComparator = MarkerResolutionWizard
				.getResolutionComparator();
		for (int i = 0; i < allChoices.length; i++) {
			if (resolutionComparator.compare(allChoices[i], resolution) == 0)
				return allChoices[i];
		}
		return null;
	}

	/**
	 * Return the description of the element.
	 * 
	 * @param element
	 * @return String
	 */
	String getDescription(IMarker element) {
		return Util.getProperty(IMarker.MESSAGE, element);
	}

}
