package kangwon.cs.capstone;

import android.widget.ImageButton;


public class Card {
    private final static int backImageID = R.drawable.cardback;
    private final static int[] frontImageID = {R.drawable.image1, R.drawable.image2,
            R.drawable.image3, R.drawable.image4,
            R.drawable.image5, R.drawable.image6,
            R.drawable.image7, R.drawable.image8 };

    int value;
    boolean isBack;
    ImageButton card;

    Card(int value) {
        this.value = value;
    }//int 8을 card의 value에 저장

    public void onBack() { // 카드 뒷면이 보이게 뒤집음
        if (!isBack) {
            card.setBackgroundResource(backImageID);
            isBack = true;
        }
    }

    public void flip() { // 카드를 뒤집음
        if (!isBack) {
            card.setBackgroundResource(backImageID);
            isBack = true;
        }
        else {
            card.setBackgroundResource(frontImageID[value]);
            isBack = false;
        }
    }


    public void onFront() { // 카드 그림면을 보여줌
        if (isBack) {
            card.setBackgroundResource(frontImageID[value]);
            isBack = false;
        }
    }
}
