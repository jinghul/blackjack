package blackjack;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    public static String[] suits = {"D", "C", "H", "S"}; // diamonds, clubs, hearts, spades
    public static String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    public static ArrayList<Card> fullDeck;
    private ArrayList<Card> cards;
    public boolean needsShuffle;

    static {
        fullDeck = new ArrayList<Card>();
        for (String s : suits) {
            for (String val : values) {
                fullDeck.add(new Card(val, s));
            }
        }
    }

    public Deck() {
        for (Card c : fullDeck) {
            c.setVisible(true);
        }
        cards = new ArrayList<Card>();
        shuffle();
    }

    public static ArrayList<Card> getFullDeck() {
        return fullDeck;
    }

    public int size() {
        return cards.size();
    }

    public void shuffle() {
        cards.clear();
        cards.addAll(getFullDeck());
        Collections.shuffle(cards);
        needsShuffle = false;
    }

    public void addDiscards(ArrayList<Card> discards) {
        cards.addAll(discards);
        Collections.shuffle(cards);
    }
    
    public Card getCard() {
        if (cards.size() == 1) {
            needsShuffle = true;
        }
        return cards.remove(0);
    }
}