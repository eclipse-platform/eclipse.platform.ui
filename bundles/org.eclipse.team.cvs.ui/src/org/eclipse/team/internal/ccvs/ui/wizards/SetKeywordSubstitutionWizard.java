package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * A wizard for changing the keyword substitution mode of files.
 * 
 * 1.  Compute the set of possibly affected resources
 * 2.  If the affected resources include existing committed files, warn the user
 *     and provide an option to exclude them from the operation.
 * 3.  If the affected resources include dirty files, warn the user and provide
 *     an option to commit them, or to exclude them from the operation.
 * 4.  Let the user select the desired keyword substitution mode.
 * 5.  Perform the operation on Finish.
 */
public class SetKeywordSubstitutionWizard extends Wizard {
	private SharedFilesPage sharedFilesPage;
	private OutgoingChangesPage outgoingChangesPage;
	private SelectModePage mainPage;
		
	private KSubstOption defaultKSubst;
	
	private IResource[] resources;
	private int depth;
	private List /* of IFile */ addedFiles;
	private List /* of IFile */ changedFiles;
	private List /* of IFile */ unchangedFiles;
	
	private static final int LIST_HEIGHT_HINT = 100;
	private static final int LABEL_WIDTH_HINT = 500;
	private static final int LABEL_INDENT_WIDTH = 32;
	private static final int VERTICAL_SPACE_HEIGHT = 10;

	/**
	 * Page to select keyword substitution mode.
	 */
	private static class SelectModePage extends WizardPage {
		private KSubstOption ksubst;
		private List ksubstOptions;
		private Button binaryRadioButton;
		private Button textRadioButton;
		private Button ksubstRadioButton;
		private Combo ksubstOptionCombo;
		
		public SelectModePage(String pageName, KSubstOption defaultKSubst) {
			super(pageName);
			this.ksubst = defaultKSubst;

			// sort the options by display text
			KSubstOption[] options = KSubstOption.getAllKSubstOptions();
			this.ksubstOptions = new ArrayList();
			for (int i = 0; i < options.length; i++) {
				KSubstOption option = options[i];
				if (! (Command.KSUBST_BINARY.equals(option) ||
					Command.KSUBST_TEXT.equals(option))) {
					ksubstOptions.add(option);
				}
			}
			Collections.sort(ksubstOptions, new Comparator() {
				public int compare(Object a, Object b) {
					String aKey = ((KSubstOption) a).getLongDisplayText();
					String bKey = ((KSubstOption) b).getLongDisplayText();
					return aKey.compareTo(bKey);
				}
			});
		}
		
		public void createControl(Composite parent) {
			Composite top = createTopControl(parent);
			setControl(top);
			createWrappingLabel(top, Policy.bind("SetKeywordSubstitution.SelectModePage.contents"));

			Listener selectionListener = new Listener() {
				public void handleEvent(Event event) {
					updateEnablements();
				}
			};

			// Binary
			binaryRadioButton = new Button(top, SWT.RADIO);
			binaryRadioButton.setText(Command.KSUBST_BINARY.getLongDisplayText());
			binaryRadioButton.addListener(SWT.Selection, selectionListener);
			binaryRadioButton.setSelection(Command.KSUBST_BINARY.equals(ksubst));
			createLabel(top, true, Policy.bind("SetKeywordSubstitution.SelectModePage.binaryLabel"));
			
			// Text
			textRadioButton = new Button(top, SWT.RADIO);
			textRadioButton.setText(Command.KSUBST_TEXT.getLongDisplayText());
			textRadioButton.addListener(SWT.Selection, selectionListener);
			textRadioButton.setSelection(Command.KSUBST_TEXT.equals(ksubst));
			createLabel(top, true, Policy.bind("SetKeywordSubstitution.SelectModePage.textLabel"));
			
			// Text with keyword substitution
			ksubstRadioButton = new Button(top, SWT.RADIO);
			ksubstRadioButton.setText(Policy.bind("SetKeywordSubstitution.SelectModePage.textWithSubstitutions"));
			ksubstRadioButton.addListener(SWT.Selection, selectionListener);
			ksubstRadioButton.setSelection(false);
			createLabel(top, true, Policy.bind("SetKeywordSubstitution.SelectModePage.textWithSubstitutionsLabel"));
			
			ksubstOptionCombo = new Combo(top, SWT.READ_ONLY);
			ksubstOptionCombo.addListener(SWT.Selection, selectionListener);
			GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_BEGINNING);
			data.horizontalIndent = LABEL_INDENT_WIDTH;
			ksubstOptionCombo.setLayoutData(data);

			// populate the combo box and select the default option
			for (int i = 0; i < ksubstOptions.size(); ++i) {
				KSubstOption option = (KSubstOption) ksubstOptions.get(i);
				ksubstOptionCombo.add(option.getLongDisplayText());
				if (option.equals(ksubst)) {
					ksubstOptionCombo.select(i);
					ksubstRadioButton.setSelection(true);
				} else if (option.equals(Command.KSUBST_TEXT_EXPAND)) {
					// if no expansion mode selected, show KSUBST_TEXT_EXPAND
					// since it is the server default
					if (! ksubstRadioButton.getSelection()) ksubstOptionCombo.select(i);
				}
			}
			updateEnablements();
		}
		
		/**
		 * Enable and disable controls based on the selected radio button.
		 */
		protected void updateEnablements() {
			if (binaryRadioButton.getSelection()) {
				ksubstOptionCombo.setEnabled(false);
				ksubst = Command.KSUBST_BINARY;
			}
			if (textRadioButton.getSelection()) {
				ksubstOptionCombo.setEnabled(false);
				ksubst = Command.KSUBST_TEXT;
			}
			if (ksubstRadioButton.getSelection()) {
				ksubstOptionCombo.setEnabled(true);
				ksubst = (KSubstOption) ksubstOptions.get(ksubstOptionCombo.getSelectionIndex());
			}
		}
		
		public KSubstOption getKSubstOption() {
			return ksubst;
		}
	}
	
	/**
	 * Page to warn user about possibly unintended changes to some files.
	 * Superclass of others.
	 */
	private static abstract class WarningPage extends WizardPage {
		private String[] elements;
		private ListViewer listViewer;
		private String groupTitle;
		
		public WarningPage(String pageName, String[] elements, String groupTitle) {
			super(pageName);
			this.elements = elements;
			this.groupTitle = groupTitle;
		}
		
		public void createAffectedFilesViewer(Composite parent) {
			Composite spacer = new Composite(parent, SWT.NONE);
			GridData data = new GridData();
			data.heightHint = VERTICAL_SPACE_HEIGHT;
			spacer.setLayoutData(data);
			
			Group group = new Group(parent, SWT.NONE);
			data = new GridData(GridData.FILL_BOTH);
			data.heightHint = LIST_HEIGHT_HINT;
			group.setLayoutData(data);
			group.setLayout(new FillLayout());
			group.setText(groupTitle);
			
			listViewer = new ListViewer(group, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
			listViewer.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					return (Object[]) inputElement;
				}
				public void dispose() {
				}
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
			});
			listViewer.setLabelProvider(new LabelProvider());
			listViewer.setSorter(new WorkbenchViewerSorter());
			listViewer.setInput(elements);
		}
	}

	/**
	 * Page to warn user about the side-effects of changing keyword
	 * substitution on already committed files.
	 */
	private static class SharedFilesPage extends WarningPage {
		private boolean onlyAddedFiles;
		private Button onlyAddedFilesButton;
		
		public SharedFilesPage(String pageName, String[] sharedFiles, boolean onlyAddedFiles) {
			super(pageName, sharedFiles,
				Policy.bind("SetKeywordSubstitution.SharedFilesPage.committedFiles"));
			this.onlyAddedFiles = onlyAddedFiles;
		}
		
		public void createControl(Composite parent) {
			Composite top = createTopControl(parent);
			setControl(top);
			createWrappingLabel(top, Policy.bind("SetKeywordSubstitution.SharedFilesPage.contents"));
			
			onlyAddedFilesButton = new Button(top, SWT.CHECK);
			onlyAddedFilesButton.setText(Policy.bind("SetKeywordSubstitution.SharedFilesPage.onlyAddedFilesButton"));
			onlyAddedFilesButton.setSelection(onlyAddedFiles);
			onlyAddedFilesButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					onlyAddedFiles = onlyAddedFilesButton.getSelection();
				}
			});
			
			createAffectedFilesViewer(top);
		}
		
		public boolean getOnlyAddedFiles() {
			return onlyAddedFiles;
		}
	}
	
	/**
	 * Page to warn user about uncommitted outgoing changes.
	 */
	private static class OutgoingChangesPage extends WarningPage {
		private boolean onlyCleanFiles;
		private Button onlyCleanFilesButton;
		
		public OutgoingChangesPage(String pageName, String[] changedFiles, boolean onlyCleanFiles) {
			super(pageName, changedFiles,
				Policy.bind("SetKeywordSubstitution.OutgoingChangesPage.changedFiles"));
			this.onlyCleanFiles = onlyCleanFiles;
		}
		
		public void createControl(Composite parent) {
			Composite top = createTopControl(parent);
			setControl(top);
			createWrappingLabel(top, Policy.bind("SetKeywordSubstitution.OutgoingChangesPage.contents"));
			
			onlyCleanFilesButton = new Button(top, SWT.CHECK);
			onlyCleanFilesButton.setText(Policy.bind("SetKeywordSubstitution.OutgoingChangesPage.onlyCleanFilesButton"));
			onlyCleanFilesButton.setSelection(onlyCleanFiles);
			onlyCleanFilesButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					onlyCleanFiles = onlyCleanFilesButton.getSelection();
				}
			});
			
			createAffectedFilesViewer(top);
		}
		
		public boolean getOnlyCleanFiles() {
			return onlyCleanFiles;
		}		
	}
	
	/**
	 * Creates a wizard to set the keyword substitution mode for the specified resources.
	 * 
	 * @param resources the resources to alter
	 * @param depth the recursion depth
	 * @param defaultOption the keyword substitution option to select by default
	 */
	public SetKeywordSubstitutionWizard(IResource[] resources, int depth, KSubstOption defaultOption) {
		super();
		this.resources = resources;
		this.depth = depth;
		this.defaultKSubst = defaultOption;
		setWindowTitle(Policy.bind("SetKeywordSubstitution.title"));
		initializeDefaultPageImageDescriptor();
	}

	/**
	 * Returns the keyword substitution option that was selected at the time
	 * the Finish button was pressed.
	 */
	public KSubstOption getKSubstOption() {
		return defaultKSubst;
	}

	public void addPages() {
		// add non-additions warning page
		if (changedFiles.size() != 0 || unchangedFiles.size() != 0) {
			String pageTitle = Policy.bind("SetKeywordSubstitution.SharedFilesPage.pageTitle"); //$NON-NLS-1$
			String pageDescription = Policy.bind("SetKeywordSubstitution.SharedFilesPage.pageDescription"); //$NON-NLS-1$

			String[] sharedFiles = new String[changedFiles.size() + unchangedFiles.size()];
			addNamesToArray(sharedFiles, 0, changedFiles);
			addNamesToArray(sharedFiles, changedFiles.size(), unchangedFiles);

			sharedFilesPage = new SharedFilesPage(pageTitle, sharedFiles, true);
			sharedFilesPage.setDescription(pageDescription);
			sharedFilesPage.setTitle(pageTitle);
			addPage(sharedFilesPage);
		}
		
		// add uncommitted changes page
		if (changedFiles.size() != 0) {
			String pageTitle = Policy.bind("SetKeywordSubstitution.OutgoingChangesPage.pageTitle"); //$NON-NLS-1$
			String pageDescription = Policy.bind("SetKeywordSubstitution.OutgoingChangesPage.pageDescription"); //$NON-NLS-1$

			String[] uncommittedFiles = new String[changedFiles.size()];
			addNamesToArray(uncommittedFiles, 0, changedFiles);

			outgoingChangesPage = new OutgoingChangesPage(pageTitle, uncommittedFiles, true);
			outgoingChangesPage.setDescription(pageDescription);
			outgoingChangesPage.setTitle(pageTitle);
			addPage(outgoingChangesPage);
		}
		
		// add main page
		String pageTitle = Policy.bind("SetKeywordSubstitution.SelectModePage.pageTitle"); //$NON-NLS-1$
		String pageDescription = Policy.bind("SetKeywordSubstitution.SelectModePage.pageDescription"); //$NON-NLS-1$
		mainPage = new SelectModePage(pageTitle, defaultKSubst);
		mainPage.setDescription(pageDescription);
		mainPage.setTitle(pageTitle);
		addPage(mainPage);
	}
	
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == sharedFilesPage) {
			if (! sharedFilesPage.getOnlyAddedFiles() && outgoingChangesPage != null) {
				return outgoingChangesPage;
			}
			return mainPage;
		} else if (page == outgoingChangesPage) {
			return mainPage;
		}
		return null;
	}
	
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page == outgoingChangesPage) {
			return sharedFilesPage;
		} else if (page == mainPage) {
			if (sharedFilesPage != null && sharedFilesPage.getOnlyAddedFiles()) {
				// we must have skipped over the outgoing changes
				return sharedFilesPage;
			} else if (outgoingChangesPage != null) {
				return outgoingChangesPage;
			}
			return sharedFilesPage;
		}
		return null;
	}
		
	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean needsProgressMonitor() {
		return true;
	}

	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean needsPreviousAndNextButtons() {
		return true;
	}
	
	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		try {
			KSubstOption ksubst = mainPage.getKSubstOption();
			defaultKSubst = ksubst;
			List affectedResources = addedFiles;
			if (sharedFilesPage != null && ! sharedFilesPage.getOnlyAddedFiles()) {
				affectedResources.addAll(unchangedFiles);
				if (outgoingChangesPage != null && ! outgoingChangesPage.getOnlyCleanFiles()) {
					affectedResources.addAll(changedFiles);
				}
			}
			IRunnableWithProgress operation = new SetKeywordSubstitutionOperation(
				(IResource[]) affectedResources.toArray(new IResource[affectedResources.size()]),
				IResource.DEPTH_ONE, ksubst, getShell());
			getContainer().run(false /*fork*/, true /*cancelable*/, operation);
			return true;
		} catch (InterruptedException e1) {
			return true;
		} catch (InvocationTargetException e2) {
			if (e2.getTargetException() instanceof CoreException) {
				CoreException e = (CoreException) e2.getTargetException();
				ErrorDialog.openError(getShell(), Policy.bind("SetKeywordSubstitution.problemsMessage"), null, e.getStatus()); //$NON-NLS-1$
				return false;
			} else {
				Throwable target = e2.getTargetException();
				if (target instanceof RuntimeException) {
					throw (RuntimeException) target;
				}
				if (target instanceof Error) {
					throw (Error) target;
				}
			}
			return true;
		}
	}

	/**
	 * Declares the wizard banner image descriptor
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath;
		if (Display.getCurrent().getIconDepth() > 4) {
			iconPath = "icons/full/"; //$NON-NLS-1$
		} else {
			iconPath = "icons/basic/"; //$NON-NLS-1$
		}
		try {
			URL installURL = CVSUIPlugin.getPlugin().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + "wizban/setksubst_wizban.gif");	//$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		} catch (MalformedURLException e) {
			// Should not happen.  Ignore.
		}
	}	

	private static void createLabel(Composite parent, boolean indent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		if (indent) data.horizontalIndent = LABEL_INDENT_WIDTH;
		label.setLayoutData(data);
	}
	
	private static void createWrappingLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setText(text);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		data.widthHint = LABEL_WIDTH_HINT;
		label.setLayoutData(data);		
	}
	
	private static Composite createTopControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		return composite;
	}
	
	private void addNamesToArray(String[] array, int i, List /* of IFile */ files) {
		for (Iterator it = files.iterator(); it.hasNext();) {
			IFile file = (IFile) it.next();
			array[i++] = file.getFullPath().toString();
		}
	}			

	/**
	 * Populates the addedFiles, changedFiles, and unchangedFiles according to the
	 * selected resources.  Ignores repeated occurrences of resources, unmanaged
	 * resources, and deleted resources.
	 */
	public void prepareToOpen() throws TeamException {
		addedFiles = new ArrayList();
		changedFiles = new ArrayList();
		unchangedFiles = new ArrayList();
		final Set seen = new HashSet();
		final TeamException[] holder = new TeamException[1];
		try {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						try {
							if (! seen.contains(resource)) {
								seen.add(resource);
								if (resource.getType() == IResource.FILE && resource.exists()) {
									IFile file = (IFile) resource;
									ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
									if (cvsFile.isManaged()) {
										ResourceSyncInfo info = cvsFile.getSyncInfo();
										if (info.isAdded()) {
											addedFiles.add(file);
										} else if (info.isDeleted()) {
											// ignore deletions
										} else if (cvsFile.isModified()) {
											changedFiles.add(file);
										} else {
											unchangedFiles.add(file);
										}
									}
								}
							}
							return true;
						} catch (TeamException e) {
							holder[0] = e;
							throw new CoreException(e.getStatus());
						}
					}
				}, depth, false);
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			if (holder[0] != null) throw holder[0];
		}
	}	
}