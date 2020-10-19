package com.energyxxer.trident.guardian;

import com.energyxxer.commodore.standard.StandardDefinitionPacks;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.nbtmapper.parser.NBTTMLexerProfile;
import com.energyxxer.nbtmapper.parser.NBTTMProductions;
import com.energyxxer.nbtmapper.parser.NBTTMTokens;

public class NBTTMLanguage extends Lang {
    public static NBTTMLanguage INSTANCE;

    private NBTTMLanguage() {
        super("NBTTM", "NBT Type Map",
                true,
                () -> new NBTTMLexerProfile(StandardDefinitionPacks.MINECRAFT_JAVA_LATEST_SNAPSHOT),
                () -> NBTTMProductions.FILE,
                "nbttm"
        );
        this.putProperty("line_comment_marker","#");
    }

    public static void load() {
        INSTANCE = new NBTTMLanguage();
    }

    @Override
    public boolean isBraceToken(Token token) {
        return token.type == NBTTMTokens.BRACE;
    }

    @Override
    public boolean isStringToken(Token token) {
        return token.type == NBTTMTokens.STRING_LITERAL;
    }
}
