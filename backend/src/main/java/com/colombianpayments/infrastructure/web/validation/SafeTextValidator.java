package com.colombianpayments.infrastructure.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Flags the usual SQL injection shapes ("' OR 1=1", "; DROP TABLE", "UNION SELECT", comments) but lets
 * normal text such as "Mom's gift" through. The queries are not built from strings anyway, this is
 * a defense in depth layer.
 */
public class SafeTextValidator implements ConstraintValidator<SafeText, String> {

    private static final Pattern SUSPICIOUS =
            Pattern.compile(
                    "('\\s*(or|and)\\b)"
                            + "|(\\b(or|and)\\b\\s+\\d+\\s*=\\s*\\d+)"
                            + "|(;\\s*(drop|delete|update|insert|select|alter)\\b)"
                            + "|(\\bunion\\s+(all\\s+)?select\\b)"
                            + "|(\\bdrop\\s+table\\b)"
                            + "|(--(\\s|$))"
                            + "|(/\\*)",
                    Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !SUSPICIOUS.matcher(value).find();
    }
}
