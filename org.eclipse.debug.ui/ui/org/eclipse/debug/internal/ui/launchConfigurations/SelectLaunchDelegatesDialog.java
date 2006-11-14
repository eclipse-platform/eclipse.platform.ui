package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.Arrays;

import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.ibm.icu.text.MessageFormat;

/**
 * This dialog class enables users to select preferred launch tooling from a list of available tooling if there are 
 * duplicates for the current launch (config type and mode set)
 * 
 * @since 3.3
 * 
 * EXPERIMENTAL
 */
public class SelectLaunchDelegatesDialog extends SelectionDialog {

	/**
	 * Builds labels for list control
	 */
	class DelegatesLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {return null;}
		public String getText(Object element) {
			if(element instanceof ILaunchDelegate) {
				ILaunchDelegate ldp = (ILaunchDelegate) element;
				String name = ldp.getName();
				if(name == null) {
					name = ldp.getContributorName();
				}
				return name;
			}
			return element.toString();
		}
		public void addListener(ILabelProviderListener listener) {}
		public void dispose() {}
		public boolean isLabelProperty(Object element, String property) {return false;}
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	private static final String SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCH_DELEGATES_DIALOG"; //$NON-NLS-1$
	
	private CheckboxTableViewer fTableViewer = null;
	private Table fTable  = null;
	private ILaunchDelegate[] fDelegates = null;
	private Text fDescriptionText = null;
	private final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * Constructor
	 * @param parentShell the parent shell
	 * @param delegates the current delegates context
	 * 
	 * @throws CoreException
	 */
	public SelectLaunchDelegatesDialog(Shell parentShell, ILaunchDelegate[] delegates) {
		super(parentShell);
		super.setMessage(MessageFormat.format(LaunchConfigurationsMessages.SelectLaunchDelegatesDialog_0, new String[] {}));
		super.setTitle(LaunchConfigurationsMessages.SelectLaunchDelegatesDialog_1);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fDelegates = delegates;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		SWTUtil.createLabel(comp, LaunchConfigurationsMessages.SelectLaunchDelegatesDialog_2, 1);
		fTable = new Table(comp, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		fTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTableViewer = new CheckboxTableViewer(fTable);
		fTableViewer.setLabelProvider(new DelegatesLabelProvider());
		fTableViewer.setContentProvider(new ArrayContentProvider());
		fTableViewer.setInput(fDelegates);
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fTableViewer.setAllChecked(false);
				fTableViewer.setChecked(event.getElement(), true);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ss = (IStructuredSelection) event.getSelection();
				if(ss != null && !ss.isEmpty()) {
					fDescriptionText.setText(((ILaunchDelegate)ss.getFirstElement()).getDescription());
				}
				else {
					fDescriptionText.setText(EMPTY_STRING);
				}
			}
		});
		Group group = SWTUtil.createGroup(comp, LaunchConfigurationsMessages.SelectLaunchDelegatesDialog_3, 1, 1, GridData.FILL_BOTH);
		fDescriptionText = SWTUtil.createText(group, SWT.WRAP | SWT.READ_ONLY, 1, GridData.FILL_BOTH);
		fDescriptionText.setBackground(group.getBackground());
		Dialog.applyDialogFont(comp);		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.SELECT_LAUNCH_DELEGATES_DIALOG);
		return comp;
	}

	/**
	 * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		Object[] o =  fTableViewer.getCheckedElements();
		if(o.length > 0) {
			setResult(Arrays.asList(o));
		}
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(SETTINGS_ID);
		} 
		return section;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		IDialogSettings settings = getDialogBoundsSettings();
		if(settings != null) {
			try {
				int width = settings.getInt("DIALOG_WIDTH"); //$NON-NLS-1$
				int height = settings.getInt("DIALOG_HEIGHT"); //$NON-NLS-1$
				if(width > 0 & height > 0) {
					return new Point(width, height);
				}
			}
			catch (NumberFormatException nfe) {
				return new Point(350, 400);
			}
		}
		return new Point(350, 400);
	}

}
