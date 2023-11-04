package me.kgaz.kasyno.poker;

import me.kgaz.kasyno.CustomRenderer;
import org.bukkit.map.MapRenderer;

public enum Card {

    ACE_SPADES(Value.ACE, Suits.SPADES, "40-A.png"),
    TWO_SPADES(Value.TWO, Suits.SPADES, "40-2.png"),
    THREE_SPADES(Value.THREE, Suits.SPADES, "40-3.png"),
    FOUR_SPADES(Value.FOUR, Suits.SPADES, "40-4.png"),
    FIVE_SPADES(Value.FIVE, Suits.SPADES, "40-5.png"),
    SIX_SPADES(Value.SIX, Suits.SPADES, "40-6.png"),
    SEVEN_SPADES(Value.SEVEN, Suits.SPADES, "40-7.png"),
    EIGHT_SPADES(Value.EIGHT, Suits.SPADES, "40-8.png"),
    NINE_SPADES(Value.NINE, Suits.SPADES, "40-9.png"),
    TEN_SPADES(Value.TEN, Suits.SPADES, "40-10.png"),
    JACK_SPADES(Value.JACK, Suits.SPADES, "40-J.png"),
    QUEEN_SPADES(Value.QUEEN, Suits.SPADES, "40-Q.png"),
    KING_SPADES(Value.KING, Suits.SPADES, "40-K.png"),

    ACE_CLUBS(Value.ACE, Suits.CLUBS, "60-A.png"),
    TWO_CLUBS(Value.TWO, Suits.CLUBS, "60-2.png"),
    THREE_CLUBS(Value.THREE, Suits.CLUBS, "60-3.png"),
    FOUR_CLUBS(Value.FOUR, Suits.CLUBS, "60-4.png"),
    FIVE_CLUBS(Value.FIVE, Suits.CLUBS, "60-5.png"),
    SIX_CLUBS(Value.SIX, Suits.CLUBS, "60-6.png"),
    SEVEN_CLUBS(Value.SEVEN, Suits.CLUBS, "60-7.png"),
    EIGHT_CLUBS(Value.EIGHT, Suits.CLUBS, "60-8.png"),
    NINE_CLUBS(Value.NINE, Suits.CLUBS, "60-9.png"),
    TEN_CLUBS(Value.TEN, Suits.CLUBS, "60-10.png"),
    JACK_CLUBS(Value.JACK, Suits.CLUBS, "60-J.png"),
    QUEEN_CLUBS(Value.QUEEN, Suits.CLUBS, "60-Q.png"),
    KING_CLUBS(Value.KING, Suits.CLUBS, "60-K.png"),

    ACE_DIAMONDS(Value.ACE, Suits.DIAMONDS, "80-A.png"),
    TWO_DIAMONDS(Value.TWO, Suits.DIAMONDS, "80-2.png"),
    THREE_DIAMONDS(Value.THREE, Suits.DIAMONDS, "80-3.png"),
    FOUR_DIAMONDS(Value.FOUR, Suits.DIAMONDS, "80-4.png"),
    FIVE_DIAMONDS(Value.FIVE, Suits.DIAMONDS, "80-5.png"),
    SIX_DIAMONDS(Value.SIX, Suits.DIAMONDS, "80-6.png"),
    SEVEN_DIAMONDS(Value.SEVEN, Suits.DIAMONDS, "80-7.png"),
    EIGHT_DIAMONDS(Value.EIGHT, Suits.DIAMONDS, "80-8.png"),
    NINE_DIAMONDS(Value.NINE, Suits.DIAMONDS, "80-9.png"),
    TEN_DIAMONDS(Value.TEN, Suits.DIAMONDS, "80-10.png"),
    JACK_DIAMONDS(Value.JACK, Suits.DIAMONDS, "80-J.png"),
    QUEEN_DIAMONDS(Value.QUEEN, Suits.DIAMONDS, "80-Q.png"),
    KING_DIAMONDS(Value.KING, Suits.DIAMONDS, "80-K.png"),

    ACE_HEARTS(Value.ACE, Suits.HEARTS, "100-A.png"),
    TWO_HEARTS(Value.TWO, Suits.HEARTS, "100-2.png"),
    THREE_HEARTS(Value.THREE, Suits.HEARTS, "100-3.png"),
    FOUR_HEARTS(Value.FOUR, Suits.HEARTS, "100-4.png"),
    FIVE_HEARTS(Value.FIVE, Suits.HEARTS, "100-5.png"),
    SIX_HEARTS(Value.SIX, Suits.HEARTS, "100-6.png"),
    SEVEN_HEARTS(Value.SEVEN, Suits.HEARTS, "100-7.png"),
    EIGHT_HEARTS(Value.EIGHT, Suits.HEARTS, "100-8.png"),
    NINE_HEARTS(Value.NINE, Suits.HEARTS, "100-9.png"),
    TEN_HEARTS(Value.TEN, Suits.HEARTS, "100-10.png"),
    JACK_HEARTS(Value.JACK, Suits.HEARTS, "100-J.png"),
    QUEEN_HEARTS(Value.QUEEN, Suits.HEARTS, "100-Q.png"),
    KING_HEARTS(Value.KING, Suits.HEARTS, "100-K.png");

    private Value value;
    private Suits suits;
    private String cardView;
    private CustomRenderer renderer;

    private Card(Value value, Suits suits, String cardView) {

        this.value = value;
        this.suits = suits;
        this.cardView = cardView;
        this.renderer = new CustomRenderer(cardView);

    }

    public String getCardView() {

        return cardView;

    }

    public MapRenderer getRenderer() {

        return renderer;

    }

    public int getValue() {

        return 5*value.getValue() + suits.getValue();

    }

    public Value getNumber() {
        return value;
    }

    public Suits getSuits() {
        return suits;
    }

    public enum Value {

        ACE(14),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13);

        private int value;

        private Value(int value) {

            this.value = value;

        }

        public static Value fromNumber(int i) {

            if(i == 14) return ACE;

            return Value.values()[i-1];

        }

        public int getValue() {

            return value;

        }

    }

    public enum Suits {

        CLUBS(1),
        DIAMONDS(2),
        HEARTS(3),
        SPADES(4);

        private int value;

        private Suits(int value) {

            this.value = value;

        }

        public int getValue() {

            return value;

        }

        public static Suits getFromValue(int value) {

            return Suits.values()[value-1];

        }

    }

}
