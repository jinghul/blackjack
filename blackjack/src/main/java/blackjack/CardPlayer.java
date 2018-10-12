package blackjack;

import java.util.ArrayList;

public abstract class CardPlayer {
    public static final int FIRST_INDEX = 0;
    public static final int SECOND_INDEX = 1;

    String name;
    public int money;
    public int bet;
    public boolean fold;
    public ArrayList<Hand> hands;

    public CardPlayer(String name) {
        this.name = name;
        hands = new ArrayList<Hand>();
    }

    public void newRound() {
        fold = false;
        for (Hand hand : hands) {
            for (Card c : hand.getAll()) {
                c.setVisible(true);
            }
        }
        hands.clear();
        hands.add(new Hand());
    };
    
    public ArrayList<Hand> getHands() {
        return hands;
    }

    public Hand getHand(int index) {
        return hands.get(index);
    }

    public String getHandsString() {
        String ret = "";
        for (Hand hand : hands) {
            ret += hand.toString() + " ";
        }
        return ret + "\n";
    }

    public String peekCards() {
        String ret = "";
        for (Hand hand : hands) {
            ret += hand.peek() + " ";
        }
        return ret + "\n";
    }

    public void loseBet(CardPlayer cp) {
        money -= bet;
        cp.money += bet;
    }

    public void winBet(CardPlayer cp) {
        cp.money -= bet;
        money += bet;
    }

    public boolean bust() {
        boolean allBust = true;
        for (Hand hand : hands) {
            allBust = hand.bust() && allBust;
        }
        return allBust;
    }

    public boolean broke() {
        return money <= 0;
    }

    public void stand(Hand hand) {
        hand.stand();
    }
}