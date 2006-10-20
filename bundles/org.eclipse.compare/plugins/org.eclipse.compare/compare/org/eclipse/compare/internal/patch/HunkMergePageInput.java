package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class HunkMergePageInput extends PatcherCompareEditorInput {

	protected HunkMergePage hunkMergePage;

	public HunkMergePageInput(){
		super();
	}
	
	public HunkMergePageInput(CompareConfiguration config){
		super(config);
	}
	
	public Viewer createDiffViewer(Composite parent) {
		this.viewer = new DiffTreeViewer(parent, getCompareConfiguration());
		return viewer;
	}

	protected void buildTree(WorkspacePatcher patcher) {
		viewer.setInput(root);
		viewer.refresh();
	}

	protected void updateTree(WorkspacePatcher patcher) {
		//no-op
	}

	public void setHunkMergePage(HunkMergePage page) {
		this.hunkMergePage = page;
	}

	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		initLabels();
		return root;
	}

	private void initLabels() {
		CompareConfiguration cc = getCompareConfiguration();
		cc.setCalculateDiffs(false);
		cc.setLeftEditable(true);
		cc.setRightEditable(false);
		cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
		
		if (config != null){
			cc.setLeftLabel(config.getLeftLabel(config));
			cc.setLeftImage(config.getLeftImage(config));
			cc.setRightLabel(config.getRightLabel(config));
			cc.setRightImage(config.getRightImage(config));
		} else {
			String leftLabel = PatchMessages.HunkMergePageInput_WorkspaceCopy;
			cc.setLeftLabel(leftLabel);
			String rightLabel = PatchMessages.HunkMergePageInput_OrphanedHunk;
			cc.setRightLabel(rightLabel);
		}
		
	}

	protected void updateEnablements() {
		//no-op	
	}

}
