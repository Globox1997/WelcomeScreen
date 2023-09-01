package net.welcomescreen.data;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class WelcomeScreenData {

    @Nullable
    public static final List<Object> TITLE_LIST = new ArrayList<Object>();
    @Nullable
    public static final List<Object> CLOSE_LIST = new ArrayList<Object>();
    @Nullable
    public static final List<Object> BACKGROUND_LIST = new ArrayList<Object>();

    @Nullable
    public static final List<List<Object>> TEXT_LIST = new ArrayList<List<Object>>();
    @Nullable
    public static final List<List<Object>> IMAGE_LIST = new ArrayList<List<Object>>();
    @Nullable
    public static final List<List<Object>> BUTTON_LIST = new ArrayList<List<Object>>();

    @Nullable
    public static final List<String> COMMAND_LIST = new ArrayList<String>();

    public static void clearData() {
        TITLE_LIST.clear();
        CLOSE_LIST.clear();
        BACKGROUND_LIST.clear();
        TEXT_LIST.clear();
        IMAGE_LIST.clear();
        BUTTON_LIST.clear();
        COMMAND_LIST.clear();
    }

}
