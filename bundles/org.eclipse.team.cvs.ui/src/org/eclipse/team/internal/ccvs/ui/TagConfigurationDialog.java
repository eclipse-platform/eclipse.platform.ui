/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSFileElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSFolderElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSRootFolderElement;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Allows configuration of the CVS tags that are shown within the workbench.
 */
public class TagConfigurationDialog extends Dialog {
	
	// show the resource contained within the roots
	private TreeViewer cvsResourceTree;
	
	// shows the tags found on the selected resources
	private CheckboxTableViewer cvsTagTree;
	
	// shows the defined tags for the given root
	private TreeViewer cvsDefinedTagsTree;
	
	// remember the root element in the defined tags tree
	private ProjectElement cvsDefinedTagsRootElement;
	
	// list of auto-refresh files
	private org.eclipse.swt.widgets.List autoRefreshFileList;
	
	// folders from which their children files can be examined for tags
	private ICVSFolder[] roots;
	private ICVSFolder root;
	
	// enable selecting auto-refresh files
	private boolean allowSettingAutoRefreshFiles = true;
	
	// sizing default hints
	private final int ALLOWREFRESH_WIDTH = 500;
	private final int ALLOWREFRESH_HEIGHT = 625;
	private final int NOREFRESH_WIDTH = 500;
	private final int NOREFRESH_HEIGHT = 550;
	
	// preference keys
	private final String ALLOWREFRESH_WIDTH_KEY = "AllowRefreshWidth"; //$NON-NLS-1$
	private final String ALLOWREFRESH_HEIGHT_KEY = "AllowRefreshHeight"; //$NON-NLS-1$
	private final String NOREFRESH_WIDTH_KEY = "NoRefreshWidth"; //$NON-NLS-1$
	private final String NOREFRESH_HEIGHT_KEY = "NoRefreshHeight"; //$NON-NLS-1$

	// buttons
	private Button addSelectedTagsButton;
	private Button addSelectedFilesButton;
	private Button removeFileButton;
	private Button removeTagButton;
	
	// dialogs settings that are persistent between workbench sessions
	private IDialogSettings settings;
	
	public TagConfigurationDialog(Shell shell, ICVSFolder[] roots) {
		super(shell);
		setShellStyle(SWT.CLOSE|SWT.RESIZE);
		this.roots = roots;
		this.root = roots[0];
		if(roots.length>1) {
			allowSettingAutoRefreshFiles = false;
		}
		
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		this.settings = workbenchSettings.getSection("TagConfigurationDialog");//$NON-NLS-1$
		if (settings == null) {
			this.settings = workbenchSettings.addNewSection("TagConfigurationDialog");//$NON-NLS-1$
		}
	}

	/**
	 * @see Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if(roots.length==1) {
			newShell.setText(Policy.bind("TagConfigurationDialog.1", roots[0].getName())); //$NON-NLS-1$
		} else {
			newShell.setText(Policy.bind("TagConfigurationDialog.2", Integer.toString(roots.length))); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite shell = new Composite(parent, SWT.NONE);
		GridData data = new GridData (GridData.FILL_BOTH);		
		shell.setLayoutData(data);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = true;
		shell.setLayout (gridLayout);

		Label description = new Label(shell,SWT.WRAP);
		description.setText(Policy.bind("TagConfigurationDialog.4")); //$NON-NLS-1$
		data = new GridData (GridData.FILL_BOTH);
		data.widthHint = 300;
		data.horizontalSpan = 2;
		description.setLayoutData(data);
		
		Label cvsResourceTreeLabel = new Label (shell, SWT.NONE);
		cvsResourceTreeLabel.setText (Policy.bind("TagConfigurationDialog.5")); //$NON-NLS-1$
		data = new GridData ();
		data.horizontalSpan = 1;
		cvsResourceTreeLabel.setLayoutData (data);

		Label cvsTagTreeLabel = new Label (shell, SWT.NONE);
		cvsTagTreeLabel.setText (Policy.bind("TagConfigurationDialog.6")); //$NON-NLS-1$
		data = new GridData ();
		data.horizontalSpan = 1;
		cvsTagTreeLabel.setLayoutData (data);
		
		Tree tree = new Tree(shell, SWT.BORDER | SWT.MULTI);
		cvsResourceTree = new TreeViewer (tree);
		cvsResourceTree.setContentProvider(new RemoteContentProvider());
		cvsResourceTree.setLabelProvider(new WorkbenchLabelProvider());
		data = new GridData (GridData.FILL_BOTH);
		data.heightHint = 150;
		data.horizontalSpan = 1;
		cvsResourceTree.getTree().setLayoutData(data);
		if(roots.length==1) {
			cvsResourceTree.setInput(new CVSFolderElement(roots[0], false /*don't include unmanaged resources*/));
		} else {
			cvsResourceTree.setInput(new CVSRootFolderElement(roots));
		}
		
		cvsResourceTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateShownTags();
				updateEnablements();
			}
		});

		Table table = new Table(shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.CHECK);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 150;
		data.horizontalSpan = 1;
		table.setLayoutData(data);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(60, true));
		table.setLayout(layout);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		cvsTagTree = new CheckboxTableViewer(table);
		cvsTagTree.setContentProvider(new WorkbenchContentProvider());
		cvsTagTree.setLabelProvider(new WorkbenchLabelProvider());
		cvsTagTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablements();
			}
		});
		
		cvsTagTree.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (!(e1 instanceof TagElement) || !(e2 instanceof TagElement)) return super.compare(viewer, e1, e2);
				CVSTag tag1 = ((TagElement)e1).getTag();
				CVSTag tag2 = ((TagElement)e2).getTag();
				int type1 = tag1.getType();
				int type2 = tag2.getType();
				if (type1 != type2) {
					return type2 - type1;
				}
				return -tag1.compareTo(tag2);
			}
		});
		
		Composite rememberedTags = new Composite(shell, SWT.NONE);
		data = new GridData (GridData.FILL_BOTH);		
		data.horizontalSpan = 2;
		rememberedTags.setLayoutData(data);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		rememberedTags.setLayout (gridLayout);

		Label rememberedTagsLabel = new Label (rememberedTags, SWT.NONE);
		rememberedTagsLabel.setText (Policy.bind("TagConfigurationDialog.7")); //$NON-NLS-1$
		data = new GridData ();
		data.horizontalSpan = 2;
		rememberedTagsLabel.setLayoutData (data);
		
		tree = new Tree(rememberedTags, SWT.BORDER | SWT.MULTI);
		cvsDefinedTagsTree = new TreeViewer (tree);
		cvsDefinedTagsTree.setContentProvider(new WorkbenchContentProvider());
		cvsDefinedTagsTree.setLabelProvider(new WorkbenchLabelProvider());
		data = new GridData (GridData.FILL_BOTH);
		data.heightHint = 100;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		cvsDefinedTagsTree.getTree().setLayoutData(data);
		cvsDefinedTagsRootElement = new ProjectElement(roots[0], false /* don't show HEAD */);
		cvsDefinedTagsRootElement.getBranches().add(CVSUIPlugin.getPlugin().getRepositoryManager().getKnownBranchTags(root));
		cvsDefinedTagsRootElement.getVersions().add(CVSUIPlugin.getPlugin().getRepositoryManager().getKnownVersionTags(root));
		cvsDefinedTagsTree.setInput(cvsDefinedTagsRootElement);
		cvsDefinedTagsTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablements();
			}
		});
		cvsDefinedTagsTree.setSorter(new ViewerSorter() {
			public int compare(Viewer v, Object o1, Object o2) {
				int result = super.compare(v, o1, o2);
				if (o1 instanceof TagElement && o2 instanceof TagElement) {
					return -result;
				}
				return result;
			}
		});

		Composite buttonComposite = new Composite(rememberedTags, SWT.NONE);
		data = new GridData ();
		data.verticalAlignment = GridData.BEGINNING;		
		buttonComposite.setLayoutData(data);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		buttonComposite.setLayout (gridLayout);
		
		addSelectedTagsButton = new Button (buttonComposite, SWT.PUSH);
		addSelectedTagsButton.setText (Policy.bind("TagConfigurationDialog.8")); //$NON-NLS-1$
		data = getStandardButtonData(addSelectedTagsButton);
		data.horizontalAlignment = GridData.FILL;
		addSelectedTagsButton.setLayoutData(data);
		addSelectedTagsButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					rememberCheckedTags();
				}
			});			
			
		removeTagButton = new Button (buttonComposite, SWT.PUSH);
		removeTagButton.setText (Policy.bind("TagConfigurationDialog.9")); //$NON-NLS-1$
		data = getStandardButtonData(removeTagButton);
		data.horizontalAlignment = GridData.FILL;		
		removeTagButton.setLayoutData(data);
		removeTagButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					deleteSelected();
				}
			});
			
		Button removeAllTags = new Button (buttonComposite, SWT.PUSH);
		removeAllTags.setText (Policy.bind("TagConfigurationDialog.10")); //$NON-NLS-1$
		data = getStandardButtonData(removeAllTags);
		data.horizontalAlignment = GridData.FILL;		
		removeAllTags.setLayoutData(data);
		removeAllTags.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					removeAllKnownTags();
				}
			});
		
		if(allowSettingAutoRefreshFiles) {
			Label explanation = new Label(rememberedTags, SWT.WRAP);
			explanation.setText(Policy.bind("TagConfigurationDialog.11")); //$NON-NLS-1$
			data = new GridData ();
			data.horizontalSpan = 2;
			data.widthHint = 300;
			explanation.setLayoutData(data);
			
			autoRefreshFileList = new org.eclipse.swt.widgets.List(rememberedTags, SWT.BORDER | SWT.MULTI);	 
			data = new GridData ();		
			data.heightHint = 45;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			autoRefreshFileList.setLayoutData(data);		
			autoRefreshFileList.setItems(CVSUIPlugin.getPlugin().getRepositoryManager().getAutoRefreshFiles(roots[0]));
			autoRefreshFileList.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					updateEnablements();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					updateEnablements();
				}
			});
	
			Composite buttonComposite2 = new Composite(rememberedTags, SWT.NONE);
			data = new GridData ();
			data.verticalAlignment = GridData.BEGINNING;		
			buttonComposite2.setLayoutData(data);
			gridLayout = new GridLayout();
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			buttonComposite2.setLayout (gridLayout);
	
			addSelectedFilesButton = new Button (buttonComposite2, SWT.PUSH);
			addSelectedFilesButton.setText (Policy.bind("TagConfigurationDialog.12")); //$NON-NLS-1$
			data = getStandardButtonData(addSelectedFilesButton);
			data.horizontalAlignment = GridData.FILL;
			addSelectedFilesButton.setLayoutData(data);
			addSelectedFilesButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						addSelectionToAutoRefreshList();
					}
				});			
				
			removeFileButton = new Button (buttonComposite2, SWT.PUSH);
			removeFileButton.setText (Policy.bind("TagConfigurationDialog.13")); //$NON-NLS-1$
			data = getStandardButtonData(removeFileButton);
			data.horizontalAlignment = GridData.FILL;		
			removeFileButton.setLayoutData(data);
			removeFileButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						String[] selected = autoRefreshFileList.getSelection();
						for (int i = 0; i < selected.length; i++) {
							autoRefreshFileList.remove(selected[i]);
							autoRefreshFileList.setFocus();
						}
					}
				});			
		}
			
		Label seperator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData (GridData.FILL_BOTH);		
		data.horizontalSpan = 2;
		seperator.setLayoutData(data);

		updateEnablements();
		return shell;
	}
	
	private void updateShownTags() {
		final CVSFileElement[] filesSelection = getSelectedFiles();
		final CVSTag[][] elements = new CVSTag[1][];
		if(filesSelection.length!=0) {
			BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
				public void run() {
					try {
						Set tags = new HashSet();
						for (int i = 0; i < filesSelection.length; i++) {
							ICVSFile file = filesSelection[i].getCVSFile();
							tags.addAll(Arrays.asList(getTagsFor(file, new NullProgressMonitor())));
						}
						elements[0] = (CVSTag[]) tags.toArray(new CVSTag[tags.size()]);
					} catch (TeamException e) {
					}
				}
			});
			cvsTagTree.getTable().removeAll();
			
			for (int i = 0; i < elements[0].length; i++) {
				CVSTag tag = elements[0][i];
				List knownTags = new ArrayList();
				knownTags.addAll(Arrays.asList(cvsDefinedTagsRootElement.getBranches().getTags()));
				knownTags.addAll(Arrays.asList(cvsDefinedTagsRootElement.getVersions().getTags()));
				if(!knownTags.contains(tag)) {
					TagElement tagElem = new TagElement(tag);
					cvsTagTree.add(tagElem);
					cvsTagTree.setChecked(tagElem, true);
				}
			}
		}
	}
	
	private CVSFileElement[] getSelectedFiles() {
		IStructuredSelection selection = (IStructuredSelection)cvsResourceTree.getSelection();
		if (!selection.isEmpty()) {
			final List filesSelection = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof CVSFileElement) {
					filesSelection.add(o);
				}
			}
			return (CVSFileElement[]) filesSelection.toArray(new CVSFileElement[filesSelection.size()]);
		}
		return new CVSFileElement[0];
	}
	
	private void addSelectionToAutoRefreshList() {
		IStructuredSelection selection = (IStructuredSelection)cvsResourceTree.getSelection();
		if (!selection.isEmpty()) {
			final List filesSelection = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof CVSFileElement) {
					filesSelection.add(o);
				}
			}
			if(!filesSelection.isEmpty()) {
				for (it = filesSelection.iterator(); it.hasNext();) {
					try {
						String relativePath = ((CVSFileElement)it.next()).getCVSFile().getRelativePath(root);
						if(autoRefreshFileList.indexOf(relativePath)==-1) {
							autoRefreshFileList.add(relativePath);					
						}
					} catch(CVSException e) {
					}
				}
			}
		}
	}
	
	private CVSTag[] getTagsFor(ICVSFile file, IProgressMonitor monitor) throws TeamException {
		Set tagSet = new HashSet();
		ILogEntry[] entries = file.getLogEntries(monitor);
		for (int j = 0; j < entries.length; j++) {
			CVSTag[] tags = entries[j].getTags();
			for (int k = 0; k < tags.length; k++) {
				tagSet.add(tags[k]);
			}
		}
		return (CVSTag[])tagSet.toArray(new CVSTag[tagSet.size()]);
	}
	
	private void rememberCheckedTags() {
		Object[] checked = cvsTagTree.getCheckedElements();
		for (int i = 0; i < checked.length; i++) {
			CVSTag tag = ((TagElement)checked[i]).getTag();
			if(tag.getType() == CVSTag.BRANCH) {
				cvsDefinedTagsRootElement.getBranches().add(new CVSTag[] {tag});
			} else {
				cvsDefinedTagsRootElement.getVersions().add(new CVSTag[] {tag});
			}
		}
		cvsDefinedTagsTree.refresh();
	}
	
	private void deleteSelected() {
		IStructuredSelection selection = (IStructuredSelection)cvsDefinedTagsTree.getSelection();
		if (!selection.isEmpty()) {
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof TagElement) {
					CVSTag tag = ((TagElement)o).getTag();
					if(tag.getType() == CVSTag.BRANCH) {
						cvsDefinedTagsRootElement.getBranches().remove(tag);
					} else if(tag.getType()==CVSTag.VERSION) {						
						cvsDefinedTagsRootElement.getVersions().remove(tag);
					}					
				}
			}
		}
		cvsDefinedTagsTree.refresh();
	}
	
	private boolean isTagSelectedInKnownTagTree() {
		IStructuredSelection selection = (IStructuredSelection)cvsDefinedTagsTree.getSelection();
		if (!selection.isEmpty()) {
			final List versions = new ArrayList();
			final List branches = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof TagElement) {
					return true;		
				}
			}
		}
		return false;
	}

	private void removeAllKnownTags() {
		cvsDefinedTagsRootElement.getBranches().removeAll();
		cvsDefinedTagsRootElement.getVersions().removeAll();
		cvsDefinedTagsTree.refresh();
	}
	
	private void updateEnablements() {
		// add checked tags
		Object[] checked = cvsTagTree.getCheckedElements();
		addSelectedTagsButton.setEnabled(checked.length!=0?true:false);
		
		// Remove known tags
		removeTagButton.setEnabled(isTagSelectedInKnownTagTree()?true:false);
		
		if(allowSettingAutoRefreshFiles) {
			// add selected files
			addSelectedFilesButton.setEnabled(getSelectedFiles().length!=0?true:false);
			
			// remove auto refresh files
			removeFileButton.setEnabled(autoRefreshFileList.getSelection().length!=0?true:false);		
		}
	}
	
	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		// save auto refresh file names
		if(allowSettingAutoRefreshFiles) {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			String[] oldFileNames = manager.getAutoRefreshFiles(root);
			manager.removeAutoRefreshFiles(root, oldFileNames);
			String[] newFileNames = autoRefreshFileList.getItems();
			if(newFileNames.length!=0) {
				manager.addAutoRefreshFiles(root, autoRefreshFileList.getItems());
			}		
		}
		
		// save defined tags
		CVSTag[] branches = cvsDefinedTagsRootElement.getBranches().getTags();
		CVSTag[] versions = cvsDefinedTagsRootElement.getVersions().getTags();
		
		// update all project with the same version tags
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();				
		manager.notifyRepoView = false;
		CVSTag[] oldTags = manager.getKnownBranchTags(root);
		manager.removeBranchTag(root, oldTags);
		for(int i = 0; i < roots.length; i++) {
			if(branches.length>0) {
				manager.addBranchTags(root, branches);
			}
			oldTags = manager.getKnownVersionTags(roots[i]);
			manager.removeVersionTags(roots[i], oldTags);
			if(versions.length>0) {
				manager.addVersionTags(roots[i], versions);
			}			
		}
		// XXX hack to force repo view to refresh only once
		manager.notifyRepoView = true;
		manager.addVersionTags(root, new CVSTag[0]);
		// end hack
		
		super.okPressed();
	}
	
	/*
	 * Returns a button that implements the standard refresh tags operation. The runnable is run immediatly after 
	 * the tags are fetched from the server. A client should refresh their widgets that show tags because they
	 * may of changed. 
	 */
	private static Button createTagRefreshButton(final Shell shell, Composite composite, String title, final IProject project, final Runnable runnable) {
		Button refreshButton = new Button(composite, SWT.PUSH);
		refreshButton.setText (title);
		updateToolTipHelpForRefreshButton(refreshButton, project);
		refreshButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
						public void run() {
							try {
								CVSUIPlugin.getPlugin().getRepositoryManager().refreshDefinedTags(CVSWorkspaceRoot.getCVSFolderFor((project)), true);
								runnable.run();
							} catch(TeamException e) {
								ErrorDialog.openError(shell, Policy.bind("TagConfigurationDialog.14"), e.getMessage(), e.getStatus()); //$NON-NLS-1$
							}
						}
					});
				}
			});	
		return refreshButton;		
	 }
	 
	private static void updateToolTipHelpForRefreshButton(Button button, IProject project) {
		StringBuffer tooltip = new StringBuffer(Policy.bind("TagConfigurationDialog.15")); //$NON-NLS-1$
		String[] autoFiles = CVSUIPlugin.getPlugin().getRepositoryManager().getAutoRefreshFiles(CVSWorkspaceRoot.getCVSFolderFor(project));
		tooltip.append(" - .project\n"); //$NON-NLS-1$
		for (int i = 0; i < autoFiles.length; i++) {
			tooltip.append("-  " + autoFiles[i] + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		tooltip.append(Policy.bind("TagConfigurationDialog.19")); //$NON-NLS-1$
		button.setToolTipText(tooltip.toString());
	 }
	 
	 public static void createTagDefinitionButtons(final Shell shell, Composite composite, final IProject[] projects, int hHint, int wHint, final Runnable afterRefresh, final Runnable afterConfigure) {
	 	Composite buttonComp = new Composite(composite, SWT.NONE);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.END;		
		buttonComp.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonComp.setLayout (layout);
	 	
	 	final Button refreshButton = TagConfigurationDialog.createTagRefreshButton(shell, buttonComp, Policy.bind("TagConfigurationDialog.20"), projects[0], afterRefresh); //$NON-NLS-1$
		data = new GridData();
		if(hHint!=0 && wHint!=0) {
			data.heightHint = hHint;
			int widthHint = wHint;
			data.widthHint = Math.max(widthHint, refreshButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		}
		data.horizontalAlignment = GridData.END;
		data.horizontalSpan = 1;
		refreshButton.setLayoutData (data);

		Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText (Policy.bind("TagConfigurationDialog.21")); //$NON-NLS-1$
		data = new GridData ();
		if(hHint!=0 && wHint!=0) {
			data.heightHint = hHint;
			int widthHint = wHint;
			data.widthHint = Math.max(widthHint, addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		}
		data.horizontalAlignment = GridData.END;
		data.horizontalSpan = 1;
		addButton.setLayoutData (data);
		addButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					ICVSFolder[] roots = new ICVSFolder[projects.length];
					for (int i = 0; i < projects.length; i++) {
						roots[i] = CVSWorkspaceRoot.getCVSFolderFor(projects[i]);
					}
					TagConfigurationDialog d = new TagConfigurationDialog(shell, roots);
					d.open();
					TagConfigurationDialog.updateToolTipHelpForRefreshButton(refreshButton, projects[0]);
					afterConfigure.run();
				}
			});		
	 }
	 
	/**
	 * @see Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		int width, height;
		if(allowSettingAutoRefreshFiles) {
			try {
				height = settings.getInt(ALLOWREFRESH_HEIGHT_KEY);
				width = settings.getInt(ALLOWREFRESH_WIDTH_KEY);
			} catch(NumberFormatException e) {
				height = ALLOWREFRESH_HEIGHT;
				width = ALLOWREFRESH_WIDTH;
			}
		} else {
			try {
				height = settings.getInt(NOREFRESH_HEIGHT_KEY);
				width = settings.getInt(NOREFRESH_WIDTH_KEY);
			} catch(NumberFormatException e) {
				height = NOREFRESH_HEIGHT;
				width = NOREFRESH_WIDTH;
			}
		}
		return new Point(width, height);
	}
	
	/**
	 * @see Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		super.cancelPressed();
	}
	
	private GridData getStandardButtonData(Button button) {
		GridData data = new GridData();
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		return data;
	}

	/**
	 * @see Window#close()
	 */
	public boolean close() {
		Rectangle bounds = getShell().getBounds();
		if(allowSettingAutoRefreshFiles) {
			settings.put(ALLOWREFRESH_HEIGHT_KEY, bounds.height);
			settings.put(ALLOWREFRESH_WIDTH_KEY, bounds.width);
		} else {
			settings.put(NOREFRESH_HEIGHT_KEY, bounds.height);
			settings.put(NOREFRESH_WIDTH_KEY, bounds.width);
		}
		return super.close();
	}
}