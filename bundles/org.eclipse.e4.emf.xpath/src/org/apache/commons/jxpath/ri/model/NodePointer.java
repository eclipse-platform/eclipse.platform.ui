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
package org.apache.commons.jxpath.ri.model;

import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.ExceptionHandler;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;

/**
 * Common superclass for Pointers of all kinds.  A NodePointer maps to
 * a deterministic XPath that represents the location of a node in an
 * object graph. This XPath uses only simple axes: child, namespace and
 * attribute and only simple, context-independent predicates.
 */
public abstract class NodePointer implements Pointer {

    /** Serialization version */
    private static final long serialVersionUID = 8117201322861007777L;

    /** Whole collection index. */
    public static final int WHOLE_COLLECTION = Integer.MIN_VALUE;

    /** Constant to indicate unknown namespace */
    public static final String UNKNOWN_NAMESPACE = "<<unknown namespace>>";

    /** Index for this NodePointer */
    protected int index = WHOLE_COLLECTION;

    private boolean attribute = false;
    private NamespaceResolver namespaceResolver;
    private ExceptionHandler exceptionHandler;
    private transient Object rootNode;

    /**
     * Allocates an entirely new NodePointer by iterating through all installed
     * NodePointerFactories until it finds one that can create a pointer.
     * @param name QName
     * @param bean Object
     * @param locale Locale
     * @return NodePointer
     */
    public static NodePointer newNodePointer(
        final QName name,
        final Object bean,
        final Locale locale) {
        NodePointer pointer;
        if (bean == null) {
            pointer = new NullPointer(name, locale);
            return pointer;
        }

        final NodePointerFactory[] factories =
            JXPathContextReferenceImpl.getNodePointerFactories();
        for (final NodePointerFactory element : factories) {
            pointer = element.createNodePointer(name, bean, locale);
            if (pointer != null) {
                return pointer;
            }
        }
        throw new JXPathException(
            "Could not allocate a NodePointer for object of "
                + bean.getClass());
    }

    /**
     * Allocates an new child NodePointer by iterating through all installed
     * NodePointerFactories until it finds one that can create a pointer.
     * @param parent pointer
     * @param name QName
     * @param bean Object
     * @return NodePointer
     */
    public static NodePointer newChildNodePointer(
        final NodePointer parent,
        final QName name,
        final Object bean) {
        final NodePointerFactory[] factories =
            JXPathContextReferenceImpl.getNodePointerFactories();
        for (final NodePointerFactory element : factories) {
            final NodePointer pointer =
                element.createNodePointer(parent, name, bean);
            if (pointer != null) {
                return pointer;
            }
        }
        throw new JXPathException(
            "Could not allocate a NodePointer for object of "
                + bean.getClass());
    }

    /** Parent pointer */
    protected NodePointer parent;

    /** Locale */
    protected Locale locale;

    /**
     * Create a new NodePointer.
     * @param parent Pointer
     */
    protected NodePointer(final NodePointer parent) {
        this.parent = parent;
    }

    /**
     * Create a new NodePointer.
     * @param parent Pointer
     * @param locale Locale
     */
    protected NodePointer(final NodePointer parent, final Locale locale) {
        this.parent = parent;
        this.locale = locale;
    }

    /**
     * Gets the NamespaceResolver associated with this NodePointer.
     * @return NamespaceResolver
     */
    public NamespaceResolver getNamespaceResolver() {
        if (namespaceResolver == null && parent != null) {
            namespaceResolver = parent.getNamespaceResolver();
        }
        return namespaceResolver;
    }

    /**
     * Sets the NamespaceResolver for this NodePointer.
     * @param namespaceResolver NamespaceResolver
     */
    public void setNamespaceResolver(final NamespaceResolver namespaceResolver) {
        this.namespaceResolver = namespaceResolver;
    }

    /**
     * Gets the parent pointer.
     * @return NodePointer
     */
    public NodePointer getParent() {
        NodePointer pointer = parent;
        while (pointer != null && pointer.isContainer()) {
            pointer = pointer.getImmediateParentPointer();
        }
        return pointer;
    }

    /**
     * Gets the immediate parent pointer.
     * @return NodePointer
     */
    public NodePointer getImmediateParentPointer() {
        return parent;
    }

    /**
     * Sets to true if the pointer represents the "attribute::" axis.
     * @param attribute boolean
     */
    public void setAttribute(final boolean attribute) {
        this.attribute = attribute;
    }

    /**
     * Returns true if the pointer represents the "attribute::" axis.
     * @return boolean
     */
    public boolean isAttribute() {
        return attribute;
    }

    /**
     * Returns true if this Pointer has no parent.
     * @return boolean
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * If true, this node does not have children
     * @return boolean
     */
    public abstract boolean isLeaf();

    /**
     * Learn whether this pointer is considered to be a node.
     * @return boolean
     * @deprecated Please use !isContainer()
     */
    @Deprecated
    public boolean isNode() {
        return !isContainer();
    }

    /**
     * If true, this node is auxiliary and can only be used as an intermediate in
     * the chain of pointers.
     * @return boolean
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * If the pointer represents a collection, the index identifies
     * an element of that collection.  The default value of {@code index}
     * is {@code WHOLE_COLLECTION}, which just means that the pointer
     * is not indexed at all.
     * Note: the index on NodePointer starts with 0, not 1.
     * @return int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of this NodePointer.
     * @param index int
     */
    public void setIndex(final int index) {
        this.index = index;
    }

    /**
     * Returns {@code true} if the value of the pointer is an array or
     * a Collection.
     * @return boolean
     */
    public abstract boolean isCollection();

    /**
     * If the pointer represents a collection (or collection element),
     * returns the length of the collection.
     * Otherwise returns 1 (even if the value is null).
     * @return int
     */
    public abstract int getLength();

    /**
     * By default, returns {@code getNode()}, can be overridden to
     * return a "canonical" value, like for instance a DOM element should
     * return its string value.
     * @return Object value
     */
    @Override
    public Object getValue() {
        final NodePointer valuePointer = getValuePointer();
        if (valuePointer != this) {
            return valuePointer.getValue();
        }
        // Default behavior is to return the same as getNode()
        return getNode();
    }

    /**
     * If this pointer manages a transparent container, like a variable,
     * this method returns the pointer to the contents.
     * Only an auxiliary (non-node) pointer can (and should) return a
     * value pointer other than itself.
     * Note that you probably don't want to override
     * {@code getValuePointer()} directly.  Override the
     * {@code getImmediateValuePointer()} method instead.  The
     * {@code getValuePointer()} method is calls
     * {@code getImmediateValuePointer()} and, if the result is not
     * {@code this}, invokes {@code getValuePointer()} recursively.
     * The idea here is to open all nested containers. Let's say we have a
     * container within a container within a container. The
     * {@code getValuePointer()} method should then open all those
     * containers and return the pointer to the ultimate contents. It does so
     * with the above recursion.
     * @return NodePointer
     */
    public NodePointer getValuePointer() {
        final NodePointer ivp = getImmediateValuePointer();
        return ivp == this ? this : ivp.getValuePointer();
    }

    /**
     * @see #getValuePointer()
     * @return NodePointer is either {@code this} or a pointer
     *   for the immediately contained value.
     */
    public NodePointer getImmediateValuePointer() {
        return this;
    }

    /**
     * An actual pointer points to an existing part of an object graph, even
     * if it is null. A non-actual pointer represents a part that does not exist
     * at all.
     * For instance consider the pointer "/address/street".
     * If both <em>address</em> and <em>street</em> are not null,
     * the pointer is actual.
     * If <em>address</em> is not null, but <em>street</em> is null,
     * the pointer is still actual.
     * If <em>address</em> is null, the pointer is not actual.
     * (In JavaBeans) if <em>address</em> is not a property of the root bean,
     * a Pointer for this path cannot be obtained at all - actual or otherwise.
     * @return boolean
     */
    public boolean isActual() {
        return index == WHOLE_COLLECTION || index >= 0 && index < getLength();
    }

    /**
     * Returns the name of this node. Can be null.
     * @return QName
     */
    public abstract QName getName();

    /**
     * Returns the value represented by the pointer before indexing.
     * So, if the node represents an element of a collection, this
     * method returns the collection itself.
     * @return Object value
     */
    public abstract Object getBaseValue();

    /**
     * Returns the object the pointer points to; does not convert it
     * to a "canonical" type.
     * @return Object node value
     * @deprecated 1.1 Please use getNode()
     */
    @Deprecated
    public Object getNodeValue() {
        return getNode();
    }

    /**
     * Returns the object the pointer points to; does not convert it
     * to a "canonical" type. Opens containers, properties etc and returns
     * the ultimate contents.
     * @return Object node
     */
    @Override
    public Object getNode() {
        return getValuePointer().getImmediateNode();
    }

    /**
     * Gets the root node.
     * @return Object value of this pointer's root (top parent).
     */
    @Override
    public synchronized Object getRootNode() {
        if (rootNode == null) {
            rootNode = parent == null ? getImmediateNode() : parent.getRootNode();
        }
        return rootNode;
    }

    /**
     * Returns the object the pointer points to; does not convert it
     * to a "canonical" type.
     * @return Object node
     */
    public abstract Object getImmediateNode();

    /**
     * Converts the value to the required type and changes the corresponding
     * object to that value.
     * @param value the value to set
     */
    @Override
    public abstract void setValue(Object value);

    /**
     * Compares two child NodePointers and returns a positive number,
     * zero or a positive number according to the order of the pointers.
     * @param pointer1 first pointer to be compared
     * @param pointer2 second pointer to be compared
     * @return int per Java comparison conventions
     */
    public abstract int compareChildNodePointers(
            NodePointer pointer1, NodePointer pointer2);

    /**
     * Checks if this Pointer matches the supplied NodeTest.
     * @param test the NodeTest to execute
     * @return true if a match
     */
    public boolean testNode(final NodeTest test) {
        if (test == null) {
            return true;
        }
        if (test instanceof NodeNameTest) {
            if (isContainer()) {
                return false;
            }
            final NodeNameTest nodeNameTest = (NodeNameTest) test;
            final QName testName = nodeNameTest.getNodeName();
            final QName nodeName = getName();
            if (nodeName == null) {
                return false;
            }

            final String testPrefix = testName.getPrefix();
            final String nodePrefix = nodeName.getPrefix();
            if (!safeEquals(testPrefix, nodePrefix)) {
                final String testNS = getNamespaceURI(testPrefix);
                final String nodeNS = getNamespaceURI(nodePrefix);
                if (!safeEquals(testNS, nodeNS)) {
                    return false;
                }
            }
            if (nodeNameTest.isWildcard()) {
                return true;
            }
            return testName.getName().equals(nodeName.getName());
        }
        return test instanceof NodeTypeTest
                && ((NodeTypeTest) test).getNodeType() == Compiler.NODE_TYPE_NODE && isNode();
    }

    /**
     *  Called directly by JXPathContext. Must create path and
     *  set value.
     *  @param context the owning JXPathContext
     *  @param value the new value to set
     *  @return created NodePointer
     */
    public NodePointer createPath(final JXPathContext context, final Object value) {
        setValue(value);
        return this;
    }

    /**
     * Remove the node of the object graph this pointer points to.
     */
    public void remove() {
        // It is a no-op

//        System.err.println("REMOVING: " + asPath() + " " + getClass());
//        printPointerChain();
    }

    /**
     * Called by a child pointer when it needs to create a parent object.
     * Must create an object described by this pointer and return
     * a new pointer that properly describes the new object.
     * @param context the owning JXPathContext
     * @return created NodePointer
     */
    public NodePointer createPath(final JXPathContext context) {
        return this;
    }

    /**
     * Called by a child pointer if that child needs to assign the value
     * supplied in the createPath(context, value) call to a non-existent
     * node. This method may have to expand the collection in order to assign
     * the element.
     * @param context the owning JXPathCOntext
     * @param name the QName at which a child should be created
     * @param index child index.
     * @param value node value to set
     * @return created NodePointer
     */
    public NodePointer createChild(
        final JXPathContext context,
        final QName name,
        final int index,
        final Object value) {
        throw new JXPathException("Cannot create an object for path "
                + asPath() + "/" + name + "[" + (index + 1) + "]"
                + ", operation is not allowed for this type of node");
    }

    /**
     * Called by a child pointer when it needs to create a parent object for a
     * non-existent collection element. It may have to expand the collection,
     * then create an element object and return a new pointer describing the
     * newly created element.
     * @param context the owning JXPathCOntext
     * @param name the QName at which a child should be created
     * @param index child index.
     * @return created NodePointer
     */
    public NodePointer createChild(final JXPathContext context, final QName name, final int index) {
        throw new JXPathException("Cannot create an object for path "
                + asPath() + "/" + name + "[" + (index + 1) + "]"
                + ", operation is not allowed for this type of node");
    }

    /**
     * Called to create a non-existing attribute
     * @param context the owning JXPathCOntext
     * @param name the QName at which an attribute should be created
     * @return created NodePointer
     */
    public NodePointer createAttribute(final JXPathContext context, final QName name) {
        throw new JXPathException("Cannot create an attribute for path "
                + asPath() + "/@" + name
                + ", operation is not allowed for this type of node");
    }

    /**
     * If the Pointer has a parent, returns the parent's locale; otherwise
     * returns the locale specified when this Pointer was created.
     * @return Locale for this NodePointer
     */
    public Locale getLocale() {
        if (locale == null && parent != null) {
            locale = parent.getLocale();
        }
        return locale;
    }

    /**
     * Check whether our locale matches the specified language.
     * @param lang String language to check
     * @return true if the selected locale name starts
     *              with the specified prefix <em>lang</em>, case-insensitive.
     */
    public boolean isLanguage(final String lang) {
        final Locale loc = getLocale();
        final String name = loc.toString().replace('_', '-');
        return name.toUpperCase(Locale.ENGLISH).startsWith(lang.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Returns a NodeIterator that iterates over all children or all children
     * that match the given NodeTest, starting with the specified one.
     * @param test NodeTest to filter children
     * @param reverse specified iteration direction
     * @param startWith the NodePointer to start with
     * @return NodeIterator
     */
    public NodeIterator childIterator(
        final NodeTest test,
        final boolean reverse,
        final NodePointer startWith) {
        final NodePointer valuePointer = getValuePointer();
        return valuePointer == null || valuePointer == this ? null
                : valuePointer.childIterator(test, reverse, startWith);
    }

    /**
     * Returns a NodeIterator that iterates over all attributes of the current
     * node matching the supplied node name (could have a wildcard).
     * May return null if the object does not support the attributes.
     * @param qname the attribute name to test
     * @return NodeIterator
     */
    public NodeIterator attributeIterator(final QName qname) {
        final NodePointer valuePointer = getValuePointer();
        return valuePointer == null || valuePointer == this ? null
                : valuePointer.attributeIterator(qname);
    }

    /**
     * Returns a NodeIterator that iterates over all namespaces of the value
     * currently pointed at.
     * May return null if the object does not support the namespaces.
     * @return NodeIterator
     */
    public NodeIterator namespaceIterator() {
        return null;
    }

    /**
     * Returns a NodePointer for the specified namespace. Will return null
     * if namespaces are not supported.
     * Will return UNKNOWN_NAMESPACE if there is no such namespace.
     * @param namespace incoming namespace
     * @return NodePointer for {@code namespace}
     */
    public NodePointer namespacePointer(final String namespace) {
        return null;
    }

    /**
     * Decodes a namespace prefix to the corresponding URI.
     * @param prefix prefix to decode
     * @return String uri
     */
    public String getNamespaceURI(final String prefix) {
        return null;
    }

    /**
     * Returns the namespace URI associated with this Pointer.
     * @return String uri
     */
    public String getNamespaceURI() {
        return null;
    }

    /**
     * Returns true if the supplied prefix represents the
     * default namespace in the context of the current node.
     * @param prefix the prefix to check
     * @return {@code true} if prefix is default
     */
    protected boolean isDefaultNamespace(final String prefix) {
        if (prefix == null) {
            return true;
        }

        final String namespace = getNamespaceURI(prefix);
        return namespace != null && namespace.equals(getDefaultNamespaceURI());
    }

    /**
     * Gets the default ns uri
     * @return String uri
     */
    protected String getDefaultNamespaceURI() {
        return null;
    }

    /**
     * Locates a node by ID.
     * @param context JXPathContext owning context
     * @param id String id
     * @return Pointer found
     */
    public Pointer getPointerByID(final JXPathContext context, final String id) {
        return context.getPointerByID(id);
    }

    /**
     * Returns an XPath that maps to this Pointer.
     * @return String xpath expression
     */
    @Override
    public String asPath() {
        // If the parent of this node is a container, it is responsible
        // for appended this node's part of the path.
        if (parent != null && parent.isContainer()) {
            return parent.asPath();
        }

        final StringBuilder buffer = new StringBuilder();
        if (parent != null) {
            buffer.append(parent.asPath());
        }

        if (buffer.length() == 0
            || buffer.charAt(buffer.length() - 1) != '/') {
            buffer.append('/');
        }
        if (attribute) {
            buffer.append('@');
        }
        buffer.append(getName());

        if (index != WHOLE_COLLECTION && isCollection()) {
            buffer.append('[').append(index + 1).append(']');
        }
        return buffer.toString();
    }

    /**
     * Clone this NodePointer.
     * @return cloned NodePointer
     */
    @Override
    public Object clone() {
        try {
            final NodePointer ptr = (NodePointer) super.clone();
            if (parent != null) {
                ptr.parent = (NodePointer) parent.clone();
            }
            return ptr;
        }
        catch (final CloneNotSupportedException ex) {
            // Of course it is supported
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return asPath();
    }

    @Override
    public int compareTo(final Object object) {
        if (object == this) {
            return 0;
        }
        // Let it throw a ClassCastException
        final NodePointer pointer = (NodePointer) object;
        if (safeEquals(parent, pointer.parent)) {
            return parent == null ? 0 : parent.compareChildNodePointers(this, pointer);
        }

        // Task 1: find the common parent
        int depth1 = 0;
        NodePointer p1 = this;
        final HashSet parents1 = new HashSet();
        while (p1 != null) {
            depth1++;
            p1 = p1.parent;
            if (p1 != null) {
                parents1.add(p1);
            }
        }
        boolean commonParentFound = false;
        int depth2 = 0;
        NodePointer p2 = pointer;
        while (p2 != null) {
            depth2++;
            p2 = p2.parent;
            if (parents1.contains(p2)) {
                commonParentFound = true;
            }
        }
        //nodes from different graphs are equal, else continue comparison:
        return commonParentFound ? compareNodePointers(this, depth1, pointer, depth2) : 0;
    }

    /**
     * Compare node pointers.
     * @param p1 pointer 1
     * @param depth1 depth 1
     * @param p2 pointer 2
     * @param depth2 depth 2
     * @return comparison result: (< 0) -> (p1 lt p2); (0) -> (p1 eq p2); (> 0) -> (p1 gt p2)
     */
    private int compareNodePointers(
        final NodePointer p1,
        final int depth1,
        final NodePointer p2,
        final int depth2) {
        if (depth1 < depth2) {
            final int r = compareNodePointers(p1, depth1, p2.parent, depth2 - 1);
            return r == 0 ? -1 : r;
        }
        if (depth1 > depth2) {
            final int r = compareNodePointers(p1.parent, depth1 - 1, p2, depth2);
            return r == 0 ? 1 : r;
        }
        //henceforth depth1 == depth2:
        if (safeEquals(p1, p2)) {
            return 0;
        }
        if (depth1 == 1) {
            throw new JXPathException(
                    "Cannot compare pointers that do not belong to the same tree: '"
                    + p1 + "' and '" + p2 + "'");
        }
        final int r = compareNodePointers(p1.parent, depth1 - 1, p2.parent, depth2 - 1);
        return r == 0 ? p1.parent.compareChildNodePointers(p1, p2) : r;
    }

    /**
     * Sets the exceptionHandler of this NodePointer.
     * @param exceptionHandler the ExceptionHandler to set
     */
    public void setExceptionHandler(final ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Handle a Throwable using an installed ExceptionHandler, if available.
     * Public to facilitate calling for RI support; not truly intended for public consumption.
     * @param t to handle
     * @param originator context
     */
    public void handle(final Throwable t, final NodePointer originator) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(t, originator);
            return;
        }
        if (parent != null) {
            parent.handle(t, originator);
        }
    }

    /**
     * Handle a Throwable using an installed ExceptionHandler, if available.
     * Public to facilitate calling for RI support; not truly intended for public consumption.
     * @param t to handle
     */
    public void handle(final Throwable t) {
        handle(t, this);
    }

    /**
     * Return a string escaping single and double quotes.
     * @param string string to treat
     * @return string with any necessary changes made.
     */
    protected String escape(final String string) {
        final char[] c = { '\'', '"' };
        final String[] esc = { "&apos;", "&quot;" };
        StringBuilder sb = null;
        for (int i = 0; sb == null && i < c.length; i++) {
            if (string.indexOf(c[i]) >= 0) {
                sb = new StringBuilder(string);
            }
        }
        if (sb == null) {
            return string;
        }
        for (int i = 0; i < c.length; i++) {
            if (string.indexOf(c[i]) < 0) {
                continue;
            }
            int pos = 0;
            while (pos < sb.length()) {
                if (sb.charAt(pos) == c[i]) {
                    sb.replace(pos, pos + 1, esc[i]);
                    pos += esc[i].length();
                }
                else {
                    pos++;
                }
            }
        }
        return sb.toString();
    }

    /**
     * Gets the AbstractFactory associated with the specified JXPathContext.
     * @param context JXPathContext
     * @return AbstractFactory
     */
    protected AbstractFactory getAbstractFactory(final JXPathContext context) {
        final AbstractFactory factory = context.getFactory();
        if (factory == null) {
            throw new JXPathException(
                "Factory is not set on the JXPathContext - cannot create path: "
                    + asPath());
        }
        return factory;
    }

    /**
     * Print deep
     * @param pointer to print
     * @param indent indentation level
     */
    private static void printDeep(final NodePointer pointer, final String indent) {
        if (indent.length() == 0) {
            System.err.println(
                "POINTER: "
                    + pointer
                    + "("
                    + pointer.getClass().getName()
                    + ")");
        }
        else {
            System.err.println(
                indent
                    + " of "
                    + pointer
                    + "("
                    + pointer.getClass().getName()
                    + ")");
        }
        if (pointer.getImmediateParentPointer() != null) {
            printDeep(pointer.getImmediateParentPointer(), indent + "  ");
        }
    }

    private static boolean safeEquals(final Object o1, final Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    /**
     * Verify the structure of a given NodePointer.
     * @param nodePointer to check
     * @return nodePointer
     * @throws JXPathNotFoundException Thrown when there is no value at the NodePointer.
     */
    public static NodePointer verify(final NodePointer nodePointer) {
        if (!nodePointer.isActual()) {
            // We need to differentiate between pointers representing
            // a non-existing property and ones representing a property
            // whose value is null.  In the latter case, the pointer
            // is going to have isActual == false, but its parent,
            // which is a non-node pointer identifying the bean property,
            // will return isActual() == true.
            final NodePointer parent = nodePointer.getImmediateParentPointer();
            if (parent == null
                || !parent.isContainer()
                || !parent.isActual()) {
                throw new JXPathNotFoundException("No value for xpath: " + nodePointer);
            }
        }
        return nodePointer;
    }
}
