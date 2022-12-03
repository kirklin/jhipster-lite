package tech.jhipster.lite.error.infrastructure.primary;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tech.jhipster.lite.error.domain.AssertionErrorType;
import tech.jhipster.lite.error.domain.AssertionException;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
class AssertionErrorsHandler {

  private static final String MESSAGES_PREFIX = "assertion-error.";

  private final MessageSource messages;

  public AssertionErrorsHandler(@Qualifier("assertionErrorMessageSource") MessageSource messages) {
    Locale.setDefault(Locale.ENGLISH);

    this.messages = messages;
  }

  @ExceptionHandler(AssertionException.class)
  ProblemDetail handleMissingMandatoryValue(AssertionException exception) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(buildStatus(exception), buildDetail(exception));

    problem.setTitle(getMessage(exception.type(), "title"));
    problem.setProperty("key", exception.type().name());

    return problem;
  }

  private HttpStatus buildStatus(AssertionException exception) {
    return Stream
      .of(exception.getStackTrace())
      .map(StackTraceElement::getClassName)
      .filter(inApplication())
      .filter(notInErrorDomain())
      .findFirst()
      .filter(primaryClass())
      .map(className -> HttpStatus.BAD_REQUEST)
      .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private Predicate<String> inApplication() {
    return className -> className.startsWith("tech.jhipster.lite.");
  }

  private Predicate<String> notInErrorDomain() {
    return className -> !className.startsWith("tech.jhipster.lite.error.domain");
  }

  private Predicate<String> primaryClass() {
    return className -> className.contains(".primary.");
  }

  private String buildDetail(AssertionException exception) {
    String messageTemplate = getMessage(exception.type(), "detail");

    return ArgumentsReplacer.replaceParameters(messageTemplate, parameters(exception));
  }

  private Map<String, String> parameters(AssertionException exception) {
    HashMap<String, String> parameters = new HashMap<>(exception.parameters());
    parameters.put("field", exception.field());

    return parameters;
  }

  private String getMessage(AssertionErrorType type, String suffix) {
    return messages.getMessage(MESSAGES_PREFIX + type.name() + "." + suffix, null, locale());
  }

  private Locale locale() {
    return LocaleContextHolder.getLocale();
  }
}
