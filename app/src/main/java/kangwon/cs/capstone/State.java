package kangwon.cs.capstone;

/**
 * Created by speak on 2018-05-16.
 */

public class State {
    //상태
    int stateHunger = 100;
    int stateHappiness = 100 ;
    int stateHealth = 100;
    int stateActive = 100;
    int stateStress = 10;
    int stateExperience = 0;

    //증감정도 -> 나중에 degree도 저장해놔(storeState, retoreState)
    int degreeHunger = 3;
    int degreeHappiness = 2;
    int degreeHealth = 1;
    int degreeActive = 1;
    int degreeStress = 1;

    //endtime

    long endTime =0;

    //getState
    public int getStateHunger(){return stateHunger;}
    public int getStateHappiness(){return stateHappiness;}
    public int getStateHealth(){return stateHealth;}
    public int getStateActive(){return stateActive;}
    public int getStateStress(){return stateStress;}
    public int getStateExperience(){return stateExperience;}

    //setState
    public void setStateHunger(int stateHunger){this.stateHunger = stateHunger;}
    public void setStateHappiness(int stateHappiness){this.stateHappiness = stateHappiness;}
    public void setStateHealth(int stateHealth){this.stateHealth = stateHealth;}
    public void setStateActive(int stateActive){this.stateActive = stateActive;}
    public void setStateStress(int stateStress){this.stateStress = stateStress;}
    public void setStateExperience(int stateExperience){this.stateExperience = stateExperience;}

    //getendTime
    public long getEndTime(){return endTime;}

    //setEndTime
    public void setEndTime(long endTime){this.endTime = endTime;}

    //getDegree
    public int getDegreeHunger(){return degreeHunger;}
    public int getDegreeHappiness(){return degreeHappiness;}
    public int getDegreeHealth(){return degreeHealth;}
    public int getDegreeActive(){return degreeActive;}
    public int getDegreeStress(){return degreeStress;}

    //setDegree
    public void setDegreeHunger(int degreeHunger){this.degreeHunger = degreeHunger;}
    public void setDegreeHappiness(int degreeHappiness){this.degreeHappiness = degreeHappiness;}
    public void setDegreeHealth(int degreeHealth){this.degreeHealth = degreeHealth;}
    public void setDegreeActive(int degreeActive){this.degreeActive = degreeActive;}
    public void setDegreeStress(int degreeStress){this.degreeStress = degreeStress;}

    private static State instance = null;

    public static synchronized State getInstance() {
        if (null == instance) {
            instance = new State();
        }
        return instance;
    }
}
