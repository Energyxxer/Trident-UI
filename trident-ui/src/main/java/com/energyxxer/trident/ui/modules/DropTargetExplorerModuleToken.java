package com.energyxxer.trident.ui.modules;

public interface DropTargetExplorerModuleToken extends ModuleToken {
    boolean canAcceptMove(DraggableExplorerModuleToken[] draggable);

    boolean canAcceptCopy(DraggableExplorerModuleToken[] draggables);
}
