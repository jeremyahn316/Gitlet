package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** the Commit class that holds all the information about an commit.
 * @author Jeremy Ahn
 */

public class Commit implements Serializable {

    /** Commit takes in the message, the parent,
     * and an Hashmap of all files that this commit is tracking.
     *
     * @param m   The inputted string that is assoicted with this commit.
     * @param p   The parent commit of this commit.
     * @param p2   The second parent commit of this commit in the case of merge.
     * @param i     The hashmap that tracks the file in this commit.
     */

    public Commit(String m, String p, String p2, HashMap<String, String> i) {
        _message = m;
        _parent = p;
        _parent2 = p2;
        if (_parent == null) {
            _timestamp = "Thu Jan 1 00:00:00 1970 -0800";
            _staged = new HashMap<>();
        } else {
            _timestamp = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z").
                    format(new Date());
            _staged = i;
        }
        S = Utils.sha1(Utils.serialize(this));
    }

    /** The message associated with this commit.
     *
      */
    protected String _message;

    /** The timestamp associated with this commit.
     *
     */
    protected String _timestamp;

    /** The parent associated with this commit.
     *
     */
    protected String _parent;

    /** The second parent associated with this commit in
     * the case of a merge.
     */
    protected String _parent2;

    /** The SHA1 associated with this commit.
     *
     */
    protected String S;

    /** The Files tracked by this commit.
     *
     */
    protected HashMap<String, String> _staged;

}
