easy-deposit-agreement-generator
===========
[![Build Status](https://travis-ci.org/DANS-KNAW/easy-deposit-agreement-generator.png?branch=master)](https://travis-ci.org/DANS-KNAW/easy-deposit-agreement-generator)

<!-- Remove this comment and extend the descriptions below -->


SYNOPSIS
--------

    easy-deposit-agreement-generator (synopsis of command line parameters)
    easy-deposit-agreement-generator (... possibly multiple lines for subcommands)


DESCRIPTION
-----------

Create a deposit agreement file for EASY deposits


ARGUMENTS
---------

    Options:

       -h, --help      Show help message
       -v, --version   Show version of this program

    Subcommand: run-service - Starts EASY Deposit Agreement Generator as a daemon that services HTTP requests
       -h, --help   Show help message
    ---

EXAMPLES
--------

    easy-deposit-agreement-generator -o value


INSTALLATION AND CONFIGURATION
------------------------------


1. Unzip the tarball to a directory of your choice, typically `/usr/local/`
2. A new directory called easy-deposit-agreement-generator-<version> will be created
3. Add the command script to your `PATH` environment variable by creating a symbolic link to it from a directory that is
   on the path, e.g. 
   
        ln -s /usr/local/easy-deposit-agreement-generator-<version>/bin/easy-deposit-agreement-generator /usr/bin



General configuration settings can be set in `cfg/application.properties` and logging can be configured
in `cfg/logback.xml`. The available settings are explained in comments in aforementioned files.


BUILDING FROM SOURCE
--------------------

Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher

Steps:

        git clone https://github.com/DANS-KNAW/easy-deposit-agreement-generator.git
        cd easy-deposit-agreement-generator
        mvn install
