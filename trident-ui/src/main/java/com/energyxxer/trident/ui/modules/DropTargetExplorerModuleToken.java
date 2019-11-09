package com.energyxxer.trident.ui.modules;

public interface DropTargetExplorerModuleToken extends ModuleToken {
    boolean canAccept(DraggableExplorerModuleToken[] draggable);
}
