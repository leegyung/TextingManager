package Texting.managing;



// 연락처 데이터 저장용 클래스.
public class Contact {
    private String mName;
    private String mID;

    public Contact(String name, String Id) {
        mName = name;
        mID = Id;
    }

    public String getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public void setID(String mID) {
        this.mID = mID;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
}
