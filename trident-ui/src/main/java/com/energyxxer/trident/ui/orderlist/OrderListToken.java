package com.energyxxer.trident.ui.orderlist;

import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.display.DisplayModule;
import com.energyxxer.trident.ui.modules.ModuleToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface OrderListToken extends ModuleToken {
    @Override
    default String getHint() {
        return null;
    }

    @Override
    default Collection<? extends ModuleToken> getSubTokens() {
        return null;
    }

    @Override
    default boolean isExpandable() {
        return false;
    }

    @Override
    default boolean isModuleSource() {
        return false;
    }

    @Override
    default DisplayModule createModule(Tab tab) {
        return null;
    }

    @Override
    default void onInteract() {
    }

    @Override
    default String getIdentifier() {
        return null;
    }

    @NotNull
    List<OrderListAction> getActions();
}
