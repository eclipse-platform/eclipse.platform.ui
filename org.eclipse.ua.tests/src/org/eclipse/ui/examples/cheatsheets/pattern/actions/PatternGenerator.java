/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.cheatsheets.pattern.actions;

import java.util.StringTokenizer;

import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.ui.cheatsheets.*;

public class PatternGenerator {

	
	private static IType findType(IJavaProject project, String typeName) throws JavaModelException {
		if (project.exists()) {
			return project.findType(typeName);
		}
		return null;
	}

	public static void generate(ICheatSheetManager csm) {
		String project = csm.getData("project"); //$NON-NLS-1$
		String pattern = csm.getData("pattern"); //$NON-NLS-1$
		String thisValue = csm.getData("this"); //$NON-NLS-1$
		String values = csm.getData("files"); //$NON-NLS-1$

		StringTokenizer tokenizer = new StringTokenizer(values, ",");
		String[] files = new String[tokenizer.countTokens()];
		for(int i = 0; tokenizer.hasMoreTokens(); i++) {
			files[i] = tokenizer.nextToken();
		}
			
		if (project == null)
			return;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject wproject = workspace.getRoot().getProject(project);
		IJavaProject jproject = JavaCore.create(wproject);

		if (pattern != null && pattern.equals("Factory")) //$NON-NLS-1$
			handleFactoryFileAutomation(pattern, csm, jproject, thisValue, files);
		else if (pattern != null && pattern.equals("Singleton")) //$NON-NLS-1$
			handleSingletonFileAutomation(pattern, csm, jproject, thisValue, files);
		else if (pattern != null && pattern.equals("Visitor")) //$NON-NLS-1$
			handleVisitorFileAutomation(pattern, csm, jproject, thisValue, files);
	}

	private static void handleSingletonFileAutomation(String pattern, ICheatSheetManager csm, IJavaProject jproject, String thisValue, String[] files) {
		IType mytype = null;
		String singletonClassName = files[0];
		if (singletonClassName == null)
			return;

		if (thisValue != null) {
			if (thisValue.equals(singletonClassName)) { //$NON-NLS-1$
				try {
					mytype = findType(jproject, singletonClassName);
					StringBuffer sb = new StringBuffer();
					sb.append("public static synchronized "); //$NON-NLS-1$
					sb.append(singletonClassName + " getInstance () {"); //$NON-NLS-1$
					sb.append("\n\t if(instance == null)\n\t\t instance = new " + singletonClassName + "();"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("\n\t return instance;"); //$NON-NLS-1$
					sb.append("\n}\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);

					sb = new StringBuffer();
					sb.append("public void print(String s) {\n\t System.out.println(s);\n}\n"); //$NON-NLS-1$
					mytype.createMethod(sb.toString(), null, false, null);

					sb = new StringBuffer();
					sb.append("private static " + singletonClassName + " instance;\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
					mytype.createField(sb.toString(), null, false, null);

				} catch (Exception je) {
				}

			}
		} else {
			try {
				mytype = findType(jproject, "TestPattern"); //$NON-NLS-1$
				StringBuffer sb = new StringBuffer();
				sb.append("public static "); //$NON-NLS-1$
				sb.append("void main (String[] args) {"); //$NON-NLS-1$
				sb.append("\n\t" + singletonClassName + " singleInstance = " + singletonClassName + ".getInstance();"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sb.append("\n\t singleInstance.print(\"Printed by the single instance.\");"); //$NON-NLS-1$
				sb.append("\n}\n\n"); //$NON-NLS-1$
				if (mytype != null)
					mytype.createMethod(sb.toString(), null, false, null);
			} catch (Exception je) {
			}
		}

	}
	private static void handleVisitorFileAutomation(String pattern, ICheatSheetManager csm, IJavaProject jproject, String thisValue, String[] files) {
		IType mytype = null;
		String visitorClassName = files[0];
		if (visitorClassName == null)
			return;
		String classToVisitClassName = files[1];
		if (classToVisitClassName == null)
			return;

		if (thisValue != null) {
			if (thisValue.equals(visitorClassName)) { //$NON-NLS-1$
				try {
					mytype = findType(jproject, visitorClassName);
					StringBuffer sb = new StringBuffer();
					sb.append("public void visit (" + classToVisitClassName + " v) {"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("\n\t System.out.println(\"Visiting ....\");"); //$NON-NLS-1$
					sb.append("\n\t System.out.println(v.getString());"); //$NON-NLS-1$
					sb.append("\n}\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);
				} catch (Exception je) {
				}

			}

			if (thisValue.equals(classToVisitClassName)) { //$NON-NLS-1$
				try {
					mytype = findType(jproject, classToVisitClassName);
					StringBuffer sb = new StringBuffer();
					sb.append("public void accept (" + visitorClassName + " v) {"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("\n\t v.visit(this);"); //$NON-NLS-1$
					sb.append("\n}\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);

					sb = new StringBuffer();
					sb.append("public String visitString = \"This is the String in the class to visit!\";\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createField(sb.toString(), null, false, null);

					sb = new StringBuffer();
					sb.append("public String getString() { \n\t return visitString; \n}\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);

				} catch (Exception je) {
				}
			}
		} else {
			try {
				mytype = findType(jproject, "TestPattern"); //$NON-NLS-1$
				StringBuffer sb = new StringBuffer();
				sb.append("public static "); //$NON-NLS-1$
				sb.append("void main (String[] args) {"); //$NON-NLS-1$
				sb.append("\n\t" + visitorClassName + " visitor = new " + visitorClassName + "();"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sb.append("\n\t" + classToVisitClassName + " objectToVisit = new " + classToVisitClassName + "();"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sb.append("\n\t objectToVisit.accept(visitor);"); //$NON-NLS-1$

				sb.append("\n}\n\n"); //$NON-NLS-1$
				if (mytype != null)
					mytype.createMethod(sb.toString(), null, false, null);
			} catch (Exception je) {
			}
		}

	}
	private static void handleFactoryFileAutomation(String pattern, ICheatSheetManager csm, IJavaProject jproject, String thisValue, String[] files) {

		IType mytype = null;
		String factoryClassName = files[0];
		if (factoryClassName == null)
			return;
		String baseClassName = files[1];
		String derived = files[2];
		String secondderived = files[3];

		if (baseClassName == null || derived == null || secondderived == null)
			return;

		if (jproject instanceof IJavaProject) {
			if (baseClassName.equals(thisValue)) { //$NON-NLS-1$
				try {
					mytype = findType(jproject, baseClassName);
					StringBuffer sb = new StringBuffer();
					sb.append("public "); //$NON-NLS-1$
					sb.append("void printObjectType () {"); //$NON-NLS-1$
					sb.append("\n}\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);
				} catch (Exception je) {
				}
			} else if (derived.equals(thisValue)) { //$NON-NLS-1$
				try {
					mytype = findType(jproject, derived);
					StringBuffer sb = new StringBuffer();
					sb.append("public "); //$NON-NLS-1$
					sb.append("void printObjectType () {"); //$NON-NLS-1$
					sb.append("\n\tSystem.out.println(\"Type is: " + derived + "\");\n}\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);
				} catch (Exception je) {
				}
			} else if (secondderived.equals(thisValue)) { //$NON-NLS-1$
				try {
					mytype = findType(jproject, secondderived);
					StringBuffer sb = new StringBuffer();
					sb.append("public "); //$NON-NLS-1$
					sb.append("void printObjectType () {"); //$NON-NLS-1$
					sb.append("\n\tSystem.out.println(\"Type is: " + secondderived + "\");\n}\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);
				} catch (Exception je) {
				}
			} else if (factoryClassName.equals(thisValue)) { //$NON-NLS-1$
				try {
					mytype = findType(jproject, factoryClassName);
					StringBuffer sb = new StringBuffer();
					sb.append("public "); //$NON-NLS-1$
					sb.append(baseClassName + " getObjectFromFactory (String derivedTypeIndicator) {"); //$NON-NLS-1$
					sb.append("\n\t if(derivedTypeIndicator.equals(\"baseOne\"))\n\t return new " + derived + "();"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("\n\t if(derivedTypeIndicator.equals(\"baseTwo\"))\n\t return new " + secondderived + "();"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("\n\t else\n\t return new " + baseClassName + "();"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append("\n}\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);
				} catch (Exception je) {
				}
			} else { //$NON-NLS-1$
				try {
					mytype = findType(jproject, "TestPattern"); //$NON-NLS-1$ //$NON-NLS-2$
					StringBuffer sb = new StringBuffer();
					sb.append("public static "); //$NON-NLS-1$
					sb.append("void main (String[] args) {"); //$NON-NLS-1$
					sb.append("\n\t" + factoryClassName + " factory = new " + factoryClassName + "();"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					sb.append("\n\tSystem.out.println(\"First Type From Factory: \");\n\t factory.getObjectFromFactory(\"baseOne\").printObjectType();\n"); //$NON-NLS-1$
					sb.append("\tSystem.out.println(\"Second Type From Factory: \");\n\t factory.getObjectFromFactory(\"baseTwo\").printObjectType();\n}\n\n"); //$NON-NLS-1$
					if (mytype != null)
						mytype.createMethod(sb.toString(), null, false, null);
				} catch (Exception je) {
				}
			}
		}

	}
}
