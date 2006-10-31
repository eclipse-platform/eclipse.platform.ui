package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.ICompareUIConstants;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

import com.ibm.icu.text.MessageFormat;


public class PreviewPatchPage2 extends WizardPage implements IContentChangeListener {

	protected final static String PREVIEWPATCHPAGE_NAME= "PreviewPatchPage";  //$NON-NLS-1$
	private PatchWizard fPatchWizard;
	private PreviewPatchPageInput patcherCompareEditorInput;
	private CompareConfiguration previewPatchPageConfiguration;
	
	private Combo fStripPrefixSegments;
	private Text fFuzzField;
	
	private Action fIncludeToggle;
	private static final String toggleInclusionID = "PreviewPatchPage_toggleInclusion"; //$NON-NLS-1$
	
	private Action fIgnoreWhiteSpace;
	private static final String ignoreWSID = "PreviewPatchPage_ignoreWhiteSpace"; //$NON-NLS-1$
	
	private Action fReversePatch;
	private static final String reversePatchID = "PreviewPatchPage_reversePatch"; //$NON-NLS-1$
	
	private Action fRetargetResource;
	private static final String  retargetDiffID = "PreviewPatchPage_retargetDiffIDSelection"; //$NON-NLS-1$
	
	private static final int DIFF_NODE = 0;
	private static final int PROJECTDIFF_NODE = 1;
	
	protected boolean pageRecalculate= true;
	//tracks which diffs actually get edited
	private HashSet alteredDiffs = new HashSet();

	// maps diffs to merged file contents
	private HashMap alteredFiles = new HashMap();
	
	
	class RetargetPatchDialog extends Dialog {

		protected TreeViewer rpTreeViewer;
		protected DiffNode rpSelectedNode;
		protected DiffProject rpSelectedProject;
		protected IProject rpTargetProject;
		private IFile rpTargetResource;
		
		protected int mode;
		public RetargetPatchDialog(Shell shell, ISelection selection) {
			super(shell);
			setShellStyle(getShellStyle()|SWT.RESIZE);
			if (selection instanceof IStructuredSelection) {
				rpSelectedNode= (DiffNode) ((IStructuredSelection) selection).getFirstElement();
			}
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite= (Composite) super.createDialogArea(parent);

			initializeDialogUnits(parent);

			getShell().setText(PatchMessages.PreviewPatchPage_RetargetPatch);

			GridLayout layout= new GridLayout();
			layout.numColumns= 1;
	        layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	        layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);			
			composite.setLayout(layout);
			final GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
			composite.setLayoutData(data);

			//add controls to composite as necessary
			Label label= new Label(composite, SWT.LEFT|SWT.WRAP);
			label.setText(NLS.bind(PatchMessages.PreviewPatchPage_SelectProject, rpSelectedNode.getName()));
			final GridData data2= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			label.setLayoutData(data2);

			rpTreeViewer= new TreeViewer(composite, SWT.BORDER);
			GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.widthHint= 0;
			gd.heightHint= 0;
			rpTreeViewer.getTree().setLayoutData(gd);

			//determine what type of selection this is, project or diff/hunk
			if (rpSelectedNode instanceof PatcherDiffNode){
				//either a diff or a hunk
				mode = DIFF_NODE;
				switch(((PatcherDiffNode) rpSelectedNode).getPatchNodeType()){
				
				case PatcherDiffNode.HUNK:
					Diff diff = (Diff) ((PatcherDiffNode) rpSelectedNode).getHunk().getParent(rpSelectedNode);
					//set the selection to the diff resource
					rpTreeViewer.setSelection(new StructuredSelection(diff.getTargetFile()));
					break;
					
				case PatcherDiffNode.DIFF:
					diff = ((PatcherDiffNode) rpSelectedNode).getDiff();
					//set the selection to the diff resource
					rpTreeViewer.setSelection(new StructuredSelection(diff.getTargetFile()));
					break;
					
				case PatcherDiffNode.PROJECT:
					rpSelectedProject = ((PatcherDiffNode) rpSelectedNode).getDiffProject();
					rpTreeViewer.setSelection(new StructuredSelection(rpSelectedProject.getProject()));
					mode = PROJECTDIFF_NODE;
					break;
				}
			}
			
			rpTreeViewer.setContentProvider(new RetargetPatchContentProvider(mode));
			rpTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
			rpTreeViewer.setSorter(new ResourceSorter(ResourceSorter.NAME));
			rpTreeViewer.setInput(ResourcesPlugin.getWorkspace());
			
			
			setupListeners();

			Dialog.applyDialogFont(composite);
			
			return parent;
		}

		protected void okPressed() {
			if (rpSelectedNode != null && rpSelectedNode instanceof PatcherDiffNode && rpTargetResource != null){
					PatcherDiffNode patchedNode =(PatcherDiffNode) rpSelectedNode;
					switch(patchedNode.getPatchNodeType()){
					
					case PatcherDiffNode.PROJECT:
						//copy over all diffs to new target resource
						
						break;
						
					case PatcherDiffNode.DIFF:
						//copy over all hunks to new target resource
						Diff tempDiff = patchedNode.getDiff();
						Hunk[] tempHunks = tempDiff.getHunks();
						for (int i = 0; i < tempHunks.length; i++) {
							tempDiff.remove(tempHunks[i]);
						}
						
						if (fPatchWizard.getPatcher().isWorkspacePatch()){
							//since the diff has no more hunks to apply, remove it from the parent and the patcher
							tempDiff.getProject().remove(tempDiff);
							fPatchWizard.getPatcher().removeDiff(tempDiff);
						} else {
							//since the diff has no more hunks to apply, remove it from the patcher
							fPatchWizard.getPatcher().removeDiff(tempDiff);
						}
						
						patcherCompareEditorInput.addHunksToFile(rpTargetResource, tempHunks);
						break;
						
					case PatcherDiffNode.HUNK:
						//copy hunk over to new target resource
						tempDiff = (Diff) patchedNode.getHunk().getParent(patchedNode);
						tempDiff.remove(patchedNode.getHunk());
						patcherCompareEditorInput.addHunksToFile(rpTargetResource, new Hunk[]{patchedNode.getHunk()});
						break;
					}
					
	
				//Set the project if needed
				if (rpSelectedProject != null)
					rpSelectedProject.setProject(rpTargetProject);
			}
			super.okPressed();	
		}

		void setupListeners() {
			rpTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			

				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection s= (IStructuredSelection) event.getSelection();
					Object obj= s.getFirstElement();
					if (obj instanceof IProject)
						rpTargetProject= (IProject) obj;
					else if (obj instanceof IFile)
						rpTargetResource = (IFile) obj;
				}
			});

			rpTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					ISelection s= event.getSelection();
					if (s instanceof IStructuredSelection) {
						Object item= ((IStructuredSelection) s).getFirstElement();
						if (rpTreeViewer.getExpandedState(item))
							rpTreeViewer.collapseToLevel(item, 1);
						else
							rpTreeViewer.expandToLevel(item, 1);
					}
				}
			});

		}

		protected Point getInitialSize() {
			final Point size= super.getInitialSize();
			size.x= convertWidthInCharsToPixels(75);
			size.y+= convertHeightInCharsToPixels(20);
			return size;
		}
	}

	class RetargetPatchContentProvider extends BaseWorkbenchContentProvider {
		//Never show closed projects
		boolean showClosedProjects= false;
		//Used to limit providers to just projects for retargeting projects
		int mode;
		public RetargetPatchContentProvider(int mode) {
			 this.mode = mode;
		}

		public Object[] getChildren(Object element) {
			if (element instanceof IWorkspace) {
				// check if closed projects should be shown
				IProject[] allProjects= ((IWorkspace) element).getRoot().getProjects();
				if (showClosedProjects)
					return allProjects;

				ArrayList accessibleProjects= new ArrayList();
				for (int i= 0; i<allProjects.length; i++) {
					if (allProjects[i].isOpen()) {
						accessibleProjects.add(allProjects[i]);
					}
				}
				return accessibleProjects.toArray();
			}

			if (element instanceof IProject && mode==PROJECTDIFF_NODE) {
				return new Object[0];
			}
			return super.getChildren(element);
		}
	}

	public PreviewPatchPage2(PatchWizard pw) {
		super(PREVIEWPATCHPAGE_NAME, PatchMessages.PreviewPatchPage_title, null);
		
		fPatchWizard= pw;		
	}
		
	public PreviewPatchPage2(PatchWizard wizard, CompareConfiguration previewPatchPageConfiguration) {
		this(wizard);
		this.previewPatchPageConfiguration = previewPatchPageConfiguration;
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//if a compare configuration has been passed make sure 
		if (previewPatchPageConfiguration != null){
			patcherCompareEditorInput = new PreviewPatchPageInput(previewPatchPageConfiguration);
		} else {
			patcherCompareEditorInput = new PreviewPatchPageInput();
		}
			
		initializeDialogUnits(parent);
		
		buildPatchOptionsGroup(composite);
		
		try {
			patcherCompareEditorInput.run(null);
		} catch (InterruptedException e) {//ignore
		} catch (InvocationTargetException e) {//ignore
		}
	
		
		Control c = patcherCompareEditorInput.createContents(composite);
		patcherCompareEditorInput.contributeDiffViewerToolbarItems(getContributedActions(), fPatchWizard.getPatcher().isWorkspacePatch());
		patcherCompareEditorInput.setPreviewPatchPage(this);
		patcherCompareEditorInput.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel= (IStructuredSelection) event.getSelection();
				Object obj= sel.getFirstElement();
				
				if (obj instanceof PatcherDiffNode){
					//either a diff or a hunk
					Diff diff = ((PatcherDiffNode) obj).getDiff();
					if (diff != null){
						patcherCompareEditorInput.setContributedActionName(retargetDiffID, PatchMessages.PreviewPatchPage2_RetargetDiff);
					} else {
						patcherCompareEditorInput.setContributedActionName(retargetDiffID, PatchMessages.PreviewPatchPage2_RetargetHunk);
					}
				} else if (obj instanceof DiffNode) {
					//either a toplevel Project or a Diff
					ITypedElement tempNode = ((DiffNode) obj).getLeft();
					if (tempNode instanceof DiffProject){
						patcherCompareEditorInput.setContributedActionName(retargetDiffID, PatchMessages.PreviewPatchPage2_RetargetProject);
					} else if (tempNode instanceof Diff){
						patcherCompareEditorInput.setContributedActionName(retargetDiffID, PatchMessages.PreviewPatchPage2_RetargetDiff);
					}				
				}
			}

		});
		
		patcherCompareEditorInput.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel= (IStructuredSelection) event.getSelection();
				Object obj= sel.getFirstElement();
				
				//make sure anu unsaved changes are saved before switching editablity
				ensureContentsSaved();
				
				if (obj instanceof PatcherDiffNode){
					switch (((PatcherDiffNode)obj).getPatchNodeType()){
						case PatcherDiffNode.HUNK:
							patcherCompareEditorInput.getCompareConfiguration().setLeftEditable(true);
							patcherCompareEditorInput.getCompareConfiguration().setLeftLabel(PatchMessages.PreviewPatchPage2_PatchedLocalFile);
							patcherCompareEditorInput.getCompareConfiguration().setRightLabel(PatchMessages.PreviewPatchPage2_OrphanedHunk);
							break;
						
						default:
							patcherCompareEditorInput.getCompareConfiguration().setLeftEditable(false);
							break;
					}
				}
			}

		});
		patcherCompareEditorInput.contributeDiffViewerMenuItems(getMenuActions());

		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
		
	}
	
	private Action[] getContributedActions() {
		fIncludeToggle= new Action(PatchMessages.PreviewPatchPage2_IncludeElement, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.RETARGET_PROJECT)) {
			public void run() {
				ISelection selection = patcherCompareEditorInput.getViewer().getSelection();
				if (selection instanceof TreeSelection){
					Object obj = ((TreeSelection) selection).getFirstElement();
					if (obj instanceof PatcherDiffNode){
						PatcherDiffNode node = ((PatcherDiffNode) obj);
						node.setIncludeElement(!node.getIncludeElement());
					}
				}
				patcherCompareEditorInput.getViewer().refresh();
			}
		};
		fIncludeToggle.setToolTipText(PatchMessages.PreviewPatchPage2_IncludeElementText);
		fIncludeToggle.setEnabled(true);
		fIncludeToggle.setId(toggleInclusionID);
		
		fIgnoreWhiteSpace = new Action(PatchMessages.PreviewPatchPage2_IgnoreWSAction, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.IGNORE_WHITESPACE_ENABLED)){
			public void run(){
				fIgnoreWhiteSpace.setChecked(isChecked());
				if (fPatchWizard.getPatcher().setIgnoreWhitespace(fIgnoreWhiteSpace.isChecked())){
					fillTree();
				}
			}
		};
		fIgnoreWhiteSpace.setChecked(false);
		fIgnoreWhiteSpace.setToolTipText(PatchMessages.PreviewPatchPage2_IgnoreWSTooltip);
		fIgnoreWhiteSpace.setDisabledImageDescriptor(CompareUIPlugin.getImageDescriptor(ICompareUIConstants.IGNORE_WHITESPACE_DISABLED));
		fIgnoreWhiteSpace.setId(ignoreWSID);
		
		fReversePatch = new Action(PatchMessages.PreviewPatchPage_ReversePatch_text, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.REVERSE_PATCH_ENABLED)){
			public void run(){
				fReversePatch.setChecked(isChecked());
				if (fPatchWizard.getPatcher().setReversed(isChecked())){
					fillTree();
				}
			}
		};
		fReversePatch.setChecked(false);
		fReversePatch.setToolTipText(PatchMessages.PreviewPatchPage_ReversePatch_text);
		fReversePatch.setId(reversePatchID);
		
		return new Action[]{fIncludeToggle, fIgnoreWhiteSpace, fReversePatch};
	}

		private Action[] getMenuActions() {
			fRetargetResource = new Action(PatchMessages.PreviewPatchPage2_RetargetDiff, CompareUIPlugin
				.getImageDescriptor(ICompareUIConstants.RETARGET_PROJECT)) {
			public void run() {
				Shell shell = getShell();
				ISelection selection = patcherCompareEditorInput.getViewer()
						.getSelection();
				final RetargetPatchDialog dialog = new RetargetPatchDialog(shell, selection);
				dialog.open();
				patcherCompareEditorInput.updateInput(fPatchWizard.getPatcher());
				patcherCompareEditorInput.getViewer().refresh();
			}
		};
		fRetargetResource
				.setToolTipText(PatchMessages.PreviewPatchPage2_RetargetTooltip);
		fRetargetResource.setEnabled(true);
		fRetargetResource.setId(retargetDiffID);

		return new Action[] { fIncludeToggle, fRetargetResource };
	}
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		//Need to handle input and rebuild tree only when becoming visible
		if(visible){
			fillTree();
		}
	}
	
	private void fillTree(){
		//Update prefix count - go through all of the diffs and find the smallest
		//path segment contained in all diffs.
		int length= 99;
		if (fStripPrefixSegments!=null&& pageRecalculate) {
			length= fPatchWizard.getPatcher().calculatePrefixSegmentCount();
			if (length!=99) {
				for (int k= 1; k<length; k++)
					fStripPrefixSegments.add(Integer.toString(k));
				pageRecalculate= false;
			}
		}
		
		patcherCompareEditorInput.updateInput(fPatchWizard.getPatcher());	
	}
	/*
	 *	Create the group for setting various patch options
	 */
	private void buildPatchOptionsGroup(Composite parent) {
		
		GridLayout gl;
		GridData gd;
		Label l;

		final WorkspacePatcher patcher= fPatchWizard.getPatcher();

		Group group= new Group(parent, SWT.NONE);
		group.setText(PatchMessages.PreviewPatchPage_PatchOptions_title);
		gl= new GridLayout(); gl.numColumns= 4;
		group.setLayout(gl);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL));

		// 1st row

		Composite pair= new Composite(group, SWT.NONE);
		gl= new GridLayout(); gl.numColumns= 2; gl.marginHeight= gl.marginWidth= 0;
		pair.setLayout(gl);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		pair.setLayoutData(gd);

		l= new Label(pair, SWT.NONE);
		l.setText(PatchMessages.PreviewPatchPage_IgnoreSegments_text);
		gd= new GridData(GridData.VERTICAL_ALIGN_CENTER|GridData.HORIZONTAL_ALIGN_BEGINNING|GridData.GRAB_HORIZONTAL);
		l.setLayoutData(gd);

		fStripPrefixSegments= new Combo(pair, SWT.DROP_DOWN|SWT.READ_ONLY|SWT.SIMPLE);
		int prefixCnt= patcher.getStripPrefixSegments();
		String prefix= Integer.toString(prefixCnt);
		fStripPrefixSegments.add(prefix);
		fStripPrefixSegments.setText(prefix);
		gd= new GridData(GridData.VERTICAL_ALIGN_CENTER|GridData.HORIZONTAL_ALIGN_END);
		fStripPrefixSegments.setLayoutData(gd);

		addSpacer(group);
		
		//
		final Button generateRejects = new Button(pair, SWT.CHECK);
		generateRejects.setText(PatchMessages.HunkMergePage_GenerateRejectFile);
		gd = new GridData(GridData.VERTICAL_ALIGN_CENTER
				| GridData.HORIZONTAL_ALIGN_BEGINNING
				| GridData.GRAB_HORIZONTAL);
		generateRejects.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fPatchWizard.getPatcher().setGenerateRejects(
						generateRejects.getSelection());
			}
		});
		generateRejects.setSelection(true);
		generateRejects.setLayoutData(gd);
		//
		
		addSpacer(group);
		// 2nd row
		pair= new Composite(group, SWT.NONE);
		gl= new GridLayout(); gl.numColumns= 3; gl.marginHeight= gl.marginWidth= 0;
		pair.setLayout(gl);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		pair.setLayoutData(gd);

		l= new Label(pair, SWT.NONE);
		l.setText(PatchMessages.PreviewPatchPage_FuzzFactor_text);
		l.setToolTipText(PatchMessages.PreviewPatchPage_FuzzFactor_tooltip);
		gd= new GridData(GridData.VERTICAL_ALIGN_CENTER|GridData.HORIZONTAL_ALIGN_BEGINNING|GridData.GRAB_HORIZONTAL);
		l.setLayoutData(gd);

		fFuzzField= new Text(pair, SWT.BORDER);
		fFuzzField.setText("2"); //$NON-NLS-1$
			gd= new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_END); gd.widthHint= 30;
		fFuzzField.setLayoutData(gd);

		Button b= new Button(pair, SWT.PUSH);
		b.setText(PatchMessages.PreviewPatchPage_GuessFuzz_text);
			b.addSelectionListener(
				new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int fuzz= guessFuzzFactor(patcher);
				if (fuzz>=0)
					fFuzzField.setText(Integer.toString(fuzz));
			}
				}
			);
		gd= new GridData(GridData.VERTICAL_ALIGN_CENTER);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = b.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		gd.widthHint = Math.max(widthHint, minSize.x);		
		b.setLayoutData(gd);
		
		// register listeners

		if (fStripPrefixSegments!=null)
			fStripPrefixSegments.addSelectionListener(
				new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (patcher.setStripPrefixSegments(getStripPrefixSegments()))
						patcherCompareEditorInput.updateInput(patcher);
				}
				}
			);
	

		fFuzzField.addModifyListener(
			new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (patcher.setFuzz(getFuzzFactor()))
					patcherCompareEditorInput.updateInput(patcher);
			}
		});
	}
	
	private void addSpacer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= 10;
		label.setLayoutData(gd);
	}
	
	public int getFuzzFactor() {
		int fuzzFactor= 0;
		if (fFuzzField!=null) {
			String s= fFuzzField.getText();
			try {
				fuzzFactor= Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				// silently ignored
			}
		}
		return fuzzFactor;
	}
	
	public int getStripPrefixSegments() {
		int stripPrefixSegments= 0;
		if (fStripPrefixSegments!=null) {
			String s= fStripPrefixSegments.getText();
			try {
				stripPrefixSegments= Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				// silently ignored
			}
		}
		return stripPrefixSegments;
	}
	
	private int guessFuzzFactor(final WorkspacePatcher patcher) {
		final int strip= getStripPrefixSegments();
		final int[] result= new int[1];
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true,
			//TimeoutContext.run(true, GUESS_TIMEOUT, getControl().getShell(),
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							result[0]= guess(patcher, monitor, strip);
						}
				}
			);
			return result[0];
		} catch (InvocationTargetException ex) {
			// NeedWork
		} catch (InterruptedException ex) {
			// NeedWork
		}
		return -1;
	}
	
	private int guess(WorkspacePatcher patcher, IProgressMonitor pm, int strip) {

		Diff[] diffs= patcher.getDiffs();
		if (diffs==null||diffs.length<=0)
			return -1;

		// now collect files and determine "work"
		IFile[] files= new IFile[diffs.length];
		int work= 0;
		for (int i= 0; i<diffs.length; i++) {
			Diff diff= diffs[i];
			if (diff==null)
				continue;
			if (diff.getDiffType()!=Differencer.ADDITION) {
				IPath p= diff.fOldPath;
				if (strip>0&&strip<p.segmentCount())
					p= p.removeFirstSegments(strip);
				IFile file= existsInSelection(p);
				if (file!=null) {
					files[i]= file;
					work+= diff.fHunks.size();
				}
			}
		}

		// do the "work"
		int[] fuzzRef= new int[1];
		String format= PatchMessages.PreviewPatchPage_GuessFuzzProgress_format;
		pm.beginTask(PatchMessages.PreviewPatchPage_GuessFuzzProgress_text, work);
		try {
			int fuzz= 0;
			for (int i= 0; i<diffs.length; i++) {
				Diff d= diffs[i];
				IFile file= files[i];
				if (d!=null&&file!=null) {
					List lines= patcher.load(file, false);
					String name= d.getPath().lastSegment();
					Iterator iter= d.fHunks.iterator();
					int shift= 0;
					for (int hcnt= 1; iter.hasNext(); hcnt++) {
						pm.subTask(MessageFormat.format(format, new String[] {name, Integer.toString(hcnt)}));
						Hunk h= (Hunk) iter.next();
						shift= patcher.calculateFuzz(h, lines, shift, pm, fuzzRef);
						int f= fuzzRef[0];
						if (f==-1) // cancel
							return -1;
						if (f>fuzz)
							fuzz= f;
						pm.worked(1);
					}
				}
			}
			return fuzz;
		} finally {
			pm.done();
		}
	}
	
	private IFile existsInSelection(IPath path) {
		return fPatchWizard.getPatcher().existsInTarget(path);
	}

	public void contentChanged(IContentChangeNotifier source) {
		if (source instanceof PatchedFileWrapper) {
			PatcherDiffNode parentNode = ((PatchedFileWrapper) source).getParent();
			
			//the only content that can be altered are hunks - make sure this is true
			Assert.isTrue(parentNode.getPatchNodeType() == PatcherDiffNode.HUNK);
			
			//since it has been changed, it is now included in the patch by default
			parentNode.setIncludeElement(true);
			
			//update the hunk node name
			String name = parentNode.getName();
			int index = name
					.lastIndexOf(PatchMessages.PreviewPatchPage_NoMatch_error);
			if (index != -1) {
				parentNode.setName(NLS.bind(PatchMessages.Diff_2Args, new String[] {name.substring(0, index), PatchMessages.HunkMergePage_Merged}));
			} else {
				//make sure we add one merged to the label
				index = name.lastIndexOf(PatchMessages.HunkMergePage_Merged);
				if (index == -1) 
					parentNode.setName(NLS.bind(PatchMessages.Diff_2Args, new String[] {name, PatchMessages.HunkMergePage_Merged}));
			}
			
			//update the diff node name 
			PatcherDiffNode diffNode= (PatcherDiffNode) parentNode.getParent();
			Assert.isTrue(diffNode.getPatchNodeType() == PatcherDiffNode.DIFF);
			index = diffNode.getName().lastIndexOf(PatchMessages.HunkMergePage_Merged);
			if (index == -1)
				diffNode.setName(NLS.bind(PatchMessages.Diff_2Args, new String[] {diffNode.getName(), PatchMessages.HunkMergePage_Merged}));
	
			patcherCompareEditorInput.getViewer().update(diffNode, null);
			patcherCompareEditorInput.getViewer().refresh();
			patcherCompareEditorInput.updateTree();
			
			Hunk tempHunk = parentNode.getHunk();
			Assert.isNotNull(tempHunk);
			Diff tempDiff = (Diff) tempHunk.getParent(tempHunk);
			Assert.isNotNull(tempDiff);
			alteredDiffs.add(tempDiff);

			alteredFiles.put(tempDiff, ((PatchedFileWrapper) source).getPatchedFilNode());
			
			// now that one hunk has been changed this page can be considered complete
			setPageComplete(true);
		}
	}
	
	public HashMap getMergedFileContents() {
		return alteredFiles;
	}

	public void setMergedFile(Diff tempDiff, PatchedFileNode node) {
		alteredFiles.put(tempDiff, node);
	}

	public HashSet getModifiedDiffs() {
		return alteredDiffs;
	}

	public void setAlteredDiff(Diff tempDiff){
		alteredDiffs.add(tempDiff);
	}
	
	public void ensureContentsSaved() {
		try {
			patcherCompareEditorInput.saveChanges(new NullProgressMonitor());
		} catch (CoreException e) {
			//ignore
		}
	}
	

}
