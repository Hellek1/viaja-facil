# viaja-facil

Viaja Facil is a software that I (David Hellekalek / Github user Hellek1) wrote in 2011. It helps the user to find his way on public transportation in the city of Buenos Aires, including the suburbs.
I decided to make it publicly available since I don't have time to go on maintaining it.

Viaja Facil is based on the following technologies:
* Java
* Google App Engine
* Google Web Toolkit
* Some custom JavaScript

Viaja Facil is split into 3 components:
* Server component that runs on Google App Engine: This component serves the web frontend and the backend services for Web and Android frontends. Currently called "Colectivos-GBA-HR" which was the working name. HR stands for high replication, a GAE feature that was introduced while I was creating viaja facil.
* Android App: The app relies on the app engine backend
* Local helpers (called "create stops"): The most significant part is the library of bus routes. Additionally, there is a tool that helps 

The project requires several publicly available java libraries which you should be able to identify if you attempt to build the code in Eclipse. Just google them and download them.

TODOS:
* Convert into Maven project and properly reference missing libraries
* Improve documentation
* Improve code ;-)
* Enhance create-stops with decent authentication (right now it uses a secret key that simply has to match on server and client side)
* Translate comments to english
* Update Google APIs (particularly Maps and Autocomplete) to the latest versions
* Update Objectify to version 5

If you have questions please open a ticket on Github within the repository and I will try to answer. Please be aware that I have a very intense job and it may take a while for me to respond.

It is absolutely necessary to have good knowledge of Google App Engine (GAE), Google Web Toolkit (GWT) and the GAE datastore (and the Objectify library) to do anything with the GAE backend.
