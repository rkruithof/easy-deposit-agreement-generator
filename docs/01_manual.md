---
title: Manual
layout: home
---

Manual
======

TABLE OF CONTENTS
-----------------

- [SYNOPSIS](#synopsis)
- [DESCRIPTION](#description)
- [ARGUMENTS](#arguments)
- [INSTALLATION AND CONFIGURATION](#installation-and-configuration)
- [BUILDING FROM SOURCE](#building-from-source)


SYNOPSIS
--------

    easy-deposit-agreement-generator [ -p ] -i <agreement-request-file> -o <agreement-file>


DESCRIPTION
-----------

A command line tool that creates a pdf document containing the deposit agreement for a given dataset. 
The tool uses the provided metadata on the dataset and the depositor to generate the deposit agreement.

`easy-deposit-agreement-generator` uses a template with placeholders. After replacing the placeholders with actual data, the template is converted into a PDF file.

Placeholder substitution is achieved using [Apache Velocity](http://velocity.apache.org/), which fills in and merges a number of template HTML 
files that are specified in `src/main/assembly/dist/res/template/`. Besides data from the dataset, several files in `src/main/assembly/dist/res/` 
are required, namely `dans_logo.png`, `agreement_version.txt`, `Metadataterms.properties` and `velocity-engine.properties`.

Pdf generation based on the assembled HTML is done using the command line tool [WeasyPrint](http://weasyprint.org/). Note that this tool 
requires to be installed before being used by `easy-deposit-agreement-generator`. 

A `--preview` or `-p` flag can be added to the command line tool to signal that a 'preview agreement' needs to be created. This version of the agreement
can be created when the deposit has net yet been submitted. Also in the title of the agreement it is clearly indicated that this version is a *preview*.

ARGUMENTS
---------

     -p, --preview   Indicates whether or not the agreement is a preview, default is false
     -i, --input     The location of the JSON file containing the request
     -o, --output    The location for resulting PDF containing the Deposit Agreement
     -h, --help      Show help message
     -v, --version   Show version of this program
    

INSTALLATION AND CONFIGURATION
------------------------------
The preferred way of install this module is using the RPM package. This will install the binaries to
`/opt/dans.knaw.nl/easy-deposit-agreement-generator`, the configuration files to `/etc/opt/dans.knaw.nl/easy-deposit-agreement-generator`,
and will install the service script for `initd` or `systemd`.

If you are on a system that does not support RPM, you can use the tarball. You will need to copy the
service scripts to the appropriate locations yourself.

General configuration settings can be set in `src/main/assembly/dist/cfg/appliation.properties` and logging can be configured
in `src/main/assembly/dist/cfg/logback.xml`. The available settings are explained in comments in aforementioned files.


**WeasyPrint** is installed according to the [installation page](http://weasyprint.readthedocs.io/en/latest/install.html) or via:

```
yum install redhat-rpm-config python-devel python-pip python-lxml cairo pango gdk-pixbuf2 libffi-devel weasyprint
```

After this, `weasyprint --help` is supposed to show the appropriate help page.


BUILDING FROM SOURCE
--------------------

Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher

Steps:

        git clone https://github.com/DANS-KNAW/easy-deposit-agreement-generator.git
        cd easy-deposit-agreement-generator
        mvn install
