package me.rockyhawk.commandpanels.builder.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    // Matches operators starting with $ (e.g., $AND, $EQUALS) or parentheses
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("\\$[A-Z]+\\b|\\(|\\)");

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = OPERATOR_PATTERN.matcher(input);
        int lastEnd = 0;

        while (matcher.find()) {
            // Text between the last operator and this operator is the argument
            String textBetween = input.substring(lastEnd, matcher.start()).trim();
            if (!textBetween.isEmpty()) {
                tokens.add(new Token(textBetween));
            }

            // Add the operator or parenthesis itself
            tokens.add(new Token(matcher.group()));
            lastEnd = matcher.end();
        }

        // Catch any remaining text at the very end of the string
        String remainingText = input.substring(lastEnd).trim();
        if (!remainingText.isEmpty()) {
            tokens.add(new Token(remainingText));
        }

        return tokens;
    }
}