package org.eclipse.compare.internal.patch;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class PreviewPatchPageInput extends PatcherCompareEditorInput {

	protected PreviewPatchPage2 previewPatchPage;

	private HashMap filesToDiffs;
	
	private boolean containsHunkErrors = false;

	public PreviewPatchPageInput(){
		super();
	}
	
	public PreviewPatchPageInput(CompareConfiguration config){
		super(config);
	}
	
	protected void updateTree() {
		if (viewer == null)
			return;
		
		//
		// Get the elements from the content provider
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer
				.getContentProvider();
		Object[] projects = contentProvider.getElements(root);
	
		// Iterate through projects and call reset on each project
		for (int j = 0; j < projects.length; j++) {
			if ((projects[j] instanceof PatcherDiffNode) && ((PatcherDiffNode)projects[j]).getPatchNodeType() == PatcherDiffNode.PROJECT) {
				PatcherDiffNode projectNode = (PatcherDiffNode) projects[j];
				
				//call reset on the project
				projectNode.getDiffProject().reset(workspacePatcher, previewPatchPage.getStripPrefixSegments(), previewPatchPage.getFuzzFactor());
				IDiffElement[] diffNodes = projectNode.getChildren();

				for (int i = 0; i < diffNodes.length; i++) {
					viewer.update(diffNodes[i], null);
					IDiffElement[] hunkNodes = ((PatcherDiffNode) diffNodes[i]).getChildren();
					for (int k = 0; k < hunkNodes.length; k++) {
						viewer.update(hunkNodes[k], null);
					}
				}

			} else {
					PatcherDiffNode diffNode = (PatcherDiffNode) projects[j];
					diffNode.getDiff().reset(workspacePatcher, previewPatchPage.getStripPrefixSegments(), previewPatchPage.getFuzzFactor());
					IDiffElement[] diffNodes = diffNode.getChildren();
					for (int i = 0; i < diffNodes.length; i++) {
						viewer.update(diffNodes[i], null);
						IDiffElement[] hunkNodes = ((PatcherDiffNode) diffNodes[i])
								.getChildren();
						for (int k = 0; k < hunkNodes.length; k++) {
							viewer.update(hunkNodes[k], null);
						}
					}
				}
			}
		viewer.refresh();

		updateEnablements();
	}
	
	protected void buildTree(WorkspacePatcher patcher){
		
		//clean root
		if (root.hasChildren()) {
			resetRoot();
		}
		
		//new nodes to diffs mapping
		nodesToDiffs = new HashMap();
		filesToDiffs = new HashMap();
		
		if (patcher.isWorkspacePatch()){
			processProjects(patcher.getDiffProjects());
		} else {
			processDiffs(patcher.getDiffs());
		}
		
		viewer.setInput(root);
		viewer.refresh();
	}
	private void processDiffs(Diff[] diffs) { 
		for (int i = 0; i < diffs.length; i++) {
			IPath filePath = new Path(diffs[i].getLabel( diffs[i]));
			IFile tempFile = workspacePatcher.existsInTarget(filePath);
			processDiff(diffs[i], tempFile, root);
		}
	}

	private void processProjects(DiffProject[] diffProjects) {
		//create diffProject nodes
		for (int i = 0; i < diffProjects.length; i++) {
			PatcherDiffNode projectNode = new PatcherDiffNode(root,Differencer.CHANGE, null, diffProjects[i], null, diffProjects[i]);
			Iterator iter = diffProjects[i].fDiffs.iterator();
			while (iter.hasNext()){
				Object obj = iter.next();
				if (obj instanceof Diff) {
					Diff diff = (Diff) obj;
					IPath filePath = new Path(diff.getLabel(diff));
					IFile tempFile = diffProjects[i].getFile(filePath);
					processDiff(diff, tempFile, projectNode);
				}
			}
		}
	}

	private void processDiff(Diff diff, IFile tempFile, DiffNode rootNode) {
		byte[] bytes;
		try {			
			bytes = quickPatch(tempFile, workspacePatcher, diff);
					
			int differencer = Differencer.CHANGE;
			if (failedHunks.size() != 0) {
				differencer += Differencer.CONFLICTING;
			}

			ITypedElement tempNode;
			PatchedFileNode patchedNode;

			if (tempFile != null && tempFile.exists()) {
				tempNode = new ResourceNode(tempFile);
				patchedNode = new PatchedFileNode(bytes, tempNode.getType(),
						tempFile.getProjectRelativePath().toString(), true);
				filesToDiffs.put(tempFile, diff);
			} else {
				IPath filePath = new Path(diff.getLabel(diff));
				tempNode = new PatchedFileNode(new byte[0], filePath
						.getFileExtension(),
						PatchMessages.PatcherCompareEditorInput_FileNotFound);
				patchedNode = new PatchedFileNode(bytes, tempNode.getType(), ""); //$NON-NLS-1$
			}
			
			//PatchedFileWrapper patchedFileWrapper = new PatchedFileWrapper(patchedNode);
			PatcherDiffNode diffNode = new PatcherDiffNode(rootNode,differencer, tempNode, tempNode, patchedNode, diff);

			nodesToDiffs.put(diff, diffNode);
			
			//process hunks
			Hunk[] hunks = diff.getHunks();
			for (int j = 0; j < hunks.length; j++) {
				processHunk(hunks[j], diff, diffNode, tempFile, tempNode, patchedNode);
			}
			
			//update differencer
			diffNode.setKind(diff.getDiffType());
			
		} catch (CoreException e) {//ignore
		}
	}

	private void processHunk(Hunk hunk, Diff diff,PatcherDiffNode diffNode, IFile fileToPatch, ITypedElement resourceNode, PatchedFileNode patchedNode) {
		Diff tempDiff = new Diff(diff.fOldPath, diff.fOldDate, diff.fNewPath,diff.fNewDate);
		tempDiff.add(hunk);
		try {
			quickPatch(fileToPatch, workspacePatcher, tempDiff);
			int differencer = Differencer.NO_CHANGE;

			switch (hunk.getHunkType()) {
			case Hunk.ADDED:
				differencer += Differencer.ADDITION;
				break;

			case Hunk.CHANGED:
				differencer += Differencer.CHANGE;
				break;

			case Hunk.DELETED:
				differencer += Differencer.DELETION;
				break;
			}

			// only add a hunk to the node if it can't be patched
			if (failedHunks.size() != 0) {
				containsHunkErrors = true;
			/*	differencer += Differencer.CONFLICTING;
				String[] hunkContents = createInput(hunk);
				PatchedFileNode ancestor = new PatchedFileNode(	hunkContents[LEFT].getBytes(), hunk.fParent.getPath().getFileExtension(), hunk.getDescription());
				PatchedFileNode patchedNode2 = new PatchedFileNode(hunkContents[RIGHT].getBytes(), resourceNode.getType(),hunk.getDescription());
				//create the new hunk node
				new PatcherDiffNode(diffNode,differencer, ancestor, resourceNode, patchedNode2, hunk);
				*/
				String strippedHunk= stripContextFromHunk(hunk);
				PatchedFileNode strippedHunkNode = new HunkPatchedFileNode(strippedHunk.getBytes(),resourceNode.getType()/*"manualHunkMerge"*/, hunk.getDescription());
				PatchedFileWrapper patchedFileWrapper = new PatchedFileWrapper(patchedNode);
				//create ancestor
				String[] hunkContents = createInput(hunk);
				PatchedFileNode ancestor = new PatchedFileNode(	hunkContents[LEFT].getBytes(), hunk.fParent.getPath().getFileExtension(), hunk.getDescription());
				
				PatcherDiffNode parentNode = new PatcherDiffNode(diffNode, Differencer.CHANGE, ancestor, patchedFileWrapper,strippedHunkNode, hunk);
				patchedFileWrapper.addContentChangeListener(previewPatchPage);
				patchedFileWrapper.setParent(parentNode);
			}
		} catch (CoreException e) {//ignore
		}
	}

	/**
	 * Stores a pointer back to the PreviewPatchPage
	 * 
	 * @param page
	 */
	public void setPreviewPatchPage(PreviewPatchPage2 page) {
		previewPatchPage = page;
	}

	/**
	 * Makes sure that at least one hunk is checked off in the tree before
	 * allowing the patch to be applied.
	 */
	protected void updateEnablements() {
		boolean atLeastOneIsEnabled = false;
		if (viewer != null) {
			ITreeContentProvider contentProvider = (ITreeContentProvider) viewer
					.getContentProvider();
			Object[] projects = contentProvider.getElements(root);
			// Iterate through projects
			for (int j = 0; j < projects.length; j++) {
				if (!(projects[j] instanceof PatcherDiffNode)) {
					DiffNode project = (DiffNode) projects[j];
					// Iterate through project diffs
					Object[] diffs = project.getChildren();
					for (int i = 0; i < diffs.length; i++) {
						PatcherDiffNode diff = (PatcherDiffNode) diffs[i];
						atLeastOneIsEnabled = updateEnablement(
								atLeastOneIsEnabled, diff);
					}
				} else if (projects[j] instanceof PatcherDiffNode) {
					atLeastOneIsEnabled = updateEnablement(atLeastOneIsEnabled,
							(PatcherDiffNode) projects[j]);
				}
			}
		}

		previewPatchPage.setPageComplete(atLeastOneIsEnabled);
	}

	private boolean updateEnablement(boolean oneIsEnabled,
			PatcherDiffNode diffNode) {
		
		
		/*boolean checked = ((CheckboxDiffTreeViewer) viewer)
				.getChecked(diffNode);
		Diff diff = diffNode.getDiff();
		Assert.isNotNull(diff);
		diff.setEnabled(checked);
		if (checked) {
			Object[] hunkItems = diffNode.getChildren();
			for (int h = 0; h < hunkItems.length; h++) {
				PatcherDiffNode hunkNode = (PatcherDiffNode) hunkItems[h];
				checked = ((CheckboxDiffTreeViewer) viewer)
						.getChecked(hunkNode);
				Hunk hunk = hunkNode.getHunk();
				Assert.isNotNull(hunk);
				hunk.setEnabled(checked);
				if (checked) {
					// For workspace patch: before setting enabled flag, make
					// sure that the project
					// that contains this hunk actually exists in the workspace.
					// This is to guard against the
					//case of having a new file in a patch that is being applied to a project that
					//doesn't currently exist.
					boolean projectExists = true;
					DiffProject project = (DiffProject) diff.getParent(null);
					if (project != null) {
						projectExists = project.getProject().exists();
					}
					if (projectExists)
						oneIsEnabled = true;
				}

			}
		}
*/
		return true;
	}

	public boolean containsHunkErrors() {
		return containsHunkErrors;
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
					//don't add removed lines
					break;
				case '+' :
					result.append(rest);
					break;
			}
		}
		
		return result.toString();
	}
	
	public void addHunksToFile(IResource rpTargetResource, Hunk[] hunks) {
		Object obj = filesToDiffs.get(rpTargetResource);
		if (obj != null && obj instanceof Diff){
			for (int i = 0; i < hunks.length; i++) {
				hunks[i].setParent((Diff) obj);
				((Diff) obj).add(hunks[i]);
			}
		} else {
			//couldn't find file either create a new diff or new project etc.
			DiffProject diffProject = null;
			if (workspacePatcher.isWorkspacePatch()){
				//see if the project already exists
				IProject project = rpTargetResource.getProject();
				DiffProject[] diffProjects = workspacePatcher.getDiffProjects();
				for (int i = 0; i < diffProjects.length; i++) {
					if (diffProjects[i].getProject().equals(project)){
						diffProject = diffProjects[i];
						break;
					}
				}
				if (diffProject == null){
					//create a new diff project
					diffProject = new DiffProject(project);
					//add the diffProject to the array
					DiffProject[] newProjectArray = new DiffProject[diffProjects.length + 1];
					System.arraycopy(diffProjects, 0, newProjectArray, 0, diffProjects.length);
					//add the new project to the end
					newProjectArray[diffProjects.length] = diffProject;
					workspacePatcher.setDiffProjects(newProjectArray);
				}
			}
			
			//Create a new diff
			Diff tempDiff = new Diff(rpTargetResource.getProjectRelativePath(), 0, rpTargetResource.getProjectRelativePath(), 0);
			for (int i = 0; i < hunks.length; i++) {
				hunks[i].setParent(tempDiff);
				tempDiff.add(hunks[i]);
			}
			tempDiff.retargetDiff((IFile) rpTargetResource);
			//add diffProject is not null only in the workspace patch case
			if (diffProject != null){
				diffProject.addDiff(tempDiff);
				workspacePatcher.addDiff(tempDiff);
			} else {
				//not a workspace patch, add newly created diff to patcher
				workspacePatcher.addDiff(tempDiff);
			}
			
			filesToDiffs.put(rpTargetResource, tempDiff);				
		}
		
	}


}
