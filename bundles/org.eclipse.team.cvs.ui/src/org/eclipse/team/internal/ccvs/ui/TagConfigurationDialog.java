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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement.ProjectElementSorter;
import org.eclipse.team.internal.ccvs.ui.model.CVSFileElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSFolderElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSRootFolderElement;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.repo.NewDateTagAction;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
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
	
	class FileSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			boolean oneIsFile = e1 instanceof CVSFileElement;
			boolean twoIsFile = e2 instanceof CVSFileElement;
			if (oneIsFile != twoIsFile) {
				return oneIsFile ? 1 : -1;
			}
			return super.compare(viewer, e1, e2);
		}
	}
	
	public TagConfigurationDialog(Shell shell, ICVSFolder[] roots) {
		super(shell);
		setShellStyle(SWT.CLOSE|SWT.RESIZE|SWT.APPLICATION_MODAL);
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
	    gridLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	    gridLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);        
		shell.setLayout (gridLayout);
		
		Composite comp = new Composite(shell, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		comp.setLayout(gridLayout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label cvsResourceTreeLabel = new Label(comp, SWT.NONE);
		cvsResourceTreeLabel.setText(Policy.bind("TagConfigurationDialog.5")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 1;
		cvsResourceTreeLabel.setLayoutData(data);
	
		Tree tree = new Tree(comp, SWT.BORDER | SWT.MULTI);
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
		cvsResourceTree.setSorter(new FileSorter());
		cvsResourceTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateShownTags();
				updateEnablements();
			}
		});
	
	
		comp = new Composite(shell, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		comp.setLayout(gridLayout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		Label cvsTagTreeLabel = new Label(comp, SWT.NONE);
		cvsTagTreeLabel.setText(Policy.bind("TagConfigurationDialog.6")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 1;
		cvsTagTreeLabel.setLayoutData(data);
		
		final Table table = new Table(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.CHECK);
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
		
		Composite selectComp = new Composite(comp, SWT.NONE);
		GridLayout selectLayout = new GridLayout(2, true);
		selectLayout.marginHeight = selectLayout.marginWidth = 0;
		selectComp.setLayout(selectLayout);
		selectComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button selectAllButton = new Button(selectComp, SWT.PUSH);
		selectAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAllButton.setText(Policy.bind("ReleaseCommentDialog.selectAll")); //$NON-NLS-1$
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int nItems = table.getItemCount();
				for (int j=0; j<nItems; j++)
					table.getItem(j).setChecked(true);
			}
		});
		Button deselectAllButton = new Button(selectComp, SWT.PUSH);
		deselectAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAllButton.setText(Policy.bind("ReleaseCommentDialog.deselectAll")); //$NON-NLS-1$
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int nItems = table.getItemCount();
				for (int j=0; j<nItems; j++)
					table.getItem(j).setChecked(false);
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
					return  type1 - type2;
				}
				// Sort in reverse order so larger numbered versions are at the top
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
		cvsDefinedTagsRootElement = new ProjectElement(roots[0], ProjectElement.INCLUDE_BRANCHES | ProjectElement.INCLUDE_VERSIONS |ProjectElement.INCLUDE_DATES);
		cvsDefinedTagsRootElement.getBranches().add(CVSUIPlugin.getPlugin().getRepositoryManager().getKnownTags(root, CVSTag.BRANCH));
		cvsDefinedTagsRootElement.getVersions().add(CVSUIPlugin.getPlugin().getRepositoryManager().getKnownTags(root, CVSTag.VERSION));
		cvsDefinedTagsRootElement.getDates().add(CVSUIPlugin.getPlugin().getRepositoryManager().getKnownTags(root, CVSTag.DATE));
		cvsDefinedTagsTree.setInput(cvsDefinedTagsRootElement);
		cvsDefinedTagsTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablements();
			}
		});
		cvsDefinedTagsTree.setSorter(new ProjectElementSorter());
	
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
					updateShownTags();
					updateEnablements();
				}
			});			
		Button addDatesButton = new Button(buttonComposite, SWT.PUSH);
		addDatesButton.setText(Policy.bind("TagConfigurationDialog.0")); //$NON-NLS-1$
		data = getStandardButtonData(addDatesButton);
		data.horizontalAlignment = GridData.FILL;
		addDatesButton.setLayoutData(data);
		addDatesButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				CVSTag dateTag = NewDateTagAction.getDateTag(getShell(), CVSUIPlugin.getPlugin().getRepositoryManager().getRepositoryLocationFor(root));
				addDateTagsSelected(dateTag);
				updateShownTags();
				updateEnablements();
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
					updateShownTags();
					updateEnablements();
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
					updateShownTags();
					updateEnablements();
				}
			});
		
		if(allowSettingAutoRefreshFiles) {
			Label explanation = new Label(rememberedTags, SWT.WRAP);
			explanation.setText(Policy.bind("TagConfigurationDialog.11")); //$NON-NLS-1$
			data = new GridData ();
			data.horizontalSpan = 2;
			//data.widthHint = 300;
			explanation.setLayoutData(data);
			
			autoRefreshFileList = new org.eclipse.swt.widgets.List(rememberedTags, SWT.BORDER | SWT.MULTI);	 
			data = new GridData ();		
			data.heightHint = 45;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			autoRefreshFileList.setLayoutData(data);		
			try {
				autoRefreshFileList.setItems(CVSUIPlugin.getPlugin().getRepositoryManager().getAutoRefreshFiles(roots[0]));
			} catch (CVSException e) {
				autoRefreshFileList.setItems(new String[0]);
				CVSUIPlugin.log(e);
			}
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
			WorkbenchHelp.setHelp(autoRefreshFileList, IHelpContextIds.TAG_CONFIGURATION_REFRESHLIST);
		}
			
		Label seperator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData (GridData.FILL_BOTH);		
		data.horizontalSpan = 2;
		seperator.setLayoutData(data);
	
		WorkbenchHelp.setHelp(shell, IHelpContextIds.TAG_CONFIGURATION_OVERVIEW);
	
		updateEnablements();
	    Dialog.applyDialogFont(parent);
		return shell;
	}

	private void updateShownTags() {
		final CVSFileElement[] filesSelection = getSelectedFiles();
		final Set tags = new HashSet();
		if(filesSelection.length!=0) {
			try {
				CVSUIPlugin.runWithProgress(getShell(), true /*cancelable*/, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Policy.bind("TagConfigurationDialog.22"), filesSelection.length); //$NON-NLS-1$
						try {
							for (int i = 0; i < filesSelection.length; i++) {
								ICVSFile file = filesSelection[i].getCVSFile();
								tags.addAll(Arrays.asList(getTagsFor(file, Policy.subMonitorFor(monitor, 1))));
							}
						} catch (TeamException e) {
							// ignore the exception
						} finally {
							monitor.done();
						}
					}
				});
			} catch (InterruptedException e) {
				// operation cancelled
			} catch (InvocationTargetException e) {
				// can't happen since we're ignoring all possible exceptions
			}
			cvsTagTree.getTable().removeAll();
			for (Iterator it = tags.iterator(); it.hasNext();) {
				CVSTag tag = (CVSTag) it.next();
				List knownTags = new ArrayList();
				knownTags.addAll(Arrays.asList(cvsDefinedTagsRootElement.getBranches().getTags()));
				knownTags.addAll(Arrays.asList(cvsDefinedTagsRootElement.getVersions().getTags()));
				knownTags.addAll(Arrays.asList(cvsDefinedTagsRootElement.getDates().getTags()));
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
						ICVSFile file = ((CVSFileElement)it.next()).getCVSFile();
						ICVSFolder fileParent = file.getParent();
						String filePath = new Path(null, fileParent.getFolderSyncInfo().getRepository())
							.append(file.getRelativePath(fileParent)).toString();
						if(autoRefreshFileList.indexOf(filePath)==-1) {
							autoRefreshFileList.add(filePath);					
						}
					} catch(CVSException e) {
						CVSUIPlugin.openError(getShell(), null, null, e);
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
			}else if(tag.getType() == CVSTag.DATE){
				cvsDefinedTagsRootElement.getDates().add(new CVSTag[] {tag});
			}else {
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
					} else if(tag.getType() == CVSTag.DATE){
						cvsDefinedTagsRootElement.getDates().remove(tag);
					}
				}
			}
		}
		cvsDefinedTagsTree.refresh();
		cvsDefinedTagsTree.getTree().setFocus();
	}
	private void addDateTagsSelected(CVSTag tag){
		if(tag == null) return;
		List knownTags = new ArrayList();
		knownTags.addAll(Arrays.asList(cvsDefinedTagsRootElement.getDates().getTags()));
		if(!knownTags.contains( tag)){
			cvsDefinedTagsRootElement.getDates().add(tag);
		}
		cvsDefinedTagsTree.refresh();
		cvsDefinedTagsTree.getTree().setFocus();
	}
	private boolean isTagSelectedInKnownTagTree() {
		IStructuredSelection selection = (IStructuredSelection)cvsDefinedTagsTree.getSelection();
		if (!selection.isEmpty()) {
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
		cvsDefinedTagsRootElement.getDates().removeAll();
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
		try {
			// save auto refresh file names
			if(allowSettingAutoRefreshFiles) {
				RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
				manager.setAutoRefreshFiles(root, autoRefreshFileList.getItems());		
			}
			
			// save defined tags and update all project with the same version tags
			final RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();	
			manager.run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					CVSTag[] branches = cvsDefinedTagsRootElement.getBranches().getTags();
					CVSTag[] versions = cvsDefinedTagsRootElement.getVersions().getTags();
					CVSTag[] dates = cvsDefinedTagsRootElement.getDates().getTags();
					try {
						for(int i = 0; i < roots.length; i++) {
							CVSTag[] oldTags = manager.getKnownTags(roots[i]);
							manager.removeTags(roots[i], oldTags);
							if(branches.length > 0) {
								manager.addTags(roots[i], branches);
							}
							if(versions.length>0) {
								manager.addTags(roots[i], versions);
							}
							if(dates.length>0) {
								manager.addTags(roots[i], dates);
							}
						}
					} catch (CVSException e) {
						throw new InvocationTargetException(e);
					}
				}
			}, null);
			
			super.okPressed();
		} catch (CVSException e) {
			CVSUIPlugin.openError(getShell(), null, null, e);
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(getShell(), null, null, e);
		} catch (InterruptedException e) {
		}
	}
	
	/*
	 * Returns a button that implements the standard refresh tags operation. The runnable is run immediatly after 
	 * the tags are fetched from the server. A client should refresh their widgets that show tags because they
	 * may of changed. 
	 */
	private static Button createTagRefreshButton(final Shell shell, Composite composite, String title, final ICVSFolder folder, final Runnable runnable) {
		Button refreshButton = new Button(composite, SWT.PUSH);
		refreshButton.setText (title);
		refreshButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					try {
						PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {
									CVSUIPlugin.getPlugin().getRepositoryManager().refreshDefinedTags(folder, false /* replace */, true, monitor);
									Display.getDefault().asyncExec(runnable);
								} catch (TeamException e) {
									throw new InvocationTargetException(e);
								}
							}
						});
					} catch (InterruptedException e) {
						// operation cancelled
					} catch (InvocationTargetException e) {
						CVSUIPlugin.openError(shell, Policy.bind("TagConfigurationDialog.14"), null, e); //$NON-NLS-1$
					}
				}
			});
		updateEnablementOnRefreshButton(refreshButton, folder);
		return refreshButton;		
	 }
	 
	 public static Control createTagDefinitionButtons(final Shell shell, Composite composite, final ICVSFolder[] folders, int hHint, int wHint, final Runnable afterRefresh, final Runnable afterConfigure) {
	 	Composite buttonComp = new Composite(composite, SWT.NONE);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.END;		
		buttonComp.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonComp.setLayout (layout);
	 	
	 	final Button refreshButton = TagConfigurationDialog.createTagRefreshButton(shell, buttonComp, Policy.bind("TagConfigurationDialog.20"), folders[0], afterRefresh); //$NON-NLS-1$
		data = new GridData();
		if(hHint!=0 && wHint!=0) {
			data.heightHint = hHint;
			//don't crop labels with large font
			//int widthHint = wHint;
			//data.widthHint = Math.max(widthHint, refreshButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		}
		data.horizontalAlignment = GridData.END;
		data.horizontalSpan = 1;
		refreshButton.setLayoutData (data);		

		Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText (Policy.bind("TagConfigurationDialog.21")); //$NON-NLS-1$
		data = new GridData ();
		if(hHint!=0 && wHint!=0) {
			data.heightHint = hHint;
			//don't crop labels with large font
			//int widthHint = wHint;
			//data.widthHint = Math.max(widthHint, addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		}
		data.horizontalAlignment = GridData.END;
		data.horizontalSpan = 1;
		addButton.setLayoutData (data);
		addButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					TagConfigurationDialog d = new TagConfigurationDialog(shell, folders);
					d.open();
					updateEnablementOnRefreshButton(refreshButton, folders[0]);
					afterConfigure.run();
				}
			});		
		
		WorkbenchHelp.setHelp(refreshButton, IHelpContextIds.TAG_CONFIGURATION_REFRESHACTION);
		WorkbenchHelp.setHelp(addButton, IHelpContextIds.TAG_CONFIGURATION_OVERVIEW);
		
		Dialog.applyDialogFont(buttonComp);
		
		return buttonComp;
	 }
	 
	 private static void updateEnablementOnRefreshButton(Button refreshButton, ICVSFolder project) {
	 	try {
			String[] files = CVSUIPlugin.getPlugin().getRepositoryManager().getAutoRefreshFiles(project);
			refreshButton.setEnabled(files.length != 0);
		} catch (CVSException e) {
			refreshButton.setEnabled(false);
			CVSUIPlugin.log(e);
		}
 		
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
				return super.getInitialSize();
			}
		} else {
			try {
				height = settings.getInt(NOREFRESH_HEIGHT_KEY);
				width = settings.getInt(NOREFRESH_WIDTH_KEY);
			} catch(NumberFormatException e) {
				return super.getInitialSize();
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
        //don't crop labels with large font
		//int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		//data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
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
