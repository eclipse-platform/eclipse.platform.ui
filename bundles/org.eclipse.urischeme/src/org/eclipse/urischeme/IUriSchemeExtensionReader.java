package org.eclipse.urischeme;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.urischeme.internal.UriSchemeExtensionReader;

/**
 * API for reading available URI schemes from the extension registry
 *
 */
public interface IUriSchemeExtensionReader {

	/**
	 * The instance of IUriSchemeExtensionReader
	 */
	IUriSchemeExtensionReader INSTANCE = UriSchemeExtensionReader.getInstance();

	/**
	 *
	 * @return The list of available URI schemes
	 */
	Collection<IScheme> getSchemes();

	/**
	 * Creates the handler for a given URI scheme as registered in extension point
	 * <code> org.eclipse.core.runtime.uriSchemeHandlers</code>
	 *
	 * @param uriScheme The URI scheme
	 * @return The handler implementation for the given URI scheme
	 * @throws CoreException
	 */
	IUriSchemeHandler getHandlerFromExtensionPoint(String uriScheme) throws CoreException;

}
