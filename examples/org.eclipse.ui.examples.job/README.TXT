This examples plug-in demonstrates the following features:

1. deferred tree/table population
    - serialized children fetch jobs or concurrent
    - batching updates to the collector
    - (optional) different ways of showing progress replacing current pending node (e.g. changing parent's label / image)
    
2. progress view
    - cancellation
    - showing progress, sub tasks
    - showing errors
    - progress groups
    - appearing/disapearing as floating window
    - system jobs aren't shown
    
3. requestInUI
    - post a request
    - post a request then cancel before user has launched it
    - post a request, cancel, and replace with another
    - post a request and allow the user to cancel without running the request
    
4. blocked foreground operation
    - run a background job that locks the workspace
    - run a foreground job that requires workspace (expect blocked dialog with progress view embeded)
    - open an editor and type then save (progress dialog should show blocked)
    
5. workbench part support for progress
    - run a job in the context of a view/editor
    - get busy cursor plus animating something
    - view or editor adding their own custom behavior when job is run
    
6. Creating progress groups
    - that are run serialized
    - that are run concurrently
    - cancel one sub job
    - parent is updated correctly
    
7. running action via busyCursorWhile