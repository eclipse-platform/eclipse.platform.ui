package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.ICompareUIConstants;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;


public class PreviewPatchPage2 extends WizardPage {

	protected final static String PREVIEWPATCHPAGE_NAME= "PreviewPatchPage";  //$NON-NLS-1$
	
	private final WorkspacePatcher fPatcher;
	private final CompareConfiguration fConfiguration;
	private PreviewPatchPageInput fInput;
	
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
	
	class RetargetPatchDialog extends Dialog {

		private TreeViewer rpTreeViewer;
		private DiffNode rpSelectedNode;
		private DiffProject rpSelectedProject;
		private IResource rpTargetResource;
		private int mode;
		
		public RetargetPatchDialog(Shell shell, ISelection selection) {
			super(shell);
			setShellStyle(getShellStyle()|SWT.RESIZE);
			if (selection instanceof IStructuredSelection) {
				rpSelectedNode= (DiffNode) ((IStructuredSelection) selection).getFirstElement();
			}
		}

		protected Control createButtonBar(Composite parent) {
			Control control = super.createButtonBar(parent);
			Button okButton = this.getButton(IDialogConstants.OK_ID);
			okButton.setEnabled(false);
			return control;
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
			if (rpSelectedNode instanceof PatchFileDiffNode) {
				PatchFileDiffNode node = (PatchFileDiffNode) rpSelectedNode;
				rpTreeViewer.setSelection(new StructuredSelection(getPatcher().getTargetFile(node.getDiffResult().getDiff())));
				mode = DIFF_NODE;
			} else if (rpSelectedNode instanceof HunkDiffNode) {
				HunkDiffNode node = (HunkDiffNode) rpSelectedNode;
				rpTreeViewer.setSelection(new StructuredSelection(getPatcher().getTargetFile(node.getHunkResult().getDiffResult().getDiff())));
				mode = DIFF_NODE;
			} else if (rpSelectedNode instanceof PatchProjectDiffNode) {
				PatchProjectDiffNode node = (PatchProjectDiffNode) rpSelectedNode;
				rpSelectedProject = node.getDiffProject();
				rpTreeViewer.setSelection(new StructuredSelection(rpSelectedProject.getProject()));
				mode = PROJECTDIFF_NODE;
			}
			
			rpTreeViewer.setContentProvider(new RetargetPatchContentProvider(mode));
			rpTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
			rpTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
			rpTreeViewer.setInput(ResourcesPlugin.getWorkspace());
			
			setupListeners();

			Dialog.applyDialogFont(composite);
			
			return parent;
		}

		protected void okPressed() {
			if (rpSelectedNode != null){
				if (rpSelectedNode instanceof PatchProjectDiffNode && rpTargetResource instanceof IProject) {
					PatchProjectDiffNode node = (PatchProjectDiffNode) rpSelectedNode;
					DiffProject project = node.getDiffProject();
					getPatcher().retargetProject(project, (IProject)rpTargetResource);
				} else if (rpSelectedNode instanceof PatchFileDiffNode && rpTargetResource instanceof IFile) {
					PatchFileDiffNode node = (PatchFileDiffNode) rpSelectedNode;
					//copy over all hunks to new target resource
					FileDiff diff = node.getDiffResult().getDiff();
					fPatcher.retargetDiff(diff, (IFile)rpTargetResource);
				} else if (rpSelectedNode instanceof HunkDiffNode && rpTargetResource instanceof IFile) {
					HunkDiffNode node = (HunkDiffNode) rpSelectedNode;
					fPatcher.retargetHunk(node.getHunkResult().getHunk(), (IFile)rpTargetResource);
				}
			}
			super.okPressed();	
		}

		void setupListeners() {
			rpTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection s= (IStructuredSelection) event.getSelection();
					Object obj= s.getFirstElement();
					if (obj instanceof IResource){
						rpTargetResource = (IResource) obj;
						if (rpSelectedNode instanceof PatchProjectDiffNode) {
							if (rpTargetResource instanceof IProject){
								Button okButton = getButton(IDialogConstants.OK_ID);
								okButton.setEnabled(true);
							}
						} else if (rpSelectedNode instanceof PatchFileDiffNode
								|| rpSelectedNode instanceof HunkDiffNode) {
							if (rpTargetResource instanceof IFile){
								Button okButton = getButton(IDialogConstants.OK_ID);
								okButton.setEnabled(true);
							}
						}
					}
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
		
	public PreviewPatchPage2(WorkspacePatcher patcher, CompareConfiguration configuration) {
		super(PREVIEWPATCHPAGE_NAME, PatchMessages.PreviewPatchPage_title, null);
		Assert.isNotNull(patcher);
		Assert.isNotNull(configuration);
		this.fPatcher = patcher;
		this.fConfiguration = configuration;
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//if a compare configuration has been passed make sure 
		fInput = new PreviewPatchPageInput(getPatcher(), getCompareConfiguration());
			
		initializeDialogUnits(parent);
		
		buildPatchOptionsGroup(composite);
		
		// Initialize the input
		try {
			fInput.run(null);
		} catch (InterruptedException e) {//ignore
		} catch (InvocationTargetException e) {//ignore
		}
	
		Control c = fInput.createContents(composite);
		fInput.contributeDiffViewerToolbarItems(getContributedActions(), getPatcher().isWorkspacePatch());
		fInput.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel= (IStructuredSelection) event.getSelection();
				Object obj= sel.getFirstElement();
				String name = null;
				boolean enable = false;
				if (obj instanceof PatchProjectDiffNode) {
					name = PatchMessages.PreviewPatchPage2_RetargetProject;
					enable = true;
				} else if (obj instanceof PatchFileDiffNode) {
					PatchFileDiffNode node = (PatchFileDiffNode) obj;
					name = PatchMessages.PreviewPatchPage2_RetargetDiff;
					enable = node.getDiffResult().getDiffProblem();
				} else if (obj instanceof HunkDiffNode) {
					name = PatchMessages.PreviewPatchPage2_RetargetHunk;
					enable = true;
				}
				if (name != null) {
					fRetargetResource.setText(name);
				}
				fRetargetResource.setEnabled(enable);
			}
		});
		fInput.contributeDiffViewerMenuItems(getMenuActions());

		c.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		setControl(composite);
		
	}
	
	/**
	 * Makes sure that at least one hunk is checked off in the tree before
	 * allowing the patch to be applied.
	 */
	private void updateEnablements() {
		boolean atLeastOneIsEnabled = false;
		if (fInput != null)
			atLeastOneIsEnabled = fInput.hasResultToApply();
		setPageComplete(atLeastOneIsEnabled);
	}
	
	private Action[] getContributedActions() {
		fIncludeToggle= new Action(PatchMessages.PreviewPatchPage2_IncludeElement, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.RETARGET_PROJECT)) {
			public void run() {
				ISelection selection = fInput.getViewer().getSelection();
				if (selection instanceof TreeSelection){
					Iterator iter = ((TreeSelection) selection).iterator();
					while (iter.hasNext()){
						Object obj = iter.next();
						if (obj instanceof PatchDiffNode){
							PatchDiffNode node = ((PatchDiffNode) obj);
							node.setEnabled(!node.isEnabled());
							// TODO: This may require a rebuild if matched hunks are shown
						} 
					}
				}
				fInput.getViewer().refresh();
			}
		};
		fIncludeToggle.setToolTipText(PatchMessages.PreviewPatchPage2_IncludeElementText);
		fIncludeToggle.setEnabled(true);
		fIncludeToggle.setId(toggleInclusionID);
		
		fIgnoreWhiteSpace = new Action(PatchMessages.PreviewPatchPage2_IgnoreWSAction, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.IGNORE_WHITESPACE_ENABLED)){
			public void run(){
				try {
					getContainer().run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask(PatchMessages.PreviewPatchPage2_IgnoreWhitespace, IProgressMonitor.UNKNOWN);
							if (isChecked() != getPatcher().isIgnoreWhitespace()) {
								if (promptToRebuild("Performing this operation will require that your manual changes be discarded.")) {
									if (getPatcher().setIgnoreWhitespace(isChecked())){
										rebuildTree();
									}
								} else {
									fIgnoreWhiteSpace.setChecked(!isChecked());
								}
							}
							monitor.done();
						}
					});
				} catch (InvocationTargetException e) { //ignore
				} catch (InterruptedException e) { //ignore
				}
			}
		};
		fIgnoreWhiteSpace.setChecked(false);
		fIgnoreWhiteSpace.setToolTipText(PatchMessages.PreviewPatchPage2_IgnoreWSTooltip);
		fIgnoreWhiteSpace.setDisabledImageDescriptor(CompareUIPlugin.getImageDescriptor(ICompareUIConstants.IGNORE_WHITESPACE_DISABLED));
		fIgnoreWhiteSpace.setId(ignoreWSID);
		
		fReversePatch = new Action(PatchMessages.PreviewPatchPage_ReversePatch_text, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.REVERSE_PATCH_ENABLED)){
			public void run(){
				try {
					getContainer().run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask(PatchMessages.PreviewPatchPage2_CalculateReverse, IProgressMonitor.UNKNOWN);
							if (isChecked() != getPatcher().isReversed()) {
								if (promptToRebuild("Reversing the patch will require that your manual changes be discarded.")) {
									if (getPatcher().setReversed(isChecked())){
										rebuildTree();
									}
								} else {
									fReversePatch.setChecked(!isChecked());
								}
							}
							monitor.done();
						}
					});
				} catch (InvocationTargetException e) { //ignore
				} catch (InterruptedException e) { //ignore
				}
				
			}
			
		};
		fReversePatch.setChecked(false);
		fReversePatch.setToolTipText(PatchMessages.PreviewPatchPage_ReversePatch_text);
		fReversePatch.setId(reversePatchID);
		
		return new Action[]{fIncludeToggle, fIgnoreWhiteSpace, fReversePatch};
	}

	private Action[] getMenuActions() {
		fRetargetResource = new Action(PatchMessages.PreviewPatchPage2_RetargetDiff,
				CompareUIPlugin.getImageDescriptor(ICompareUIConstants.RETARGET_PROJECT)) {
			public void run() {
				Shell shell = getShell();
				ISelection selection = fInput.getViewer().getSelection();
				final RetargetPatchDialog dialog = new RetargetPatchDialog(
						shell, selection);
				int returnCode = dialog.open();
				if (returnCode == Window.OK) {
					// TODO: This could be a problem. We should only rebuild the affected nodes
					rebuildTree();
				}
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
		if (visible){
			fillSegmentCombo();
			// TODO: We should only do this if the tree needs to be rebuilt
			rebuildTree();
		}
	}
	
	private boolean promptToRebuild(final String promptToConfirm){
		final Control ctrl = getControl();
		final boolean[] result = new boolean[] { false };
		if (ctrl != null && !ctrl.isDisposed()){
			Runnable runnable = new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
						// flush any viewers before prompting
						try {
							fInput.saveChanges(null);
						} catch (CoreException e) {
							CompareUIPlugin.log(e);
						}
						result[0] = fInput.confirmRebuild(promptToConfirm);
					}
				}
			};
			if (Display.getCurrent() == null)
				ctrl.getDisplay().syncExec(runnable);
			else
				runnable.run();
		}
		return result[0];
	}
	
	private void rebuildTree(){
		final Control ctrl = getControl();
		if (ctrl != null && !ctrl.isDisposed()){
			Runnable runnable = new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
						fInput.buildTree();
						updateEnablements();
					}
				}
			};
			if (Display.getCurrent() == null)
				ctrl.getDisplay().syncExec(runnable);
			else
				runnable.run();
		}
	}

	private void fillSegmentCombo() {
		if (getPatcher().isWorkspacePatch()) {
			fStripPrefixSegments.setEnabled(false);
		} else {
			fStripPrefixSegments.setEnabled(true);
			int length= 99;
			if (fStripPrefixSegments!=null && pageRecalculate) {
				length= getPatcher().calculatePrefixSegmentCount();
				if (length!=99) {
					for (int k= 1; k<length; k++)
						fStripPrefixSegments.add(Integer.toString(k));
					pageRecalculate= false;
				}
			}
		}
	}
	/*
	 *	Create the group for setting various patch options
	 */
	private void buildPatchOptionsGroup(Composite parent) {
		
		GridLayout gl;
		GridData gd;
		Label l;

		final WorkspacePatcher patcher= getPatcher();

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
				getPatcher().setGenerateRejects(
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
			b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (promptToRebuild("Changing the fuzz factor will require that your manual changes be discarded.")) {
							int fuzz= guessFuzzFactor(patcher);
							if (fuzz>=0)
								fFuzzField.setText(Integer.toString(fuzz));
						}
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
					if (patcher.getStripPrefixSegments() != getStripPrefixSegments()) {
						if (promptToRebuild("Performing this operation will require that your manual changes be discarded.")) {
							if (patcher.setStripPrefixSegments(getStripPrefixSegments()))
								rebuildTree();
							}
						}
					}
				}
			);
	

		fFuzzField.addModifyListener(
			new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (patcher.getFuzz() != getFuzzFactor()) {
					if (promptToRebuild("Changing the fuzz factor will require that your manual changes be discarded.")) {
						if (patcher.setFuzz(getFuzzFactor()))
							rebuildTree();
					} else {
						fFuzzField.setText(Integer.toString(patcher.getFuzz()));
					}
				}
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
		final int[] result= new int[] { -1 };
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true,
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							result[0]= patcher.guessFuzzFactor(monitor);
						}
				}
			);
		} catch (InvocationTargetException ex) {
			// NeedWork
		} catch (InterruptedException ex) {
			// NeedWork
		}
		return result[0];
	}
	
	public void ensureContentsSaved() {
		try {
			fInput.saveChanges(new NullProgressMonitor());
		} catch (CoreException e) {
			//ignore
		}
	}

	public WorkspacePatcher getPatcher() {
		return fPatcher;
	}

	public CompareConfiguration getCompareConfiguration() {
		return fConfiguration;
	}
	

}
