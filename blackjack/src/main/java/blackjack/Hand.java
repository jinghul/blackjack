package blackjack;

import java.util.ArrayList;

public class Hand implements Comparable<Hand> {
    public static int MAX_VAL;
    private ArrayList<Card> cards;

    public boolean bust;
    public boolean stand;

    public Hand() {
        cards = new ArrayList<Card>();
    }

    public Hand(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public static void setMax(int max) {
        MAX_VAL = max;
    }

    public void add(Card card) {
        cards.add(card);
    }

    /* Acts as ArrayList of Cards */
    public int size() {
        return cards.size();
    }

    public Card get(int index) {
        return cards.get(index);
    }

    public Card remove(int index) {
        return cards.remove(index);
    }

    public ArrayList<Card> getAll() {
        return cards;
    }
    /* ArrayList methods end */

    public int computeValue() {
        boolean ace = false;
        int sum = 0;
        for (Card c : cards) {
            if (c.isAce()) {
                ace = true;
                continue;
            }
            sum += c.getValue();
        }

        if (ace) {
            if (sum > (MAX_VAL-11)) {
                sum += 1;
            } else {
                sum += 11;
            }
        }
        
        return sum;
    }

    public void stand() {
        stand = true;
    }

    public boolean bust() {
        return bust;
    }

    public boolean done() {
        return bust || stand;
    }

    public int compareTo(Hand other) {
        int otherVal = other.computeValue();
        int thisVal = computeValue();
        if (thisVal == otherVal) {
            return 0;
        } else if (thisVal > otherVal) {
            return 1;
        } else {
            return -1;
        }
    }

    public String toString() {
        String ret = "[ ";
        for (Card c : cards) {
            ret += "[" + c.toString() + "] ";
        }
        return ret + "]";
    }

    public String peek() {
        String ret = "[ ";
        for (Card c : cards) {
            ret += "[" + c.peek() + "] ";
        }
        return ret + "]";
    }
}