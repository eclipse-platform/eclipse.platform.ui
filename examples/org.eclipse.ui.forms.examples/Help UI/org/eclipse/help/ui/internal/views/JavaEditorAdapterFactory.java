/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.util.JavadocHelpContext;
import org.eclipse.swt.widgets.Widget;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JavaEditorAdapterFactory implements IAdapterFactory {
	private EditorContextHelpProvider provider;
	
	private class EditorContextHelpProvider implements IContextHelpProvider {
		private Object[] selected;
		private String id;
		
		public EditorContextHelpProvider(String id) {
			this.id = id;
		}
		public void setSelected(Object[] selected) {
			this.selected = selected;
		}
		public int getContextHelpChangeMask() {
			return SELECTION;
		}
		public IContext getHelpContext(Widget widget) {
			IContext context= HelpSystem.getContext(id);
			if (context != null) {
				if (selected != null && selected.length > 0) {
					try {
						context= new JavadocHelpContext(context, selected);
					}
					catch (CoreException e) {
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
	public JavaEditorAdapterFactory() {
		provider = new EditorContextHelpProvider(IJavaHelpContextIds.JAVA_EDITOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IContextHelpProvider.class.isAssignableFrom(adapterType)
				&& adaptableObject instanceof JavaEditor)
			return createJavaEditorAdapter((JavaEditor) adaptableObject);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IContextHelpProvider.class };
	}
	private IContextHelpProvider createJavaEditorAdapter(JavaEditor editor) {
		try {
			Object[] selected= null;
			IJavaElement input= SelectionConverter.getInput(editor);
			if (ActionUtil.isOnBuildPath(input)) {
				selected= SelectionConverter.codeResolve(editor);					
			}
			provider.setSelected(selected);
			return provider;

		} catch (CoreException x) {
			System.out.println(x);
		}		
		return null;
	}
}
