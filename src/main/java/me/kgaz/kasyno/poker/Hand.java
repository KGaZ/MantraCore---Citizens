package me.kgaz.kasyno.poker;

import java.util.ArrayList;
import java.util.List;

public class Hand {

    private List<Card> cards;
    private List<Card> wspolne;

    public Hand() {

        cards = new ArrayList<>();
        wspolne = new ArrayList<>();

    }

    public void addCard(Card card) {

        cards.add(card);

    }

    public void addWspolne(Card card) {

        wspolne.add(card);

    }

    public List<Card> getCards() {

        List<Card> temp = new ArrayList<>(cards);
        temp.addAll(this.wspolne);
        return temp;

    }

    public int evaluate() {

        List<Card> all = new ArrayList<>();
        all.addAll(cards);
        all.addAll(wspolne);

        int base = getHighestCard(cards).getValue();

        if(evaluateStraightFlush(all) > 0) return base+evaluateStraightFlush(all);
        if(evaluateFourOfAKind(all) > 0) return base+evaluateFourOfAKind(all);
        if(evaluateFullHouse(all) > 0) return base+evaluateFullHouse(all);
        if(evaluateFlush(all) > 0) return base+evaluateFlush(all);
        if(evaluateStraight(all) > 0) return base+evaluateStraight(all);
        if(evaluateThreeOfAKind(all) > 0) return base+evaluateThreeOfAKind(all);
        if(evaluatePairs(all) > 0) return base+evaluatePairs(all);

        return base;

    }

    public int evaluateStraightFlush(List<Card> cards) {

        if(evaluateStraight(cards) > 0) {

            List<Card> straight = getStraight(cards);
            if(evaluateFlush(straight) > 0) {

                //STRAIGHT FLUSH
                if(getHighestCard(straight).getNumber() == Card.Value.ACE) {

                    //ROYAL FLUSH
                    return 2_000_000 + evaluateFlush(straight);

                } else {

                    return 1_000_000 + (1000*getHighestCard(straight).getValue());

                }

            } else return 0;

        } else return 0;

    }

    public int evaluateFourOfAKind(List<Card> cards){

        List<Card> all = new ArrayList<>(cards);

        for(Card card : all) {

            int amount = 0;

            for(Card card2 : all) {

                if(card2.getNumber() == card.getNumber()) amount++;


            }

            if(amount >= 4) return 600_000 + (card.getNumber().getValue()*1000);

        }

        return 0;

    }

    public int evaluateFullHouse(List<Card> cards) {

        if(evaluateThreeOfAKind(cards) > 0 && evaluatePairs(cards) > 0) {
            return evaluateThreeOfAKind(cards)+200_000;
        }

        return 0;

    }

    public int evaluatePairs(List<Card> cards){

        List<Card> all = new ArrayList<>(cards);

        List<Card.Value> pairs = new ArrayList<>();

        for(Card card : all) {

            if(pairs.contains(card.getNumber())) continue;

            for(Card c1 : all) {

                if(c1.getNumber() == card.getNumber() && c1 != card) {

                    pairs.add(c1.getNumber());

                }

            }

        }

        if(pairs.size() == 1) {

            return 100_000 + (pairs.get(0).getValue()*1000);

        } else if (pairs.size() >= 2) {

            int higher = Math.max(pairs.get(0).getValue(), pairs.get(1).getValue());

            if(pairs.size() >= 3) higher = Math.max(pairs.get(0).getValue(), Math.max(pairs.get(1).getValue(), pairs.get(2).getValue()));

            return 200_000 + (higher*1000);

        }

        return 0;

    }

    public int evaluateThreeOfAKind(List<Card> cards){

        List<Card> all = new ArrayList<>(cards);

        for(Card card : all) {

            int amount = 0;

            for(Card card2 : all) {

                if(card2.getNumber() == card.getNumber()) amount++;


            }

            if(amount >= 3) return 300_000 + (card.getNumber().getValue()*1000);

        }

        return 0;

    }

    private List<Card> getStraight(List<Card> cards) {

        List<Card> all = new ArrayList<>(cards);

        for(Card card : all) {

            int num = 0;
            Card highest = null;

            int val = card.getNumber().getValue();

            if(card.getNumber() == Card.Value.ACE) val = 1;

            List<Card> straight = new ArrayList<>();

            for(int i = 0; i < 7; i++) {

                if(val+i > 14) break;

                if(hasMatchingCard(all, Card.Value.fromNumber(val+i))) {

                    straight.addAll(getMatchingCard(all, Card.Value.fromNumber(val+i)));

                    num++;

                } else break;

            }

            if(num >= 5) return straight;

        }

        return new ArrayList<>();

    }

    private int evaluateFlush(List<Card> cards) {

        List<Card> all = new ArrayList<>(cards);

        for(Card card : all) {

            int amount = 0;
            List<Card> flush = new ArrayList<>();

            for(Card card2 : all) {

                if(card.getSuits() == card2.getSuits()) {
                    amount++;
                    flush.add(card2);
                }

            }

            if(amount >= 5) {

                return 500_000 + (getHighestCard(flush).getValue()*1000);

            }

        }

        return 0;

    }

    private int evaluateStraight(List<Card> cards) {

        List<Card> all = new ArrayList<>(cards);

        for(Card card : all) {

            int num = 0;
            Card highest = null;

            int val = card.getNumber().getValue();

            if(card.getNumber() == Card.Value.ACE) val = 1;

            for(int i = 0; i < 7; i++) {

                if(val+i > 14) break;

                if(hasMatchingCard(all, Card.Value.fromNumber(val+i))) {

                    num++;

                } else break;

            }

            if(num >= 5) return 400_000 + (1000*(val+num));

        }

        return 0;

    }

    public Card getHighestCard(List<Card> cards) {

        int max = 0;
        Card card = null;

        for(Card c : cards) {

            if(c.getValue() > max) {

                max = c.getValue();
                card = c;

            }

        }

        return card;

    }

    private boolean hasCard(List<Card> cards, Card card) {

        return cards.contains(card);

    }

    private List<Card> getMatchingCard(List<Card> cards, Card.Value value) {

        List<Card> retu = new ArrayList<>();

        for(Card card : cards) {

            if(card.getNumber() == value) {

                retu.add(card);

            }

        }

        return retu;

    }

    private boolean hasMatchingCard(List<Card> cards, Card.Value value) {

        for(Card card : cards) {

            if(card.getNumber() == value) {

                return true;

            }

        }

        return false;

    }

    private List<Card> getMatchingCard(List<Card> cards, Card.Suits value) {

        List<Card> retu = new ArrayList<>();

        for(Card card : cards) {

            if(card.getSuits() == value) {

                retu.add(card);

            }

        }

        return retu;

    }

    private boolean hasMatchingCard(List<Card> cards, Card.Suits suits) {

        for(Card card : cards) {

            if(card.getSuits() == suits) {

                return true;

            }

        }

        return false;

    }

    public void addWspolne(List<Card> cards) {

        this.wspolne.addAll(cards);

    }
}
