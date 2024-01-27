package it.italiandudes.bot6329.modules.localization;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.throwables.exceptions.module.localization.LocalizationException;
import it.italiandudes.bot6329.throwables.exceptions.module.localization.LocalizationModuleException;
import it.italiandudes.bot6329.utils.Resource;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Localization {
    EN_US("English (US)"),
    IT_IT("Italiano"),
    ;

    // Attributes
    public final String EXTENDED_LANG;

    // Localization Files Extension
    public static final String LOCALIZATION_FILE_EXTENSION = "json";

    // Fallback Localization
    public static final Localization FALLBACK = EN_US;

    // Constructor
    Localization(@NotNull final String EXTENDED_LANG) {
        this.EXTENDED_LANG = EXTENDED_LANG;
    }

    // Methods
    @NotNull
    public String localizeString(@NotNull final String key) {
        try {
            return ModuleLocalization.getInstance().localizeString(this, key);
        } catch (LocalizationModuleException | LocalizationException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
            return ModuleLocalization.LOCALIZATION_ERROR_MESSAGE;
        }
    }
    @Nullable
    public static Localization getLocalizationByLocale(@NotNull final String LOCALE) {
        for (Localization l : Localization.values()) {
            if (l.toString().equalsIgnoreCase(LOCALE)) return l;
        }
        return null;
    }
    @Nullable
    public static Localization getLocalizationByExtendedLang(@NotNull final String EXTENDED_LANG) {
        for (Localization l : Localization.values()) {
            if (l.EXTENDED_LANG.equalsIgnoreCase(EXTENDED_LANG)) return l;
        }
        return null;
    }
    @NotNull
    public static String getLangFilepath(@Nullable final Localization localization) {
        if (localization == null) return getFallbackFilepath();
        return Resource.Localization.LOCALIZATION_DIR + localization.name().toLowerCase() + '.' + LOCALIZATION_FILE_EXTENSION;
    }
    @NotNull
    public static String getFallbackFilepath() {
        return Resource.Localization.LOCALIZATION_DIR + FALLBACK.name().toLowerCase() + '.' + LOCALIZATION_FILE_EXTENSION;
    }
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
