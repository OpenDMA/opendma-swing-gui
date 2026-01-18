@echo off
set DIR=%~dp0
java -cp "%DIR%lib\*;%DIR%adaptors\*" com.xaldon.opendma.swingmanager.OpendmaSwingManager %*