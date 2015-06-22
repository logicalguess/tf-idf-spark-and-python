package logicalguess

import java.util.regex.Pattern

/**
 * Helper object that provides methods to clean-up lines of SMS text or normalize these
 * lines, e.g., replace typical patterns like number, urls, email addresses, etc. by
 * placeholder strings.
 *
 * @example
 * {{{
 *    import smsClassificationWithLogRegr.LineCleaner
 *    LineCleaner.normalizeCurrencySymbol("Replaces € in this text by \" normalizedcurrencysymbol \"")
 * }}}
 */
object LineCleaner {

  /**
   * Returns `text` with all sub-strings matching the regular expression `regex`
   * replaced by the string `normalizationString`.
   *
   * @note This is used as common template for string normalization methods provided
   *       by this object.
   */
  def applyNormalizationTemplate(text: String, regex: String, normalizationString: String): String = {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(text)
    val normalizedText = matcher.replaceAll(normalizationString)

    normalizedText
  }

  /**
   * Returns `text` with the following occurrences removed:
   * 1) Punctuation: '.', ',', ':', '-', '!', '?' and combinations/repetitions of characters from 1) like '--',
   * '!?!?!', '...' (ellipses), etc.
   * 2) Special characters: '\n', '\t', '%', '#', '*', '|', '=', '(', ')', '"', '>', '<', '/'
   *
   * @note Use this with care if you are interested in phone numbers (they contain '-') or
   *       smileys (like ':-)').
   */
  def removePunctuationAndSpecialChars(text: String): String = {
    val regex = "[\\.\\,\\:\\-\\!\\?\\n\\t,\\%\\#\\*\\|\\=\\(\\)\\\"\\>\\<\\/]"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(text)

    // Remove all matches, split at whitespace (allow repetitions) then join again.
    val cleanedText = matcher.replaceAll(" ").split("[ ]+").mkString(" ")

    cleanedText
  }

  /**
   * Returns `text` with every occurrence of one of the currency symbol
   * '$', '€' or '£' replaced by the string literal " normalizedcurrencysymbol ".
   */
  def normalizeCurrencySymbol(text: String): String = {
    val regex = "[\\$\\€\\£]"
    applyNormalizationTemplate(text, regex, " normalizedcurrencysymbol ")
  }

  /**
   * Returns `text` with every occurrence of an emonicon (see implementation for
   * details) replaced by the string literal " normalizedemonicon ".
   */
  def normalizeEmonicon(text: String): String = {
    val emonicons = List(":-)", ":)", ":D", ":o)", ":]", ":3", ":c)", ":>", "=]", "8)")
    val regex = "(" + emonicons.map(Pattern.quote).mkString("|") + ")"
    applyNormalizationTemplate(text, regex, " normalizedemonicon ")
  }

  /**
   * Returns `text` with every occurrence of one of a number
   * replaced by the string literal "normalizednumber".
   */
  def normalizeNumbers(text: String): String = {
    val regex = "\\d+"
    applyNormalizationTemplate(text, regex, " normalizednumber ")
  }

  /**
   * Returns `text` with every occurrence of one of an URL
   * replaced by the string literal " normalizedurl ".
   *
   * @note This implementation does only a very naive test and
   *       also will miss certain cases.
   */
  def normalizeURL(text: String): String = {
    val regex = "(http://|https://)?www\\.\\w+?\\.(de|com|co.uk)"
    applyNormalizationTemplate(text, regex, " normalizedurl ")
  }

  /**
   * Returns `text` with every occurrence of one of an email address
   * replaced by the string literal " normalizedemailadress ".
   *
   * @note This implementation does only a very naive test and
   *       also will miss certain cases.
   */
  def normalizeEmailAddress(text: String): String = {
    val regex = "\\w+(\\.|-)*\\w+@.*\\.(com|de|uk)"
    applyNormalizationTemplate(text, regex, " normalizedemailadress ")
  }

  /**
   * Returns `line` with HTML character entities, excluding whitespace "&nbsp;"
   * which will be treated elsewhere, removed.
   */
  def removeHTMLCharacterEntities(text: String): String = {
    val HTMLCharacterEntities = List("&lt;", "&gt;", "&amp;", "&cent;", "&pound;", "&yen;", "&euro;", "&copy;", "&reg;")
    val regex = "(" + HTMLCharacterEntities.map(x => "\\" + x).mkString("|") + ")"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(text)

    val cleanedText = matcher.replaceAll("")

    cleanedText
  }

  /**
   * First normalizes the `text` and then removes unwanted characters from it.
   */
  def clean(text: String): String = {
    List(text).map(text => text.toLowerCase())
      .map(normalizeEmonicon)
      .map(normalizeURL)
      .map(normalizeEmailAddress)
      .map(normalizeCurrencySymbol)
      .map(removeHTMLCharacterEntities)
      .map(normalizeNumbers)
      .map(removePunctuationAndSpecialChars)
      .map(_.trim)
      .head
  }
}