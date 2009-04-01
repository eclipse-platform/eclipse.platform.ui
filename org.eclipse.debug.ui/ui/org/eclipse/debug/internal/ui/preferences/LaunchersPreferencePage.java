/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * This class provides a preference page for selecting and changing preferred launch delegates for those of them
 * that have conflicting delegates.
 * 
 * Delegates are considered to be conflicting if they are for the same launch configuration type, and apply to the same 
 * mode sets.
 * 
 * @since 3.3
 */
public class LaunchersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Class to collect and persist attributes to sufficiently describe a duplicate launch delegate
	 */
	class DuplicateDelegate {
		private ILaunchConfigurationType fType = null;
		private ILaunchDelegate[] fDelegates = null;
		private Set fModes = null;
		
		public DuplicateDelegate(ILaunchConfigurationType type, ILaunchDelegate[] delegates, Set modes) {
			fModes = modes;
			fType = type;
			fDelegates = delegates;
		}
		
		public ILaunchConfigurationType getType() {
			return fType;
		}
		public ILaunchDelegate[] getDelegates() {
			return fDelegates;
		}
		public Set getModeSet() {
			return fModes;
		}
	}
	
	/**
	 * label provider to extend the default one, provides labels to both the tree and table of this page
	 */
	class LabelProvider extends DefaultLabelProvider {
		public String getText(Object element) {
			if(element instanceof ILaunchConfigurationType) {
				return super.getText(element);
			}
			else if(element instanceof DuplicateDelegate) {
				DuplicateDelegate dd = (DuplicateDelegate) element;
				return LaunchConfigurationPresentationManager.getDefault().getLaunchModeNames(dd.getModeSet()).toString();
			}
			else if(element instanceof ILaunchDelegate){
				return ((ILaunchDelegate) element).getName();
			}
			return element.toString();
		}
	}
	
	/**
	 * This class is used to provide content to the tree
	 */
	class TreeProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof ILaunchConfigurationType) {
				ILaunchConfigurationType type = (ILaunchConfigurationType) parentElement;
				Set dupes = (Set) fDuplicates.get(type);
				if(dupes != null) {
					return dupes.toArray();
				}
				return null;
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			return element instanceof ILaunchConfigurationType;
		}
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof Map) {
				return ((Map)inputElement).keySet().toArray();
			}
			return null;
		}
		public Object getParent(Object element) {return null;}
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	private TreeViewer fTreeViewer = null;
	private CheckboxTableViewer fTableViewer = null;
	private Map fDuplicates = null;
	private Map fDupeSelections = null;
	private boolean fDirty = false;
	private Text fDescription = null;
	
	/**
	 * Constructor
	 */
	public LaunchersPreferencePage() {
		setTitle(DebugPreferencesMessages.LaunchDelegatesPreferencePage_0);
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.LAUNCH_DELEGATES_PREFERENCE_PAGE);
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_BOTH);
		SWTFactory.createWrapLabel(comp, DebugPreferencesMessages.LaunchDelegatesPreferencePage_1, 2, 300);
		
		boolean enabled = fDuplicates.size() > 0;
		if(!enabled) {
			SWTFactory.createVerticalSpacer(comp, 1);
			SWTFactory.createWrapLabel(comp, DebugPreferencesMessages.LaunchersPreferencePage_0, 2, 300);
		}
		
		SWTFactory.createVerticalSpacer(comp, 1);
	//tree
		Composite comp1 = SWTFactory.createComposite(comp, 1, 1, GridData.FILL_VERTICAL);
		SWTFactory.createLabel(comp1, DebugPreferencesMessages.LaunchDelegatesPreferencePage_2, 1);
		Tree tree = new Tree(comp1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		tree.setEnabled(enabled);
		tree.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = false;
		tree.setLayoutData(gd);
		fTreeViewer = new TreeViewer(tree);
		fTreeViewer.setComparator(new WorkbenchViewerComparator());
		fTreeViewer.setContentProvider(new TreeProvider());
		fTreeViewer.setLabelProvider(new LabelProvider());
		fTreeViewer.setInput(fDuplicates);
		fTreeViewer.expandToLevel(2);
		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if(obj instanceof DuplicateDelegate) {
					fTableViewer.setAllChecked(false);
					DuplicateDelegate dd = (DuplicateDelegate) obj;
					fTableViewer.setInput(dd.getDelegates());
					fTableViewer.setSelection(null);
					obj = fDupeSelections.get(dd);
					if(obj != null) {
						fTableViewer.setChecked(obj, true);
						fTableViewer.setSelection(new StructuredSelection(obj));
					}
				}
				else {
					fTableViewer.setInput(null);
				}
			}
		});
		
	//table
		Composite comp2 = SWTFactory.createComposite(comp, comp.getFont(), 1, 1, GridData.FILL_BOTH);
		SWTFactory.createLabel(comp2, DebugPreferencesMessages.LaunchDelegatesPreferencePage_3, 1);
		Table table = new Table(comp2, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK | SWT.SINGLE);
		table.setEnabled(enabled);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setFont(parent.getFont());
		fTableViewer = new CheckboxTableViewer(table);
		fTableViewer.setComparator(new WorkbenchViewerComparator());
		fTableViewer.setLabelProvider(new LabelProvider());
		fTableViewer.setContentProvider(new ArrayContentProvider());
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ss = (IStructuredSelection) event.getSelection();
				if(ss != null && !ss.isEmpty()) {
					ILaunchDelegate delegate = (ILaunchDelegate)ss.getFirstElement();
					fDescription.setText(delegate.getDescription());
				}
				else {
					fDescription.setText(IInternalDebugCoreConstants.EMPTY_STRING);
				}
			}
		});
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fDirty = true;
				Object element = event.getElement();
				boolean checked = event.getChecked();
				//always set checked, this way users cannot 'undo' a change to selecting a preferred delegate
				//The story for this is that on startup if there are dupes, the user is prompted to pick a delegate, after that they cannot 
				//return to a state of not being able to launch something, but can pick a different delegate
				fTableViewer.setCheckedElements(new Object[] {element});
				//set the selection to be the checked element
				//https://bugs.eclipse.org/bugs/show_bug.cgi?id=233233
				fTableViewer.setSelection(new StructuredSelection(element));
				//persist the selection
				Object obj = ((IStructuredSelection) fTreeViewer.getSelection()).getFirstElement();
				if(obj instanceof DuplicateDelegate) {
					fDupeSelections.remove(obj);
					if(checked) {
						fDupeSelections.put(obj, element);
					}
				}
			}
		});
		Group group = SWTFactory.createGroup(comp, DebugPreferencesMessages.LaunchDelegatesPreferencePage_4, 1, 2, GridData.FILL_BOTH);
		fDescription = SWTFactory.createText(group, SWT.WRAP | SWT.READ_ONLY, 1, GridData.FILL_BOTH);
		fDescription.setEnabled(enabled);
		fDescription.setBackground(group.getBackground());
		return comp;
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		if(fDirty && fDupeSelections != null && fDupeSelections.size() > 0) {
			fDirty = false;
			DuplicateDelegate dd = null;
			ILaunchDelegate delegate = null;
			for(Iterator iter = fDupeSelections.keySet().iterator(); iter.hasNext();) {
				dd = (DuplicateDelegate) iter.next();
				delegate = (ILaunchDelegate) fDupeSelections.get(dd);
				try {
					dd.getType().setPreferredDelegate(dd.getModeSet(), delegate);
				} 
				catch (CoreException e) {DebugUIPlugin.log(e);}
			}
		}
		return super.performOk();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		//init a listing of duplicate delegates arranged by type
		try {
			setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
			LaunchManager lm = (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType[] types = lm.getLaunchConfigurationTypes();
			fDuplicates = new HashMap();
			fDupeSelections = new HashMap();
			Set modes = null;
			ILaunchDelegate[] delegates = null;
			Set modeset = null;
			Set tmp = null;
			ILaunchDelegate prefdelegate = null;
			DuplicateDelegate dd = null;
			for(int i = 0; i < types.length; i++) {
				modes = types[i].getSupportedModeCombinations();
				for(Iterator iter = modes.iterator(); iter.hasNext();) {
					modeset = (Set) iter.next();
					delegates = types[i].getDelegates(modeset);
					if(delegates.length > 1) {
						tmp = (Set) fDuplicates.get(types[i]);
						if(tmp == null) {
							tmp = new HashSet();
						}
						dd = new DuplicateDelegate(types[i], delegates, modeset);
						tmp.add(dd);
						fDuplicates.put(types[i], tmp);
						prefdelegate = types[i].getPreferredDelegate(modeset);
						if(prefdelegate != null) {
							fDupeSelections.put(dd, prefdelegate);
						}
					}
				}
			}
		}
		catch(CoreException e) {DebugUIPlugin.log(e);}
	}

}
