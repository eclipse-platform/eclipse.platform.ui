package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class HunkMergePage extends WizardPage implements IContentChangeListener {

	protected final static String HUNKMERGEPAGE_NAME = "HunkMergePage"; //$NON-NLS-1$

	private HunkMergePageInput hunkMergeInput = new HunkMergePageInput();

	PatchWizard fPatchWizard;

	/*
	 * private ArrayList patchedDiffs; private DiffNode rootNode;
	 */

	// tracks which diffs actually get edited
	private HashSet alteredDiffs = new HashSet();

	// maps diffs to merged file contents
	private HashMap alteredFiles = new HashMap();

	protected HunkMergePage(PatchWizard pw) {
		super(HUNKMERGEPAGE_NAME, PatchMessages.HunkMergePage_PageTitle, null);
		setDescription(PatchMessages.HunkMergePage_Info);
		fPatchWizard = pw;

	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		initializeDialogUnits(parent);

		buildPatchOptionsGroup(composite);

		try {
			hunkMergeInput.run(null);
		} catch (InterruptedException e) {// ignore
		} catch (InvocationTargetException e) {// ignore
		}

		Control c = hunkMergeInput.createContents(composite);
		hunkMergeInput.setHunkMergePage(this);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
	}

	public void setRoot(DiffNode newRoot) {
		this.hunkMergeInput.root = newRoot;
	}

	private void fillTree() {
		hunkMergeInput.updateInput(fPatchWizard.getPatcher());
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// Need to handle input and rebuild tree only when becoming visible
		if (visible) {
			fillTree();
		}
	}

	public void contentChanged(IContentChangeNotifier source) {
		if (source instanceof PatchedFileWrapper) {
			PatcherDiffNode parentNode = ((PatchedFileWrapper) source)
					.getParent();
			String name = parentNode.getName();
			int index = name
					.lastIndexOf(PatchMessages.PreviewPatchPage_NoMatch_error);
			if (index != -1) {
				parentNode.setName(NLS.bind(PatchMessages.Diff_2Args, new String[] {name.substring(0, index), PatchMessages.HunkMergePage_Merged}));
				hunkMergeInput.getViewer().refresh();
			}
			Hunk tempHunk = parentNode.getHunk();
			Assert.isNotNull(tempHunk);
			Diff tempDiff = (Diff) tempHunk.getParent(tempHunk);
			Assert.isNotNull(tempDiff);
			alteredDiffs.add(tempDiff);

			// now that one hunk has been changed this page can be considered complete
			setPageComplete(true);
		}
	}

	/*
	 * Create the group for setting various patch options
	 */
	private void buildPatchOptionsGroup(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText(PatchMessages.PreviewPatchPage_PatchOptions_title);
		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		group.setLayout(gl);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL));

		// 1st row

		Composite pair = new Composite(group, SWT.NONE);
		gl = new GridLayout();
		gl.numColumns = 2;
		gl.marginHeight = gl.marginWidth = 0;
		pair.setLayout(gl);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		pair.setLayoutData(gd);

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

	}

	public HashMap getMergedFileContents() {
		return alteredFiles;
	}

	public void setMergedFile(Diff tempDiff, PatchedFileNode patchedNode) {
		alteredFiles.put(tempDiff, patchedNode);
	}

	public HashSet getModifiedDiffs() {
		return alteredDiffs;
	}

	public void ensureContentsSaved() {
		try {
			hunkMergeInput.saveChanges(new NullProgressMonitor());
		} catch (CoreException e) {
			//ignore
		}
	}
}
