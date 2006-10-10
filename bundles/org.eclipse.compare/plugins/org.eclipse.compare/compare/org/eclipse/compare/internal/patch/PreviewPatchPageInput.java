package org.eclipse.compare.internal.patch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class PreviewPatchPageInput extends PatcherCompareEditorInput {

	protected PreviewPatchPage2 previewPatchPage;

	private boolean containsHunkErrors = false;

	protected void updateTree(WorkspacePatcher patcher) {
		if (viewer == null)
			return;

		int strip = previewPatchPage.getStripPrefixSegments();
		// Get the elements from the content provider
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer
				.getContentProvider();
		Object[] projects = contentProvider.getElements(root);
		ArrayList hunksToCheck = new ArrayList();
		ArrayList nodesToCheck = new ArrayList();
		// Iterate through projects and call reset on each project
		for (int j = 0; j < projects.length; j++) {
			if (!(projects[j] instanceof PatcherDiffNode)) {
				DiffNode projectNode = (DiffNode) projects[j];
				ITypedElement project = projectNode.getLeft();
				Assert.isNotNull(project);
				Assert.isTrue(project instanceof DiffProject);
				hunksToCheck.addAll(((DiffProject) project).reset(patcher,
						strip, previewPatchPage.getFuzzFactor()));
				IDiffElement[] diffNodes = projectNode.getChildren();

				Iterator iter = hunksToCheck.iterator();
				while (iter.hasNext()) {
					Hunk hunkToMatch = (Hunk) iter.next();
					Object matchingHunkNode = nodesToDiffs.get(hunkToMatch);
					if (matchingHunkNode != null)
						nodesToCheck.add(matchingHunkNode);

				}
				for (int i = 0; i < diffNodes.length; i++) {
					viewer.update(diffNodes[i], null);
					IDiffElement[] hunkNodes = ((PatcherDiffNode) diffNodes[i])
							.getChildren();
					for (int k = 0; k < hunkNodes.length; k++) {
						viewer.update(hunkNodes[k], null);
					}
				}

			} else {
				if (projects[j] instanceof PatcherDiffNode) {
					PatcherDiffNode diffNode = (PatcherDiffNode) projects[j];
					hunksToCheck.addAll(diffNode.getDiff().reset(patcher,
							strip, previewPatchPage.getFuzzFactor()));
					IDiffElement[] diffNodes = diffNode.getChildren();

					Iterator iter = hunksToCheck.iterator();
					while (iter.hasNext()) {
						Hunk hunkToMatch = (Hunk) iter.next();
						Object matchingHunkNode = nodesToDiffs.get(hunkToMatch);
						if (matchingHunkNode != null)
							nodesToCheck.add(matchingHunkNode);

					}
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
		}
		viewer.refresh();
		((CheckboxDiffTreeViewer) viewer).setCheckedElements(nodesToCheck
				.toArray());

		updateEnablements();
	}

	protected void buildTree(WorkspacePatcher patcher) {

		if (patcher.isWorkspacePatch()) {

			if (root.hasChildren()) {
				IDiffElement[] children = root.getChildren();
				for (int i = 0; i < children.length; i++) {
					root.remove(children[i]);
				}
			}

			nodesToDiffs = new HashMap();

			DiffProject[] projects = patcher.getDiffProjects();
			try {
				for (int i = 0; i < projects.length; i++) {
					DiffNode projectNode = new DiffNode(root,
							Differencer.CHANGE, null, projects[i], null);
					Iterator iter = projects[i].fDiffs.iterator();
					while (iter.hasNext()) {
						Object obj = iter.next();
						if (obj instanceof Diff) {
							Diff diff = (Diff) obj;
							IPath filePath = new Path(diff.getLabel(diff));
							IFile tempFile = projects[i].getFile(filePath);
							byte[] bytes = quickPatch(tempFile, patcher, diff);
							int differencer = Differencer.CHANGE;
							if (failedHunks.size() != 0) {
								differencer += Differencer.CONFLICTING;
							}

							ITypedElement tempNode;
							PatchedFileNode patchedNode;

							if (tempFile != null && tempFile.exists()) {
								tempNode = new ResourceNode(tempFile);
								patchedNode = new PatchedFileNode(bytes,
										tempNode.getType(), tempFile
												.getProjectRelativePath()
												.toString());
							} else {
								tempNode = new PatchedFileNode(
										new byte[0],
										filePath.getFileExtension(),
										PatchMessages.PatcherCompareEditorInput_FileNotFound);
								patchedNode = new PatchedFileNode(bytes,
										tempNode.getType(), ""); //$NON-NLS-1$
							}

							PatcherDiffNode allFile = new PatcherDiffNode(
									projectNode, differencer, tempNode,
									tempNode, patchedNode, diff);
							// Add individual hunks to each Diff node
							Hunk[] hunks = diff.getHunks();
							for (int j = 0; j < hunks.length; j++) {
								Diff tempDiff = new Diff(diff.fOldPath,
										diff.fOldDate, diff.fNewPath,
										diff.fNewDate);
								tempDiff.add(hunks[j]);
								bytes = quickPatch(tempFile, patcher, tempDiff);
								differencer = Differencer.NO_CHANGE;
								switch (hunks[j].getHunkType()) {
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

								if (failedHunks.size() != 0) {
									containsHunkErrors = true;
									differencer += Differencer.CONFLICTING;
									String[] hunkContents = createInput(hunks[j]);
									PatchedFileNode ancestor = new PatchedFileNode(
											hunkContents[LEFT].getBytes(),
											hunks[j].fParent.getPath()
													.getFileExtension(),
											hunks[j].getDescription());
									patchedNode = new PatchedFileNode(
											hunkContents[RIGHT].getBytes(),
											tempNode.getType(), hunks[j]
													.getDescription());
									PatcherDiffNode hunkNode = new PatcherDiffNode(
											allFile, differencer, ancestor,
											tempNode, patchedNode, hunks[j]);
									nodesToDiffs.put(hunks[j], hunkNode);
								} else {
									patchedNode = new PatchedFileNode(bytes,
											tempNode.getType(), hunks[j]
													.getDescription());
									PatcherDiffNode hunkNode = new PatcherDiffNode(
											allFile, differencer, tempNode,
											tempNode, patchedNode, hunks[j]);
									nodesToDiffs.put(hunks[j], hunkNode);
								}
							}

						}

					}

				}

			} catch (CoreException e) {
				// ignore
			}
			viewer.setInput(root);
			viewer.refresh();
		} else {
			if (root.hasChildren()) {
				IDiffElement[] children = root.getChildren();
				for (int i = 0; i < children.length; i++) {
					root.remove(children[i]);
				}
			}

			nodesToDiffs = new HashMap();

			Diff[] diffs = patcher.getDiffs();
			try {
				for (int i = 0; i < diffs.length; i++) {
					Diff diff = diffs[i];
					IPath filePath = new Path(diff.getLabel(diff));
					IFile tempFile = patcher.existsInTarget(filePath);

					byte[] bytes = quickPatch(tempFile, patcher, diff);
					int differencer = Differencer.CHANGE;
					if (failedHunks.size() != 0) {
						differencer += Differencer.CONFLICTING;
					}

					ITypedElement tempNode;
					PatchedFileNode patchedNode;

					if (tempFile != null && tempFile.exists()) {
						tempNode = new ResourceNode(tempFile);
						patchedNode = new PatchedFileNode(bytes, tempNode
								.getType(), tempFile.getProjectRelativePath()
								.toString());
					} else {
						tempNode = new PatchedFileNode(
								new byte[0],
								filePath.getFileExtension(),
								PatchMessages.PatcherCompareEditorInput_FileNotFound);
						patchedNode = new PatchedFileNode(bytes, tempNode
								.getType(), ""); //$NON-NLS-1$
					}

					PatcherDiffNode allFile = new PatcherDiffNode(root,
							differencer, tempNode, tempNode, patchedNode, diff);
					// Add individual hunks to each Diff node
					Hunk[] hunks = diff.getHunks();
					for (int j = 0; j < hunks.length; j++) {
						Diff tempDiff = new Diff(diff.fOldPath, diff.fOldDate,
								diff.fNewPath, diff.fNewDate);
						tempDiff.add(hunks[j]);
						bytes = quickPatch(tempFile, patcher, tempDiff);
						differencer = Differencer.NO_CHANGE;
						switch (hunks[j].getHunkType()) {
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

						if (failedHunks.size() != 0) {
							containsHunkErrors = true;
							differencer += Differencer.CONFLICTING;
							String[] hunkContents = createInput(hunks[j]);
							PatchedFileNode ancestor = new PatchedFileNode(
									hunkContents[LEFT].getBytes(),
									hunks[j].fParent.getPath()
											.getFileExtension(), hunks[j]
											.getDescription());
							patchedNode = new PatchedFileNode(
									hunkContents[RIGHT].getBytes(), tempNode
											.getType(), hunks[j]
											.getDescription());
							PatcherDiffNode hunkNode = new PatcherDiffNode(
									allFile, differencer, ancestor, tempNode,
									patchedNode, hunks[j]);
							nodesToDiffs.put(hunks[j], hunkNode);
						} else {
							patchedNode = new PatchedFileNode(bytes, tempNode
									.getType(), hunks[j].getDescription());
							PatcherDiffNode hunkNode = new PatcherDiffNode(
									allFile, differencer, tempNode, tempNode,
									patchedNode, hunks[j]);
							nodesToDiffs.put(hunks[j], hunkNode);
						}
					}

				}
			} catch (CoreException ex) {// ignore
			}

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
		boolean checked = ((CheckboxDiffTreeViewer) viewer)
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

		return oneIsEnabled;
	}

	public boolean containsHunkErrors() {
		return containsHunkErrors;
	}

}
