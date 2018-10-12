package blackjack;
import java.util.Scanner;

public class Player extends CardPlayer {

    public Player(String name, int money) {
        super(name);
        this.money = money;
        this.name = name;
    }

    public void start(Scanner sc) {
        System.out.printf("=== %s turn ===\n\n", name);
        System.out.printf("%s cards: %s\n", name, hands.get(FIRST_INDEX).toString());
        System.out.printf("%s currently has $%d.\n\nHow much would you like to bet?\n!!! Bet '0' or 'fold' to fold.\n!!! Input `p` or `peek` to see your cards.\n\n",name, money);
        bet = -1;
        do {
            System.out.printf("Please enter a number less than or equal to $%d.\n--- $", money);
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("fold") || input.equals("0")) {
                fold = true;
                System.out.println("");
                return;
            } else if (input.equalsIgnoreCase("p") || input.equalsIgnoreCase("peek")) {
                System.out.printf("\n" + name + ": " + peekCards() + "\n");
                continue;
            }

            try {
                bet = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                bet = -1;
            }
        } while (bet > money || bet < 0);
        System.out.println("");
    }

    public void hit(Hand hand, Dealer dealer) {
        dealer.hit(hand, this);
    }

    public boolean split(Hand hand, Dealer dealer) {
        if (hand.size() > 2) {
            System.out.printf("More than two cards in hand %s", hand.toString());
            return false;
        } else if (!hand.get(FIRST_INDEX).equalValue(hand.get(SECOND_INDEX))) {
            System.out.printf("Cards not the same value in hand %s", hand.toString());
            return false;
        } else if (bet * (hands.size() + 1) > money) {
            System.out.printf("Not enough money! Need $%d only $%d left", bet, money);
            return false;
        }
        
        hands.remove(hand);

        System.out.printf("Split hand of %s\n", hand.toString());
        Hand clone = new Hand();
        clone.add(hand.remove(FIRST_INDEX));

        // add new hands to player hands
        hands.add(hand);
        hands.add(clone);

        /* Hit both hands with a new card */
        dealer.dealCard(this, hand);
        dealer.dealCard(this, clone);
        return true;
    }

    public boolean doubleUp(Hand hand, Dealer dealer) {
        if (bet * (hands.size() + 1) > money) {
            System.out.printf("Not enough money! Need $%d only $%d left", bet, money);
            return false;
        } else {
            bet *= 2;
            dealer.hit(hand, this);
            hand.stand();
            return true;
        }
    }

    public boolean done() {
        if (fold || broke()) {
            return true;
        }

        boolean done = true;
        for (Hand hand : hands) {
            done = done && hand.done();
        } 
        return done;
    }
}