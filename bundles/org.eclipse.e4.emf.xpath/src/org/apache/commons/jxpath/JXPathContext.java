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
package org.apache.commons.jxpath;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.util.KeyManagerUtils;

/**
 * JXPathContext  provides APIs for the traversal of graphs of JavaBeans using
 * the XPath syntax. Using JXPathContext, you can read and write properties of
 * JavaBeans, arrays, collections and maps. JXPathContext uses JavaBeans
 * introspection to enumerate and access JavaBeans properties.
 * <p>
 * JXPathContext  allows alternative implementations. This is why instead of
 * allocating JXPathContext directly, you should call a static
 * {@code newContext} method.  This method will utilize the
 * {@link JXPathContextFactory} API to locate a suitable implementation of
 * JXPath. Bundled with JXPath comes a default implementation called Reference
 * Implementation.
 * </p>
 *
 * <h2>JXPath Interprets XPath Syntax on Java Object Graphs</h2>
 *
 * JXPath uses an intuitive interpretation of the xpath syntax in the context
 * of Java object graphs. Here are some examples:
 *
 * <h3>Example 1: JavaBean Property Access</h3>
 *
 * JXPath can be used to access properties of a JavaBean.
 *
 * <pre>
 * public class Employee {
 *    public String getFirstName(){
 *       ...
 *    }
 * }
 *
 * Employee emp = new Employee();
 * ...
 *
 * JXPathContext context = JXPathContext.newContext(emp);
 * String fName = (String)context.getValue("firstName");
 * </pre>
 *
 * In  this example, we are using JXPath to access a property of the
 * {@code emp} bean. In this simple case the invocation of JXPath is
 * equivalent to invocation of getFirstName() on the bean.
 *
 * <h3>Example 2: Nested Bean Property Access</h3>
 * JXPath can traverse object graphs:
 *
 * <pre>
 * public class Employee {
 *    public Address getHomeAddress(){
 *       ...
 *    }
 * }
 * public class Address {
 *    public String getStreetNumber(){
 *       ...
 *    }
 * }
 *
 * Employee emp = new Employee();
 * ...
 *
 * JXPathContext context = JXPathContext.newContext(emp);
 * String sNumber = (String)context.getValue("homeAddress/streetNumber");
 * </pre>
 *
 * In this case XPath is used to access a property of a nested bean.
 * <p>
 * A property identified by the xpath does not have to be a "leaf" property.
 * For instance, we can extract the whole Address object in above example:
 * </p>
 * <pre>
 *    Address addr = (Address)context.getValue("homeAddress");
 * </pre>
 *
 * <h3>Example 3: Collection Subscripts</h3>
 * JXPath can extract elements from arrays and collections.
 *
 * <pre>
 * public class Integers {
 *    public int[] getNumbers(){
 *       ...
 *    }
 * }
 *
 * Integers ints = new Integers();
 * ...
 *
 * JXPathContext context = JXPathContext.newContext(ints);
 * Integer thirdInt = (Integer)context.getValue("numbers[3]");
 * </pre>
 * A  collection can be an arbitrary array or an instance of java.util.
 * Collection.
 * <p>
 * Note: in XPath the first element of a collection has index 1, not 0.<br>
 *
 * <h3>Example 4: Map Element Access</h3>
 *
 * JXPath supports maps. To get a value use its key.
 *
 * <pre>
 * public class Employee {
 *    public Map getAddresses(){
 *       return addressMap;
 *    }
 *
 *    public void addAddress(String key, Address address){
 *       addressMap.put(key, address);
 *    }
 *    ...
 * }
 *
 * Employee emp = new Employee();
 * emp.addAddress("home", new Address(...));
 * emp.addAddress("office", new Address(...));
 * ...
 *
 * JXPathContext context = JXPathContext.newContext(emp);
 * String homeZipCode = (String)context.getValue("addresses/home/zipCode");
 * </pre>
 *
 * Often you will need to use the alternative syntax for accessing Map
 * elements:
 *
 * <pre>
 * String homeZipCode =
 *     (String) context.getValue("addresses[@name='home']/zipCode");
 * </pre>
 *
 * In this case, the key can be an expression, e.g. a variable.<br>
 *
 * Note: At this point JXPath only supports Maps that use strings for keys.<br>
 * Note: JXPath supports the extended notion of Map: any object with
 *       dynamic properties can be handled by JXPath provided that its
 *       class is registered with the {@link JXPathIntrospector}.
 *
 * <h3>Example 5: Retrieving Multiple Results</h3>
 *
 * JXPath can retrieve multiple objects from a graph. Note that the method
 * called in this case is not {@code getValue}, but {@code iterate}.
 *
 * <pre>{@code
 * public class Author {
 *    public Book[] getBooks(){
 *       ...
 *    }
 * }
 *
 * Author auth = new Author();
 * ...
 *
 * JXPathContext context = JXPathContext.newContext(auth);
 * Iterator threeBooks = context.iterate("books[position() < 4]");
 * }</pre>
 *
 * This returns a list of at most three books from the array of all books
 * written by the author.
 *
 * <h3>Example 6: Setting Properties</h3>
 * JXPath can be used to modify property values.
 *
 * <pre>
 * public class Employee {
 *    public Address getAddress() {
 *       ...
 *    }
 *
 *    public void setAddress(Address address) {
 *       ...
 *    }
 * }
 *
 * Employee emp = new Employee();
 * Address addr = new Address();
 * ...
 *
 * JXPathContext context = JXPathContext.newContext(emp);
 * context.setValue("address", addr);
 * context.setValue("address/zipCode", "90190");
 *
 * </pre>
 *
 * <h3>Example 7: Creating objects</h3>
 * JXPath  can be used to create new objects. First, create a subclass of {@link
 * AbstractFactory AbstractFactory} and install it on the JXPathContext. Then
 * call {@link JXPathContext#createPath createPathAndSetValue()} instead of
 * "setValue". JXPathContext will invoke your AbstractFactory when it discovers
 * that an intermediate node of the path is <strong>null</strong>.  It will not override
 * existing nodes.
 *
 * <pre>
 * public class AddressFactory extends AbstractFactory {
 *    public boolean createObject(JXPathContext context,
 *               Pointer pointer, Object parent, String name, int index){
 *     if ((parent instanceof Employee) &amp;&amp; name.equals("address"){
 *       ((Employee)parent).setAddress(new Address());
 *       return true;
 *     }
 *     return false;
 *   }
 * }
 *
 * JXPathContext context = JXPathContext.newContext(emp);
 * context.setFactory(new AddressFactory());
 * context.createPathAndSetValue("address/zipCode", "90190");
 * </pre>
 *
 * <h3>Example 8: Using Variables</h3>
 * JXPath supports the notion of variables. The XPath syntax for accessing
 * variables is <em>"$varName"</em>.
 *
 * <pre>
 * public class Author {
 *    public Book[] getBooks(){
 *       ...
 *    }
 * }
 *
 * Author auth = new Author();
 * ...
 *
 * JXPathContext context = JXPathContext.newContext(auth);
 * context.getVariables().declareVariable("index", new Integer(2));
 *
 * Book secondBook = (Book)context.getValue("books[$index]");
 * </pre>
 *
 * You can also set variables using JXPath:
 *
 * <pre>
 * context.setValue("$index", new Integer(3));
 * </pre>
 *
 * Note: you can only <em>change</em> the value of an existing variable this
 * way, you cannot <em>define</em> a new variable.
 *
 * <p>
 * When a variable contains a JavaBean or a collection, you can
 * traverse the bean or collection as well:
 * <pre>
 * ...
 * context.getVariables().declareVariable("book", myBook);
 * String title = (String)context.getValue("$book/title);
 *
 * Book array[] = new Book[]{...};
 *
 * context.getVariables().declareVariable("books", array);
 *
 * String title = (String)context.getValue("$books[2]/title);
 * </pre>
 *
 * <h3>Example 9: Using Nested Contexts</h3>
 * If  you need to use the same set of variable while interpreting XPaths with
 * different beans, it makes sense to put the variables in a separate context
 * and specify that context as a parent context every time you allocate a new
 * JXPathContext for a JavaBean.
 *
 * <pre>
 * JXPathContext varContext = JXPathContext.newContext(null);
 * varContext.getVariables().declareVariable("title", "Java");
 *
 * JXPathContext context = JXPathContext.newContext(varContext, auth);
 *
 * Iterator javaBooks = context.iterate("books[title = $title]");
 * </pre>
 *
 * <h3>Using Custom Variable Pools</h3>
 * By default, JXPathContext creates a HashMap of variables. However,
 * you can substitute a custom implementation of the Variables
 * interface to make JXPath work with an alternative source of variables.
 * For example, you can define implementations of Variables that
 * cover a servlet context, HTTP request or any similar structure.
 *
 * <h3>Example 10: Using Standard Extension Functions</h3>
 * Using the standard extension functions, you can call methods on objects,
 * static methods on classes and create objects using any constructor.
 * The class names should be fully qualified.
 * <p>
 * Here's how you can create new objects:
 * <pre>
 * Book book =
 *    (Book) context.getValue(
 *         "org.apache.commons.jxpath.example.Book.new ('John Updike')");
 * </pre>
 *
 * Here's how you can call static methods:
 * <pre>
 *   Book book =
 *    (Book) context.getValue(
 *       "org. apache.commons.jxpath.example.Book.getBestBook('John Updike')");
 * </pre>
 *
 * Here's how you can call regular methods:
 * <pre>
 * String firstName = (String)context.getValue("getAuthorsFirstName($book)");
 * </pre>
 * As you can see, the target of the method is specified as the first parameter
 * of the function.
 *
 * <h3>Example 11: Using Custom Extension Functions</h3>
 * Collections of custom extension functions can be implemented
 * as {@link Functions Functions} objects or as Java classes, whose methods
 * become extenstion functions.
 * <p>
 * Let's say the following class implements various formatting operations:
 * <pre>
 * public class Formats {
 *    public static String date(Date d, String pattern){
 *        return new SimpleDateFormat(pattern).format(d);
 *    }
 *    ...
 * }
 * </pre>
 *
 * We can register this class with a JXPathContext:
 *
 * <pre>
 * context.setFunctions(new ClassFunctions(Formats.class, "format"));
 * ...
 *
 * context.getVariables().declareVariable("today", new Date());
 * String today = (String)context.getValue("format:date($today, 'MM/dd/yyyy')");
 *
 * </pre>
 * You can also register whole packages of Java classes using PackageFunctions.
 * <p>
 * Also, see {@link FunctionLibrary FunctionLibrary}, which is a class
 * that allows you to register multiple sets of extension functions with
 * the same JXPathContext.
 *
 * <h2>Configuring JXPath</h2>
 *
 * JXPath uses JavaBeans introspection to discover properties of JavaBeans.
 * You can provide alternative property lists by supplying
 * custom JXPathBeanInfo classes (see {@link JXPathBeanInfo JXPathBeanInfo}).
 *
 * <h2>Notes</h2>
 * <ul>
 * <li> JXPath does not support DOM attributes for non-DOM objects. Even though
 * XPaths like "para[@type='warning']" are legitimate, they will always produce
 * empty results. The only attribute supported for JavaBeans is "name".  The
 * XPath "foo/bar" is equivalent to "foo[@name='bar']".
 *
 * <li id='matches_no_property_in_the_graph'>The term <b>matches no property in
 * the graph</b> is used throughout the documentation. It describes a property or
 * path that can be determined as not belonging to the graph. Determining
 * whether a property or path belongs to the graph depends on the type of object
 * being used as {@code cotextBean} (see {@link #newContext(Object)}).
 * It is only possible strongly typed models where a specific Java model is used
 * as context. It is not possible with dynamic models such Maps or DOM
 * implementations.
 * <p>When a XPath does not match a property in the graph, the methods of this
 * class that retrieve a pointer will generally behave in the following way,
 * depending on the last value configured with {@link #setLenient(boolean)}:</p>
 *
 *  <ol style='list-style:upper-alpha'>
 *      <li>If {@code lenient} is {@code false} (default) - methods
 *          will throw {@link JXPathNotFoundException}.
 *      <li>If {@code lenient} is {@code true} - methods will throw
 *          no exception and return a value appropriate for that method to
 *          express the absence: might be a Java {@code null} or a
 *          {@link Pointer} whose {@link Pointer#getValue()} returns
 *          {@code null}, depends on the method.
 *  </ol>
 * </li>
 * </ul>
 *
 * See  <a href="http://www.w3schools.com/xpath">XPath Tutorial by
 * W3Schools</a><br>. Also see <a href="http://www.w3.org/TR/xpath">XML Path
 * Language (XPath) Version 1.0</a><br><br>
 *
 * You will also find more information and examples in
 * <a href="https://commons.apache.org/jxpath/users-guide.html">
 * JXPath User's Guide</a>
 */
public abstract class JXPathContext {
    private static volatile JXPathContextFactory contextFactory;
    private static volatile JXPathContext compilationContext;

    private static final PackageFunctions GENERIC_FUNCTIONS =
        new PackageFunctions("", null);

    /** Parent context */
    protected JXPathContext parentContext;
    /** Context bean */
    protected Object contextBean;
    /**  Variables */
    protected Variables vars;
    /** Functions */
    protected Functions functions;
    /** AbstractFactory */
    protected AbstractFactory factory;
    /** IdentityManager */
    protected IdentityManager idManager;
    /** KeyManager */
    protected KeyManager keyManager;
    /** Decimal format map */
    protected HashMap<String, DecimalFormatSymbols> decimalFormats;

    private Locale locale;
    private boolean lenientSet = false;
    private boolean lenient = false;

    /**
     * Creates a new JXPathContext with the specified object as the root node.
     * @param contextBean Object
     * @return JXPathContext
     */
    public static JXPathContext newContext(final Object contextBean) {
        return getContextFactory().newContext(null, contextBean);
    }

    /**
     * Creates a new JXPathContext with the specified bean as the root node and
     * the specified parent context. Variables defined in a parent context can
     * be referenced in XPaths passed to the child context.
     * @param parentContext parent context
     * @param contextBean Object
     * @return JXPathContext
     */
    public static JXPathContext newContext(
            final JXPathContext parentContext,
            final Object contextBean) {
        return getContextFactory().newContext(parentContext, contextBean);
    }

    /**
     * Acquires a context factory and caches it.
     * @return JXPathContextFactory
     */
    private static JXPathContextFactory getContextFactory () {
        if (contextFactory == null) {
            contextFactory = JXPathContextFactory.newInstance();
        }
        return contextFactory;
    }

    /**
     * This constructor should remain protected - it is to be overridden by
     * subclasses, but never explicitly invoked by clients.
     * @param parentContext parent context
     * @param contextBean Object
     */
    protected JXPathContext(final JXPathContext parentContext, final Object contextBean) {
        this.parentContext = parentContext;
        this.contextBean = contextBean;
    }

    /**
     * Returns the parent context of this context or null.
     * @return JXPathContext
     */
    public JXPathContext getParentContext() {
        return parentContext;
    }

    /**
     * Returns the JavaBean associated with this context.
     * @return Object
     */
    public Object getContextBean() {
        return contextBean;
    }

    /**
     * Returns a Pointer for the context bean.
     * @return Pointer
     */
    public abstract Pointer getContextPointer();

    /**
     * Returns a JXPathContext that is relative to the current JXPathContext.
     * The supplied pointer becomes the context pointer of the new context.
     * The relative context inherits variables, extension functions, locale etc
     * from the parent context.
     * @param pointer Pointer
     * @return JXPathContext
     */
    public abstract JXPathContext getRelativeContext(Pointer pointer);

    /**
     * Installs a custom implementation of the Variables interface.
     * @param vars Variables
     */
    public void setVariables(final Variables vars) {
        this.vars = vars;
    }

    /**
     * Returns the variable pool associated with the context. If no such
     * pool was specified with the {@link #setVariables} method,
     * returns the default implementation of Variables,
     * {@link BasicVariables BasicVariables}.
     * @return Variables
     */
    public Variables getVariables() {
        if (vars == null) {
            vars = new BasicVariables();
        }
        return vars;
    }

    /**
     * Install a library of extension functions.
     * @param functions Functions
     * @see FunctionLibrary
     */
    public void setFunctions(final Functions functions) {
        this.functions = functions;
    }

    /**
     * Returns the set of functions installed on the context.
     * @return Functions
     */
    public Functions getFunctions() {
        if (functions != null) {
            return functions;
        }
        if (parentContext == null) {
            return GENERIC_FUNCTIONS;
        }
        return null;
    }

    /**
     * Install an abstract factory that should be used by the
     * {@code createPath()} and {@code createPathAndSetValue()}
     * methods.
     * @param factory AbstractFactory
     */
    public void setFactory(final AbstractFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the AbstractFactory installed on this context.
     * If none has been installed and this context has a parent context,
     * returns the parent's factory.  Otherwise returns null.
     * @return AbstractFactory
     */
    public AbstractFactory getFactory() {
        if (factory == null && parentContext != null) {
            return parentContext.getFactory();
        }
        return factory;
    }

    /**
     * Sets the locale for this context.  The value of the "lang"
     * attribute as well as the lang() function will be
     * affected by the locale.  By default, JXPath uses
     * {@code Locale.getDefault()}
     * @param locale Locale
     */
    public synchronized void setLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns the locale set with setLocale. If none was set and
     * the context has a parent, returns the parent's locale.
     * Otherwise, returns Locale.getDefault().
     * @return Locale
     */
    public synchronized Locale getLocale() {
        if (locale == null) {
            if (parentContext != null) {
                return parentContext.getLocale();
            }
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * Sets {@link DecimalFormatSymbols} for a given name. The DecimalFormatSymbols
     * can be referenced as the third, optional argument in the invocation of
     * {@code format-number (number,format,decimal-format-name)} function.
     * By default, JXPath uses the symbols for the current locale.
     *
     * @param name the format name or null for default format.
     * @param symbols DecimalFormatSymbols
     */
    public synchronized void setDecimalFormatSymbols(final String name,
            final DecimalFormatSymbols symbols) {
        if (decimalFormats == null) {
            decimalFormats = new HashMap<>();
        }
        decimalFormats.put(name, symbols);
    }

    /**
     * Gets the named DecimalFormatSymbols.
     * @param name key
     * @return DecimalFormatSymbols
     * @see #setDecimalFormatSymbols(String, DecimalFormatSymbols)
     */
    public synchronized DecimalFormatSymbols getDecimalFormatSymbols(final String name) {
        if (decimalFormats == null) {
            return parentContext == null ? null : parentContext.getDecimalFormatSymbols(name);
        }
        return decimalFormats.get(name);
    }

    /**
     * If the context is in the lenient mode, then getValue() returns null
     * for inexistent paths.  Otherwise, a path that does not map to
     * an existing property will throw an exception.  Note that if the
     * property exists, but its value is null, the exception is <em>not</em>
     * thrown.
     * <p>
     * By default, lenient = false
     * @param lenient flag
     */
    public synchronized void setLenient(final boolean lenient) {
        this.lenient = lenient;
        lenientSet = true;
    }

    /**
     * Learn whether this JXPathContext is lenient.
     * @return boolean
     * @see #setLenient(boolean)
     */
    public synchronized boolean isLenient() {
        if (!lenientSet && parentContext != null) {
            return parentContext.isLenient();
        }
        return lenient;
    }

    /**
     * Compiles the supplied XPath and returns an internal representation
     * of the path that can then be evaluated.  Use CompiledExpressions
     * when you need to evaluate the same expression multiple times
     * and there is a convenient place to cache CompiledExpression
     * between invocations.
     * @param xpath to compile
     * @return CompiledExpression
     */
    public static CompiledExpression compile(final String xpath) {
        if (compilationContext == null) {
            compilationContext = newContext(null);
        }
        return compilationContext.compilePath(xpath);
    }

    /**
     * Overridden by each concrete implementation of JXPathContext
     * to perform compilation. Is called by {@code compile()}.
     * @param xpath to compile
     * @return CompiledExpression
     */
    protected abstract CompiledExpression compilePath(String xpath);

    /**
     * Finds the first object that matches the specified XPath. It is equivalent
     * to {@code getPointer(xpath).getNode()}. Note that this method
     * produces the same result as {@code getValue()} on object models
     * like JavaBeans, but a different result for DOM/JDOM etc., because it
     * returns the Node itself, rather than its textual contents.
     *
     * @param xpath the xpath to be evaluated
     * @return the found object
     */
    public Object selectSingleNode(final String xpath) {
        final Pointer pointer = getPointer(xpath);
        return pointer == null ? null : pointer.getNode();
    }

    /**
     * Finds all nodes that match the specified XPath.
     *
     * @param xpath the xpath to be evaluated
     * @return a list of found objects
     */
    public List selectNodes(final String xpath) {
        final ArrayList list = new ArrayList();
        final Iterator<Pointer> iterator = iteratePointers(xpath);
        while (iterator.hasNext()) {
            final Pointer pointer = iterator.next();
            list.add(pointer.getNode());
        }
        return list;
    }

    /**
     * Evaluates the xpath and returns the resulting object. Primitive
     * types are wrapped into objects.
     * @param xpath to evaluate
     * @return Object found
     */
    public abstract Object getValue(String xpath);

    /**
     * Evaluates the xpath, converts the result to the specified class and
     * returns the resulting object.
     * @param xpath to evaluate
     * @param requiredType required type
     * @return Object found
     */
    public abstract Object getValue(String xpath, Class requiredType);

    /**
     * Modifies the value of the property described by the supplied xpath.
     * Will throw an exception if one of the following conditions occurs:
     * <ul>
     * <li>The xpath does not in fact describe an existing property
     * <li>The property is not writable (no public, non-static set method)
     * </ul>
     * @param xpath indicating position
     * @param value to set
     */
    public abstract void setValue(String xpath, Object value);

    /**
     * Creates missing elements of the path by invoking an {@link AbstractFactory},
     * which should first be installed on the context by calling {@link #setFactory}.
     * <p>
     * Will throw an exception if the AbstractFactory fails to create
     * an instance for a path element.
     * @param xpath indicating destination to create
     * @return pointer to new location
     */
    public abstract Pointer createPath(String xpath);

    /**
     * The same as setValue, except it creates intermediate elements of
     * the path by invoking an {@link AbstractFactory}, which should first be
     * installed on the context by calling {@link #setFactory}.
     * <p>
     * Will throw an exception if one of the following conditions occurs:
     * <ul>
     * <li>Elements of the xpath aleady exist, but the path does not in
     *  fact describe an existing property
     * <li>The AbstractFactory fails to create an instance for an intermediate
     * element.
     * <li>The property is not writable (no public, non-static set method)
     * </ul>
     * @param xpath indicating position to create
     * @param value to set
     * @return pointer to new location
     */
    public abstract Pointer createPathAndSetValue(String xpath, Object value);

    /**
     * Removes the element of the object graph described by the xpath.
     * @param xpath indicating position to remove
     */
    public abstract void removePath(String xpath);

    /**
     * Removes all elements of the object graph described by the xpath.
     * @param xpath indicating positions to remove
     */
    public abstract void removeAll(String xpath);

    /**
     * Traverses the xpath and returns an Iterator of all results found
     * for the path. If the xpath matches no properties
     * in the graph, the Iterator will be empty, but not null.
     *
     * @param <E> the type of elements returned by the iterator.
     * @param xpath to iterate
     * @return Iterator
     */
    public abstract <E> Iterator<E> iterate(String xpath);

    /**
     * Traverses the xpath and returns a Pointer. A Pointer provides easy access
     * to a property.
     * <p>
     * If the xpath <a href='#matches_no_property_in_the_graph'>matches no
     * properties in the graph</a> the behavior depends on the value that has
     * been configured with {@link #setLenient(boolean)}:</p>
     * <ul>
     * <li> {@code false} (default) the method will throw a
     * {@link JXPathNotFoundException}.
     * <li> {@code true} the method returns a pointer whose
     * {@link Pointer#getValue()} method will always return null.
     * </ul>
     *
     * @param xpath desired
     * @return Pointer A {@link Pointer}, never {@code null}.
     * @throws JXPathNotFoundException see method description.
     */
    public abstract Pointer getPointer(String xpath);

    /**
     * Traverses the xpath and returns an Iterator of Pointers.
     * A Pointer provides easy access to a property.
     * If the xpath matches no properties
     * in the graph, the Iterator be empty, but not null.
     * @param xpath to iterate
     * @return Iterator
     */
    public abstract Iterator<Pointer> iteratePointers(String xpath);

    /**
     * Install an identity manager that will be used by the context
     * to look up a node by its ID.
     * @param idManager IdentityManager to set
     */
    public void setIdentityManager(final IdentityManager idManager) {
        this.idManager = idManager;
    }

    /**
     * Returns this context's identity manager. If none has been installed,
     * returns the identity manager of the parent context.
     * @return IdentityManager
     */
    public IdentityManager getIdentityManager() {
        if (idManager == null && parentContext != null) {
            return parentContext.getIdentityManager();
        }
        return idManager;
    }

    /**
     * Locates a Node by its ID.
     *
     * @param id is the ID of the sought node.
     * @return Pointer
     */
    public Pointer getPointerByID(final String id) {
        final IdentityManager manager = getIdentityManager();
        if (manager != null) {
            return manager.getPointerByID(this, id);
        }
        throw new JXPathException(
            "Cannot find an element by ID - "
                + "no IdentityManager has been specified");
    }

    /**
     * Install a key manager that will be used by the context
     * to look up a node by a key value.
     * @param keyManager KeyManager
     */
    public void setKeyManager(final KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    /**
     * Returns this context's key manager. If none has been installed,
     * returns the key manager of the parent context.
     * @return KeyManager
     */
    public KeyManager getKeyManager() {
        if (keyManager == null && parentContext != null) {
            return parentContext.getKeyManager();
        }
        return keyManager;
    }

    /**
     * Locates a Node by a key value.
     * @param key string
     * @param value string
     * @return Pointer found
     */
    public Pointer getPointerByKey(final String key, final String value) {
        final KeyManager manager = getKeyManager();
        if (manager != null) {
            return manager.getPointerByKey(this, key, value);
        }
        throw new JXPathException(
            "Cannot find an element by key - "
                + "no KeyManager has been specified");
    }

    /**
     * Locates a NodeSet by key/value.
     * @param key string
     * @param value object
     * @return NodeSet found
     */
    public NodeSet getNodeSetByKey(final String key, final Object value) {
        final KeyManager manager = getKeyManager();
        if (manager != null) {
            return KeyManagerUtils.getExtendedKeyManager(manager)
                    .getNodeSetByKey(this, key, value);
        }
        throw new JXPathException("Cannot find an element by key - "
                + "no KeyManager has been specified");
    }

    /**
     * Registers a namespace prefix.
     *
     * @param prefix A namespace prefix
     * @param namespaceURI A URI for that prefix
     */
    public void registerNamespace(final String prefix, final String namespaceURI) {
        throw new UnsupportedOperationException(
                "Namespace registration is not implemented by " + getClass());
    }

    /**
     * Given a prefix, returns a registered namespace URI. If the requested
     * prefix was not defined explicitly using the registerNamespace method,
     * JXPathContext will then check the context node to see if the prefix is
     * defined there. See
     * {@link #setNamespaceContextPointer(Pointer) setNamespaceContextPointer}.
     *
     * @param prefix The namespace prefix to look up
     * @return namespace URI or null if the prefix is undefined.
     */
    public String getNamespaceURI(final String prefix) {
        throw new UnsupportedOperationException(
                "Namespace registration is not implemented by " + getClass());
    }

    /**
     * Gets the prefix associated with the specifed namespace URI.
     * @param namespaceURI the ns URI to check.
     * @return String prefix
     * @since JXPath 1.3
     */
    public String getPrefix(final String namespaceURI) {
        throw new UnsupportedOperationException(
                "Namespace registration is not implemented by " + getClass());
    }

    /**
     * Namespace prefixes can be defined implicitly by specifying a pointer to a
     * context where the namespaces are defined. By default,
     * NamespaceContextPointer is the same as the Context Pointer, see
     * {@link #getContextPointer() getContextPointer()}
     *
     * @param namespaceContextPointer The pointer to the context where prefixes used in
     *        XPath expressions should be resolved.
     */
    public void setNamespaceContextPointer(final Pointer namespaceContextPointer) {
        throw new UnsupportedOperationException(
                "Namespace registration is not implemented by " + getClass());
    }

    /**
     * Returns the namespace context pointer set with
     * {@link #setNamespaceContextPointer(Pointer) setNamespaceContextPointer()}
     * or, if none has been specified, the context pointer otherwise.
     *
     * @return The namespace context pointer.
     */
    public Pointer getNamespaceContextPointer() {
        throw new UnsupportedOperationException(
                "Namespace registration is not implemented by " + getClass());
    }

    /**
     * Sets the ExceptionHandler used by this context, if any.
     * @param exceptionHandler to set
     * @since 1.4
     */
    public void setExceptionHandler(final ExceptionHandler exceptionHandler) {
        throw new UnsupportedOperationException(
                "ExceptionHandler registration is not implemented by " + getClass());
    }
}
