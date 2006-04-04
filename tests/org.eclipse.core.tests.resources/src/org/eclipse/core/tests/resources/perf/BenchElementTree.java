/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import java.util.Vector;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.resources.OldCorePerformanceTest;

/**
 * Benchmarks for <code>ElementTree</code>.
 */
public class BenchElementTree extends OldCorePerformanceTest {
	static String[] javaLangUnits = {"AbstractMethodError.java", "ArithmeticException.java", "ArrayIndexOutOfBoundsException.java", "ArrayStoreException.java", "Boolean.java", //
			"Byte.java", "Character.java", "Class.java", "ClassCastException.java", "ClassCircularityError.java", "ClassFormatError.java", "ClassLoader.java", "ClassNotFoundException.java", //
			"Cloneable.java", "CloneNotSupportedException.java", "Compiler.java", "Double.java", "Error.java", "Exception.java", "ExceptionInInitializerError.java", "Float.java", "FloatingDecimal.java", //
			"IllegalAccessError.java", "IllegalAccessException.java", "IllegalArgumentException.java", "IllegalMonitorStateException.java", "IllegalStateException.java", "IllegalThreadStateException.java", //
			"IncompatibleClassChangeError.java", "IndexOutOfBoundsException.java", "InstantiationError.java", "InstantiationException.java", "Integer.java", "InternalError.java", "InterruptedException.java", //
			"LinkageError.java", "Long.java", "Math.java", "NegativeArraySizeException.java", "NoClassDefFoundError.java", "NoSuchFieldError.java", "NoSuchFieldException.java", "NoSuchMethodError.java", //
			"NoSuchMethodException.java", "NullPointerException.java", "Number.java", "NumberFormatException.java", "Object.java", "OutOfMemoryError.java", "Process.java", "Runnable.java", //
			"Runtime.java", "RuntimeException.java", "SecurityException.java", "SecurityManager.java", "Short.java", "StackOverflowError.java", "String.java", "StringBuffer.java", //
			"StringIndexOutOfBoundsException.java", "System.java", "Thread.java", "ThreadDeath.java", "ThreadGroup.java", "Throwable.java", "UnknownError.java", "UnsatisfiedLinkError.java", //
			"VerifyError.java", "VirtualMachineError.java", "Void.java"};

	static final IPath solution = Path.ROOT.append("solution");
	static final IPath project = solution.append("project");
	static final IPath folder = project.append("folder");
	static final IPath[] files = getFilePaths();

	public BenchElementTree() {
		super();
	}

	public BenchElementTree(String name) {
		super(name);
	}

	/**
	 * Tests the performance of the createElement operation.
	 */
	public void testCreateElement() {
		new PerformanceTestRunner() {
			protected void test() {
				createTestTree(false);
			}
		}.run(this, 10, 400);
	}

	/**
	 * Tests the performance of the deleteElement operation.
	 */
	public void benchDeleteElement() {
		final int repeat = 400;

		/* create copies of the original tree */
		ElementTree[] trees = new ElementTree[repeat];
		for (int i = 0; i < repeat; i++) {
			trees[i] = createTestTree(false);
		}

		startBench();
		for (int rep = repeat; --rep >= 0;) {
			for (int i = 0, len = files.length; i < len; ++i) {
				trees[rep].deleteElement(files[i]);
			}
		}

		stopBench("benchDeleteElement", repeat * files.length);
	}

	/**
	 * Tests the performance of the getElementData operation.
	 */
	public void benchGetElementData() {
		final int repeat = 500;

		ElementTree tree = createTestTree(false);

		startBench();

		for (int rep = repeat; --rep >= 0;) {
			for (int i = 0, len = files.length; i < len; ++i) {
				tree.getElementData(files[i]);
			}
		}

		stopBench("benchGetElementData", repeat * files.length);
	}

	/**
	 * Tests the performance of the mergeDeltaChain operation.
	 */
	public void benchMergeDeltaChain() {
		final int repeat = 50;

		/* create all the test trees */
		ElementTree[] bases = new ElementTree[repeat];
		ElementTree[][] chains = new ElementTree[repeat][];
		for (int i = 0; i < repeat; i++) {
			bases[i] = createTestTree(true);
			chains[i] = buildDeltaChain(createTestTree(false));
		}
		startBench();

		for (int i = repeat; --i >= 0;) {
			bases[i].mergeDeltaChain(folder, chains[i]);
		}

		stopBench("benchMergeDeltaChain", repeat);
	}

	/**
	 * Tests a typical series of ElementTree operations, where
	 * a new delta is generated for each operation.
	 */
	public void benchRoutineOperations() {
		final int repeat = 75;

		int opCount = 0;
		startBench();

		for (int i = 0; i < repeat; i++) {
			opCount += doRoutineOperations();
		}

		stopBench("benchRoutineOperations", opCount);
	}

	/**
	 * Tests the performance of the setElementData operation.
	 */
	public void benchSetElementData() {
		ElementTree tree = createTestTree(false);
		Object data = new Object();

		startBench();

		for (int rep = 500; --rep >= 0;) {
			for (int i = 0, len = files.length; i < len; ++i) {
				tree.setElementData(files[i], data);
			}
		}

		stopBench("benchSetElementData", 500 * files.length);
	}

	/**
	 * Does several routine operations on the given tree.  
	 * Returns an array of all the intermediary element trees.
	 */
	private ElementTree[] buildDeltaChain(ElementTree tree) {
		Vector trees = new Vector();
		trees.addElement(tree);

		int repeat = 1;

		/* create file elements */
		tree = tree.newEmptyDelta();
		IPath[] filePaths = getFilePaths();
		for (int i = 0; i < filePaths.length; i++) {
			Object data = filePaths[i].toString();
			tree.createElement(filePaths[i], data);

			tree.immutable();
			trees.addElement(tree);
			tree = tree.newEmptyDelta();
		}

		/* modify the data of all file elements a few times */
		for (int i = 0; i < repeat; i++) {
			Object data = "data" + i;
			for (int f = 0; f < filePaths.length; f++) {
				tree.setElementData(filePaths[f], data);

				tree.immutable();
				trees.addElement(tree);
				tree = tree.newEmptyDelta();
			}
		}

		/* delete all file elements */
		for (int i = 0; i < filePaths.length; i++) {
			tree.deleteElement(filePaths[i]);
			tree.immutable();
			trees.addElement(tree);
			tree = tree.newEmptyDelta();
		}

		ElementTree[] results = new ElementTree[trees.size()];
		trees.copyInto(results);
		return results;
	}

	/**
	 * Creates a test element tree.  If withDeltas is true,
	 * a new delta is created after each operation.
	 */
	ElementTree createTestTree(boolean withDeltas) {
		ElementTree tree = new ElementTree();
		Object data = new Object();

		tree.createElement(solution, data);

		if (withDeltas) {
			tree.immutable();
			tree = tree.newEmptyDelta();
		}
		tree.createElement(project, data);
		if (withDeltas) {
			tree.immutable();
			tree = tree.newEmptyDelta();
		}
		tree.createElement(folder, data);

		for (int i = 0, len = javaLangUnits.length; i < len; ++i) {
			if (withDeltas) {
				tree.immutable();
				tree = tree.newEmptyDelta();
			}
			IPath file = folder.append(javaLangUnits[i]);
			tree.createElement(file, data);
		}
		return tree;
	}

	/**
	 * Tests a typical series of ElementTree operations, where
	 * a new delta is generated for each operation.
	 * Returns the number of basic ElementTree operations performed.
	 */
	private int doRoutineOperations() {
		ElementTree tree = createTestTree(true);
		int repeat = 1;

		/* modify the data of all file elements a few times */
		for (int i = 0; i < repeat; i++) {
			Object data = "data" + i;
			for (int f = 0; f < files.length; f++) {
				tree = tree.newEmptyDelta();
				tree.setElementData(files[f], data);
				tree.immutable();
			}
		}

		/* delete all file elements */
		for (int i = 0; i < files.length; i++) {
			tree = tree.newEmptyDelta();
			tree.deleteElement(files[i]);
			tree.immutable();
		}

		return (repeat + 2) * files.length;
	}

	static IPath[] getFilePaths() {
		IPath[] jcuIDs = new IPath[javaLangUnits.length];
		for (int i = 0, len = javaLangUnits.length; i < len; ++i) {
			jcuIDs[i] = folder.append(javaLangUnits[i]);
		}
		return jcuIDs;
	}

	/**
	 * The environment should be set-up in the main method.
	 */
	protected void setUp() throws Exception {
		//benchmarks don't use the core testing infrastructure
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(BenchElementTree.class.getName());

		suite.addTest(new BenchElementTree("benchCreateElement"));
		suite.addTest(new BenchElementTree("benchDeleteElement"));
		suite.addTest(new BenchElementTree("benchDeltaLargeTreeFewChanges"));
		suite.addTest(new BenchElementTree("benchDeltaLargeTreeManyChangesInOneLayer"));
		suite.addTest(new BenchElementTree("benchDeltaSmallTreeManyChangesInSeperateLayers"));
		suite.addTest(new BenchElementTree("benchGetElementData"));
		//	suite.addTest(new BenchElementTree("benchMergeDeltaChain"));
		suite.addTest(new BenchElementTree("benchReverseDeltaLargeTreeFewChanges"));
		suite.addTest(new BenchElementTree("benchReverseDeltaLargeTreeManyChangesInOneLayer"));
		suite.addTest(new BenchElementTree("benchReverseDeltaSmallTreeManyChangesInSeperateLayers"));
		suite.addTest(new BenchElementTree("benchRoutineOperations"));
		suite.addTest(new BenchElementTree("benchSetElementData"));

		return suite;
	}

	/**
	 * 
	 */
	protected void tearDown() throws Exception {
		//ElementTree tests don't use the CoreTest infrastructure
	}
}
