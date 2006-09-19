package org.eclipse.compare.internal.patch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.DiffImage;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class PatcherCompareEditorInput extends CompareEditorInput {

	class PatchedFileNode implements ITypedElement, IStreamContentAccessor {

		byte[] bytes;
		String type;
		String name;
		
		
		public PatchedFileNode(byte[] bytes, String type, String name){
			this.bytes = bytes;
			this.type = type;
			this.name = name;
		}
		
		public Image getImage() {
			return null;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(bytes);
		}
		
	}

	class MyDiffNode extends DiffNode {
		
		//Diff associated with this MyDiffNode
		private Diff diff = null;
		//Hunk associated with this MyDiffNode
		private Hunk hunk = null;
		

		public MyDiffNode(IDiffContainer parent, int kind, ITypedElement ancestor, ITypedElement left, ITypedElement right, Diff diff) {
			super(parent, kind, ancestor, left, right);
			this.diff = diff;
		}
		
		public MyDiffNode(IDiffContainer parent, int kind, ITypedElement ancestor, ITypedElement left, ITypedElement right, Hunk hunk) {
			super(parent, kind, ancestor, left, right);
			this.hunk = hunk;
		}

		public String getName() {
			if (diff != null)
				return diff.getLabel(diff);
			
			if (hunk != null)
				return hunk.getLabel(hunk);
			
			return ""; //$NON-NLS-1$
		}

		Diff getDiff() {
			return diff;
		}


		Hunk getHunk() {
			return hunk;
		}

	}
	
	class PatcherCompareEditorDecorator implements ILabelDecorator {

		/** Maps strings to images */
		private Map fImages= new Hashtable(10);
		private List fDisposeOnShutdownImages= new ArrayList();

		ImageDescriptor errId= CompareUIPlugin.getImageDescriptor("ovr16/error_ov.gif");	//$NON-NLS-1$
		
		static final String error = "error"; //$NON-NLS-1$
		static final String add = "add"; //$NON-NLS-1$
		static final String delete = "del"; //$NON-NLS-1$
		
		public Image decorateImage(Image image, Object element) {
			if (element instanceof MyDiffNode){
				MyDiffNode myDiffNode = (MyDiffNode) element;
				Diff diff = myDiffNode.getDiff();
				Hunk hunk = myDiffNode.getHunk();
				if (diff != null){
				  switch (diff.getType()){
					  case Differencer.ADDITION:
					  return getImageFor(add + (diff.fMatches ? "" : error), image, diff.fMatches); //$NON-NLS-1$
				
					  case Differencer.DELETION:
					  return getImageFor(delete + (diff.fMatches ? "" : error), image, diff.fMatches); //$NON-NLS-1$
					  
					  default:
					  return getImageFor(diff.fMatches ? "" : error, image, diff.fMatches); //$NON-NLS-1$
				  }
				} else if (hunk != null){
					return getImageFor((hunk.fMatches ? "" : error),image, hunk.fMatches); //$NON-NLS-1$
				}
			}
			return null;
		}

		private Image getImageFor(String id, Image image, boolean hasMatches) {
			Image cached_image = (Image) fImages.get(id);
			if (cached_image == null){
				DiffImage diffImage = new DiffImage(image, hasMatches ? null : errId, 16, false);
				cached_image = diffImage.createImage();
				fImages.put(id, cached_image);
				fDisposeOnShutdownImages.add(cached_image);
			}
			return cached_image;
		}

		public String decorateText(String text, Object element) {
			if (element instanceof DiffNode){	
				ITypedElement typedElement = ((DiffNode) element).getLeft();
				if (typedElement != null && typedElement instanceof DiffProject){
					DiffProject project = (DiffProject) typedElement;
					if (!project.getName().equals(project.getOriginalProjectName()))	
						return NLS.bind(PatchMessages.Diff_2Args, 
								new String[]{project.getOriginalProjectName(),
								NLS.bind(PatchMessages.PreviewPatchPage_Target, new String[]{project.getName()})});
				}
			} 
			
			return null;
		}

		public void dispose() {
			if (fDisposeOnShutdownImages != null) {
				Iterator i= fDisposeOnShutdownImages.iterator();
				while (i.hasNext()) {
					Image img= (Image) i.next();
					if (!img.isDisposed())
						img.dispose();
				}
				fImages= null;
			}
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void addListener(ILabelProviderListener listener) {
			//don't need listener	
		}
		
		public void removeListener(ILabelProviderListener listener) {
			//don't need listener
		} 
		
	}
	private DiffNode root;
	private List failedHunks;
	
	private CheckboxDiffTreeViewer viewer;
	private final static int LEFT = 0;
	private final static int RIGHT = 1;
	private PreviewPatchPage2 previewPatchPage;
	
	private HashMap nodesToDiffs;
	private HashMap contributedActions;
	
	public PatcherCompareEditorInput() {
		super(new CompareConfiguration());
		root = new DiffNode(Differencer.NO_CHANGE) {
			public boolean hasChildren() {
				return true;
			}
		};
	}

	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		initLabels();
		return root;
	}
	
	private void initLabels() {
		CompareConfiguration cc = getCompareConfiguration();
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
		String leftLabel = PatchMessages.PatcherCompareEditorInput_LocalCopy;
		cc.setLeftLabel(leftLabel);
		String rightLabel = PatchMessages.PatcherCompareEditorInput_AfterPatch;
		cc.setRightLabel(rightLabel);
	}

	public void updateInput(WorkspacePatcher patcher) {
		buildTree(patcher);
		updateTree(patcher);
	}
	
	private void updateTree(WorkspacePatcher patcher){
		if (viewer == null)
			return;
		
		int strip= previewPatchPage.getStripPrefixSegments();
		//Get the elements from the content provider
		ITreeContentProvider contentProvider= (ITreeContentProvider) viewer.getContentProvider();
		Object[] projects= contentProvider.getElements(root);
		ArrayList hunksToCheck= new ArrayList();
		ArrayList nodesToCheck=new ArrayList();
		//Iterate through projects and call reset on each project
		for (int j= 0; j<projects.length; j++) {
			if (!(projects[j] instanceof MyDiffNode)) {
				DiffNode projectNode = (DiffNode) projects[j];
				ITypedElement project = projectNode.getLeft();
				Assert.isNotNull(project);
				Assert.isTrue(project instanceof DiffProject);
				hunksToCheck.addAll(((DiffProject)project).reset(patcher, strip, previewPatchPage.getFuzzFactor()));
				IDiffElement[] diffNodes = projectNode.getChildren();
				
				Iterator iter = hunksToCheck.iterator();
				while (iter.hasNext()){
					Hunk hunkToMatch = (Hunk) iter.next();
					Object matchingHunkNode = nodesToDiffs.get(hunkToMatch);
					if (matchingHunkNode != null)
						nodesToCheck.add(matchingHunkNode);
					
				}
				for (int i = 0; i < diffNodes.length; i++) {
					viewer.update(diffNodes[i], null);
					IDiffElement[] hunkNodes =((MyDiffNode) diffNodes[i]).getChildren();
					for (int k = 0; k < hunkNodes.length; k++) {
						viewer.update(hunkNodes[k],null);
					}
				}
				
			} else {
				if (projects[j] instanceof MyDiffNode) {
					MyDiffNode diffNode = (MyDiffNode) projects[j];
					hunksToCheck.addAll(diffNode.getDiff().reset(patcher, strip, previewPatchPage.getFuzzFactor()));
					IDiffElement[] diffNodes = diffNode.getChildren();
					
					Iterator iter = hunksToCheck.iterator();
					while (iter.hasNext()){
						Hunk hunkToMatch = (Hunk) iter.next();
						Object matchingHunkNode = nodesToDiffs.get(hunkToMatch);
						if (matchingHunkNode != null)
							nodesToCheck.add(matchingHunkNode);
						
					}
					for (int i = 0; i < diffNodes.length; i++) {
						viewer.update(diffNodes[i], null);
						IDiffElement[] hunkNodes =((MyDiffNode) diffNodes[i]).getChildren();
						for (int k = 0; k < hunkNodes.length; k++) {
							viewer.update(hunkNodes[k],null);
						}
					}
				}
			}
		}
		viewer.refresh();
		viewer.setCheckedElements(nodesToCheck.toArray());
	
		updateEnablements();
	}
	
	private void buildTree(WorkspacePatcher patcher) {

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
					DiffNode projectNode = new DiffNode(root, Differencer.CHANGE, null, projects[i], null);
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
							
							if (tempFile != null && tempFile.exists()){
								tempNode = new ResourceNode(tempFile);
								patchedNode = new PatchedFileNode(bytes, tempNode.getType(), tempFile.getProjectRelativePath().toString());
							}
							else{ 
								tempNode = new PatchedFileNode(new byte[0], filePath.getFileExtension(), PatchMessages.PatcherCompareEditorInput_FileNotFound);
								patchedNode = new PatchedFileNode(bytes, tempNode.getType(), ""); //$NON-NLS-1$
							}
						
							MyDiffNode allFile = new MyDiffNode(projectNode, differencer, tempNode, tempNode, patchedNode, diff);
							//Add individual hunks to each Diff node
							Hunk[] hunks = diff.getHunks();
							for (int j = 0; j < hunks.length; j++) {
								Diff tempDiff = new Diff(diff.fOldPath, diff.fOldDate, diff.fNewPath, diff.fNewDate);
								tempDiff.add(hunks[j]);
								bytes = quickPatch(tempFile, patcher, tempDiff);
								differencer = Differencer.NO_CHANGE;
								switch (hunks[j].getHunkType()) {
									case Hunk.ADDED :
										differencer += Differencer.ADDITION;
										break;

									case Hunk.CHANGED :
										differencer += Differencer.CHANGE;
										break;

									case Hunk.DELETED :
										differencer += Differencer.DELETION;
										break;
								}

								if (failedHunks.size() != 0) {
									differencer += Differencer.CONFLICTING;
									String[] hunkContents = createInput(hunks[j]);
									PatchedFileNode ancestor = new PatchedFileNode(hunkContents[LEFT].getBytes(), hunks[j].fParent.getPath().getFileExtension(), hunks[j].getDescription());
									patchedNode = new PatchedFileNode(hunkContents[RIGHT].getBytes(), tempNode.getType(), hunks[j].getDescription());
									MyDiffNode hunkNode = new MyDiffNode(allFile, differencer, ancestor, tempNode, patchedNode, hunks[j]);
									nodesToDiffs.put(hunks[j], hunkNode);
								} else {
									patchedNode = new PatchedFileNode(bytes, tempNode.getType(), hunks[j].getDescription());
									MyDiffNode hunkNode = new MyDiffNode(allFile, differencer, tempNode, tempNode, patchedNode, hunks[j]);
									nodesToDiffs.put(hunks[j], hunkNode);
								}
							}

						}

					}
				
				}

			} catch (CoreException e) {
				//ignore
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
					
					if (tempFile != null && tempFile.exists()){
						tempNode = new ResourceNode(tempFile);
						patchedNode = new PatchedFileNode(bytes, tempNode.getType(), tempFile.getProjectRelativePath().toString());
					}
					else{ 
						tempNode = new PatchedFileNode(new byte[0], filePath.getFileExtension(), PatchMessages.PatcherCompareEditorInput_FileNotFound);
						patchedNode = new PatchedFileNode(bytes, tempNode.getType(), ""); //$NON-NLS-1$
					}
					
					MyDiffNode allFile = new MyDiffNode(root, differencer, tempNode, tempNode, patchedNode, diff);
					//Add individual hunks to each Diff node
					Hunk[] hunks = diff.getHunks();
					for (int j = 0; j < hunks.length; j++) {
						Diff tempDiff = new Diff(diff.fOldPath, diff.fOldDate, diff.fNewPath, diff.fNewDate);
						tempDiff.add(hunks[j]);
						bytes = quickPatch(tempFile, patcher, tempDiff);
						differencer = Differencer.NO_CHANGE;
						switch (hunks[j].getHunkType()) {
							case Hunk.ADDED :
								differencer += Differencer.ADDITION;
								break;

							case Hunk.CHANGED :
								differencer += Differencer.CHANGE;
								break;

							case Hunk.DELETED :
								differencer += Differencer.DELETION;
								break;
						}

						if (failedHunks.size() != 0) {
							differencer += Differencer.CONFLICTING;
							String[] hunkContents = createInput(hunks[j]);
							PatchedFileNode ancestor = new PatchedFileNode(hunkContents[LEFT].getBytes(), hunks[j].fParent.getPath().getFileExtension(), hunks[j].getDescription());
							patchedNode = new PatchedFileNode(hunkContents[RIGHT].getBytes(), tempNode.getType(), hunks[j].getDescription());
							MyDiffNode hunkNode = new MyDiffNode(allFile, differencer, ancestor, tempNode, patchedNode, hunks[j]);
							nodesToDiffs.put(hunks[j], hunkNode);
						} else {
							patchedNode = new PatchedFileNode(bytes, tempNode.getType(), hunks[j].getDescription());
							MyDiffNode hunkNode = new MyDiffNode(allFile, differencer, tempNode, tempNode, patchedNode, hunks[j]);
							nodesToDiffs.put(hunks[j], hunkNode);
						}
					}

				}
			} catch (CoreException ex) {//ignore
			}

		}

	}
	
	private byte[] quickPatch(IFile tempFile, WorkspacePatcher patcher, Diff diff) throws CoreException {
			
			failedHunks = new ArrayList();
			List result = patcher.apply(diff, tempFile, diff.getType() == Differencer.ADDITION, failedHunks);
			String patchedResults = patcher.createString(result);
			byte[] bytes;
			try {
				bytes = patchedResults.getBytes(Utilities.getCharset(tempFile));
			} catch (UnsupportedEncodingException e) {
				// uses default encoding
				bytes = patchedResults.getBytes();
			}
		
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#createDiffViewer(org.eclipse.swt.widgets.Composite)
	 */
	public Viewer createDiffViewer(Composite parent) {
		viewer =  new CheckboxDiffTreeViewer(parent, getCompareConfiguration());
		viewer.setLabelDecorator(new PatcherCompareEditorDecorator());
		viewer.getTree().setData(CompareUI.COMPARE_VIEWER_TITLE, PatchMessages.PatcherCompareEditorInput_PatchContents);
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				viewer.setSubtreeChecked(event.getElement(),event.getChecked());
				updateEnablements();
			}
		});
		viewer.setInput(this);
		return viewer;
	}
	
	private String[] createInput(Hunk hunk) {

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

		
		return new String[]{left.toString(),right.toString()};
	}
	
	public void contributeDiffViewerToolbarItems(Action[] actions, boolean workspacePatch){
		ToolBarManager tbm= CompareViewerPane.getToolBarManager(viewer.getControl().getParent());
		contributedActions = new HashMap();
		if (tbm != null) {
			tbm.removeAll();
			
			tbm.add(new Separator("contributed")); //$NON-NLS-1$
			
			for (int i = 0; i < actions.length; i++) {
				contributedActions.put(actions[i].getId(), actions[i]);
				tbm.appendToGroup("contributed", actions[i]); //$NON-NLS-1$
			}
			
			tbm.update(true);
		}
	}
	
	public CheckboxDiffTreeViewer getViewer() {
		return viewer;
	}
	
	/**
	 * Makes sure that at least one hunk is checked off in the tree before
	 * allowing the patch to be applied.
	 */
	/* private */void updateEnablements() {
		boolean atLeastOneIsEnabled= false;
		if (viewer!=null) {
			ITreeContentProvider contentProvider= (ITreeContentProvider) viewer.getContentProvider();
			Object[] projects= contentProvider.getElements(root);
			//Iterate through projects
			for (int j= 0; j<projects.length; j++) {
				if (!(projects[j] instanceof MyDiffNode)) {
					DiffNode project = (DiffNode) projects[j];
					//Iterate through project diffs
					Object[] diffs= project.getChildren();
					for (int i= 0; i<diffs.length; i++) {
						MyDiffNode diff= (MyDiffNode) diffs[i];
						atLeastOneIsEnabled= updateEnablement(atLeastOneIsEnabled, diff);
					}
				} else if (projects[j] instanceof MyDiffNode) {
					atLeastOneIsEnabled= updateEnablement(atLeastOneIsEnabled, (MyDiffNode) projects[j]);
				}
			}
		}

		previewPatchPage.setPageComplete(atLeastOneIsEnabled);
	}

	private boolean updateEnablement(boolean oneIsEnabled, MyDiffNode diffNode) {
		boolean checked= viewer.getChecked(diffNode);
		Diff diff = diffNode.getDiff();
		Assert.isNotNull(diff);
		diff.setEnabled(checked);
		if (checked) {
			Object[] hunkItems= diffNode.getChildren();
			for (int h= 0; h<hunkItems.length; h++) {
				MyDiffNode hunkNode = (MyDiffNode) hunkItems[h];
				checked= viewer.getChecked(hunkNode);
				Hunk hunk= hunkNode.getHunk();
				Assert.isNotNull(hunk);
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
						oneIsEnabled= true;
				}

			}
		}
	
		return oneIsEnabled;
	}
	

	/**
	 * Stores a pointer back to the PreviewPatchPage
	 * @param page
	 */
	public void setPreviewPatchPage(PreviewPatchPage2 page) {
		previewPatchPage = page;
	}
	
	public void setContributedActionEnablement(String actionID, boolean enabled){
		Object obj = contributedActions.get(actionID);
		if (obj != null && obj instanceof Action){
			((Action) obj).setEnabled(enabled);
		}
	}
}
