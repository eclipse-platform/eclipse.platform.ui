package org.eclipse.help.ui.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.ui.*;

/**
 * A working set for help elements. 
 * NOTE: The only reason we inherit from ui's working set is because there is a
 * cast in the wizard, when getting the page id...
 * TODO: open bug on the ui component to fix the page id..
 */
public class HelpWorkingSet extends org.eclipse.ui.internal.WorkingSet implements IAdaptable, IPersistableElement, IWorkingSet {

	private WorkingSet workingSet;
	
	/**
	 * Constructor for HelpWorkingSet.
	 * @param name
	 * @param elements
	 */
	public HelpWorkingSet(String name, IAdaptable[] elements) {
		this(HelpSystem.getWorkingSetManager().createWorkingSet(name, (AdaptableHelpResource[])elements));
	}

	public HelpWorkingSet(WorkingSet ws) {
		super(ws.getName(), ws.getElements());
		this.workingSet = ws;
		// we need to also add the working set, as this is created from the Eclipse UI.
		HelpSystem.getWorkingSetManager().addWorkingSet(workingSet);
	}
	
	/**
	 * Tests the receiver and the object for equality
	 *
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same.
	 * 	false otherwise
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof WorkingSet) {
			WorkingSet ws = (WorkingSet) object;
			//String objectPageId = ws.getEditPageId();
			//String pageId = getEditPageId();
			boolean pageIdEqual =  true; //(objectPageId == null && pageId == null) || (objectPageId != null && objectPageId.equals(pageId));
			return workingSet.getName().equals(getName()) && ws.getElements().equals(workingSet.getElements()) && pageIdEqual;
		}
		return false;
	}
	
	/**
	 * @see org.eclipse.ui.IWorkingSet#getName()
	 */
	public String getName() {
		return workingSet.getName();
	}

	/**
	 * @see org.eclipse.ui.IWorkingSet#getElements()
	 */
	public IAdaptable[] getElements() {
		return workingSet.getElements();
	}

	/**
	 * @see org.eclipse.ui.IWorkingSet#setElements(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElements(IAdaptable[] elements) {
		AdaptableHelpResource[] res = new AdaptableHelpResource[elements.length];
		System.arraycopy(elements, 0,res,0, elements.length);
		workingSet.setElements(res);
	}

	/**
	 * @see org.eclipse.ui.IWorkingSet#setName(java.lang.String)
	 */
	public void setName(String name) {
		workingSet.setName(name);
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkingSet.class || adapter == IPersistableElement.class) 
			return this;
		else
			return null;
	}

	/**
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	public String getFactoryId() {
		return "org.eclipse.help.ui.internal.workingset.HelpResourceFactory";
	}

	/**
	 * @see org.eclipse.ui.IPersistableElement#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		HelpSystem.getWorkingSetManager().saveState();
		
		memento.putString("workingSet", workingSet.getName());
		//memento.putString(IWorkbenchConstants.TAG_EDIT_PAGE_ID, editPageId);

		for (int i=0; i<workingSet.getElements().length; i++){
			saveState((AdaptableHelpResource)workingSet.getElements()[i], memento);
		}
	}
	
	private void saveState(AdaptableHelpResource element, IMemento memento) {
		IToc toc = (IToc)element.getAdapter(IToc.class);
		ITopic topic = (ITopic)element.getAdapter(ITopic.class);
		if (toc != null)
			memento.putString("toc", toc.getHref());
		else if (topic != null) {
			AdaptableHelpResource parent = (AdaptableHelpResource)element.getParent();
			memento.putString("toc", parent.getHref());
			// get the index of this topic
			IAdaptable[] topics = parent.getChildren();
			for (int i = 0; i < topics.length; i++)
				if (topics[i] == this) {
					memento.putString("topic", String.valueOf(i));
					return;
				}
		}
	}

	/**
	 * NOTE: OVERRIDES THE SUPER METHOD. 
	 * TO DO: THIS METHOD SHOULD BE CLEANed-UP.  
	 * Sets the id of the working set
	 * page that was used to create the receiver.
	 *
	 * @param pageId the id of the working set page.
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 */
	public String getEditPageId() {
		return HelpWorkingSetPage.PAGE_ID;
	}
}
