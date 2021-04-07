package kangwon.cs.capstone;

public class Result {
    private String s_result;
    int s_result_to, medal = 0;

    public String getSave_result(){
        return s_result;
    }            //alone 기록 저장
    public int getSave_result_to(){
        return s_result_to;
    }        //together 기록 저장
    public int getSave_medal(){
        return medal;
    }                   //together 승자 medal 부여

    public void setSave_result(String s_result) {
        this.s_result = s_result;
    }
    public void setSave_result_to(int s_result_to) {
        this.s_result_to = s_result_to;
    }
    public void setSave_medal(int medal) {
        this.medal = medal;
    }

    private static Result instance = null;

    public static synchronized Result getInstance() {
        if (null == instance) {
            instance = new Result();
        }
        return instance;
    }
}
