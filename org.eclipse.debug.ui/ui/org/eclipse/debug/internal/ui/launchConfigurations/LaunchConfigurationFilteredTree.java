/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Eike Stepper    - bug 343228
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * Overrides the default filtered tree to use our own tree viewer which supports preserving selection after filtering
 * 
 * @see LaunchConfigurationView
 * @see LaunchConfigurationViewer
 * 
 * @since 3.3 
 */
public final class LaunchConfigurationFilteredTree extends FilteredTree {

	private ILaunchGroup fLaunchGroup = null;
	private ViewerFilter[] fFilters = null;
	private int fTreeStyle = -1;
	private PatternFilter fPatternFilter = null;
	
	/**
	 * Constructor
	 * @param parent the parent {@link Composite}
	 * @param treeStyle the style
	 * @param filter the initial filter pattern
	 * @param group the launch group to open on
	 * @param filters the initial group of filters
	 */
	public LaunchConfigurationFilteredTree(Composite parent, int treeStyle, PatternFilter filter, ILaunchGroup group, ViewerFilter[] filters) {
		super(parent, treeStyle, filter, true);
		fLaunchGroup = group;
		fFilters = filters;
		fPatternFilter = filter;
		fTreeStyle = treeStyle;
	}
	
	/**
	 * @see org.eclipse.ui.dialogs.FilteredTree#doCreateTreeViewer(org.eclipse.swt.widgets.Composite, int)
	 */
	protected TreeViewer doCreateTreeViewer(Composite cparent, int style) {
		treeViewer = new LaunchConfigurationViewer(cparent, style);
		treeViewer.setLabelProvider(new DecoratingLabelProvider(DebugUITools.newDebugModelPresentation(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		treeViewer.setComparator(new WorkbenchViewerComparator());
		treeViewer.setContentProvider(new LaunchConfigurationTreeContentProvider(fLaunchGroup.getMode(), cparent.getShell()));
		treeViewer.addFilter(new LaunchGroupFilter(fLaunchGroup));
		treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		if(fFilters != null) {
			for (int i = 0; i < fFilters.length; i++) {
				treeViewer.addFilter(fFilters[i]);
			}
		}
		treeViewer.getControl().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent evt) {
				handleHelpRequest(evt);
			}
		});
		return treeViewer;
	}
	
	/**
	 * @see org.eclipse.ui.dialogs.FilteredTree#createControl(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void createControl(Composite cparent, int treeStyle) {
		super.createControl(cparent, treeStyle);
		setBackground(cparent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	}

	/**
	 * @see org.eclipse.ui.dialogs.FilteredTree#init(int, org.eclipse.ui.dialogs.PatternFilter)
	 * force it to do nothing so that we can initialize the class properly
	 */
	protected void init(int treeStyle, PatternFilter filter) {}

	/**
	 * This method is used to create the actual set of controls for the dialog
	 */
	public void createViewControl() {
		super.init(fTreeStyle, fPatternFilter);
	}
	
	/**
	 * Handle help events locally rather than deferring to WorkbenchHelp.  This
	 * allows help specific to the selected config type to be presented.
	 * @param evt the {@link HelpEvent}
	 */
	protected void handleHelpRequest(HelpEvent evt) {
		if (getViewer().getTree() != evt.getSource()) {
			return;
		}
		String id = computeContextId();
		if (id != null)
			PlatformUI.getWorkbench().getHelpSystem().displayHelp(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredTree#textChanged()
	 */
	protected void textChanged() {
		LaunchConfigurationsDialog dialog = (LaunchConfigurationsDialog)LaunchConfigurationsDialog.getCurrentlyVisibleLaunchConfigurationDialog();
		if(dialog == null) {
			return;
		}
		LaunchConfigurationTabGroupViewer viewer = dialog.getTabViewer();
		if(viewer == null) {
			return;
		}
		if(viewer.isDirty()) {
			String text = getFilterString();
			if(text.equals(IInternalDebugCoreConstants.EMPTY_STRING)) {
				//we have removed the last char of select-all delete key, reset like the filter control does
				getPatternFilter().setPattern(null);
				getViewer().refresh();
				return;
			}
			else if(text.equals(getInitialText())) {
				//ignore, this is the default text set from losing focus
				return;
			}
			String message = LaunchConfigurationsMessages.LaunchConfigurationFilteredTree_search_with_errors;
			String title = LaunchConfigurationsMessages.LaunchConfigurationFilteredTree_discard_changes;
			boolean cansave = viewer.canSave();
			if(cansave) {
				message = LaunchConfigurationsMessages.LaunchConfigurationFilteredTree_search_with_changes;
				title = LaunchConfigurationsMessages.LaunchConfigurationFilteredTree_save_changes;
			}
			if(MessageDialog.openQuestion(getShell(), title, message)) {
				if(cansave) {
					viewer.handleApplyPressed();
				}
				else {
					viewer.handleRevertPressed();
				}
				super.textChanged();
			}
			else {
				clearText();
			}
		}
		else {
			super.textChanged();
		}
	}
	
	/**
	 * Computes the context id for this viewer
	 * @return the context id
	 */
	public String computeContextId() {
		try {
			ISelection selection = getViewer().getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection ) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object firstSelected = structuredSelection.getFirstElement();
				ILaunchConfigurationType configType = null;
				if (firstSelected instanceof ILaunchConfigurationType) {
					configType = (ILaunchConfigurationType) firstSelected;
				} 
				else if (firstSelected instanceof ILaunchConfiguration) {
					configType = ((ILaunchConfiguration) firstSelected).getType();
				}
				if (configType != null) {
					String helpContextId = LaunchConfigurationPresentationManager.getDefault().getHelpContext(configType, fLaunchGroup.getMode());
					if (helpContextId != null) {
						return helpContextId;
					}
				}
			}
		} 
		catch (CoreException ce) {DebugUIPlugin.log(ce);}
		return null;
	}
	
	/**
	 * Returns the launch configuration viewer for this filtered tree
	 * @return the tree viewer appropriately cast
	 */
	public LaunchConfigurationViewer getLaunchConfigurationViewer() {
		return (LaunchConfigurationViewer) getViewer();
	}

	/* (non-Javadoc)
	 * 
	 * Called after a re-filter due to user typing text. Update the filter count
	 * in the LCD
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredTree#updateToolbar(boolean)
	 */
	protected void updateToolbar(boolean visible) {
		super.updateToolbar(visible);
		// update filter count
		getLaunchConfigurationViewer().filterChanged();
	}

}
