Eclipse4/RCP/Contexts
=====================

The Eclipse 4 Application Platform manages state and services using a set of _contexts_; this information is used for injection. 
Contexts are used as the sources for [Dependency Injection](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Eclipse4_RCP_Dependency_Injection.md). 
In this respect, they are somewhat analogous to _modules_ in Guice. 
Normally code should not have to use or know about the context.

  

Contents
--------

*   [1 What is a Context?](#What-is-a-Context)
*   [2 The Use of Contexts in Eclipse 4](#The-Use-of-Contexts-in-Eclipse-4)
*   [3 Context Variables](#Context-Variables)
*   [4 Context Chains and the Active Context](#Context-Chains-and-the-Active-Context)
*   [5 Context Functions](#Context-Functions)
*   [6 Run And Tracks](#Run-And-Tracks)
*   [7 Exposing Services and State on an Eclipse Context](#Exposing-Services-and-State-on-an-Eclipse-Context)
    *   [7.1 Context Functions](#context-functions-1)
    *   [7.2 OSGi Services](#OSGi-Services)
    *   [7.3 Context Functions Exposed As OSGi Declarative Services](#Context-Functions-Exposed-As-OSGi-Declarative-Services)
*   [8 Creating New Contexts](#Creating-New-Contexts)
*   [9 Advanced Topics](#Advanced-Topics)
    *   [9.1 How do I access the current context?](#How-do-I-access-the-current-context)
    *   [9.2 @Active vs ACTIVE_*](#active-vs-active_)
*   [10 References](#References)

What is a Context?
------------------

A context (a IEclipseContext) is a hierarchical key-value map. The keys are strings, often Java class names, and the values are any Java object. Each context has a parent, such that contexts are linked together to form a tree structure. When a key is not found in a context, the lookup is retried on the parent, repeating until either a value is found or the root of the tree has been reached.


The Use of Contexts in Eclipse 4
--------------------------------

![300px-Ui-context-hierarchy.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/300px-Ui-context-hierarchy.png)


Eclipse 4 associates contexts to the container elements in the UI

Eclipse 4 uses contexts to simplify access to workbench services (the _service locator_ pattern) and other interesting state. 
Contexts provide special support for creating and destroying values as necessary, and for tracking changes made to context values.

Values are normally added to an Eclipse Context via _IEclipseContext#set(key,value)_ or _#modify(key,value)_. 
Values are retrieved by _#get(key)_, which returns _null_ if not found. 
There is a special variant of _get_: Java class names are frequently used as keys and instances as values, so there is a special _T get(Class<T>)_ that casts the value as an instance of _T_.

The power of contexts comes as Eclipse 4 associates a context with most model elements — the logical UI containers — such that the context tree matches the UI hierarchy. 
So an _MPart_ and its containing _MPerspective_, _MWindow_, and the _MApplication_, each have contexts and are chained together. 
Looking up a key that is not found in the part will cause the lookup to continue at the perspective, window, and application. 
At the top of the tree is a special node that looks up keys among the available OSGi services. 
Many application-, window-, and view-level services are installed by the Eclipse 4 at various levels in the context hierarchy. 
Thus the placement of values within the context hierarchy, such as in the perspective or window's context, provides a natural form of variable scoping.

**Example**  

For example, many client-server applications may require communicating with multiple servers, but with one server chosen as a master server at any one time. 
An Eclipse 4 application could record this master server in the application's context using a well-known key (e.g., "MASTER\_SERVER"). 
All parts requesting injection for that key will have the value resolved from the application's context. 
Should that value change, all parts will be re-injected with the new value. 
A particular part could have a different master server from other parts by setting the master in that part's context. 
All other parts will continue to resolve MASTER\_SERVER from the application. But perhaps the developers later realize that it would be very powerful to have a different master server for each window. 
The master could instead be set in each window's context. 
Or perhaps the app would prefer to have a different master server for each perspective, or even on particular part stacks. 
Or the app could continue to set the normal master server in the application's context, and optionally override it on a per-window basis by setting the override value in the window's context.

  

Context Variables
-----------------

Being able to resolve a value from somewhere in the context hierarchy is very powerful. 
But to change the value, we need to know where in the context hierarchy the value should be set. 
Rather than hard code this location, we can instead declare a _context variable_: we declare the variable at the appropriate context, and instead _modify_, rather than _set_, the context value: the context then looks up the chain to find the variable declaration and sets the value there. 
This separates defining _where_ a context value should be placed from the code that actually _sets_ it.

**Example (continued)**  

By declaring a context variable for the master server, if we later decide that we want the master-server to actually be maintained on a per-perspective basis, then we simply move the context variable definition to be on the perspective; the code obtaining and modifying the value is completely oblivious to the change.


Context Chains and the Active Context
-------------------------------------

![300px-Ui-contexts-active.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/300px-Ui-contexts-active.png)

The editor is the active leaf

Contexts are chained together via the parent link. 
A context may have many children, but a context only exposes its active child. 
The chain of active children from a node is called its _active branch_, and the end node is the _active leaf_. 
There are many active branches in a context tree, but there is only ever a single active branch from the root.

A node can be made active in two ways. 
Calling _#activate()_ makes the receiver the active child of its parent node, but does not otherwise disturb the rest of the tree. 
Calling _#activateBranch()_ on the other hand effectively the same as:

       void activateBranch() {
          activate();
          if(getParent() != null) getParent().activateBranch(); 
       }

It makes the receiver the active child of its parent, and then recursively calls _#activateBranch()_ on its parent.

It's often useful to resolve values from the active leaf with #getActive(key).

Eclipse 4 keeps its IEclipseContext activation state in sync with the UI state, such that the active window's context is the active window-level context, and each window's active part is that window's active leaf o.

Context Functions
-----------------

Contexts support a special type of value called a _context function_. 
When a retrieved key's value is a context function, the IEclipseContext calls _compute(context, key)_ and returns the result of the computation. 
Context sanctions must subclass _org.eclipse.e4.core.contexts.ContextFunction_.

For example, the Eclipse 4 Workbench makes the current selection available via a context function:

    appContext.set(SELECTION, new ContextFunction() {
        @Override
        public Object compute(IEclipseContext context, String contextKey) {
            IEclipseContext parent = context.getParent();
            while (parent != null) {
                context = parent;
                parent = context.getParent();
            }
            return context.getActiveLeaf().get("out.selection");
        }
    });

The result of a context function are _memoized_: they are only recomputed when another referenced value is changed. 
See the section on _Run And Tracks_ below.

  

Run And Tracks
--------------

_RunAndTracks_ , affectionally called _RATs_, are a special form of a _Runnable_. 
RATs are executed within a context, and the context tracks all of the values accessed. 
When any of these values are changed, the runnable is automatically re-evaluated. 
The following example will print _20.9895_ and then _20.12993_:


    final IEclipseContext context = EclipseContextFactory.create();
    context.set("price", 19.99);
    context.set("tax", 0.05);
    context.runAndTrack(new RunAndTrack() {
        @Override
        public boolean changed(IEclipseContext context) {
            total = (Double) context.get("price") * (1.0 + (Double) context.get("tax"));
            return true;
        }
     
        @Override
        public String toString() {
            return "calculator";
        }
    });
    print(total);
    context.set("tax", 0.07);
    print(total);

A RAT continues executing until either its context is disposed, or its changed() method returns _false_.

Note that RATs are only re-evaluated when the value is changed (i.e., IEclipseContext#set() or #modify() are called), and not when the contents of the value are changed.

Exposing Services and State on an Eclipse Context
-------------------------------------------------

Values are normally add to an Eclipse Context via _IEclipseContext#set(key,value)_ or _#modify(key,value)_. 
But these require knowing and being able to find the context to be modified. 
But developers sometimes need to be able to add values on-the-fly. 
There are a few techniques.

### Context Functions

A [Context Function](#context-functions) is provided both the key that was requested and the source context, where the retrieval began. 
The context function can return an instance created for that particular context, or set a value in that context — or elsewhere. 
This approach is very useful for computing results based on the active part (_IEclipseContext#getActiveLeaf()_).

### OSGi Services

The Eclipse 4 workbench roots its context hierarchy from an _EclipseContextOSGi_, a special Eclipse Context that knows to look up keys in the OSGi Service Registry. 
_EclipseContextOSGi_ instances are obtained via _EclipseContextFactory#getServiceContext(BundleContext)_. 
These contexts — and the services requested — are bounded by the lifecycle of the provided bundle.

  

### Context Functions Exposed As OSGi Declarative Services

This approach exposes a context function as the implementation of a service defined OSGi Declarative Services. 
This pattern is used for creating the _IEventBroker_, using the new DS annotations support.

    @Component(service = IContextFunction.class)
    @IContextFunction.ServiceContextKey(org.eclipse.e4.core.services.events.IEventBroker.class)
    public class EventBrokerFactory extends ContextFunction {
        @Override
        public Object compute(IEclipseContext context, String contextKey) {
            EventBroker broker = context.getLocal(EventBroker.class);
            if (broker == null) {
                broker = ContextInjectionFactory.make(EventBroker.class, context);
                context.set(EventBroker.class, broker);
            }
            return broker;
        }
    }

Note that the service is actually exposed as an IContextFunction, not an IEventBroker. 
This approach is specific to being used for values retrieved from an IEclipseContext.

Creating New Contexts
---------------------

Contexts can be created either as a leaf of another context (see _IEclipseContext#newChild()_) or as a new root (see _EclipseContextFactory#create()_). 
A special EclipseContext implementation exists (_EclipseContextOSGi_, obtained by _EclipseContextFactory#getServiceContext()_) to expose OSGi Services too.

  

Advanced Topics
---------------

### How do I access the current context?

_Current_ really depends on the requesting context. 
An _MPart_ or _IViewPart_ rarely wants the active part, which may not be itself, but a particular part, such as the active editor.

_IServiceLocator_, either implemented by or provided by many components in the Eclipse Workbench, was the primary means to obtain services in the Eclipse Workbench. 
It is now backed by an IEclipseContext. 
You can either fetch values directly via _IServiceLocator#getService(key)_ or obtain the _IServiceLocator_s _IEclipseContext_ directly (_IServiceLocator.getService(IEclipseContext.class)_). 
Most UI containers implement _IServiceLocator_ like _IWorkbench_, _IWorkbenchWindow_, _IWorkbenchPart_.

  

### @Active vs ACTIVE_*

@Active is an annotation that causes our DI to look up a value from the source context's active leaf, where the source context is the context that was used for injecting that object.

ACTIVE_PART, on the other hand, looks for the active leaf as constrained by the source context's window's context.

    public class ActivePartLookupFunction extends ContextFunction {
        @Override
        public Object compute(IEclipseContext context, String contextKey) {
            MContext window = context.get(MWindow.class);
            if (window == null) {
                window = context.get(MApplication.class);
                if (window == null) {
                    return null;
                }
            }
            IEclipseContext current = window.getContext();
            if (current == null) {
                return null;
            }
            return current.getActiveLeaf().get(MPart.class);
        }
    }

ACTIVE_SHELL is _(needs some work)_

The moral: the implementation of _active X_ is not necessarily as straightforward as might appear.
