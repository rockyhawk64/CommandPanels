package me.rockyhawk.commandpanels.builder.logic;

import java.util.List;

public class ConditionParser {

    private List<Token> tokens;
    private int index;

    public ConditionNode parse(String input) {
        ConditionParser parser = new ConditionParser();
        parser.tokens = Tokenizer.tokenize(input);
        parser.index = 0;
        return parser.parseOr(); // Start with OR (lowest precedence)
    }

    // OR is the lowest precedence
    private ConditionNode parseOr() {
        ConditionNode left = parseAnd();

        while (match("$OR")) {
            ConditionNode right = parseAnd();
            left = new LogicalNode("$OR", List.of(left, right));
        }

        return left;
    }

    // AND binds tighter than OR
    private ConditionNode parseAnd() {
        ConditionNode left = parsePrimary();

        while (match("$AND")) {
            ConditionNode right = parsePrimary();
            left = new LogicalNode("$AND", List.of(left, right));
        }

        return left;
    }

    // Primary = (group) or comparison
    private ConditionNode parsePrimary() {
        if (match("$NOT")) {
            // Wrap the next primary node in a NotNode
            ConditionNode node = parsePrimary();
            return new NotNode(node);
        }

        if (match("(")) {
            ConditionNode node = parseOr();  // recurse into expression
            expect(")");
            return node;
        }

        // Otherwise parse a comparison like: <left> <op> <right>
        String left = nextToken().value;
        String operator = nextToken().value;
        String right = nextToken().value;

        return new ComparisonNode(left, operator, right);
    }

    private boolean match(String expected) {
        if (index < tokens.size() && tokens.get(index).value.equalsIgnoreCase(expected)) {
            index++;
            return true;
        }
        return false;
    }

    private void expect(String expected) {
        if (!match(expected)) {
            throw new IllegalArgumentException("Expected token: " + expected);
        }
    }

    private Token nextToken() {
        if (index >= tokens.size()) {
            throw new IllegalStateException("Unexpected end of tokens");
        }
        return tokens.get(index++);
    }
}

