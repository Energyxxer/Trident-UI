package com.energyxxer.trident.ui.explorer;

import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 5/16/2017.
 */
public class NoticeExplorerMaster extends StyledExplorerMaster {

    public NoticeExplorerMaster() {
        explorerFlags.put(ExplorerFlag.DYNAMIC_ROW_HEIGHT, true);
    }

    public void addNotice(Notice n) {
        this.children.add(new NoticeItem(this, n));
    }

    public void addNoticeGroup(String label, List<Notice> notices) {
        this.children.add(new NoticeGroupElement(this, label, notices));
    }

    public synchronized void setNotices(HashMap<String, ArrayList<Notice>> map) {
        this.children.clear();

        if(map.containsKey(null)) {
            ArrayList<Notice> standalones = map.get(null);
            standalones.forEach(this::addNotice);
        }
        map.keySet().forEach(k -> {
            if(k != null) this.addNoticeGroup(k, map.get(k));
        });

        this.repaint();
    }
}
