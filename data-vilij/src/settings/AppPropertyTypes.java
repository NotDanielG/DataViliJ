package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    CHART_TITLE,
    DUPLICATE_ERROR,
    LINE_NUMBER,
    DISPLAY_ERROR,
    DISPLAY_BUTTON_TEXT,
    LOAD_TITLE,
    DONE,
    EDIT,
    CLASS,
    CLUSTER,
    CLASS1,
    CLUSTER1,
    CONFIGCLASS1,
    CONFIGCLUSTER1,
    INSTANCES_LOADED,
    LABELS_LOADED,
    LABELS_ARE,
    PATH_SOURCE,
    CLUSTER_AMOUNT,
    MAXITERATIONS,
    INTERVALS,
    CONTINUOUS,
    CLICK_WHEN_DONE,
    PNG,
    PNG_DESC;
}
