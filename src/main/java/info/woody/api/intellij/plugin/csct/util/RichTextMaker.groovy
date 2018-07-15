package info.woody.api.intellij.plugin.csct.util

import groovy.xml.XmlUtil

import static info.woody.api.intellij.plugin.csct.util.Const.SING_SINGLE_QUOTE

/**
 * Utility class to create HTML tags.
 *
 * @author Woody
 * @since 15/06/2018
 */
class RichTextMaker {

    /**
     * Create a new link tag.
     *
     * @param href Hyperlink.
     * @param title Title.
     * @param text Text content.
     * @return A complete HTML link tag.
     */
    static String newLink(String href, String title, String text) {
        "<a href='${href}' title='${escapeContent(title)}'>${text}</a>"
    }

    /**
     * Create a new highlight text.
     * @param text Text content.
     * @return A text in color.
     */
    static String newHighlight(String text) {
        "<font color='red'>${escapeContent(text)}</font>"
    }

    /**
     * Escape HTML chars for content.
     *
     * @param string Content to process.
     * @return Processed content.
     */
    static String escapeContent(String string) {
        XmlUtil.escapeXml(string).replace("&apos;", SING_SINGLE_QUOTE)
    }

    /**
     * Escape HTML chars for arguments.
     *
     * @param strings Content to process.
     * @return Processed content.
     */
    static String[] escapeArgs(String... strings) {
        strings.collect { XmlUtil.escapeXml(it).replace("&apos;", SING_SINGLE_QUOTE) }
    }
}
