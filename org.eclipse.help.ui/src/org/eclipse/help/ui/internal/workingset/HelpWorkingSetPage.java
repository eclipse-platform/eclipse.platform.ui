package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.*;

/**
 * Page for help working sets.
 */
public class HelpWorkingSetPage extends WizardPage implements IWorkingSetPage {

	public final static String PAGE_ID = "helpWorkingSetPage";
	public final static String PAGE_TITLE = "Help Working Set";

	private Text workingSetName;
	private CheckboxTreeViewer tree;
	private ITreeContentProvider treeContentProvider;
	private ILabelProvider elementLabelProvider;

	private boolean firstCheck;
	private IWorkingSet workingSet;

	/**
	 * Default constructor.
	 */
	public HelpWorkingSetPage() {
		super(PAGE_ID, PAGE_TITLE, null);
		setDescription("help working set description");
		firstCheck = true;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label label = new Label(composite, SWT.WRAP);
		label.setText("WorkingSetName");
		GridData gd =
			new GridData(
				GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		workingSetName = new Text(composite, SWT.SINGLE | SWT.BORDER);
		workingSetName.setLayoutData(
			new GridData(
				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		workingSetName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		workingSetName.setFocus();

		label = new Label(composite, SWT.WRAP);
		label.setText("WorkingSetContent");
		gd =
			new GridData(
				GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		tree =
			new CheckboxTreeViewer(
				composite,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint = convertHeightInCharsToPixels(15);
		tree.getControl().setLayoutData(gd);

		treeContentProvider = new HelpWorkingSetTreeContentProvider();
		tree.setContentProvider(treeContentProvider);

		elementLabelProvider = new HelpWorkingSetElementLabelProvider();
		tree.setLabelProvider(elementLabelProvider);

		tree.setUseHashlookup(true);

		tree.setInput(
			new HelpResource(WorkbenchHelp.getHelpSupport().getTocs()));

		tree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});

		tree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
			}
			public void treeExpanded(TreeExpansionEvent event) {
				final Object element = event.getElement();
				if (tree.getGrayed(element) == false)
					BusyIndicator
						.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						setSubtreeChecked(
							element,
							tree.getChecked(element),
							false);
					}
				});
			}
		});

		if (workingSet != null)
			workingSetName.setText(workingSet.getName());
		initializeCheckedState();
		validateInput();

		// Set help for the page 
		//WorkbenchHelp.setHelp(tree, "help_workingset_page");
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
	 */
	public void finish() {
		String workingSetName = this.workingSetName.getText();
		ArrayList elements = new ArrayList(10);
		findCheckedElements(elements, tree.getInput());
		if (workingSet == null) {
			IWorkingSetManager workingSetManager =
				PlatformUI.getWorkbench().getWorkingSetManager();
			workingSet =
				workingSetManager.createWorkingSet(
					workingSetName,
					(IAdaptable[]) elements.toArray(
						new IAdaptable[elements.size()]));
		} else {
			workingSet.setName(workingSetName);
			workingSet.setElements(
				(IAdaptable[]) elements.toArray(
					new IAdaptable[elements.size()]));
		}
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
	 */
	public IWorkingSet getSelection() {
		return workingSet;
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(org.eclipse.ui.IWorkingSet)
	 */
	public void setSelection(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet, "Working set must not be null");
		this.workingSet = workingSet;
		if (getContainer() != null
			&& getShell() != null
			&& workingSetName != null) {
			firstCheck = false;
			workingSetName.setText(workingSet.getName());
			initializeCheckedState();
			validateInput();
		}
	}

	private void validateInput() {
		String errorMessage = null;
		String newText = workingSetName.getText();

		if (newText.equals(newText.trim()) == false)
			errorMessage =
				"The name must not have leading or trailing whitespace";
		if (newText.equals("")) {
			if (firstCheck) {
				setPageComplete(false);
				firstCheck = false;
				return;
			} else
				errorMessage = "The name must not be empty";
		}

		firstCheck = false;

		if (errorMessage == null
			&& (workingSet == null
				|| newText.equals(workingSet.getName()) == false)) {
			IWorkingSet[] workingSets =
				PlatformUI
					.getWorkbench()
					.getWorkingSetManager()
					.getWorkingSets();
			for (int i = 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName())) {
					errorMessage = "The working set already exists";
				}
			}
		}
		if (errorMessage == null && tree.getCheckedElements().length == 0)
			errorMessage = "Need to select something";

		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	private void initializeCheckedState() {
		if (workingSet == null)
			return;

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Object[] elements = workingSet.getElements();
				tree.setCheckedElements(elements);
				for (int i = 0; i < elements.length; i++) {
					Object element = elements[i];
					if (isExpandable(element))
						setSubtreeChecked(element, true, true);
					updateParentState(element, true);
				}
			}
		});
	}

	private boolean isExpandable(Object element) {
		return treeContentProvider.hasChildren(element);
	}

	private void updateParentState(Object child, boolean baseChildState) {
		if (child == null)
			return;

		Object parent = treeContentProvider.getParent(child);
		if (parent == null)
			return;

		boolean allSameState = true;
		Object[] children = null;
		children = treeContentProvider.getChildren(parent);

		for (int i = children.length - 1; i >= 0; i--) {
			if (tree.getChecked(children[i]) != baseChildState
				|| tree.getGrayed(children[i])) {
				allSameState = false;
				break;
			}
		}

		tree.setGrayed(parent, !allSameState);
		tree.setChecked(parent, !allSameState || baseChildState);

		updateParentState(parent, baseChildState);
	}

	private void setSubtreeChecked(
		Object parent,
		boolean state,
		boolean checkExpandedState) {

		Object[] children = treeContentProvider.getChildren(parent);
		for (int i = children.length - 1; i >= 0; i--) {
			Object element = children[i];
			if (state) {
				tree.setChecked(element, true);
				tree.setGrayed(element, false);
			} else
				tree.setGrayChecked(element, false);
			if (isExpandable(element))
				setSubtreeChecked(element, state, checkExpandedState);
		}
	}

	private void findCheckedElements(
		java.util.List checkedResources,
		Object parent) {
		Object[] children = treeContentProvider.getChildren(parent);
		for (int i = 0; i < children.length; i++) {
			if (tree.getGrayed(children[i]))
				findCheckedElements(checkedResources, children[i]);
			else if (tree.getChecked(children[i]))
				checkedResources.add(children[i]);
		}
	}

	void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Object element = event.getElement();
				boolean state = event.getChecked();
				tree.setGrayed(element, false);
				if (isExpandable(element))
					setSubtreeChecked(element, state, state);
				// only check subtree if state is set to true

				updateParentState(element, state);
				validateInput();
			}
		});
	}

}