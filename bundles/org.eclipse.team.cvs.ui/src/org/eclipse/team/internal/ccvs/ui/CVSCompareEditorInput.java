package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.resources.CVSLocalSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;

/**
 * A compare input for performing naive comparisons between
 * resources and resource editions.
 */
public class CVSCompareEditorInput extends CompareEditorInput {
	private ITypedElement left;
	private ITypedElement right;
	private ITypedElement ancestor;	
	
	// comparison constants
	private static final int NODE_EQUAL = 0;
	private static final int NODE_NOT_EQUAL = 1;
	private static final int NODE_UNKNOWN = 2;
	
	/**
	 * Creates a new CVSCompareEditorInput.
	 */
	public CVSCompareEditorInput(ITypedElement left, ITypedElement right) {
		this(left, right, null);
	}
	
	/**
	 * Creates a new CVSCompareEditorInput.
	 */
	public CVSCompareEditorInput(ITypedElement left, ITypedElement right, ITypedElement ancestor) {
		super(new CompareConfiguration());
		this.left = left;
		this.right = right;
		this.ancestor = ancestor;
	}
	
	/**
	 * Overridden to create the CVSDiffTreeViewer to have the proper popup actions
	 */
	//public Viewer createDiffViewer(Composite parent) {
	//	return new CVSDiffTreeViewer(parent, this);
	//}

	/**
	 * Returns the label for the given input element.
	 */
	private String getLabel(ITypedElement element) {
		if (element instanceof ResourceNode) {
			return Policy.bind("VCMCompareEditorInput.workspace", new Object[] {element.getName()} );
		}
		if (element instanceof ResourceEditionNode) {
			ICVSRemoteResource edition = ((ResourceEditionNode)element).getRemoteResource();
			return edition.getName();
			/*if (edition.isTeamStreamResource()) {
				return Policy.bind("VCMCompareEditorInput.inStream", new Object[] {edition.getName(), edition.getTeamStream().getName()} );
			} else {
				return Policy.bind("VCMCompareEditorInput.repository", new Object[] {edition.getName(), edition.getVersionName()} );
			}*/
		}
		return element.getName();
	}
	
	/**
	 * Returns the label for the given input element.
	 */
	private String getVersionLabel(ITypedElement element) {
		if (element instanceof ResourceNode) {
			return Policy.bind("VCMCompareEditorInput.workspaceLabel");
		}
		if (element instanceof ResourceEditionNode) {
			ICVSRemoteResource edition = ((ResourceEditionNode)element).getRemoteResource();
			/*if (edition.isTeamStreamResource()) {
				return Policy.bind("VCMCompareEditorInput.streamLabel", new Object[] {edition.getTeamStream().getName()} );
			} else {
				return edition.getVersionName();
			}*/
			return edition.getName();
		}
		return element.getName();
	}
	
	/*
	 * Returns a guess of the resource name being compared, for display
	 * in the title.
	 */
	private String guessResourceName() {
		if (left != null) {
			return left.getName();
		}
		if (right != null) {
			return right.getName();
		}
		if (ancestor != null) {
			return ancestor.getName();
		}
		return "";
	}
	
	/**
	 * Handles a random exception and sanitizes it into a reasonable
	 * error message.  
	 */
	private void handle(Exception e) {
		// create a status
		Throwable t = e;
		// unwrap any invoc-target-exception
		if (t instanceof InvocationTargetException) {
			t = ((InvocationTargetException)t).getTargetException();
		}
		IStatus error;
		if (t instanceof CoreException) {
			error = ((CoreException)t).getStatus();
		} else {
			error = new Status(IStatus.ERROR, CVSUIPlugin.ID, 1, Policy.bind("internal"), t);
		}
		setMessage(error.getMessage());
		CVSUIPlugin.log(error);
	}
	
	/**
	 * Sets up the title and pane labels for the comparison view.
	 */
	private void initLabels() {
		CompareConfiguration cc = (CompareConfiguration) getCompareConfiguration();
	
		String leftLabel = getLabel(left);
		cc.setLeftLabel(leftLabel);
		cc.setLeftImage(left.getImage());
	
		String rightLabel = getLabel(right);
		cc.setRightLabel(rightLabel);
		cc.setRightImage(right.getImage());
	
		String title;
		if (ancestor != null) {
			cc.setAncestorLabel(getLabel(ancestor));
			cc.setAncestorImage(ancestor.getImage());
			title = Policy.bind("VCMCompareEditorInput.titleAncestor", new Object[] {guessResourceName(), getVersionLabel(ancestor), getVersionLabel(left), getVersionLabel(right)} );
		} else {
			String leftName = null;
			if (left != null) leftName = left.getName();
			String rightName = null;
			if (right != null) rightName = right.getName();
			boolean differentNames = false;
			if (leftName != null && !leftName.equals(rightName)) {
				title = Policy.bind("VCMCompareEditorInput.titleNoAncestorDifferent", new Object[] {leftName, getVersionLabel(left), rightName, getVersionLabel(right)} ); 
			} else {
				title = Policy.bind("VCMCompareEditorInput.titleNoAncestor", new Object[] {guessResourceName(), getVersionLabel(left), getVersionLabel(right)} );
			}
		}
		setTitle(title);
	}
	
	/* (Non-javadoc)
	 * Method declared on CompareEditorInput
	 */
	public boolean isSaveNeeded() {
		return false;
	}

	/* (non-Javadoc)
	 * Method declared on CompareEditorInput
	 */
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		boolean threeWay = ancestor != null;
		if (right == null || left == null) {
			setMessage(Policy.bind("VCMCompareEditorInput.different"));
			return null;
		}
		
		initLabels();
	
		Differencer d = new Differencer() {
			protected boolean contentsEqual(Object input1, Object input2) {
				int compare = teamEqual(input1, input2);
				if (compare == NODE_EQUAL) {
					return true;
				}
				if (compare == NODE_NOT_EQUAL) {
					return false;
				}
				//revert to slow content comparison
				return super.contentsEqual(input1, input2);
			}
			protected void updateProgress(IProgressMonitor progressMonitor, Object node) {
				super.updateProgress(progressMonitor, node);
				progressMonitor.worked(1);
			}
			protected Object[] getChildren(Object input) {
				if (input instanceof IStructureComparator) {
					Object[] children= ((IStructureComparator)input).getChildren();
					if (children != null)
						return children;
				}
				return null;
			}
		};
		
		try {
			monitor.beginTask(Policy.bind("VCMCompareEditorInput.comparing"), 30);
			
			// do the diff	
			IProgressMonitor sub = new SubProgressMonitor(monitor, 10);
			try {
				sub.beginTask(Policy.bind("VCMCompareEditorInput.comparing"), 100);
				return d.findDifferences(threeWay, sub, null, ancestor, left, right);
			} finally {
				sub.done();
			}
		} catch (OperationCanceledException e) {
			throw new InterruptedException(e.getMessage());
		} catch (RuntimeException e) {
			handle(e);
			return null;	
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Compares two nodes to determine if they are equal.  Returns NODE_EQUAL
	 * of they are the same, NODE_NOT_EQUAL if they are different, and
	 * NODE_UNKNOWN if comparison was not possible.
	 */
	protected int teamEqual(Object left, Object right) {
		ICVSRemoteResource leftEdition = null;
		if (left instanceof ResourceEditionNode) {
			leftEdition = ((ResourceEditionNode)left).getRemoteResource();
		} else if (left instanceof ResourceNode) {
			IResource resource = ((ResourceNode)left).getResource();
			// Hack
			CVSLocalSyncElement element = new CVSLocalSyncElement(resource, null);
			if (!element.isDirty()) {
				leftEdition = (ICVSRemoteResource)element.getBase();
			}
		}
		ICVSRemoteResource rightEdition = null;
		if (right instanceof ResourceEditionNode)
			rightEdition = ((ResourceEditionNode)right).getRemoteResource();
			
		if (leftEdition == null || rightEdition == null) {
			return NODE_UNKNOWN;
		}
		// if they're both non-files, they're the same
		if (leftEdition.isContainer() && rightEdition.isContainer()) {
			return NODE_EQUAL;
		}
		// if they have different types, they're different
		if (leftEdition.isContainer() != rightEdition.isContainer()) {
			return NODE_NOT_EQUAL;
		}
		
		if (!leftEdition.getRepository().getLocation().equals(rightEdition.getRepository().getLocation())) {
			return NODE_UNKNOWN;
		}
		// Non-API hack
		try {
			ResourceSyncInfo leftInfo = ((ICVSResource)leftEdition).getSyncInfo();
			ResourceSyncInfo rightInfo = ((ICVSResource)rightEdition).getSyncInfo();
			
			if (leftEdition.getRelativePath().equals(rightEdition.getRelativePath()) &&
				leftInfo.getRevision().equals(rightInfo.getRevision())) {
				return NODE_EQUAL;
			} else {
				// To do: If the branch tags are different, then force a content comparison.
				// Currently this case fails.
				return NODE_NOT_EQUAL;
			}
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			return NODE_UNKNOWN;
		}
	}
}