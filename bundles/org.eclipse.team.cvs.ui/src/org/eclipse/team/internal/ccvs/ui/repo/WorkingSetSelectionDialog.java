package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WorkingSetSelectionDialog extends SelectionDialog {
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

	private static class WorkingSetLabelProvider extends LabelProvider {
		private Map icons;

		public WorkingSetLabelProvider() {
			icons = new Hashtable();
		}
		public void dispose() {
			Iterator iterator = icons.values().iterator();
			
			while (iterator.hasNext()) {
				Image icon = (Image) iterator.next();
				icon.dispose();
			}
			super.dispose();
		}
		public Image getImage(Object workingSet) {
			return null;
		}
		public String getText(Object workingSet) {
			return ((CVSWorkingSet) workingSet).getName();
		}
	}

	public class ListContentProvider implements IStructuredContentProvider {
		List fContents;	
	
		public ListContentProvider() {
		}
		
		public Object[] getElements(Object input) {
			if (fContents != null && fContents == input)
				return fContents.toArray();
			return new Object[0];
		}
	
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof List) 
				fContents= (List)newInput;
			else
				fContents= null;
			// we use a fixed set.
		}
	
		public void dispose() {
		}
		
		public boolean isDeleted(Object o) {
			return fContents != null && !fContents.contains(o);
		}
	}

	private ILabelProvider labelProvider;
	private IStructuredContentProvider contentProvider;
	private TableViewer listViewer;
	private Button newButton;
	private Button detailsButton;
	private Button removeButton;
	private CVSWorkingSet[] result;
	private boolean multiSelect;
	private List addedWorkingSets;	
	private List removedWorkingSets;
	private Map editedWorkingSets;

	/**
	 * Creates a working set selection dialog.
	 *
	 * @param parentShell the parent shell
	 * @param multi true=more than one working set can be chosen 
	 * 	in the dialog. false=only one working set can be chosen. Multiple
	 * 	working sets can still be selected and removed from the list but
	 * 	the dialog can only be closed when a single working set is selected.
	 */
	public WorkingSetSelectionDialog(Shell parentShell, boolean multi) {
		super(parentShell);
		contentProvider = new ListContentProvider();
		labelProvider = new WorkingSetLabelProvider();
		multiSelect = multi;		
		if (multiSelect) {
			setTitle(Policy.bind("WorkingSetSelectionDialog.title.multiSelect")); //$NON-NLS-1$;
			setMessage(Policy.bind("WorkingSetSelectionDialog.message.multiSelect")); //$NON-NLS-1$
		}
		else {
			setTitle(Policy.bind("WorkingSetSelectionDialog.title")); //$NON-NLS-1$;
			setMessage(Policy.bind("WorkingSetSelectionDialog.message")); //$NON-NLS-1$
		}
	}
	/**
	 * Adds the modify buttons to the dialog.
	 * 
	 * @param composite Composite to add the buttons to
	 */
	private void addModifyButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		int id = IDialogConstants.CLIENT_ID + 1;
		newButton = createButton(buttonComposite, id++, Policy.bind("WorkingSetSelectionDialog.newButton.label"), false); //$NON-NLS-1$
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createWorkingSet();
			}
		});

		detailsButton = createButton(buttonComposite, id++, Policy.bind("WorkingSetSelectionDialog.detailsButton.label"), false); //$NON-NLS-1$
		detailsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSelectedWorkingSet();
			}
		});

		removeButton = createButton(buttonComposite, id++, Policy.bind("WorkingSetSelectionDialog.removeButton.label"), false); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSelectedWorkingSets();
			}
		});
	}
	/**
	 * Overrides method from Dialog.
	 * 
	 * @see Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		restoreAddedWorkingSets();
		restoreChangedWorkingSets();
		restoreRemovedWorkingSets();
		super.cancelPressed();
	}
	/** 
	 * Overrides method from Window.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
// todo
//		WorkbenchHelp.setHelp(shell, IHelpContextIds.WORKING_SET_SELECTION_DIALOG);
	}
	/**
	 * Overrides method from Dialog.
	 * Create the dialog widgets.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		createMessageArea(composite);
		listViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);
		
		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(contentProvider);
		listViewer.setSorter(new WorkbenchViewerSorter());
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		addModifyButtons(composite);
		listViewer.setInput(Arrays.asList(CVSUIPlugin.getPlugin().getRepositoryManager().getWorkingSets()));

		return composite;
	}
	/**
	 * Overrides method from Dialog.
	 * Sets the initial selection, if any.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		List selections = getInitialElementSelections();
		if (!selections.isEmpty()) {
			listViewer.setSelection(new StructuredSelection(selections), true);
		}
		updateButtonAvailability();

		return control;
	}
	/**
	 * Opens a working set wizard for creating a new working set.
	 */
	private void createWorkingSet() {
		CVSWorkingSetWizard wizard = new CVSWorkingSetWizard();
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
//		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.WORKING_SET_NEW_WIZARD);
		if (dialog.open() == Window.OK) {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();			
			CVSWorkingSet workingSet = wizard.getSelection();

			listViewer.add(workingSet);
			listViewer.setSelection(new StructuredSelection(workingSet), true);
			manager.addWorkingSet(workingSet);
			addedWorkingSets.add(workingSet);
		}
	}
	/**
	 * Opens a working set wizard for editing the currently selected 
	 * working set.
	 * 
	 * @see org.eclipse.ui.IWorkingSetPage
	 */
	private void editSelectedWorkingSet() {
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();			
		CVSWorkingSet editWorkingSet = (CVSWorkingSet) getSelectedWorkingSets().get(0);		
		CVSWorkingSetWizard wizard = new CVSWorkingSetWizard(editWorkingSet);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		CVSWorkingSet originalWorkingSet = (CVSWorkingSet) editedWorkingSets.get(editWorkingSet);
		boolean firstEdit = originalWorkingSet == null;
		
		// save the original working set values for restoration when selection 
		// dialog is cancelled.
		if (firstEdit) {
			originalWorkingSet = (CVSWorkingSet)editWorkingSet.clone();
		}
		else {
			editedWorkingSets.remove(editWorkingSet);
		}
		dialog.create();
//		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.WORKING_SET_EDIT_WIZARD);
		if (dialog.open() == Window.OK) {		
			editWorkingSet = (CVSWorkingSet) wizard.getSelection();
			listViewer.update(editWorkingSet, null);
		}
		editedWorkingSets.put(editWorkingSet, originalWorkingSet);
	}
	/**
	 * Implements IWorkingSetSelectionDialog.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#getSelection()
	 */
	public CVSWorkingSet[] getSelection() {
		return result;
	}
	/**
	 * Returns the selected working sets.
	 * 
	 * @return the selected working sets
	 */
	private List getSelectedWorkingSets() {
		ISelection selection = listViewer.getSelection();
		if (selection instanceof IStructuredSelection)
			return ((IStructuredSelection) selection).toList();
		return null;
	}
	/**
	 * Called when the selection has changed.
	 */
	private void handleSelectionChanged() {
		updateButtonAvailability();
	}
	/**
	 * Sets the selected working sets as the dialog result.
	 * Overrides method from Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		List newResult = getSelectedWorkingSets();

		result = (CVSWorkingSet[]) newResult.toArray(new CVSWorkingSet[newResult.size()]);
		setResult(newResult);
		super.okPressed();
	}
	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#open()
	 */
	public int open() {
		addedWorkingSets = new ArrayList();
		removedWorkingSets = new ArrayList();
		editedWorkingSets = new HashMap();
		return super.open();
	}
	/**
	 * Removes the selected working sets from the workbench.
	 */
	private void removeSelectedWorkingSets() {
		ISelection selection = listViewer.getSelection();

		if (selection instanceof IStructuredSelection) {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			Iterator iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				CVSWorkingSet workingSet = (CVSWorkingSet) iter.next();
				manager.removeWorkingSet(workingSet);
				if (addedWorkingSets.contains(workingSet)) {
					addedWorkingSets.remove(workingSet);
				}
				else {
					removedWorkingSets.add(workingSet);
				}
			}
			listViewer.remove(((IStructuredSelection) selection).toArray());
		}			
	}
	/**
	 * Removes newly created working sets from the working set manager.
	 */
	private void restoreAddedWorkingSets() {
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		Iterator iterator = addedWorkingSets.iterator();
		
		while (iterator.hasNext()) {
			manager.removeWorkingSet(((CVSWorkingSet) iterator.next()));
		}		
	}
	/**
	 * Rolls back changes to working sets.
	 */
	private void restoreChangedWorkingSets() {
		Iterator iterator = editedWorkingSets.keySet().iterator();
		
		while (iterator.hasNext()) {
			CVSWorkingSet editedWorkingSet = (CVSWorkingSet) iterator.next();
			CVSWorkingSet originalWorkingSet = (CVSWorkingSet) editedWorkingSets.get(editedWorkingSet);
						
			if (editedWorkingSet.getName().equals(originalWorkingSet.getName()) == false) {
				editedWorkingSet.setName(originalWorkingSet.getName());
			}
			if (!editedWorkingSet.equals(originalWorkingSet)) {
				editedWorkingSet.mutate(originalWorkingSet);
			}
		}		
	}

	/**
	 * Adds back removed working sets to the working set manager.
	 */
	private void restoreRemovedWorkingSets() {
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		Iterator iterator = removedWorkingSets.iterator();
		
		while (iterator.hasNext()) {
			manager.addWorkingSet(((CVSWorkingSet) iterator.next()));
		}		
	}
	/**
	 * Implements IWorkingSetSelectionDialog.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(IWorkingSet[])
	 */
	public void setSelection(CVSWorkingSet[] workingSets) {
		result = workingSets;
		setInitialSelections(workingSets);
	}
	/**
	 * Updates the modify buttons' enabled state based on the 
	 * current seleciton.
	 */
	private void updateButtonAvailability() {
		ISelection selection = listViewer.getSelection();
		boolean hasSelection = selection != null && !selection.isEmpty();
		boolean hasSingleSelection = hasSelection;

		removeButton.setEnabled(hasSelection);
		if (hasSelection && selection instanceof IStructuredSelection) {
			hasSingleSelection = ((IStructuredSelection) selection).size() == 1;
		}
		detailsButton.setEnabled(hasSingleSelection);
		if (multiSelect == false) {
			getOkButton().setEnabled(hasSelection == false || hasSingleSelection);
		}
	}
}
