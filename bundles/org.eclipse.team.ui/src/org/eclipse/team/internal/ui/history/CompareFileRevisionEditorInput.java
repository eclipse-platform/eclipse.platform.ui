package org.eclipse.team.internal.ui.history;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;


public class CompareFileRevisionEditorInput extends CompareEditorInput {

	private ITypedElement left;
	private ITypedElement right;
	private ITypedElement ancestor;	
	private Image leftImage;
	private Image rightImage;
	private Image ancestorImage;
	
	// comparison constants
	private static final int NODE_EQUAL = 0;
	private static final int NODE_NOT_EQUAL = 1;
	private static final int NODE_UNKNOWN = 2;
	
	String toolTipText;
    private String title;
	
	/**
	 * Creates a new CompareFileRevisionEditorInput.
	 */
	public CompareFileRevisionEditorInput(RevisionEditionNode left, RevisionEditionNode right) {
		this(left, right, null);
	}
	
	public CompareFileRevisionEditorInput(String title, String toolTip, RevisionEditionNode left, RevisionEditionNode right) {
		this(left, right, null);
		this.title = title;
		this.toolTipText = toolTip;
	}
	
	/**
	 * Creates a new CVSCompareEditorInput.
	 */
	public CompareFileRevisionEditorInput(RevisionEditionNode left, RevisionEditionNode right, RevisionEditionNode ancestor) {
		super(new CompareConfiguration());
		// TODO: Invokers of this method should ensure that trees and contents are prefetched
		this.left = left;
		this.right = right;
		this.ancestor = ancestor;
		if (left != null) {
			this.leftImage = left.getImage();
		}
		if (right != null) {
			this.rightImage = right.getImage();
		}
		if (ancestor != null) {
			this.ancestorImage = ancestor.getImage();
		}
	}
	
	/**
	 * Returns the label for the given input element.
	 */
	private String getLabel(ITypedElement element) {
		if (element instanceof RevisionEditionNode) {
			IFileRevision revision = ((RevisionEditionNode)element).getFileRevision();
			return NLS.bind(TeamUIMessages.nameAndRevision, new String[]{revision.getName(), revision.getContentIndentifier()});
		}
		return element.getName();
	}
	
	/**
	 * Returns the label for the given input element.
	 */
	private String getVersionLabel(ITypedElement element) {
		if (element instanceof RevisionEditionNode) {
			IFileRevision revision = ((RevisionEditionNode)element).getFileRevision();
			//try {
				return revision.getContentIndentifier();
			/*} catch (TeamException e) {
				handle(e);
				// Fall through and get the default label
			}*/
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
		return ""; //$NON-NLS-1$
	}
	
	/*
	 * Returns a guess of the resource path being compared, for display
	 * in the tooltip.
	 */
	private Object guessResourcePath() {
		/*if (left != null && left instanceof RevisionEditionNode) {
			return ((RevisionEditionNode)left).getRemoteResource().getRepositoryRelativePath();
		}
		if (right != null && right instanceof RevisionEditionNode) {
			return ((RevisionEditionNode)right).getRemoteResource().getRepositoryRelativePath();
		}
		if (ancestor != null && ancestor instanceof RevisionEditionNode) {
			return ((RevisionEditionNode)ancestor).getRemoteResource().getRepositoryRelativePath();
		}*/
		return guessResourceName();
	}
	
	/**
	 * Handles a random exception and sanitizes it into a reasonable
	 * error message.  
	 */
	private void handle(Exception e) {
		// create a status
		Throwable t = e;
		// unwrap the invocation target exception
		if (t instanceof InvocationTargetException) {
			t = ((InvocationTargetException)t).getTargetException();
		}
		IStatus error;
		if (t instanceof CoreException) {
			error = ((CoreException)t).getStatus();
		} else if (t instanceof TeamException) {
			error = ((TeamException)t).getStatus();
		} else {
			error = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, TeamUIMessages.internal, t); 
		}
		setMessage(error.getMessage());
		if (!(t instanceof TeamException)) {
			TeamUIPlugin.log(error.getSeverity(), error.getMessage(), t);
		}
	}
	
	/**
	 * Sets up the title and pane labels for the comparison view.
	 */
	private void initLabels() {
		CompareConfiguration cc = getCompareConfiguration();
		setLabels(cc, new StructuredSelection());
		
		if (title == null) {
			if (ancestor != null) {
				title = NLS.bind(TeamUIMessages.TeamCompareEditorInput_titleAncestor, (new Object[] {guessResourceName(), getVersionLabel(ancestor), getVersionLabel(left), getVersionLabel(right)})); 
				toolTipText = NLS.bind(TeamUIMessages.TeamCompareEditorInput_titleAncestor, (new Object[] {guessResourcePath(), getVersionLabel(ancestor), getVersionLabel(left), getVersionLabel(right)})); 
			} else {
				String leftName = null;
				if (left != null) leftName = left.getName();
				String rightName = null;
				if (right != null) rightName = right.getName();
				if (leftName != null && !leftName.equals(rightName)) {
					title = NLS.bind(TeamUIMessages.TeamCompareEditorInput_titleNoAncestorDifferent, (new Object[] {leftName, getVersionLabel(left), rightName, getVersionLabel(right)}));  
				} else {
					title = NLS.bind(TeamUIMessages.TeamCompareEditorInput_titleNoAncestor, (new Object[] {guessResourceName(), getVersionLabel(left), getVersionLabel(right)})); 
					title = NLS.bind(TeamUIMessages.TeamCompareEditorInput_titleNoAncestor, (new Object[] {guessResourcePath(), getVersionLabel(left), getVersionLabel(right)})); 
				}
			}
		}
		setTitle(title);
	}

	private void setLabels(CompareConfiguration cc, IStructuredSelection selection) {
		ITypedElement left = this.left;
		ITypedElement right = this.right;
		ITypedElement ancestor = this.ancestor;
		
		if (left != null) {
			cc.setLeftLabel(getLabel(left));
			cc.setLeftImage(leftImage);
		}
	
		if (right != null) {
			cc.setRightLabel(getLabel(right));
			cc.setRightImage(rightImage);
		}
		
		if (ancestor != null) {
			cc.setAncestorLabel(getLabel(ancestor));
			cc.setAncestorImage(ancestorImage);
		}
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
		final boolean threeWay = ancestor != null;
		if (right == null || left == null) {
			setMessage(TeamUIMessages.TeamCompareEditorInput_different); 
			return null;
		}
		
		initLabels();
	
		final Differencer d = new Differencer() {
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
				if (node instanceof ITypedElement) {
					ITypedElement element = (ITypedElement)node;
					progressMonitor.subTask(NLS.bind(TeamUIMessages.TeamCompareEditorInput_fileProgress, (new String[] {element.getName()}))); 
					progressMonitor.worked(1);
				}
			}
			protected Object[] getChildren(Object input) {
				if (input instanceof IStructureComparator) {
					Object[] children= ((IStructureComparator)input).getChildren();
					if (children != null)
						return children;
				}
				return null;
			}
			protected Object visit(Object data, int result, Object ancestor, Object left, Object right) {
				return new DiffNode((IDiffContainer) data, result, (ITypedElement)ancestor, (ITypedElement)left, (ITypedElement)right);
			}
		};
		
		try {	
			// do the diff	
			Object result = null;
			monitor.beginTask(TeamUIMessages.TeamCompareEditorInput_comparing, 30); 
			IProgressMonitor sub = new SubProgressMonitor(monitor, 30);
			sub.beginTask(TeamUIMessages.TeamCompareEditorInput_comparing, 100); 
			try {
				result = d.findDifferences(threeWay, sub, null, ancestor, left, right);
			} finally {
				sub.done();
			}
			return result;
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
		// calculate the type for the left contribution
		IFileRevision leftEdition = null;
		if (left instanceof RevisionEditionNode) {
			leftEdition = ((RevisionEditionNode)left).getFileRevision();
		}
		
		// calculate the type for the right contribution
		IFileRevision rightEdition = null;
		if (right instanceof RevisionEditionNode)
			rightEdition = ((RevisionEditionNode)right).getFileRevision();
		
		
		// compare them
			
		/*if (leftEdition == null || rightEdition == null) {
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
		
		String leftLocation = leftEdition.getRepository().getLocation(false);
		String rightLocation = rightEdition.getRepository().getLocation(false);
		if (!leftLocation.equals(rightLocation)) {
			return NODE_UNKNOWN;
		}
		try {
			ResourceSyncInfo leftInfo = ((ICVSResource)leftEdition).getSyncInfo();
			ResourceSyncInfo rightInfo = ((ICVSResource)rightEdition).getSyncInfo();
			
			if (leftEdition.getRepositoryRelativePath().equals(rightEdition.getRepositoryRelativePath()) &&
				leftInfo.getRevision().equals(rightInfo.getRevision())) {
				return NODE_EQUAL;
			} else {
				if(considerContentIfRevisionOrPathDiffers()) {
					return NODE_UNKNOWN;
				} else {
					return NODE_NOT_EQUAL;
				}
			}
		} catch (TeamException e) {
			handle(e);
			return NODE_UNKNOWN;
		}*/
		
		return NODE_UNKNOWN;
	}
	
	private boolean considerContentIfRevisionOrPathDiffers() {
		return true; //TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_CONSIDER_CONTENTS);
	}
	
	public Viewer createDiffViewer(Composite parent) {
		final Viewer viewer = super.createDiffViewer(parent);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				CompareConfiguration cc = getCompareConfiguration();
				setLabels(cc, (IStructuredSelection)event.getSelection());
			}
		});
		((StructuredViewer)viewer).addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                ISelection selection = event.getSelection();
                if (! selection.isEmpty() && selection instanceof IStructuredSelection) {
                    Object o = ((IStructuredSelection)selection).getFirstElement();
                    if (o instanceof DiffNode) {
                        updateLabelsFor((DiffNode)o);
                    }
                }
            }
        });
        ((StructuredViewer)viewer).addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                ISelection selection = event.getSelection();
                if (! selection.isEmpty() && selection instanceof IStructuredSelection) {
                    Object o = ((IStructuredSelection)selection).getFirstElement();
                    if (o instanceof DiffNode) {
                        DiffNode diffNode = ((DiffNode)o);
                        if (diffNode.hasChildren()) {
                            AbstractTreeViewer atv = ((AbstractTreeViewer)viewer);
                            atv.setExpandedState(o, !atv.getExpandedState(o));
                        }
                    }
                }
            }
        });
		return viewer;
	}
	
	/*
	 * Update the labels for the given DiffNode
     */
    protected void updateLabelsFor(DiffNode node) {
        CompareConfiguration cc = getCompareConfiguration();
        ITypedElement l = node.getLeft();
        if (l == null) {
            cc.setLeftLabel(TeamUIMessages.TeamCompareEditorInput_new); 
            cc.setLeftImage(null);
        } else {
	        cc.setLeftLabel(getLabel(l));
	        cc.setLeftImage(l.getImage());
        }
        ITypedElement r = node.getRight();
        if (r == null) {
            cc.setRightLabel(TeamUIMessages.TeamCompareEditorInput_deleted); 
            cc.setRightImage(null);
        } else {
	        cc.setRightLabel(getLabel(r));
	        cc.setRightImage(r.getImage());
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		if (toolTipText != null) {
			return toolTipText;
		}
		return super.getToolTipText();
	}
}
