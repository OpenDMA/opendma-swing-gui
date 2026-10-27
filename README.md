# Simple graphical user interface for OpenDMA

Provide any OpenDMA adaptor on the classpath, configure `opendma-swing-gui.properties`
with the system ID and connection properties, and then run the main class
`com.xaldon.opendma.swingmanager.OpendmaSwingManager`.

The easiest way to do this is to create a new maven project which depends on this
artifact and any number of OpenDMA adaptors. You can take a look at
[this](https://github.com/xaldon/opendma-adaptor-examples/blob/main/alfresco/alfresco-gui-example/README.md)
project configuring this GUI for Alfresco.

## Copyright

While the code of this application is licensed inder GPL, it contains artwork licensed
under different terms.

Please see [LICENSE](https://github.com/OpenDMA/opendma-swing-gui/blob/main/LICENSE).

## Build

As usual:

```
mvn  clean package
```