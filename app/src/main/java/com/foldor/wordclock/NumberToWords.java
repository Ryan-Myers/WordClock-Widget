package com.foldor.wordclock;

/**
 * Quick and dirty class to convert an integer into the English equivalent. Works for 0-69.
 * Use: NumberToWords.get(int)
 *
 */
class NumberToWords {
    private static final String[] ONES = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine" };
    private static final String[] TEENS = { "Ten", "Eleven", "Twelve", "Thirteen", null, "Fifteen", null, null, "Eighteen", null };
    private static final String[] TENS = {null, null, "Twenty", "Thirty", "Forty", "Fifty", "Sixty"};

    private static String numberToWords(int number) {
        if (number < 10) {
            return ONES[number];
        } 
        else if (number < 20) {
            int n = number - 10;
            String words = TEENS[n];

            return words == null ? ONES[n] + "teen" : TEENS[n];
        } 
        else {
            int n = number % 10;
            return TENS[number / 10] +
            (n == 0 ? "" : ("-" + numberToWords(n)));
        }
    }
    
    /**
     * Pass an integer from 0-69 and get the English representation returned.
     */
    public static String get(int Num) {
        return numberToWords(Num);
    }
}