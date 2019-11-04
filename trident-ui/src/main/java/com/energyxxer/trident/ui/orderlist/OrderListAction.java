package com.energyxxer.trident.ui.orderlist;

import java.awt.*;

public interface OrderListAction {
    Image getIcon();
    String getDescription();
    int perform();
}
