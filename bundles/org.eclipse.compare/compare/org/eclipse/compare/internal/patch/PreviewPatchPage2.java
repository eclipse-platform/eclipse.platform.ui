package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.ICompareUIConstants;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardPage;
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


public class PreviewPatchPage2 extends WizardPage {

	protected final static String PREVIEWPATCHPAGE_NAME= "PreviewPatchPage";  //$NON-NLS-1$
	private PatchWizard fPatchWizard;
	private PreviewPatchPageInput patcherCompareEditorInput = new PreviewPatchPageInput();
	
	private Combo fStripPrefixSegments;
	private Text fFuzzField;
	
	private Action fRetargetSelection;
	private static final String retargetID = "PreviewPatchPage_retargetSelection"; //$NON-NLS-1$
	
	private Action fIgnoreWhiteSpace;
	private static final String ignoreWSID = "PreviewPatchPage_ignoreWhiteSpace"; //$NON-NLS-1$
	
	private Action fReversePatch;
	private static final String reversePatchID = "PreviewPatchPage_reversePatch"; //$NON-NLS-1$
	
	private static final int DIFF_NODE = 0;
	private static final int PROJECTDIFF_NODE = 1;
	
	protected boolean pageRecalculate= true;
 
	class RetargetPatchDialog extends Dialog {

		protected TreeViewer rpTreeViewer;
		protected DiffNode rpSelectedNode;
		protected DiffProject rpSelectedProject;
		protected IProject rpTargetProject;

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

			rpTreeViewer.setContentProvider(new RetargetPatchContentProvider());
			rpTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
			rpTreeViewer.setSorter(new ResourceSorter(ResourceSorter.NAME));
			rpTreeViewer.setInput(ResourcesPlugin.getWorkspace());
			ITypedElement tempProject = rpSelectedNode.getLeft();
			if (tempProject instanceof DiffProject){
				rpSelectedProject = (DiffProject)tempProject;
				rpTreeViewer.setSelection(new StructuredSelection(rpSelectedProject.getProject()));
			}
			
			setupListeners();

			Dialog.applyDialogFont(composite);
			
			return parent;
		}

		protected void okPressed() {
			rpSelectedProject.setProject(rpTargetProject);
			super.okPressed();
		}

		void setupListeners() {
			rpTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection s= (IStructuredSelection) event.getSelection();
					Object obj= s.getFirstElement();
					if (obj instanceof IProject)
						rpTargetProject= (IProject) obj;
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

			if (element instanceof IProject) {
				return new Object[0];
			}
			return super.getChildren(element);
		}
	}

	public PreviewPatchPage2(PatchWizard pw) {
		super(PREVIEWPATCHPAGE_NAME, PatchMessages.PreviewPatchPage_title, null);
		
		fPatchWizard= pw;		
	}
		
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
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
				
				patcherCompareEditorInput.setContributedActionEnablement(retargetID, false);
				if (fPatchWizard.getPatcher().isWorkspacePatch() && obj instanceof DiffNode) {
					//check to see that the selected element is a Diff Project
					ITypedElement element = ((DiffNode) obj).getLeft();
					if (element != null && element instanceof DiffProject){
						patcherCompareEditorInput.setContributedActionEnablement(retargetID, true);
					}
				}

			}

		});
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
		
	}
	
	private Action[] getContributedActions() {
		fRetargetSelection= new Action(PatchMessages.PreviewPatchPage2_RetargetAction, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.RETARGET_PROJECT)) {
			public void run() {
				Shell shell = getShell();
				ISelection selection = patcherCompareEditorInput.getViewer().getSelection();
				final RetargetPatchDialog dialog= new RetargetPatchDialog(shell, selection);
				dialog.open();
				patcherCompareEditorInput.updateInput(fPatchWizard.getPatcher());
			}
		};
		fRetargetSelection.setToolTipText(PatchMessages.PreviewPatchPage2_RetargetTooltip);
		fRetargetSelection.setEnabled(false);
		fRetargetSelection.setId(retargetID);
		
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
		
		return new Action[]{fIgnoreWhiteSpace, fRetargetSelection, fReversePatch};
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
	
	public IWizardPage getNextPage() {
		
		/*//check to see if this page is complete
		if (!isPageComplete())
			return null;*/
		
		//set the contents of the hunk merge page
		
		HunkMergePage hunkMergePage = (HunkMergePage) fPatchWizard.getPage(HunkMergePage.HUNKMERGEPAGE_NAME);
        IDiffElement[] children = patcherCompareEditorInput.getRoot().getChildren();
 
		//Go through the children and build a new tree. Each Diff should contain all of the 
        //hunks that have already been selected (and can be successfully applied) and the hunks
        //that cannot be applied. If there are no hunks that can't be applied, skip the Hunk Merge
        //page and proceed directly to the preview page.
        boolean atLeastOneDiffProblem = false;
        
        if (children != null){
        	
            DiffNode newRoot = new DiffNode(Differencer.NO_CHANGE) {
    			public boolean hasChildren() {
    				return true;
    			}
    		};
    		
        	for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof PatcherDiffNode){
					PatcherDiffNode patcherDiffNode = (PatcherDiffNode) children[i];
					if (patcherDiffNode.getDiff().containsProblems()){
						atLeastOneDiffProblem = true;
						processDiff(patcherDiffNode, newRoot, hunkMergePage,DIFF_NODE);
					}
				} else if (children[i] instanceof DiffNode){
					//project node
					
					//Get diff nodes
					IDiffElement[] diffs = ((DiffNode)children[i]).getChildren();
					
					for (int j = 0; j < diffs.length; j++) {
						if (diffs[j] instanceof PatcherDiffNode){
							PatcherDiffNode diff = (PatcherDiffNode) diffs[j];
							Diff tempDiff = diff.getDiff();
							if (tempDiff.containsProblems()){
								atLeastOneDiffProblem = true;
								DiffNode projectNode = new DiffNode(newRoot, Differencer.CHANGE, null, children[i], null);
								processDiff(diff, projectNode, hunkMergePage,PROJECTDIFF_NODE);
							}
						}
					}
				}
			}
        	
            hunkMergePage.setRoot(newRoot); 
        }
		
        if (atLeastOneDiffProblem){
        	return super.getNextPage();
        }
        
        return null;
	}

	private void processDiff(PatcherDiffNode diff, DiffNode rootNode, HunkMergePage hunkMergePage, int nodeType) {
		//Get the diffs from the project
		IDiffElement[] hunks = diff.getChildren();
		if (hunks != null) {
			//Construct a diff node that contains all of the selected hunks that can be successfully applied
			//and make a temp patch of the file - all of the failed hunks should appear as children below it 
			Diff tempDiff = diff.getDiff();
			Diff tempNewDiff = new Diff(tempDiff.fOldPath, tempDiff.fOldDate, tempDiff.fNewPath, tempDiff.fNewDate);

			ArrayList failedHunks = new ArrayList();
			ArrayList successfulHunks = new ArrayList();

			for (int i = 0; i < hunks.length; i++) {
				if (hunks[i] instanceof PatcherDiffNode) {
					PatcherDiffNode hunkNode = (PatcherDiffNode) hunks[i];
					Hunk tempHunk = hunkNode.getHunk();
					if (tempHunk.getHunkProblem()) {
						failedHunks.add(hunkNode);
					} else {
						successfulHunks.add(hunkNode);
						tempNewDiff.add(tempHunk);
					}
				}
			}
			try {
				IPath filePath = new Path(tempDiff.getLabel(tempDiff));
				IFile tempFile = null;
				if (nodeType == PROJECTDIFF_NODE){
					tempFile = tempDiff.getProject().getFile(filePath);
				} else if (nodeType == DIFF_NODE){
					tempFile = tempDiff.getTargetFile();
				}
				byte[] bytes = patcherCompareEditorInput.quickPatch(tempFile, this.fPatchWizard.getPatcher(), tempNewDiff);

				ITypedElement tempNode;
				PatchedFileNode patchedNode;

				if (tempFile != null && tempFile.exists()) {
					tempNode = new ResourceNode(tempFile);
					patchedNode = new PatchedFileNode(bytes, tempNode.getType()/*"MANUALHUNKMERGE"*/, tempFile.getProjectRelativePath().toString(), true);
				} else {
					tempNode = new PatchedFileNode(new byte[0], filePath.getFileExtension(), PatchMessages.PatcherCompareEditorInput_FileNotFound);
					patchedNode = new PatchedFileNode(bytes, tempNode.getType(), ""); //$NON-NLS-1$
				}

				//Set the patched file in the Hunk Merge for later retrieval
				hunkMergePage.setMergedFile(tempDiff, patchedNode);
				
				DiffNode allFile = new DiffNode(rootNode, Differencer.CHANGE, null, tempDiff, null);
				
				//hang all of the failed nodes off of the new patched diff node
				for (Iterator iterator = failedHunks.iterator(); iterator.hasNext();) {
					PatcherDiffNode object = (PatcherDiffNode) iterator.next();
					String strippedHunk= stripContextFromHunk(object.getHunk());
					PatchedFileNode strippedHunkNode = new PatchedFileNode(strippedHunk.getBytes(),object.getRight().getType()/*"manualHunkMerge"*/, object.getRight().getName());
					PatchedFileWrapper patchedFileWrapper = new PatchedFileWrapper(patchedNode);
					PatcherDiffNode parentNode = new PatcherDiffNode(allFile, Differencer.CHANGE, null, patchedFileWrapper,strippedHunkNode, object.getHunk());
					patchedFileWrapper.addContentChangeListener(hunkMergePage);
					patchedFileWrapper.setParent(parentNode);
				}
			} catch (CoreException ex) {
				//ignore 
			}

		}
	}

	private String stripContextFromHunk(Hunk hunk) {
		String[] hunkLines = hunk.getLines();
		StringBuffer result= new StringBuffer();
		for (int i= 0; i<hunkLines.length; i++) {
			String line= hunkLines[i];
			String rest= line.substring(1);
			switch (line.charAt(0)) {
				case ' ' :
					//skip the context
					break;
				case '-' :
					result.append(rest);
					break;
				case '+' :
					result.append(rest);
					break;
			}
		}
		
		return result.toString();
	}
	
	public boolean canFlipToNextPage() {
		if (patcherCompareEditorInput.containsHunkErrors()){
			return true;
		}
		
		return false;
	}

}
