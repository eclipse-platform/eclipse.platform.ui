/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.util.JavadocHelpContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class JavaViewAdapterFactory implements IAdapterFactory,
		IExecutableExtension {
	private ViewContextHelpProvider provider;

	private String id;

	private class ViewContextHelpProvider implements IContextHelpProvider {
		private Object[] selected;

		public ViewContextHelpProvider() {
		}

		public void setSelected(Object[] selected) {
			this.selected = selected;
		}

		public int getContextHelpChangeMask() {
			return SELECTION;
		}

		public IContext getHelpContext(Widget widget) {
			IContext context = HelpSystem.getContext(id);
			if (context != null) {
				if (selected != null && selected.length > 0) {
					try {
						context = new JavadocHelpContext(context, selected);
					} catch (CoreException e) {
						System.out.println(e);
					}
				}
			}
			return context;
		}
	}

	/**
	 * 
	 */
	public JavaViewAdapterFactory() {
		provider = new ViewContextHelpProvider();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 *      java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IContextHelpProvider.class.isAssignableFrom(adapterType)
				&& adaptableObject instanceof ViewPart)
			return createJavaViewerAdapter((ViewPart) adaptableObject);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IContextHelpProvider.class };
	}

	private IContextHelpProvider createJavaViewerAdapter(ViewPart view) {
		IStructuredSelection sel = (IStructuredSelection) view.getSite()
				.getSelectionProvider().getSelection();
		provider.setSelected(sel.toArray());
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		this.id = IJavaHelpContextIds.PREFIX + (String) data;
	}
}