package org.eclipse.e4.tools.emf.editor3x.emf;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.osgi.framework.Bundle;

public class EditUIUtil {
	static final Class<?> FILE_CLASS;
	static {
		Class<?> fileClass = null;
		try {
			fileClass = IFile.class;
		} catch (final Exception exception) {
			// Ignore any exceptions and assume the class isn't available.
		}
		FILE_CLASS = fileClass;
	}

	static final Class<?> FILE_REVISION_CLASS;
	static final Method FILE_REVISION_GET_URI_METHOD;
	static {
		Class<?> fileRevisionClass = null;
		Method fileRevisionGetURIMethod = null;
		final Bundle bundle = Platform.getBundle("org.eclipse.team.core"); //$NON-NLS-1$
		if (bundle != null
			&& (bundle.getState() & (Bundle.ACTIVE | Bundle.STARTING | Bundle.RESOLVED)) != 0) {
			try {
				fileRevisionClass = bundle
					.loadClass("org.eclipse.team.core.history.IFileRevision"); //$NON-NLS-1$
				fileRevisionGetURIMethod = fileRevisionClass
					.getMethod("getURI"); //$NON-NLS-1$
			} catch (final Exception exeption) {
				// Ignore any exceptions and assume the class isn't available.
			}
		}
		FILE_REVISION_CLASS = fileRevisionClass;
		FILE_REVISION_GET_URI_METHOD = fileRevisionGetURIMethod;
	}

	static final Class<?> URI_EDITOR_INPUT_CLASS;
	static {
		Class<?> uriEditorInputClass = null;
		try {
			uriEditorInputClass = IURIEditorInput.class;
		} catch (final Exception exception) {
			// The class is not available.
		}
		URI_EDITOR_INPUT_CLASS = uriEditorInputClass;
	}

	public static URI getURI(IEditorInput editorInput) {

		if (FILE_CLASS != null) {
			final IFile file = (IFile) editorInput.getAdapter(FILE_CLASS);
			if (file != null) {
				return URI.createPlatformResourceURI(file.getFullPath()
					.toString(), true);
			}
		}
		if (FILE_REVISION_CLASS != null) {
			final Object fileRevision = editorInput.getAdapter(FILE_REVISION_CLASS);
			if (fileRevision != null) {
				try {
					return URI
						.createURI(((java.net.URI) FILE_REVISION_GET_URI_METHOD
							.invoke(fileRevision)).toString());
				} catch (final Exception exception) {
					// TODO Log error
				}
			}
		}
		if (URI_EDITOR_INPUT_CLASS != null) {
			if (editorInput instanceof IURIEditorInput) {
				return URI.createURI(
					((IURIEditorInput) editorInput).getURI().toString())
					.trimFragment();
			}
		}

		return null;
	}
}
