#!/usr/bin/python3
# -*- coding: utf-8 -*-
"""
watchtower - Tool for monitoring the changes in multiple files and executing action.

Author: igrek51
License: Beerware
"""

import sys
import os
import subprocess
import time
import hashlib
from time import strftime
import fnmatch
import glob

# Console text formatting characters
C_RESET = '\033[0m'
C_BOLD = '\033[1m'
C_DIM = '\033[2m'
C_ITALIC = '\033[3m'
C_UNDERLINE = '\033[4m'

C_BLACK = 0
C_RED = 1
C_GREEN = 2
C_YELLOW = 3
C_BLUE = 4
C_MAGENTA = 5
C_CYAN = 6
C_WHITE = 7


def textColor(colorNumber):
    """Return character changing console text colour."""
    return '\033[%dm' % (30 + colorNumber)

C_INFO = textColor(C_BLUE) + C_BOLD
C_OK = textColor(C_GREEN) + C_BOLD
C_WARN = textColor(C_YELLOW) + C_BOLD
C_ERROR = textColor(C_RED) + C_BOLD
T_INFO = C_INFO + '[info]' + C_RESET
T_OK = C_OK + '[OK]' + C_RESET
T_WARN = C_WARN + '[warn]' + C_RESET
T_ERROR = C_ERROR + '[ERROR]' + C_RESET

def info(message):
    print(T_INFO + " " + message)

def ok(message):
    print(T_OK + " " + message)

def warn(message):
    print(T_WARN + " " + message)

def error(message):
    print(T_ERROR + " " + message)

def fatal(message):
    error(message)
    sys.exit()


def shellExec(cmd):
    """Execute shell command."""
    errCode = subprocess.call(cmd, shell=True)
    if errCode != 0:
        fatal('failed executing: %s' % cmd)


def shellExecErrorCode(cmd):
    """Execute shell command and returns error code."""
    return subprocess.call(cmd, shell=True)


def popArg(args):
    """Return first arg from args list and rest of the args."""
    if len(args) == 0:
        return (None, args)
    nextArg = args[0]
    args = args[1:]
    return (nextArg, args)


def nextArg(args):
    """Return first arg from args list."""
    if len(args) == 0:
        return None
    return args[0]


def md5File(fname):
    """Return MD5 hash of file."""
    if not os.path.isfile(fname):
        fatal('file does not exist: %s' % fname)
    hash_md5 = hashlib.md5()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()


def checksumFile(filename):
    """Calculate checksum of file."""
    return md5File(filename)


def printHelp():
    """Print help message."""
    print("""Tool for monitoring the changes in multiple files. \
When any change is detected a given command is executed.

Usage:
 %s [options] -f '<files>' [...] -e <command>

Optional arguments:
 -f, --files <file1> [<file2>] ['<pattern1>'] [...]\tinclude masks - absolute or relative pathnames \
or shell-style wildcard patterns (including recursive directories and subdirectories), which refer to files to be observed
  example patterns: file1, prefix*, 'dir1/*', "*.tex", 'dir2/*.py', "*", '**', '**/*.py', '/abs/path/*', '/abs/path/**'
 -x, --exclude <file1> [<file2>] ['<pattern1>'] [...]\texclude masks - filenames \
or shell-style wildcard patterns, which contains files not to be observed
 -e, --exec <command>\texecute given command when any change is detected
 -i, --interval <seconds>\tset interval between subsequent changes checks \
(default 1 s)
 --noinit\tdo not execute command on the initialization
 -h, --help\tdisplay this help and exit""" % sys.argv[0])


def currentTime():
    """Return current time as a string."""
    return strftime("%H:%M:%S", time.localtime())


class ObservedFile:
    """Data structure containing file path with its last checksum."""

    def __init__(self, filePath):
        """Create ObservedFile.

        @param filePath: file path
        """
        self.filePath = filePath
        self.lastChecksum = None

    def __str__(self):
        return "%s:%s" % (self.filePath, self.lastChecksum)


class Main:
    """Main logic."""

    def __init__(self):
        """Create Main."""
        self.interval = 1  # seconds between subsequent changes checks
        self.executeCmd = None
        self.includePatterns = []
        self.excludePatterns = []
        self.observedFiles = []
        self.firstInit = True

    def start(self):
        """Start application."""
        self._analyzeArgs()
        self._validateArgs()
        self._listObservedFiles() # TODO refresh observed files list in cycles - not only at startup
        self._lookForChanges()

    def _analyzeArgs(self):
        args = sys.argv[1:]  # additional arguments list

        if not args:
            printHelp()
            sys.exit()

        while args:
            args = self._analyzeArg(*popArg(args))

    def _analyzeArg(self, arg, args):
        # help message
        if arg == '-h' or arg == '--help':
            printHelp()
            sys.exit()
        # interval set
        if arg == '-i' or arg == '--interval':
            (intervalStr, args) = popArg(args)
            self.interval = int(intervalStr)
        # execute command - everything after -e
        elif arg == '-e' or arg == '--exec':
            if not args:
                fatal('not given command to execute')
            # pop all args
            self.executeCmd = ' '.join(args)
            args = []
        # no init
        elif arg == '--noinit':
            self.firstInit = False
        # select files to observe
        elif arg == '-f' or arg == '--files':
            if nextArg(args) is None:
                fatal('no including file patterns specified')
            '''read params until there is no param'''
            ''' or param is from another option group'''
            while True:
                nextA = nextArg(args)  # just read next param, do not pop
                # if param is from another group
                if nextA is None or nextA.startswith('-'):
                    break
                (pattern, args) = popArg(args)
                self.includePatterns.append(pattern)
        # excluded files
        elif arg == '-x' or arg == '--exclude':
            if nextArg(args) is None:
                fatal('no excluding file patterns specified')
            '''read params until there is no param'''
            ''' or param is from another option group'''
            while True:
                nextA = nextArg(args)  # just read next param, do not pop
                # if param is from another group
                if nextA is None or nextA.startswith('-'):
                    break
                (pattern, args) = popArg(args)
                self.excludePatterns.append(pattern)
        else:
            fatal('invalid argument: %s' % arg)
        return args

    def _validateArgs(self):
        if self.interval < 1:
            fatal('interval < 1')
        if len(self.includePatterns) == 0:
            fatal('no file patterns specified')

    def _listObservedFiles(self):
        workdir = os.getcwd()

        includedPaths = self._listFileAbsPaths(workdir, self.includePatterns)
        excludedPaths = self._listFileAbsPaths(workdir, self.excludePatterns)
        # get files from includedPaths but not in excludedPaths
        filePaths = includedPaths - excludedPaths

        # create list of unique relative paths
        for filePath in filePaths:
            relativePath = os.path.relpath(filePath, workdir)
            self.observedFiles.append(ObservedFile(relativePath))
        # validate found files
        if not self.observedFiles:
            fatal('no matching file found for specified patterns')
        # TEST ovserved files list
        # for p in self.observedFiles:
        #     print(p)

    def _listFileAbsPaths(self, workdir, patterns):
        # collection of unique absolute file paths
        paths = set()
        for pattern in patterns:
            files = glob.glob(pattern, recursive=True)
            for file in files:
                if os.path.isfile(file):
                    absPath = os.path.join(workdir, file)
                    paths.add(absPath)
        return paths

    def _isMatchingAnyPattern(self, filename, patterns):
        for pattern in patterns:
            if fnmatch.fnmatch(filename, pattern):
                return True
        return False

    def _lookForChanges(self):
        try:
            initialRun = True
            while True:
                changedFiles = self._findChangedFiles()
                # if anything has been changed
                if changedFiles:
                    for changedFile in changedFiles:
                        if initialRun:
                            info('Observed file found: %s' % changedFile.filePath)
                        else:
                            info('%s - File change detected: %s' % (currentTime(), changedFile.filePath))
                    # execute given command
                    if self.executeCmd:
                        # executing on initialization or next change detected
                        if self.firstInit or not initialRun:
                            msg = 'First executing: %s' if initialRun else 'Executing: %s'
                            info(msg % self.executeCmd)
                            errCode = shellExecErrorCode(self.executeCmd)
                            if errCode == 0:
                                ok('') # success
                            else:
                                error('failed executing: %s' % self.executeCmd)
                    initialRun = False
                # wait some time before next check
                time.sleep(self.interval)

        except KeyboardInterrupt:
            # Ctrl + C handling without printing stack trace
            print  # new line

    def _findChangedFiles(self):
        """Check if some of the observed files has changed
        and return all changed files.
        """
        changedFiles = []
        # calculate and update checksums always for ALL files
        for observedFile in self.observedFiles:
            if os.path.isfile(observedFile.filePath):
                currentChecksum = checksumFile(observedFile.filePath)
            else:
                currentChecksum = None
            # different values with None value checking
            if ((observedFile.lastChecksum is None
                    and currentChecksum is not None)
                    or observedFile.lastChecksum != currentChecksum):
                changedFiles.append(observedFile)  # notify change
                observedFile.lastChecksum = currentChecksum  # update checksum

        return changedFiles


Main().start()
