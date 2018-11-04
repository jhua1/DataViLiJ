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
    ALGORITHM_RESOURCE_PATH,
    EDIT_ICON_PATH,
    ALGORITHM_CLASS_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    EXIT_WHILE_RUNNING_WARNING,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    EXIT_WHILE_RUNNING_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    CLUSTER_NUMBER,
    MAX_ITERATIONS,
    CONTINUOUS,
    UPDATE_INTERVAL,
    CONFIGURATION_EDIT_TITLE,
    EDIT_CONFIGURATION,
    Random_Clustering,
    Random_Classification,
    Algorithm_Cluster,
    Algorithm_Class,
    APP_CSS_FILE_DIR,
    APP_CSS_FILE,
    TOGGLE_DISPLAY,
    IMG_FILE_EXT,
    IMG_FILE_EXT_DESC,
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    CHART_TITLE,
    DISPLAY_BUTTON_TEXT
}
