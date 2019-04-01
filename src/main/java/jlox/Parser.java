package jlox;

import java.util.List;
import java.util.function.Supplier;

import static jlox.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private Integer current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    /*
     *   HELPER METHODS
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /*
     *   NAVIGATION METHODS
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    /*
    *   GRAMMAR METHODS
    */
    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        return leftAssociativeBinaryOperation(() -> comparison(), BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expr comparison() {
        return leftAssociativeBinaryOperation(() -> addition(), GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expr addition() {
        return leftAssociativeBinaryOperation(() -> multiplication(), MINUS, PLUS);
    }

    private Expr multiplication() {
        return leftAssociativeBinaryOperation(() -> unary(), SLASH, STAR);
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private Expr leftAssociativeBinaryOperation(Supplier<Expr> operand, TokenType...stopTokens) {
        Expr expr = operand.get();

        while(match(stopTokens)) {
            Token operator = previous();
            Expr right = operand.get();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
    *   ERROR HANDLING METHODS
    */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
