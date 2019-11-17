package com.energyxxer.trident.ui.orderlist;

import com.energyxxer.trident.ui.explorer.base.StyleProvider;

import javax.swing.*;

public interface ItemActionHost {
    JComponent getComponent();
    void performOperation(int code);
    StyleProvider getStyleProvider();
}
