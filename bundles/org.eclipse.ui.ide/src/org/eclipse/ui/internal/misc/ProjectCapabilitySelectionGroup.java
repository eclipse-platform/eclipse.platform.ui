package org.eclipse.ui.internal.misc;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *        IBM Corporation - initial API and implementation
 *        Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *        activated and used by other components.
*********************************************************************/
import java.text.Collator;
import java.util.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A group of controls used to view and modify the
 * set of capabilities on a project.
 */
public class ProjectCapabilitySelectionGroup {
	private static final String EMPTY_DESCRIPTION = "\n\n\n"; //$NON-NLS-1$
	
	private CapabilityRegistry registry;
	private ICategory[] initialCategories;
	private Capability[] initialCapabilities;
	private Capability[] disabledCapabilities;
	private boolean modified = false;
	private Text descriptionText;
	private CheckboxTableViewer checkboxViewer;
	private ICheckStateListener checkStateListener;
	private ArrayList visibleCapabilities = new ArrayList();
	private ArrayList checkedCapabilities = new ArrayList();
	private Collection disabledCaps;

	// For a given capability as key, the value will be a list of
	// other capabilities that require the capability. Also,
	// it may include the capability key if it was selected by the
	// user before being required by other capabilities.
	private HashMap dependents = new HashMap();
	
	// For a given membership set id as key, the value is
	// a checked capability
	private HashMap memberships = new HashMap();

	// Sort categories
	private Comparator categoryComparator = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object ob1, Object ob2) {
			ICategory c1 = (ICategory) ob1;
			ICategory c2 = (ICategory) ob2;
			return collator.compare(c1.getLabel(), c2.getLabel());
		}
	};

	// Sort capabilities
	private Comparator capabilityComparator = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object ob1, Object ob2) {
			Capability c1 = (Capability) ob1;
			Capability c2 = (Capability) ob2;
			return collator.compare(c1.getName(), c2.getName());
		}
	};
	
	/**
	 * Creates a new instance of the <code>ProjectCapabilitySelectionGroup</code>
	 * 
	 * @param categories the initial collection of valid categories to select
	 * @param capabilities the intial collection of valid capabilities to select
	 * @param registry all available capabilities registered by plug-ins
	 */
	public ProjectCapabilitySelectionGroup(ICategory[] categories, Capability[] capabilities, CapabilityRegistry registry) {
		this(categories, capabilities, null, registry);
	}

	/**
	 * Creates a new instance of the <code>ProjectCapabilitySelectionGroup</code>
	 * 
	 * @param categories the initial collection of valid categories to select
	 * @param capabilities the intial collection of valid capabilities to select
	 * @param disabledCapabilities the collection of capabilities to show as disabled
	 * @param registry all available capabilities registered by plug-ins
	 */
	public ProjectCapabilitySelectionGroup(ICategory[] categories, Capability[] capabilities, Capability[] disabledCapabilities, CapabilityRegistry registry) {
		super();
		this.initialCategories = categories;
		this.initialCapabilities = capabilities;
		this.disabledCapabilities = disabledCapabilities;
		this.registry = registry;
	}

	/**
	 * Create the contents of this group. The basic layout is a checkbox
	 * list with a text field at the bottom to display the capability
	 * description.
	 */
	public Control createContents(Composite parent) {
		Font font = parent.getFont();
		// Create the main composite for the other controls
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Composite for category label and list...
		Composite catComposite = new Composite(composite, SWT.NONE);
		catComposite.setLayout(new GridLayout());
		catComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add a label to identify the list viewer of categories
		Label categoryLabel = new Label(catComposite, SWT.LEFT);
		categoryLabel.setText(WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.categories")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		categoryLabel.setLayoutData(data);
		categoryLabel.setFont(font);
		
		// List viewer of all available categories
		ListViewer listViewer = new ListViewer(catComposite);
		listViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		listViewer.getList().setFont(font);
		listViewer.setLabelProvider(new WorkbenchLabelProvider());
		listViewer.setContentProvider(getContentProvider());
		listViewer.setInput(getAvailableCategories());
		
		// Composite for capability label and table...
		Composite capComposite = new Composite(composite, SWT.NONE);
		capComposite.setLayout(new GridLayout());
		capComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add a label to identify the checkbox tree viewer of capabilities
		Label capabilityLabel = new Label(capComposite, SWT.LEFT);
		capabilityLabel.setText(WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.capabilities")); //$NON-NLS-1$
		data = new GridData();
		data.verticalAlignment = SWT.TOP;
		capabilityLabel.setLayoutData(data);
		capabilityLabel.setFont(font);
		
		// Checkbox tree viewer of capabilities in selected categories
		checkboxViewer = CheckboxTableViewer.newCheckList(capComposite, SWT.SINGLE | SWT.TOP | SWT.BORDER);
		checkboxViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxViewer.getTable().setFont(font);
		checkboxViewer.setLabelProvider(new CapabilityLabelProvider());
		checkboxViewer.setContentProvider(getContentProvider());
		checkboxViewer.setInput(visibleCapabilities);

		// Add a label to identify the text field of capability's description
		Label descLabel = new Label(composite, SWT.LEFT);
		descLabel.setText(WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.description")); //$NON-NLS-1$
		data = new GridData();
		data.verticalAlignment = SWT.TOP;
		data.horizontalSpan = 2;
		descLabel.setLayoutData(data);
		descLabel.setFont(font);
		
		// Text field to display the capability's description
		descriptionText = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		descriptionText.setText(EMPTY_DESCRIPTION);
		descriptionText.setEditable(false);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		descriptionText.setLayoutData(data);
		descriptionText.setFont(font);
		
		// Add a text field to explain grayed out items
		Label grayLabel = new Label(composite, SWT.LEFT);
		grayLabel.setText(WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.grayItems")); //$NON-NLS-1$
		data = new GridData();
		data.verticalAlignment = SWT.TOP;
		data.horizontalSpan = 2;
		grayLabel.setLayoutData(data);
		grayLabel.setFont(font);

		// Setup initial context		
		populateDependents();
		populateMemberships();
		
		// Listen for selection changes to update the description field
		checkboxViewer.addSelectionChangedListener(new ISelectionChangedListener () {
			public void selectionChanged(SelectionChangedEvent event) {
				updateDescription(event.getSelection());
			}
		});
		
		// Properly handle user checking and unchecking project features
		checkboxViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Capability cap = (Capability)event.getElement();
				if (event.getChecked())
					handleCapabilityChecked(cap);
				else
					handleCapabilityUnchecked(cap);
				checkboxViewer.setSelection(new StructuredSelection(cap));
			}
		});

		// Listen for category selection and update the list of capabilities
		listViewer.addSelectionChangedListener(new ISelectionChangedListener () {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)event.getSelection();
					visibleCapabilities.clear();
					Iterator enum = sel.iterator();
					while (enum.hasNext()) {
						ICategory cat = (ICategory)enum.next();
						visibleCapabilities.addAll(cat.getElements());
					}
					Collections.sort(visibleCapabilities, capabilityComparator);
					checkboxViewer.refresh();
					enum = visibleCapabilities.iterator();
					while (enum.hasNext()) {
						Capability cap = (Capability)enum.next();
						if (hasDependency(cap))
							checkboxViewer.setGrayed(cap, true);
						if (checkedCapabilities.contains(cap))
							checkboxViewer.setChecked(cap, true);
					}
					updateDescription(checkboxViewer.getSelection());
				}
			}
		});

		// initialize
		if (initialCapabilities != null)
			checkedCapabilities.addAll(Arrays.asList(initialCapabilities));
		if (initialCategories != null)
			listViewer.setSelection(new StructuredSelection(initialCategories));
			
		return composite;
	}
	
	/**
	 * Marks the capability as being checked.
	 */
	private void markCapabilityChecked(Capability target, Capability dependent) {
		// Check the target capability
		if (!checkedCapabilities.contains(target))
			checkedCapabilities.add(target);
		checkboxViewer.setChecked(target, true);
		
		// Gray the target to show the user its required
		// by another capability.
		if (target != dependent)
			checkboxViewer.setGrayed(target, true);
			
		// Update the dependent map for the target capability
		addDependency(target, dependent);
		
		// Update the membership set for the target capability
		String[] ids = registry.getMembershipSetIds(target);
		for (int j = 0; j < ids.length; j++) 
			memberships.put(ids[j], target);
	}
	
	/**
	 * Marks the capability as being unchecked.
	 */
	private void markCapabilityUnchecked(Capability target) {
		// Uncheck the target capability
		checkedCapabilities.remove(target);
		checkboxViewer.setChecked(target, false);

		// Ungray the target as there is no dependency on it
		checkboxViewer.setGrayed(target, false);

		// Remove the dependency entry
		dependents.remove(target);
							
		// Update the membership set for the target capability
		String[] ids = registry.getMembershipSetIds(target);
		for (int j = 0; j < ids.length; j++) {
			if (memberships.get(ids[j]) == target)
				memberships.remove(ids[j]);
		}
	}
	
	/**
	 * Returns the list of categories that have capabilities
	 * registered against it.
	 */
	private ArrayList getAvailableCategories() {
		ArrayList results = registry.getUsedCategories();
		Collections.sort(results, categoryComparator);
		if (registry.getMiscCategory() != null)
			results.add(registry.getMiscCategory());
		return results;
	}
	
	/**
	 * Return <code>true</code> if the user may have made changes
	 * to the capabilities of the project. Otherwise <code>false</code>
	 * if no changes were made.
	 * 
	 * @return <code>true</true> when possible changes may have been made,
	 *    <code>false</code> otherwise
	 */
	public boolean getCapabilitiesModified() {
		return modified;
	}

	/**
	 * Returns the content provider for the viewers
	 */
	private IContentProvider getContentProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof ArrayList)
					return ((ArrayList)parentElement).toArray();
				else
					return null;
			}
		};
	}
	
	/**
	 * The user has changed the project capability selection.
	 * Set the modified flag and clear the caches.
	 */
	private void capabilitiesModified() {
		modified = true;
	}
	
	/**
	 * Add a dependency between the target and dependent
	 * capabilities
	 */
	private void addDependency(Capability target, Capability dependent) {
		ArrayList descriptors = (ArrayList) dependents.get(target);
		if (descriptors == null) {
			descriptors = new ArrayList();
			descriptors.add(dependent);
			dependents.put(target, descriptors);
		}
		else if (!descriptors.contains(dependent)) {
			descriptors.add(dependent);
		}
	}

	/**
	 * Returns true if the capability has any
	 * dependencies on it.
	 */
	private boolean hasDependency(Capability capability) {
		ArrayList descriptors = (ArrayList) dependents.get(capability);
		if (descriptors == null)
			return false;
		if (descriptors.size() == 1 && descriptors.get(0) == capability)
			return false;
		return true;
	}
	
	/**
	 * Returns whether the category is considered disabled
	 */
	private boolean isDisabledCapability(Capability cap) {
		if (disabledCaps == null) {
			if (disabledCapabilities == null)
				disabledCaps = new ArrayList(0);
			else
				disabledCaps = Arrays.asList(disabledCapabilities);
		}
		return disabledCaps.contains(cap);
	}

	/**
	 * Populate the dependents map based on the
	 * current set of capabilities.
	 */
	private void populateDependents() {
		if (initialCapabilities == null)
			return;
			
		LinkedList capabilities = new LinkedList();
		capabilities.addAll(Arrays.asList(initialCapabilities));
			
		while (!capabilities.isEmpty()) {
			// Retrieve the target capability
			Capability target;
			target = (Capability) capabilities.removeFirst();
			// Add the capability as a dependent of itself.
			// It will indicate to the uncheck handler to not uncheck this
			// capability automatically even if a another capability which
			// depended on it is unchecked.
			addDependency(target, target);
			
			if (registry.hasPrerequisites(target)) {
				// Retrieve the prerequisite capabilities...
				String[] prereqIds = registry.getPrerequisiteIds(target);
				Capability[] prereqCapabilities;
				prereqCapabilities = registry.findCapabilities(prereqIds);
				// For each prerequisite capability...
				for (int i = 0; i < prereqCapabilities.length; i++) {
					// Update the dependent map for the prerequisite capability
					addDependency(prereqCapabilities[i], target);
					// Recursive if prerequisite capability also has prerequisites
					if (registry.hasPrerequisites(prereqCapabilities[i]))
						capabilities.addLast(prereqCapabilities[i]);
				}
			}
		}
	}
	
	/**
	 * Populate the memberships map based on the
	 * current set of capabilities.
	 */
	private void populateMemberships() {
		if (initialCapabilities == null)
			return;
			
		Iterator enum = (Arrays.asList(initialCapabilities)).iterator();
		while (enum.hasNext()) {
			Capability cap = (Capability)enum.next();
			String[] ids = registry.getMembershipSetIds(cap);
			for (int j = 0; j < ids.length; j++) {
				memberships.put(ids[j], cap);
			}
		}
	}
	
	/**
	 * Handle the case of a capability being checked
	 * by ensuring the action is allowed and the prerequisite
	 * capabilities are also checked.
	 */
	private void handleCapabilityChecked(Capability capability) {
		// Cannot allow a disabled capability to be checked
		if (isDisabledCapability(capability)) {
			MessageDialog.openWarning(
				checkboxViewer.getControl().getShell(),
				WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("ProjectCapabilitySelectionGroup.disabledCapability", new Object[] {capability.getName()})); //$NON-NLS-1$
			checkboxViewer.setChecked(capability, false);
			return;
		}
		
		// Cannot allow an invalid capability to be checked
		if (!capability.isValid()) {
			MessageDialog.openWarning(
				checkboxViewer.getControl().getShell(),
				WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("ProjectCapabilitySelectionGroup.invalidCapability", new Object[] {capability.getName()})); //$NON-NLS-1$
			checkboxViewer.setChecked(capability, false);
			return;
		}

		// Is there a membership set problem...
		String[] ids = registry.getMembershipSetIds(capability);
		for (int i = 0; i < ids.length; i++) {
			Capability member = (Capability)memberships.get(ids[i]);
			if (member != null && member != capability) {
				MessageDialog.openWarning(
					checkboxViewer.getControl().getShell(),
					WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format("ProjectCapabilitySelectionGroup.membershipConflict", new Object[] {capability.getName(), member.getName()})); //$NON-NLS-1$
				checkboxViewer.setChecked(capability, false);
				return;
			}
		}
		
		// Handle prerequisite by auto-checking them if possible
		if (registry.hasPrerequisites(capability)) {
			// Check for any prerequisite problems...
			// Retrieve all the prerequisite capabilities, including
			// any prerequisite of the prerequisites!
			LinkedList capabilities = new LinkedList();
			capabilities.addLast(capability);
			while (!capabilities.isEmpty()) {
				Capability target;
				target = (Capability) capabilities.removeFirst();
				// Retrieve the capability's immediate prerequisites
				String[] prereqIds = registry.getPrerequisiteIds(target);
				Capability[] prereqCapabilities;
				prereqCapabilities = registry.findCapabilities(prereqIds);
				for (int i = 0; i < prereqCapabilities.length; i++) {
					// If the prerequisite is missing, warn the user and
					// do not allow the check to proceed.
					if (prereqCapabilities[i] == null || isDisabledCapability(prereqCapabilities[i]) || !prereqCapabilities[i].isValid()) {
						MessageDialog.openWarning(
							checkboxViewer.getControl().getShell(),
							WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.errorTitle"), //$NON-NLS-1$
							WorkbenchMessages.format("ProjectCapabilitySelectionGroup.missingPrereqs", new Object[] {capability.getName(), prereqIds[i]})); //$NON-NLS-1$
						checkboxViewer.setChecked(capability, false);
						return;
					}
					// If there is a membership problem, warn the user and
					// do not allow the check to proceed
					ids = registry.getMembershipSetIds(prereqCapabilities[i]);
					for (int j = 0; j < ids.length; j++) {
						Capability member = (Capability)memberships.get(ids[j]);
						if (member != null && member != prereqCapabilities[i]) {
							MessageDialog.openWarning(
								checkboxViewer.getControl().getShell(),
								WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.errorTitle"), //$NON-NLS-1$
								WorkbenchMessages.format("ProjectCapabilitySelectionGroup.membershipPrereqConflict", new Object[] {capability.getName(), prereqCapabilities[i].getName(), member.getName()})); //$NON-NLS-1$
							checkboxViewer.setChecked(capability, false);
							return;
						}
					}
					// If the prerequisite capability has prerequisites
					// also, then add it to be processed.
					if (registry.hasPrerequisites(prereqCapabilities[i]))
						capabilities.addLast(prereqCapabilities[i]);
				}
			}
			
			// Auto-check all prerequisite capabilities
			capabilities = new LinkedList();
			capabilities.addLast(capability);
			// For each capability that has prerequisites...
			while (!capabilities.isEmpty()) {
				Capability target;
				target = (Capability) capabilities.removeFirst();
				// Retrieve the prerequisite capabilities...
				String[] prereqIds = registry.getPrerequisiteIds(target);
				Capability[] prereqCapabilities;
				prereqCapabilities = registry.findCapabilities(prereqIds);
				// For each prerequisite capability...
				for (int i = 0; i < prereqCapabilities.length; i++) {
					// Mark it as being checked
					markCapabilityChecked(prereqCapabilities[i], target);
					// Recursive if prerequisite capability also has prerequisites
					if (registry.hasPrerequisites(prereqCapabilities[i]))
						capabilities.addLast(prereqCapabilities[i]);
				}
			}
		}
		
		// Mark the capability as checked. Adds itself as a
		// dependent - this will indicate to the uncheck handler
		// to not uncheck this capability automatically even if
		// another capability which depends on it is unchecked.
		markCapabilityChecked(capability, capability);

		// Notify those interested
		capabilitiesModified();
		notifyCheckStateListner();
	}
	
	/**
	 * Handle the case of a capability being unchecked
	 * by ensuring the action is allowed. 
	 */
	private void handleCapabilityUnchecked(Capability capability) {
		ArrayList descriptors = (ArrayList) dependents.get(capability);
		
		// Note, there is no need to handle the case where descriptors size
		// is zero because it cannot happen. For this method to be called, the
		// item must have been checked previously. If it was checked by the user,
		// then the item itself would be a dependent. If the item was checked
		// because it was required by another capability, then that other capability
		// would be a dependent.
		
		if (descriptors.size() == 1 && descriptors.get(0) == capability) {
			// If the only dependent is itself, then its ok to uncheck
			capabilitiesModified();
			markCapabilityUnchecked(capability);

			// Remove this capability as a dependent on its prerequisite
			// capabilities. Recursive if a prerequisite capability
			// no longer has any dependents.
			if (registry.hasPrerequisites(capability)) {
				LinkedList capabilities = new LinkedList();
				capabilities.addLast(capability);
				// For each capability that has prerequisite capabilities
				while (!capabilities.isEmpty()) {
					Capability target;
					target = (Capability) capabilities.removeFirst();
					// Retrieve the prerequisite capabilities...
					String[] prereqIds = registry.getPrerequisiteIds(target);
					Capability[] prereqCapabilities;
					prereqCapabilities = registry.findCapabilities(prereqIds);
					// For each prerequisite capability...
					for (int i = 0; i < prereqCapabilities.length; i++) {
						// Retrieve the list of dependents on the prerequisite capability...
						Capability prereqCap = prereqCapabilities[i];
						ArrayList prereqDependents = (ArrayList) dependents.get(prereqCap);
						// Remove the dependent target capability...
						prereqDependents.remove(target);
						if (prereqDependents.isEmpty()) {
							// Unchecked the prerequisite capability
							markCapabilityUnchecked(prereqCap);
							// Recursive if prerequisite capability also has
							// prerequisite capabilities
							if (registry.hasPrerequisites(prereqCap))
								capabilities.addLast(prereqCap);
						}
						else if (prereqDependents.size() == 1 && prereqDependents.get(0) == prereqCap) {
							// Only dependent is itself so ungray the item to let the
							// user know no other capability is dependent on it
							checkboxViewer.setGrayed(prereqCap, false);
						}
					}
				}
			}
			
			// Notify those interested
			notifyCheckStateListner();
		}
		else {
			// At least one other capability depends on it being checked
			// so force it to remain checked and warn the user.
			checkboxViewer.setChecked(capability, true);
			// Get a copy and remove the target capability
			ArrayList descCopy = (ArrayList) descriptors.clone();
			descCopy.remove(capability);
			// Show the prereq problem to the user
			if (descCopy.size() == 1) {
				Capability cap = (Capability) descCopy.get(0);
				MessageDialog.openWarning(
					checkboxViewer.getControl().getShell(),
					WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format("ProjectCapabilitySelectionGroup.requiredPrereq", new Object[] {capability.getName(), cap.getName()})); //$NON-NLS-1$
			} else {
				StringBuffer msg = new StringBuffer();
				Iterator enum = descCopy.iterator();
				while (enum.hasNext()) {
					Capability cap = (Capability) enum.next();
					msg.append("\n    "); //$NON-NLS-1$
					msg.append(cap.getName());
				}
				MessageDialog.openWarning(
					checkboxViewer.getControl().getShell(),
					WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format("ProjectCapabilitySelectionGroup.requiredPrereqs", new Object[] {capability.getName(), msg.toString()})); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Returns the collection of capabilities selected
	 * by the user. The collection is not in prerequisite
	 * order.
	 * 
	 * @return array of selected capabilities
	 */
	public Capability[] getSelectedCapabilities() {
		Capability[] capabilities = new Capability[checkedCapabilities.size()];
		checkedCapabilities.toArray(capabilities);
		return capabilities;
	}

	/**
	 * Return the current listener interested when the check
	 * state of a capability actually changes.
	 * 
	 * @return Returns a ICheckStateListener
	 */
	public ICheckStateListener getCheckStateListener() {
		return checkStateListener;
	}

	/**
	 * Set the current listener interested when the check
	 * state of a capability actually changes.
	 * 
	 * @param checkStateListener The checkStateListener to set
	 */
	public void setCheckStateListener(ICheckStateListener checkStateListener) {
		this.checkStateListener = checkStateListener;
	}

	/**
	 * Notify the check state listener that a capability
	 * check state has changed. The event past will
	 * always be <code>null</code> as it could be
	 * triggered by code instead of user input.
	 */
	private void notifyCheckStateListner() {
		if (checkStateListener != null)
			checkStateListener.checkStateChanged(null);
	}
	
	/**
	 * Updates the description field for the selected capability
	 */
	private void updateDescription(ISelection selection) {
		String text = EMPTY_DESCRIPTION;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			Capability cap = (Capability)sel.getFirstElement();
			if (cap != null)
				text = cap.getDescription();
		}
		descriptionText.setText(text);
	}
	
	class CapabilityLabelProvider extends LabelProvider {
		private Map imageTable;
		
		public void dispose() {
			if (imageTable != null) {
				Iterator enum = imageTable.values().iterator();
				while (enum.hasNext())
					((Image) enum.next()).dispose();
				imageTable = null;
			}
		}

		public Image getImage(Object element) {
			ImageDescriptor descriptor = ((Capability) element).getIconDescriptor();
			if (descriptor == null)
				return null;
			
			//obtain the cached image corresponding to the descriptor
			if (imageTable == null) {
				 imageTable = new Hashtable(40);
			}
			Image image = (Image) imageTable.get(descriptor);
			if (image == null) {
				image = descriptor.createImage();
				imageTable.put(descriptor, image);
			}
			return image;
		}

		public String getText(Object element) {
			Capability cap = (Capability) element;
			String text = cap.getName();
			if (isDisabledCapability(cap))
				text = WorkbenchMessages.format("ProjectCapabilitySelectionGroup.disabledLabel", new Object[] {text}); //$NON-NLS-1$
			return text;
		}
	}
}


