package org.opentripplanner.generate.doc.framework;

import java.util.regex.Pattern;

/**
 * Replace a text in a file wrapped using HTML comments
 */
@SuppressWarnings("NewClassNamingConvention")
public class TemplateUtil {

  private static final String PARAMETERS_TABLE = "PARAMETERS-TABLE";
  private static final String PARAMETERS_DETAILS = "PARAMETERS-DETAILS";

  private static final String NEW_LINE = "\n";
  private static final String COMMENT_OPEN = "<!-- ";
  private static final String COMMENT_CLOSE = " -->";
  private static final String START = " BEGIN";
  private static final String END = " END";
  private static final String MULTI_LINE = "(?s)";
  private static final String ANY_TEXT = "(.*)";
  private static final String AUTO_GENERATION_INFO =
    "\n<!-- NOTE! This section is auto-generated. Do not change, change doc in code instead. -->\n";

  public static String replaceParametersTable(String doc, String replacement) {
    return replaceSection(doc, PARAMETERS_TABLE, replacement);
  }

  public static String replaceParametersDetails(String doc, String replacement) {
    return replaceSection(doc, PARAMETERS_DETAILS, replacement);
  }

  public static String replaceSection(String doc, String token, String replacement) {
    var start = start(token);
    var end = end(token);

    String regex = MULTI_LINE + start + ANY_TEXT + end;
    var m = Pattern.compile(regex).matcher(doc);

    if (!m.find()) {
      throw new IllegalStateException("Regexp did not match document. Regexp: /" + regex + "/");
    }

    return (
      doc.substring(0, m.start(1)) +
      AUTO_GENERATION_INFO +
      air(replacement) +
      doc.substring(m.end(1))
    );
  }

  private static String air(String section) {
    return NEW_LINE + section + NEW_LINE;
  }

  private static String start(String token) {
    return COMMENT_OPEN + token + START + COMMENT_CLOSE;
  }

  private static String end(String token) {
    return COMMENT_OPEN + token + END + COMMENT_CLOSE;
  }
}
