package info.woody.api.intellij.plugin.csct.bean

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Modifier

/**
 * Error messages.
 *
 * @author Woody
 * @since 14/06/2018
 */
class CodeStyleCheckIssues {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeStyleCheckIssues)

    public static final String GLOBAL_NO_AUTHORS = "AUTHOR CANNOT BE FOUND IN CLASS DOCUMENTATION."
    public static final String GLOBAL_DOC_AUTHOR_SINCE = "IT IS BETTER TO HAVE INFO FOR AUTHOR AND SINCE AS `@author` AND `@since`."
    public static final String GLOBAL_CONSECUTIVE_EMPTY_LINES = "CONSECUTIVE EMPTY LINES WERE FOUND."
    public static final String GLOBAL_LAST_METHOD_HAS_TAILING_EMPTY_LINE = "EMPTY LINE WAS NOT ALLOWED TO APPEAR AT THE END OF LAST METHOD."
    public static final String GLOBAL_STATIC_FINAL = "ALL `final static` SHOULD BE CORRECTED TO `static final`."
    public static final String GLOBAL_FILE_END_EMPTY_LINE = "LAST LINE OF THE FILE SHOULD BE EMPTY LINE."
    public static final String GLOBAL_MORE_THAN_500_LINES = "SOURCE CODE LINES ARE MORE THAN 500."
    public static final String GLOBAL_TODO_FIXME_HACK_XXX = "TODO/FIXME/HACK/XXX should be fixed ASAP."
    public static final String GLOBAL_BAD_CLASS_NAMING_WITH_PROFILE = "CLASS NAME SHOULD BE LIKE `PhoneValidatorChineseImpl` OR `BookingServiceInternationalImpl`."
    public static final String GLOBAL_MOCKITO_ORDER = "CORRECT FIELD DECLARATION ORDER FOR UNIT TEST: @Rule, @Spy, @Mock, @InjectMocks."
    public static final String GLOBAL_MISS_ERROR_CODE_TEST = "UNIT TEST MISSED ASSERTION FOR BELOW ERROR(S):%s"
    public static final String GLOBAL_CONSTRUCTOR_AFTER_METHOD = "CONSTRUCTOR SHOULD BE DEFINED BEFORE NON-CONSTRUCTOR METHODS."
    public static final String GLOBAL_CONSTANT_AFTER_FIELD = "CONSTANT SHOULD BE ALWAYS DEFINED BEFORE OTHER FIELDS."
    public static final String GLOBAL_STATIC_AFTER_FIELD = "STATIC FIELDS SHOULD BE DEFINED BEFORE NON-STATIC FIELDS."

    public static final String LINE_EXTRAORDINARY_LONG = "The line is too long to check, please wrap properly then check again."
    public static final String LINE_INCORRECT_CREATION_DATE_FORMAT = "Date format should be dd/mm/yyyy."
    public static final String LINE_DOCUMENTATION_FORMAT = "Documentation should start with a capital and end with `.`, `:`, `,`, `!`, `?` or `>`."
    public static final String LINE_DOCUMENTATION_REDUNDANT_EMPTY_LINES = "Please remove redundant empty lines!"
    public static final String LINE_CODE_IN_DOCUMENTATION = "Only first letter is allowed to be a capital unless it`s a proper noun or an acronym; use {@link ...} or {@code ...} if it refers to code."
    public static final String LINE_NO_DOCUMENTATION_CONTENT = "Documentation content must appear at the line following `/**`."
    public static final String LINE_COMMENTED_OUT_CODES = "The commented out codes should be removed."
    public static final String LINE_REDUNDANT_CODE_DESC = "Comments are only necessary for business descriptions or complex processes; it`s unnecessary for self-explanatory codes."
    public static final String LINE_SINGLE_LINE_COMMENT_FORMAT = "The format of single line comment should be `// description`."
    public static final String LINE_LEFT_CURLY_BRACE_LINE = "Left curly brace `{` isn`t allowed to occupy a single line."
    public static final String LINE_IMPORT_ASTERISK = "The asterisk `*` was found in import statement."
    public static final String LINE_NEVER_USED_IMPORTED = "This is imported but never used."
    public static final String LINE_CONSTANT_AS_LEFT_OPERAND = "Please use the constant as left operand to avoid NullPointerException."
    public static final String LINE_ENUM_COMPARE = "Please use `==` instead of `equals` for enum comparison."
    public static final String LINE_UNUSED_FIELD = "You have an unused field declaration."
    public static final String LINE_UNIT_TEST_PRIVATE_FIELD = "The field in test class should be private if it`s not referred outside."
    public static final String LINE_FIELD_MODIFIER_FOR_CONTROLLER = "You might need private if it`s not referred outside."
    public static final String LINE_FIELD_MODIFIER_FOR_SERVICE = "Use `protected` to make this type easily customisable."
    public static final String LINE_FIELD_NAME_CONVENTION = "Field declaration should be like `FullClassName fullClassName`."
    public static final String LINE_LOGGER_NAME_CONVENTION = "Please use LOGGER as the variable name."
    public static final String LINE_LOGGER_TARGET_CLASS = "LOGGER`s target class should be the current class."
    public static final String LINE_MISSING_FINAL = "Do you miss the keyword `final` or have redundant keyword `static`?"
    public static final String LINE_CONSTANT_NAME_CONVENTION = "All letters in constant should be uppercase."
    public static final String LINE_MISSING_UNIT_TEST = "Is the unit test missing?"
    public static final String LINE_CLASS_MISSING_DOCUMENTATION = "Do you have documentation for this class/interface/enum?"
    public static final String LINE_METHOD_MISSING_DOCUMENTATION = "Do you have documentation for this method?"
    public static final String LINE_PARAM_MISSING_DOCUMENTATION = "Do you have documentation for parameter: %s?"
    public static final String LINE_UNUSED_METHOD = "This method is never used in current class."
    public static final String LINE_CONSTRUCTOR_MISSING_DOCUMENTATION = "Do you have documentation for this constructor?"
    public static final String LINE_ASSERT = "To get more details, please use `Assert.assertXxx` for Java test."
    public static final String LINE_CONSTANT_FOR_LITERAL = "Literal should be extracted as a constant."
    public static final String LINE_NOT_FORMATTED = "Is this line formatted well? Tip: Ctrl + Alt + L"
    public static final String LINE_BAD_VARIABLE_PATTERN = "String, str, redis or xdist is a bad naming pattern."
    public static final String LINE_CONSTANT_REQUESTPROPERTIES = "Please use RequestParameters.REQUESTPROPERTIES instead."
    public static final String LINE_GROOVY_DEF = "Please replace `def` with an explicit type."
    public static final String LINE_BAD_PRINT = "Use LOGGER to replace `print` or `println`."
    public static final String LINE_EXCEED_140_CHARS = "This line exceeds 140 chars."
    public static final String LINE_MERGE_LINES = "Could previous line and this line be merged?"
    public static final String LINE_ENUM_IMPORT = "Import enum type directly, e.g GenderEnum.MALE. Don`t forget to clear useless import."
    public static final String LINE_IDENTICAL_EXPRESSIONS = "Identical expressions can be extracted as a variable to eliminate repetition: %s"
    public static final String LINE_REDUCE_MULTIPLE_CALCULATION = "Please define a variable to store the value of length/size."
    public static final String LINE_OPTIMIZE_BOOLEAN_RETURN = "Please optimize multiple `return` to one; sometime ternary operator `(expr) ? x : y` might be helpful."
    public static final String LINE_OPTIMIZE_RETURN = "Please optimize multiple `return` to single; sometime ternary operator `(expr) ? x : y` might be helpful."
    public static final String LINE_MOVE_UPPER_ADVICE = "Could this line be moved upper?"
    public static final String LINE_REDUNDANT_GROOVY_SEMICOLON = "Semicolon is unnecessary in Groovy."
    @Deprecated public static final String LINE_GROOVY_PUBLIC_IN_FIELD = "Keyword `public` is redundant for non-static fields in Groovy."
    public static final String LINE_GROOVY_PUBLIC_IN_METHOD = "Keyword `public` is redundant for methods in Groovy."
    public static final String LINE_GROOVY_PUBLIC_IN_CLASS = "Keyword `public` is redundant for class/interface in Groovy."
    public static final String LINE_IMPROPER_ACRONYM_NAMING = "Please correct the name containing acronym, e.g. getHTMLChar() -> getHtmlChar()."
    public static final String LINE_VALIDATOR_CONVENTION = "A validator must implement a Validator/ValidationErrorCollector interface."
    public static final String LINE_BOOLEAN_LITERAL_COMPARE = "Never compare with Boolean literal!"
    public static final String LINE_LOG_EXCEPTION = "Please log the error like `LOGGER.error(message, e)` or `LOGGER.warn(message, e)`."
    public static final String LINE_TEST_METHOD_PREFIX_WRONG = "Test method should have a prefix `test`."
    public static final String LINE_METHOD_VERB_STARTS = "The method name should start with a verb."
    public static final String LINE_CLASS_VERB_STARTS = "The class name should not start with a verb."
    public static final String LINE_TEST_METHOD_SETUP = "Please change setup to `setUp`."
    public static final String LINE_EMPTY_STATEMENT = "Please remove the empty statement."
    public static final String LINE_SNAKE_CASE_NAMING = "Please use CamelCase instead of snake_case."
    public static final String LINE_UNNECESSARY_NULL_CHECK = "Is null check here really necessary?"
    public static final String LINE_UNNECESSARY_EMPTY_CHECK = "Could org.apache.commons.lang.StringUtils be helpful?"
    public static final String LINE_GROOVY_NULL_CHECK = "Please use proper check expression for null. In Groovy`s boolean expressions, null is false while !null is true."

    static Map<String, String> ALL_CHECK_ITEMS() {
        Map<String, String> resultMap = [:]

        CodeStyleCheckIssues.getDeclaredFields().findAll { field ->
            int modifiers = field.getModifiers()
            return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
        }.sort { f1, f2 ->
            try {
                char field1Char = f1.getName().charAt(0)
                char field2Char = f2.getName().charAt(0)
                int charCompareResult = Character.compare(field1Char, field2Char)
                return (charCompareResult == 0) ? f1.get(null).toString() <=> f2.get(null).toString() : charCompareResult
            } catch (IllegalAccessException e) {
                LOGGER.error('Failed to `sort` check items for check items generation!', e)
                return 0
            }
        }.each { field ->
            try {
                int modifiers = field.getModifiers()
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
                        && Modifier.isFinal(modifiers) && field.getName().matches('^(GLOBAL|LINE).*$')) {
                    String checkItem = field.get(null).toString()
                    resultMap.put(field.name, checkItem)
                }
            } catch (IllegalAccessException e) {
                LOGGER.error('Failed to `process` check items for check items generation!', e)
            }
        }
        resultMap
    }
}
