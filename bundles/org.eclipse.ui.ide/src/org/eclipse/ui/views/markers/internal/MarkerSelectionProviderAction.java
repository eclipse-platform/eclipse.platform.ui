package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * MarkerSelectionProviderAction is the abstract super class of the
 * selection provider actions used by marker views.
 *
 */
public abstract class MarkerSelectionProviderAction extends SelectionProviderAction {

	/**
	 * Create a new instance of the receiver.
	 * @param provider
	 * @param text
	 */
	public MarkerSelectionProviderAction(ISelectionProvider provider, String text) {
		super(provider, text);
		
	}

	/**
	 * Return whether or not the selection has one element that
	 * is concrete.
	 * @param selection
	 * @return <true>code</true> if the selection has one element that
	 * is concrete.
	 */
	protected boolean hasSingleConcreteSelection(IStructuredSelection selection) {
		return selection != null && selection.size() == 1
				&& ((MarkerNode) selection.getFirstElement()).isConcrete();
	}
	
	/**
	 * Get the selected markers in the receiver.
	 * @return IMarker[]
	 */
	IMarker[] getSelectedMarkers(){
		
		return getSelectedMarkers(getStructuredSelection());
	}

	/**
	 * Return the selected markers for the structured selection.
	 * @param structured IStructuredSelection
	 * @return IMarker[]
	 */
	IMarker[] getSelectedMarkers(IStructuredSelection structured) {
		Object[] selection = structured.toArray();
		ArrayList markers = new ArrayList();
		for (int i = 0; i < selection.length; i++) {
			MarkerNode object =(MarkerNode) selection[i];
			if(object.isConcrete())
				markers.add(((ConcreteMarker) object).getMarker());
		}
		
		return (IMarker[]) markers.toArray(new IMarker[markers.size()]);
	}
	
	/**
	 * Get the selected marker in the receiver.
	 * @return IMarker
	 */
	IMarker getSelectedMarker(){
		
		ConcreteMarker selection = (ConcreteMarker) getStructuredSelection().getFirstElement();
		return selection.getMarker();
	}

}
