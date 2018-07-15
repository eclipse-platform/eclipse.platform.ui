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
	 * Simple pojo holding information about an available URI scheme
	 *
	 */
	public static class Scheme {

		private String uriScheme;
		private String uriSchemeDescription;

		/**
		 * Returns an instance of Scheme
		 *
		 * @param uriScheme            The URI scheme
		 * @param uriSchemeDescription The description of the URI scheme
		 */
		public Scheme(String uriScheme, String uriSchemeDescription) {
			super();
			this.uriScheme = uriScheme;
			this.uriSchemeDescription = uriSchemeDescription;
		}

		/**
		 *
		 * @return The URI scheme
		 */
		public String getUriScheme() {
			return uriScheme;
		}

		/**
		 *
		 * @return The description of the URI scheme
		 */
		public String getUriSchemeDescription() {
			return uriSchemeDescription;
		}

	}

	/**
	 * The instance of IUriSchemeExtensionReader
	 */
	IUriSchemeExtensionReader INSTANCE = UriSchemeExtensionReader.getInstance();

	/**
	 *
	 * @return The list of available URI schemes
	 */
	Collection<Scheme> getSchemes();

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
