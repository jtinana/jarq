# Introduction #

This Advice intercepts the calls of the methods of the class, and it checks if the call it has been already executed and stored at memcache. If yes, just retrieve that previous result and finish the method execution, without need of database access.
On the other hand, if it was the firts time that the method (with that parameters) it's been executed, it stores the result at memcache and mark the method as cached for next executions.


# Details #

You will need to have an installation of memcache in your enviroment in order to configure the advice, but there is no dependecies with any other componente of JArq, so may be in future releases could be moved to as a standalone project.
To see details about the configuration just wait to register here, or see the JavaDoc.