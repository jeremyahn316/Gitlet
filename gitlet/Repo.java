package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/** The maion body of the Gitlet Repo.
 * @author Jeremy Ahn
 */
public class Repo {

    /** Creates a new gitlet repo.
     *
     * @throws IOException
     */
    public void init() throws IOException {
        if (_repo.exists()) {
            System.out.println("Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        _repo.mkdir();
        _blobs.mkdir();
        _commit.mkdir();
        _branch.mkdir();
        _branches.add("master");
        Utils.writeObject(new File(".gitlet/current"), _currBranch);
        Utils.writeObject(new File(".gitlet/stagingadd"), _stagingAdd);
        Utils.writeObject(new File(".gitlet/stagingremoval"), _stagingR);
        Commit initial = new Commit("initial commit", null, null, null);
        _head = initial.S;
        Utils.serialize(_head);
        Utils.writeObject(new File(".gitlet/head"), _head);
        File commit = new File(".gitlet/commit/" + _head);
        commit.createNewFile();
        Utils.writeObject(commit, initial);
        Utils.writeObject(new File(".gitlet/branch/master"), initial.S);
        Utils.serialize(_branches);
        Utils.writeObject(new File(".gitlet/branches"), _branches);
    }

    /** Adds the that are to be added.
     *
     * @param name  The name of the file that we're adding
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void add(String name) throws IOException {
        File add = new File(name);
        _stagingR = Utils.readObject
                (new File(".gitlet/stagingremoval"), ArrayList.class);
        _stagingAdd = Utils.readObject
                (new File(".gitlet/stagingadd"), HashMap.class);
        if (_stagingR.contains(name)) {
            _stagingR.remove(name);
            Utils.writeObject(new File(".gitlet/stagingremoval"),
                    _stagingR);
        } else if (!add.exists()) {
            System.out.println("File does not exist.");
            return;
        } else {
            String sha1 = Utils.sha1(Utils.readContents(add))
                    + Utils.sha1(Utils.serialize(add));
            File stage = new File(".gitlet/blobs/" + sha1);
            if (!stage.exists()) {
                stage.createNewFile();
                Utils.writeContents(stage, Utils.readContentsAsString(add));
            }
            String recent = Utils.readObject
                    (new File(".gitlet/head"), String.class);
            Commit commit = Utils.readObject
                    (new File(".gitlet/commit/" + recent), Commit.class);
            if (_stagingAdd.containsKey(name)
                    && commit._staged.containsKey(name)) {
                if (_stagingAdd.get(name).equals(commit._staged.get(name))) {
                    _stagingAdd.remove(name);
                }
            }
            if (commit._staged == null || commit._staged.get(name) == null
                    || !(commit._staged.get(name).equals(sha1))) {
                _stagingAdd.put(name, sha1);
            }
            Utils.writeObject(new File(".gitlet/stagingadd"), _stagingAdd);
        }
    }


    /** Commits the current updates so that this programs remembers.
     *
     * @param message The message that is associated with this particular commit
     * @param merge The sha1 of the merge
     */
    @SuppressWarnings("unchecked")
    public void commit(String message, String merge) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        _currBranch = Utils.readObject
                (new File(".gitlet/current"), String.class);
        _stagingR = Utils.readObject
                (new File(".gitlet/stagingremoval"), ArrayList.class);
        _stagingAdd = Utils.readObject
                (new File(".gitlet/stagingadd"), HashMap.class);
        if (_stagingAdd.isEmpty() && _stagingR.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        String recent = Utils.readObject
                (new File(".gitlet/head"), String.class);
        Commit recentCommit = Utils.readObject
                (new File(".gitlet/commit/" + recent), Commit.class);
        HashMap<String, String> stage = new HashMap<>(recentCommit._staged);
        for (String name: _stagingAdd.keySet()) {
            stage.put(name, _stagingAdd.get(name));
        }
        if (!_stagingR.isEmpty()) {
            for (String commit: _stagingR) {
                stage.remove(commit);
            }
        }
        Commit clone = new Commit(message, recent, merge, stage);
        _head = clone.S;
        Utils.writeObject(new File(".gitlet/head"), _head);
        Utils.writeObject(new File(".gitlet/commit/" + _head), clone);
        Utils.writeObject(new File(".gitlet/branch/" + _currBranch), _head);
        _stagingR = new ArrayList<String>();
        _stagingAdd = new HashMap<String, String>();
        Utils.writeObject(new File(".gitlet/stagingadd"), _stagingAdd);
        Utils.writeObject(new File(".gitlet/stagingremoval"), _stagingR);
    }

    /** Removes a file given its file name.
     *
     * @param name The name of the file that is to be deleted
     */
    @SuppressWarnings("unchecked")
    public void rm(String name) {

        String recent = Utils.readObject
                (new File(".gitlet/head"), String.class);
        Commit recentCommit = Utils.readObject
                (new File(".gitlet/commit/" + recent), Commit.class);
        _stagingAdd = Utils.readObject
                (new File(".gitlet/stagingadd"), HashMap.class);
        _stagingR = Utils.readObject
                (new File(".gitlet/stagingremoval"), ArrayList.class);
        if (_stagingAdd.containsKey(name)) {
            _stagingAdd.remove(name);
            Utils.writeObject(new File(".gitlet/stagingadd"), _stagingAdd);
        } else if (recentCommit._staged.containsKey(name)) {
            _stagingR.add(name);
            if (Utils.plainFilenamesIn(System.getProperty("user.dir"))
                    .contains(name)) {
                Utils.restrictedDelete(name);
            }
            Utils.writeObject(new File(".gitlet/stagingremoval"),
                    _stagingR);
        } else if (!_stagingR.contains(name)
                && !recentCommit._staged.containsKey(name)) {
            System.out.println("No reason to remove the file.");
            return;
        }
    }

    /** Gives the log info from a certain commit to its initial commit.
     *
     */
    public void log() {
        String recent = Utils.readObject
                (new File(".gitlet/head"), String.class);
        while (recent != null) {
            Commit rC = Utils.readObject
                    (new File(".gitlet/commit/" + recent), Commit.class);
            System.out.println("===");
            System.out.println("commit " + rC.S);
            if (rC._parent2 != null) {
                System.out.println("Merge: " + rC._parent.substring(0, 7)
                        + " " + rC._parent2.substring(0, 7));
            }
            System.out.println("Date: " + rC._timestamp);
            System.out.println(rC._message);
            System.out.println();
            recent = rC._parent;
        }
    }

    /** Gives the log of all commits.
     *
     */
    public void globalLog() {
        ArrayList<String> checkboi = new ArrayList<String>();
        String[] recentAll = new File(".gitlet/commit").list();
        for (String recent: recentAll) {
            while (recent != null) {
                Commit recentCommit = Utils.readObject
                        (new File(".gitlet/commit/" + recent), Commit.class);
                if (checkboi.contains(recent)) {
                    recent = recentCommit._parent;
                } else {
                    System.out.println("===");
                    System.out.println("commit " + recentCommit.S);
                    System.out.println("Date: " + recentCommit._timestamp);
                    System.out.println(recentCommit._message);
                    System.out.println();
                    checkboi.add(recent);
                    recent = recentCommit._parent;
                }
            }
        }
    }

    /** Given a commit message, find the commit id.
     *
     * @param message   The message associated with a commit
     */
    public void find(String message) {
        boolean exists = false;
        String[] recentAll = new File(".gitlet/commit").list();
        for (String recent: recentAll) {
            Commit recentCommit = Utils.readObject
                    (new File(".gitlet/commit/" + recent), Commit.class);
            if (recentCommit._message.equals(message)) {
                System.out.println(recent);
                exists = true;
            }
        }
        if (!exists) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    /** Gives all the status of each things.
     *
     */
    public void status() {
        statusBranches();
        statusStaged();
        statusRemoved();
        statusModified();
        statusUntracked();
    }

    /** The status of all branches.
     *
     */
    @SuppressWarnings("unchecked")
    public void statusBranches() {
        _currBranch = Utils.readObject
                (new File(".gitlet/current"), String.class);
        _branches = Utils.readObject
                (new File(".gitlet/branches"), ArrayList.class);
        _branches.remove(_currBranch);
        System.out.println("=== Branches ===");
        System.out.println("*" + _currBranch);
        for (String branch: _branches) {
            System.out.println(branch);
        }
        System.out.println();
    }

    /** The status of added files.
     *
     */
    @SuppressWarnings("unchecked")
    public void statusStaged() {
        _stagingAdd = Utils.readObject
                (new File(".gitlet/stagingadd"), HashMap.class);
        System.out.println("=== Staged Files ===");
        for (String recent: _stagingAdd.keySet()) {
            System.out.println(recent);
        }
        System.out.println();
    }

    /** The status of removed files.
     *
     */
    @SuppressWarnings("unchecked")
    public void statusRemoved() {
        _stagingR = Utils.readObject
                (new File(".gitlet/stagingremoval"), ArrayList.class);
        System.out.println("=== Removed Files ===");
        for (String recent: _stagingR) {
            System.out.println(recent);
        }
        System.out.println();
    }

    /** The status of modified files.
     *
     */
    public void statusModified() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
    }

    /** The status of untracked files.
     *
     */
    public void statusUntracked() {
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }


    /** Reverts the given file to its previous commit.
     *
     * @param name  The file name that we want to evert
     */
    public void checkoutFileName(String name) {
        String recent = Utils.readObject
                (new File(".gitlet/head"), String.class);
        Commit recentCommit = Utils.readObject
                (new File(".gitlet/commit/" + recent), Commit.class);
        if (!recentCommit._staged.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            String sha = recentCommit._staged.get(name);
            File previous = new File(".gitlet/blobs/" + sha);
            File change = new File(name);
            Utils.writeContents(change, Utils.readContentsAsString(previous));
        }
    }

    /** Switching a certain file to its previous commit given the id.
     *
     * @param id    The commit id of the previous commit
     * @param name  The name of the file that you want to revert
     */
    public void checkoutCommitID(String id, String name) {
        File recent = null;
        String[] allCommit = new File(".gitlet/commit").list();
        for (String commit: allCommit) {
            if (commit.contains(id)) {
                recent = new File(".gitlet/commit/" + commit);
            }
        }
        if (recent == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit previousCommit = Utils.readObject(recent, Commit.class);
        if (!previousCommit._staged.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String sha = previousCommit._staged.get(name);
        File before = new File(".gitlet/blobs/" + sha);
        File change = new File(name);
        Utils.writeContents(change, Utils.readContentsAsString(before));
    }

    /** Updates the current branch to the branch name given.
     *
     * @param name  The branch we want to switch to
     */
    @SuppressWarnings("unchecked")
    public void checkoutBranch(String name) {
        _currBranch = Utils.readObject
                (new File(".gitlet/current"), String.class);
        _branches = Utils.readObject
                (new File(".gitlet/branches"), ArrayList.class);

        if (!_branches.contains(name)) {
            System.out.println("No such branch exists.");
            return;
        } else if (_currBranch.equals(name)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        String changeID = Utils.readObject
                (new File(".gitlet/branch/" + name), String.class);
        Commit changeCommit = Utils.readObject
                (new File(".gitlet/commit/" + changeID), Commit.class);

        String currenthead = Utils.readObject
                (new File(".gitlet/head"), String.class);
        Commit currentCommit = Utils.readObject
                (new File(".gitlet/commit/" + currenthead), Commit.class);

        for (String commit: Utils.plainFilenamesIn
                (System.getProperty("user.dir"))) {
            if (!currentCommit._staged.containsKey(commit)
                    && changeCommit._staged.containsKey(commit)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }

        for (String file: changeCommit._staged.keySet()) {
            String sha1 = changeCommit._staged.get(file);
            File before = new File(".gitlet/blobs/" + sha1);
            File change = new File(file);
            Utils.writeContents(change, Utils.readContentsAsString(before));
        }

        for (String file: currentCommit._staged.keySet()) {
            if (!changeCommit._staged.containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }

        Utils.writeObject(new File(".gitlet/stagingadd"), new HashMap<>());

        Utils.writeObject(new File(".gitlet/head"), changeID);

        Utils.writeObject(new File(".gitlet/current"), name);
    }

    /** Creates a new branch.
     *
     * @param branchName    Creates a new brnach with the given branch name
     */
    @SuppressWarnings("unchecked")
    public void branch(String branchName) {
        _branches = Utils.readObject
                (new File(".gitlet/branches"), ArrayList.class);
        if (_branches.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        } else {
            _branches.add(branchName);
            _head = Utils.readObject(new File(".gitlet/head"), String.class);
            Utils.writeObject(new File(".gitlet/branch/" + branchName), _head);
            Utils.writeObject(new File(".gitlet/branches"), _branches);
        }
    }

    /** Removes a branch from the repo.
     *
     * @param branchName    The branch name that we want to remove
     */
    @SuppressWarnings("unchecked")
    public void rmBranch(String branchName) {
        _currBranch = Utils.readObject
                (new File(".gitlet/current"), String.class);
        _branches = Utils.readObject
                (new File(".gitlet/branches"), ArrayList.class);
        if (!_branches.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (_currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else {
            File temp = new File(".gitlet/branch/" + branchName);
            temp.delete();
            _branches.remove(branchName);
            Utils.writeObject(new File(".gitlet/branches"), _branches);
        }
    }

    /** Resets the a certain commit given its id and sets it to the head.
     *
     * @param id    The commit id of the a specific commit object
     */
    public void reset(String id) {
        _currBranch = Utils.readObject
                (new File(".gitlet/current"), String.class);
        File recent = null;
        String[] allCommit = new File(".gitlet/commit").list();
        for (String commit: allCommit) {
            if (commit.contains(id)) {
                recent = new File(".gitlet/commit/" + commit);
            }
        }
        if (recent == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit changeCommit = Utils.readObject
                (new File(".gitlet/commit/" + id), Commit.class);

        String currenthead = Utils.readObject
                (new File(".gitlet/head"), String.class);
        Commit currentCommit = Utils.readObject
                (new File(".gitlet/commit/" + currenthead), Commit.class);

        for (String commit: Utils.plainFilenamesIn
                (System.getProperty("user.dir"))) {
            if (!currentCommit._staged.containsKey(commit)
                    && changeCommit._staged.containsKey(commit)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        for (String file: changeCommit._staged.keySet()) {
            if (changeCommit._staged.get(file).equals(id)) {
                checkoutCommitID(id, file);
            }
        }
        for (String file: currentCommit._staged.keySet()) {
            if (!changeCommit._staged.containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }

        Utils.writeObject(new File(".gitlet/stagingadd"), new HashMap<>());

        Utils.writeObject(new File(".gitlet/head"), id);
        Utils.writeObject(new File(".gitlet/branch/" + _currBranch), id);
    }

    /** Helper method for merge, it gives the error messages
     * of method.
     *
     * @param branchName    Name of the branch we're trying to merge
     *
     */
    @SuppressWarnings("unchecked")
    public void merge(String branchName) throws IOException {
        _currBranch = Utils.readObject
                (new File(".gitlet/current"), String.class);
        _branches = Utils.readObject
                (new File(".gitlet/branches"), ArrayList.class);
        _stagingR = Utils.readObject
                (new File(".gitlet/stagingremoval"), ArrayList.class);
        _stagingAdd = Utils.readObject
                (new File(".gitlet/stagingadd"), HashMap.class);
        if (!_branches.contains(branchName)) {
            System.out.println("A branch with that name does not exist. ");
            return;
        } else if (!_stagingAdd.isEmpty() || !_stagingR.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        String changeID = Utils.readObject
                (new File(".gitlet/branch/" + branchName), String.class);
        Commit changeCommit = Utils.readObject
                (new File(".gitlet/commit/" + changeID), Commit.class);
        String currenthead = Utils.readObject
                (new File(".gitlet/head"), String.class);
        Commit currentCommit = Utils.readObject
                (new File(".gitlet/commit/" + currenthead), Commit.class);
        String holder = currenthead;
        while (holder != null) {
            Commit beforeCommit = Utils.readObject
                    (new File(".gitlet/commit/" + holder), Commit.class);
            if (branchName.equals(_currBranch)) {
                System.out.println("Cannot merge a branch with itself.");
                return;
            } else if (holder.equals(changeID)) {
                System.out.println("Given branch is an ancestor "
                        + "of the current branch.");
                return;
            }
            holder = beforeCommit._parent;
        }
        holder = changeID;
        while (holder != null) {
            Commit beforeCommit = Utils.readObject
                    (new File(".gitlet/commit/" + holder), Commit.class);
            if (holder.equals(currenthead)) {
                checkoutBranch(branchName);
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            holder = beforeCommit._parent;
        }
        for (String commit: Utils.plainFilenamesIn
                (System.getProperty("user.dir"))) {
            if (!currentCommit._staged.containsKey(commit)
                    && changeCommit._staged.containsKey(commit)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        merge(currenthead, changeID, changeCommit, currentCommit, branchName);
    }


    /** Merges the files within the current branch
     * and the given branch into one.
     * @param current the sha1 of the current commit
     * @param branch the name of the branch we're merging
     * @param other     the sha1 of the other commit
     * @param otherCommit commit from the other branch
     * @param currCommit commit from the current branch
     *
     */
    public void merge(String current, String other, Commit otherCommit,
                      Commit currCommit, String branch) throws IOException {
        Commit splitPoint = splitpoint(current, other);
        _currBranch = Utils.readObject
                (new File(".gitlet/current"), String.class);

        for (String name: splitPoint._staged.keySet()) {
            if (otherCommit._staged.containsKey(name)
                    && currCommit._staged.containsKey(name)) {
                String point = splitPoint._staged.get(name);
                if (!otherCommit._staged.containsKey(name)
                        && !currCommit._staged.containsKey(name)) {
                    continue;
                }
                String change = otherCommit._staged.get(name);
                String curr = currCommit._staged.get(name);
                if (!point.equals(change) && point.equals(curr)) {
                    checkoutCommitID(other, name);
                    add(name);
                }
            }
        }

        for (String name: otherCommit._staged.keySet()) {
            if (!splitPoint._staged.containsKey(name)
                    && !currCommit._staged.containsKey(name)) {
                checkoutCommitID(other, name);
                add(name);
            }
        }
        for (String name: splitPoint._staged.keySet()) {
            String point = splitPoint._staged.get(name);
            if (currCommit._staged.containsKey(name)) {
                String curr = currCommit._staged.get(name);
                if (point.equals(curr)
                        && !otherCommit._staged.containsKey(name)) {
                    rm(name);
                }
            }
        }
        mergeConflict(otherCommit, currCommit, splitPoint);
        commit("Merged " + branch + " into "
                + _currBranch + ".", other);
    }


    /** Helper method to help find the split point between current branch
     * and the other branch.
     * @param curr   The current commit we are one
     * @param other    The other commit that we're trying to find
     *                  the splitpoint of
     * @return  The commit that is the latest common ancestor between
     *          curent and other commit
     */
    private Commit splitpoint(String curr, String other) {
        Commit result = null;
        HashMap<String, Integer> currCommit
                = currMerge(curr, new HashMap<>(), 0);
        ArrayList<String> otherCommit = otherMerge(other, new ArrayList<>());
        HashMap<String, Integer> newCommit = new HashMap<>();
        for (String commit: currCommit.keySet()) {
            if (otherCommit.contains(commit)) {
                newCommit.put(commit, currCommit.get(commit));
            }
        }
        int dist = Collections.min(newCommit.values());
        for (String commit: newCommit.keySet()) {
            if (newCommit.get(commit) == dist) {
                result = Utils.readObject
                        (new File(".gitlet/commit/" + commit), Commit.class);
            }
        }
        return result;
    }


    /** Helps the current sha1 to merge.
     *
     * @param curr sha1 of current commit
     * @param record   the hashmap that records each commit with its distance
     * @param depth the depth of this commit
     * @return  the hashmap to show distance of each commit
     */
    private static HashMap<String, Integer> currMerge(
            String curr, HashMap<String, Integer> record, int depth) {
        if (curr == null) {
            return record;
        } else {
            Commit check = Utils.readObject
                    (new File(".gitlet/commit/" + curr), Commit.class);
            if (!record.containsKey(curr)) {
                record.put(curr, depth);
            }
            HashMap<String, Integer> record1 =
                    currMerge(check._parent, record, depth + 1);
            HashMap<String, Integer> record2 =
                    currMerge(check._parent2, record, depth + 1);
            for (String commit: record2.keySet()) {
                record1.put(commit, record2.get(commit));
            }
            return record1;
        }
    }

    /** Helps the other sha1 to merge.
     *
     * @param other the sha1 of other commit
     * @param arr keeps track of other commit
     * @return  returns the list that keeps track
     */
    private ArrayList<String> otherMerge(
            String other, ArrayList<String> arr) {
        if (other == null) {
            return arr;
        } else {
            Commit check = Utils.readObject
                    (new File(".gitlet/commit/" + other), Commit.class);
            arr.add(other);
            ArrayList<String> arr1 =
                    otherMerge(check._parent, arr);
            ArrayList<String> arr2 =
                    otherMerge(check._parent2, arr);
            arr1.addAll(arr2);
            return arr1;
        }
    }


    /** Helps find and sort out any errors that
     *  occur when merging two branches together.
     * @param otherCommit the other commit
     * @param currCommit the current commit
     * @param splitPoint the commit of the splitpoint between
     *                  current and other
     */

    private void mergeConflict(Commit otherCommit, Commit currCommit,
                               Commit splitPoint) throws IOException {
        boolean mergedConflict = false;
        for (String name: otherCommit._staged.keySet()) {
            File overwrite = new File(name);
            String other = otherCommit._staged.get(name);
            String othersha = Utils.readContentsAsString
                    (new File(".gitlet/blobs/" + other));
            if (splitPoint._staged.containsKey(name)
                    && !currCommit._staged.containsKey(name)) {
                String overWriting = "<<<<<<< HEAD\n=======\n"
                        + othersha + ">>>>>>>\n";
                if (overwrite.exists()) {
                    Utils.writeContents(overwrite, overWriting);
                    add(name);
                    mergedConflict = true;
                }
            } else if (currCommit._staged.containsKey(name)) {
                String curr = currCommit._staged.get(name);
                if (!other.equals(curr)) {
                    String writeCur = Utils.readContentsAsString
                            (new File(".gitlet/blobs/" + curr));
                    String overWriting = "<<<<<<< HEAD\n" + writeCur
                            + "=======\n" + othersha + ">>>>>>>\n";
                    if (overwrite.exists()) {
                        Utils.writeContents(overwrite, overWriting);
                        add(name);
                        mergedConflict = true;
                    }

                }
            }
        }
        for (String name: currCommit._staged.keySet()) {
            File overwrite = new File(name);
            if (splitPoint._staged.containsKey(name)
                    && !otherCommit._staged.containsKey(name)) {
                String curr = currCommit._staged.get(name);
                String currsha = Utils.readContentsAsString
                        (new File(".gitlet/blobs/" + curr));
                String oversha = "<<<<<<< HEAD\n" + currsha
                        + "=======\n>>>>>>>\n";
                if (overwrite.exists()) {
                    Utils.writeContents(overwrite, oversha);
                    add(name);
                    mergedConflict = true;
                }
            }
        }
        if (mergedConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Create the gitlet repo.
     *
     */
    private File _repo = new File(".gitlet");

    /** Creates blobs with info and their sha1.
     *
     */
    private File _blobs = new File(".gitlet/blobs");

    /** Create a dir of File with commit and their sha1.
     *
     */
    private File _commit = new File(".gitlet/commit");

    /** Creates a dir of File with the title as the
     *  different branches and the content as the head commit's sha1.
     */
    private File _branch = new File(".gitlet/branch");

    /** All the branches.
     *
     */
    @SuppressWarnings("unchecked")
    private ArrayList<String> _branches = new ArrayList();

    /** The current branch.
     *
     */
    private String _currBranch = "master";

    /** Names of files to its sha1 that's to be added.
     *
     */
    private HashMap<String, String> _stagingAdd = new HashMap<>();

    /** Names of files to be deleted.
     *
     */
    private ArrayList<String> _stagingR = new ArrayList<>();

    /** Reference to the SHA1 of the most recent commit.
     */
    private String _head;
}
