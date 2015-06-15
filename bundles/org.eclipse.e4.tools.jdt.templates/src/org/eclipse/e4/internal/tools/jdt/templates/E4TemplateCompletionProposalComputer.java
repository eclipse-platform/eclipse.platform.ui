/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation (adapted from JDT's SWTTemplateCompletionProposalComputer)
 *******************************************************************************/
package org.eclipse.e4.internal.tools.jdt.templates;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.AbstractTemplateCompletionProposalComputer;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * Computer that computes the template proposals for the E4 context type.
 *
 */
@SuppressWarnings("restriction")
public class E4TemplateCompletionProposalComputer extends AbstractTemplateCompletionProposalComputer {

	/**
	 * The name of <code>javax.inject.Inject</code> used to detect
	 * if a project uses e4.
	 */
	private static final String E4_TYPE_NAME = "org.eclipse.e4.ui.di.Focus"; //$NON-NLS-1$

	/**
	 * Listener that resets the cached java project if its build path changes.
	 */
	private final class BuildPathChangeListener implements IElementChangedListener {

		@Override
		public void elementChanged(ElementChangedEvent event) {
			final IJavaProject javaProject = getCachedJavaProject();
			if (javaProject == null) {
				return;
			}

			final IJavaElementDelta[] children = event.getDelta().getChangedChildren();
			for (int i = 0; i < children.length; i++) {
				final IJavaElementDelta child = children[i];
				if (javaProject.equals(child.getElement())) {
					if (isClasspathChange(child)) {
						setCachedJavaProject(null);
					}
				}
			}
		}

		/**
		 * Does the delta indicate a classpath change?
		 * 
		 * @param delta the delta to inspect
		 * @return true if classpath has changed
		 */
		private boolean isClasspathChange(IJavaElementDelta delta) {
			final int flags = delta.getFlags();
			if (isClasspathChangeFlag(flags)) {
				return true;
			}

			if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
				final IJavaElementDelta[] children = delta.getAffectedChildren();
				for (int i = 0; i < children.length; i++) {
					if (isClasspathChangeFlag(children[i].getFlags())) {
						return true;
					}
				}
			}

			return false;
		}

		/**
		 * Do the flags indicate a classpath change?
		 * 
		 * @param flags the flags to inspect
		 * @return true if the flag flags a classpath change
		 */
		private boolean isClasspathChangeFlag(int flags) {
			if ((flags & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0) {
				return true;
			}

			if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) {
				return true;
			}

			if ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) {
				return true;
			}

			if ((flags & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0) {
				return true;
			}

			return false;
		}
	}

	/**
	 * Engine used to compute the proposals for this computer
	 */
	private final TemplateEngine fE4TemplateEngine;
	private final TemplateEngine fE4MembersTemplateEngine;
	private final TemplateEngine fE4StatementsTemplateEngine;

	/**
	 * The Java project of the compilation unit for which a template
	 * engine has been computed last time if any
	 */
	private IJavaProject fCachedJavaProject;
	/**
	 * Is org.eclipse.e4.core.services.IDisposable on class path of <code>fJavaProject</code>. Invalid
	 * if <code>fJavaProject</code> is <code>false</code>.
	 */
	private boolean fIsE4OnClasspath;

	public E4TemplateCompletionProposalComputer() {
		final ContextTypeRegistry templateContextRegistry = JavaPlugin.getDefault().getTemplateContextRegistry();
		fE4TemplateEngine = createTemplateEngine(templateContextRegistry, E4ContextType.ID_ALL);
		fE4MembersTemplateEngine = createTemplateEngine(templateContextRegistry, E4ContextType.ID_MEMBERS);
		fE4StatementsTemplateEngine = createTemplateEngine(templateContextRegistry, E4ContextType.ID_STATEMENTS);

		JavaCore.addElementChangedListener(new BuildPathChangeListener());
	}

	private static TemplateEngine createTemplateEngine(ContextTypeRegistry templateContextRegistry, String contextTypeId) {
		final TemplateContextType contextType = templateContextRegistry.getContextType(contextTypeId);
		Assert.isNotNull(contextType);
		return new TemplateEngine(contextType);
	}

	@Override
	protected TemplateEngine computeCompletionEngine(JavaContentAssistInvocationContext context) {
		final ICompilationUnit unit = context.getCompilationUnit();
		if (unit == null) {
			return null;
		}

		final IJavaProject javaProject = unit.getJavaProject();
		if (javaProject == null) {
			return null;
		}

		if (isE4OnClasspath(javaProject)) {
			final CompletionContext coreContext = context.getCoreContext();
			if (coreContext != null) {
				final int tokenLocation = coreContext.getTokenLocation();
				if ((tokenLocation & CompletionContext.TL_MEMBER_START) != 0) {
					return fE4MembersTemplateEngine;
				}
				if ((tokenLocation & CompletionContext.TL_STATEMENT_START) != 0) {
					return fE4StatementsTemplateEngine;
				}
			}
			return fE4TemplateEngine;
		}

		return null;
	}

	/**
	 * Tells whether E4 is on the given project's class path.
	 *
	 * @param javaProject the Java project
	 * @return <code>true</code> if the given project's class path
	 */
	private synchronized boolean isE4OnClasspath(IJavaProject javaProject) {
		if (!javaProject.equals(fCachedJavaProject)) {
			fCachedJavaProject = javaProject;
			try {
				final IType type = javaProject.findType(E4_TYPE_NAME);
				fIsE4OnClasspath = type != null;
			} catch (final JavaModelException e) {
				fIsE4OnClasspath = false;
			}
		}
		return fIsE4OnClasspath;
	}

	/**
	 * Returns the cached Java project.
	 *
	 * @return the cached Java project or <code>null</code> if none
	 */
	private synchronized IJavaProject getCachedJavaProject() {
		return fCachedJavaProject;
	}

	/**
	 * Set the cached Java project.
	 *
	 * @param project or <code>null</code> to reset the cache
	 */
	private synchronized void setCachedJavaProject(IJavaProject project) {
		fCachedJavaProject = project;
	}

}
