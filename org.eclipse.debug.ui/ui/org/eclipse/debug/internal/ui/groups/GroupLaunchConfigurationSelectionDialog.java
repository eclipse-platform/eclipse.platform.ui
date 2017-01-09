/*******************************************************************************
 *  Copyright (c) 2009, 2016 QNX Software Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *      Freescale Semiconductor
 *      SSI Schaefer
 *******************************************************************************/
package org.eclipse.debug.internal.ui.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.groups.GroupLaunchConfigurationDelegate;
import org.eclipse.debug.internal.core.groups.GroupLaunchElement;
import org.eclipse.debug.internal.core.groups.GroupLaunchElement.GroupElementPostLaunchAction;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationFilteredTree;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Dialog to select launch configuration(s)
 */
class GroupLaunchConfigurationSelectionDialog extends TitleAreaDialog implements ISelectionChangedListener {
	private static final String GROUP_TYPE_ID = "org.eclipse.debug.core.groups.GroupLaunchConfigurationType"; //$NON-NLS-1$

	private ISelection fSelection;
	private String mode;
	private GroupElementPostLaunchAction action = GroupElementPostLaunchAction.NONE;
	private Object actionParam;
	private boolean adoptIfRunning;
	private ViewerFilter emptyTypeFilter;
	private IStructuredSelection fInitialSelection;
	private ComboControlledStackComposite fStackComposite;
	private Label fActionParamLabel;
	private Text fActionParamWidget; // in seconds
	private boolean fForEditing; // true if dialog was opened to edit an entry,
									// otherwise it was opened to add one
	private ILaunchConfigurationType groupType;
	private ILaunchConfiguration selfRef;

	public GroupLaunchConfigurationSelectionDialog(Shell shell, String initMode, boolean forEditing, ILaunchConfiguration self) {
		super(shell);
		mode = initMode;
		fForEditing = forEditing;
		selfRef = self;
		setShellStyle(getShellStyle() | SWT.RESIZE);

		groupType = getLaunchManager().getLaunchConfigurationType(GROUP_TYPE_ID);
		emptyTypeFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				try {
					if (element instanceof ILaunchConfigurationType) {
						ILaunchConfigurationType type = (ILaunchConfigurationType) element;
						if (type.equals(groupType)) {
							// we're hiding ourselves. if we're the only group,
							// don't show the type.
							return getLaunchManager().getLaunchConfigurations(type).length > 1;
						}

						return getLaunchManager().getLaunchConfigurations(type).length > 0;
					} else if (element instanceof ILaunchConfiguration) {
						ILaunchConfiguration c = (ILaunchConfiguration) element;
						if (c.getName().equals(self.getName()) && c.getType().equals(groupType)) {
							return false;
						}

						if (c.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false)) {
							return false;
						}

						return GroupLaunchConfigurationTabGroup.isValidLaunchReference(c);
					}
					return true;
				} catch (CoreException e) {
					return false;
				}
			}
		};
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control x = super.createContents(parent);
		validate();
		setErrorMessage(null);
		return x;
	}

	@Override
	protected Control createDialogArea(Composite parent2) {
		Composite comp = (Composite) super.createDialogArea(parent2);

		// title bar
		getShell().setText(fForEditing ? DebugUIMessages.GroupLaunchConfigurationSelectionDialog_13 : DebugUIMessages.GroupLaunchConfigurationSelectionDialog_12);

		// dialog message area (not title bar)
		setTitle(fForEditing ? DebugUIMessages.GroupLaunchConfigurationSelectionDialog_15 : DebugUIMessages.GroupLaunchConfigurationSelectionDialog_14);

		fStackComposite = new ComboControlledStackComposite(comp, SWT.NONE);

		Map<String, ILaunchGroup> modes = new LinkedHashMap<>();
		modes.put(GroupLaunchElement.MODE_INHERIT, new InheritModeGroup());
		Set<ILaunchGroup> sortedGroups = new TreeSet<>((a, b) -> {
			return a.getLabel().compareTo(b.getLabel());
		});
		LaunchConfigurationManager mgr = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		sortedGroups.addAll(Arrays.asList(mgr.getLaunchGroups()));
		for (ILaunchGroup launchGroup : sortedGroups) {
			LaunchHistory history = mgr.getLaunchHistory(launchGroup.getIdentifier());
			if (history == null) {
				// mode currently not supported.
				continue;
			}

			if (!modes.containsKey(launchGroup.getMode())) {
				modes.put(launchGroup.getMode(), launchGroup);
			}
		}

		for (Map.Entry<String, ILaunchGroup> entry : modes.entrySet()) {
			ILaunchGroup launchGroup = entry.getValue();
			LaunchConfigurationFilteredTree fTree = new LaunchConfigurationFilteredTree(fStackComposite.getStackParent(), SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), launchGroup, null);
			String lgm = entry.getKey();
			fStackComposite.addItem(lgm, fTree);
			fTree.createViewControl();
			ViewerFilter[] filters = fTree.getViewer().getFilters();
			for (ViewerFilter viewerFilter : filters) {
				if (viewerFilter instanceof LaunchGroupFilter) {
					fTree.getViewer().removeFilter(viewerFilter);
				}
			}
			fTree.getViewer().addFilter(emptyTypeFilter);
			fTree.getViewer().addSelectionChangedListener(this);
			if (lgm.equals(this.mode)) {
				fStackComposite.setSelection(lgm);
			}
			if (fInitialSelection != null) {
				fTree.getViewer().setSelection(fInitialSelection, true);
			}
		}
		fStackComposite.setLabelText(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_4);
		fStackComposite.pack();
		Rectangle bounds = fStackComposite.getBounds();
		// adjust size
		GridData data = ((GridData) fStackComposite.getLayoutData());
		if (data == null) {
			data = new GridData(GridData.FILL_BOTH);
			fStackComposite.setLayoutData(data);
		}
		data.heightHint = Math.max(convertHeightInCharsToPixels(15), bounds.height);
		data.widthHint = Math.max(convertWidthInCharsToPixels(40), bounds.width);
		fStackComposite.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mode = fStackComposite.getSelection();
			}
		});

		Button chkAdopt = new Button(comp, SWT.CHECK);
		chkAdopt.setText(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_adoptText);
		chkAdopt.setToolTipText(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_adoptTooltip);
		chkAdopt.setSelection(adoptIfRunning);
		chkAdopt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				adoptIfRunning = chkAdopt.getSelection();
			}
		});

		createPostLaunchControl(comp);
		return comp;
	}

	private void createPostLaunchControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(4, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = new Label(comp, SWT.NONE);
		label.setText(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_8);
		Combo combo = new Combo(comp, SWT.READ_ONLY);
		combo.add(GroupElementPostLaunchAction.NONE.getDescription());
		combo.add(GroupElementPostLaunchAction.WAIT_FOR_TERMINATION.getDescription());
		combo.add(GroupElementPostLaunchAction.DELAY.getDescription());
		combo.add(GroupElementPostLaunchAction.OUTPUT_REGEXP.getDescription());
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String actionStr = ((Combo) e.widget).getText();
				action = GroupElementPostLaunchAction.valueOfDescription(actionStr);
				showHideDelayAmountWidgets();
				validate();
			}
		});
		combo.setText(action.getDescription());

		fActionParamLabel = new Label(comp, SWT.NONE);
		fActionParamWidget = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fActionParamWidget);
		fActionParamWidget.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text) e.widget).getText();
				if (action == GroupElementPostLaunchAction.DELAY) {
					try {
						actionParam = Integer.valueOf(text);
					} catch (NumberFormatException exc) {
						actionParam = null;
					}
				} else if (action == GroupElementPostLaunchAction.OUTPUT_REGEXP) {
					actionParam = text;
				}
				validate();
			}
		});
		if (actionParam instanceof Integer) {
			fActionParamWidget.setText(((Integer) actionParam).toString());
		} else if (actionParam instanceof String) {
			fActionParamWidget.setText(actionParam.toString());
		}

		showHideDelayAmountWidgets();
	}

	private void showHideDelayAmountWidgets() {
		final boolean visible = (action == GroupElementPostLaunchAction.DELAY || action == GroupElementPostLaunchAction.OUTPUT_REGEXP);
		fActionParamLabel.setVisible(visible);
		fActionParamWidget.setVisible(visible);

		if (action == GroupElementPostLaunchAction.DELAY) {
			fActionParamLabel.setText(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_9);
		} else if (action == GroupElementPostLaunchAction.OUTPUT_REGEXP) {
			fActionParamLabel.setText(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_regexp);
		}

		fActionParamLabel.getParent().layout();
	}

	public ILaunchConfiguration[] getSelectedLaunchConfigurations() {
		List<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>();
		if (fSelection != null && !fSelection.isEmpty()) {
			for (Iterator<?> iter = ((IStructuredSelection) fSelection).iterator(); iter.hasNext();) {
				Object selection = iter.next();
				if (selection instanceof ILaunchConfiguration) {
					configs.add((ILaunchConfiguration) selection);
				}
			}
		}
		return configs.toArray(new ILaunchConfiguration[configs.size()]);
	}

	public String getMode() {
		return mode;
	}

	public GroupElementPostLaunchAction getAction() {
		return action;
	}

	public boolean getAdoptIfRunning() {
		return adoptIfRunning;
	}

	public Object getActionParam() {
		return actionParam;
	}

	public static GroupLaunchConfigurationSelectionDialog createDialog(Shell shell, String initMode, boolean forEditing, ILaunchConfiguration self) {
		return new GroupLaunchConfigurationSelectionDialog(shell, initMode, forEditing, self);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {

		// This listener gets called for a selection change in the launch
		// configuration viewer embedded in the dialog. Problem is, there are
		// numerous viewers--one for each platform debug ILaunchGroup (run,
		// debug, profile). These viewers are stacked, so only one is ever
		// visible to the user. During initialization, we get a selection change
		// notification for every viewer. We need to ignore all but the one that
		// matters--the visible one.

		Tree topTree = null;
		final Control topControl = fStackComposite.getTopControl();
		if (topControl instanceof FilteredTree) {
			final TreeViewer viewer = ((FilteredTree) topControl).getViewer();
			if (viewer != null) {
				topTree = viewer.getTree();
			}
		}
		if (topTree == null) {
			return;
		}

		boolean selectionIsForVisibleViewer = false;
		final Object src = event.getSource();
		if (src instanceof Viewer) {
			final Control viewerControl = ((Viewer) src).getControl();
			if (viewerControl == topTree) {
				selectionIsForVisibleViewer = true;
			}
		}

		if (!selectionIsForVisibleViewer) {
			return;
		}

		fSelection = event.getSelection();
		validate();
	}

	protected void validate() {
		Button ok_button = getButton(IDialogConstants.OK_ID);
		boolean isValid = true;
		if (getSelectedLaunchConfigurations().length < 1) {
			setErrorMessage(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_7);
			isValid = false;
		} else {
			setErrorMessage(null);
		}

		if (isValid) {
			if (fForEditing) {
				// must have only one selection
				if (getSelectedLaunchConfigurations().length > 1) {
					setErrorMessage(DebugUIMessages.GroupLaunchConfigurationSelectionDialog_11);
					isValid = false;
				}
			}
		}

		try {
			for (ILaunchConfiguration sel : getSelectedLaunchConfigurations()) {
				if (isValid && sel.getType().equals(groupType)) {
					// check whether there is a recursive reference to self
					isValid = !hasSelfRecursive(sel);
					setErrorMessage(isValid ? null : DebugUIMessages.GroupLaunchConfigurationSelectionDialog_0);
				}
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}

		if (isValid) {
			if (action == GroupElementPostLaunchAction.DELAY) {
				isValid = (actionParam instanceof Integer) && ((Integer) actionParam > 0);
				setErrorMessage(isValid ? null : DebugUIMessages.GroupLaunchConfigurationSelectionDialog_10);
			}

			if (action == GroupElementPostLaunchAction.OUTPUT_REGEXP) {
				isValid = actionParam instanceof String && !((String) actionParam).isEmpty();
				setErrorMessage(isValid ? null : DebugUIMessages.GroupLaunchConfigurationSelectionDialog_errorNoRegexp);
			}
		}

		if (ok_button != null) {
			ok_button.setEnabled(isValid);
		}
	}

	private boolean hasSelfRecursive(ILaunchConfiguration c) throws CoreException {
		if (c == null) {
			return false;
		}

		if(c.getType().equals(groupType)) {
			// it's a launch group
			if (c.getName().equals(selfRef.getName())) {
				return true;
			}

			// recurse to all elements of the group
			for (GroupLaunchElement e : GroupLaunchConfigurationDelegate.createLaunchElements(c)) {
				// if any of the contained configs is self
				if (hasSelfRecursive(e.data)) {
					return true;
				}
			}
		}

		return false;
	}

	public void setInitialSelection(GroupLaunchElement el) {
		action = el.action;
		actionParam = el.actionParam;
		adoptIfRunning = el.adoptIfRunning;
		fInitialSelection = new StructuredSelection(el.data);
		fSelection = fInitialSelection;
	}

	/**
	 * Required to satisfy the tree in mode inherit.
	 */
	private static final class InheritModeGroup implements ILaunchGroup {

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public ImageDescriptor getBannerImageDescriptor() {
			return null;
		}

		@Override
		public String getLabel() {
			return null;
		}

		@Override
		public String getIdentifier() {
			return null;
		}

		@Override
		public String getCategory() {
			return null;
		}

		@Override
		public String getMode() {
			return null;
		}

		@Override
		public boolean isPublic() {
			return false;
		}

	}
}
