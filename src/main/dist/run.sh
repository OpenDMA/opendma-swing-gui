#!/usr/bin/env sh
DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec java -cp "$DIR/lib/*:$DIR/adaptors/*" com.xaldon.opendma.swingmanager.OpendmaSwingManager "$@"
