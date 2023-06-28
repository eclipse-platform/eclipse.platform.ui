/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core.pathmatch;

import org.apache.tools.ant.types.selectors.TokenizedPath;
import org.apache.tools.ant.types.selectors.TokenizedPattern;
import org.eclipse.core.resources.IResource;

public class ResourceMatchers {

	/**
	 * ResourceMatcher to match anything unconditional.
	 */
	public static ResourceMatcher ANY = new ResourceMatcher() {
		@Override
		public String toString() {
			return "ResourceMatcher(ANY)"; //$NON-NLS-1$
		}
		@Override
		public boolean matches(IResource resource) {
			return true;
		}
	};

	/**
	 * Build ResourceMatcher for (optional) comma separated path patterns. More than
	 * one pattern can be supplied comma separated. For more than one pattern the
	 * matcher will match if <em>any</em> of the patterns match. Empty input match
	 * anything.
	 *
	 * @param text patterns to match. Not <code>null</code>
	 * @return ResourceMatcher for given path patterns
	 */
	public static ResourceMatcher commaSeparatedPaths(String text) {
		text = text.trim();
		if (text.isEmpty()) {
			return ANY;
		}
		String[] paths = text.split(","); //$NON-NLS-1$
		if (paths.length==1) {
			return path(paths[0].trim());
		} else {
			ResourceMatcher[] matchers = new ResourceMatcher[paths.length];
			for (int i = 0; i < matchers.length; i++) {
				String pattern = paths[i].trim();
				if (!pattern.isEmpty()) {
					matchers[i] = path(pattern);
				}
			}
			return either(matchers);
		}
	}

	/**
	 * Create ResourceMatcher returning <code>true</code>/match if <em>any</em> of
	 * the given matchers returns a match.
	 *
	 * @param matchers the matchers to combined with OR semantics. Not
	 *                 <code>null</code>.
	 * @return new ResourceMatcher combining the given matchers
	 */
	private static ResourceMatcher either(ResourceMatcher... matchers) {
		return new ResourceMatcher() {

			@Override
			public String toString() {
				StringBuilder buf = new StringBuilder("ResourceMatcher(");  //$NON-NLS-1$
				for (int i = 0; i < matchers.length; i++) {
					if (i>0) {
						buf.append(", ");  //$NON-NLS-1$
					}
					buf.append(matchers[i]);
				}
				buf.append(")");  //$NON-NLS-1$
				return buf.toString();
			}

			@Override
			public boolean matches(IResource resource) {
				for (ResourceMatcher m : matchers) {
					if (m != null && m.matches(resource)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * Create a ResourceMatcher for the given pattern.
	 *
	 * @param _pat the pattern to match
	 * @return the ResourceMatcher for given pattern
	 */
	private static ResourceMatcher path(String _pat) {
		if (!_pat.startsWith("/") && !_pat.startsWith("**/")) { //$NON-NLS-1$ //$NON-NLS-2$
			_pat = "**/"+_pat; //$NON-NLS-1$
		}
		final String pat = _pat;
		TokenizedPattern matcher = new TokenizedPattern(pat);
		return new ResourceMatcher() {

			@Override
			public String toString() {
				return pat;
			}

			@Override
			public boolean matches(IResource resource) {
				TokenizedPath path = new TokenizedPath(resource.getFullPath().toString());
				return matcher.matchPath(path, true);
			}
		};
	}

}
