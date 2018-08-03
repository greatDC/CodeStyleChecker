package info.woody.api.intellij.plugin.csct.bean;

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

public class CodeStyleCheckDictionary {

    private static final Map<String, List<String>> wordMap = new HashMap<>();

    private static class CodeStyleCheckDictionaryFactory {
        public static CodeStyleCheckDictionary dictionary = new CodeStyleCheckDictionary();

        static {
            try (InputStreamReader inputStreamReader = new InputStreamReader(CodeStyleCheckDictionary.class.getResource("/dictionary.txt").openStream());
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                wordMap.putAll(bufferedReader.lines()
                        .collect(Collectors.groupingByConcurrent(lineToKey -> lineToKey.replaceAll("^(\\w+).+$", "$1").toLowerCase(), toList())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private CodeStyleCheckDictionary() {
    }

    public static CodeStyleCheckDictionary getInstance() {
        return CodeStyleCheckDictionaryFactory.dictionary;
    }

    public boolean isVerb(String word) {
        List<String> stringList = wordMap.get(word.toLowerCase());
        if (stringList == null) {
            return false;
        }
        return stringList.stream().anyMatch(s -> s.indexOf("v.") > -1);
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

    public boolean isBeginningWithVerb(String camelCaseWord) {
        return isVerb(getFirstWord(camelCaseWord));
    }
}
