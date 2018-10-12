package blackjack;

public class Computer extends Dealer{
    public Computer(String name, String tag, int money) {
        super(name, tag, money);
    }

    @Override
    public boolean start(int max) {
        super.start(max);
        Hand hand = hands.get(FIRST_INDEX);
        while (hand.computeValue() < max) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {}
            hit(hand, this);
        }

        // returns false to not go through input loop
        return false;
    }
}