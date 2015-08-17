# Introduction #

There are many projects that allows you to implement the authentication into your app. Of course, you can use any of them with JArq, just ignore this, and mark your global controller with the [authentication flag](http://jarq.es/api/Core/es/onlysolutions/arq/core/mvc/controller/GlobalController.html#setAuthenticationEnabled(boolean)) as **false**.

# IUserSettings #

This interface provides JArq all the information that needs to handles with the authentication (and later, as you will see, will autorization too).

You need to code your own class that implements this interface, and a login controller that introduce your objet into the HttpSession under the key **[IUserSettings.USER\_SETTINGS\_ATTRIBUTE\_NAME](http://jarq.es/api/Core/es/onlysolutions/arq/core/auth/IUserSettings.html#USER_SETTINGS_ATTRIBUTE_NAME)**.

Actually, you need to implements your own LoginController and your own LoginValidator (or similar) to check the auth token and construct your [IUserSettings](http://jarq.es/api/Core/es/onlysolutions/arq/core/auth/IUserSettings.html) object.

# GenericController #

All your controllers needs is extends from [GenericController](http://jarq.es/api/Core/es/onlysolutions/arq/core/mvc/controller/GenericController.html). with finally extends from SimpleFormController. The explanation about how SimpleFormController works is out of the scope of this documentation.

# Other details #

If for any reason you need to implement a controller without the Authentication enabled, just set the _authenticationEnabled_ flag to false.