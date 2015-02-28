/*******************************************************************************
 * Copyright (C) 2014 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Marcus Eng (Google) - initial API and implementation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

/**
 * Checks if the {@link UiFreezeEvent} matches any defined filters.
 * <p>
 * <strong>This class is not thread safe.<strong>
 * </p>
 */
public class FilterHandler {
	private static final String DOUBLE_BACKSLASH = "\\\\"; //$NON-NLS-1$

	/** Reusable object used to avoid object creation in filtering methods. */
	private final CompoundName compoundName = new CompoundName("", ""); //$NON-NLS-1$//$NON-NLS-2$

	/**
	 * Groups the class name and method name defined in the filter.
	 */
	private static class StackFrame implements Comparable<StackFrame> {
		final String className;
		final String methodName;

		public StackFrame(String className, String methodName) {
			this.className = className;
			this.methodName = methodName;
		}

		@Override
		public int compareTo(StackFrame other) {
			int c = methodName.compareTo(other.methodName);
			if (c != 0) {
				return c;
			}
			return className.compareTo(other.className);
		}
	}

	private static class CompoundName implements CharSequence {
		private String first;
		private String last;

		CompoundName(String first, String last) {
			Assert.isNotNull(first);
			Assert.isNotNull(last);
			this.first = first;
			this.last = last;
		}

		void reset(String first, String last) {
			Assert.isNotNull(first);
			Assert.isNotNull(last);
			this.first = first;
			this.last = last;
		}

		@Override
		public int length() {
			return first.length() + 1 + last.length();
		}

		@Override
		public char charAt(int index) {
			int firstLen = first.length();
			if (index < firstLen) {
				return first.charAt(index);
			} else if (index == firstLen) {
				return '.';
			} else {
				return last.charAt(index - firstLen - 1);
			}
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			int lastOffset = first.length() + 1; // Offset of the last name in the sequence.
			if (end < lastOffset) {
				return first.subSequence(start, end);
			} else if (start < lastOffset) {
				return new CompoundName(first.substring(start), last.substring(0, end - lastOffset));
			} else {
				return last.subSequence(start - lastOffset, end - lastOffset);
			}
		}

		@Override
		public String toString() {
			return first + '.' + last;
		}
	}

	private final StackFrame[] filterFrames;
	private final Pattern[] filterPatterns;

	/**
	 * Creates the filter.
	 *
	 * @param commaSeparatedMethods comma separated fully qualified method names to filter on.
	 *     Method names may contain wildcard characters '*' and '?'.
	 */
	public FilterHandler(String commaSeparatedMethods) {
		String[] filters = commaSeparatedMethods.split(","); //$NON-NLS-1$

		List<StackFrame> stackFrames = new ArrayList<StackFrame>(filters.length);
		List<Pattern> stackPatterns = new ArrayList<Pattern>(filters.length);
		for (String filter : filters) {
			if (containsWildcards(filter)) {
				Pattern pattern = createPattern(filter);
				stackPatterns.add(pattern);
			} else {
				int lastDot = filter.lastIndexOf('.');
				stackFrames.add(lastDot >= 0 ?
						new StackFrame(filter.substring(0, lastDot), filter.substring(lastDot + 1)) :
						new StackFrame("", filter)); //$NON-NLS-1$
			}
		}

		Collections.sort(stackFrames);
		filterFrames = stackFrames.toArray(new StackFrame[stackFrames.size()]);
		filterPatterns = stackPatterns.toArray(new Pattern[stackPatterns.size()]);
	}

	/**
	 * Returns {@code true} if the stack samples do not contain filtered stack frames in the stack
	 * traces of the display thread.
	 *
	 * @param stackSamples the array containing stack trace samples for a long event in the first
	 *     {@code numSamples} elements
	 * @param numSamples the number of valid stack trace samples in the {@code stackSamples} array
	 * @param displayThreadId the ID of the display thread
	 */
	public boolean shouldLogEvent(StackSample[] stackSamples, int numSamples,
			long displayThreadId) {
		if (filterFrames.length != 0 || filterPatterns.length != 0) {
			for (int i = 0; i < numSamples; i++) {
				if (hasFilteredTraces(stackSamples[i].getStackTraces(), displayThreadId)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if the stack trace of the display thread contains any frame that matches the filter.
	 */
	private boolean hasFilteredTraces(ThreadInfo[] stackTraces, long displayThreadId) {
		for (ThreadInfo threadInfo : stackTraces) {
			if (threadInfo.getThreadId() == displayThreadId) {
				for (StackTraceElement element : threadInfo.getStackTrace()) {
					if (matchesFilter(element)) {
						return true;
					}
				}
				return false;
			}
		}

		MonitoringPlugin.logError(Messages.FilterHandler_missing_thread_error, null);
		return false;
	}

	/**
	 * Checks whether the given stack frame matches the filter.
	 */
	boolean matchesFilter(StackTraceElement stackFrame) {
		String className = stackFrame.getClassName();
		String methodName = stackFrame.getMethodName();
		if (filterPatterns.length != 0) {
			// Match against patterns in filterPatterns.
			compoundName.reset(className, methodName);
			for (Pattern pattern : filterPatterns) {
				if (pattern.matcher(compoundName).matches()) {
					return true;
				}
			}
		}
		// Binary search in filterFrames.
		int low = 0;
		int high = filterFrames.length;
		while (low < high) {
			int mid = (low + high) >>> 1;
			StackFrame filter = filterFrames[mid];
			int c = methodName.compareTo(filter.methodName);
			if (c == 0) {
				c = className.compareTo(filter.className);
			}
			if (c == 0) {
				return true;
			} else if (c < 0) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return false;
	}

	private boolean containsWildcards(String pattern) {
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c != '.' && !Character.isJavaIdentifierPart(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts a glob pattern to a regular expression.
	 *
	 * @param pattern The glob pattern to convert
	 * @return the equivalent regular expression pattern
	 * @throws PatternSyntaxException if compilation of the regular expression fails
	 */
	private static Pattern createPattern(String pattern) throws PatternSyntaxException {
		int len = pattern.length();
		StringBuilder buf = new StringBuilder(len * 2);
		boolean isEscaped = false;
		for (int i = 0; i < len; i++) {
		    char c = pattern.charAt(i);
		    switch (c) {
		    case '\\':
		        // The backslash is an escape character.
		        if (!isEscaped) {
		            isEscaped = true;
		        } else {
		            buf.append(DOUBLE_BACKSLASH);
		            isEscaped = false;
		        }
		        break;
		    // Characters that have to be escaped in a regular expression.
		    case '(':
		    case ')':
		    case '{':
		    case '}':
		    case '.':
		    case '[':
		    case ']':
		    case '$':
		    case '^':
		    case '+':
		    case '|':
		        if (isEscaped) {
		            buf.append(DOUBLE_BACKSLASH);
		            isEscaped = false;
		        }
		        buf.append('\\');
		        buf.append(c);
		        break;
		    case '?':
		        if (!isEscaped) {
		            buf.append('.');
		        } else {
		            buf.append('\\');
		            buf.append(c);
		            isEscaped = false;
		        }
		        break;
		    case '*':
		        if (!isEscaped) {
		            buf.append(".*"); //$NON-NLS-1$
		        } else {
		            buf.append('\\');
		            buf.append(c);
		            isEscaped = false;
		        }
		        break;
		    default:
		        if (isEscaped) {
		            buf.append(DOUBLE_BACKSLASH);
		            isEscaped = false;
		        }
		        buf.append(c);
		        break;
		    }
		}
		if (isEscaped) {
		    buf.append(DOUBLE_BACKSLASH);
		    isEscaped= false;
		}
		return Pattern.compile(buf.toString());
	}
}
