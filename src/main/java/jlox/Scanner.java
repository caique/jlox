package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

public class Scanner {

    private final String source;
    private final List<Token> tokens;
    private static final Map<String, TokenType> keywords;

    private Integer start;
    private Integer current;
    private Integer line;

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();

        this.start = 0;
        this.current = 0;
        this.line = 1;
    }

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            this.start = this.current;
            scanToken();
        }

        this.tokens.add(new Token(EOF, "", null, line));
        return this.tokens;
    }

    private void scanToken() {
        Character nextCharacter = advance();

        switch (nextCharacter) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    multilineComment();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(nextCharacter)) {
                    number();
                } else if (isAlpha(nextCharacter)){
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = this.source.substring(start, current);
        this.tokens.add(new Token(type, text, literal, line));
    }

    /*
    * FEATURES
     */

    private void identifier() {
        while(isAlphanumeric(peek())) advance();

        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void multilineComment() {
        while (peek() != '*') advance();
        while (peek() != '/') advance();
        advance();
    }

    /*
    * HELPER METHODS
     */
    private Boolean isAlpha(Character character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z')
                || character == '_';
    }

    private Boolean isAlphanumeric(Character character) {
        return isAlpha(character) || isDigit(character);
    }

    private Boolean isDigit(Character character) {
        return character >= '0' && character <= '9';
    }

    private Boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    /*
    * NAVIGATION METHODS
     */
    private Character peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private Character peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private Character advance() {
        this.current++;
        return source.charAt(current - 1);
    }

    private Boolean match(Character expected) {
        if (isAtEnd()) return Boolean.FALSE;
        if (source.charAt(current) != expected) return Boolean.FALSE;

        current++;
        return Boolean.TRUE;
    }

}
