package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IUserInfo;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.dialogs.PropertyPage;

public class CVSPropertiesPage extends PropertyPage {
	IProject project;
	
	// Widgets
	Text userText;
	Text passwordText;
	Combo methodType;
	Label hostLabel;
	Label pathLabel;
	Label moduleLabel;
	Label portLabel;
	
	boolean passwordChanged;

	IUserInfo info;
	CVSTeamProvider provider;
		
	/*
	 * @see PreferencesPage#createContents
	 */
	protected Control createContents(Composite parent) {
		initialize();
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Label label = createLabel(composite, Policy.bind("CVSPropertiesPage.connectionType"));
		methodType = createCombo(composite);
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.user"));
		userText = createTextField(composite);
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.password"));
		passwordText = createTextField(composite);
		passwordText.setEchoChar('*');
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.host"));
		hostLabel = createLabel(composite, "");
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.port"));
		portLabel = createLabel(composite, "");
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.path"));
		pathLabel = createLabel(composite, "");
		
		label = createLabel(composite, Policy.bind("CVSPropertiesPage.module"));
		moduleLabel = createLabel(composite, "");
		
		initializeValues();
		passwordText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				passwordChanged = true;
			}
		});
		return composite;
	}
	/**
	 * Utility method that creates a combo box
	 *
	 * @param parent  the parent for the new label
	 * @return the new widget
	 */
	protected Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		combo.setLayoutData(data);
		return combo;
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
	/**
	 * Create a text field specific for this application
	 *
	 * @param parent  the parent of the new text field
	 * @return the new text field
	 */
	protected Text createTextField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		text.setLayoutData(data);
		return text;
	}
	/**
	 * Initializes the page
	 */
	private void initialize() {
		// Get the project that is the source of this property page
		project = null;
		IAdaptable element = getElement();
		if (element instanceof IProject) {
			project = (IProject)element;
		} else {
			Object adapter = element.getAdapter(IProject.class);
			if (adapter instanceof IProject) {
				project = (IProject)adapter;
			}
		}
	}
	/**
	 * Set the initial values of the widgets
	 */
	private void initializeValues() {
		passwordChanged = false;
		
		provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(project);
		if (provider == null) return;
		
		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
		String[] methods = CVSProviderPlugin.getProvider().getSupportedConnectionMethods();
		for (int i = 0; i < methods.length; i++) {
			methodType.add(methods[i]);
		}
		try {
			ICVSRepositoryLocation location = cvsRoot.getRemoteLocation();
			String method = location.getMethod().getName();
			methodType.select(methodType.indexOf(method));
		
			info = location.getUserInfo(true);
			userText.setText(info.getUsername());
		} catch (TeamException e) {
			handle(e);
		}
		passwordText.setText("*********");
		
		try {
			ICVSRemoteResource resource = cvsRoot.getRemoteResourceFor(project);
			ICVSRepositoryLocation location = resource.getRepository();
			hostLabel.setText(location.getHost());
			int port = location.getPort();
			if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT) {
				portLabel.setText(Policy.bind("CVSPropertiesPage.defaultPort"));
			} else {
				portLabel.setText("" + port);
			}
			pathLabel.setText(location.getRootDirectory());
			moduleLabel.setText(resource.getRepositoryRelativePath());
		} catch (TeamException e) {
			handle(e);
		}
	}
	/*
	 * @see PreferencesPage#performOk
	 */
	public boolean performOk() {
		info.setUsername(userText.getText());
		if (passwordChanged) {
			info.setPassword(passwordText.getText());
		}
		final String type = methodType.getText();
		try {
			new ProgressMonitorDialog(getShell()).run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						provider.setConnectionInfo(project, type, info, monitor);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof TeamException) {
				handle((TeamException)t);
			} else if (t instanceof CoreException) {
				handle(((CoreException)t).getStatus());
			} else {
				IStatus status = new Status(IStatus.ERROR, CVSUIPlugin.ID, 1, "Internal error occured", t);
				handle(status);
				CVSUIPlugin.log(status);
			}
		} catch (InterruptedException e) {
		}
					
		return true;
	}
	/**
	 * Shows the given errors to the user.
	 */
	protected void handle(TeamException e) {
		handle(e.getStatus());
	}
	
	protected void handle(IStatus status) {
		if (!status.isOK()) {
			IStatus toShow = status;
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1) {
					toShow = children[0];
				}
			}
			ErrorDialog.openError(getShell(), status.getMessage(), null, toShow);
		}
	}
}

