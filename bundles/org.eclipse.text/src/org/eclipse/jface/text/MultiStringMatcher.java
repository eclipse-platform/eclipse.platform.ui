/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski, Thomas Wolf, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski; Thomas Wolf - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Fast matcher to find the occurrences of any of a fixed set of constant strings. Supports finding
 * all (possibly overlapping) matches, or only the leftmost longest match.
 *
 * @since 3.9
 */
public class MultiStringMatcher {

	// An implementation of the Aho-Corasick algorithm (without the DFA construction from section 6 of the
	// paper; just the failure and output links).
	//
	// See Aho, Alfred V.; Corasick, Margaret J.: "Efficient String Matching: An Aid to Bibliographic Search",
	// CACM 18(6), 1975.
	//
	// The algorithm has been modified to support reporting either all matches or only leftmost longest matches.

	/**
	 * Describes a match result of {@link MultiStringMatcher#indexOf(CharSequence, int)}, giving
	 * access to the matched string and the offset in the text it was matched at.
	 */
	public static interface Match {

		/**
		 * Obtains the matched string.
		 *
		 * @return the text matched
		 */
		String getText();

		/**
		 * Obtains the offset the {@link #getText() text} was matched at.
		 *
		 * @return the offset
		 */
		int getOffset();

	}

	/** A Builder for creating a {@link MultiStringMatcher}. */
	public static interface Builder {

		/**
		 * Adds search strings to be looked for. {@code null} and empty strings in the arguments are
		 * ignored.
		 *
		 * @param searchStrings to add to be looked for by the matcher.
		 * @return this
		 * @throws IllegalStateException if the {@link MultiStringMatcher} was already built.
		 */
		Builder add(String... searchStrings);

		/**
		 * Returns the {@link MultiStringMatcher} built by this builder.
		 * <p>
		 * Note that a {@link Builder} instance can build only <em>one</em>
		 * {@link MultiStringMatcher} instance. This is by design; otherwise the builder would have
		 * to store all the searchStrings somewhere, which may be rather memory intensive if a lot
		 * of search strings are added.
		 * </p>
		 *
		 * @return the {@link MultiStringMatcher}
		 * @throws IllegalStateException if the {@link MultiStringMatcher} was already built.
		 */
		MultiStringMatcher build();
	}

	private static class BuilderImpl implements Builder {

		private MultiStringMatcher m;

		BuilderImpl() {
			m= new MultiStringMatcher();
		}

		private void check() {
			if (m == null) {
				throw new IllegalStateException("Builder.build() was already called"); //$NON-NLS-1$
			}
		}

		@Override
		public Builder add(String... searchStrings) {
			check();
			m.add(searchStrings);
			return this;
		}

		@Override
		public MultiStringMatcher build() {
			check();
			MultiStringMatcher result= m;
			m= null;
			if (!result.root.hasChildren()) {
				// no search strings were added; return a specialized "matches nothing" matcher
				return new MultiStringMatcher() {
					@Override
					public void find(CharSequence text, int offset, Consumer<Match> matches) {
						return;
					}

					@Override
					public Match indexOf(CharSequence text, int offset) {
						return null;
					}
				};
			}
			result.buildLinks();
			return result;
		}
	}

	/**
	 * Creates an initially empty {@link Builder}.
	 *
	 * @return the {@link Builder}
	 */
	public static Builder builder() {
		return new BuilderImpl();
	}

	private static class MatchResult implements Match {

		private final String match;

		private final int offset;

		public MatchResult(String match, int offset) {
			this.match= match;
			this.offset= offset;
		}

		@Override
		public String getText() {
			return match;
		}

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(match) * 31 + Integer.hashCode(offset);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			MatchResult other= (MatchResult) obj;
			return offset == other.offset && Objects.equals(match, other.match);
		}

		@Override
		public String toString() {
			return '[' + match + ", " + offset + ']'; //$NON-NLS-1$
		}
	}

	/** A node in the trie built from the search strings. */
	private static class Node {
		HashMap<Character, Node> children;

		String match;

		Node fail;

		Node output;

		final int depth;

		Node(int depth) {
			this.depth= depth;
		}

		Node next(Character c) {
			return children == null ? null : children.get(c);
		}

		Node add(char c) {
			if (children == null) {
				children= new HashMap<>();
			}
			return children.computeIfAbsent(Character.valueOf(c), key -> new Node(depth + 1));
		}

		boolean hasChildren() {
			return children != null;
		}

		@Override
		public String toString() {
			return "[depth=" + depth + ", match=" + match //$NON-NLS-1$ //$NON-NLS-2$
					+ ", children=" + (children == null ? "<none>" : children.keySet().stream().map(c -> c.toString()).collect(Collectors.joining(", "))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ ']';
		}
	}

	/** Root node of the trie. */
	private final Node root= new Node(0) {
		@Override
		Node next(Character c) {
			// Implements the sentinel loop on the root node for all non-matching characters.
			Node child= super.next(c);
			return child == null ? this : child;
		}
	};

	private MultiStringMatcher() {
		// Always use a Builder or the static helper methods to create a MultiStringMatcher
	}

	private void add(String... searchStrings) {
		if (searchStrings != null) {
			for (String searchString : searchStrings) {
				if (searchString == null || searchString.isEmpty()) {
					continue;
				}
				Node node= root;
				for (char c : searchString.toCharArray()) {
					node= node.add(c);
				}
				node.match= searchString;
			}
		}
	}

	private void buildLinks() {
		// Build the fail and output links. See the paper referenced at the top; this
		// is a one-to-one implementation of the original algorithm. Variable names
		// s, r, and state are kept as in the paper.
		List<Node> queue= new LinkedList<>();
		for (Node s : root.children.values()) {
			if (s.hasChildren()) {
				// No need to queue nodes without children since we don't do anything
				// with them anyway.
				queue.add(s);
			}
			s.fail= root;
		}
		while (!queue.isEmpty()) {
			Node r= queue.remove(0);
			for (Map.Entry<Character, Node> entry : r.children.entrySet()) {
				Character c= entry.getKey();
				Node s= entry.getValue();
				if (s.hasChildren()) {
					queue.add(s);
				}
				Node state= r.fail;
				Node f;
				while ((f= state.next(c)) == null) {
					state= state.fail;
				}
				s.fail= f;
				if (f.match != null) {
					s.output= f;
				} else if (f.output != null) {
					s.output= f.output;
				}
			}
		}
	}

	/**
	 * Finds all occurrences of any of the search strings of the {@link MultiStringMatcher} in the
	 * given {@code text} starting at the given {@code offset}, including overlapping occurrences.
	 *
	 * @param text to search (not {@code null})
	 * @param offset to start searching at
	 * @param matches {@link Consumer} all matches are fed to
	 *
	 * @since 3.10
	 */
	public void find(CharSequence text, int offset, Consumer<Match> matches) {
		// Main search loop of the standard Aho-Corasick algorithm.
		int textEnd= text.length();
		Node node= root;
		for (int i= offset; i < textEnd; i++) {
			Character c= Character.valueOf(text.charAt(i));
			Node next;
			while ((next= node.next(c)) == null) {
				node= node.fail;
			}
			node= next;
			if (node.match != null) {
				matches.accept(new MatchResult(node.match, i - node.depth + 1));
			}
			Node out= node.output;
			while (out != null) {
				matches.accept(new MatchResult(out.match, i - out.depth + 1));
				out= out.output;
			}
		}
	}

	/**
	 * Finds all occurrences of any of the search strings of the {@link MultiStringMatcher} in the
	 * given {@code text} starting at the given {@code offset}, including overlapping occurrences.
	 *
	 * @param text to search (not {@code null})
	 * @param offset to start searching at
	 * @return a possibly empty list of matches
	 */
	public List<Match> find(CharSequence text, int offset) {
		List<Match> matches= new LinkedList<>();
		find(text, offset, matches::add);
		return matches;
	}

	/**
	 * Find the next occurrence of any of the search strings of the {@link MultiStringMatcher} in
	 * the given {@code text} starting at the given {@code offset}.
	 * <p>
	 * Performs a simultaneous search for all the strings, returning the leftmost match. If multiple
	 * search strings match at the same index, the longest match is returned.
	 * </p>
	 *
	 * @param text to search (not {@code null})
	 * @param offset to start searching at
	 * @return the leftmost longest match found, or {@code null} if no match was found.
	 */
	public Match indexOf(CharSequence text, int offset) {
		// Main search loop of the Aho-Corasick algorithm, modified to stop after
		// the leftmost longest match.
		//
		// To find a match, we pursue a primary goal (lowest offset) and a secondary goal
		// (longest match). We differentiate between primary and sub-matches. Matching starts
		// by walking down one path of the trie. Any match we find on this path is a primary
		// match and any new primary match is better than the one before.  A sub-match is a
		// matching prefix of a suffix of the text currently scanned along the path. These
		// sub-matches occur on paths off the one we're currently following, and they are
		// linked in the trie via the output links. Their offset is always greater than that
		// of a primary match, and sub-matches further into the 'output' chain are shorter.
		// Therefore we are interested only in the first such sub-match. While walking down
		// a path, sub-matches off this path are not found in offset order, so we have to
		// check whether a new sub-match is better (lower offset, or longer) than a previously
		// found sub-match.
		//
		// When we can't continue matching on the current path, the algorithm uses the fail
		// links to try to find an alternate path to match (which would match a suffix of
		// what was traversed so far). Therefore, if we already had a primary match, it is
		// returned, since any other match must have a higher offset. If there is no alternate
		// path, we fall off the trie (the algorithm bring us back to root, and would start
		// again from the top). If we have any match, we may stop and return it. If we _do_
		// change to an alternate path but there's a sub-match with a lower offset, we also
		// may return that. Otherwise we continue normally on the new path.
		int textEnd= text.length();
		Match primaryMatch= null;
		Match subMatch= null;
		Node node= root;
		for (int i= offset; i < textEnd; i++) {
			Character c= Character.valueOf(text.charAt(i));
			Node next= node.next(c);
			if (next == null) {
				// Can't continue on this path.
				if (primaryMatch != null) {
					// Return primary match because any other match must have a higher offset.
					return primaryMatch;
				}
				// Search for another path to continue matching.
				do {
					node= node.fail;
				} while ((next= node.next(c)) == null);
				if (subMatch != null) {
					if (next == root) {
						// We fell off the trie and could not switch to another. Return the best
						// sub-match.
						return subMatch;
					} else if (subMatch.getOffset() < i - node.depth) {
						// The new path starts at i - node.depth == i - next.depth + 1, so if a
						// sub-match is earlier, we may return it. Any primary match on this path
						// or on any other path we might switch to later on will have a higher
						// offset, and so will any sub-matches we might discover on these paths.
						return subMatch;
					}
				}
			}
			node= next;
			if (node.match != null) {
				// Any new primary match is better because all have the same offset but any new one
				// must be longer. An existing sub-match from a previous path is checked above.
				primaryMatch= new MatchResult(node.match, i - node.depth + 1);
				if (!node.hasChildren()) {
					// We will fall off the trie on the next character, so we can return right here.
					return primaryMatch;
				}
			}
			// Check for sub matches but only if there is no primary match because only another
			// primary match can be better.
			if (primaryMatch == null) {
				Node out= node.output;
				if (out != null) {
					int newOffset= i - out.depth + 1;
					if (subMatch == null
							|| newOffset < subMatch.getOffset()
							|| (newOffset == subMatch.getOffset() && out.depth > subMatch.getText().length())) {
						subMatch= new MatchResult(out.match, newOffset);
					}
				}
			}
		}
		return primaryMatch != null ? primaryMatch : subMatch;
	}

	/**
	 * Finds the leftmost longest occurrence of any of the given {@code searchStrings} in the
	 * {@code text} starting at the given {@code offset}.
	 * <p>
	 * To match the same set of search strings repeatedly against texts it is more efficient to
	 * build and re-use a {@link MultiStringMatcher}.
	 * </p>
	 *
	 * @param text to search (not {@code null})
	 * @param offset to start searching at
	 * @param searchStrings to look for; non-{@code null} and non-empty strings are ignored
	 * @return a {@link Match} describing the match found, or {@code null} if no match was found or
	 *         there are no non-{@code null} non-empty {@code searchStrings}
	 */
	public static Match indexOf(CharSequence text, int offset, String... searchStrings) {
		return create(searchStrings).indexOf(text, offset);
	}

	/**
	 * Creates a {@link MultiStringMatcher} for the given {@code searchStrings}.
	 * <p>
	 * If there are no non-{@code null} non-empty {@code searchStrings}, the returned
	 * {@link MultiStringMatcher} will never match anything.
	 * </p>
	 *
	 * @param searchStrings to look for; non-{@code null} and non-empty strings are ignored
	 * @return the {@link MultiStringMatcher}
	 */
	public static MultiStringMatcher create(String... searchStrings) {
		return builder().add(searchStrings).build();
	}
}
