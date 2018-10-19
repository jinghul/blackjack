package blackjack;

import java.util.ArrayList;

public class Dealer extends CardPlayer {
    public static final int SLEEP_TIME = 500;
    public static final int FACE_UP = 0;
    public static final int FACE_DOWN = 1;

    public Deck deck;
    private String tag;
    private ArrayList<Card> discards;

    public Dealer(String name) {
        super(name);
        deck = new Deck();
    }

    public Dealer(String name, String tag, int money) {
        this("(" + tag + ") " + name);
        this.money = money;
        this.tag = "(" + tag + ") ";
        discards = new ArrayList<Card>();
    }

    public String name() {
        return name.substring(tag.length());
    }

    public void dealCard(CardPlayer player, Hand hand) {
        checkDeck();
        System.out.printf("%s gives one card to %s.\n", name, player.name);
        hand.add(deck.getCard());
        System.out.printf("%s: %s\n", player.name, player.getHandsString());
    }

    public void dealCard(CardPlayer player, Hand hand, int visible) {
        checkDeck();
        Card card = deck.getCard();
        if (visible == FACE_DOWN) {
            card.setFaceDown();
            System.out.printf("%s gives one face-down card to %s.\n", name, player.name);
        } else {
            System.out.printf("%s gives one face-up card to %s.\n", name, player.name);
        }

        hand.add(card);
        System.out.printf("%s: %s\n", player.name, player.getHandsString());
    }

    private void checkDeck() {
        if (deck.size() == 0) {
            deck.addDiscards(discards);
            discards.clear();
        }
    }

    public void hit(Hand hand, CardPlayer player) {
        System.out.printf("%s Hits! --- ", player.name);
        dealCard(player, hand);
    }

    public Hand getHand() {
        return hands.get(FIRST_INDEX);
    }

    public boolean done() {
        return getHand().done();
    }

    public boolean start(int max) {
        // human player
        System.out.printf("=== %s turn ===\n\n", name);
        Hand hand = hands.get(FIRST_INDEX);
        if (hand.size() > 1) {
            hand.get(SECOND_INDEX).setFaceUp();
        }

        // returns true to go to input loop
        return true;
    }

    // for 31, first check if empty then shuffle
    // if not empty then discard hand from previous round
    public void checkAndShuffle(ArrayList<Player> players) {
        if (deck.needsShuffle) {
            System.out.printf("%s: Shuffling cards into deck...\n", name);
            deck.shuffle();
            discards.clear();
        } else {
            if (hands.size() > 0) {
                discards.addAll(getHand().getAll());
                discardAll(players);
            }
        }
    }

    public void shuffle() {
        deck.shuffle();
    }

    public void discardAll(ArrayList<Player> players) {
        for (Player player : players) {
            for (Hand hand : player.getHands()) {
                discards.addAll(hand.getAll());
            }
        }
    }
}