# Introduction #

JArq provides you with a simple aspect that allows to specify a permission for every method that match the aspect.
If the user that is executing the method doesn't have the defined permission an [AutorisationException](http://jarq.es/api/Core/es/onlysolutions/arq/core/auth/exception/AutorisationException.html) is thrown.


# AOP #

Basically, [the advice](http://jarq.es/api/Core/es/onlysolutions/arq/core/aop/AutorizationAdvice.html) just check if the user that are in session contains the permission that you defined in the permission's file.
You will need that the facade method are call inside a Controller that extends GenericController. If not, you will need to handle manually the [AutorisationException](http://jarq.es/api/Core/es/onlysolutions/arq/core/auth/exception/AutorisationException.html) and redirect the petition to your desired page.

# JSTL Tag #

There is a tag called [hasPermission](http://jarq.es/api/Core/es/onlysolutions/arq/core/mvc/tag/HasPermissionTag.html) that allows you to hide the code inside the body of the tag if the user doesn't have the permissions defined in the _permission_ element. Check the JavaDoc for more information.