package me.rockyhawk.commandpanels.builder.logic;

public class Token {
    public final String value;

    public Token(String value) {
        this.value = value;
    }

    public boolean isOperator() {
        return value.equals("$AND") || value.equals("$OR") ||
                value.equals("$EQUALS") || value.equals("$ISGREATER") || value.equals("$HASPERM");
    }

    public boolean isLogicalOperator() {
        return value.equals("$AND") || value.equals("$OR");
    }

    public boolean isLeftParen() {
        return value.equals("(");
    }

    public boolean isRightParen() {
        return value.equals(")");
    }

    @Override
    public String toString() {
        return value;
    }
}
