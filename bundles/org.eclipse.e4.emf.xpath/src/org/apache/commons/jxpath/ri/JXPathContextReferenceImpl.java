/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.ExceptionHandler;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathFunctionNotFoundException;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.JXPathTypeConversionException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Path;
import org.apache.commons.jxpath.ri.compiler.TreeCompiler;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.VariablePointerFactory;
import org.apache.commons.jxpath.ri.model.beans.BeanPointerFactory;
import org.apache.commons.jxpath.ri.model.beans.CollectionPointerFactory;
import org.apache.commons.jxpath.ri.model.container.ContainerPointerFactory;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPointerFactory;
import org.apache.commons.jxpath.util.ClassLoaderUtil;
import org.apache.commons.jxpath.util.ReverseComparator;
import org.apache.commons.jxpath.util.TypeUtils;

/**
 * The reference implementation of JXPathContext.
 */
public class JXPathContextReferenceImpl extends JXPathContext {

    /**
     * Change this to {@code false} to disable soft caching of
     * CompiledExpressions.
     */
    public static final boolean USE_SOFT_CACHE = true;

    private static final Compiler COMPILER = new TreeCompiler();
    private static Map compiled = new HashMap();
    private static int cleanupCount = 0;

    private static NodePointerFactory[] nodeFactoryArray = null;
    // The frequency of the cache cleanup
    private static final int CLEANUP_THRESHOLD = 500;
    private static final Vector nodeFactories = new Vector();

    static {
        nodeFactories.add(new CollectionPointerFactory());
        nodeFactories.add(new BeanPointerFactory());
        nodeFactories.add(new DynamicPointerFactory());
        nodeFactories.add(new VariablePointerFactory());

        // DOM  factory is only registered if DOM support is on the classpath
        final Object domFactory = allocateConditionally(
                "org.apache.commons.jxpath.ri.model.dom.DOMPointerFactory",
                "org.w3c.dom.Node");
        if (domFactory != null) {
            nodeFactories.add(domFactory);
        }

        // JDOM  factory is only registered if JDOM is on the classpath
        final Object jdomFactory = allocateConditionally(
                "org.apache.commons.jxpath.ri.model.jdom.JDOMPointerFactory",
                "org.jdom.Document");
        if (jdomFactory != null) {
            nodeFactories.add(jdomFactory);
        }

        // DynaBean factory is only registered if BeanUtils are on the classpath
        final Object dynaBeanFactory =
            allocateConditionally(
                "org.apache.commons.jxpath.ri.model.dynabeans."
                    + "DynaBeanPointerFactory",
                "org.apache.commons.beanutils.DynaBean");
        if (dynaBeanFactory != null) {
            nodeFactories.add(dynaBeanFactory);
        }

        nodeFactories.add(new ContainerPointerFactory());
        createNodeFactoryArray();
    }

    /**
     * Create the default node factory array.
     */
    private static synchronized void createNodeFactoryArray() {
        if (nodeFactoryArray == null) {
            nodeFactoryArray =
                (NodePointerFactory[]) nodeFactories.
                    toArray(new NodePointerFactory[nodeFactories.size()]);
            Arrays.sort(nodeFactoryArray, (a, b) -> {
                final int orderA = ((NodePointerFactory) a).getOrder();
                final int orderB = ((NodePointerFactory) b).getOrder();
                return orderA - orderB;
            });
        }
    }

    /**
     * Call this with a custom NodePointerFactory to add support for
     * additional types of objects.  Make sure the factory returns
     * a name that puts it in the right position on the list of factories.
     * @param factory NodePointerFactory to add
     */
    public static void addNodePointerFactory(final NodePointerFactory factory) {
        synchronized (nodeFactories) {
            nodeFactories.add(factory);
            nodeFactoryArray = null;
        }
    }

    /**
     * Removes support for additional types of objects.
     *
     * @param factory NodePointerFactory to remove
     * @return true if this implementation contained the specified element
     * @since 1.4.0
     */
    public static boolean removeNodePointerFactory(final NodePointerFactory factory) {
        synchronized (nodeFactories) {
            final boolean remove = nodeFactories.remove(factory);
            nodeFactoryArray = null;
            return remove;
        }
    }

    /**
     * Gets the registered NodePointerFactories.
     * @return NodePointerFactory[]
     */
    public static NodePointerFactory[] getNodePointerFactories() {
        return nodeFactoryArray;
    }

    /** Namespace resolver */
    protected NamespaceResolver namespaceResolver;

    private Pointer rootPointer;
    private Pointer contextPointer;

    /**
     * Create a new JXPathContextReferenceImpl.
     * @param parentContext parent context
     * @param contextBean Object
     */
    protected JXPathContextReferenceImpl(final JXPathContext parentContext,
            final Object contextBean) {
        this(parentContext, contextBean, null);
    }

    /**
     * Create a new JXPathContextReferenceImpl.
     * @param parentContext parent context
     * @param contextBean Object
     * @param contextPointer context pointer
     */
    public JXPathContextReferenceImpl(final JXPathContext parentContext,
            final Object contextBean, final Pointer contextPointer) {
        super(parentContext, contextBean);

        synchronized (nodeFactories) {
            createNodeFactoryArray();
        }

        if (contextPointer != null) {
            this.contextPointer = contextPointer;
            this.rootPointer =
                NodePointer.newNodePointer(
                    new QName(null, "root"),
                    contextPointer.getRootNode(),
                    getLocale());
        }
        else {
            this.contextPointer =
                NodePointer.newNodePointer(
                    new QName(null, "root"),
                    contextBean,
                    getLocale());
            this.rootPointer = this.contextPointer;
        }

        NamespaceResolver parentNR = null;
        if (parentContext instanceof JXPathContextReferenceImpl) {
            parentNR = ((JXPathContextReferenceImpl) parentContext).getNamespaceResolver();
        }
        namespaceResolver = new NamespaceResolver(parentNR);
        namespaceResolver
                .setNamespaceContextPointer((NodePointer) this.contextPointer);
    }

    /**
     * Returns a static instance of TreeCompiler.
     *
     * Override this to return an alternate compiler.
     * @return Compiler
     */
    protected Compiler getCompiler() {
        return COMPILER;
    }

    @Override
    protected CompiledExpression compilePath(final String xpath) {
        return new JXPathCompiledExpression(xpath, compileExpression(xpath));
    }

    /**
     * Compile the given expression.
     * @param xpath to compile
     * @return Expression
     */
    private Expression compileExpression(final String xpath) {
        Expression expr;

        synchronized (compiled) {
            if (USE_SOFT_CACHE) {
                expr = null;
                final SoftReference ref = (SoftReference) compiled.get(xpath);
                if (ref != null) {
                    expr = (Expression) ref.get();
                }
            }
            else {
                expr = (Expression) compiled.get(xpath);
            }
        }

        if (expr != null) {
            return expr;
        }

        expr = (Expression) Parser.parseExpression(xpath, getCompiler());

        synchronized (compiled) {
            if (USE_SOFT_CACHE) {
                if (cleanupCount++ >= CLEANUP_THRESHOLD) {
                    final Iterator it = compiled.entrySet().iterator();
                    while (it.hasNext()) {
                        final Entry me = (Entry) it.next();
                        if (((SoftReference) me.getValue()).get() == null) {
                            it.remove();
                        }
                    }
                    cleanupCount = 0;
                }
                compiled.put(xpath, new SoftReference(expr));
            }
            else {
                compiled.put(xpath, expr);
            }
        }

        return expr;
    }

    /**
     * Traverses the xpath and returns the resulting object. Primitive
     * types are wrapped into objects.
     * @param xpath expression
     * @return Object found
     */
    @Override
    public Object getValue(final String xpath) {
        final Expression expression = compileExpression(xpath);
// TODO: (work in progress) - trying to integrate with Xalan
//        Object ctxNode = getNativeContextNode(expression);
//        if (ctxNode != null) {
//            System.err.println("WILL USE XALAN: " + xpath);
//            CachedXPathAPI api = new CachedXPathAPI();
//            try {
//                if (expression instanceof Path) {
//                    Node node = api.selectSingleNode((Node)ctxNode, xpath);
//                    System.err.println("NODE: " + node);
//                    if (node == null) {
//                        return null;
//                    }
//                    return new DOMNodePointer(node, null).getValue();
//                }
//                else {
//                    XObject object = api.eval((Node)ctxNode, xpath);
//                    switch (object.getType()) {
//                    case XObject.CLASS_STRING: return object.str();
//                    case XObject.CLASS_NUMBER: return new Double(object.num());
//                    case XObject.CLASS_BOOLEAN: return new Boolean(object.bool());
//                    default:
//                        System.err.println("OTHER TYPE: " + object.getTypeString());
//                    }
//                }
//            }
//            catch (TransformerException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            return
//        }

        return getValue(xpath, expression);
    }

//    private Object getNativeContextNode(Expression expression) {
//        Object node = getNativeContextNode(getContextBean());
//        if (node == null) {
//            return null;
//        }
//
//        List vars = expression.getUsedVariables();
//        if (vars != null) {
//            return null;
//        }
//
//        return node;
//    }

//    private Object getNativeContextNode(Object bean) {
//        if (bean instanceof Number || bean instanceof String || bean instanceof Boolean) {
//            return bean;
//        }
//        if (bean instanceof Node) {
//            return (Node)bean;
//        }
//
//        if (bean instanceof Container) {
//            bean = ((Container)bean).getValue();
//            return getNativeContextNode(bean);
//        }
//
//        return null;
//    }

    /**
     * Gets the value indicated.
     * @param xpath String
     * @param expr Expression
     * @return Object
     */
    public Object getValue(final String xpath, final Expression expr) {
        Object result = expr.computeValue(getEvalContext());
        if (result == null) {
            if (expr instanceof Path && !isLenient()) {
                throw new JXPathNotFoundException("No value for xpath: "
                        + xpath);
            }
            return null;
        }
        if (result instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) result;
            result = ctx.getSingleNodePointer();
            if (!isLenient() && result == null) {
                throw new JXPathNotFoundException("No value for xpath: "
                        + xpath);
            }
        }
        if (result instanceof NodePointer) {
            result = ((NodePointer) result).getValuePointer();
            if (!isLenient()) {
                NodePointer.verify((NodePointer) result);
            }
            result = ((NodePointer) result).getValue();
        }
        return result;
    }

    /**
     * Calls getValue(xpath), converts the result to the required type
     * and returns the result of the conversion.
     * @param xpath expression
     * @param requiredType Class
     * @return Object
     */
    @Override
    public Object getValue(final String xpath, final Class requiredType) {
        final Expression expr = compileExpression(xpath);
        return getValue(xpath, expr, requiredType);
    }

    /**
     * Gets the value indicated.
     * @param xpath expression
     * @param expr compiled Expression
     * @param requiredType Class
     * @return Object
     */
    public Object getValue(final String xpath, final Expression expr, final Class requiredType) {
        Object value = getValue(xpath, expr);
        if (value != null && requiredType != null) {
            if (!TypeUtils.canConvert(value, requiredType)) {
                throw new JXPathTypeConversionException(
                    "Invalid expression type. '"
                        + xpath
                        + "' returns "
                        + value.getClass().getName()
                        + ". It cannot be converted to "
                        + requiredType.getName());
            }
            value = TypeUtils.convert(value, requiredType);
        }
        return value;
    }

    /**
     * Traverses the xpath and returns a Iterator of all results found
     * for the path. If the xpath matches no properties
     * in the graph, the Iterator will not be null.
     * @param xpath expression
     * @return Iterator
     */
    @Override
    public Iterator iterate(final String xpath) {
        return iterate(xpath, compileExpression(xpath));
    }

    /**
     * Traverses the xpath and returns a Iterator of all results found
     * for the path. If the xpath matches no properties
     * in the graph, the Iterator will not be null.
     * @param xpath expression
     * @param expr compiled Expression
     * @return Iterator
     */
    public Iterator iterate(final String xpath, final Expression expr) {
        return expr.iterate(getEvalContext());
    }

    @Override
    public Pointer getPointer(final String xpath) {
        return getPointer(xpath, compileExpression(xpath));
    }

    /**
     * Gets a pointer to the specified path/expression.
     * @param xpath String
     * @param expr compiled Expression
     * @return Pointer
     */
    public Pointer getPointer(final String xpath, final Expression expr) {
        Object result = expr.computeValue(getEvalContext());
        if (result instanceof EvalContext) {
            result = ((EvalContext) result).getSingleNodePointer();
        }
        if (result instanceof Pointer) {
            if (!isLenient() && !((NodePointer) result).isActual()) {
                throw new JXPathNotFoundException("No pointer for xpath: "
                        + xpath);
            }
            return (Pointer) result;
        }
        return NodePointer.newNodePointer(null, result, getLocale());
    }

    @Override
    public void setValue(final String xpath, final Object value) {
        setValue(xpath, compileExpression(xpath), value);
    }

    /**
     * Sets the value of xpath to value.
     * @param xpath path
     * @param expr compiled Expression
     * @param value Object
     */
    public void setValue(final String xpath, final Expression expr, final Object value) {
        try {
            setValue(xpath, expr, value, false);
        }
        catch (final Throwable ex) {
            throw new JXPathException(
                "Exception trying to set value with xpath " + xpath, ex);
        }
    }

    @Override
    public Pointer createPath(final String xpath) {
        return createPath(xpath, compileExpression(xpath));
    }

    /**
     * Create the given path.
     * @param xpath String
     * @param expr compiled Expression
     * @return resulting Pointer
     */
    public Pointer createPath(final String xpath, final Expression expr) {
        try {
            final Object result = expr.computeValue(getEvalContext());
            Pointer pointer;

            if (result instanceof Pointer) {
                pointer = (Pointer) result;
            }
            else if (result instanceof EvalContext) {
                final EvalContext ctx = (EvalContext) result;
                pointer = ctx.getSingleNodePointer();
            }
            else {
                checkSimplePath(expr);
                // This should never happen
                throw new JXPathException("Cannot create path:" + xpath);
            }
            return ((NodePointer) pointer).createPath(this);
        }
        catch (final Throwable ex) {
            throw new JXPathException(
                "Exception trying to create xpath " + xpath,
                ex);
        }
    }

    @Override
    public Pointer createPathAndSetValue(final String xpath, final Object value) {
        return createPathAndSetValue(xpath, compileExpression(xpath), value);
    }

    /**
     * Create the given path setting its value to value.
     * @param xpath String
     * @param expr compiled Expression
     * @param value Object
     * @return resulting Pointer
     */
    public Pointer createPathAndSetValue(final String xpath, final Expression expr,
            final Object value) {
        try {
            return setValue(xpath, expr, value, true);
        }
        catch (final Throwable ex) {
            throw new JXPathException(
                "Exception trying to create xpath " + xpath,
                ex);
        }
    }

    /**
     * Sets the specified value.
     * @param xpath path
     * @param expr compiled Expression
     * @param value destination value
     * @param create whether to create missing node(s)
     * @return Pointer created
     */
    private Pointer setValue(final String xpath, final Expression expr, final Object value,
            final boolean create) {
        final Object result = expr.computeValue(getEvalContext());
        Pointer pointer;

        if (result instanceof Pointer) {
            pointer = (Pointer) result;
        }
        else if (result instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) result;
            pointer = ctx.getSingleNodePointer();
        }
        else {
            if (create) {
                checkSimplePath(expr);
            }

            // This should never happen
            throw new JXPathException("Cannot set value for xpath: " + xpath);
        }
        if (create) {
            pointer = ((NodePointer) pointer).createPath(this, value);
        }
        else {
            pointer.setValue(value);
        }
        return pointer;
    }

    /**
     * Checks if the path follows the JXPath restrictions on the type
     * of path that can be passed to create... methods.
     * @param expr Expression to check
     */
    private void checkSimplePath(final Expression expr) {
        if (!(expr instanceof LocationPath)
            || !((LocationPath) expr).isSimplePath()) {
            throw new JXPathInvalidSyntaxException(
                "JXPath can only create a path if it uses exclusively "
                    + "the child:: and attribute:: axes and has "
                    + "no context-dependent predicates");
        }
    }

    /**
     * Traverses the xpath and returns an Iterator of Pointers.
     * A Pointer provides easy access to a property.
     * If the xpath matches no properties
     * in the graph, the Iterator be empty, but not null.
     * @param xpath expression
     * @return Iterator
     */
    @Override
    public Iterator iteratePointers(final String xpath) {
        return iteratePointers(xpath, compileExpression(xpath));
    }

    /**
     * Traverses the xpath and returns an Iterator of Pointers.
     * A Pointer provides easy access to a property.
     * If the xpath matches no properties
     * in the graph, the Iterator be empty, but not null.
     * @param xpath expression
     * @param expr compiled Expression
     * @return Iterator
     */
    public Iterator iteratePointers(final String xpath, final Expression expr) {
        return expr.iteratePointers(getEvalContext());
    }

    @Override
    public void removePath(final String xpath) {
        removePath(xpath, compileExpression(xpath));
    }

    /**
     * Remove the specified path.
     * @param xpath expression
     * @param expr compiled Expression
     */
    public void removePath(final String xpath, final Expression expr) {
        try {
            final NodePointer pointer = (NodePointer) getPointer(xpath, expr);
            if (pointer != null) {
                pointer.remove();
            }
        }
        catch (final Throwable ex) {
            throw new JXPathException(
                "Exception trying to remove xpath " + xpath,
                ex);
        }
    }

    @Override
    public void removeAll(final String xpath) {
        removeAll(xpath, compileExpression(xpath));
    }

    /**
     * Remove all matching nodes.
     * @param xpath expression
     * @param expr compiled Expression
     */
    public void removeAll(final String xpath, final Expression expr) {
        try {
            final ArrayList list = new ArrayList();
            Iterator it = expr.iteratePointers(getEvalContext());
            while (it.hasNext()) {
                list.add(it.next());
            }
            Collections.sort(list, ReverseComparator.INSTANCE);
            it = list.iterator();
            if (it.hasNext()) {
                final NodePointer pointer = (NodePointer) it.next();
                pointer.remove();
                while (it.hasNext()) {
                    removePath(((NodePointer) it.next()).asPath());
                }
            }
        }
        catch (final Throwable ex) {
            throw new JXPathException(
                "Exception trying to remove all for xpath " + xpath,
                ex);
        }
    }

    @Override
    public JXPathContext getRelativeContext(final Pointer pointer) {
        final Object contextBean = pointer.getNode();
        if (contextBean == null) {
            throw new JXPathException(
                "Cannot create a relative context for a non-existent node: "
                    + pointer);
        }
        return new JXPathContextReferenceImpl(this, contextBean, pointer);
    }

    @Override
    public Pointer getContextPointer() {
        return contextPointer;
    }

    /**
     * Gets absolute root pointer.
     * @return NodePointer
     */
    private NodePointer getAbsoluteRootPointer() {
        return (NodePointer) rootPointer;
    }

    /**
     * Gets the evaluation context.
     * @return EvalContext
     */
    private EvalContext getEvalContext() {
        return new InitialContext(new RootContext(this,
                (NodePointer) getContextPointer()));
    }

    /**
     * Gets the absolute root context.
     * @return EvalContext
     */
    public EvalContext getAbsoluteRootContext() {
        return new InitialContext(new RootContext(this,
                getAbsoluteRootPointer()));
    }

    /**
     * Gets a VariablePointer for the given variable name.
     * @param name variable name
     * @return NodePointer
     */
    public NodePointer getVariablePointer(final QName name) {
        return NodePointer.newNodePointer(name, VariablePointerFactory
                .contextWrapper(this), getLocale());
    }

    /**
     * Gets the named Function.
     * @param functionName name
     * @param parameters function args
     * @return Function
     */
    public Function getFunction(final QName functionName, final Object[] parameters) {
        final String namespace = functionName.getPrefix();
        final String name = functionName.getName();
        JXPathContext funcCtx = this;
        Function func;
        Functions funcs;
        while (funcCtx != null) {
            funcs = funcCtx.getFunctions();
            if (funcs != null) {
                func = funcs.getFunction(namespace, name, parameters);
                if (func != null) {
                    return func;
                }
            }
            funcCtx = funcCtx.getParentContext();
        }
        throw new JXPathFunctionNotFoundException(
            "Undefined function: " + functionName.toString());
    }

    @Override
    public void registerNamespace(final String prefix, final String namespaceURI) {
        if (namespaceResolver.isSealed()) {
            namespaceResolver = (NamespaceResolver) namespaceResolver.clone();
        }
        namespaceResolver.registerNamespace(prefix, namespaceURI);
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        return namespaceResolver.getNamespaceURI(prefix);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.jxpath.JXPathContext#getPrefix(java.lang.String)
     */
    @Override
    public String getPrefix(final String namespaceURI) {
        return namespaceResolver.getPrefix(namespaceURI);
    }

    @Override
    public void setNamespaceContextPointer(final Pointer pointer) {
        if (namespaceResolver.isSealed()) {
            namespaceResolver = (NamespaceResolver) namespaceResolver.clone();
        }
        namespaceResolver.setNamespaceContextPointer((NodePointer) pointer);
    }

    @Override
    public Pointer getNamespaceContextPointer() {
        return namespaceResolver.getNamespaceContextPointer();
    }

    /**
     * Gets the namespace resolver.
     * @return NamespaceResolver
     */
    public NamespaceResolver getNamespaceResolver() {
        namespaceResolver.seal();
        return namespaceResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExceptionHandler(final ExceptionHandler exceptionHandler) {
        if (rootPointer instanceof NodePointer) {
            ((NodePointer) rootPointer).setExceptionHandler(exceptionHandler);
        }
    }

    /**
     * Checks if existenceCheckClass exists on the class path. If so, allocates
     * an instance of the specified class, otherwise returns null.
     * @param className to instantiate
     * @param existenceCheckClassName guard class
     * @return className instance
     */
    public static Object allocateConditionally(final String className,
            final String existenceCheckClassName) {
        try {
            try {
                ClassLoaderUtil.getClass(existenceCheckClassName, true);
            }
            catch (final ClassNotFoundException ex) {
                return null;
            }
            final Class cls = ClassLoaderUtil.getClass(className, true);
            return cls.getConstructor().newInstance();
        }
        catch (final Exception ex) {
            throw new JXPathException("Cannot allocate " + className, ex);
        }
    }
}
