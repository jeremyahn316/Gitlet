package gitlet;

import java.io.IOException;
import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jeremy Ahn
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String...args)
            throws IOException {
        Repo newRepo = new Repo();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init")) {
            newRepo.init();
        } else if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else if (args[0].equals("add")) {
            newRepo.add(args[1]);
        } else if (args[0].equals("commit")) {
            if (args.length == 1) {
                System.out.println("Please enter a commit message.");
            } else {
                newRepo.commit(args[1], null);
            }
        } else if (args[0].equals("rm")) {
            newRepo.rm(args[1]);
        } else if (args[0].equals("log")) {
            newRepo.log();
        } else if (args[0].equals("global-log")) {
            newRepo.globalLog();
        } else if (args[0].equals("find")) {
            newRepo.find(args[1]);
        } else if (args[0].equals("status")) {
            newRepo.status();
        } else if (args[0].equals("checkout")) {
            if (args.length == 2) {
                newRepo.checkoutBranch(args[1]);
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                } else {
                    newRepo.checkoutFileName(args[2]);
                }
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                } else {
                    newRepo.checkoutCommitID(args[1], args[3]);
                }
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("branch")) {
            newRepo.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            newRepo.rmBranch(args[1]);
        } else if (args[0].equals("reset")) {
            newRepo.reset(args[1]);
        } else if (args[0].equals("merge")) {
            newRepo.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
        }
    }
}
