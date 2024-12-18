package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Static imports: don't have to qualify with the class, frowned upon due to conflict potential
import static jlox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("for",     FOR);
        keywords.put("fun",     FUN);
        keywords.put("if",      IF);
        keywords.put("nil",     NIL);
        keywords.put("or",      OR);
        keywords.put("print",   PRINT);
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // Beginning of the next lexeme
            start = current;
            scanToken();
        }
        
        // Add a final EOF token
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        
        switch(c) {
            // Single character lexemes
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

            // Some tokens may be double-characters, scan the next character as well
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            // Division or a comment
            case '/':
                if (match('/')) {
                    // Comment goes on until the end of the line; we don't care about these tokens
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) { // Challenge 4: multiline comment support
                    // Continue advancing until a */ is found
                    boolean terminated = false;
                    while (!isAtEnd()) {
                        char c2 = advance();
                        if (c2 == '*') {
                            if (peek() == '/') {
                                advance();
                                terminated = true;
                                break;
                            }
                        } else if (c2 == '\n') line++;
                    }

                    if (!terminated) Lox.error(line, "Unexpected comment.");
                } else {
                    addToken(SLASH);
                }
                break;

            // Skip over newlines and whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;

            // Strings are surrounded with ""
            case '"': string(); break;

            // Still advanced current index, and will continue scanning tokens
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // Multiline string support
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance(); // Closing "

        // Trim surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // May have stopped at the fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the .
            advance();

            // Continue reading digits
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER; // Not a reserved keyword
        addToken(type);
    }

    // "Conditional" advance, scans a character and only advances if it what was expected
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        return true;
    }

    // Peek at the current character that has not been consumed yet
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // Peek at the next character (second to be consumed)
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private boolean isAlpha(char c) {
        return ('a' <= c && c <= 'z') ||
               ('A' <= c && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Returns the character currently pointed at, then advances the current index
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
