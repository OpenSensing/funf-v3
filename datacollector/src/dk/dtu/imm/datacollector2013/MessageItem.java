package dk.dtu.imm.datacollector2013;

public class MessageItem {

    String title;
    long timestamp;
    String message;
    String url;
    boolean collapsed = true;

    public MessageItem(String title, long timestamp, String message, String url) {
        this.title = title;
        this.timestamp = timestamp;
        this.message = message;
        this.url = url;
    }

}
