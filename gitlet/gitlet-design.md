# Gitlet Design Document

**Name**: Jeremy Ahn and Vicky Yu

## Classes and Data Structures
Main/Gitlet: represents the Git object itself; contains the code for all the main commands to be executed
Branch: represents the structure of the Git committing/updating; keeps track of the head and master branch as well as previous commits; similar structure to a linked list

## Algorithms
Gitlet Class (Serializable): 

Commands:
   
- java gitlet.Main init 
    - Allows control on flat working directory
    - Initializes a separate hidden gitlet directory (.gitlet), containing all of our information
    - Calls commit
- java gitlet.Main add [file name]
    - Adds only modified or new files of the file name to the staging area 
    - If a file already exists, do not add.
java gitlet.Main commit [message]
    - Checks to see if changes were made, and commits appropriately; updating the head and master
    - Each commit is a branch and can have several branches off from the master
- java gitlet.Main rm [file name]
- java gitlet.Main log
- java gitlet.Main global-log
- java gitlet.Main status
    - Updated by the add and commit commands (possibility of also being updated by remove too)
    - Prints status of current branch and staged/removed/modified/untracked files
- java gitlet.Main checkout -- [file name]
- java gitlet.Main checkout [commit id] -- [file name]
- java gitlet.Main checkout [branch name]
- java gitlet.Main branch [branch name]
- java gitlet.Main rm-branch [branch name]
- java gitlet.Main reset [commit id]
- java gitlet.Main merge [branch name]

Branch Class (Serializable):
updateBranch(head, name): updates the current commit as a new branch; also updates the head of the most recent commit
subBranch(head, name): adds sub-branch to main branch

## Persistence
- Init: creates a repo folder, inside this repo folder; there is also a folder initialized inside the repo folder that represents the first commit; this first commit is a new branch, and is the current head of the branch
- Add: making a new file, unless it already exists
- Commit: adds a new folder into the repo folder, with the corresponding commit tag (labeling them by their commit ID possibly); inside the latest commit folder/branch, it has a copy of the contents inside the previous commit branch (going into the previous branch, getting the contents, and reading them), and makes edits appropriately according to the added files; the current commit becomes the head of the branch; also adds a commit.txt in the folder that contains the the remaining metadata (date and commit message)
- Checkout (w/o id): grabs the contents of the head branch/commit folder by reading it
- Checkout (w/ id): traverses backwards through the branch/linked-list object to find the folder that matches the appropriate commit-id, and grabs the contents from there
- Log: traverses the branch backwards exactly twice and grabs the contents of the commit.txt info that has the remaining metadata; since the commit ID is already stated as the folder name, use the folder’s name to print the appropriate ID for the commit

