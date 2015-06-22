package logicalguess

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import LineCleaner._

@RunWith(classOf[JUnit4])
class LineCleanerTest {
  @Test
  def test1(): Unit = {
    val actual = normalizeCurrencySymbol("$")
    val desired = " normalizedcurrencysymbol "
    assertEquals("`normalizeCurrencySymbol` should replace $.", actual, desired)
  }

  @Test
  def test2(): Unit = {
    val actual = normalizeCurrencySymbol("€")
    val desired = " normalizedcurrencysymbol "
    assertEquals("`normalizeCurrencySymbol` should replace €.", actual, desired)
  }

  @Test
  def test3(): Unit = {
    val actual = normalizeCurrencySymbol("£")
    val desired =" normalizedcurrencysymbol "
    assertEquals("`normalizeCurrencySymbol` should replace £.", actual, desired)
  }

  @Test
  def test4(): Unit = {
    val HTMLCharacterEntities = List("&lt;", "&gt;", "&amp;", "&cent;", "&pound;", "&yen;",
      "&euro;", "&copy;", "&reg;")
    val text = HTMLCharacterEntities.mkString("")

    val expected = removeHTMLCharacterEntities(text)
    val actual = ""

    assertEquals("`removeHTMLCharacterEntities` should remove all HTML character entities.", expected, actual)
  }

  @Test
  def normalizeURLTest1(): Unit = {
    val text = "This text contains www.example.co.uk"
    val actual = normalizeURL(text)
    val expected = "This text contains  normalizedurl "

    val msg = "Substring starting with 'www.' should be normalized."
    assertEquals(msg, expected, actual)
  }

  @Test
  def normalizeURLTest2(): Unit = {
    val text = "This text contains http://www.something.com."
    val actual = normalizeURL(text)
    val expected = "This text contains  normalizedurl ."

    val msg = "Substring starting with 'http://www.' should be normalized."
    assertEquals(msg, expected, actual)
  }

  @Test
  def normalizeURLTest3(): Unit = {
    val text = "This text contains https://www.security.com."
    val actual = normalizeURL(text)
    val expected = "This text contains  normalizedurl ."

    val msg = "Substring starting with 'https://www.' should be normalized."
    assertEquals(msg, expected, actual)
  }

  @Test
  def normalizeEmailAddressTest1(): Unit = {
    val text = "This text contains john.doe@example.com."
    val actual = normalizeEmailAddress(text)
    val expected = "This text contains  normalizedemailadress ."

    val msg = "Substring containing '@' should be normalized."
    assertEquals(msg, expected, actual)
  }

  @Test
  def normalizeEmailAddressTest2(): Unit = {
    val text = "This text contains herr-mustermann@bespiel.de."
    val actual = normalizeEmailAddress(text)
    val expected = "This text contains  normalizedemailadress ."

    val msg = "Substring containing '@' should be normalized."
    assertEquals(msg, expected, actual)
  }

  @Test
  def normalizeEmailAddressTest3(): Unit = {
    val text = "This text contains herrmustermann@bespiel.de."
    val actual = normalizeEmailAddress(text)
    val expected = "This text contains  normalizedemailadress ."

    val msg = "Substring containing '@' should be normalized."
    assertEquals(msg, expected, actual)
  }

  @Test
  def normalizeEmailAddressTest4(): Unit = {
    val text = "This text contains kate@palace.uk."
    val actual = normalizeEmailAddress(text)
    val expected = "This text contains  normalizedemailadress ."

    val msg = "Substring containing '@' should be normalized."
    assertEquals(msg, expected, actual)
  }
}