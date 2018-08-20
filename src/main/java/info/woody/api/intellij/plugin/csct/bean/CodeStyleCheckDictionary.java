package info.woody.api.intellij.plugin.csct.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Plugin dictionary.
 *
 * @author zhengwei.ren
 * @since 04/08/2018
 */
public class CodeStyleCheckDictionary {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeStyleCheckDictionary.class);
    private static final Map<String, List<String>> wordMap = new HashMap<>();

    private static class CodeStyleCheckDictionaryFactory {
        public static final CodeStyleCheckDictionary dictionary = new CodeStyleCheckDictionary();

        static {
            try (InputStreamReader inputStreamReader = new InputStreamReader(
                    CodeStyleCheckDictionary.class.getResource("/dictionary.txt").openStream());
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                wordMap.putAll(bufferedReader.lines().collect(Collectors.groupingByConcurrent(
                        lineToKey -> lineToKey.replaceAll("^(\\w+).+$", "$1").toLowerCase(), toList())));
            } catch (Exception e) {
                LOGGER.error("Failed to initialise `CodeStyleCheckDictionary.wordMap`.", e);
            }
        }
    }

    /**
     * Constructor.
     */
    private CodeStyleCheckDictionary() {
    }

    /**
     * Get the dictionary.
     *
     * @return The dictionary.
     */
    public static CodeStyleCheckDictionary getDictionary() {
        return CodeStyleCheckDictionaryFactory.dictionary;
    }

    public boolean isVerb(String word, boolean isUnique) {
        List<String> wordList = wordMap.get(word.toLowerCase());
        if (wordList == null) {
            if (word.endsWith("s")) {
                wordList = wordMap.get(word.replaceFirst(".$", ""));
            }
            if (wordList == null) {
                return false;
            }
        }
        if (isUnique && wordList.stream().anyMatch(s -> s.indexOf("n.") > -1 || s.indexOf("a.") > -1 || s.indexOf("adj.") > -1)) {
            return false;
        }
        return wordList.stream().anyMatch(s -> s.indexOf("v.") > -1);
    }

    public boolean isVerb(String word) {
        return isVerb(word, false);
    }

    public boolean isUniqueVerb(String word) {
        return isVerb(word, true);
    }

    public boolean isNotVerb(String word) {
        List<String> wordList = wordMap.get(word.toLowerCase());
        if (wordList == null) {
            if (word.endsWith("s")) {
                wordList = wordMap.get(word.replaceFirst(".$", ""));
            }
            if (wordList == null) {
                return false;
            }
        }
        return wordList.stream().anyMatch(s -> s.matches("^.*\\b[^v][.].*$"));
    }

    public boolean isWord(String word) {
        return wordMap.get(word) != null;
    }

    public List<String> getPossibleWords(String camelCaseWord) {
        if (!camelCaseWord.matches("^.*([a-z][A-Z]|[A-Z][a-z]).*$")) {
            return Stream.of(camelCaseWord).collect(toList());
        }
        return Arrays.stream(camelCaseWord
                .replaceAll("([a-z])([A-Z])", "$1~$2")
                .replaceAll("([A-Z])([a-z])", "~$1$2")
                .split("~"))
                .filter(s -> s.length() > 0).collect(toList());
    }

    public String getFirstWord(String camelCaseWord) {
        Optional<String> first = getPossibleWords(camelCaseWord).stream().findAny();
        return first.orElse(null);
    }

    public boolean isVerbBeginning(String camelCaseWord) {
        return isVerb(getFirstWord(camelCaseWord));
    }

    public boolean isUniqueVerbBeginning(String camelCaseWord) {
        return isUniqueVerb(getFirstWord(camelCaseWord));
    }
}
