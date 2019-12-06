import java.util.ArrayList;
import java.util.HashMap;

public class ConnectedUsers {
    long userKey;
    long removeUserTimer;
    long retransmissionTimer;
    int sentSeqNumber;
    int receivedSeqNumber;
    long lastContacted;
    HashMap<Integer,String> sentMessageBuffer;
    HashMap<Integer,String> receivedMessageBuffer;

    public long getUserKey() {
        return userKey;
    }

    public void setUserKey(long userKey) {
        this.userKey = userKey;
    }

    public long getRemoveUserTimer() {
        return removeUserTimer;
    }

    public void setRemoveUserTimer(long removeUserTimer) {
        this.removeUserTimer = removeUserTimer;
    }

    public long getRetransmissionTimer() {
        return retransmissionTimer;
    }

    public void setRetransmissionTimer(long retransmissionTimer) {
        this.retransmissionTimer = retransmissionTimer;
    }

    public int getSentSeqNumber() {
        return sentSeqNumber;
    }

    public void setSentSeqNumber(int sentSeqNumber) {
        this.sentSeqNumber = sentSeqNumber;
    }

    public int getReceivedSeqNumber() {
        return receivedSeqNumber;
    }

    public void setReceivedSeqNumber(int receivedSeqNumber) {
        this.receivedSeqNumber = receivedSeqNumber;
    }

    public long getLastContacted() {
        return lastContacted;
    }

    public void setLastContacted(long lastContacted) {
        this.lastContacted = lastContacted;
    }

    ConnectedUsers(String key){
        userKey = Long.parseLong(key);
    }


}
