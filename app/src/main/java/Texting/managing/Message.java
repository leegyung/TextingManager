package Texting.managing;

// 유저가 저장할 메세지 데이터 저장용 class.
public class Message {
    private String mName;
    private String mMessage;

    public Message(String name, String msg) {
        mName = name;
        mMessage = msg;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getTitle() {
        return mName;
    }

    public void setMessage(String msg) {
        this.mMessage = msg;
    }

    public void setTitle(String mName) {
        this.mName = mName;
    }
}
