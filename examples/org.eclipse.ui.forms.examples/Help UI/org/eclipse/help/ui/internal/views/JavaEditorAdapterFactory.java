/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.*;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.util.JavadocHelpContext;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JavaEditorAdapterFactory implements IAdapterFactory {
	private EditorContextHelpProvider provider;
	
	private class EditorContextHelpProvider implements IContextProvider {
		private Object[] selected;
		private String id;
		private String expression;
		
		public EditorContextHelpProvider(String id) {
			this.id = id;
		}
		public void setSelected(Object[] selected) {
			this.selected = selected;
		}
		public void setSearchExpression(String expression) {
			this.expression = expression;
		}
		public int getContextChangeMask() {
			return SELECTION;
		}
		public IContext getContext(Object target) {
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
		public String getSearchExpression(Object target) {
			return expression;
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
		if (IContextProvider.class.isAssignableFrom(adapterType)
				&& adaptableObject instanceof JavaEditor)
			return createJavaEditorAdapter((JavaEditor) adaptableObject);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IContextProvider.class };
	}
	private IContextProvider createJavaEditorAdapter(JavaEditor editor) {
		try {
			Object[] selected= null;
			IJavaElement input= SelectionConverter.getInput(editor);
			if (ActionUtil.isOnBuildPath(input)) {
				selected= SelectionConverter.codeResolve(editor);					
			}
			provider.setSelected(selected);
			provider.setSearchExpression(createSearchExpression(editor, selected));
			return provider;

		} catch (CoreException x) {
			System.out.println(x);
		}		
		return null;
	}
	private String createSearchExpression(JavaEditor editor, Object[] selected) {
		StringBuffer buf = new StringBuffer();
		if (selected!=null) {
			for (int i=0; i<selected.length; i++) {
				Object obj = selected[i];
				if (obj instanceof IJavaElement) {
					IJavaElement el = (IJavaElement)obj;
					if (buf.length()>0)
						buf.append(" ");
					buf.append(el.getElementName());
				}
			}
		}
		if (buf.length()>0)
			buf.append(" ");
		buf.append(editor.getEditorSite().getRegisteredName());
		return buf.toString();
	}
}