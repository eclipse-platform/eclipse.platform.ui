/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.StatusTextEditor;


/**
 * The standard implementation of <code>IEncodingSupport</code>.
 * @since 2.0
 */
public class DefaultEncodingSupport implements IEncodingSupport {
	
	/** Internal property change listener. */
	private Preferences.IPropertyChangeListener fPropertyChangeListener;
	/** The editor this support is associated with. */
	private StatusTextEditor fTextEditor;
	/** The action group of this support. */
	private EncodingActionGroup fEncodingActionGroup;
	
	/**
	 * Creates a new encoding support.
	 */
	public DefaultEncodingSupport() {
		super();
	}
	
	/**
	 * Associates this encoding support to the given text editor and initializes this encoding.
	 * 
	 * @param textEditor the editor
	 */
	public void initialize(StatusTextEditor textEditor) {
		
		fTextEditor= textEditor;
		
		fPropertyChangeListener= new Preferences.IPropertyChangeListener() {
			public void propertyChange(Preferences.PropertyChangeEvent e) {
				if (ResourcesPlugin.PREF_ENCODING.equals(e.getProperty()))
					setEncoding(null, false); // null means: use default
			}
		};
		
		Preferences p= ResourcesPlugin.getPlugin().getPluginPreferences();
		p.addPropertyChangeListener(fPropertyChangeListener);
		
		fEncodingActionGroup= new EncodingActionGroup(fTextEditor);
		fEncodingActionGroup.update();
	}
	
	/**
	 * Disposes this encoding support.
	 */
	public void dispose() {
		Preferences p= ResourcesPlugin.getPlugin().getPluginPreferences();
		p.removePropertyChangeListener(fPropertyChangeListener);
		
		fEncodingActionGroup.dispose();
		fEncodingActionGroup= null;
		
		fTextEditor= null;
	}
	
	/**
	 * Resets this encoding support. Should be called if, e.g., the input element of the 
	 * associated editor changed.
	 */
	public void reset() {
		fEncodingActionGroup.update();
	}
	
	/**
	 * Sets the encoding of the editor's input to the given value. If <code>overwrite</code> is
	 * <code>true</code> the value is set even if the encoding is already set.
	 * 
	 * @param encoding the new encoding
	 * @param overwrite <code>true</code> if current encoding should be overwritten
	 */
	protected void setEncoding(String encoding, boolean overwrite) {
		IDocumentProvider p= fTextEditor.getDocumentProvider();
		if (p instanceof IStorageDocumentProvider) {
			final IEditorInput input= fTextEditor.getEditorInput();
			IStorageDocumentProvider provider= (IStorageDocumentProvider)p;
			String current= provider.getEncoding(input);
			if (!fTextEditor.isDirty()) {
				String internal= encoding == null ? "" : encoding; //$NON-NLS-1$
				boolean apply= (overwrite || current == null) && !internal.equals(current);
				if (apply) {
					provider.setEncoding(input, encoding);
					Runnable encodingSetter=
						new Runnable() {
							   public void run() {
								   fTextEditor.doRevertToSaved();
							   }
						};
					Display display= fTextEditor.getSite().getShell().getDisplay();
					if (display != null && !display.isDisposed())
						BusyIndicator.showWhile(display, encodingSetter);
					else
						encodingSetter.run();
				}
			}
			fEncodingActionGroup.update();
		}
	}
	
	/*
	 * @see IEncodingSupport#setEncoding(String)
	 */
	public void setEncoding(String encoding) {
		setEncoding(encoding, true);
	}
	
	/*
	 * @see IEncodingSupport#getEncoding()
	 */
	public String getEncoding() {
		IDocumentProvider p= fTextEditor.getDocumentProvider();
		if (p instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider) p;
			return provider.getEncoding(fTextEditor.getEditorInput());
		}
		return null;
	}
	
	/*
	 * @see IEncodingSupport#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		IDocumentProvider p= fTextEditor.getDocumentProvider();
		if (p instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider) p;
			return provider.getDefaultEncoding();
		}
		return null;
	}
	
	/**
	 * Returns a status header for the given status.
	 * 
	 * @param status the status
	 * @return a status header for the given status.
	 */
	public String getStatusHeader(IStatus status) {
		Throwable t= status.getException();
		
		if (t instanceof CharConversionException)
			return TextEditorMessages.getString("Editor.error.unreadable_encoding.header"); //$NON-NLS-1$
		
		if (t instanceof UnsupportedEncodingException)
			return TextEditorMessages.getString("Editor.error.unsupported_encoding.header"); //$NON-NLS-1$
		
		return null;
	}
	
	/**
	 * Returns a banner for the given status.
	 * 
	 * @param status the status
	 * @return a banner for the given status.
	 */
	public String getStatusBanner(IStatus status) {
		Throwable t= status.getException();
		
		if (t instanceof CharConversionException)
			return TextEditorMessages.getString("Editor.error.unreadable_encoding.banner"); //$NON-NLS-1$
		
		if (t instanceof UnsupportedEncodingException)
			return TextEditorMessages.getString("Editor.error.unsupported_encoding.banner"); //$NON-NLS-1$
		
		return null;

	}
	
	/**
	 * Returns a status message if any.
	 * 
	 * @param status the status
	 * @return a status message indicating encoding problems or <code>null</code> otherwise
	 */
	public String getStatusMessage(IStatus status) {
		Throwable t= status.getException();
		if (t instanceof CharConversionException || t instanceof UnsupportedEncodingException) {
			
			String encoding= getEncoding();
			if (encoding == null)
				encoding= getDefaultEncoding();
			
			if (t instanceof CharConversionException) {
				if (encoding != null)
					return MessageFormat.format(TextEditorMessages.getString("Editor.error.unreadable_encoding.message_arg"), new Object[] { encoding }); //$NON-NLS-1$
				return TextEditorMessages.getString("Editor.error.unreadable_encoding.message"); //$NON-NLS-1$
			}
			
			if (t instanceof UnsupportedEncodingException) {
				if (encoding != null)
					return TextEditorMessages.getFormattedString("Editor.error.unsupported_encoding.message_arg", encoding); //$NON-NLS-1$
				return TextEditorMessages.getString("Editor.error.unsupported_encoding.message"); //$NON-NLS-1$
			}
		}
		
		return null;
	}
}
