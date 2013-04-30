DomDIsc
=======

This app - downloadable from the Google Play Android app-catalogue - lets you take an IBM Domino 
discussion database with you on your Android device. 
The content is stored locally on the device, letting you read and participate in the discussion 
even if there is no network connection. When you do have a network connection, the database 
replicates new entries from the server to the app, and any entries written by you on the device 
get replicated to the Domino server.
Replication happens in the background on a schedule configured by you.
You can configure the app to replicate with multiple discussion databases.
The app is developed using the Android SDK. It is a completely native app, using just Java.
The Domino Data Service api is used to replicate with IBM Domino. This requires the server to be 
either release 9+ or it can be release 8.5.3+ with the Extension Library or Upgrade Pack. 
It also requires the server and discussion database to be configured to allow the use of the 
Domino Data Service api.
