package kangwon.cs.capstone;

public class Global {

    private String other, user;

    public String getGlobal() {
        return other;
    }

    public void setGlobal(String s) {
        this.other = s;
    }

    public String getGlobal_u() {
        return user;
    }

    public void setGlobal_u(String s) {
        this.user = s;
    }

    private static Global instance = null;

    public static synchronized Global getInstance() {
        if (null == instance) {
            instance = new Global();
        }
        return instance;
    }
}