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
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.DiffImage;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
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
			if (element instanceof PatcherDiffNode){
				if (!((PatcherDiffNode) element).getIncludeElement()){
					 return NLS.bind(PatchMessages.Diff_2Args, 
							new String[]{text, PatchMessages.PatcherCompareEditorInput_NotIncluded});
				}
			}
			
			if (element instanceof DiffNode){	
				ITypedElement typedElement = ((DiffNode) element).getLeft();
				if (typedElement != null && typedElement instanceof DiffProject){
					DiffProject project = (DiffProject)typedElement;
					//Check to see if this project exists in the workspace
					IResource projectExistsInWorkspace = ResourcesPlugin.getWorkspace().getRoot().findMember(project.getProject().getFullPath());
					if(projectExistsInWorkspace == null){
						project.setEnabled(false);
						return NLS.bind(PatchMessages.Diff_2Args, new String[]{text, PatchMessages.PreviewPatchLabelDecorator_ProjectDoesNotExist});
					}
					
					if (!project.getName().equals(project.getOriginalProjectName()))	
						return NLS.bind(PatchMessages.Diff_2Args, 
								new String[]{project.getOriginalProjectName(),
								NLS.bind(PatchMessages.PreviewPatchPage_Target, new String[]{project.getName()})});
				}
				
				if (typedElement != null && typedElement instanceof Diff){
					Diff diff = (Diff) typedElement;
					if (diff.isRetargeted())	
						return NLS.bind(PatchMessages.Diff_2Args, 
								new String[]{diff.getOriginalName(),
								NLS.bind(PatchMessages.PreviewPatchPage_Target, new String[]{diff.getName()})});
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
				//fImages= null;
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
	
	class PatcherCompareEditorLabelProvider extends LabelProvider {
		
		public String getText(Object element) {
		
			if (element instanceof IDiffElement)
				return ((IDiffElement)element).getName();
			
			
			return "No name"; //$NON-NLS-1$
		}
	
		public Image getImage(Object element) {
			if (element instanceof IDiffElement) {
				IDiffElement input= (IDiffElement) element;	
				return input.getImage();
			}
			return null;
		}
	}
	
	protected DiffNode root;
	protected List failedHunks;
	
	protected TreeViewer viewer;
	protected final static int LEFT = 0;
	protected final static int RIGHT = 1;

	
	protected HashMap nodesToDiffs;
	protected HashMap contributedActions;
	protected HashMap contributedMenuActions;
	
	protected CompareConfiguration config;
	
	protected WorkspacePatcher workspacePatcher;
	private Action[] fContributedMenuActions;
	
	public PatcherCompareEditorInput() {
		super(new CompareConfiguration());
		root = new DiffNode(Differencer.NO_CHANGE) {
			public boolean hasChildren() {
				return true;
			}
		};
	}
	
	/**
	 * Creates a new PatchCompareEditorInput and makes use of the passed in CompareConfiguration
	 * to configure the UI elements.
	 * @param config
	 */
	public PatcherCompareEditorInput(CompareConfiguration config) {
		this();
		this.config = config;
	}


	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		initLabels();
		return root;
	}
	
	private void initLabels() {
		CompareConfiguration cc = getCompareConfiguration();
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
		cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
		if (config != null){
			cc.setLeftLabel(config.getLeftLabel(config));
			cc.setLeftImage(config.getLeftImage(config));
			cc.setRightLabel(config.getRightLabel(config));
			cc.setRightImage(config.getRightImage(config));
		} else {
			String leftLabel = PatchMessages.PatcherCompareEditorInput_LocalCopy;
			cc.setLeftLabel(leftLabel);
			String rightLabel = PatchMessages.PatcherCompareEditorInput_AfterPatch;
			cc.setRightLabel(rightLabel);
		}
	}

	public void updateInput(WorkspacePatcher patcher) {
		this.workspacePatcher = patcher;
		buildTree(patcher);
		updateTree();
	}
	
	abstract protected void updateTree();
	
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
		viewer =  new DiffTreeViewer(parent, getCompareConfiguration()){
				protected void fillContextMenu(IMenuManager manager) {
					int actionLength = fContributedMenuActions.length;
					if (actionLength > 0){
						for (int i = 0; i < actionLength; i++) {
							manager.add(fContributedMenuActions[i]);
						}
					}
				}
			};
			
		IBaseLabelProvider labelProvider = ((DiffTreeViewer) viewer).getLabelProvider();
		if (labelProvider instanceof ILabelProvider){
			((DiffTreeViewer)viewer).setLabelProvider(new DecoratingLabelProvider(new PatcherCompareEditorLabelProvider(), new PatcherCompareEditorDecorator()));
		}
		((DiffTreeViewer)viewer).getTree().setData(CompareUI.COMPARE_VIEWER_TITLE, PatchMessages.PatcherCompareEditorInput_PatchContents);
		((DiffTreeViewer)viewer).setInput(this);
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
	
	public void setContributedActionName(String actionID, String name){
		Object obj = contributedActions.get(actionID);
		if (obj != null && obj instanceof Action){
			((Action) obj).setText(name);
		}
	}
	
	public void contributeDiffViewerMenuItems(Action[] menuActions) {
		fContributedMenuActions = new Action[menuActions.length];
		System.arraycopy(menuActions, 0, fContributedMenuActions, 0, fContributedMenuActions.length);
		//also add actions to the enablement listener
		contributedActions = new HashMap();
		for (int i = 0; i < menuActions.length; i++) 
				contributedActions.put(menuActions[i].getId(), menuActions[i]);
	}
	
	public DiffNode getRoot() {
		return root;
	}
	
	public void resetRoot() {
		root = new DiffNode(Differencer.NO_CHANGE) {
			public boolean hasChildren() {
				return true;
			}
		};
	}
}
