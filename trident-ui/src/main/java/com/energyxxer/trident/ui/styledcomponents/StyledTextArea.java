package com.energyxxer.trident.ui.styledcomponents;

import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.XTextArea;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class StyledTextArea extends XTextArea {

    private String namespace = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public StyledTextArea() {
        this(null,null);
    }

    public StyledTextArea(String text) {
        this(text,null);
    }

    public StyledTextArea(String text, String namespace) {
        if(text != null) this.setText(text);
        if(namespace != null) this.setNamespace(namespace);

        tlm.addThemeChangeListener(t -> {
            if(this.namespace != null) {
                setBackground       (t.getColor(new Color(220, 220, 220), this.namespace + ".textfield.background","General.textfield.background"));
                setForeground       (t.getColor(Color.BLACK, this.namespace + ".textfield.foreground","General.textfield.foreground","General.foreground"));
                setSelectionColor   (t.getColor(new Color(50, 100, 175), this.namespace + ".textfield.selection.background","General.textfield.selection.background"));
                setSelectedTextColor(t.getColor(getForeground(), this.namespace + ".textfield.selection.foreground","General.textfield.selection.foreground"));
                setBorder(t.getColor(new Color(200, 200, 200), this.namespace + ".textfield.border.color","General.textfield.border.color"),Math.max(t.getInteger(1,this.namespace + ".textfield.border.thickness","General.textfield.border.thickness"),0));
                setFont(t.getFont(this.namespace+".textfield","General.textfield","General"));

                setDisabledTextColor(t.getColor(getForeground(), this.namespace + ".textfield.disabled.foreground","General.textfield.disabled.foreground"));
            } else {
                setBackground       (t.getColor(new Color(220, 220, 220), "General.textfield.background"));
                setForeground       (t.getColor(Color.BLACK, "General.textfield.foreground","General.foreground"));
                setSelectionColor   (t.getColor(new Color(50, 100, 175), "General.textfield.selection.background"));
                setSelectedTextColor(t.getColor(getForeground(), "General.textfield.selection.foreground"));
                setBorder(t.getColor(new Color(200, 200, 200), "General.textfield.border.color"),Math.max(t.getInteger(1,"General.textfield.border.thickness"),0));
                setFont(t.getFont("General.textfield","General"));

                setDisabledTextColor(t.getColor(getForeground(), "General.textfield.disabled.foreground"));
            }
        });
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setDefaultSize(Dimension size) {
        this.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        Debug.log("inserted");
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        Debug.log("removed");
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        Debug.log("changed");
                    }
                }
        );
    }
}
