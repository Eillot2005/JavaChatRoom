public class FriendRequest {
    private int requestId;
    private User sender;
    private User receiver;
    private String statue;

    public FriendRequest(int requestId,User sender, User receiver,String statue) {
        this.requestId = requestId;
        this.sender = sender;
        this.receiver = receiver;
        this.statue = statue;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void accept() {
        receiver.acceptFriendRequest(sender);
        sender.acceptFriendRequest(receiver);
    }

    public void reject() {
        receiver.removeFriendRequest(sender);
    }

    public int getRequestId() {
        return requestId;
    }

    public String getSenderName(){
        return sender.getUsername();
    }

    public Object getReceiverName() {
        return receiver.getUsername();
    }
}