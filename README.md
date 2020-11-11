# Gitlet

This project was created as a part of CS61B: Data Structures at UC Berkeley using Java.

This project, implemented a version-control system that mimics some of the basic features of the popular system Git. However, this version is much smaller and simpler than its popular counter-part.

A version-control system is essentially a backup system for related collections of files. The main functionality that Gitlet supports is:

- Saving the contents of entire directories of files. In Gitlet, this is called committing, and the saved contents themselves are called commits.
- Restoring a version of one or more files or entire commits. In Gitlet, this is called checking out those files or that commit.
- Viewing the history of your backups. In Gitlet, you view this history in something called the log.
- Maintaining related sequences of commits, called branches.
- Merging changes made in one branch into another.
