package blackjack;

public class Card {

    // Card properties
    private String value;
    private String suit;
    private boolean visible = true;

    public Card(String value, String suit) {
        this.suit = suit;
        this.value = value;
    }

    public int getValue() {
        if (isFace()) {
            return 10;
        } else if (isAce()) {
            System.out.println("Get value with ace!!! Not a good case!!");
            return -1;
        }
        return Integer.parseInt(value);
    }

    public String getSuit() {
        return suit;
    }

    public void setFaceDown() {
        visible = false;
    }

    public void setFaceUp() {
        if (!visible) {
            visible = true;
            System.out.printf("!!! [%s] revealed.\n\n", toString());
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isAce() {
        return value == "A";
    }

    public boolean isFace() {
        return (value == "K" || value == "Q" || value == "J");
    }

    public boolean equalValue(Card other) {
        return other.value.equals(value);
    }

    public String toString() {
        if (visible) {
            return value + suit;
        } else {
            return "??";
        }
    }

    public String peek() {
        return value + suit;
    }
}