package checks;

import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class FailInTryCatchCheck {

  @Test
  public void should_throw_assertion_error() {
    //JUnit 4
    try {
      throwAssertionError(); // This test will pass even if we comment this line!
      org.junit.Assert.fail("Expected an AssertionError!"); // Noncompliant [[sc=24;ec=28;secondary=19]] {{Don't use fail() inside a try-catch catching an AssertionError.}}
    } catch (AssertionError e) {

    }

    //JUnit 5
    try {
      throwAssertionError(); // This test will pass even if we comment this line!
      fail("Expected an AssertionError!"); // Noncompliant [[sc=7;ec=11;secondary=29]]
    } catch (IllegalStateException e) {

    } catch (AssertionError e) {

    }

    try {
      fail("Expected an AssertionError!", new IllegalArgumentException()); // Noncompliant
    } catch (AssertionFailedError e) {}

    try {
      fail(); // Noncompliant
    } catch (Error error) {}

    try {
      fail(); // Noncompliant
    } catch (Throwable error) {}

    try {
      fail("Expected an AssertionError!"); // Noncompliant
    } catch (AssertionError error) {
      Object e = "somethingElse";
      Assert.assertThat(e.toString(), is("Assertion error"));
    }

    try {
      fail("Expected an AssertionError!"); // Noncompliant
    } catch (AssertionError error) {
      System.out.println("An unrelated message");
    }

    try {
      fail("Expected an AssertionError!"); // Noncompliant
    } catch (AssertionError|OutOfMemoryError error) {}

    try {
      if (something()) {
        throwAssertionError();
      } else {
        fail("Expected an AssertionError!"); // Noncompliant
      }
    } catch (AssertionError error) {}
  }

  @Test
  public void test_compliant() {
    assertThrows(AssertionError.class, () -> throwAssertionError()); // Compliant

    //JUnit 4
    try {
      org.junit.Assert.fail("Expected an AssertionError!"); // Compliant, we are testing the properties
    } catch (AssertionError e) {
      Assert.assertThat(e.getMessage(), is("Assertion error"));
    }

    //JUnit 5
    try {
      fail("Expected an IllegalStateException!"); // Compliant, we are expecting a IllegalStateException
    } catch (IllegalStateException e) {
    }

    try {
      throwAssertionError(); // Compliant, we are not using fail in body
    } catch (AssertionError e) {
    }

    try {
      fail("Expected an AssertionError!"); // Compliant, we are testing the properties
    } catch (AssertionError e) {
      Assert.assertThat(e.getMessage(), is("Assertion error"));
    }

    try {
      org.junit.Assert.fail("Expected an AssertionError!"); // Compliant, FN, we are not testing error properties correctly
    } catch (AssertionError e) {
      System.out.println(e.getMessage());
    }
  }

  private boolean something() {
    return false;
  }

  private void throwAssertionError() {
    throw new AssertionError("Assertion error");
  }

}
