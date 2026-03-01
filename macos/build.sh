#!/usr/bin/env bash

clang \
  -dynamiclib \
  -framework AppKit \
  -framework CoreServices \
  -o libMacOSBridge.dylib \
  MacOSBridge.m
