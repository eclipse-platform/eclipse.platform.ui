package org.eclipse.compare.internal.patch;

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
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.DiffImage;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public abstract class PatcherCompareEditorInput extends CompareEditorInput {

	class PatcherCompareEditorDecorator implements ILabelDecorator {

		/** Maps strings to images */
		private Map fImages= new Hashtable(10);
		private List fDisposeOnShutdownImages= new ArrayList();

		ImageDescriptor errId= CompareUIPlugin.getImageDescriptor("ovr16/error_ov.gif");	//$NON-NLS-1$
		
		static final String error = "error"; //$NON-NLS-1$
		static final String add = "add"; //$NON-NLS-1$
		static final String delete = "del"; //$NON-NLS-1$
		
		public Image decorateImage(Image image, Object element) {
			if (element instanceof PatcherDiffNode){
				PatcherDiffNode myDiffNode = (PatcherDiffNode) element;
				Diff diff = myDiffNode.getDiff();
				Hunk hunk = myDiffNode.getHunk();
				if (diff != null){
				  switch (diff.getDiffType()){
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
	protected DiffNode root;
	protected List failedHunks;
	
	protected TreeViewer viewer;
	protected final static int LEFT = 0;
	protected final static int RIGHT = 1;

	
	protected HashMap nodesToDiffs;
	protected HashMap contributedActions;
	
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
	
	abstract protected void updateTree(WorkspacePatcher patcher);
	
	abstract protected void buildTree(WorkspacePatcher patcher); 
	
	public byte[] quickPatch(IFile tempFile, WorkspacePatcher patcher, Diff diff) throws CoreException {
			
			failedHunks = new ArrayList();
			List result = patcher.apply(diff, tempFile, diff.getDiffType() == Differencer.ADDITION, failedHunks);
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
		((CheckboxDiffTreeViewer)viewer).setLabelDecorator(new PatcherCompareEditorDecorator());
		((CheckboxDiffTreeViewer)viewer).getTree().setData(CompareUI.COMPARE_VIEWER_TITLE, PatchMessages.PatcherCompareEditorInput_PatchContents);
		((CheckboxDiffTreeViewer)viewer).addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				((CheckboxDiffTreeViewer)viewer).setSubtreeChecked(event.getElement(),event.getChecked());
				updateEnablements();
			}
		});
		((CheckboxDiffTreeViewer)viewer).setInput(this);
		return viewer;
	}
	
	/**
	 * Called after an element gets checked off in the tree viewer. Subclasses should
	 * determine whether the page could be marked as complete and set the page complete
	 * based on the current state of the tree.
	 */
	abstract protected void updateEnablements();

	protected String[] createInput(Hunk hunk) {

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
	
	public TreeViewer getViewer() {
		return viewer;
	}
	
	
	
	public void setContributedActionEnablement(String actionID, boolean enabled){
		Object obj = contributedActions.get(actionID);
		if (obj != null && obj instanceof Action){
			((Action) obj).setEnabled(enabled);
		}
	}

	public DiffNode getRoot() {
		return root;
	}
}
