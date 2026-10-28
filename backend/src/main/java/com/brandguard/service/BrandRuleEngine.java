package com.brandguard.service;

import com.brandguard.domain.BrandRule;
import com.brandguard.domain.Product;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class BrandRuleEngine {
    private static final Pattern PRICE = Pattern.compile("(?i)(?:₹\\s*|rs\\.?\\s*)([\\d,]+(?:\\.\\d{1,2})?)");
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Pattern GUARANTEE = Pattern.compile("(?i)\\b(?:guarantee(?:d|s)?|100\\s*%\\s*results|risk[- ]free)\\b");

    public List<FindingDraft> analyze(String content, List<Product> products, List<BrandRule> rules) {
        List<FindingDraft> findings = new ArrayList<>();
        for (Product product : products) {
            if (!containsApprovedName(content, product.getName())) {
                String excerpt = findProductMention(content, product.getName());
                if (excerpt != null) {
                    findings.add(new FindingDraft("PRODUCT_NAME", "HIGH", "JAVA_RULE", excerpt,
                            "The product wording does not use the exact approved product name.",
                            product.getName(), product.getId(), product.getName(), false));
                }
            }

            Matcher prices = PRICE.matcher(content);
            while (prices.find()) {
                BigDecimal mentioned = new BigDecimal(prices.group(1).replace(",", ""));
                if (mentioned.compareTo(product.getPrice()) != 0) {
                    findings.add(new FindingDraft("PRICE_MISMATCH", "HIGH", "JAVA_RULE", prices.group(),
                            "The stated price differs from the approved product price.",
                            formatPrice(product.getPrice()), product.getId(), formatPrice(product.getPrice()), false));
                }
            }
        }

        for (BrandRule rule : rules) {
            if ("PROHIBITED_TERM".equals(rule.getKind())) {
                Matcher matcher = Pattern.compile(Pattern.quote(rule.getValue()), Pattern.CASE_INSENSITIVE).matcher(content);
                while (matcher.find()) {
                    findings.add(new FindingDraft("PROHIBITED_TERM", rule.getSeverity(), "JAVA_RULE", matcher.group(),
                            rule.getExplanation(), null, rule.getId(), null, false));
                }
            } else if ("APPROVED_TERM".equals(rule.getKind())) {
                String[] terms = rule.getValue().split("=>", 2);
                if (terms.length == 2 && !terms[0].isBlank() && !terms[1].isBlank()) {
                    Matcher matcher = Pattern.compile(Pattern.quote(terms[0].trim()), Pattern.CASE_INSENSITIVE).matcher(content);
                    while (matcher.find()) {
                        findings.add(new FindingDraft("APPROVED_TERMINOLOGY", rule.getSeverity(), "JAVA_RULE",
                                matcher.group(), rule.getExplanation(), terms[1].trim(), rule.getId(),
                                terms[1].trim(), false));
                    }
                }
            } else if ("REQUIRED_DISCLAIMER".equals(rule.getKind())
                    && !content.toLowerCase(Locale.ROOT).contains(rule.getValue().toLowerCase(Locale.ROOT))) {
                findings.add(new FindingDraft("MISSING_DISCLAIMER", rule.getSeverity(), "JAVA_RULE", content,
                        rule.getExplanation(), rule.getValue(), rule.getId(), rule.getValue(), false));
            } else if ("PROMOTIONAL_CLAIM".equals(rule.getKind())) {
                Matcher matcher = Pattern.compile(rule.getValue(), Pattern.CASE_INSENSITIVE).matcher(content);
                while (matcher.find()) {
                    findings.add(new FindingDraft("UNSUPPORTED_CLAIM", rule.getSeverity(), "JAVA_RULE", matcher.group(),
                            rule.getExplanation(), null, rule.getId(), "Replace the guarantee with a factual, verifiable description.", true));
                }
            }
        }

        if (rules.stream().noneMatch(rule -> "PROMOTIONAL_CLAIM".equals(rule.getKind()))) {
            Matcher matcher = GUARANTEE.matcher(content);
            while (matcher.find()) {
                findings.add(new FindingDraft("UNSUPPORTED_CLAIM", "HIGH", "JAVA_RULE", matcher.group(),
                        "A performance guarantee requires supporting evidence and approval.",
                        "Promotional claim policy", null, "Replace the guarantee with a factual, verifiable description.", true));
            }
        }
        return findings;
    }

    private boolean containsApprovedName(String content, String productName) {
        return content.toLowerCase(Locale.ROOT).contains(productName.toLowerCase(Locale.ROOT));
    }

    private String findProductMention(String content, String productName) {
        Matcher words = WORD.matcher(productName);
        while (words.find()) {
            String token = words.group();
            if (token.length() < 4) continue;
            Matcher mention = Pattern.compile(Pattern.quote(token), Pattern.CASE_INSENSITIVE).matcher(content);
            if (mention.find()) {
                Matcher surroundingWords = WORD.matcher(content);
                int previousStart = mention.start();
                int nextEnd = mention.end();
                while (surroundingWords.find()) {
                    if (surroundingWords.end() <= mention.start()) previousStart = surroundingWords.start();
                    if (surroundingWords.start() >= mention.end()) {
                        nextEnd = surroundingWords.end();
                        break;
                    }
                }
                return content.substring(previousStart, nextEnd).replaceAll("[!.,;:]+$", "").trim();
            }
        }
        return null;
    }

    private String formatPrice(BigDecimal price) {
        return "₹" + NumberFormat.getNumberInstance(new Locale("en", "IN")).format(price);
    }
}
