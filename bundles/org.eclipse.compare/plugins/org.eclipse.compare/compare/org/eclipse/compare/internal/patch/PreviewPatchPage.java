/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.Splitter;
import org.eclipse.compare.internal.ICompareContextIds;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Shows the parsed patch file and any mismatches
 * between files, hunks and the currently selected
 * resources.
 */
/* package */ class PreviewPatchPage extends WizardPage {
		
	/**
	 * Used with CompareInput
	 */
	static class HunkInput implements ITypedElement, IEncodedStreamContentAccessor {
		static final String UTF_16= "UTF-16"; //$NON-NLS-1$
		String fContent;
		String fType;
		
		HunkInput(String type, String s) {
			fType= type;
			fContent= s;
		}
		public Image getImage() {
			return null;
		}
		public String getName() {
			return PatchMessages.PreviewPatchPage_NoName_text; 
		}
		public String getType() {
			return fType;
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(Utilities.getBytes(fContent, UTF_16));
		}
		public String getCharset() {
			return UTF_16;
		}
	}

	class RetargetPatchDialog extends Dialog {

		protected TreeViewer rpTreeViewer;
		protected DiffProject rpSelectedProject;
		protected IProject rpTargetProject;

		public RetargetPatchDialog(Shell shell, ISelection selection) {
			super(shell);
			setShellStyle(getShellStyle()|SWT.RESIZE);
			if (selection instanceof IStructuredSelection) {
				rpSelectedProject= (DiffProject) ((IStructuredSelection) selection).getFirstElement();
			}
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite= (Composite) super.createDialogArea(parent);

			initializeDialogUnits(parent);

			getShell().setText(PatchMessages.PreviewPatchPage_RetargetPatch);

			GridLayout layout= new GridLayout();
			layout.numColumns= 1;
			composite.setLayout(layout);
			final GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
			composite.setLayoutData(data);

			//add controls to composite as necessary
			Label label= new Label(composite, SWT.LEFT|SWT.WRAP);
			label.setText(NLS.bind(PatchMessages.Diff_2Args, new String[] {PatchMessages.PreviewPatchPage_SelectProject, rpSelectedProject.getName()}));
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
			rpTreeViewer.setSelection(new StructuredSelection(rpSelectedProject.getProject()));

			setupListeners();

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

	private PatchWizard fPatchWizard;

	private ContainerCheckedTreeViewer fTreeViewer;
	private Combo fStripPrefixSegments;
	private CompareViewerSwitchingPane fHunkViewer;
	private Button fIgnoreWhitespaceButton;
	private Button fReversePatchButton;
	private Text fFuzzField;
	private Button fMatchProject;

	private Object inputElement;
	private CompareConfiguration fCompareConfiguration;

	protected boolean pageRecalculate= true;
	protected final static String PREVIEWPATCHPAGE_NAME= "PreviewPatchPage"; //$NON-NLS-1$

	/* package */ PreviewPatchPage(PatchWizard pw) {
		super(PREVIEWPATCHPAGE_NAME, PatchMessages.PreviewPatchPage_title, null);

		setMessage(PatchMessages.PreviewPatchPage_message);

		fPatchWizard= pw;
		//setPageComplete(false);

		fCompareConfiguration= new CompareConfiguration();

		fCompareConfiguration.setLeftEditable(false);
		fCompareConfiguration.setLeftLabel(PatchMessages.PreviewPatchPage_Left_title);

		fCompareConfiguration.setRightEditable(false);
		fCompareConfiguration.setRightLabel(PatchMessages.PreviewPatchPage_Right_title);
	}

	/* (non-Javadoc)
	 * Method declared in WizardPage
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			buildTree();
			updateTree();
		}
		super.setVisible(visible);
	}

	public void createControl(Composite parent) {

		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL|GridData.HORIZONTAL_ALIGN_FILL));

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICompareContextIds.PATCH_PREVIEW_WIZARD_PAGE);

		setControl(composite);

		buildPatchOptionsGroup(composite);

		Splitter splitter= new Splitter(composite, SWT.VERTICAL);
		splitter.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
					| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));

		// top pane showing diffs and hunks in a check box tree 
		createTreeViewer(splitter);

		// bottom pane showing hunks in compare viewer 
		fHunkViewer= new CompareViewerSwitchingPane(splitter, SWT.BORDER|SWT.FLAT) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				return CompareUI.findContentViewer(oldViewer, (ICompareInput) input, this, fCompareConfiguration);
			}
		};
		GridData gd= new GridData();
		gd.verticalAlignment= GridData.FILL;
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.grabExcessVerticalSpace= true;
		fHunkViewer.setLayoutData(gd);

		//create Match Project button
		fMatchProject= new Button(composite, SWT.PUSH);
		gd= new GridData();
		gd.verticalAlignment= GridData.BEGINNING;
		gd.horizontalAlignment= GridData.END;
		fMatchProject.setLayoutData(gd);

		fMatchProject.setText(PatchMessages.PreviewPatchPage_MatchProjects);
		fMatchProject.setEnabled(false);
		fMatchProject.setVisible(false);
		fMatchProject.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final RetargetPatchDialog dialog= new RetargetPatchDialog(getShell(), fTreeViewer.getSelection());
				dialog.open();
				updateTree();
			}
		});

		// creating tree's content
		buildTree();
		Dialog.applyDialogFont(composite);
	}

	private void createTreeViewer(Splitter splitter) {
		fTreeViewer= new ContainerCheckedTreeViewer(splitter, SWT.BORDER);
		fTreeViewer.setContentProvider(new BaseWorkbenchContentProvider());
		fTreeViewer.setLabelProvider(new DecoratingLabelProvider(new WorkbenchLabelProvider(), new PreviewPatchLabelDecorator()));
		fTreeViewer.setSorter(new WorkbenchViewerSorter());
		fTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object obj = event.getElement();
				ICheckable checked = event.getCheckable();
				DiffProject proj = null;
				if (obj instanceof DiffProject){
					proj = (DiffProject) obj;
					// Check to see if any of the Diffs contained by the DiffProject
					// have their diff problems set
					Object[] diffs = proj.getChildren(null);
					for (int i= 0; i<diffs.length; i++) {
					if (((Diff) diffs[i]).containsProblems()){
						checked.setChecked(obj, false);
							break;
						}
					}
				} else if (obj instanceof Diff){
					proj = ((Diff) obj).getProject();
					// If Diff has any diff problems set, at least one hunk underneath 
					// does not match - so don't allow entire tree to be checked
					if (((Diff) obj).containsProblems()){
						checked.setChecked(obj, false);
					}
				} else if (obj instanceof Hunk){
					Diff diff = (Diff) ((Hunk) obj).getParent(null);
					proj = diff.getProject();
					// Check to see if this hunk has any problems OR
					// if its parent has any problems
					if( diff.getDiffProblem() ||
						((Hunk) obj).getHunkProblem()){
						checked.setChecked(obj, false);
					}
				}
				if (proj!= null &&
				   !proj.getProject().exists()){
					checked.setChecked(obj, false);
				}
				updateEnablements();
			}
		});

		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel= (IStructuredSelection) event.getSelection();
				Object obj= sel.getFirstElement();

				if (obj instanceof Hunk) {
					PreviewPatchPage.this.fHunkViewer.setInput(createInput((Hunk) obj));
				} else
					PreviewPatchPage.this.fHunkViewer.setInput(null);

				fMatchProject.setEnabled(false);
				//See if we need to enable match project button
				if (fPatchWizard.getPatcher().isWorkspacePatch()&&obj instanceof DiffProject) {
					fMatchProject.setEnabled(true);
				}

			}

		});
		fTreeViewer.setInput(null);
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
		gl= new GridLayout(); gl.numColumns= 4; gl.marginHeight= 0;
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

		fReversePatchButton= new Button(group, SWT.CHECK);
		fReversePatchButton.setText(PatchMessages.PreviewPatchPage_ReversePatch_text);

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
		b.setLayoutData(gd);

		addSpacer(group);

		fIgnoreWhitespaceButton= new Button(group, SWT.CHECK);
		fIgnoreWhitespaceButton.setText(PatchMessages.PreviewPatchPage_IgnoreWhitespace_text);

		addSpacer(group);

		// register listeners

		if (fStripPrefixSegments!=null)
			fStripPrefixSegments.addSelectionListener(
				new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (patcher.setStripPrefixSegments(getStripPrefixSegments()))
						updateTree();
				}
				}
			);
		fReversePatchButton.addSelectionListener(
			new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (patcher.setReversed(fReversePatchButton.getSelection()))
					updateTree();
			}
			}
		);
		fIgnoreWhitespaceButton.addSelectionListener(
			new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (patcher.setIgnoreWhitespace(fIgnoreWhitespaceButton.getSelection()))
					updateTree();
			}
			}
		);

		fFuzzField.addModifyListener(
			new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (patcher.setFuzz(getFuzzFactor()))
					updateTree();
			}
		});
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
			if (diff.getType()!=Differencer.ADDITION) {
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

	ICompareInput createInput(Hunk hunk) {

		String[] lines= hunk.fLines;
		StringBuffer left= new StringBuffer();
		StringBuffer right= new StringBuffer();

		for (int i= 0; i<lines.length; i++) {
			String line= lines[i];
			String rest= line.substring(1);
			switch (line.charAt(0)) {
				case ' ' :
					left.append(rest);
					right.append(rest);
					break;
				case '-' :
					left.append(rest);
					break;
				case '+' :
					right.append(rest);
					break;
			}
		}

		Diff diff= hunk.fParent;
		IPath path= diff.getPath();
		String type= path.getFileExtension();

		return new DiffNode(new HunkInput(type, left.toString()), new HunkInput(type, right.toString()));
	}

	private IFile existsInSelection(IPath path) {
		return fPatchWizard.getPatcher().existsInTarget(path);
	}

	private void buildTree() {

		inputElement= fPatchWizard.getPatcher();

		//Update prefix count - go through all of the diffs and find the smallest
		//path segment contained in all diffs.
		int length= 99;
		if (fStripPrefixSegments!=null&&pageRecalculate) {
			length= fPatchWizard.getPatcher().calculatePrefixSegmentCount();
			if (length!=99) {
				for (int k= 1; k<length; k++)
					fStripPrefixSegments.add(Integer.toString(k));
				pageRecalculate= false;
			}
		}

		fTreeViewer.setInput(inputElement);
	}

	/**
	 * Updates label and checked state of tree items.
	 */
	private void updateTree() {

		if (fTreeViewer==null)
			return;

		int strip= getStripPrefixSegments();
		//Get the elements from the content provider
		BaseWorkbenchContentProvider contentProvider= (BaseWorkbenchContentProvider) fTreeViewer.getContentProvider();
		Object[] projects= contentProvider.getElements(inputElement);
		ArrayList hunksToCheck= new ArrayList();
		//Iterate through projects and call reset on each project
		for (int j= 0; j<projects.length; j++) {
			if (projects[j] instanceof DiffProject) {
				DiffProject project= (DiffProject) projects[j];
				hunksToCheck.addAll(project.reset(fPatchWizard.getPatcher(), strip, getFuzzFactor()));
				for (Iterator iter= project.fDiffs.iterator(); iter.hasNext();) {
					Diff diff= (Diff) iter.next();
					fTreeViewer.update(diff, null);
				}
			} else {
				if (projects[j] instanceof Diff) {
					Diff diff= (Diff) projects[j];
					hunksToCheck.addAll(diff.reset(fPatchWizard.getPatcher(), strip, getFuzzFactor()));
					fTreeViewer.update(diff, null);
				}
			}
		}
		fTreeViewer.refresh();
		fTreeViewer.setCheckedElements(hunksToCheck.toArray());
		updateEnablements();
	}

	private void addSpacer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= 20;
		label.setLayoutData(gd);
	}

	private int getStripPrefixSegments() {
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

	private int getFuzzFactor() {
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

	/**
	 * Makes sure that at least one hunk is checked off in the tree before
	 * allowing the patch to be applied.
	 */
	/* private */void updateEnablements() {
		boolean atLeastOneIsEnabled= false;
		if (fTreeViewer!=null) {
			BaseWorkbenchContentProvider contentProvider= (BaseWorkbenchContentProvider) fTreeViewer.getContentProvider();
			Object[] projects= contentProvider.getElements(inputElement);
			//Iterate through projects
			for (int j= 0; j<projects.length; j++) {
				if (projects[j] instanceof DiffProject) {
					DiffProject project= (DiffProject) projects[j];
					//Iterate through project diffs
					Object[] diffs= project.getChildren(project);
					for (int i= 0; i<diffs.length; i++) {
						Diff diff= (Diff) diffs[i];
						atLeastOneIsEnabled= updateEnablement(atLeastOneIsEnabled, diff);
					}
				} else if (projects[j] instanceof Diff) {
					Diff diff= (Diff) projects[j];
					atLeastOneIsEnabled= updateEnablement(atLeastOneIsEnabled, diff);
				}
			}
		}

		//Check to see if Match Project button should be visible
		fMatchProject.setVisible(fPatchWizard.getPatcher().isWorkspacePatch());

		setPageComplete(atLeastOneIsEnabled);
	}

	private boolean updateEnablement(boolean atLeastOneIsEnabled, Diff diff) {
		boolean checked= fTreeViewer.getChecked(diff);
		diff.setEnabled(checked);
		if (checked) {
			Object[] hunkItems= diff.getChildren(diff);
			for (int h= 0; h<hunkItems.length; h++) {
				Hunk hunk= (Hunk) hunkItems[h];
				checked= fTreeViewer.getChecked(hunk);
				hunk.setEnabled(checked);
				if (checked) {
					//For workspace patch: before setting enabled flag, make sure that the project
					//that contains this hunk actually exists in the workspace. This is to guard against the 
					//case of having a new file in a patch that is being applied to a project that
					//doesn't currently exist.
					boolean projectExists= true;
					DiffProject project= (DiffProject)diff.getParent(null);
					if (project!= null){
						projectExists=project.getProject().exists();
					}
					if (projectExists)
						atLeastOneIsEnabled= true;
				}

			}
		}
	
		return atLeastOneIsEnabled;
	}

}
