/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.help.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.markers.internal.*;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MarkerViewAdapterFactory implements IAdapterFactory {
	private MarkerViewContextHelpProvider provider;

	/**
	 * 
	 */
	private class MarkerViewContextHelpProvider implements IContextProvider {
		private String id;

		public IContext getContext(Object widget) {
			return HelpSystem.getContext(id);
		}

		public int getContextChangeMask() {
			return SELECTION;
		}

		public void setId(String id) {
			this.id = id;
		}
		public String getSearchExpression(Object target) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public MarkerViewAdapterFactory() {
		provider = new MarkerViewContextHelpProvider();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 *      java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IContextProvider.class.isAssignableFrom(adapterType)
				&& adaptableObject instanceof MarkerView) {
			return createMarkerViewAdapter((MarkerView) adaptableObject);
		}
		return null;
	}

	private Object createMarkerViewAdapter(MarkerView view) {
		String contextId = null;
		// See if there is a context registered for the current selection
		Object selected = ((IStructuredSelection) view.getSite()
				.getSelectionProvider().getSelection()).getFirstElement();
		IMarker marker = null;

		if (selected instanceof ConcreteMarker)
			marker = ((ConcreteMarker) selected).getMarker();
		else if (selected instanceof IMarker)
			marker = (IMarker) selected;

		if (marker != null) {
			contextId = IDE.getMarkerHelpRegistry().getHelp(marker);
		}
		if (contextId == null) {
			contextId = PlatformUI.PLUGIN_ID + ".task_list_view_context"; //$NON-NLS-1$
		}
		provider.setId(contextId);
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IContextProvider.class };
	}
}