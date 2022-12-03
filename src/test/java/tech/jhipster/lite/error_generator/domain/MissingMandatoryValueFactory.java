package tech.jhipster.lite.error_generator.domain;

import tech.jhipster.lite.error.domain.Assert;

public final class MissingMandatoryValueFactory {

  public static void missingMandatoryValue() {
    Assert.notNull("myField", null);
  }
}
