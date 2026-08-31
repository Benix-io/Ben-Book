package com.systsync.ui;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private static final Pattern KEYWORDS = Pattern.compile(
        "\\b(public|private|protected|class|interface|void|int|float|double|boolean|String|if|else|for|while|return|import|package|def|fn|let|const|var|function|async|await)\\b"
    );
    private static final Pattern STRINGS = Pattern.compile("\"(\\\\.|[^\"])*\"");
    private static final Pattern COMMENTS = Pattern.compile("(//.*|/\\*[\\s\\S]*?\\*/)");

    public static Spannable highlight(String code) {
        if (code == null) return new SpannableString("");
        SpannableString spannable = new SpannableString(code);

        // Strings (Blue/Cyan)
        Matcher strMatcher = STRINGS.matcher(code);
        while (strMatcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#A5D6FF")), 
                strMatcher.start(), strMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Keywords (Orange/Red)
        Matcher kwMatcher = KEYWORDS.matcher(code);
        while (kwMatcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF7B72")), 
                kwMatcher.start(), kwMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Comments (Grey)
        Matcher commentMatcher = COMMENTS.matcher(code);
        while (commentMatcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#8B949E")), 
                commentMatcher.start(), commentMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }
}
