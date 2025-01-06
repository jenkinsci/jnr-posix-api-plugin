# JNR Posix API Plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/jnr-posix-api-plugin/job/main/badge/icon)](https://ci.jenkins.io/job/Plugins/job/jnr-posix-api-plugin/job/main/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/jnr-posix-api.svg)](https://plugins.jenkins.io/jnr-posix-api)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/jnr-posix-api-plugin.svg?label=changelog)](https://github.com/jenkinsci/jnr-posix-api-plugin/releases/latest)
[![GitHub license](https://img.shields.io/github/license/jenkinsci/jnr-posix-api-plugin)](https://github.com/jenkinsci/jnr-posix-api-plugin/blob/main/LICENSE.md)

This plugin exposes the [jnr-posix](http://github.com/jnr/jnr-posix) API to Jenkins plugins.
which was removed from jenkins-core since release 2.338

# Build

To build the plugin locally:

```
mvn -Dset.changelist clean install
```
