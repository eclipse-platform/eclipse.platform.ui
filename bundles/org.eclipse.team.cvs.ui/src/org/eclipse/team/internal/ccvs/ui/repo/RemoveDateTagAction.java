
package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.team.internal.ccvs.ui.model.CVSTagElement;
import org.eclipse.ui.actions.SelectionListenerAction;


public class RemoveDateTagAction extends SelectionListenerAction {
	private IStructuredSelection selection;
	
	public RemoveDateTagAction() {
		super("Remove");
	}

	public void run() {
		CVSTagElement[] elements = getSelectedCVSTagElements();
		if (elements.length == 0) return;
		for(int i = 0; i < elements.length; i++){
			RepositoryManager mgr = CVSUIPlugin.getPlugin().getRepositoryManager();
			CVSTag tag = elements[i].getTag();
			if(tag.getType() == CVSTag.DATE){
				mgr.removeDateTag(elements[i].getRoot(),tag);
			}				
		}
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		this.selection = selection;
		boolean b = containsDataTag();
		setEnabled(b);
		return b;
	}
	
	private boolean containsDataTag(){
		CVSTagElement[] elements = getSelectedCVSTagElements();
		if (elements.length > 0){ 		
			for(int i = 0; i < elements.length; i++){
				CVSTag tag = elements[i].getTag();
				if(tag.getType() == CVSTag.DATE){
					return true;
				}				
			}
		}
		return false;
	}
	
	/**
	 * Returns the selected CVS date tag elements
	 */
	private CVSTagElement[] getSelectedCVSTagElements() {
		ArrayList cvsTagElements = null;
		if (selection!=null && !selection.isEmpty()) {
			cvsTagElements = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = CVSAction.getAdapter(elements.next(), CVSTagElement.class);
				if (next instanceof CVSTagElement) {
					cvsTagElements.add(next);
				}
			}
		}
		if (cvsTagElements != null && !cvsTagElements.isEmpty()) {
			CVSTagElement[] result = new CVSTagElement[cvsTagElements.size()];
			cvsTagElements.toArray(result);
			return result;
		}
		return new CVSTagElement[0];
	}
}
