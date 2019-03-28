package jlox;

import jlox.tokens.Token;
import jlox.tokens.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.tokens.TokenType.*;

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
            case '!': addToken(nextMatches('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(nextMatches('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(nextMatches('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(nextMatches('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (nextMatches('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
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

    private Boolean isAlpha(Character character) {
        return (character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z')
                || character == '_';
    }

    private void identifier() {
        while(isAlphanumeric(peek())) advance();

        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private Boolean isAlphanumeric(Character character) {
        return isAlpha(character) || isDigit(character);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private Character peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private Boolean isDigit(Character character) {
        return character >= '0' && character <= '9';
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

    private Character peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private Boolean nextMatches(Character expected) {
        if (isAtEnd()) return Boolean.FALSE;
        if (source.charAt(current) != expected) return Boolean.FALSE;

        current++;
        return Boolean.TRUE;
    }

    private Boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    private Character advance() {
        this.current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = this.source.substring(start, current);
        this.tokens.add(new Token(type, text, literal, line));
    }

}
