/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

 
import java.util.*;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement.ProjectElementSorter;
import org.eclipse.team.internal.ccvs.ui.repo.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog to prompt the user to choose a tag for a selected resource
 */
public class TagSelectionDialog extends Dialog{
	private ICVSFolder[] folders;
	private int includeFlags;
	private CVSTag result;
	private String helpContext;
	private IStructuredSelection selection;
	
	public static final int INCLUDE_HEAD_TAG = ProjectElement.INCLUDE_HEAD_TAG;
	public static final int INCLUDE_BASE_TAG = ProjectElement.INCLUDE_BASE_TAG;
	public static final int INCLUDE_BRANCHES = ProjectElement.INCLUDE_BRANCHES;
	public static final int INCLUDE_VERSIONS = ProjectElement.INCLUDE_VERSIONS;
	public static final int INCLUDE_DATES = ProjectElement.INCLUDE_DATES;
	public static final int INCLUDE_ALL_TAGS = ProjectElement.INCLUDE_ALL_TAGS;
	
	// widgets;
	private TreeViewer tagTree;
	private Button okButton;
	
	// dialog title, should indicate the action in which the tag selection
	// dialog is being shown
	private String title;
	private String message;
	
	private boolean recurse = true;
	private boolean showRecurse;
	
	// constants
	private static final int SIZING_DIALOG_WIDTH = 400;
	private static final int SIZING_DIALOG_HEIGHT = 250;
	
	public static CVSTag getTagToCompareWith(Shell shell, IProject[] projects) {
		return getTagToCompareWith(shell, getCVSFoldersFor(projects));
	}
		
	public static CVSTag getTagToCompareWith(Shell shell, ICVSFolder[] folders) {
		TagSelectionDialog dialog = new TagSelectionDialog(shell, folders, 
			Policy.bind("CompareWithTagAction.message"),  //$NON-NLS-1$
			Policy.bind("TagSelectionDialog.Select_a_Tag_1"), //$NON-NLS-1$
			TagSelectionDialog.INCLUDE_ALL_TAGS, 
			false, /* show recurse*/
			IHelpContextIds.COMPARE_TAG_SELECTION_DIALOG);
		dialog.setBlockOnOpen(true);
		int result = dialog.open();
		if (result == Dialog.CANCEL) {
			return null;
		}
		return dialog.getResult();
	}
	/**
	 * Creates a new TagSelectionDialog.
	 * @param resource The resource to select a version for.
	 */
	public TagSelectionDialog(Shell parentShell, IProject[] projects, String title, String message, int includeFlags, boolean showRecurse, String helpContext) {
		this(parentShell, getCVSFoldersFor(projects), title, message, includeFlags, showRecurse, helpContext); //$NON-NLS-1$		
	}
	
	private static ICVSFolder[] getCVSFoldersFor(IProject[] projects) {
		ICVSFolder[] folders = new ICVSFolder[projects.length];
		for (int i = 0; i < projects.length; i++) {
			folders[i] = CVSWorkspaceRoot.getCVSFolderFor(projects[i]);
		}
		return folders;
	}
	
	/**
	 * Creates a new TagSelectionDialog.
	 * @param resource The resource to select a version for.
	 */
	public TagSelectionDialog(Shell parentShell, ICVSFolder[] folders, String title, String message, int includeFlags, boolean showRecurse, String helpContext) {
		super(parentShell);
		this.folders = folders;
		this.title = title;
		this.message = message;
		this.includeFlags = includeFlags;
		this.showRecurse = showRecurse;
		this.helpContext = helpContext;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}
	
	/**
	 * Creates this window's widgetry.
	 * <p>
	 * The default implementation of this framework method
	 * creates this window's shell (by calling <code>createShell</code>),
	 * its control (by calling <code>createContents</code>),
	 * and initializes this window's shell bounds 
	 * (by calling <code>initializeBounds</code>).
	 * This framework method may be overridden; however,
	 * <code>super.create</code> must be called.
	 * </p>
	 */
	public void create() {
		super.create();
		initialize();
	}
	
	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/**
	 * Creates and returns the contents of the upper part 
	 * of this dialog (above the button bar).
	 * <p>
	 * The default implementation of this framework method
	 * creates and returns a new <code>Composite</code> with
	 * standard margins and spacing.
	 * Subclasses should override.
	 * </p>
	 *
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite)super.createDialogArea(parent);
		// Add F1 help
		if (helpContext != null) {
			WorkbenchHelp.setHelp(top, helpContext);
		}
		Composite inner = new Composite(top, SWT.NULL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_DIALOG_WIDTH;
		data.heightHint = SIZING_DIALOG_HEIGHT;
		inner.setLayoutData(data);
		GridLayout layout = new GridLayout();
		inner.setLayout(layout);
		
		Label l = new Label (inner, SWT.NONE);
		l.setText(message); //$NON-NLS-1$
		
		tagTree = createTree(inner);
		tagTree.setInput(new ProjectElement(folders[0], includeFlags));
		tagTree.setSorter(new ProjectElementSorter());
		Runnable refresh = new Runnable() {
			public void run() {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						tagTree.refresh();
					}
				});
			}
		};
		
		// Create the popup menu
		MenuManager menuMgr = new MenuManager();
		Tree tree = tagTree.getTree();
		Menu menu = menuMgr.createContextMenu(tree);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				addMenuItemActions(manager);
			}

		});
		menuMgr.setRemoveAllWhenShown(true);
		tree.setMenu(menu);
		
		if(showRecurse) {
			final Button recurseCheck = new Button(top, SWT.CHECK);
			recurseCheck.setText(Policy.bind("TagSelectionDialog.recurseOption")); //$NON-NLS-1$
			recurseCheck.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					recurse = recurseCheck.getSelection();
				}
			});
			recurseCheck.setSelection(true);
		}

		
		TagConfigurationDialog.createTagDefinitionButtons(getShell(), top, folders, 
														  convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT), 
														  convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
														  refresh, refresh);
		
		Label seperator = new Label(top, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData (GridData.FILL_BOTH);		
		data.horizontalSpan = 2;
		seperator.setLayoutData(data);
		
		updateEnablement();
        Dialog.applyDialogFont(parent);
        
		return top;
	}
	
	
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	
	protected TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.MULTI | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));	
		TreeViewer result = new TreeViewer(tree);
		result.setContentProvider(new WorkbenchContentProvider());
		result.setLabelProvider(new WorkbenchLabelProvider());
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {				
				updateEnablement();
			}
		});
		// select and close on double click
		// To do: use defaultselection instead of double click
		result.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
				if (!selection.isEmpty() && (selection.getFirstElement() instanceof TagElement)) {
					okPressed();
				}
			}
		});
		result.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				handleKeyPressed(event);
			}
			public void keyReleased(KeyEvent event) {
				handleKeyReleased(event);
			}
		});
		result.setSorter(new RepositorySorter());
		return result;
	}
	
	/**
	 * Returns the selected tag.
	 */
	public CVSTag getResult() {
		return result;
	}
	
	public boolean getRecursive() {
		return recurse;
	}

	/**
	 * Initializes the dialog contents.
	 */
	protected void initialize() {
		okButton.setEnabled(false);
	}
	
	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The default implementation of this framework method sets
	 * this dialog's return code to <code>Window.OK</code>
	 * and closes the dialog. Subclasses may override.
	 * </p>
	 */
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
		Object o = selection.getFirstElement();
		TagElement element = (TagElement)o;
		result = element.getTag();
		super.okPressed();
	}

	
	/**
	 * Updates the dialog enablement.
	 */
	protected void updateEnablement() {
		selection = (IStructuredSelection)tagTree.getSelection();		
		if(okButton!=null) {
			if (selection.isEmpty() || selection.size() != 1 || !(selection.getFirstElement() instanceof TagElement)) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
	}

	public void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {			
			deleteDateTag();
		}
	}
	private void deleteDateTag() {
		TagElement[] selectedDateTagElements = getSelectedDateTagElement();
		if (selectedDateTagElements.length == 0) return;
		for(int i = 0; i < selectedDateTagElements.length; i++){
			RepositoryManager mgr = CVSUIPlugin.getPlugin().getRepositoryManager();
			CVSTag tag = selectedDateTagElements[i].getTag();
			if(tag.getType() == CVSTag.DATE){
				mgr.removeDateTag(getLocation(),tag);
			}				
		}
		tagTree.refresh();
		updateEnablement();
	}

	protected void handleKeyReleased(KeyEvent event) {
	}
	
	private ICVSRepositoryLocation getLocation(){
		RepositoryManager mgr = CVSUIPlugin.getPlugin().getRepositoryManager();
		ICVSRepositoryLocation location = mgr.getRepositoryLocationFor( folders[0]);
		return location;
	}
	
	/**
	 * Returns the selected date tag elements
	 */
	private TagElement[] getSelectedDateTagElement() {
		ArrayList dateTagElements = null;
		if (selection!=null && !selection.isEmpty()) {
			dateTagElements = new ArrayList();
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				Object next = CVSAction.getAdapter(elements.next(), TagElement.class);
				if (next instanceof TagElement) {
					if(((TagElement)next).getTag().getType() == CVSTag.DATE){
						dateTagElements.add(next);
					}
				}
			}
		}
		if (dateTagElements != null && !dateTagElements.isEmpty()) {
			TagElement[] result = new TagElement[dateTagElements.size()];
			dateTagElements.toArray(result);
			return result;
		}
		return new TagElement[0];
	}
	private void addDateTag(CVSTag tag){
		if(tag == null) return;
		List dateTags = new ArrayList();
		dateTags.addAll(Arrays.asList(CVSUIPlugin.getPlugin().getRepositoryManager().getKnownTags(folders[0],CVSTag.DATE)));
		if(!dateTags.contains( tag)){
			CVSUIPlugin.getPlugin().getRepositoryManager().addDateTag(getLocation(),tag);
		}
		try {
			tagTree.getControl().setRedraw(false);
			tagTree.refresh();
			// TODO: Hack to instantiate the model before revealing the selection
			Object[] expanded = tagTree.getExpandedElements();
			tagTree.expandToLevel(2);
			tagTree.collapseAll();
			for (int i = 0; i < expanded.length; i++) {
				Object object = expanded[i];
				tagTree.expandToLevel(object, 1);
			}
			// Reveal the selection
			tagTree.reveal(new TagElement(tag));
			tagTree.setSelection(new StructuredSelection(new TagElement(tag)));
		} finally {
			tagTree.getControl().setRedraw(true);
		}
		updateEnablement();
	}
	private void addMenuItemActions(IMenuManager manager) {
		manager.add(new Action(Policy.bind("TagSelectionDialog.0")) { //$NON-NLS-1$
			public void run() {
				CVSTag dateTag = NewDateTagAction.getDateTag(getShell(), CVSUIPlugin.getPlugin().getRepositoryManager().getRepositoryLocationFor(folders[0]));
				addDateTag(dateTag);
			}
		});
		if(getSelectedDateTagElement().length > 0){
			manager.add(new Action(Policy.bind("TagSelectionDialog.1")) { //$NON-NLS-1$
				public void run() {
					deleteDateTag();
				}
			});			
		}

	}

}
