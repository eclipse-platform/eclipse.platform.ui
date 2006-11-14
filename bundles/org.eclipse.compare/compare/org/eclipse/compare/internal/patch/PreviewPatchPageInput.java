package org.eclipse.compare.internal.patch;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffElement;

public abstract class PreviewPatchPageInput extends PatcherCompareEditorInput {
	
	private boolean containsHunkErrors = false;
	
	public PreviewPatchPageInput(WorkspacePatcher patcher, CompareConfiguration configuration){
		super(patcher, configuration);
	}
	
	protected void updateTree() {
		if (getViewer() != null && !getViewer().getControl().isDisposed())
			getViewer().refresh(true);
	}
	
	protected void buildTree(){
		
		// Reset the input node so it is empty
		if (getRoot().hasChildren()) {
			resetRoot();
		}
		// Reset the input of the viewer so the old state is no longer used
		getViewer().setInput(getRoot());
		
		// Refresh the patcher state
		getPatcher().refresh();
		
		// Build the diff tree
		if (getPatcher().isWorkspacePatch()){
			processProjects(getPatcher().getDiffProjects());
		} else {
			processDiffs(getPatcher().getDiffs());
		}
		
		// Refresh the viewer
		getViewer().refresh();
	}
	
	private void processDiffs(FileDiff[] diffs) { 
		for (int i = 0; i < diffs.length; i++) {
			processDiff(diffs[i], getRoot());
		}
	}

	private void processProjects(DiffProject[] diffProjects) {
		//create diffProject nodes
		for (int i = 0; i < diffProjects.length; i++) {
			PatchProjectDiffNode projectNode = new PatchProjectDiffNode(getRoot(), diffProjects[i], getPatcher());
			FileDiff[] diffs = diffProjects[i].getFileDiffs();
			for (int j = 0; j < diffs.length; j++) {
				FileDiff fileDiff = diffs[j];
				processDiff(fileDiff, projectNode);
			}
		}
	}

	private void processDiff(FileDiff diff, DiffNode parent) {
		FileDiffResult diffResult = getPatcher().getDiffResult(diff);
		PatchFileDiffNode node = PatchFileDiffNode.createDiffNode(parent, diffResult);
		HunkResult[] hunkResults = diffResult.getHunkResults();
		for (int i = 0; i < hunkResults.length; i++) {
			HunkResult hunkResult = hunkResults[i];
			if (!hunkResult.isOK()) {
				HunkDiffNode hunkNode = HunkDiffNode.createDiffNode(node, hunkResult, true);
				Object left = hunkNode.getLeft();
				if (left instanceof UnmatchedHunkTypedElement) {
					UnmatchedHunkTypedElement element = (UnmatchedHunkTypedElement) left;
					element.addContentChangeListener(new IContentChangeListener() {
						public void contentChanged(IContentChangeNotifier source) {
							if (getViewer() == null || getViewer().getControl().isDisposed())
								return;
							getViewer().refresh(true);
						}
					});
				}
			}
		}
	}

	/**
	 * Return whether this input has a result to apply. The input
	 * has a result to apply if at least one hunk is selected for inclusion.
	 * @return whether this input has a result to apply
	 */
	public boolean hasResultToApply() {
		boolean atLeastOneIsEnabled = false;
		if (getViewer() != null) {
			IDiffElement[] elements = getRoot().getChildren();
			for (int i = 0; i < elements.length; i++) {
				IDiffElement element = elements[i];
				if (isEnabled(element)) {
					atLeastOneIsEnabled = true;
					break;
				}
			}
		}
		return atLeastOneIsEnabled;
	}

	private boolean isEnabled(IDiffElement element) {
		if (element instanceof PatchDiffNode) {
			PatchDiffNode node = (PatchDiffNode) element;
			return node.isEnabled();
		}
		return false;
	}

	public boolean containsHunkErrors() {
		return containsHunkErrors;
	}

}
