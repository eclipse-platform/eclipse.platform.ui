/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak  (brockj@tpg.com.au) - Bug 142960 Performance tweak for ignored file processing
 ******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.Vector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 *
 */
public abstract class ViewerComparatorTest extends ViewerTestCase {
	protected String UI = "UI";
	protected String[] TEAM1 = {"Karice", "Tod", "Eric", "Paul", 
				"Mike", "Michael", "Andrea", "Kim", "Boris", "Susan"};
	protected String[] TEAM1_SORTED = {"Andrea", "Boris", "Eric", "Karice", "Kim", 
			"Michael", "Mike", "Paul", "Susan", "Tod"};
	protected String[] TEAM1_SORTED_WITH_INSERT = {"Andrea", "Boris", "Duong", "Eric", "Karice", "Kim", 
			"Michael", "Mike", "Paul", "Susan", "Tod"};
	
	protected String RUNTIME = "Runtime";
	protected String[] TEAM2 = {"Pascal", "DJ", "Jeff", "Andrew", "Oleg"};
	protected String[] TEAM2_SORTED = {"Andrew", "DJ", "Jeff", "Oleg", "Pascal"};
	
	protected String CORE = "Core";
	protected String[] TEAM3 = {"John", "Michael", "Bogdan"};
	protected String[] TEAM3_SORTED = {"Bogdan", "John", "Michael"};

	protected Team team1 = new Team(UI, TEAM1);
	protected Team team2 = new Team(RUNTIME, TEAM2);
	protected Team team3 = new Team(CORE, TEAM3);
	
	/*
	 * model object - parent
	 */
	protected class Team {
		Vector fListeners = new Vector();
		
		TeamMember[] members;
		String name;
		
		public Team(String name, String[] members){
			this.name = name;
			this.members = new TeamMember[members.length];
			for (int i = 0; i < members.length; i++)
				this.members[i] = new TeamMember(members[i], this);
		}
		
		public void addMember(String person){
			TeamMember newMember = new TeamMember(person, this);
			TeamMember[] newMembers = new TeamMember[members.length + 1];
			for (int i = 0; i < members.length; i++){
				newMembers[i] = members[i];
			}
			newMembers[newMembers.length - 1] = newMember;
			members = null;
			members = newMembers;
			newMembers = null;
			fireModelChanged(new ComparatorModelChange(TestModelChange.INSERT, this, newMember));
		}
		
	    public void addListener(IComparatorModelListener listener) {
	        fListeners.addElement(listener);
	    }

	    /**
	     * Fires a model changed event to all listeners.
	     */
	    public void fireModelChanged(ComparatorModelChange change) {
	        for (int i = 0; i < fListeners.size(); ++i) {
	        	IComparatorModelListener listener = (IComparatorModelListener) fListeners
	                    .get(i);
	            listener.modelChanged(change);
	        }
	    }

	    public void removeListener(IComparatorModelListener listener) {
	        fListeners.removeElement(listener);
	    }
	}
	
	/*
	 * model object - child
	 */
	protected class TeamMember {
		String name;
		Team team;
		
		public TeamMember(String name, Team team){
			this.name = name;
			this.team = team;
		}
	}
	
	/*
	 * label provider
	 */
	protected class TeamModelLabelProvider extends LabelProvider{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof Team)
				return ((Team)element).name;
			else if (element instanceof TeamMember){
				return ((TeamMember)element).name;
			}
			return element.toString();
		}			
	}
	
	/*
	 * content provider
	 */
	protected class TeamModelContentProvider implements IComparatorModelListener,IStructuredContentProvider{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Team){
				return ((Team)inputElement).members;
			}
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null){
				((Team) oldInput).removeListener(this);;
			}
	       
	        if (newInput != null){
	        	((Team) newInput).addListener(this);
	        }
		}

	    public void modelChanged(ComparatorModelChange change) {
	        switch (change.getKind()) {
	        case TestModelChange.INSERT:
	            doInsert(change);
	            break;
	        case TestModelChange.REMOVE:
	            doRemove(change);
	            break;
	        case TestModelChange.STRUCTURE_CHANGE:
	            doStructureChange(change);
	            break;
	        case TestModelChange.NON_STRUCTURE_CHANGE:
	            doNonStructureChange(change);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown kind of change");
	        }

	        StructuredSelection selection = new StructuredSelection(change
	                .getChildren());
	        if ((change.getModifiers() & TestModelChange.SELECT) != 0) {
	            fViewer.setSelection(selection);
	        }
	        if ((change.getModifiers() & TestModelChange.REVEAL) != 0) {
	            Object element = selection.getFirstElement();
	            if (element != null) {
	                fViewer.reveal(element);
	            }
	        }
	    }
	    
	    protected void doInsert(ComparatorModelChange change) {
	        if (fViewer instanceof ListViewer) {
	            if (change.getParent() != null
	                    && change.getParent().equals(fViewer.getInput())) {
	                ((ListViewer) fViewer).add(change.getChildren());
	            }
	        } else if (fViewer instanceof TableViewer) {
	            if (change.getParent() != null
	                    && change.getParent().equals(fViewer.getInput())) {
	                ((TableViewer) fViewer).add(change.getChildren());
	            }
	        } else if (fViewer instanceof AbstractTreeViewer) {
	            ((AbstractTreeViewer) fViewer).add(change.getParent(), change
	                    .getChildren());
	        } else if (fViewer instanceof ComboViewer) {
	            ((ComboViewer) fViewer).add(change.getChildren());
	        } else {
	            Assert.isTrue(false, "Unknown kind of viewer");
	        }
	    }

	    protected void doNonStructureChange(ComparatorModelChange change) {
           fViewer.update(change.getParent(),
                    new String[] { IBasicPropertyConstants.P_TEXT });
	    }

	    protected void doRemove(ComparatorModelChange change) {
	        if (fViewer instanceof ListViewer) {
	            ((ListViewer) fViewer).remove(change.getChildren());
	        } else if (fViewer instanceof TableViewer) {
	            ((TableViewer) fViewer).remove(change.getChildren());
	        } else if (fViewer instanceof AbstractTreeViewer) {
	            ((AbstractTreeViewer) fViewer).remove(change.getChildren());
	        } else if (fViewer instanceof ComboViewer) {
	            ((ComboViewer) fViewer).remove(change.getChildren());
	        } else {
	            Assert.isTrue(false, "Unknown kind of viewer");
	        }
	    }

	    protected void doStructureChange(ComparatorModelChange change) {
            fViewer.refresh(change.getParent());
	    }
	    
	}
	
	public ViewerComparatorTest(String name) {
		super(name);
	}

}
